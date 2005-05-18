#region Apache Notice
/*****************************************************************************
 * $Header: $
 * $Revision: $
 * $Date: $
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

using System;
using System.Collections;
using System.Text;

using IBatisNet.DataMapper.Configuration.Sql.Dynamic;
using IBatisNet.DataMapper.Scope;
using IBatisNet.DataMapper.TypeHandlers;
using IBatisNet.DataMapper.Exceptions;

using IBatisNet.Common.Utilities;
using IBatisNet.Common.Exceptions;
using IBatisNet.Common.Utilities.Objects;
using IBatisNet.DataMapper.Configuration.Statements;

#endregion 

namespace IBatisNet.DataMapper.Configuration.ParameterMapping
{
	/// <summary>
	/// Summary description for InlineParameterMapParser.
	/// </summary>
	internal class InlineParameterMapParser
	{

		#region Fields

		private const string PARAMETER_TOKEN = "#";
		private const string PARAM_DELIM = ":";

		private ErrorContext _errorContext= null;

		#endregion 

		#region Constructors

		/// <summary>
		/// Constructor
		/// </summary>
		/// <param name="errorContext"></param>
		public InlineParameterMapParser(ErrorContext errorContext)
		{
			_errorContext = errorContext;
		}
		#endregion 

		/// <summary>
		/// Parse Inline ParameterMap
		/// </summary>
		/// <param name="statement"></param>
		/// <param name="sqlStatement"></param>
		/// <returns>A new sql command text.</returns>
		/// <param name="typeHandlerFactory"></param>
		public SqlText ParseInlineParameterMap(TypeHandlerFactory typeHandlerFactory, IStatement statement, string sqlStatement)
		{
			string newSql = sqlStatement;
			ArrayList mappingList = new ArrayList();
			Type parameterClass = null;

			if (statement != null)
			{
				parameterClass = statement.ParameterClass;
			}

			StringTokenizer parser = new StringTokenizer(sqlStatement, PARAMETER_TOKEN, true);
			StringBuilder newSqlBuffer = new StringBuilder();

			string token = null;
			string lastToken = null;

			IEnumerator enumerator = parser.GetEnumerator();

			while (enumerator.MoveNext()) 
			{
				token = (string)enumerator.Current;

				if (PARAMETER_TOKEN.Equals(lastToken)) 
				{
					if (PARAMETER_TOKEN.Equals(token)) 
					{
						newSqlBuffer.Append(PARAMETER_TOKEN);
						token = null;
					} 
					else 
					{
						//ParameterMapping mapping = null; Java
						ParameterProperty mapping =  ParseMapping(token, parameterClass, typeHandlerFactory);

						mappingList.Add(mapping);
						newSqlBuffer.Append("? ");

						enumerator.MoveNext();
						token = (string)enumerator.Current;
						if (!PARAMETER_TOKEN.Equals(token)) 
						{
							throw new DataMapperException("Unterminated inline parameter in mapped statement (" + statement.Id + ").");
						}
						token = null;
					}
				} 
				else 
				{
					if (!PARAMETER_TOKEN.Equals(token)) 
					{
						newSqlBuffer.Append(token);
					}
				}

				lastToken = token;
			}

			newSql = newSqlBuffer.ToString();

			ParameterProperty[] mappingArray = (ParameterProperty[]) mappingList.ToArray(typeof(ParameterProperty));

			SqlText sqlText = new SqlText();
			sqlText.Text = newSql;
			sqlText.Parameters = mappingArray;

			return sqlText;
		}


		private ParameterProperty ParseMapping(string token, Type parameterClass, TypeHandlerFactory typeHandlerFactory) 
		{
			ParameterProperty mapping = new ParameterProperty();

			if (token.IndexOf(PARAM_DELIM) > -1) 
			{
				StringTokenizer paramParser = new StringTokenizer(token, PARAM_DELIM, true);
				IEnumerator enumeratorParam = paramParser.GetEnumerator();

				int n1 = paramParser.TokenNumber;
				if (n1 == 3) 
				{
					enumeratorParam.MoveNext();
					string propertyName = ((string)enumeratorParam.Current).Trim();
					mapping.PropertyName = propertyName;

					enumeratorParam.MoveNext();
					enumeratorParam.MoveNext(); //ignore ":"
					string dBType = ((string)enumeratorParam.Current).Trim();
					mapping.DbType = dBType;

					ITypeHandler handler = null;
					if (parameterClass == null) 
					{
						handler = typeHandlerFactory.GetUnkownTypeHandler();
					} 
					else 
					{
						handler = ResolveTypeHandler(typeHandlerFactory, parameterClass, propertyName, null, dBType);
					}
					mapping.TypeHandler = handler;
					mapping.Initialize(typeHandlerFactory, _errorContext);
				} 
				else if (n1 >= 5) 
				{
					enumeratorParam.MoveNext();
					string propertyName = ((string)enumeratorParam.Current).Trim();
					enumeratorParam.MoveNext();
					enumeratorParam.MoveNext(); //ignore ":"
					string dBType = ((string)enumeratorParam.Current).Trim();
					enumeratorParam.MoveNext();
					enumeratorParam.MoveNext(); //ignore ":"
					string nullValue = ((string)enumeratorParam.Current).Trim();
					while (enumeratorParam.MoveNext()) 
					{
						nullValue = nullValue + ((string)enumeratorParam.Current).Trim();
					}

					mapping.PropertyName = propertyName;
					mapping.DbType = dBType;
					mapping.NullValue = nullValue;
					ITypeHandler handler;
					if (parameterClass == null) 
					{
						handler = typeHandlerFactory.GetUnkownTypeHandler();
					} 
					else 
					{
						handler = ResolveTypeHandler(typeHandlerFactory, parameterClass, propertyName, null, dBType);
					}
					mapping.TypeHandler = handler;
					mapping.Initialize(typeHandlerFactory, _errorContext);
				} 
				else 
				{
					throw new ConfigurationException("Incorrect inline parameter map format: " + token);
				}
			} 
			else 
			{
				mapping.PropertyName = token;
				ITypeHandler handler;
				if (parameterClass == null) 
				{
					handler = typeHandlerFactory.GetUnkownTypeHandler();
				} 
				else 
				{
					handler = ResolveTypeHandler(typeHandlerFactory, parameterClass, token, null, null);
				}
				mapping.TypeHandler = handler;
				mapping.Initialize(typeHandlerFactory, _errorContext);
			}
			return mapping;
		}


		/// <summary>
		/// Resolve TypeHandler
		/// </summary>
		/// <param name="type"></param>
		/// <param name="propertyName"></param>
		/// <param name="propertyType"></param>
		/// <param name="dbType"></param>
		/// <param name="typeHandlerFactory"></param>
		/// <returns></returns>
		private ITypeHandler ResolveTypeHandler(TypeHandlerFactory typeHandlerFactory, 
			Type type, string propertyName, 
			string propertyType, string dbType) 
		{
			ITypeHandler handler = null;

			if (type == null) 
			{
				handler = typeHandlerFactory.GetUnkownTypeHandler();
			} 
			else if (typeof(IDictionary).IsAssignableFrom(type))
			{
				if (propertyType == null || propertyType.Length==0) 
				{
					handler = typeHandlerFactory.GetUnkownTypeHandler();
				} 
				else 
				{
					try 
					{
						Type typeClass = Resources.TypeForName( propertyType );
						handler = typeHandlerFactory.GetTypeHandler(typeClass, dbType);
					} 
					catch (Exception e) 
					{
						throw new ConfigurationException("Error. Could not set TypeHandler.  Cause: " + e.Message, e);
					}
				}
			} 
			else if (typeHandlerFactory.GetTypeHandler(type, dbType) != null) 
			{
				handler = typeHandlerFactory.GetTypeHandler(type, dbType);
			} 
			else 
			{
				Type typeClass = ObjectProbe.GetPropertyTypeForGetter(type, propertyName);
				handler = typeHandlerFactory.GetTypeHandler(typeClass, dbType);
			}

			return handler;
		}

	}
}
