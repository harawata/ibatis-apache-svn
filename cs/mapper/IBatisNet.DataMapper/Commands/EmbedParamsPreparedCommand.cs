#region Apache Notice

/*****************************************************************************
 * $Header: $
 * $Revision: $
 * $Date: $
 * 
 * iBATIS.NET Data Mapper
 * Copyright (C) 2005 - Gilles Bayon
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

#region Imports

using System;
using System.Data;
using System.Collections;
using IBatisNet.Common;
using IBatisNet.Common.Utilities.Objects;
using IBatisNet.DataMapper.Configuration.Statements;
using IBatisNet.DataMapper.Configuration.ParameterMapping;
using IBatisNet.DataMapper.Exceptions;
using IBatisNet.DataMapper.Scope;

#endregion

namespace IBatisNet.DataMapper.Commands
{
	/// <summary>
	/// Summary description for EmbedParamsPreparedCommand.
	/// </summary>
	public class EmbedParamsPreparedCommand : DefaultPreparedCommand
	{
		#region EmbedParamsPreparedCommand Members

		/// <summary>
		/// 
		/// </summary>
		/// <param name="session"></param>
		/// <param name="command"></param>
		/// <param name="request"></param>
		/// <param name="statement"></param>
		/// <param name="parameterObject"></param>
		protected override void ApplyParameterMap
			(IDalSession session, IDbCommand command,
			 RequestScope request, IStatement statement, object parameterObject)
		{
			ArrayList properties = request.PreparedStatement.DbParametersName;
			ArrayList parameters = request.PreparedStatement.DbParameters;
			object parameterValue = null;

			for (int i = 0; i < properties.Count; ++i)
			{
				IDataParameter sqlParameter = (IDataParameter) parameters[i];
				string propertyName = (string) properties[i];

				if (command.CommandType == CommandType.Text)
				{
					if (propertyName != "value") // Inline Parameters && Parameters via ParameterMap
					{
						ParameterProperty property = request.ParameterMap.GetProperty(i);

						parameterValue = request.ParameterMap.GetValueOfProperty(parameterObject,
							property.PropertyName);
					}
					else // 'value' parameter
					{
						parameterValue = parameterObject;
					}
				}
				else // CommandType.StoredProcedure
				{
					// A store procedure must always use a ParameterMap 
					// to indicate the mapping order of the properties to the columns
					if (request.ParameterMap == null) // Inline Parameters
					{
						throw new DataMapperException("A procedure statement tag must alway have a parameterMap attribut, which is not the case for the procedure '" + statement.Id + "'.");
					}
					else // Parameters via ParameterMap
					{
						ParameterProperty property = request.ParameterMap.GetProperty(i);

						if (property.DirectionAttribut.Length == 0)
						{
							property.Direction = sqlParameter.Direction;
						}

						//						IDbDataParameter dataParameter = (IDbDataParameter)parameters[i];
						//						property.Precision = dataParameter.Precision;
						//						property.Scale = dataParameter.Scale;
						//						property.Size = dataParameter.Size;

						sqlParameter.Direction = property.Direction;
						parameterValue = request.ParameterMap.GetValueOfProperty(parameterObject, property.PropertyName);
					}
				}

				IDataParameter parameterCopy = command.CreateParameter();
				// Fix JIRA 20
				sqlParameter.Value = parameterValue;
				parameterCopy.Value = parameterValue;

				parameterCopy.Direction = sqlParameter.Direction;

				// With a ParameterMap, we could specify the ParameterDbTypeProperty
				if (statement.ParameterMap != null)
				{
					if (request.ParameterMap.GetProperty(i).DbType.Length > 0)
					{
						string dbTypePropertyName = session.DataSource.Provider.ParameterDbTypeProperty;

						ObjectProbe.SetPropertyValue(parameterCopy, dbTypePropertyName, ObjectProbe.GetPropertyValue(sqlParameter, dbTypePropertyName));
					}
					else
					{
						//parameterCopy.DbType = sqlParameter.DbType;
					}
				}
				else
				{
					//parameterCopy.DbType = sqlParameter.DbType;
				}

				((IDbDataParameter) parameterCopy).Size = ((IDbDataParameter) sqlParameter).Size;
				((IDbDataParameter) parameterCopy).Precision = ((IDbDataParameter) sqlParameter).Precision;
				((IDbDataParameter) parameterCopy).Scale = ((IDbDataParameter) sqlParameter).Scale;

				parameterCopy.ParameterName = sqlParameter.ParameterName;

				command.Parameters.Add(parameterCopy);


				// NOTE: 
				// Code from Oleksa Borodie to embed parameter values 
				// into command text/sql statement.
				// NEED TO MERGE WITH ABOVE AFTER INITIAL TESTING
				// TO REMOVE REDUNDANT LOOPING!

				// replace parameter names with parameter values
				// only for parameters with names, parameterMaps will be ignored
				IDataParameter p;
				for (int iCnt = command.Parameters.Count - 1; iCnt >= 0; iCnt--)
				{
					p = (IDataParameter) command.Parameters[iCnt];
					if (p.Direction == ParameterDirection.Input &&
						command.CommandText.IndexOf(p.ParameterName) > 0)
					{
						switch (p.DbType)
						{
							case DbType.String:
							case DbType.AnsiString:
							case
							DbType.AnsiStringFixedLength:
							case DbType.StringFixedLength:
								command.CommandText =
									command.CommandText.Replace(p.ParameterName,
									"\'" + p.Value.ToString().Replace("\'", "\'\'") + "\'");
								break;
							case DbType.Date:
							case DbType.DateTime:
								DateTime v =
									Convert.ToDateTime(p.Value);
								command.CommandText =
									command.CommandText.Replace(p.ParameterName,
									String.Format("\'{0}.{1}.{2} {3}:{4}:{5}.{6}\'", v.Year, v.Month,
									v.Day, v.Hour, v.Minute, v.Second, v.Millisecond));
								//                                                      command.CommandText =
								command.CommandText.Replace(p.ParameterName,
									"\'" + p.Value.ToString() + "\'");
								break;
							case DbType.Double:
							case DbType.Decimal:
							case DbType.Currency:
							case DbType.Single:
								command.CommandText =
									command.CommandText.Replace(p.ParameterName,
									p.Value.ToString().Replace(',', '.'));
								break;
							default:
								command.CommandText =
									command.CommandText.Replace(p.ParameterName, p.Value.ToString());
								break;
						}
						command.Parameters.RemoveAt(iCnt);
					}
				}
			
			
			}
		}

		#endregion
	}
}
