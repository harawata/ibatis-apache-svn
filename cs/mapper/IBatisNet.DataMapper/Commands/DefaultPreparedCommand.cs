
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
	/// Summary description for DefaultPreparedCommand.
	/// </summary>
	public class DefaultPreparedCommand : IPreparedCommand
	{
		#region IPreparedCommand Members

		/// <summary>
		/// Create an IDbCommand for the IDalSession and the current SQL Statement
		/// and fill IDbCommand IDataParameter's with the parameterObject.
		/// </summary>
		/// <param name="request"></param>
		/// <param name="session">The IDalSession</param>
		/// <param name="statement">The IStatement</param>
		/// <param name="parameterObject">
		/// The parameter object that will fill the sql parameter
		/// </param>
		/// <returns>An IDbCommand with all the IDataParameter filled.</returns>
		public IDbCommand Create(RequestScope request, IDalSession session, IStatement statement, object parameterObject )
		{
			// the IDbConnection & the IDbTransaction are assign in the CreateCommand 
			IDbCommand command = session.CreateCommand(statement.CommandType);
				
			command.CommandText = request.PreparedStatement.PreparedSql;

			ApplyParameterMap( session, command, request, statement, parameterObject );

			return command;
		}

		/// <summary>
		/// 
		/// </summary>
		/// <param name="session"></param>
		/// <param name="command"></param>
		/// <param name="request"></param>
		/// <param name="statement"></param>
		/// <param name="parameterObject"></param>
		protected virtual void ApplyParameterMap
			( IDalSession session, IDbCommand command,
			RequestScope request, IStatement statement, object parameterObject )
		{
			ArrayList properties = request.PreparedStatement.DbParametersName;
			ArrayList parameters = request.PreparedStatement.DbParameters;
			object parameterValue = null;

			for ( int i = 0; i < properties.Count; ++i )
			{
				IDataParameter sqlParameter = (IDataParameter)parameters[i];
				string propertyName = (string)properties[i];

				if (command.CommandType == CommandType.Text)
				{
					if ( propertyName != "value" ) // Inline Parameters && Parameters via ParameterMap
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
						throw new DataMapperException("A procedure statement tag must alway have a parameterMap attribut, which is not the case for the procedure '"+statement.Id+"'."); 
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
						parameterValue = request.ParameterMap.GetValueOfProperty( parameterObject, property.PropertyName );
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
					if (request.ParameterMap.GetProperty(i).DbType.Length >0)
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

				((IDbDataParameter)parameterCopy).Size = ((IDbDataParameter)sqlParameter).Size;
				((IDbDataParameter)parameterCopy).Precision = ((IDbDataParameter)sqlParameter).Precision;
				((IDbDataParameter)parameterCopy).Scale = ((IDbDataParameter)sqlParameter).Scale;

				parameterCopy.ParameterName = sqlParameter.ParameterName;

				command.Parameters.Add( parameterCopy );
			}
		}

		#endregion
	}
}
