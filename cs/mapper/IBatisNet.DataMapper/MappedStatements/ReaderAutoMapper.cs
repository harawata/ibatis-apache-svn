
#region Apache Notice
/*****************************************************************************
 * $Header: $
 * $Revision: 397590 $
 * $Date: 2006-04-29 11:39:42 +0200 (Sat, 29 Apr 2006) $
 * 
 * iBATIS.NET Data Mapper
 * Copyright (C) 2004 - Gilles Bayon
 *  
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 ********************************************************************************/
#endregion

#region Using

#region Apache Notice
/*****************************************************************************
 * $Revision: 374175 $
 * $LastChangedDate: 2006-04-25 19:40:27 +0200 (mar., 25 avr. 2006) $
 * $LastChangedBy: gbayon $
 * 
 * iBATIS.NET Data Mapper
 * Copyright (C) 2006/2005 - The Apache Software Foundation
 *  
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 ********************************************************************************/
#endregion

using System;
using System.Collections;
using System.Data;
using System.Reflection;
using IBatisNet.Common.Logging;
using IBatisNet.Common.Utilities.Objects;
using IBatisNet.Common.Utilities.Objects.Members;
using IBatisNet.DataMapper.Configuration.ResultMapping;
using IBatisNet.DataMapper.DataExchange;
using IBatisNet.DataMapper.Exceptions;
using IBatisNet.DataMapper.MappedStatements.PropertyStrategy;
using IBatisNet.DataMapper.TypeHandlers;

#endregion

namespace IBatisNet.DataMapper.MappedStatements
{
    /// <summary>
    /// Build a dynamic instance of a ResultMap
    /// </summary>
    public sealed class ReaderAutoMapper 
	{
		private ResultMap _resultMap = null;
        private static readonly ILog _logger = LogManager.GetLogger(MethodBase.GetCurrentMethod().DeclaringType);

        /// <summary>
        /// Initializes a new instance of the <see cref="ReaderAutoMapper"/> class.
        /// </summary>
        /// <param name="dataExchangeFactory">The data exchange factory.</param>
        /// <param name="reader">The reader.</param>
        /// <param name="resultObject">The result object.</param>
		public ReaderAutoMapper(DataExchangeFactory dataExchangeFactory,
		                        IDataReader reader,
			                    ref object resultObject) 
		{
        	Type targetType = resultObject.GetType();
			_resultMap = new ResultMap(dataExchangeFactory);
			_resultMap.DataExchange = dataExchangeFactory.GetDataExchangeForClass( targetType );
			try 
			{
				// Get all PropertyInfo from the resultObject properties
				ReflectionInfo reflectionInfo = ReflectionInfo.GetInstance(targetType);
				string[] membersName = reflectionInfo.GetWriteableMemberNames();

				Hashtable propertyMap = new Hashtable();
				int length = membersName.Length;
				for (int i = 0; i < length; i++) 
				{
                    ISetAccessorFactory setAccessorFactory = dataExchangeFactory.AccessorFactory.SetAccessorFactory;
                    ISetAccessor setAccessor = setAccessorFactory.CreateSetAccessor(targetType, membersName[i]);
                    propertyMap.Add(membersName[i].ToUpper(), setAccessor);
				}

				// Get all column Name from the reader
				// and build a resultMap from with the help of the PropertyInfo[].
				DataTable dataColumn = reader.GetSchemaTable();
				int count = dataColumn.Rows.Count;
				for (int i = 0; i < count; i++) 
				{
					string columnName = dataColumn.Rows[i][0].ToString();
                    ISetAccessor matchedSetAccessor = propertyMap[columnName.ToUpper()] as ISetAccessor;

					ResultProperty property = new ResultProperty();
					property.ColumnName = columnName;
					property.ColumnIndex = i;

					if (resultObject is Hashtable) 
					{
						property.PropertyName = columnName;
						_resultMap.AddResultPropery(property);
					}

					Type propertyType = null;

                    if (matchedSetAccessor == null)
					{
						try
						{
							propertyType = ObjectProbe.GetMemberTypeForSetter(resultObject, columnName);
						}
						catch
						{
							_logger.Error("The column [" + columnName + "] could not be auto mapped to a property on [" + resultObject.ToString() + "]");
						}
					}
					else
					{
                        propertyType = matchedSetAccessor.MemberType;
					}

                    if (propertyType != null || matchedSetAccessor != null) 
					{
                        property.PropertyName = (matchedSetAccessor != null ? matchedSetAccessor.Name : columnName);
                        if (matchedSetAccessor != null)
						{
                            property.Initialize(dataExchangeFactory.TypeHandlerFactory, matchedSetAccessor);
						}
						else
						{
                            property.TypeHandler = dataExchangeFactory.TypeHandlerFactory.GetTypeHandler(propertyType);
						}

						property.PropertyStrategy = PropertyStrategyFactory.Get(property);
						_resultMap.AddResultPropery(property);
					} 
				}
			} 
			catch (Exception e) 
			{
				throw new DataMapperException("Error automapping columns. Cause: " + e.Message, e);
			}
		}


        /// <summary>
        /// AutoMap the the <see cref="IDataReader"/> to the result object.
        /// </summary>
        /// <param name="reader">The reader.</param>
        /// <param name="resultObject">The result object.</param>
		public void AutoMapReader(IDataReader reader, ref object resultObject)
		{
			for(int index=0; index< _resultMap.Properties.Count; index++)

			{
				ResultProperty property = _resultMap.Properties[index];
				_resultMap.SetValueOfProperty( ref resultObject, property, 
					property.GetDataBaseValue( reader ));
			}
		}

	}

}
