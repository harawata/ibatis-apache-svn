
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

#region Using
using System.Data;
using System.Collections;
using System.Text;

using log4net;

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
	internal class DefaultPreparedCommand : IPreparedCommand
	{

		#region Fields
		private static readonly ILog _logger = LogManager.GetLogger( System.Reflection.MethodBase.GetCurrentMethod().DeclaringType );
		#endregion 

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

			if (_logger.IsDebugEnabled)
			{
				_logger.Debug("PreparedStatement : [" + command.CommandText + "]");
			}

			ApplyParameterMap( session, command, request, statement, parameterObject  );

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
			StringBuilder paramLogList = new StringBuilder(); // Log info
			StringBuilder typeLogList = new StringBuilder(); // Log info

			object parameterValue = null;

			for ( int i = 0; i < properties.Count; ++i )
			{
				IDataParameter sqlParameter = (IDataParameter)parameters[i];
				string propertyName = (string)properties[i];
				IDataParameter parameterCopy = command.CreateParameter();
				ParameterProperty property = request.ParameterMap.GetProperty(i);

				#region Logging
				if (_logger.IsDebugEnabled)
				{
					paramLogList.Append(sqlParameter.ParameterName);
					paramLogList.Append("=[");
					typeLogList.Append(sqlParameter.ParameterName);
					typeLogList.Append("=[");
				}
				#endregion

				if (command.CommandType == CommandType.Text)
				{

					#region text command

					if ( propertyName != "value" ) // Inline Parameters && Parameters via ParameterMap
					{

						parameterValue = request.ParameterMap.GetValueOfProperty(parameterObject,
							property.PropertyName);

						#region Logging
						if (_logger.IsDebugEnabled)
						{
							paramLogList.Append( property.PropertyName );
							paramLogList.Append( "," );
						}
						#endregion 
					}
					else // 'value' parameter
					{
						parameterValue = parameterObject;

						#region Logging
						if (_logger.IsDebugEnabled)
						{
							paramLogList.Append( "'value'" );
							paramLogList.Append( "," );
						}
						#endregion 
					}
					#endregion 
				}
				else // CommandType.StoredProcedure
				{
					#region store procedure command

					// A store procedure must always use a ParameterMap 
					// to indicate the mapping order of the properties to the columns
					if (request.ParameterMap == null) // Inline Parameters
					{
						throw new DataMapperException("A procedure statement tag must alway have a parameterMap attribute, which is not the case for the procedure '"+statement.Id+"'."); 
					}
					else // Parameters via ParameterMap
					{
//						ParameterProperty property = request.ParameterMap.GetProperty(i);

						if (property.DirectionAttribute.Length == 0)
						{
							property.Direction = sqlParameter.Direction;
						}

						// DbDataParameter dataParameter = (IDbDataParameter)parameters[i];
						// property.Precision = dataParameter.Precision;
						// property.Scale = dataParameter.Scale;
						// property.Size = dataParameter.Size;

						sqlParameter.Direction = property.Direction;
						parameterValue = request.ParameterMap.GetValueOfProperty( parameterObject, property.PropertyName );
					
						#region Logging
						if (_logger.IsDebugEnabled)
						{
							paramLogList.Append( property.PropertyName );
							paramLogList.Append( "," );
						}
						#endregion 					
					}
					#endregion 
				}


				// Fix JIRA 20
				//parameterCopy.Value = parameterValue;
				property.TypeHandler.SetParameter(property, parameterCopy, parameterValue);

				
				#region Logging
				if (_logger.IsDebugEnabled)
				{
					if (parameterValue == null) 
					{
						paramLogList.Append("null");
						paramLogList.Append( "], " );
						typeLogList.Append("System.DBNull, null");
						typeLogList.Append( "], " );
					} 
					else 
					{ 

						paramLogList.Append( parameterValue.ToString() );
						paramLogList.Append( "], " );
						typeLogList.Append( sqlParameter.DbType.ToString() );
						typeLogList.Append( ", " );
						typeLogList.Append( parameterValue.GetType().ToString() );
						typeLogList.Append( "], " );
					}
				}
				#endregion 

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

				// JIRA-49 Fixes (size, precision, and scale)
				if (session.DataSource.Provider.SetDbParameterSize) 
				{
					if (((IDbDataParameter)sqlParameter).Size > 0) 
					{
						((IDbDataParameter)parameterCopy).Size = ((IDbDataParameter)sqlParameter).Size;
					}
				}

				if (session.DataSource.Provider.SetDbParameterPrecision) 
				{
					((IDbDataParameter)parameterCopy).Precision = ((IDbDataParameter)sqlParameter).Precision;
				}
				
				if (session.DataSource.Provider.SetDbParameterScale) 
				{
					((IDbDataParameter)parameterCopy).Scale = ((IDbDataParameter)sqlParameter).Scale;
				}				

				parameterCopy.ParameterName = sqlParameter.ParameterName;

				command.Parameters.Add( parameterCopy );
			}

			#region Logging

			if (_logger.IsDebugEnabled && properties.Count>0)
			{
				_logger.Debug("Parameters: [" + paramLogList.ToString(0, paramLogList.Length - 2)  + "]");
				_logger.Debug("Types: [" + typeLogList.ToString(0, typeLogList.Length - 2)  + "]");			
			}
			#endregion 
		}

		#endregion
	}
}
