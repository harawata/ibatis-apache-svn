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
using System.Data;
using IBatisNet.DataMapper.Configuration.ResultMapping;
using IBatisNet.DataMapper.Scope;
using IBatisNet.DataMapper.TypeHandlers;

namespace IBatisNet.DataMapper.MappedStatements.PropertyStrategy
{
	/// <summary>
	/// <see cref="IPropertyStrategy"/> implementation when no 'select' or
	/// 'resultMapping' attribute exists on a <see cref="ResultProperty"/>.
	/// </summary>
    public sealed class DefaultStrategy : IPropertyStrategy
	{
		#region IPropertyStrategy members

		///<summary>
		/// Sets value of the specified <see cref="ResultProperty"/> on the target object
		/// when the 'select' and 'resultMap' attributes 
		/// on the <see cref="ResultProperty"/> are empties.
		/// </summary>
		/// <param name="request">The request.</param>
		/// <param name="resultMap">The result map.</param>
		/// <param name="mapping">The ResultProperty.</param>
		/// <param name="target">The target.</param>
		/// <param name="reader">The reader.</param>
		/// <param name="keys">The keys</param>
		public void Set(RequestScope request, ResultMap resultMap, 
			ResultProperty mapping, ref object target, IDataReader reader, object keys)
		{
			if (mapping.TypeHandler == null || 
				mapping.TypeHandler is UnknownTypeHandler) // Find the TypeHandler
			{
				lock(mapping) 
				{
					if (mapping.TypeHandler == null || mapping.TypeHandler is UnknownTypeHandler)
					{
						int columnIndex = 0;
						if (mapping.ColumnIndex == ResultProperty.UNKNOWN_COLUMN_INDEX) 
						{
							columnIndex = reader.GetOrdinal(mapping.ColumnName);
						} 
						else 
						{
							columnIndex = mapping.ColumnIndex;
						}
						Type systemType =((IDataRecord)reader).GetFieldType(columnIndex);

						mapping.TypeHandler = request.DataExchangeFactory.TypeHandlerFactory.GetTypeHandler(systemType);
					}
				}					
			}

			object dataBaseValue = mapping.GetDataBaseValue( reader );
			request.IsRowDataFound = request.IsRowDataFound || (dataBaseValue != null);

			resultMap.SetValueOfProperty( ref target, mapping, dataBaseValue );
		}

		#endregion
	}
}