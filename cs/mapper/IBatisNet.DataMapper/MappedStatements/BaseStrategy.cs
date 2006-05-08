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
using IBatisNet.Common;
using IBatisNet.Common.Utilities.Objects;
using IBatisNet.DataMapper.Commands;
using IBatisNet.DataMapper.Configuration.ResultMapping;
using IBatisNet.DataMapper.Exceptions;
using IBatisNet.DataMapper.Scope;
using IBatisNet.DataMapper.TypeHandlers;

namespace IBatisNet.DataMapper.MappedStatements
{
	/// <summary>
	/// BaseStrategy.
	/// </summary>
	public abstract class BaseStrategy
	{
//		/// <summary>
//		/// Gets the argument value for an argument constructor.
//		/// </summary>
//		/// <param name="request">The request.</param>
//		/// <param name="resultMap">The result map.</param>
//		/// <param name="mapping">The mapping.</param>
//		/// <param name="reader">The reader.</param>
//		/// <returns>The argument value</returns>
//		protected object GetObjectArgument(RequestScope request, ResultMap resultMap, ResultProperty mapping, ref IDataReader reader)
//		{
//			string selectStatement = mapping.Select;
//
//			if (selectStatement.Length == 0 && mapping.NestedResultMap == null)
//			{
//				// If the property is not a 'select' ResultProperty 
//				//                     or a 'resultMap' ResultProperty
//				// We have a 'normal' ResultMap
//
//				#region Not a select statement
//				if (mapping.TypeHandler == null || mapping.TypeHandler is UnknownTypeHandler) // Find the TypeHandler
//				{
//					lock(mapping) 
//					{
//						if (mapping.TypeHandler == null || mapping.TypeHandler is UnknownTypeHandler)
//						{
//							int columnIndex = 0;
//							if (mapping.ColumnIndex == ResultProperty.UNKNOWN_COLUMN_INDEX) 
//							{
//								columnIndex = reader.GetOrdinal(mapping.ColumnName);
//							} 
//							else 
//							{
//								columnIndex = mapping.ColumnIndex;
//							}
//							Type systemType =((IDataRecord)reader).GetFieldType(columnIndex);
//
//							mapping.TypeHandler = request.DataExchangeFactory.TypeHandlerFactory.GetTypeHandler(systemType);
//						}
//					}					
//				}
//
//				object dataBaseValue = mapping.GetDataBaseValue( reader );
//				request.IsRowDataFound = request.IsRowDataFound || (dataBaseValue != null);
//
//				return dataBaseValue;
//
//				#endregion
//			}
//			else if (mapping.NestedResultMap != null) // 'resultMap' ResultProperty
//			{
//				object[] parameters = null;
//				if (mapping.NestedResultMap.Parameters.Count >0)
//				{
//					parameters = new object[resultMap.Parameters.Count];
//					// Fill parameters array
//					for(int index=0; index< mapping.NestedResultMap.Parameters.Count; index++)
//					{
//						ResultProperty property = mapping.NestedResultMap.Parameters[index];
//						parameters[index] = property.GetDataBaseValue( reader );
//						request.IsRowDataFound = request.IsRowDataFound || (parameters[index] != null);
//					}
//				}
//
//				object obj = mapping.NestedResultMap.CreateInstanceOfResult(parameters);
//				if (FillObjectWithReaderAndResultMap(request, reader, mapping.NestedResultMap, obj) == false)
//				{
//					obj = null;
//				}
//
//				return obj;
//			}
//			else //'select' ResultProperty 
//			{
//				// Get the select statement
//				IMappedStatement queryStatement = request.MappedStatement.SqlMap.GetMappedStatement(selectStatement);
//				string paramString = mapping.ColumnName;
//				object keys = null;
//				bool wasNull = false;
//
//				#region Find Key(s)
//				if (paramString.IndexOf(',')>0 || paramString.IndexOf('=')>0) // composite parameters key
//				{
//					IDictionary keyMap = new Hashtable();
//					keys = keyMap;
//					// define which character is seperating fields
//					char[] splitter  = {'=',','};
//
//					string[] paramTab = paramString.Split(splitter);
//					if (paramTab.Length % 2 != 0) 
//					{
//						throw new DataMapperException("Invalid composite key string format in '"+mapping.PropertyName+". It must be: property1=column1,property2=column2,..."); 
//					}
//					IEnumerator enumerator = paramTab.GetEnumerator();
//					while (!wasNull && enumerator.MoveNext()) 
//					{
//						string hashKey = ((string)enumerator.Current).Trim();
//						enumerator.MoveNext();
//						object hashValue = reader.GetValue( reader.GetOrdinal(((string)enumerator.Current).Trim()) );
//
//						keyMap.Add(hashKey, hashValue );
//						wasNull = (hashValue == DBNull.Value);
//					}
//				} 
//				else // single parameter key
//				{
//					keys = reader.GetValue(reader.GetOrdinal(paramString));
//					wasNull = reader.IsDBNull(reader.GetOrdinal(paramString));
//				}
//				#endregion
//
//				if (wasNull) 
//				{
//					// set the value of an object property to null
//					return null;
//				} 
//				else // Collection object or .Net object
//				{
//					// lazyLoading is not permit for argument constructor
//
//					#region Collection object or .NET object
//					
//					if (mapping.MemberType.BaseType == typeof(Array))
//					{
//						reader = DataReaderTransformer.Transforme(reader, request.Session.DataSource.DbProvider);
//						IList values = queryStatement.ExecuteQueryForList(request.Session, keys); 
//
//						Type elementType = mapping.MemberType.GetElementType();
//						Array array = Array.CreateInstance(elementType, values.Count);
//						int count = values.Count;
//						for(int i=0;i<count;i++)
//						{
//							array.SetValue(values[i],i);
//						}
//						return array;
//					}
//						// Check if the object to Map implement 'IList' or is IList type
//						// If yes the ResultProperty is map to a IList object
//					else if ( typeof(IList).IsAssignableFrom(mapping.MemberType) )
//					{
//						if (mapping.MemberType == typeof(IList))
//						{
//							reader = DataReaderTransformer.Transforme(reader, request.Session.DataSource.DbProvider);
//							return queryStatement.ExecuteQueryForList(request.Session, keys); 
//						}
//						else // Strongly typed List
//						{
//							reader = DataReaderTransformer.Transforme(reader, request.Session.DataSource.DbProvider);
//							IFactory factory = request.ObjectFactory.CreateFactory(mapping.MemberType, Type.EmptyTypes);
//							object values = factory.CreateInstance(null);
//							queryStatement.ExecuteQueryForList(request.Session, keys, (IList)values);
//							return values;
//						}
//					}
//#if dotnet2
//                    else if (mapping.MemberType.IsGenericType &&
//                         mapping.MemberType.GetGenericTypeDefinition() == typeof(IList<>)) 
//                    {
//                        reader = DataReaderTransformer.Transforme(reader, request.Session.DataSource.DbProvider);
//
//                        Type[] typeArgs = mapping.MemberType.GetGenericArguments();
//                        Type genericList = typeof(IList<>);
//                        Type constructedType = genericList.MakeGenericType(typeArgs);
//                        Type elementType = mapping.MemberType.GetGenericArguments()[0];
//
//                        Type mappedStatementType = queryStatement.GetType();
//
//                    	Type[] typeArguments = { typeof(IDalSession), typeof(object) };
//
//                        MethodInfo[] mis = mappedStatementType.GetMethods(BindingFlags.InvokeMethod | BindingFlags.Public | BindingFlags.Instance);
//                        MethodInfo mi = null;
//                        foreach (MethodInfo m in mis)
//                        {
//                            if (m.IsGenericMethod &&
//                                m.Name == "ExecuteQueryForList" &&
//                                m.GetParameters().Length == 2)
//                            {
//                                mi = m;
//                                break;
//                            }
//                        }
//
//                    	MethodInfo miConstructed = mi.MakeGenericMethod(elementType);
//
//                        // Invoke the method.
//                        object[] args = { request.Session, keys };
//                        object values = miConstructed.Invoke(queryStatement, args);
//
//                        return values;
//                    }
//#endif
//					else // The ResultProperty is map to a .Net object
//					{
//						reader = DataReaderTransformer.Transforme(reader, request.Session.DataSource.DbProvider);
//						return queryStatement.ExecuteQueryForObject(request.Session, keys);
//					}
//					#endregion
//
//				}
//			}
//		}
//			

		/// <summary>
		/// Fills the object with reader and result map.
		/// </summary>
		/// <param name="request">The request.</param>
		/// <param name="reader">The reader.</param>
		/// <param name="resultMap">The result map.</param>
		/// <param name="resultObject">The result object.</param>
		/// <returns>Indicates if we have found a row.</returns>
		protected bool FillObjectWithReaderAndResultMap(RequestScope request,IDataReader reader, 
		                                                ResultMap resultMap, object resultObject)
		{
			bool dataFound = false;
			
			// For each Property in the ResultMap, set the property in the object 
			for(int index=0; index< resultMap.Properties.Count; index++)
			{
				request.IsRowDataFound = false;
				ResultProperty property = resultMap.Properties[index];
				property.PropertyStrategy.Set(request, resultMap, property, ref resultObject, reader, null);
				dataFound = dataFound || request.IsRowDataFound;
			}

			request.IsRowDataFound = dataFound;
			return dataFound;
		}
	}
}
