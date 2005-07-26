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

using System.Collections;
using IBatisNet.Common;
using IBatisNet.DataMapper.Commands;
using IBatisNet.DataMapper.Configuration.Cache;
using IBatisNet.DataMapper.Configuration.Statements;
using IBatisNet.DataMapper.Scope;
using IBatisNet.DataMapper.MappedStatements;

#endregion 

namespace IBatisNet.DataMapper.MappedStatements
{
	/// <summary>
	/// Summary description for CachingStatement.
	/// </summary>
	public class CachingStatement : IMappedStatement
	{
		private MappedStatement _mappedStatement =null;

		/// <summary>
		/// Event launch on exceute query
		/// </summary>
		public event ExecuteEventHandler Execute;

		/// <summary>
		/// Constructor
		/// </summary>
		/// <param name="statement"></param>
		public CachingStatement(MappedStatement statement) 
		{
			_mappedStatement = statement;
		}

		#region IMappedStatement Members

		/// <summary>
		/// The IPreparedCommand to use
		/// </summary>
		public IPreparedCommand PreparedCommand
		{
			get { return _mappedStatement.PreparedCommand; }
		}

		/// <summary>
		/// Name used to identify the MappedStatement amongst the others.
		/// This the name of the SQL statment by default.
		/// </summary>
		public string Name
		{
			get { return _mappedStatement.Name; }
		}

		/// <summary>
		/// The SQL statment used by this MappedStatement
		/// </summary>
		public IStatement Statement
		{
			get { return _mappedStatement.Statement; }
		}

		/// <summary>
		/// The SqlMap used by this MappedStatement
		/// </summary>
		public SqlMapper SqlMap
		{
			get {return _mappedStatement.SqlMap; }
		}

		/// <summary>
		/// Executes the SQL and retuns all rows selected in a map that is keyed on the property named
		/// in the keyProperty parameter.  The value at each key will be the value of the property specified
		/// in the valueProperty parameter.  If valueProperty is null, the entire result object will be entered.
		/// </summary>
		/// <param name="session">The session used to execute the statement</param>
		/// <param name="parameterObject">The object used to set the parameters in the SQL. </param>
		/// <param name="keyProperty">The property of the result object to be used as the key. </param>
		/// <param name="valueProperty">The property of the result object to be used as the value (or null)</param>
		/// <returns>A hashtable of object containing the rows keyed by keyProperty.</returns>
		///<exception cref="IBatisNet.DataMapper.Exceptions.DataMapperException">If a transaction is not in progress, or the database throws an exception.</exception>
		public IDictionary ExecuteQueryForMap(IDalSession session, object parameterObject, string keyProperty, string valueProperty)
		{
			IDictionary map = new Hashtable();
			RequestScope request = this.Statement.Sql.GetRequestScope(parameterObject, session);;

				CacheKey key = null;
				if (this.Statement.ParameterMap != null) 
				{
					key = new CacheKey(this.SqlMap.TypeHandlerFactory, this.Name, 
						request.PreparedStatement.PreparedSql, 
						parameterObject, 
						request.ParameterMap.GetPropertyNameArray(), 
						MappedStatement.NO_SKIPPED_RESULTS, 
						MappedStatement.NO_MAXIMUM_RESULTS, 
						CacheKeyType.Map);
				} 
				else 
				{
					key = new CacheKey(this.SqlMap.TypeHandlerFactory, this.Name, 
						request.PreparedStatement.PreparedSql,  
						parameterObject, 
						new string[0], 
						MappedStatement.NO_SKIPPED_RESULTS, 
						MappedStatement.NO_MAXIMUM_RESULTS, 
						CacheKeyType.Map);
				}

				map = (IDictionary)this.Statement.CacheModel[key];
				if (map == null) 
				{
					map = _mappedStatement.RunQueryForMap( request, session, parameterObject, keyProperty, valueProperty, null );
					this.Statement.CacheModel[key] = map;
				}

			return map;
		}

		
		
		/// <summary>
		/// Execute an update statement. Also used for delete statement.
		/// Return the number of row effected.
		/// </summary>
		/// <param name="session">The session used to execute the statement.</param>
		/// <param name="parameterObject">The object used to set the parameters in the SQL.</param>
		/// <returns>The number of row effected.</returns>
		public int ExecuteUpdate(IDalSession session, object parameterObject)
		{
			return _mappedStatement.ExecuteUpdate(session, parameterObject);
		}

		/// <summary>
		/// Execute an insert statement. Fill the parameter object with 
		/// the ouput parameters if any, also could return the insert generated key
		/// </summary>
		/// <param name="session">The session</param>
		/// <param name="parameterObject">The parameter object used to fill the statement.</param>
		/// <returns>Can return the insert generated key.</returns>
		public object ExecuteInsert(IDalSession session, object parameterObject)
		{
			return _mappedStatement.ExecuteInsert(session, parameterObject);
		}


		/// <summary>
		/// Executes the SQL and and fill a strongly typed collection.
		/// </summary>
		/// <param name="session">The session used to execute the statement.</param>
		/// <param name="parameterObject">The object used to set the parameters in the SQL.</param>
		/// <param name="resultObject">A strongly typed collection of result objects.</param>
		public void ExecuteQueryForList(IDalSession session, object parameterObject, IList resultObject)
		{
			_mappedStatement.ExecuteQueryForList(session, parameterObject, resultObject);
		}

		/// <summary>
		/// Executes the SQL and retuns a subset of the rows selected.
		/// </summary>
		/// <param name="session">The session used to execute the statement.</param>
		/// <param name="parameterObject">The object used to set the parameters in the SQL.</param>
		/// <param name="skipResults">The number of rows to skip over.</param>
		/// <param name="maxResults">The maximum number of rows to return.</param>
		/// <returns>A List of result objects.</returns>
		public IList ExecuteQueryForList(IDalSession session, object parameterObject, int skipResults, int maxResults)
		{
			IList list = null;
			RequestScope request = this.Statement.Sql.GetRequestScope(parameterObject, session);;

			CacheKey key = null;
			if (this.Statement.ParameterMap != null) 
			{
				key = new CacheKey(this.SqlMap.TypeHandlerFactory, this.Name, 
					request.PreparedStatement.PreparedSql, 
					parameterObject, 
					request.ParameterMap.GetPropertyNameArray(), 
					skipResults, 
					maxResults, 
					CacheKeyType.List);
			} 
			else 
			{
				key = new CacheKey(this.SqlMap.TypeHandlerFactory, this.Name, 
					request.PreparedStatement.PreparedSql,  
					parameterObject, 
					new string[0], 
					skipResults, 
					maxResults, 
					CacheKeyType.List);
			}

			list = (IList)this.Statement.CacheModel[key];
			if (list == null) 
			{
				list = _mappedStatement.RunQueryForList(request, session, parameterObject, skipResults, maxResults, null);
				this.Statement.CacheModel[key] = list;
			}

			return list;
		}

		
		/// <summary>
		/// Executes the SQL and retuns all rows selected. This is exactly the same as
		/// calling ExecuteQueryForList(session, parameterObject, NO_SKIPPED_RESULTS, NO_MAXIMUM_RESULTS).
		/// </summary>
		/// <param name="session">The session used to execute the statement.</param>
		/// <param name="parameterObject">The object used to set the parameters in the SQL.</param>
		/// <returns>A List of result objects.</returns>
		public IList ExecuteQueryForList(IDalSession session, object parameterObject)
		{
			return this.ExecuteQueryForList( session, parameterObject, MappedStatement.NO_SKIPPED_RESULTS, MappedStatement.NO_MAXIMUM_RESULTS);
		}

		/// <summary>
		/// Executes an SQL statement that returns a single row as an Object.
		/// </summary>
		/// <param name="session">The session used to execute the statement.</param>
		/// <param name="parameterObject">The object used to set the parameters in the SQL.</param>
		/// <returns>The object</returns>
		public object ExecuteQueryForObject(IDalSession session, object parameterObject)
		{
			return this.ExecuteQueryForObject(session, parameterObject, null);
		}

		/// <summary>
		/// Executes an SQL statement that returns a single row as an Object of the type of
		/// the resultObject passed in as a parameter.
		/// </summary>
		/// <param name="session">The session used to execute the statement.</param>
		/// <param name="parameterObject">The object used to set the parameters in the SQL.</param>
		/// <param name="resultObject">The result object.</param>
		/// <returns>The object</returns>
		public object ExecuteQueryForObject(IDalSession session, object parameterObject, object resultObject)
		{
			object obj = null;
			RequestScope request = this.Statement.Sql.GetRequestScope(parameterObject, session);;

			CacheKey key = null;
			if (this.Statement.ParameterMap != null) 
			{
				key = new CacheKey(this.SqlMap.TypeHandlerFactory, this.Name, 
					request.PreparedStatement.PreparedSql,
					parameterObject, 
					request.ParameterMap.GetPropertyNameArray(), 
					MappedStatement.NO_SKIPPED_RESULTS, 
					MappedStatement.NO_MAXIMUM_RESULTS, 
					CacheKeyType.Object);
			} 
			else 
			{
				key = new CacheKey(this.SqlMap.TypeHandlerFactory, this.Name, 
					request.PreparedStatement.PreparedSql,
					parameterObject, 
					new string[0], 
					MappedStatement.NO_SKIPPED_RESULTS, 
					MappedStatement.NO_MAXIMUM_RESULTS, 
					CacheKeyType.Object);
			}

			obj = this.Statement.CacheModel[key];
			// check if this query has alreay been run 
			if (obj == CacheModel.NULL_OBJECT) 
			{ 
				// convert the marker object back into a null value 
				obj = null; 
			} 
			else if (obj == null) 
			{
				obj = _mappedStatement.RunQueryForObject(request, session, parameterObject, resultObject);
				this.Statement.CacheModel[key] = obj;
			}

			return obj;
		}

		
		/// <summary>
		/// Runs a query with a custom object that gets a chance 
		/// to deal with each row as it is processed.
		/// </summary>
		/// <param name="session">The session used to execute the statement.</param>
		/// <param name="parameterObject">The object used to set the parameters in the SQL.</param>
		/// <param name="rowDelegate"></param>
		public IList ExecuteQueryForRowDelegate(IDalSession session, object parameterObject, SqlMapper.RowDelegate rowDelegate)
		{
			return _mappedStatement.ExecuteQueryForRowDelegate(session, parameterObject, rowDelegate);
		}

		/// <summary>
		/// Runs a query with a custom object that gets a chance 
		/// to deal with each row as it is processed.
		/// </summary>
		/// <param name="session">The session used to execute the statement</param>
		/// <param name="parameterObject">The object used to set the parameters in the SQL. </param>
		/// <param name="keyProperty">The property of the result object to be used as the key. </param>
		/// <param name="valueProperty">The property of the result object to be used as the value (or null)</param>
		/// <param name="rowDelegate"></param>
		/// <returns>A hashtable of object containing the rows keyed by keyProperty.</returns>
		/// <exception cref="IBatisNet.DataMapper.Exceptions.DataMapperException">If a transaction is not in progress, or the database throws an exception.</exception>
		public IDictionary ExecuteQueryForMapWithRowDelegate(IDalSession session, object parameterObject, string keyProperty, string valueProperty, SqlMapper.DictionaryRowDelegate rowDelegate)
		{
			return _mappedStatement.ExecuteQueryForMapWithRowDelegate(session, parameterObject, keyProperty, valueProperty, rowDelegate);
		}

		#endregion
	}
}
