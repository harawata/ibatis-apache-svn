#region Apache Notice
/*****************************************************************************
 * $Header: $
 * $Revision: 587946 $
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

using System.Collections;
using System.Collections.Generic;
using System.Data;
using System.Diagnostics;
using Apache.Ibatis.DataMapper.Data;
using Apache.Ibatis.DataMapper.Model;
using Apache.Ibatis.DataMapper.Model.Cache;
using Apache.Ibatis.DataMapper.Model.Events;
using Apache.Ibatis.DataMapper.Model.Statements;
using Apache.Ibatis.DataMapper.Scope;
using Apache.Ibatis.DataMapper.Session;

#endregion 

namespace Apache.Ibatis.DataMapper.MappedStatements
{
	/// <summary>
    /// Acts as a decorator arounf an <see cref="IMappedStatement"/> to add cache functionality
	/// </summary>
    [DebuggerDisplay("MappedStatement: {mappedStatement.Id}")]
    public sealed class CachingStatement : MappedStatementEventSupport, IMappedStatement
	{
		private readonly MappedStatement mappedStatement =null;

		/// <summary>
		/// Event launch on exceute query
		/// </summary>
        public event ExecuteEventHandler Execute = delegate { };

		/// <summary>
		/// Constructor
		/// </summary>
		/// <param name="statement"></param>
        public CachingStatement(MappedStatement statement) 
		{
			mappedStatement = statement;
		}

		#region IMappedStatement Members

		/// <summary>
		/// The IPreparedCommand to use
		/// </summary>
		public IPreparedCommand PreparedCommand
		{
			get { return mappedStatement.PreparedCommand; }
		}

		/// <summary>
		/// Name used to identify the MappedStatement amongst the others.
		/// This the name of the SQL statment by default.
		/// </summary>
		public string Id
		{
			get { return mappedStatement.Id; }
		}

		/// <summary>
		/// The SQL statment used by this MappedStatement
		/// </summary>
		public IStatement Statement
		{
			get { return mappedStatement.Statement; }
		}

        /// <summary>
        /// The <see cref="IModelStore"/> used by this MappedStatement
        /// </summary>
        /// <value>The model store.</value>
        public IModelStore ModelStore
		{
            get { return mappedStatement.ModelStore; }
		}

        /// <summary>
        /// Executes an SQL statement that returns DataTable.
        /// </summary>
        /// <param name="session">The session used to execute the statement.</param>
        /// <param name="parameterObject">The object used to set the parameters in the SQL.</param>
        /// <returns>The object</returns>
        public DataTable ExecuteQueryForDataTable(ISession session, object parameterObject)
        {
            return mappedStatement.ExecuteQueryForDataTable(session, parameterObject);
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
		///<exception cref="Apache.Ibatis.DataMapper.Exceptions.DataMapperException">If a transaction is not in progress, or the database throws an exception.</exception>
		public IDictionary ExecuteQueryForMap(ISession session, object parameterObject, string keyProperty, string valueProperty)
		{
			IDictionary map = new Hashtable();

			RequestScope request = Statement.Sql.GetRequestScope(this, parameterObject, session);

			mappedStatement.PreparedCommand.Create( request, session, Statement, parameterObject );

			CacheKey cacheKey = GetCacheKey(request);
			cacheKey.Update("ExecuteQueryForMap");
			if (keyProperty!=null)
			{
				cacheKey.Update(keyProperty);
			}
			if (valueProperty!=null)
			{
				cacheKey.Update(valueProperty);
			}

			map = Statement.CacheModel[cacheKey] as IDictionary;
			if (map == null) 
			{
				map = mappedStatement.RunQueryForMap( request, session, parameterObject, keyProperty, valueProperty, null );
				Statement.CacheModel[cacheKey] = map;
			}

			return map;
		}

        #region ExecuteQueryForMap .NET 2.0
	    
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
        ///<exception cref="Apache.Ibatis.DataMapper.Exceptions.DataMapperException">If a transaction is not in progress, or the database throws an exception.</exception>
        public IDictionary<K, V> ExecuteQueryForDictionary<K, V>(ISession session, object parameterObject, string keyProperty, string valueProperty)
        {
            IDictionary<K, V> map = new Dictionary<K, V>();
            RequestScope request = Statement.Sql.GetRequestScope(this, parameterObject, session);

            mappedStatement.PreparedCommand.Create(request, session, Statement, parameterObject);

            CacheKey cacheKey = GetCacheKey(request);
            cacheKey.Update("ExecuteQueryForMap");
            if (keyProperty != null)
            {
                cacheKey.Update(keyProperty);
            }
            if (valueProperty != null)
            {
                cacheKey.Update(valueProperty);
            }

            map = Statement.CacheModel[cacheKey] as IDictionary<K, V>;
            if (map == null)
            {
                map = mappedStatement.RunQueryForDictionary<K, V>(request, session, parameterObject, keyProperty, valueProperty, null);
                Statement.CacheModel[cacheKey] = map;
            }

            return map;
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
        /// <exception cref="Apache.Ibatis.DataMapper.Exceptions.DataMapperException">If a transaction is not in progress, or the database throws an exception.</exception>
        public IDictionary<K, V> ExecuteQueryForDictionary<K, V>(ISession session, object parameterObject, string keyProperty, string valueProperty, DictionaryRowDelegate<K, V> rowDelegate)
        {
            return mappedStatement.ExecuteQueryForDictionary<K, V>(session, parameterObject, keyProperty, valueProperty, rowDelegate);
        }
        #endregion
        
	    /// <summary>
		/// Execute an update statement. Also used for delete statement.
		/// Return the number of row effected.
		/// </summary>
		/// <param name="session">The session used to execute the statement.</param>
		/// <param name="parameterObject">The object used to set the parameters in the SQL.</param>
		/// <returns>The number of row effected.</returns>
		public int ExecuteUpdate(ISession session, object parameterObject)
		{
			return mappedStatement.ExecuteUpdate(session, parameterObject);
		}

		/// <summary>
		/// Execute an insert statement. Fill the parameter object with 
		/// the ouput parameters if any, also could return the insert generated key
		/// </summary>
		/// <param name="session">The session</param>
		/// <param name="parameterObject">The parameter object used to fill the statement.</param>
		/// <returns>Can return the insert generated key.</returns>
		public object ExecuteInsert(ISession session, object parameterObject)
		{
			return mappedStatement.ExecuteInsert(session, parameterObject);
        }

        #region ExecuteQueryForList

        /// <summary>
		/// Executes the SQL and and fill a strongly typed collection.
		/// </summary>
		/// <param name="session">The session used to execute the statement.</param>
		/// <param name="parameterObject">The object used to set the parameters in the SQL.</param>
		/// <param name="resultObject">A strongly typed collection of result objects.</param>
		public void ExecuteQueryForList(ISession session, object parameterObject, IList resultObject)
		{
			mappedStatement.ExecuteQueryForList(session, parameterObject, resultObject);
		}

        /// <summary>
        /// Executes the SQL and retuns all rows selected. 
        /// </summary>
        /// <param name="session">The session used to execute the statement.</param>
        /// <param name="parameterObject">The object used to set the parameters in the SQL.</param>
        /// <returns>A List of result objects.</returns>
        public IList ExecuteQueryForList(ISession session, object parameterObject)
        {
            IList list = null;

            object param = RaisePreEvent<PreSelectEventArgs>(PreSelectEvent, parameterObject);

            RequestScope request = Statement.Sql.GetRequestScope(this, param, session);

            mappedStatement.PreparedCommand.Create(request, session, Statement, param);

            CacheKey cacheKey = GetCacheKey(request);
            cacheKey.Update("ExecuteQueryForList");

            list = Statement.CacheModel[cacheKey] as IList;
            if (list == null)
            {
                list = mappedStatement.RunQueryForList(request, session, param);
                Statement.CacheModel[cacheKey] = list;
            }

            return RaisePostEvent<IList, PostSelectEventArgs>(PostSelectEvent, param, list);
        }
        #endregion

        #region ExecuteQueryForList .NET 2.0

        /// <summary>
        /// Executes the SQL and and fill a strongly typed collection.
        /// </summary>
        /// <param name="session">The session used to execute the statement.</param>
        /// <param name="parameterObject">The object used to set the parameters in the SQL.</param>
        /// <param name="resultObject">A strongly typed collection of result objects.</param>
        public void ExecuteQueryForList<T>(ISession session, object parameterObject, IList<T> resultObject)
        {
            mappedStatement.ExecuteQueryForList(session, parameterObject, resultObject);
        }

        /// <summary>
        /// Executes the SQL and retuns all rows selected. 
        /// </summary>
        /// <param name="session">The session used to execute the statement.</param>
        /// <param name="parameterObject">The object used to set the parameters in the SQL.</param>
        /// <returns>A List of result objects.</returns>
        public IList<T> ExecuteQueryForList<T>(ISession session, object parameterObject)
        {
            IList<T> list = null;

            object param = RaisePreEvent<PreSelectEventArgs>(PreSelectEvent, parameterObject);

            RequestScope request = Statement.Sql.GetRequestScope(this, param, session);

            mappedStatement.PreparedCommand.Create(request, session, Statement, param);

            CacheKey cacheKey = GetCacheKey(request);
            cacheKey.Update("ExecuteQueryForList");

            list = Statement.CacheModel[cacheKey] as IList<T>;
            if (list == null)
            {
                list = mappedStatement.RunQueryForList<T>(request, session, param);
                Statement.CacheModel[cacheKey] = list;
            }
            return RaisePostEvent<IList<T>, PostSelectEventArgs>(PostSelectEvent, param, list);
        }
        #endregion

        #region ExecuteQueryForObject

		/// <summary>
		/// Executes an SQL statement that returns a single row as an Object of the type of
		/// the resultObject passed in as a parameter.
		/// </summary>
		/// <param name="session">The session used to execute the statement.</param>
		/// <param name="parameterObject">The object used to set the parameters in the SQL.</param>
		/// <param name="resultObject">The result object.</param>
		/// <returns>The object</returns>
		public object ExecuteQueryForObject(ISession session, object parameterObject, object resultObject)
		{
			object obj = null;

            object param = RaisePreEvent<PreSelectEventArgs>(PreSelectEvent, parameterObject);

            RequestScope request = Statement.Sql.GetRequestScope(this, param, session);

            mappedStatement.PreparedCommand.Create(request, session, Statement, param);

			CacheKey cacheKey = GetCacheKey(request);
			cacheKey.Update("ExecuteQueryForObject");

			obj = Statement.CacheModel[cacheKey];
			// check if this query has alreay been run 
			if (obj == CacheModel.NULL_OBJECT) 
			{ 
				// convert the marker object back into a null value 
				obj = null; 
			} 
			else if (obj ==null)
			{
                obj = mappedStatement.RunQueryForObject(request, session, param, resultObject);
				Statement.CacheModel[cacheKey] = obj;
			}

            return RaisePostEvent<object, PostSelectEventArgs>(PostSelectEvent, param, obj);
        }
        
        #endregion

        #region ExecuteQueryForObject .NET 2.0

        /// <summary>
        /// Executes an SQL statement that returns a single row as an Object of the type of
        /// the resultObject passed in as a parameter.
        /// </summary>
        /// <param name="session">The session used to execute the statement.</param>
        /// <param name="parameterObject">The object used to set the parameters in the SQL.</param>
        /// <param name="resultObject">The result object.</param>
        /// <returns>The object</returns>
        public T ExecuteQueryForObject<T>(ISession session, object parameterObject, T resultObject)
        {
            T obj = default(T);

            object param = RaisePreEvent<PreSelectEventArgs>(PreSelectEvent, parameterObject);

            RequestScope request = Statement.Sql.GetRequestScope(this, param, session);

            mappedStatement.PreparedCommand.Create(request, session, Statement, param);

            CacheKey cacheKey = GetCacheKey(request);
            cacheKey.Update("ExecuteQueryForObject");

            object cacheObjet = Statement.CacheModel[cacheKey];
            // check if this query has alreay been run 
            if (cacheObjet is T)
            {
                obj = (T)cacheObjet;
            }
            else if (cacheObjet == CacheModel.NULL_OBJECT)
            {
                // convert the marker object back into a null value 
                obj = default(T);
            }
            else //if ((object)obj == null)
            {
                obj = (T)mappedStatement.RunQueryForObject(request, session, param, resultObject);
                Statement.CacheModel[cacheKey] = obj;
            }

            return RaisePostEvent<T, PostSelectEventArgs>(PostSelectEvent, param, obj);
        }
        
        #endregion

        /// <summary>
		/// Runs a query with a custom object that gets a chance 
		/// to deal with each row as it is processed.
		/// </summary>
		/// <param name="session">The session used to execute the statement.</param>
		/// <param name="parameterObject">The object used to set the parameters in the SQL.</param>
		/// <param name="rowDelegate"></param>
		public IList ExecuteQueryForRowDelegate(ISession session, object parameterObject, RowDelegate rowDelegate)
		{
			return mappedStatement.ExecuteQueryForRowDelegate(session, parameterObject, rowDelegate);
		}

        /// <summary>
        /// Runs a query with a custom object that gets a chance 
        /// to deal with each row as it is processed.
        /// </summary>
        /// <param name="session">The session used to execute the statement.</param>
        /// <param name="parameterObject">The object used to set the parameters in the SQL.</param>
        /// <param name="rowDelegate"></param>
        public IList<T> ExecuteQueryForRowDelegate<T>(ISession session, object parameterObject, RowDelegate<T> rowDelegate)
        {
            return mappedStatement.ExecuteQueryForRowDelegate<T>(session, parameterObject, rowDelegate);
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
		/// <exception cref="Apache.Ibatis.DataMapper.Exceptions.DataMapperException">If a transaction is not in progress, or the database throws an exception.</exception>
		public IDictionary ExecuteQueryForMapWithRowDelegate(ISession session, object parameterObject, string keyProperty, string valueProperty, DictionaryRowDelegate rowDelegate)
		{
			return mappedStatement.ExecuteQueryForMapWithRowDelegate(session, parameterObject, keyProperty, valueProperty, rowDelegate);
		}

		#endregion

		/// <summary>
		/// Gets a percentage of successful cache hits achieved
		/// </summary>
		/// <returns>The percentage of hits (0-1), or -1 if cache is disabled.</returns>
		public double GetDataCacheHitRatio() 
		{
			if (mappedStatement.Statement.CacheModel != null) 
			{
				return mappedStatement.Statement.CacheModel.HitRatio;
			} 
			else 
			{
				return -1;
			}
		}

        /// <summary>
        /// Gets the cache key.
        /// </summary>
        /// <param name="request">The request.</param>
        /// <returns>the cache key</returns>
		private CacheKey GetCacheKey(RequestScope request) 
		{
			CacheKey cacheKey = new CacheKey();
			int count = request.IDbCommand.Parameters.Count;
			for (int i = 0; i < count; i++) 
			{
				IDataParameter dataParameter = (IDataParameter)request.IDbCommand.Parameters[i];
				if (dataParameter.Value != null) 
				{
					cacheKey.Update( dataParameter.Value );
				}
			}
			
			cacheKey.Update(mappedStatement.Id);
            cacheKey.Update(mappedStatement.ModelStore.SessionFactory.DataSource.ConnectionString);
			cacheKey.Update(request.IDbCommand.CommandText);

			CacheModel cacheModel = mappedStatement.Statement.CacheModel;
			if (!cacheModel.IsReadOnly && !cacheModel.IsSerializable) 
			{
				cacheKey.Update(request);
			}
			return cacheKey;
		}
    }
}
