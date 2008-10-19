
#region Apache Notice
/*****************************************************************************
 * $Revision: 575902 $
 * $LastChangedDate$
 * $LastChangedBy$
 * 
 * iBATIS.NET Data Mapper
 * Copyright (C) 2008/2005 - The Apache Software Foundation
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
using System.Data;
using Apache.Ibatis.Common.Utilities.Objects;
using Apache.Ibatis.DataMapper.Data;
using Apache.Ibatis.DataMapper.Model;
using Apache.Ibatis.DataMapper.Model.Events;
using Apache.Ibatis.DataMapper.Model.ParameterMapping;
using Apache.Ibatis.DataMapper.Model.Statements;
using Apache.Ibatis.DataMapper.MappedStatements.ResultStrategy;
using Apache.Ibatis.DataMapper.Scope;
using Apache.Ibatis.DataMapper.MappedStatements.PostSelectStrategy;
using Apache.Ibatis.DataMapper.Exceptions;
using Apache.Ibatis.DataMapper.TypeHandlers;
using Apache.Ibatis.DataMapper.Session;
using System.Diagnostics;

#endregion

namespace Apache.Ibatis.DataMapper.MappedStatements
{

    /// <summary>
    /// Base implementation of <see cref="IMappedStatement"/>.
    /// </summary>
    [DebuggerDisplay("MappedStatement: {Id}")]
    public class MappedStatement : MappedStatementEventSupport, IMappedStatement
    {
        /// <summary>
        /// Event launch on execute query
        /// </summary>
        public event EventHandler<ExecuteEventArgs> Execute = delegate { };

        #region Fields
        private readonly IStatement statement = null;
        private readonly IModelStore modelStore = null;
        private readonly IPreparedCommand preparedCommand = null;
        private readonly IResultStrategy resultStrategy = null;
        #endregion

        /// <summary>
        /// Initializes a new instance of the <see cref="MappedStatement"/> class.
        /// </summary>
        /// <param name="modelStore">The model store.</param>
        /// <param name="statement">The statement.</param>
        public MappedStatement(IModelStore modelStore, IStatement statement)
        {
            this.modelStore = modelStore;
            this.statement = statement;
            preparedCommand = new DefaultPreparedCommand();
            resultStrategy = ResultStrategyFactory.Get(this.statement);
        }

        #region IDataMapper Members

        #region properties
        /// <summary>
        /// The IPreparedCommand to use
        /// </summary>
        public IPreparedCommand PreparedCommand
        {
            get { return preparedCommand; }
        }

        /// <summary>
        /// Name used to identify the MappedStatement amongst the others.
        /// This the name of the SQL statement by default.
        /// </summary>
        public string Id
        {
            get { return statement.Id; }
        }

        /// <summary>
        /// The SQL statment used by this MappedStatement
        /// </summary>
        public IStatement Statement
        {
            get { return statement; }
        }

        /// <summary>
        /// The <see cref="IModelStore"/> used by this MappedStatement
        /// </summary>
        /// <value>The model store.</value>
        public IModelStore ModelStore
        {
            get { return modelStore; }
        } 
        #endregion

        #region ExecuteForObject


        /// <summary>
        /// Executes an SQL statement that returns a single row as an Object of the type of
        /// the resultObject passed in as a parameter.
        /// </summary>
        /// <param name="session">The session used to execute the statement.</param>
        /// <param name="parameterObject">The object used to set the parameters in the SQL.</param>
        /// <param name="resultObject">The result object.</param>
        /// <returns>The object</returns>
        public virtual object ExecuteQueryForObject(ISession session, object parameterObject, object resultObject)
        {
            object obj = null;

            object param = RaisePreEvent<PreSelectEventArgs>(PreSelectEvent, parameterObject);

            RequestScope request = statement.Sql.GetRequestScope(this, param, session);
            preparedCommand.Create(request, session, Statement, param);

            obj = RunQueryForObject(request, session, param, resultObject);

            return RaisePostEvent<object, PostSelectEventArgs>(PostSelectEvent, param, obj);
        }

        /// <summary>
        /// Executes an SQL statement that returns a single row as an Object of the type of
        /// the resultObject passed in as a parameter.
        /// </summary>
        /// <param name="request">The request scope.</param>
        /// <param name="session">The session used to execute the statement.</param>
        /// <param name="parameterObject">The object used to set the parameters in the SQL.</param>
        /// <param name="resultObject">The result object.</param>
        /// <returns>The object</returns>
        internal object RunQueryForObject(RequestScope request, ISession session, object parameterObject, object resultObject)
        {
            object result = resultObject;

            using (IDbCommand command = request.IDbCommand)
            {
                IDataReader reader = command.ExecuteReader();
                try
                {
                    while (reader.Read())
                    {
                        object obj = resultStrategy.Process(request, ref reader, resultObject);
                        if (obj != BaseStrategy.SKIP)
                        {
                            result = obj;
                        }
                    }
                }
                finally
                {
                    reader.Close();
                    reader.Dispose();
                }

                ExecuteDelayedLoad(request);

                #region remark
                // If you are using the OleDb data provider (as you are), you need to close the
                // DataReader before output parameters are visible.
                #endregion

                RetrieveOutputParameters(request, session, command, parameterObject);
            }

            RaiseExecuteEvent();

            return result;
        }

        #endregion

        #region ExecuteForObject .NET 2.0

        /// <summary>
        /// Executes an SQL statement that returns a single row as an Object of the type of
        /// the resultObject passed in as a parameter.
        /// </summary>
        /// <param name="session">The session used to execute the statement.</param>
        /// <param name="parameterObject">The object used to set the parameters in the SQL.</param>
        /// <param name="resultObject">The result object.</param>
        /// <returns>The object</returns>
        public virtual T ExecuteQueryForObject<T>(ISession session, object parameterObject, T resultObject)
        {
            T obj = default(T);

            object param = RaisePreEvent<PreSelectEventArgs>(PreSelectEvent, parameterObject);

            RequestScope request = statement.Sql.GetRequestScope(this, param, session);

            preparedCommand.Create(request, session, Statement, param);

            obj = RunQueryForObject<T>(request, session, param, resultObject);

            return RaisePostEvent<T, PostSelectEventArgs>(PostSelectEvent, param, obj);
        }


        /// <summary>
        /// Executes an SQL statement that returns a single row as an Object of the type of
        /// the resultObject passed in as a parameter.
        /// </summary>
        /// <param name="request">The request scope.</param>
        /// <param name="session">The session used to execute the statement.</param>
        /// <param name="parameterObject">The object used to set the parameters in the SQL.</param>
        /// <param name="resultObject">The result object.</param>
        /// <returns>The object</returns>
        internal T RunQueryForObject<T>(RequestScope request, ISession session, object parameterObject, T resultObject)
        {
            T result = resultObject;

            using (IDbCommand command = request.IDbCommand)
            {
                IDataReader reader = command.ExecuteReader();
                try
                {
                    while (reader.Read())
                    {
                        object obj = resultStrategy.Process(request, ref reader, resultObject);
                        if (obj != BaseStrategy.SKIP)
                        {
                            result = (T)obj;
                        }
                    }
                }
                finally
                {
                    reader.Close();
                    reader.Dispose();
                }

                ExecuteDelayedLoad(request);

                #region remark
                // If you are using the OleDb data provider, you need to close the
                // DataReader before output parameters are visible.
                #endregion

                RetrieveOutputParameters(request, session, command, parameterObject);
            }

            RaiseExecuteEvent();

            return result;
        }

        #endregion

        #region ExecuteQueryForList

        
        /// <summary>
        /// Runs a query with a custom object that gets a chance 
        /// to deal with each row as it is processed.
        /// </summary>
        /// <param name="session">The session used to execute the statement.</param>
        /// <param name="parameterObject">The object used to set the parameters in the SQL.</param>
        /// <param name="rowDelegate"></param>
        public virtual IList ExecuteQueryForRowDelegate(ISession session, object parameterObject, RowDelegate rowDelegate)
        {
            object param = RaisePreEvent<PreSelectEventArgs>(PreSelectEvent, parameterObject);

            RequestScope request = statement.Sql.GetRequestScope(this, param, session);

            preparedCommand.Create(request, session, Statement, param);

            if (rowDelegate == null)
            {
                throw new DataMapperException("A null RowDelegate was passed to QueryForRowDelegate.");
            }

            IList list = RunQueryForList(request, session, param, null, rowDelegate);

            return RaisePostEvent<IList, PostSelectEventArgs>(PostSelectEvent, param, list);
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
        ///<exception cref="DataMapperException">If a transaction is not in progress, or the database throws an exception.</exception>
        public virtual IDictionary ExecuteQueryForMapWithRowDelegate(ISession session, object parameterObject, string keyProperty, string valueProperty, DictionaryRowDelegate rowDelegate)
        {
            RequestScope request = statement.Sql.GetRequestScope(this, parameterObject, session);

            if (rowDelegate == null)
            {
                throw new DataMapperException("A null DictionaryRowDelegate was passed to QueryForMapWithRowDelegate.");
            }

            preparedCommand.Create(request, session, Statement, parameterObject);

            return RunQueryForMap(request, session, parameterObject, keyProperty, valueProperty, rowDelegate);
        }


        /// <summary>
        /// Executes the SQL and retuns all rows selected. 
        /// </summary>
        /// <param name="session">The session used to execute the statement.</param>
        /// <param name="parameterObject">The object used to set the parameters in the SQL.</param>
        /// <returns>A List of result objects.</returns>
        public virtual IList ExecuteQueryForList(ISession session, object parameterObject)
        {
            object param = RaisePreEvent<PreSelectEventArgs>(PreSelectEvent, parameterObject);

            RequestScope request = statement.Sql.GetRequestScope(this, param, session);

            preparedCommand.Create(request, session, Statement, param);

            IList list = RunQueryForList(request, session, param);

            return RaisePostEvent<IList, PostSelectEventArgs>(PostSelectEvent, param, list);
        }

        /// <summary>
        /// Executes the SQL and and fill a strongly typed collection.
        /// </summary>
        /// <param name="session">The session used to execute the statement.</param>
        /// <param name="parameterObject">The object used to set the parameters in the SQL.</param>
        /// <param name="resultObject">A strongly typed collection of result objects.</param>
        public virtual void ExecuteQueryForList(ISession session, object parameterObject, IList resultObject)
        {
            object param = RaisePreEvent<PreSelectEventArgs>(PreSelectEvent, parameterObject);

            RequestScope request = statement.Sql.GetRequestScope(this, param, session);

            preparedCommand.Create(request, session, Statement, param);

            RunQueryForList(request, session, param, resultObject, null);

            RaisePostEvent<IList, PostSelectEventArgs>(PostSelectEvent, param, resultObject);
        }

        /// <summary>
        /// Runs the query for list.
        /// </summary>
        /// <param name="request">The request.</param>
        /// <param name="session">The session used to execute the statement.</param>
        /// <param name="parameterObject">The object used to set the parameters in the SQL.</param>
        /// <returns>A List of result objects.</returns>
        internal IList RunQueryForList(RequestScope request, ISession session, object parameterObject)
        {
            IList list = null;
            
            using (IDbCommand command = request.IDbCommand)
            {
                if (statement.ListClass == null)
                {
                    list = new ArrayList();
                }
                else
                {
                    list = statement.CreateInstanceOfListClass();
                }

                IDataReader reader = command.ExecuteReader();

                try
                {
                     do
                     {
                         IList currentList = null;
                         if (request.Statement.ResultsMap.Count == 1)
                         {
                             currentList = list;
                         }
                         else
                         {
                             if (request.CurrentResultMap != null)
                             {
                                 Type genericListType = typeof(List<>).MakeGenericType(new Type[] { request.CurrentResultMap.Class });
                                 currentList = (IList)Activator.CreateInstance(genericListType);
                             }
                             else
                             {
                                 currentList = new ArrayList();
                             }
                             list.Add(currentList);

                         }
                         // Get Results
                         while (reader.Read())
                         {
                             object obj = resultStrategy.Process(request, ref reader, null);
                             if (obj != BaseStrategy.SKIP)
                             {
                                 currentList.Add(obj);
                             }
                         }
                     }
                     while (reader.NextResult());
                }
                finally
                {
                    reader.Close();
                    reader.Dispose();
                }

                ExecuteDelayedLoad(request);

                RetrieveOutputParameters(request, session, command, parameterObject);
            }

            return list;
        }

        /// <summary>
        /// Executes the SQL and retuns a List of result objects.
        /// </summary>
        /// <param name="request">The request scope.</param>
        /// <param name="session">The session used to execute the statement.</param>
        /// <param name="parameterObject">The object used to set the parameters in the SQL.</param>
        /// <param name="resultObject">A strongly typed collection of result objects.</param>
        /// <param name="rowDelegate"></param>
        /// <returns>A List of result objects.</returns>
        internal IList RunQueryForList(RequestScope request, ISession session, object parameterObject, IList resultObject, RowDelegate rowDelegate)
        {
            IList list = resultObject;

            using (IDbCommand command = request.IDbCommand)
            {
                if (resultObject==null)
                {
                    if (statement.ListClass == null)
                    {
                        list = new ArrayList();
                    }
                    else
                    {
                        list = statement.CreateInstanceOfListClass();
                    }
                }

                IDataReader reader = command.ExecuteReader();

                try
                { 
                    do
                    {
                        if (rowDelegate == null)
                        {
                            //***
                            IList currentList = null;
                            if (request.Statement.ResultsMap.Count == 1)
                            {
                                currentList = list;
                            }
                            else
                            {
                                if (request.CurrentResultMap != null)
                                {
                                    Type genericListType = typeof(List<>).MakeGenericType(new Type[] { request.CurrentResultMap.Class });
                                    currentList = (IList)Activator.CreateInstance(genericListType);
                                }
                                else
                                {
                                    currentList = new ArrayList();
                                }
                                list.Add(currentList);

                            }
                            //***
                            while (reader.Read())
                            {
                                object obj = resultStrategy.Process(request, ref reader, null);
                                if (obj != BaseStrategy.SKIP)
                                {
                                    //list.Add(obj);
                                    currentList.Add(obj);
                                }
                            }
                        }
                        else
                        {
                            while (reader.Read())
                            {
                                object obj = resultStrategy.Process(request, ref reader, null);
                                rowDelegate(obj, parameterObject, list);
                            }
                        }
                    }
                    while (reader.NextResult());
                }
                finally
                {
                    reader.Close();
                    reader.Dispose();
                }

                ExecuteDelayedLoad(request);
                RetrieveOutputParameters(request, session, command, parameterObject);
            }

            return list;
        }


        #endregion

        #region ExecuteQueryForList .NET 2.0

        /// <summary>
        /// Runs a query with a custom object that gets a chance 
        /// to deal with each row as it is processed.
        /// </summary>
        /// <param name="session">The session used to execute the statement.</param>
        /// <param name="parameterObject">The object used to set the parameters in the SQL.</param>
        /// <param name="rowDelegate"></param>
        public virtual IList<T> ExecuteQueryForRowDelegate<T>(ISession session, object parameterObject, RowDelegate<T> rowDelegate)
        {
            object param = RaisePreEvent<PreSelectEventArgs>(PreSelectEvent, parameterObject);

            RequestScope request = statement.Sql.GetRequestScope(this, param, session);

            preparedCommand.Create(request, session, Statement, param);

            if (rowDelegate == null)
            {
                throw new DataMapperException("A null RowDelegate was passed to QueryForRowDelegate.");
            }
            IList<T> list = RunQueryForList<T>(request, session, param, null, rowDelegate);

            return RaisePostEvent<IList<T>, PostSelectEventArgs>(PostSelectEvent, param, list);
        }


        /// <summary>
        /// Executes the SQL and retuns all rows selected. 
        /// </summary>
        /// <param name="session">The session used to execute the statement.</param>
        /// <param name="parameterObject">The object used to set the parameters in the SQL.</param>
        /// <returns>A List of result objects.</returns>
        public virtual IList<T> ExecuteQueryForList<T>(ISession session, object parameterObject)
        {
            object param = RaisePreEvent<PreSelectEventArgs>(PreSelectEvent, parameterObject);

            RequestScope request = statement.Sql.GetRequestScope(this, param, session);

            preparedCommand.Create(request, session, Statement, param);

            IList<T> list = RunQueryForList<T>(request, session, param, null, null);

            return RaisePostEvent<IList<T>, PostSelectEventArgs>(PostSelectEvent, param, list);
        }

        /// <summary>
        /// Executes the SQL and and fill a strongly typed collection.
        /// </summary>
        /// <param name="session">The session used to execute the statement.</param>
        /// <param name="parameterObject">The object used to set the parameters in the SQL.</param>
        /// <param name="resultObject">A strongly typed collection of result objects.</param>
        public virtual void ExecuteQueryForList<T>(ISession session, object parameterObject, IList<T> resultObject)
        {
            object param = RaisePreEvent<PreSelectEventArgs>(PreSelectEvent, parameterObject);

            RequestScope request = statement.Sql.GetRequestScope(this, param, session);

            preparedCommand.Create(request, session, Statement, param);

            RunQueryForList<T>(request, session, param, resultObject, null);

            RaisePostEvent<IList<T>, PostSelectEventArgs>(PostSelectEvent, param, resultObject);
        }

        /// <summary>
        /// Executes the SQL and retuns a List of result objects.
        /// </summary>
        /// <param name="request">The request scope.</param>
        /// <param name="session">The session used to execute the statement.</param>
        /// <param name="parameterObject">The object used to set the parameters in the SQL.</param>
        /// <returns>A List of result objects.</returns>
        internal IList<T> RunQueryForList<T>(RequestScope request, ISession session, object parameterObject)
        {
            IList<T> list = null;

            using (IDbCommand command = request.IDbCommand)
            {
                if (statement.ListClass == null)
                {
                    list = new List<T>();
                }
                else
                {
                    list = statement.CreateInstanceOfGenericListClass<T>();
                }

                IDataReader reader = command.ExecuteReader();
                try
                {
                    do
                    {
                        while (reader.Read())
                        {
                            object obj = resultStrategy.Process(request, ref reader, null);
                            if (obj != BaseStrategy.SKIP)
                            {
                                list.Add((T) obj);
                            }
                        }
                    }
                    while (reader.NextResult());
                }
                finally
                {
                    reader.Close();
                    reader.Dispose();
                }

                ExecuteDelayedLoad(request);

                RetrieveOutputParameters(request, session, command, parameterObject);
            }

            return list;
        }

        /// <summary>
        /// Executes the SQL and retuns a List of result objects.
        /// </summary>
        /// <param name="request">The request scope.</param>
        /// <param name="session">The session used to execute the statement.</param>
        /// <param name="parameterObject">The object used to set the parameters in the SQL.</param>
        /// <param name="resultObject">The result object</param>
        /// <param name="rowDelegate"></param>
        /// <returns>A List of result objects.</returns>
        internal IList<T> RunQueryForList<T>(RequestScope request, ISession session, 
                                             object parameterObject, IList<T> resultObject, RowDelegate<T> rowDelegate)
        {
            IList<T> list = resultObject;

            using (IDbCommand command = request.IDbCommand)
            {
                if (resultObject == null)
                {
                    if (statement.ListClass == null)
                    {
                        list = new List<T>();
                    }
                    else
                    {
                        list = statement.CreateInstanceOfGenericListClass<T>();
                    }
                }

                IDataReader reader = command.ExecuteReader();
                try
                {
                    do
                    {
                        if (rowDelegate == null)
                        {
                            while (reader.Read())
                            {
                                object obj = resultStrategy.Process(request, ref reader, null);
                                if (obj != BaseStrategy.SKIP)
                                {
                                    list.Add((T)obj);
                                }
                            }
                        }
                        else
                        {
                            while (reader.Read())
                            {
                                T obj = (T)resultStrategy.Process(request, ref reader, null);
                                rowDelegate(obj, parameterObject, list);
                            }
                        }
                    }
                    while (reader.NextResult());
                }
                finally
                {
                    reader.Close();
                    reader.Dispose();
                }

                ExecuteDelayedLoad(request);
                RetrieveOutputParameters(request, session, command, parameterObject);
            }

            return list;
        }

        #endregion

        #region ExecuteUpdate, ExecuteInsert

        /// <summary>
        /// Execute an update statement. Also used for delete statement.
        /// Return the number of row effected.
        /// </summary>
        /// <param name="session">The session used to execute the statement.</param>
        /// <param name="parameterObject">The object used to set the parameters in the SQL.</param>
        /// <returns>The number of row effected.</returns>
        public virtual int ExecuteUpdate(ISession session, object parameterObject)
        {
            int rows = 0; // the number of rows affected

            object param = RaisePreEvent<PreUpdateOrDeleteEventArgs>(PreUpdateOrDeleteEvent, parameterObject);

            RequestScope request = statement.Sql.GetRequestScope(this, param, session);

            preparedCommand.Create(request, session, Statement, param);

            using (IDbCommand command = request.IDbCommand)
            {
                rows = command.ExecuteNonQuery();

                RetrieveOutputParameters(request, session, command, param);
            }

            RaiseExecuteEvent();

            return RaisePostEvent<int, PostUpdateOrDeleteEventArgs>(PostUpdateOrDeleteEvent, param, rows);
        }


        /// <summary>
        /// Execute an insert statement. Fill the parameter object with 
        /// the ouput parameters if any, also could return the insert generated key
        /// </summary>
        /// <param name="session">The session</param>
        /// <param name="parameterObject">The parameter object used to fill the statement.</param>
        /// <returns>Can return the insert generated key.</returns>
        public virtual object ExecuteInsert(ISession session, object parameterObject)
        {
            object generatedKey = null;
            SelectKey selectKeyStatement = null;

            object param = RaisePreEvent<PreInsertEventArgs>(PreInsertEvent, parameterObject);

            RequestScope request = statement.Sql.GetRequestScope(this, param, session);

            if (statement is Insert)
            {
                selectKeyStatement = ((Insert)statement).SelectKey;
            }

            if (selectKeyStatement != null && !selectKeyStatement.isAfter)
            {
                IMappedStatement mappedStatement = modelStore.GetMappedStatement(selectKeyStatement.Id);
                generatedKey = mappedStatement.ExecuteQueryForObject(session, param, null);

                ObjectProbe.SetMemberValue(param, selectKeyStatement.PropertyName, generatedKey,
                    request.DataExchangeFactory.ObjectFactory,
                    request.DataExchangeFactory.AccessorFactory);
            }

            preparedCommand.Create(request, session, Statement, param);

            using (IDbCommand command = request.IDbCommand)
            {
                if (statement is Insert)
                {
                    command.ExecuteNonQuery();
                }
                // Retrieve output parameter if the result class is specified
                else if (statement is Procedure && (statement.ResultClass != null) &&
                        modelStore.DataExchangeFactory.TypeHandlerFactory.IsSimpleType(statement.ResultClass))
                {
                    IDataParameter returnValueParameter = command.CreateParameter();
                    returnValueParameter.Direction = ParameterDirection.ReturnValue;
                    command.Parameters.Add(returnValueParameter);

                    command.ExecuteNonQuery();
                    generatedKey = returnValueParameter.Value;

                    ITypeHandler typeHandler = modelStore.DataExchangeFactory.TypeHandlerFactory.GetTypeHandler(statement.ResultClass);
                    generatedKey = typeHandler.GetDataBaseValue(generatedKey, statement.ResultClass);
                }
                else
                {
                    generatedKey = command.ExecuteScalar();
                    if ((statement.ResultClass != null) &&
                        modelStore.DataExchangeFactory.TypeHandlerFactory.IsSimpleType(statement.ResultClass))
                    {
                        ITypeHandler typeHandler = modelStore.DataExchangeFactory.TypeHandlerFactory.GetTypeHandler(statement.ResultClass);
                        generatedKey = typeHandler.GetDataBaseValue(generatedKey, statement.ResultClass);
                    }
                }

                if (selectKeyStatement != null && selectKeyStatement.isAfter)
                {
                    IMappedStatement mappedStatement = modelStore.GetMappedStatement(selectKeyStatement.Id);
                    generatedKey = mappedStatement.ExecuteQueryForObject(session, param, null);

                    ObjectProbe.SetMemberValue(param, selectKeyStatement.PropertyName, generatedKey,
                        request.DataExchangeFactory.ObjectFactory,
                        request.DataExchangeFactory.AccessorFactory);
                }

                RetrieveOutputParameters(request, session, command, param);
            }

            RaiseExecuteEvent();

            return RaisePostEvent<object, PostInsertEventArgs>(PostInsertEvent, param, generatedKey);
        }

        #endregion

        #region ExecuteQueryForMap

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
        ///<exception cref="DataMapperException">If a transaction is not in progress, or the database throws an exception.</exception>
        public virtual IDictionary ExecuteQueryForMap(ISession session, object parameterObject, string keyProperty, string valueProperty)
        {
            RequestScope request = statement.Sql.GetRequestScope(this, parameterObject, session);

            preparedCommand.Create(request, session, Statement, parameterObject);

            return RunQueryForMap(request, session, parameterObject, keyProperty, valueProperty, null);
        }

       
        /// <summary>
        /// Executes the SQL and retuns all rows selected in a map that is keyed on the property named
        /// in the keyProperty parameter.  The value at each key will be the value of the property specified
        /// in the valueProperty parameter.  If valueProperty is null, the entire result object will be entered.
        /// </summary>
        /// <param name="request">The request scope.</param>
        /// <param name="session">The session used to execute the statement</param>
        /// <param name="parameterObject">The object used to set the parameters in the SQL.</param>
        /// <param name="keyProperty">The property of the result object to be used as the key.</param>
        /// <param name="valueProperty">The property of the result object to be used as the value (or null)</param>
        /// <param name="rowDelegate">A delegate called once per row in the QueryForMapWithRowDelegate method</param>
        /// <returns>A hashtable of object containing the rows keyed by keyProperty.</returns>
        ///<exception cref="DataMapperException">If a transaction is not in progress, or the database throws an exception.</exception>
        internal IDictionary RunQueryForMap(RequestScope request,
            ISession session,
            object parameterObject,
            string keyProperty,
            string valueProperty,
            DictionaryRowDelegate rowDelegate)
        {
            IDictionary map = new Hashtable();

            using (IDbCommand command = request.IDbCommand)
            {
               IDataReader reader = command.ExecuteReader();
               try
                {
                    
                   if (rowDelegate == null)
                    {
                        while (reader.Read())
                        {
                            object obj = resultStrategy.Process(request, ref reader, null);
                            object key = ObjectProbe.GetMemberValue(obj, keyProperty, request.DataExchangeFactory.AccessorFactory);
                            object value = obj;
                            if (valueProperty != null)
                            {
                                value = ObjectProbe.GetMemberValue(obj, valueProperty, request.DataExchangeFactory.AccessorFactory);
                            }
                            map.Add(key, value);
                        }
                    }
                    else
                    {
                        while (reader.Read())
                        {
                            object obj = resultStrategy.Process(request, ref reader, null);
                            object key = ObjectProbe.GetMemberValue(obj, keyProperty, request.DataExchangeFactory.AccessorFactory);
                            object value = obj;
                            if (valueProperty != null)
                            {
                                value = ObjectProbe.GetMemberValue(obj, valueProperty, request.DataExchangeFactory.AccessorFactory);
                            }
                            rowDelegate(key, value, parameterObject, map);

                        }
                    }
                }
                finally
                {
                    reader.Close();
                    reader.Dispose();
                }
                ExecuteDelayedLoad(request);
            }
            return map;

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
        /// <returns>A IDictionary of object containing the rows keyed by keyProperty.</returns>
        ///<exception cref="Apache.Ibatis.DataMapper.Exceptions.DataMapperException">If a transaction is not in progress, or the database throws an exception.</exception>
        public virtual IDictionary<K, V> ExecuteQueryForDictionary<K, V>(ISession session, object parameterObject, string keyProperty, string valueProperty)
        {
            RequestScope request = statement.Sql.GetRequestScope(this, parameterObject, session);

            preparedCommand.Create(request, session, Statement, parameterObject);

            return RunQueryForDictionary<K, V>(request, session, parameterObject, keyProperty, valueProperty, null);

        }

        /// <summary>
        /// Runs a query with a custom object that gets a chance 
        /// to deal with each row as it is processed.
        /// </summary>
        /// <param name="session">The session used to execute the statement</param>
        /// <param name="parameterObject">The object used to set the parameters in the SQL. </param>
        /// <param name="keyProperty">The property of the result object to be used as the key. </param>
        /// <param name="valueProperty">The property of the result object to be used as the value (or null)</param>
        /// <param name="rowDelegate">A delegate called once per row in the QueryForDictionary method</param>
        /// <returns>A hashtable of object containing the rows keyed by keyProperty.</returns>
        ///<exception cref="DataMapperException">If a transaction is not in progress, or the database throws an exception.</exception>
        public virtual IDictionary<K, V> ExecuteQueryForDictionary<K, V>(ISession session, object parameterObject, string keyProperty, string valueProperty, DictionaryRowDelegate<K, V> rowDelegate)
        {
            RequestScope request = statement.Sql.GetRequestScope(this, parameterObject, session);

            if (rowDelegate == null)
            {
                throw new DataMapperException("A null DictionaryRowDelegate was passed to QueryForDictionary.");
            }

            preparedCommand.Create(request, session, Statement, parameterObject);

            return RunQueryForDictionary<K, V>(request, session, parameterObject, keyProperty, valueProperty, rowDelegate);
        }

        /// <summary>
        /// Executes the SQL and retuns all rows selected in a map that is keyed on the property named
        /// in the keyProperty parameter.  The value at each key will be the value of the property specified
        /// in the valueProperty parameter.  If valueProperty is null, the entire result object will be entered.
        /// </summary>
        /// <param name="request">The request scope.</param>
        /// <param name="session">The session used to execute the statement</param>
        /// <param name="parameterObject">The object used to set the parameters in the SQL.</param>
        /// <param name="keyProperty">The property of the result object to be used as the key.</param>
        /// <param name="valueProperty">The property of the result object to be used as the value (or null)</param>
        /// <param name="rowDelegate">A delegate called once per row in the QueryForMapWithRowDelegate method</param>
        /// <returns>A IDictionary of object containing the rows keyed by keyProperty.</returns>
        ///<exception cref="DataMapperException">If a transaction is not in progress, or the database throws an exception.</exception>
        internal IDictionary<K, V> RunQueryForDictionary<K, V>(RequestScope request,
            ISession session,
            object parameterObject,
            string keyProperty,
            string valueProperty,
            DictionaryRowDelegate<K, V> rowDelegate)
        {
            IDictionary<K, V> map = new Dictionary<K, V>();

            using (IDbCommand command = request.IDbCommand)
            {
                IDataReader reader = command.ExecuteReader();
                try
                {

                    if (rowDelegate == null)
                    {
                        while (reader.Read())
                        {
                            object obj = resultStrategy.Process(request, ref reader, null);
                            K key = (K)ObjectProbe.GetMemberValue(obj, keyProperty, request.DataExchangeFactory.AccessorFactory);
                            V value = default(V);
                            if (valueProperty != null)
                            {
                                value = (V)ObjectProbe.GetMemberValue(obj, valueProperty, request.DataExchangeFactory.AccessorFactory);
                            }
                            else
                            {
                                value = (V)obj;
                            }
                            map.Add(key, value);
                        }
                    }
                    else
                    {
                        while (reader.Read())
                        {
                            object obj = resultStrategy.Process(request, ref reader, null);
                            K key = (K)ObjectProbe.GetMemberValue(obj, keyProperty, request.DataExchangeFactory.AccessorFactory);
                            V value = default(V);
                            if (valueProperty != null)
                            {
                                value = (V)ObjectProbe.GetMemberValue(obj, valueProperty, request.DataExchangeFactory.AccessorFactory);
                            }
                            else
                            {
                                value = (V)obj;
                            }
                            rowDelegate(key, value, parameterObject, map);
                        }
                    }
                }
                finally
                {
                    reader.Close();
                    reader.Dispose();
                }
                ExecuteDelayedLoad(request);
            }
            return map;

        }
        
        #endregion

        #region ExecuteQueryForDataTable
        /// <summary>
        /// Executes an SQL statement that returns DataTable.
        /// </summary>
        /// <param name="session">The session used to execute the statement.</param>
        /// <param name="parameterObject">The object used to set the parameters in the SQL.</param>
        /// <returns>The object</returns>
        public virtual DataTable ExecuteQueryForDataTable(ISession session, object parameterObject)
        {
            DataTable dataTable = null;

            object param = RaisePreEvent<PreSelectEventArgs>(PreSelectEvent, parameterObject);

            RequestScope request = statement.Sql.GetRequestScope(this, param, session);
            preparedCommand.Create(request, session, Statement, param);

            dataTable = RunQueryForForDataTable(request, session, param);

            return RaisePostEvent<DataTable, PostSelectEventArgs>(PostSelectEvent, param, dataTable);
        }

        /// <summary>
        /// Runs the query for for data table.
        /// </summary>
        /// <param name="request">The request.</param>
        /// <param name="session">The session.</param>
        /// <param name="parameterObject">The parameter object.</param>
        /// <returns></returns>
        internal DataTable RunQueryForForDataTable(RequestScope request, ISession session, object parameterObject)
        {
            DataTable dataTable = new DataTable("DataTable");

            using (IDbCommand command = request.IDbCommand)
            {
                IDataReader reader = command.ExecuteReader();

                try
                {
                    // Get Results
                    while (reader.Read())
                    {
                        DataRow dataRow = dataTable.NewRow();
                        dataTable.Rows.Add(dataRow);
                        resultStrategy.Process(request, ref reader, dataRow);
                    }
                }
                finally
                {
                    reader.Close();
                    reader.Dispose();
                }

                // do we need ??
                //ExecuteDelayedLoad(request);

                // do we need ??
                //RetrieveOutputParameters(request, session, command, parameterObject);
            }

            return dataTable;
        } 
        #endregion

        #endregion

       /// <summary>
        /// Retrieve the output parameter and map them on the result object.
        /// This routine is only use is you specified a ParameterMap and some output attribute
        /// or if you use a store procedure with output parameter...
        /// </summary>
        /// <param name="request"></param>
        /// <param name="session">The current session.</param>
        /// <param name="result">The result object.</param>
        /// <param name="command">The command sql.</param>
        private static void RetrieveOutputParameters(RequestScope request, ISession session, IDbCommand command, object result)
        {
            if (request.ParameterMap != null && request.ParameterMap.HasOutputParameter)
            {
                int count = request.ParameterMap.PropertiesList.Count;
                for (int i = 0; i < count; i++)
                {
                    ParameterProperty mapping = request.ParameterMap.GetProperty(i);
                    if (mapping.Direction == ParameterDirection.Output ||
                        mapping.Direction == ParameterDirection.InputOutput)
                    {
                        string parameterName = string.Empty;
                        if (session.SessionFactory.DataSource.DbProvider.UseParameterPrefixInParameter == false)
                        {
                            parameterName = mapping.ColumnName;
                        }
                        else
                        {
                            parameterName = session.SessionFactory.DataSource.DbProvider.ParameterPrefix +
                                mapping.ColumnName;
                        }

                        if (mapping.TypeHandler == null) // Find the TypeHandler
                        {
                            lock (mapping)
                            {
                                if (mapping.TypeHandler == null)
                                {
                                    Type propertyType = ObjectProbe.GetMemberTypeForGetter(result, mapping.PropertyName);

                                    mapping.TypeHandler = request.DataExchangeFactory.TypeHandlerFactory.GetTypeHandler(propertyType);
                                }
                            }
                        }

                        // Fix IBATISNET-239
                        //"Normalize" System.DBNull parameters
                        IDataParameter dataParameter = (IDataParameter)command.Parameters[parameterName];
                        object dbValue = dataParameter.Value;

                        object value = null;

                        bool wasNull = (dbValue == DBNull.Value);
                        if (wasNull)
                        {
                            if (mapping.HasNullValue)
                            {
                               value = mapping.TypeHandler.ValueOf(mapping.GetAccessor.MemberType, mapping.NullValue);
                            }
                            else
                            {
                                value = mapping.TypeHandler.NullValue;
                            }
                        }
                        else
                        {
                            value = mapping.TypeHandler.GetDataBaseValue(dataParameter.Value, result.GetType());
                        }

                        request.IsRowDataFound = request.IsRowDataFound || (value != null);

                        request.ParameterMap.SetOutputParameter(ref result, mapping, value);
                    }
                }
            }
        }

        /// <summary>
        /// Executes the <see cref="PostBindind"/>.
        /// </summary>
        /// <param name="request">The current <see cref="RequestScope"/>.</param>
        private static void ExecuteDelayedLoad(RequestScope request)
        {
            while (request.DelayedLoad.Count > 0)
            {
                PostBindind postSelect = request.DelayedLoad.Dequeue();

                PostSelectStrategyFactory.Get(postSelect.Method).Execute(postSelect, request);
            }
        }

        /// <summary>
        /// Raise an event ExecuteEventArgs
        /// (Used when a query is executed)
        /// </summary>
        private void RaiseExecuteEvent()
        {
            ExecuteEventArgs e = new ExecuteEventArgs();
            e.StatementName = statement.Id;
            Execute(this, e);
        }


    }
}
