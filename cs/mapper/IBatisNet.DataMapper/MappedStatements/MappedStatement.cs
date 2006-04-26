
#region Apache Notice
/*****************************************************************************
 * $Header: $
 * $Revision$
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
#if dotnet2
using System.Collections.Generic;
#endif
using System.Data;
using System.Reflection;
using System.Text;
using IBatisNet.Common;
using IBatisNet.Common.Logging;
using IBatisNet.Common.Utilities.Objects;
using IBatisNet.DataMapper.Commands;
using IBatisNet.DataMapper.Configuration.ParameterMapping;
using IBatisNet.DataMapper.Configuration.ResultMapping;
using IBatisNet.DataMapper.Configuration.Statements;
using IBatisNet.DataMapper.Exceptions;
using IBatisNet.DataMapper.Scope;
using IBatisNet.DataMapper.TypeHandlers;
using IBatisNet.Common.Utilities.Objects.Members;
using IBatisNet.DataMapper.DataExchange;

#endregion

namespace IBatisNet.DataMapper.MappedStatements
{

	/// <summary>
	/// Summary description for MappedStatement.
	/// </summary>
	public class MappedStatement : IMappedStatement
	{
		/// <summary>
		/// Event launch on exceute query
		/// </summary>
		public event ExecuteEventHandler Execute;

		/// <summary>
		/// Enumeration of the ExecuteQuery method.
		/// </summary>
		private enum ExecuteMethod : int
		{
			ExecuteQueryForObject =0,
			ExecuteQueryForIList,
			ExecuteQueryForGenericIList,
			ExecuteQueryForArrayList,
			ExecuteQueryForStrongTypedIList
		}


		/// <summary>
		/// All data tor retrieve 'select' result property
		/// </summary>
		/// <remarks>
		/// As ADO.NET allows to open DataReader per connection at once, we keep
		/// all th data to make the open the 'whish' DataReader after having closed the current. 
		/// </remarks>
		private class PostBindind
		{
			#region Fields
			private IMappedStatement _statement = null;
			private ResultProperty _property = null;
			private object _target = null;
			private object _keys = null;
			private ExecuteMethod _method = ExecuteMethod.ExecuteQueryForIList;
			#endregion

			#region Properties
			/// <summary>
			/// 
			/// </summary>
			public IMappedStatement Statement
			{
				set { _statement = value; }
				get { return _statement; }
			}

			/// <summary>
			/// 
			/// </summary>
			public ResultProperty ResultProperty
			{
				set { _property = value; }
				get { return _property; }
			}

			/// <summary>
			/// 
			/// </summary>
			public object Target
			{
				set { _target = value; }
				get { return _target; }
			}


			/// <summary>
			/// 
			/// </summary>
			public object Keys
			{
				set { _keys = value; }
				get { return _keys; }
			}

			/// <summary>
			/// 
			/// </summary>
			public ExecuteMethod Method
			{
				set { _method = value; }
				get { return _method; }
			}
			#endregion

		}


		#region Fields 

		// Magic number used to set the the maximum number of rows returned to 'all'. 
		internal const int NO_MAXIMUM_RESULTS = -1;
		// Magic number used to set the the number of rows skipped to 'none'. 
		internal const int NO_SKIPPED_RESULTS = -1;

		private static readonly ILog _logger = LogManager.GetLogger( MethodBase.GetCurrentMethod().DeclaringType );

		private IStatement _statement = null;

		private SqlMapper _sqlMap = null;

		private IPreparedCommand _preparedCommand = null;

		#endregion

		#region Properties

		/// <summary>
		/// The IPreparedCommand to use
		/// </summary>
		public IPreparedCommand PreparedCommand
		{
			get { return _preparedCommand; }
		}

		/// <summary>
		/// Name used to identify the MappedStatement amongst the others.
		/// This the name of the SQL statement by default.
		/// </summary>
		public string Id
		{
			get { return _statement.Id; }
		}

		/// <summary>
		/// The SQL statment used by this MappedStatement
		/// </summary>
		public IStatement Statement
		{
			get { return _statement; }
		}

		/// <summary>
		/// The SqlMap used by this MappedStatement
		/// </summary>
		public SqlMapper SqlMap
		{
			get { return _sqlMap; }
		}
		#endregion

		#region Constructor (s) / Destructor
		/// <summary>
		/// Constructor
		/// </summary>
		/// <param name="sqlMap">An SqlMap</param>
		/// <param name="statement">An SQL statement</param>
		internal MappedStatement( SqlMapper sqlMap, IStatement statement )
		{
			_sqlMap = sqlMap;
			_statement = statement;
			_preparedCommand = PreparedCommandFactory.GetPreparedCommand( false );
		}
		#endregion

		#region Methods

        /// <summary>
        /// Fills the object with reader and result map.
        /// </summary>
        /// <param name="request">The request.</param>
        /// <param name="reader">The reader.</param>
        /// <param name="resultMap">The result map.</param>
        /// <param name="resultObject">The result object.</param>
        /// <returns>Indicates if we have found a row.</returns>
		private bool FillObjectWithReaderAndResultMap(RequestScope request,IDataReader reader, 
			ResultMap resultMap, object resultObject)
		{
			bool dataFound = false;
			
			// For each Property in the ResultMap, set the property in the object 
			for(int index=0; index< resultMap.Properties.Count; index++)
			{
				request.IsRowDataFound = false;
				ResultProperty property = resultMap.Properties[index];
				SetObjectProperty(request, resultMap, property, ref resultObject, reader);
				dataFound = dataFound || request.IsRowDataFound;
			}

			request.IsRowDataFound = dataFound;
			return dataFound;
		}


        /// <summary>
        /// Applies the result map.
        /// </summary>
        /// <param name="request">The request.</param>
        /// <param name="reader">The reader.</param>
        /// <param name="resultObject">The result object.</param>
        /// <returns>The result object.</returns>
		private object ApplyResultMap(RequestScope request, IDataReader reader, object resultObject)
		{
			object outObject = resultObject; 

			// If there's an ResultMap, use it
			if (request.ResultMap != null) 
			{
				ResultMap resultMap = request.GetResultMap(reader);
				if (outObject == null) 
				{
					object[] parameters = null;
					if (resultMap.Parameters.Count >0)
					{
						parameters = new object[resultMap.Parameters.Count];
						// Fill parameters array
						for(int index=0; index< resultMap.Parameters.Count; index++)
						{
							ResultProperty resultProperty = resultMap.Parameters[index];
							parameters[index] = this.GetObjectArgument(request, resultMap, resultProperty, ref reader);
						}
					}

					outObject = resultMap.CreateInstanceOfResult(parameters);
				}

				// For each Property in the ResultMap, set the property in the object 
				for(int index=0; index< resultMap.Properties.Count; index++)
				{
					ResultProperty property = resultMap.Properties[index];
					SetObjectProperty(request, resultMap, property, ref outObject, reader);
				}
			} 
			else // else try to use a ResultClass
			{
				if (_statement.ResultClass != null) 
				{
					if (outObject == null) 
					{
						outObject = _statement.CreateInstanceOfResultClass();
					}

					// Check if the ResultClass is a 'primitive' Type
					if (_sqlMap.TypeHandlerFactory.IsSimpleType(_statement.ResultClass))
					{
						// Create a ResultMap
						ResultMap resultMap = new ResultMap(request.DataExchangeFactory);

						// Create a ResultProperty
						ResultProperty property = new ResultProperty();
						property.PropertyName = "value";
						property.ColumnIndex = 0;
						property.TypeHandler = _sqlMap.TypeHandlerFactory.GetTypeHandler(outObject.GetType());
						
						resultMap.AddResultPropery(property);
						resultMap.DataExchange = request.DataExchangeFactory.GetDataExchangeForClass( typeof(int) );// set the PrimitiveDataExchange

						SetObjectProperty(request, resultMap, property, ref outObject, reader);
					}
					else if (outObject is IDictionary) 
					{
						int count = reader.FieldCount;
						for (int i = 0; i < count; i++) 
						{
							ResultProperty property = new ResultProperty();
							property.PropertyName = "value";
							property.ColumnIndex = i;
							property.TypeHandler = _sqlMap.TypeHandlerFactory.GetTypeHandler(reader.GetFieldType(i));
							((IDictionary) outObject).Add(
								reader.GetName(i), 
								property.GetDataBaseValue(reader));
						}
					}
					else if (outObject is IList) 
					{
						int count = reader.FieldCount;
						for (int i = 0; i < count; i++) 
						{
							ResultProperty property = new ResultProperty();
							property.PropertyName = "value";
							property.ColumnIndex = i;
							property.TypeHandler = _sqlMap.TypeHandlerFactory.GetTypeHandler(reader.GetFieldType(i));
							((IList) outObject).Add(property.GetDataBaseValue(reader));
						}
					}
					else
					{
						AutoMapReader( reader, ref outObject);
					}
				}
				else
				{
					if (reader.FieldCount == 1)
					{
						ResultProperty property = new ResultProperty();
						property.PropertyName = "value";
						property.ColumnIndex = 0;
						property.TypeHandler = _sqlMap.TypeHandlerFactory.GetTypeHandler(reader.GetFieldType(0));
						outObject = property.GetDataBaseValue(reader);
					}
					else if (reader.FieldCount > 1)
					{
						object[] newOutObject = new object[reader.FieldCount];
						int count = reader.FieldCount;
						for (int i = 0; i < count; i++) 
						{
							ResultProperty property = new ResultProperty();
							property.PropertyName = "value";
							property.ColumnIndex = i;
							property.TypeHandler = _sqlMap.TypeHandlerFactory.GetTypeHandler(reader.GetFieldType(i));
							newOutObject[i] = property.GetDataBaseValue(reader);
						}

						outObject = newOutObject;
					}
					else
					{
						// do nothing if 0 fields
					}
				}
			}

			return outObject;
		}		

		
		/// <summary>
		/// Retrieve the output parameter and map them on the result object.
		/// This routine is only use is you specified a ParameterMap and some output attribute
		/// or if you use a store procedure with output parameter...
		/// </summary>
		/// <param name="request"></param>
		/// <param name="session">The current session.</param>
		/// <param name="result">The result object.</param>
		/// <param name="command">The command sql.</param>
		private void RetrieveOutputParameters(RequestScope request, IDalSession session, IDbCommand command, object result)
		{
			if (request.ParameterMap != null)
			{
				int count = request.ParameterMap.PropertiesList.Count;
				for(int i=0; i<count; i++)
				{
					ParameterProperty  mapping = request.ParameterMap.GetProperty(i);
					if (mapping.Direction == ParameterDirection.Output || 
						mapping.Direction == ParameterDirection.InputOutput) 
					{
						string parameterName = string.Empty;
						if (session.DataSource.DbProvider.UseParameterPrefixInParameter == false)
						{
							parameterName =  mapping.ColumnName;
						}
						else
						{
							parameterName = session.DataSource.DbProvider.ParameterPrefix + 
								mapping.ColumnName;
						}
						
						if (mapping.TypeHandler == null) // Find the TypeHandler
						{
							lock(mapping) 
							{
								if (mapping.TypeHandler == null)
								{
									Type propertyType =ObjectProbe.GetMemberTypeForGetter(result,mapping.PropertyName);

									mapping.TypeHandler = _sqlMap.TypeHandlerFactory.GetTypeHandler(propertyType);
								}
							}					
						}
						
						object dataBaseValue = mapping.TypeHandler.GetDataBaseValue( ((IDataParameter)command.Parameters[parameterName]).Value, result.GetType() );
						request.IsRowDataFound = request.IsRowDataFound || (dataBaseValue != null);

						request.ParameterMap.SetOutputParameter(ref result, mapping, dataBaseValue);
					}
				}
			}
		}

		
		#region ExecuteForObject

		/// <summary>
		/// Executes an SQL statement that returns a single row as an Object.
		/// </summary>
		/// <param name="session">The session used to execute the statement.</param>
		/// <param name="parameterObject">The object used to set the parameters in the SQL.</param>
		/// <returns>The object</returns>
		public virtual object ExecuteQueryForObject( IDalSession session, object parameterObject )
		{
			return ExecuteQueryForObject(session, parameterObject, null);
		}


		/// <summary>
		/// Executes an SQL statement that returns a single row as an Object of the type of
		/// the resultObject passed in as a parameter.
		/// </summary>
		/// <param name="session">The session used to execute the statement.</param>
		/// <param name="parameterObject">The object used to set the parameters in the SQL.</param>
		/// <param name="resultObject">The result object.</param>
		/// <returns>The object</returns>
		public virtual object ExecuteQueryForObject(IDalSession session, object parameterObject, object resultObject )
		{
			object obj = null;
			RequestScope request = _statement.Sql.GetRequestScope(parameterObject, session);

			_preparedCommand.Create( request, session, this.Statement, parameterObject );

			obj = RunQueryForObject(request, session, parameterObject, resultObject);
			
			return obj;
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
		internal object RunQueryForObject(RequestScope request, IDalSession session, object parameterObject, object resultObject )
		{
			object result = resultObject;
			
			using ( IDbCommand command = request.IDbCommand )
			{
				using ( IDataReader reader = command.ExecuteReader() )
				{				
					if ( reader.Read() )
					{
						result = ApplyResultMap(request, reader, resultObject);		
					}
				}

				ExecutePostSelect( session, request);

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
#if dotnet2

        /// <summary>
        /// Executes an SQL statement that returns a single row as an Object.
        /// </summary>
        /// <param name="session">The session used to execute the statement.</param>
        /// <param name="parameterObject">The object used to set the parameters in the SQL.</param>
        /// <returns>The object</returns>
        public virtual T ExecuteQueryForObject<T>(IDalSession session, object parameterObject)
        {
            return ExecuteQueryForObject<T>(session, parameterObject, default(T));
        }


        /// <summary>
        /// Executes an SQL statement that returns a single row as an Object of the type of
        /// the resultObject passed in as a parameter.
        /// </summary>
        /// <param name="session">The session used to execute the statement.</param>
        /// <param name="parameterObject">The object used to set the parameters in the SQL.</param>
        /// <param name="resultObject">The result object.</param>
        /// <returns>The object</returns>
        public virtual T ExecuteQueryForObject<T>(IDalSession session, object parameterObject, T resultObject)
        {
            T obj = default(T);
            RequestScope request = _statement.Sql.GetRequestScope(parameterObject, session);

            _preparedCommand.Create(request, session, this.Statement, parameterObject);

            obj = (T)RunQueryForObject(request, session, parameterObject, resultObject);

            return obj;
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
        internal T RunQueryForObject<T>(RequestScope request, IDalSession session, object parameterObject, T resultObject)
        {
            T result = resultObject;

            using (IDbCommand command = request.IDbCommand)
            {
                using (IDataReader reader = command.ExecuteReader())
                {
                    if (reader.Read())
                    {
                        result = (T)ApplyResultMap(request, reader, resultObject);
                    }
                }

                ExecutePostSelect(session, request);

		#region remark
                // If you are using the OleDb data provider (as you are), you need to close the
                // DataReader before output parameters are visible.
		#endregion

                RetrieveOutputParameters(request, session, command, parameterObject);
            }

            RaiseExecuteEvent();

            return result;
        }
#endif
		#endregion

		#region ExecuteQueryForList

		/// <summary>
		/// Runs a query with a custom object that gets a chance 
		/// to deal with each row as it is processed.
		/// </summary>
		/// <param name="session">The session used to execute the statement.</param>
		/// <param name="parameterObject">The object used to set the parameters in the SQL.</param>
		/// <param name="rowDelegate"></param>
		public virtual IList ExecuteQueryForRowDelegate( IDalSession session, object parameterObject, SqlMapper.RowDelegate rowDelegate )
		{
			RequestScope request = _statement.Sql.GetRequestScope(parameterObject, session);

			_preparedCommand.Create( request, session, this.Statement, parameterObject );

			if (rowDelegate == null) 
			{
				throw new DataMapperException("A null RowDelegate was passed to QueryForRowDelegate.");
			}
			
			return RunQueryForList(request, session, parameterObject, NO_SKIPPED_RESULTS, NO_MAXIMUM_RESULTS, rowDelegate);
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
		public virtual IDictionary ExecuteQueryForMapWithRowDelegate( IDalSession session, object parameterObject, string keyProperty, string valueProperty, SqlMapper.DictionaryRowDelegate rowDelegate )
		{
			RequestScope request = _statement.Sql.GetRequestScope(parameterObject, session);

			if (rowDelegate == null) 
			{
				throw new DataMapperException("A null DictionaryRowDelegate was passed to QueryForMapWithRowDelegate.");
			}
			
			_preparedCommand.Create(request, session, this.Statement, parameterObject);

			return RunQueryForMap(request, session, parameterObject, keyProperty, valueProperty, rowDelegate);
		}

		
		/// <summary>
		/// Executes the SQL and retuns all rows selected. This is exactly the same as
		/// calling ExecuteQueryForList(session, parameterObject, NO_SKIPPED_RESULTS, NO_MAXIMUM_RESULTS).
		/// </summary>
		/// <param name="session">The session used to execute the statement.</param>
		/// <param name="parameterObject">The object used to set the parameters in the SQL.</param>
		/// <returns>A List of result objects.</returns>
		public virtual IList ExecuteQueryForList( IDalSession session, object parameterObject )
		{
			return ExecuteQueryForList( session, parameterObject, NO_SKIPPED_RESULTS, NO_MAXIMUM_RESULTS);
		}


		/// <summary>
		/// Executes the SQL and retuns a subset of the rows selected.
		/// </summary>
		/// <param name="session">The session used to execute the statement.</param>
		/// <param name="parameterObject">The object used to set the parameters in the SQL.</param>
		/// <param name="skipResults">The number of rows to skip over.</param>
		/// <param name="maxResults">The maximum number of rows to return.</param>
		/// <returns>A List of result objects.</returns>
		public virtual IList ExecuteQueryForList( IDalSession session, object parameterObject, int skipResults, int maxResults )
		{
			IList list = null;
			RequestScope request = _statement.Sql.GetRequestScope(parameterObject, session);

			_preparedCommand.Create( request, session, this.Statement, parameterObject );

			list = RunQueryForList(request, session, parameterObject, skipResults, maxResults, null);
			
			return list;
		}

		
		/// <summary>
		/// Executes the SQL and retuns a List of result objects.
		/// </summary>
		/// <param name="request">The request scope.</param>
		/// <param name="session">The session used to execute the statement.</param>
		/// <param name="parameterObject">The object used to set the parameters in the SQL.</param>
		/// <param name="skipResults">The number of rows to skip over.</param>
		/// <param name="maxResults">The maximum number of rows to return.</param>
		/// <param name="rowDelegate"></param>
		/// <returns>A List of result objects.</returns>
		internal IList RunQueryForList(RequestScope request, IDalSession session, object parameterObject, int skipResults, int maxResults,  SqlMapper.RowDelegate rowDelegate)
		{
			IList list = null;

			using ( IDbCommand command = request.IDbCommand )
			{
				if (_statement.ListClass == null)
				{
					list = new ArrayList();
				}
				else
				{
					list = _statement.CreateInstanceOfListClass();
				}

				using ( IDataReader reader = command.ExecuteReader() )
				{			
					// skip results
					for (int i = 0; i < skipResults; i++) 
					{
						if (!reader.Read()) 
						{
							break;
						}
					}

					int n = 0;

					if (rowDelegate == null) 
					{
						while ( (maxResults == NO_MAXIMUM_RESULTS || n < maxResults) 
							&& reader.Read() )
						{
							object obj = ApplyResultMap(request, reader, null);
						
							list.Add( obj );
							n++;
						}
					}
					else
					{
						while ( (maxResults == NO_MAXIMUM_RESULTS || n < maxResults) 
							&& reader.Read() )
						{
							object obj = ApplyResultMap(request, reader, null);

							rowDelegate(obj, parameterObject, list);
							n++;
						}
					}
				}

				ExecutePostSelect( session, request);

				RetrieveOutputParameters(request, session, command, parameterObject);
			}

			return list;
		}
		
		
		/// <summary>
		/// Executes the SQL and and fill a strongly typed collection.
		/// </summary>
		/// <param name="session">The session used to execute the statement.</param>
		/// <param name="parameterObject">The object used to set the parameters in the SQL.</param>
		/// <param name="resultObject">A strongly typed collection of result objects.</param>
		public virtual void ExecuteQueryForList(IDalSession session, object parameterObject, IList resultObject )
		{
			RequestScope request = _statement.Sql.GetRequestScope(parameterObject, session);

			_preparedCommand.Create( request, session, this.Statement, parameterObject );

			using ( IDbCommand command = request.IDbCommand )
			{
				using ( IDataReader reader = command.ExecuteReader() )
				{			
					while ( reader.Read() )
					{
						object obj = ApplyResultMap(request, reader, null);
				
						resultObject.Add( obj );
					}
				}

				ExecutePostSelect( session, request);

				RetrieveOutputParameters(request, session, command, parameterObject);
			}
		}

		
		#endregion

		#region ExecuteQueryForList .NET 2.0
#if dotnet2

        /// <summary>
        /// Runs a query with a custom object that gets a chance 
        /// to deal with each row as it is processed.
        /// </summary>
        /// <param name="session">The session used to execute the statement.</param>
        /// <param name="parameterObject">The object used to set the parameters in the SQL.</param>
        /// <param name="rowDelegate"></param>
        public virtual IList<T> ExecuteQueryForRowDelegate<T>(IDalSession session, object parameterObject, SqlMapper.RowDelegate<T> rowDelegate)
        {
            RequestScope request = _statement.Sql.GetRequestScope(parameterObject, session);

            _preparedCommand.Create(request, session, this.Statement, parameterObject);

            if (rowDelegate == null)
            {
                throw new DataMapperException("A null RowDelegate was passed to QueryForRowDelegate.");
            }

            return RunQueryForList<T>(request, session, parameterObject, NO_SKIPPED_RESULTS, NO_MAXIMUM_RESULTS, rowDelegate);
        }


        /// <summary>
        /// Executes the SQL and retuns all rows selected. This is exactly the same as
        /// calling ExecuteQueryForList(session, parameterObject, NO_SKIPPED_RESULTS, NO_MAXIMUM_RESULTS).
        /// </summary>
        /// <param name="session">The session used to execute the statement.</param>
        /// <param name="parameterObject">The object used to set the parameters in the SQL.</param>
        /// <returns>A List of result objects.</returns>
        public virtual IList<T> ExecuteQueryForList<T>(IDalSession session, object parameterObject)
        {
            return ExecuteQueryForList<T>(session, parameterObject, NO_SKIPPED_RESULTS, NO_MAXIMUM_RESULTS);
        }


        /// <summary>
        /// Executes the SQL and retuns a subset of the rows selected.
        /// </summary>
        /// <param name="session">The session used to execute the statement.</param>
        /// <param name="parameterObject">The object used to set the parameters in the SQL.</param>
        /// <param name="skipResults">The number of rows to skip over.</param>
        /// <param name="maxResults">The maximum number of rows to return.</param>
        /// <returns>A List of result objects.</returns>
        public virtual IList<T> ExecuteQueryForList<T>(IDalSession session, object parameterObject, int skipResults, int maxResults)
        {
            IList<T> list = null;
            RequestScope request = _statement.Sql.GetRequestScope(parameterObject, session);

            _preparedCommand.Create(request, session, this.Statement, parameterObject);

            list = RunQueryForList<T>(request, session, parameterObject, skipResults, maxResults, null);

            return list;
        }


        /// <summary>
        /// Executes the SQL and retuns a List of result objects.
        /// </summary>
        /// <param name="request">The request scope.</param>
        /// <param name="session">The session used to execute the statement.</param>
        /// <param name="parameterObject">The object used to set the parameters in the SQL.</param>
        /// <param name="skipResults">The number of rows to skip over.</param>
        /// <param name="maxResults">The maximum number of rows to return.</param>
        /// <param name="rowDelegate"></param>
        /// <returns>A List of result objects.</returns>
        internal IList<T> RunQueryForList<T>(RequestScope request, IDalSession session, object parameterObject, int skipResults, int maxResults, SqlMapper.RowDelegate<T> rowDelegate)
        {
            IList<T> list = null;

            using (IDbCommand command = request.IDbCommand)
            {
                // TODO:  Should we ignore this?, I think so in the case of generics.  
                //if (_statement.ListClass == null)
                //{
                //    list = new ArrayList();
                //}
                //else
                //{
                //    list = _statement.CreateInstanceOfListClass();
                //}
                list = new List<T>();

                using (IDataReader reader = command.ExecuteReader())
                {
                    // skip results
                    for (int i = 0; i < skipResults; i++)
                    {
                        if (!reader.Read())
                        {
                            break;
                        }
                    }

                    int n = 0;

                    if (rowDelegate == null)
                    {
                        while ((maxResults == NO_MAXIMUM_RESULTS || n < maxResults)
                            && reader.Read())
                        {
                            T obj = (T)ApplyResultMap(request, reader, null);

                            list.Add(obj);
                            n++;
                        }
                    }
                    else
                    {
                        while ((maxResults == NO_MAXIMUM_RESULTS || n < maxResults)
                            && reader.Read())
                        {
                            T obj = (T)ApplyResultMap(request, reader, null);

                            rowDelegate(obj, parameterObject, list);
                            n++;
                        }
                    }
                }

                ExecutePostSelect(session, request);

                RetrieveOutputParameters(request, session, command, parameterObject);
            }

            return list;
        }


        /// <summary>
        /// Executes the SQL and and fill a strongly typed collection.
        /// </summary>
        /// <param name="session">The session used to execute the statement.</param>
        /// <param name="parameterObject">The object used to set the parameters in the SQL.</param>
        /// <param name="resultObject">A strongly typed collection of result objects.</param>
        public virtual void ExecuteQueryForList<T>(IDalSession session, object parameterObject, IList<T> resultObject)
        {
            RequestScope request = _statement.Sql.GetRequestScope(parameterObject, session);

            _preparedCommand.Create(request, session, this.Statement, parameterObject);

            using (IDbCommand command = request.IDbCommand)
            {
                using (IDataReader reader = command.ExecuteReader())
                {
                    while (reader.Read())
                    {
                        T obj = (T)ApplyResultMap(request, reader, null);

                        resultObject.Add(obj);
                    }
                }

                ExecutePostSelect(session, request);

                RetrieveOutputParameters(request, session, command, parameterObject);
            }
        }

#endif
		#endregion

		#region ExecuteUpdate, ExecuteInsert

		/// <summary>
		/// Execute an update statement. Also used for delete statement.
		/// Return the number of row effected.
		/// </summary>
		/// <param name="session">The session used to execute the statement.</param>
		/// <param name="parameterObject">The object used to set the parameters in the SQL.</param>
		/// <returns>The number of row effected.</returns>
		public virtual int ExecuteUpdate(IDalSession session, object parameterObject )
		{
			int rows = 0; // the number of rows affected
			RequestScope request = _statement.Sql.GetRequestScope(parameterObject, session);

			_preparedCommand.Create( request, session, this.Statement, parameterObject );
	
			using ( IDbCommand command = request.IDbCommand )
			{
				rows = command.ExecuteNonQuery();

				ExecutePostSelect( session, request);

				RetrieveOutputParameters(request, session, command, parameterObject);
			}

			RaiseExecuteEvent();

			return rows;
		}

		
		/// <summary>
		/// Execute an insert statement. Fill the parameter object with 
		/// the ouput parameters if any, also could return the insert generated key
		/// </summary>
		/// <param name="session">The session</param>
		/// <param name="parameterObject">The parameter object used to fill the statement.</param>
		/// <returns>Can return the insert generated key.</returns>
		public virtual object ExecuteInsert(IDalSession session, object parameterObject )
		{
			object generatedKey = null;
			SelectKey selectKeyStatement = null;
			RequestScope request = _statement.Sql.GetRequestScope(parameterObject, session);

			if (_statement is Insert)
			{
				selectKeyStatement = ((Insert)_statement).SelectKey;
			}

			if (selectKeyStatement != null && !selectKeyStatement.isAfter)
			{
				IMappedStatement mappedStatement = _sqlMap.GetMappedStatement( selectKeyStatement.Id );
				generatedKey = mappedStatement.ExecuteQueryForObject(session, parameterObject);

				ObjectProbe.SetMemberValue(parameterObject, selectKeyStatement.PropertyName, generatedKey, 
					request.ObjectFactory,
					request.MemberAccessorFactory);
			}

			_preparedCommand.Create( request, session, this.Statement, parameterObject );
			using ( IDbCommand command = request.IDbCommand)
			{
				if (_statement is Insert)
				{
					command.ExecuteNonQuery();
				}
				else
				{
					generatedKey = command.ExecuteScalar();
					if ( (_statement.ResultClass!=null) && 
						_sqlMap.TypeHandlerFactory.IsSimpleType(_statement.ResultClass) )
					{
						ITypeHandler typeHandler = _sqlMap.TypeHandlerFactory.GetTypeHandler(_statement.ResultClass);
						generatedKey = typeHandler.GetDataBaseValue(generatedKey, _statement.ResultClass);
					}
				}
			
				if (selectKeyStatement != null && selectKeyStatement.isAfter)
				{
					IMappedStatement mappedStatement = _sqlMap.GetMappedStatement( selectKeyStatement.Id );
					generatedKey = mappedStatement.ExecuteQueryForObject(session, parameterObject);

					ObjectProbe.SetMemberValue(parameterObject, selectKeyStatement.PropertyName, generatedKey, 
						request.ObjectFactory,
						request.MemberAccessorFactory);
				}

				ExecutePostSelect( session, request);

				RetrieveOutputParameters(request, session, command, parameterObject);
			}

			RaiseExecuteEvent();

			return generatedKey;
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
		public virtual IDictionary ExecuteQueryForMap( IDalSession session, object parameterObject, string keyProperty, string valueProperty )
		{
			IDictionary map = new Hashtable();
			RequestScope request = _statement.Sql.GetRequestScope(parameterObject, session);

			_preparedCommand.Create(request, session, this.Statement, parameterObject);

			map = RunQueryForMap(request, session, parameterObject, keyProperty, valueProperty, null );
			
			return map;
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
		/// <param name="rowDelegate"></param>
		/// <returns>A hashtable of object containing the rows keyed by keyProperty.</returns>
		///<exception cref="DataMapperException">If a transaction is not in progress, or the database throws an exception.</exception>
		internal IDictionary RunQueryForMap( RequestScope request, 
			IDalSession session, 
			object parameterObject, 
			string keyProperty, 
			string valueProperty, 
			SqlMapper.DictionaryRowDelegate rowDelegate  )
		{
			IDictionary map = new Hashtable();

			using (IDbCommand command = request.IDbCommand)
			{
				using (IDataReader reader = command.ExecuteReader())
				{
					if (rowDelegate == null)
					{
						while (reader.Read() )
						{
							object obj = ApplyResultMap(request, reader, null);
							object key = ObjectProbe.GetMemberValue(obj, keyProperty, request.MemberAccessorFactory);
							object value = obj;
							if (valueProperty != null)
							{
								value = ObjectProbe.GetMemberValue(obj, valueProperty, request.MemberAccessorFactory);
							}
							map.Add(key, value);
						}
					}
					else
					{
						while (reader.Read())
						{
							object obj = ApplyResultMap(request, reader, null);
							object key = ObjectProbe.GetMemberValue(obj, keyProperty,request.MemberAccessorFactory);
							object value = obj;
							if (valueProperty != null)
							{
								value = ObjectProbe.GetMemberValue(obj, valueProperty, request.MemberAccessorFactory);
							}
							rowDelegate(key, value, parameterObject, map);

						}
					}
				}
			}
			return map;

		}

		
		#endregion

		/// <summary>
		/// Process 'select' result properties
		/// </summary>
		/// <param name="request"></param>
		/// <param name="session"></param>
		private void ExecutePostSelect(IDalSession session, RequestScope request)
		{
			while (request.QueueSelect.Count>0)
			{
				PostBindind postSelect = request.QueueSelect.Dequeue() as PostBindind;

				if (postSelect.Method == ExecuteMethod.ExecuteQueryForIList)
				{
					object values = postSelect.Statement.ExecuteQueryForList(session, postSelect.Keys); 
					postSelect.ResultProperty.MemberAccessor.Set(postSelect.Target, values);
				}
				else if (postSelect.Method == ExecuteMethod.ExecuteQueryForStrongTypedIList)
				{
					object values = Activator.CreateInstance(postSelect.ResultProperty.MemberAccessor.MemberType);
					postSelect.Statement.ExecuteQueryForList(session, postSelect.Keys, (IList)values);

					postSelect.ResultProperty.MemberAccessor.Set(postSelect.Target, values);
				}
				else if (postSelect.Method == ExecuteMethod.ExecuteQueryForArrayList)
				{
					IList values = postSelect.Statement.ExecuteQueryForList(session, postSelect.Keys); 
					Type elementType = postSelect.ResultProperty.MemberAccessor.MemberType.GetElementType();

					Array array = Array.CreateInstance(elementType, values.Count);
					int count = values.Count;
					for(int i=0;i<count;i++)
					{
						array.SetValue(values[i],i);
					}

					postSelect.ResultProperty.MemberAccessor.Set(postSelect.Target, array);
				}
#if dotnet2
                else if (postSelect.Method == ExecuteMethod.ExecuteQueryForGenericIList)
                {
                    // How to: Examine and Instantiate Generic Types with Reflection  
                    // http://msdn2.microsoft.com/en-us/library/b8ytshk6.aspx

                    Type[] typeArgs = postSelect.ResultProperty.MemberAccessor.MemberType.GetGenericArguments();
                    Type genericList = typeof(IList<>);
                    Type constructedType = genericList.MakeGenericType(typeArgs);
                    Type elementType = postSelect.ResultProperty.MemberAccessor.MemberType.GetGenericArguments()[0];

                    Type mappedStatementType = postSelect.Statement.GetType();

                    Type[] typeArguments = {typeof(IDalSession), typeof(object)};

                    MethodInfo[] mis = mappedStatementType.GetMethods(BindingFlags.InvokeMethod | BindingFlags.Public | BindingFlags.Instance);
                    MethodInfo mi = null;
                    foreach(MethodInfo m in mis)
                    {
                        if (m.IsGenericMethod && 
                            m.Name == "ExecuteQueryForList" && 
                            m.GetParameters().Length==2)
                        {
                            mi = m;
                            break;
                        }
                    }

                    MethodInfo miConstructed = mi.MakeGenericMethod(elementType);

                    // Invoke the method.
                    object[] args = {session, postSelect.Keys};
                    object values = miConstructed.Invoke(postSelect.Statement, args);

					postSelect.ResultProperty.MemberAccessor.Set(postSelect.Target, values);
                }
#endif
				else if (postSelect.Method == ExecuteMethod.ExecuteQueryForObject)
				{
					object value = postSelect.Statement.ExecuteQueryForObject(session, postSelect.Keys);
					postSelect.ResultProperty.MemberAccessor.Set(postSelect.Target, value);
				}
			}
		}

		/// <summary>
		/// 
		/// </summary>
		/// <param name="request"></param>
		/// <param name="resultMap"></param>
		/// <param name="mapping"></param>
		/// <param name="target"></param>
		/// <param name="reader"></param>
		private void SetObjectProperty(RequestScope request, ResultMap resultMap, 
			ResultProperty mapping, ref object target, IDataReader reader)
		{
			string selectStatement = mapping.Select;

			if (selectStatement.Length == 0 && mapping.NestedResultMap == null)
			{
				// If the property is not a 'select' ResultProperty 
				//                     or a 'resultMap' ResultProperty
				// We have a 'normal' ResultMap

				#region Not a select statement
				if (mapping.TypeHandler == null || mapping.TypeHandler is UnknownTypeHandler) // Find the TypeHandler
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

							mapping.TypeHandler = _sqlMap.TypeHandlerFactory.GetTypeHandler(systemType);
						}
					}					
				}

				object dataBaseValue = mapping.GetDataBaseValue( reader );
				request.IsRowDataFound = request.IsRowDataFound || (dataBaseValue != null);

				resultMap.SetValueOfProperty( ref target, mapping, dataBaseValue );

				#endregion
			}
			else if (mapping.NestedResultMap != null) // 'resultMap' ResultProperty
			{
				object[] parameters = null;
				if (mapping.NestedResultMap.Parameters.Count >0)
				{
					parameters = new object[resultMap.Parameters.Count];
					// Fill parameters array
					for(int index=0; index< mapping.NestedResultMap.Parameters.Count; index++)
					{
						ResultProperty resultProperty = mapping.NestedResultMap.Parameters[index];
						parameters[index] = GetObjectArgument(request, resultMap, resultProperty, ref reader);
						request.IsRowDataFound = request.IsRowDataFound || (parameters[index] != null);
					}
				}

				object obj = mapping.NestedResultMap.CreateInstanceOfResult(parameters);
				if (FillObjectWithReaderAndResultMap(request, reader, mapping.NestedResultMap, obj) == false)
				{
					obj = null;
				}

				resultMap.SetValueOfProperty( ref target, mapping, obj );
			}
			else //'select' ResultProperty 
			{
				// Get the select statement
				IMappedStatement queryStatement = _sqlMap.GetMappedStatement(selectStatement);
				string paramString = mapping.ColumnName;
				object keys = null;
				bool wasNull = false;

				#region Find Key(s)
				if (paramString.IndexOf(',')>0 || paramString.IndexOf('=')>0) // composite parameters key
				{
					IDictionary keyMap = new Hashtable();
					keys = keyMap;
					// define which character is seperating fields
					char[] splitter  = {'=',','};

					string[] paramTab = paramString.Split(splitter);
					if (paramTab.Length % 2 != 0) 
					{
						throw new DataMapperException("Invalid composite key string format in '"+mapping.PropertyName+". It must be: property1=column1,property2=column2,..."); 
					}
					IEnumerator enumerator = paramTab.GetEnumerator();
					while (!wasNull && enumerator.MoveNext()) 
					{
						string hashKey = ((string)enumerator.Current).Trim();
						enumerator.MoveNext();
						object hashValue = reader.GetValue( reader.GetOrdinal(((string)enumerator.Current).Trim()) );

						keyMap.Add(hashKey, hashValue );
						wasNull = (hashValue == DBNull.Value);
					}
				} 
				else // single parameter key
				{
					keys = reader.GetValue(reader.GetOrdinal(paramString));
					wasNull = reader.IsDBNull(reader.GetOrdinal(paramString));
				}
				#endregion

				if (wasNull) 
				{
					// set the value of an object property to null
					mapping.MemberAccessor.Set(target, null);
				} 
				else // Collection object or .Net object
				{
					PostBindind postSelect = new PostBindind();
					postSelect.Statement = queryStatement;
					postSelect.Keys = keys;
					postSelect.Target = target;
					postSelect.ResultProperty = mapping;

					#region Collection object or .NET object
					
                    if (mapping.MemberAccessor.MemberType.BaseType == typeof(Array))
					{
						postSelect.Method = ExecuteMethod.ExecuteQueryForArrayList;
					}
					// Check if the object to Map implement 'IList' or is IList type
					// If yes the ResultProperty is map to a IList object
					else if ( typeof(IList).IsAssignableFrom(mapping.MemberAccessor.MemberType) )
					{
						if (mapping.IsLazyLoad)
						{
							object values = LazyLoadList.NewInstance(queryStatement, keys, target, mapping.PropertyName);
							mapping.MemberAccessor.Set(target, values);
						}
						else
						{
							if (mapping.MemberAccessor.MemberType == typeof(IList))
							{
								postSelect.Method = ExecuteMethod.ExecuteQueryForIList;
							}
							else
							{
                                postSelect.Method = ExecuteMethod.ExecuteQueryForStrongTypedIList;
                            }
						}
					}
#if dotnet2
                    else if (mapping.MemberAccessor.MemberType.IsGenericType &&
                         mapping.MemberAccessor.MemberType.GetGenericTypeDefinition() == typeof(IList<>)) 
                    {
                        if (mapping.IsLazyLoad)
                        {
                            object values = LazyLoadList.NewInstance(queryStatement, keys, target, mapping.PropertyName);
							mapping.MemberAccessor.Set(target, values);
                        }
                        else
                        {
                            if (mapping.MemberAccessor.MemberType.GetGenericTypeDefinition() == typeof(IList<>))
                            {
                                postSelect.Method = ExecuteMethod.ExecuteQueryForGenericIList;
                            }
                        }
                    }
#endif
					else // The ResultProperty is map to a .Net object
					{
						postSelect.Method = ExecuteMethod.ExecuteQueryForObject;
					}
					#endregion

					if (!mapping.IsLazyLoad)
					{
						request.QueueSelect.Enqueue(postSelect);
					}
				}
			}
		}

        /// <summary>
        /// Gets the argument value for an argument constructor.
        /// </summary>
        /// <param name="request">The request.</param>
        /// <param name="resultMap">The result map.</param>
        /// <param name="mapping">The mapping.</param>
        /// <param name="reader">The reader.</param>
        /// <returns>The argument value</returns>
		private object GetObjectArgument(RequestScope request, ResultMap resultMap, ResultProperty mapping, ref IDataReader reader)
		{
			string selectStatement = mapping.Select;

			if (selectStatement.Length == 0 && mapping.NestedResultMap == null)
			{
				// If the property is not a 'select' ResultProperty 
				//                     or a 'resultMap' ResultProperty
				// We have a 'normal' ResultMap

				#region Not a select statement
				if (mapping.TypeHandler == null || mapping.TypeHandler is UnknownTypeHandler) // Find the TypeHandler
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

							mapping.TypeHandler = _sqlMap.TypeHandlerFactory.GetTypeHandler(systemType);
						}
					}					
				}

				object dataBaseValue = mapping.GetDataBaseValue( reader );
				request.IsRowDataFound = request.IsRowDataFound || (dataBaseValue != null);

				return dataBaseValue;

				#endregion
			}
			else if (mapping.NestedResultMap != null) // 'resultMap' ResultProperty
			{
				object[] parameters = null;
				if (mapping.NestedResultMap.Parameters.Count >0)
				{
					parameters = new object[resultMap.Parameters.Count];
					// Fill parameters array
					for(int index=0; index< mapping.NestedResultMap.Parameters.Count; index++)
					{
						ResultProperty property = mapping.NestedResultMap.Parameters[index];
						parameters[index] = property.GetDataBaseValue( reader );
						request.IsRowDataFound = request.IsRowDataFound || (parameters[index] != null);
					}
				}

				object obj = mapping.NestedResultMap.CreateInstanceOfResult(parameters);
				if (FillObjectWithReaderAndResultMap(request, reader, mapping.NestedResultMap, obj) == false)
				{
					obj = null;
				}

				return obj;
			}
			else //'select' ResultProperty 
			{
				// Get the select statement
				IMappedStatement queryStatement = _sqlMap.GetMappedStatement(selectStatement);
				string paramString = mapping.ColumnName;
				object keys = null;
				bool wasNull = false;

				#region Find Key(s)
				if (paramString.IndexOf(',')>0 || paramString.IndexOf('=')>0) // composite parameters key
				{
					IDictionary keyMap = new Hashtable();
					keys = keyMap;
					// define which character is seperating fields
					char[] splitter  = {'=',','};

					string[] paramTab = paramString.Split(splitter);
					if (paramTab.Length % 2 != 0) 
					{
						throw new DataMapperException("Invalid composite key string format in '"+mapping.PropertyName+". It must be: property1=column1,property2=column2,..."); 
					}
					IEnumerator enumerator = paramTab.GetEnumerator();
					while (!wasNull && enumerator.MoveNext()) 
					{
						string hashKey = ((string)enumerator.Current).Trim();
						enumerator.MoveNext();
						object hashValue = reader.GetValue( reader.GetOrdinal(((string)enumerator.Current).Trim()) );

						keyMap.Add(hashKey, hashValue );
						wasNull = (hashValue == DBNull.Value);
					}
				} 
				else // single parameter key
				{
					keys = reader.GetValue(reader.GetOrdinal(paramString));
					wasNull = reader.IsDBNull(reader.GetOrdinal(paramString));
				}
				#endregion

				if (wasNull) 
				{
					// set the value of an object property to null
					return null;
				} 
				else // Collection object or .Net object
				{
					// lazyLoading is not permit for argument constructor

					#region Collection object or .NET object
					
					if (mapping.MemberType.BaseType == typeof(Array))
					{
						reader = DataReaderTransformer.Transforme(reader, request.Session.DataSource.DbProvider);
						IList values = queryStatement.ExecuteQueryForList(request.Session, keys); 

						Type elementType = mapping.MemberType.GetElementType();
						Array array = Array.CreateInstance(elementType, values.Count);
						int count = values.Count;
						for(int i=0;i<count;i++)
						{
							array.SetValue(values[i],i);
						}
						return array;
					}
					// Check if the object to Map implement 'IList' or is IList type
					// If yes the ResultProperty is map to a IList object
					else if ( typeof(IList).IsAssignableFrom(mapping.MemberType) )
					{
						if (mapping.MemberType == typeof(IList))
						{
							reader = DataReaderTransformer.Transforme(reader, request.Session.DataSource.DbProvider);
							return queryStatement.ExecuteQueryForList(request.Session, keys); 
						}
						else // Strongly typed List
						{
							reader = DataReaderTransformer.Transforme(reader, request.Session.DataSource.DbProvider);
							object values = Activator.CreateInstance(mapping.MemberType);
							queryStatement.ExecuteQueryForList(request.Session, keys, (IList)values);
							return values;
						}
					}
#if dotnet2
                    else if (mapping.MemberType.IsGenericType &&
                         mapping.MemberType.GetGenericTypeDefinition() == typeof(IList<>)) 
                    {
                        reader = DataReaderTransformer.Transforme(reader, request.Session.DataSource.DbProvider);

                        Type[] typeArgs = mapping.MemberType.GetGenericArguments();
                        Type genericList = typeof(IList<>);
                        Type constructedType = genericList.MakeGenericType(typeArgs);
                        Type elementType = mapping.MemberType.GetGenericArguments()[0];

                        Type mappedStatementType = queryStatement.GetType();

                        Type[] typeArguments = { typeof(IDalSession), typeof(object) };

                        MethodInfo[] mis = mappedStatementType.GetMethods(BindingFlags.InvokeMethod | BindingFlags.Public | BindingFlags.Instance);
                        MethodInfo mi = null;
                        foreach (MethodInfo m in mis)
                        {
                            if (m.IsGenericMethod &&
                                m.Name == "ExecuteQueryForList" &&
                                m.GetParameters().Length == 2)
                            {
                                mi = m;
                                break;
                            }
                        }

                        MethodInfo miConstructed = mi.MakeGenericMethod(elementType);

                        // Invoke the method.
                        object[] args = { request.Session, keys };
                        object values = miConstructed.Invoke(queryStatement, args);

                        return values;
                    }
#endif
					else // The ResultProperty is map to a .Net object
					{
						reader = DataReaderTransformer.Transforme(reader, request.Session.DataSource.DbProvider);
						return queryStatement.ExecuteQueryForObject(request.Session, keys);
					}
					#endregion

				}
			}
		}
			
		/// <summary>
		/// Raise an event ExecuteEventArgs
		/// (Used when a query is executed)
		/// </summary>
		private void RaiseExecuteEvent()
		{
			ExecuteEventArgs e = new ExecuteEventArgs();
			e.StatementName = _statement.Id;
			if (Execute != null)
			{
				Execute(this, e);
			}
		}

		/// <summary>
		/// ToString implementation.
		/// </summary>
		/// <returns>A string that describes the MappedStatement</returns>
		public override string ToString() 
		{
			StringBuilder buffer = new StringBuilder();
			buffer.Append("\tMappedStatement: " + this.Id);
			buffer.Append(Environment.NewLine);
			if (_statement.ParameterMap != null) buffer.Append(_statement.ParameterMap.Id);
			if (_statement.ResultMap != null) buffer.Append(_statement.ResultMap.Id);

			return buffer.ToString();
		}
	

		private ReaderAutoMapper _readerAutoMapper = null;

        /// <summary>
        /// Auto-map the reader to the result object.
        /// </summary>
        /// <param name="reader">The reader.</param>
        /// <param name="resultObject">The result object.</param>
		private void AutoMapReader( IDataReader reader,ref object resultObject) 
		{
			if (_statement.RemapResults)
			{
				ReaderAutoMapper readerAutoMapper = new ReaderAutoMapper(_sqlMap.TypeHandlerFactory, 
					_sqlMap.MemberAccessorFactory,
					_sqlMap.DataExchangeFactory,
					reader, 
					ref resultObject);
				readerAutoMapper.AutoMapReader( reader, ref resultObject );
				_logger.Debug("The RemapResults");
			}
			else
			{
				if (_readerAutoMapper == null)
				{
					lock (this) 
					{
						if (_readerAutoMapper == null) 
						{
							_readerAutoMapper = new ReaderAutoMapper(
								_sqlMap.TypeHandlerFactory, 
								_sqlMap.MemberAccessorFactory,
								_sqlMap.DataExchangeFactory,
								reader, 
								ref resultObject);
						}
					}
				}
				_logger.Debug("The AutoMapReader");
				_readerAutoMapper.AutoMapReader( reader, ref resultObject );				
			}

		}
		#endregion

		private class ReaderAutoMapper 
		{
			private ResultMap _resultMap = null;

			/// <summary>
			/// 
			/// </summary>
			/// <param name="reader"></param>
			/// <param name="resultObject"></param>
			/// <param name="typeHandlerFactory"></param>
			/// <param name="memberAccessorFactory"></param>
			/// <param name="dataExchangeFactory"></param>
			public ReaderAutoMapper(TypeHandlerFactory typeHandlerFactory, 
				IMemberAccessorFactory memberAccessorFactory,
				DataExchangeFactory dataExchangeFactory,
				IDataReader reader,
				ref object resultObject) 
			{
				Type targetType = resultObject.GetType();
				_resultMap = new ResultMap(dataExchangeFactory);
				_resultMap.DataExchange = dataExchangeFactory.GetDataExchangeForClass( targetType );
				try 
				{
					// Get all PropertyInfo from the resultObject properties
					ReflectionInfo reflectionInfo = ReflectionInfo.GetInstance(targetType);
					string[] membersName = reflectionInfo.GetWriteableMemberNames();

					Hashtable propertyMap = new Hashtable();
					int length = membersName.Length;
					for (int i = 0; i < length; i++) 
					{
						IMemberAccessor memberAccessor = memberAccessorFactory.CreateMemberAccessor(targetType, membersName[i]);
						propertyMap.Add( membersName[i].ToUpper(), memberAccessor );
					}

					// Get all column Name from the reader
					// and build a resultMap from with the help of the PropertyInfo[].
					DataTable dataColumn = reader.GetSchemaTable();
					int count = dataColumn.Rows.Count;
					for (int i = 0; i < count; i++) 
					{
						string columnName = dataColumn.Rows[i][0].ToString();
						IMemberAccessor matchedMemberAccessor = propertyMap[columnName.ToUpper()] as IMemberAccessor;

						ResultProperty property = new ResultProperty();
						property.ColumnName = columnName;
						property.ColumnIndex = i;

						if (resultObject is Hashtable) 
						{
							property.PropertyName = columnName;
							_resultMap.AddResultPropery(property);
						}

						Type propertyType = null;

						if (matchedMemberAccessor == null )
						{
							try
							{
								propertyType = ObjectProbe.GetMemberTypeForSetter(resultObject, columnName);
							}
							catch
							{
								_logger.Error("The column [" + columnName + "] could not be auto mapped to a property on [" + resultObject.ToString() + "]");
							}
						}
						else
						{
							propertyType = matchedMemberAccessor.MemberType;
						}

						if(propertyType != null || matchedMemberAccessor != null) 
						{
							property.PropertyName = (matchedMemberAccessor != null ? matchedMemberAccessor.Name : columnName );
							if (matchedMemberAccessor != null)
							{
								property.Initialize(typeHandlerFactory, matchedMemberAccessor );
							}
							else
							{
								property.TypeHandler = typeHandlerFactory.GetTypeHandler(propertyType);
							}
							_resultMap.AddResultPropery(property);
						} 
					}
				} 
				catch (Exception e) 
				{
					throw new DataMapperException("Error automapping columns. Cause: " + e.Message, e);
				}
			}

			/// <summary>
			/// 
			/// </summary>
			/// <param name="reader"></param>
			/// <param name="resultObject"></param>
			public void AutoMapReader(IDataReader reader, ref object resultObject)
			{
				for(int index=0; index< _resultMap.Properties.Count; index++)

				{
					ResultProperty property = _resultMap.Properties[index];
					_resultMap.SetValueOfProperty( ref resultObject, property, 
						property.GetDataBaseValue( reader ));
				}
			}

		}
	}
}
