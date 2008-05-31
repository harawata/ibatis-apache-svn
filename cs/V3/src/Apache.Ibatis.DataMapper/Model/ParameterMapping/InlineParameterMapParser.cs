#region Apache Notice
/*****************************************************************************
 * $Header: $
 * $Revision: 408099 $
 * $Date$
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
using System.Collections.Generic;
using System.Text;
using Apache.Ibatis.Common.Exceptions;
using Apache.Ibatis.Common.Utilities;
using Apache.Ibatis.DataMapper.DataExchange;
using Apache.Ibatis.DataMapper.Exceptions;
using Apache.Ibatis.DataMapper.Model.Sql.Dynamic;
using Apache.Ibatis.DataMapper.Model.Statements;

#endregion 

namespace Apache.Ibatis.DataMapper.Model.ParameterMapping
{
	/// <summary>
	/// Builds Paremeter property for Inline Parameter Map.
	/// </summary>
	public sealed class InlineParameterMapParser
	{
		private const string PARAMETER_TOKEN = "#";
		private const string PARAM_DELIM = ":";
        private const string MARK_TOKEN = "?";

        private const string NEW_BEGIN_TOKEN = "@{";
        private const string NEW_END_TOKEN = "}";

        /// <summary>
        /// Parse Inline ParameterMap
        /// </summary>
        /// <param name="dataExchangeFactory">The data exchange factory.</param>
        /// <param name="statement">The statement.</param>
        /// <param name="sqlStatement">The SQL statement.</param>
        /// <returns>A new sql command text.</returns>
        public SqlText ParseInlineParameterMap(DataExchangeFactory dataExchangeFactory, IStatement statement, string sqlStatement)
		{
			string newSql = sqlStatement;
            List<ParameterProperty> mappingList = new List<ParameterProperty>();
			Type parameterClassType = null;

			if (statement != null)
			{
				parameterClassType = statement.ParameterClass;
			}

            if (sqlStatement.Contains(NEW_BEGIN_TOKEN))
            {
                if (newSql != null)
                {
                    string toAnalyse = newSql;
                    int start = toAnalyse.IndexOf(NEW_BEGIN_TOKEN);
                    int end = toAnalyse.IndexOf(NEW_END_TOKEN);
                    StringBuilder newSqlBuffer = new StringBuilder();

                    while (start > -1 && end > start)
                    {
                        string prepend = toAnalyse.Substring(0, start);
                        string append = toAnalyse.Substring(end + NEW_END_TOKEN.Length);
                       
                        //EmailAddress,type=string,dbType=Varchar,nullValue=no_email@provided.com
                        string parameter = toAnalyse.Substring(start + NEW_BEGIN_TOKEN.Length, end - start - NEW_BEGIN_TOKEN.Length);
                        ParameterProperty mapping = NewParseMapping(parameter, parameterClassType, dataExchangeFactory);
                        mappingList.Add(mapping);
                        newSqlBuffer.Append(prepend);
                        newSqlBuffer.Append(MARK_TOKEN);
                        toAnalyse = append;
                        start = toAnalyse.IndexOf(NEW_BEGIN_TOKEN);
                        end = toAnalyse.IndexOf(NEW_END_TOKEN);
                    }
                    newSqlBuffer.Append(toAnalyse);
                    newSql = newSqlBuffer.ToString();
                }
            }
            else
            {
                #region old syntax
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
                        // Double token ## = # 
					    if (PARAMETER_TOKEN.Equals(token)) 
					    {
						    newSqlBuffer.Append(PARAMETER_TOKEN);
						    token = null;
					    } 
					    else 
					    {
						    ParameterProperty mapping = null; 
						    if (token.IndexOf(PARAM_DELIM) > -1) 
						    {
                                mapping = OldParseMapping(token, parameterClassType, dataExchangeFactory);
						    } 
						    else 
						    {
                                mapping = NewParseMapping(token, parameterClassType, dataExchangeFactory);
						    }															 

						    mappingList.Add(mapping);
						    newSqlBuffer.Append(MARK_TOKEN+" ");

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
 
	            #endregion            
            }

			ParameterProperty[] mappingArray =  mappingList.ToArray();                

			SqlText sqlText = new SqlText();
			sqlText.Text = newSql;
			sqlText.Parameters = mappingArray;

			return sqlText;
		}

        /// <summary>
        /// Parse inline parameter with syntax as
        /// #propertyName,type=string,dbype=Varchar,direction=Input,nullValue=N/A,handler=string#
        /// </summary>
        /// <param name="token">The token.</param>
        /// <param name="parameterClassType">Type of the parameter class.</param>
        /// <param name="dataExchangeFactory">The data exchange factory.</param>
        /// <returns></returns>
        /// <example>
        /// #propertyName,type=string,dbype=Varchar,direction=Input,nullValue=N/A,handler=string#
        /// </example>
        private ParameterProperty NewParseMapping(string token, Type parameterClassType, DataExchangeFactory dataExchangeFactory) 
		{
            string propertyName = string.Empty;
            string type = string.Empty;
            string dbType = string.Empty;
            string direction = string.Empty;
            string callBack = string.Empty;
            string nullValue = null;

			StringTokenizer paramParser = new StringTokenizer(token, "=,", false);
			IEnumerator enumeratorParam = paramParser.GetEnumerator();
			enumeratorParam.MoveNext();

            propertyName = ((string)enumeratorParam.Current).Trim();

			while (enumeratorParam.MoveNext()) 
			{
				string field = (string)enumeratorParam.Current;
				if (enumeratorParam.MoveNext()) 
				{
					string value = (string)enumeratorParam.Current;
					if ("type".Equals(field)) 
					{
                        type = value;
					} 
					else if ("dbType".Equals(field)) 
					{
                        dbType = value;
					} 
					else if ("direction".Equals(field)) 
					{
                        direction = value;
					} 
					else if ("nullValue".Equals(field)) 
					{
                        nullValue = value;
					} 
					else if ("handler".Equals(field)) 
					{
                        callBack = value;
					} 
					else 
					{
						throw new DataMapperException("Unrecognized parameter mapping field: '" + field + "' in " + token);
					}
				} 
				else 
				{
					throw new DataMapperException("Incorrect inline parameter map format (missmatched name=value pairs): " + token);
				}
			}

            return new ParameterProperty(
                propertyName,
                string.Empty,
                callBack,
                type,
                dbType,
                direction,
                nullValue,
                0,
                0,
                -1,
                parameterClassType,
                dataExchangeFactory);
		}

        /// <summary>
        /// Parse inline parameter with syntax as
        /// </summary>
        /// <param name="token">The token.</param>
        /// <param name="parameterClassType">Type of the parameter class.</param>
        /// <param name="dataExchangeFactory">The data exchange factory.</param>
        /// <example>
        /// #propertyName:dbType:nullValue#
        /// </example>
        /// <returns></returns>
        private ParameterProperty OldParseMapping(string token, Type parameterClassType, DataExchangeFactory dataExchangeFactory) 
		{
            string propertyName = string.Empty;
            string dbType = string.Empty;
            string nullValue = null;

			if (token.IndexOf(PARAM_DELIM) > -1) 
			{
				StringTokenizer paramParser = new StringTokenizer(token, PARAM_DELIM, true);
				IEnumerator enumeratorParam = paramParser.GetEnumerator();

				int n1 = paramParser.TokenNumber;
				if (n1 == 3) 
				{
					enumeratorParam.MoveNext();
					propertyName = ((string)enumeratorParam.Current).Trim();

					enumeratorParam.MoveNext();
					enumeratorParam.MoveNext(); //ignore ":"
                    dbType = ((string)enumeratorParam.Current).Trim();
				} 
				else if (n1 >= 5) 
				{
					enumeratorParam.MoveNext();
					propertyName = ((string)enumeratorParam.Current).Trim();

					enumeratorParam.MoveNext();
					enumeratorParam.MoveNext(); //ignore ":"
                    dbType = ((string)enumeratorParam.Current).Trim();

					enumeratorParam.MoveNext();
					enumeratorParam.MoveNext(); //ignore ":"
					nullValue = ((string)enumeratorParam.Current).Trim();

					while (enumeratorParam.MoveNext()) 
					{
						nullValue = nullValue + ((string)enumeratorParam.Current).Trim();
					}
				} 
				else 
				{
					throw new ConfigurationException("Incorrect inline parameter map format: " + token);
				}
			} 
			else 
			{
				propertyName = token;
			}

            return new ParameterProperty(
                propertyName,
                string.Empty,
                string.Empty,
                string.Empty,
                dbType,
                string.Empty,
                nullValue,
                0,
                0,
                -1,
                parameterClassType,
                dataExchangeFactory);
		}

	}
}
