
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

#region Imports
using System;
using System.Data;

using IBatisNet.Common;

using IBatisNet.DataAccess;
using IBatisNet.DataAccess.Exceptions; 
using IBatisNet.DataAccess.Interfaces;

using NHibernate;
using NHibernate.Cfg;

using log4net;

#endregion

#region Remarks

//<context>
//<transactionManager type="SQLMAP">
//<property name="SqlMapConfig" value="com/domain/dao/sqlmap/SqlMapConfig.xml"/>
//</transactionManager>
//<dao interface="com.domain.dao.PersonDao" implementation="com.domain.dao.sqlmap.SqlMapPersonDao"/>
//<dao interface="com.domain.dao.BusinessDao" implementation="com.domain.dao.sqlmap.SqlMapBusinessDao"/>
//<dao interface="com.domain.dao.AccountDao" implementation="com.domain.dao.sqlmap.SqlMapAccountDao"/>
//</context>
//
//<!--===============================================
//Example Hibernate DAO Configuration 
//===============================================-->
//
//<context>
//<transactionManager type="HIBERNATE">
//<property name="hibernate.dialect" value="net.sf.hibernate.dialect.PostgreSQLDialect"/>
//<property name="hibernate.connection.driver_class" value="${driver}"/>
//<property name="hibernate.connection.url" value="${url}"/>
//<property name="hibernate.connection.username" value="${username}"/>
//<property name="hibernate.connection.password" value="${password}"/>
//<property name="class.1" value="com.domain.Person"/>
//<property name="class.2" value="com.domain.Business"/>
//<property name="class.3" value="com.domain.Account"/>
//</transactionManager>
//<dao interface="com.domain.dao.CategoryDao" implementation="com.domain.dao.hbn.HbnCategoryDao"/>
//<dao interface="com.domain.dao.ProductDao" implementation="com.domain.dao.hbn.HbnProductDao"/>
//<dao interface="com.domain.dao.ItemDao" implementation="com.domain.dao.hbn.HbnItemDao"/>
//</context>
//
//<!--===============================================
//Example JDBC DAO Configuration 
//===============================================-->
//
//<context>
//<transactionManager type="JDBC">
//<property name="DataSource" value="SIMPLE"/>
//<property name="JDBC.Driver" value="${driver}"/>
//<property name="JDBC.ConnectionURL" value="${url}"/>
//<property name="JDBC.Username" value="${username}"/>
//<property name="JDBC.Password" value="${password}"/>
//<property name="JDBC.DefaultAutoCommit" value="true" />
//<property name="Pool.MaximumActiveConnections" value="10"/>
//<property name="Pool.MaximumIdleConnections" value="5"/>
//<property name="Pool.MaximumCheckoutTime" value="120000"/>
//<property name="Pool.TimeToWait" value="500"/>
//<property name="Pool.PingQuery" value="select 1 from ACCOUNT"/>
//<property name="Pool.PingEnabled" value="false"/>
//<property name="Pool.PingConnectionsOlderThan" value="1"/>
//<property name="Pool.PingConnectionsNotUsedFor" value="1"/>
//<property name="Pool.QuietMode" value="true"/>
//</transactionManager>
//<dao interface="com.domain.dao.OrderDao" implementation="com.domain.dao.jdbc.JdbcOrderDao"/>
//<dao interface="com.domain.dao.LineItemDao" implementation="com.domain.dao.jdbc.JdbcLineItemDao"/>
//<dao interface="com.domain.dao.CustomerDao" implementation="com.domain.dao.jdbc.JdbcCustomerDao"/>
//</context>

#endregion


namespace IBatisNet.DataAccess.DaoSessionHandlers
{
	/// <summary>
	/// Summary description for NHibernateDaoSession.
	/// </summary>
	public class NHibernateDaoSession : DaoSession
	{
		#region Fields
		private ISessionFactory _factory = null;
		private ISession _session = null;
		private ITransaction _transaction = null;
		private bool _consistent = false;

		#endregion

		#region Properties

		/// <summary>
		/// Changes the vote for transaction to commit (true) or to abort (false).
		/// </summary>
		private bool Consistent
		{
			set
			{
				_consistent = value;
			}
		}

		/// <summary>
		/// 
		/// </summary>
		public ISession Session
		{
			get { return _session; }
		}

		/// <summary>
		/// 
		/// </summary>
		public ISessionFactory Factory
		{
			get { return _factory; }
		}

		/// <summary>
		/// 
		/// </summary>
		public override DataSource DataSource
		{
			get 
			{ 
				throw new DataAccessException("DataSource is not supported with Hibernate.");
			}
		}

		/// <summary>
		/// 
		/// </summary>
		public override IDbConnection Connection
		{
			get { return _session.Connection; }
		}

		/// <summary>
		/// 
		/// </summary>
		public override IDbTransaction Transaction
		{
			get { return (_session.Transaction as IDbTransaction); }
		}	
	
		#endregion

		#region Constructor (s) / Destructor
		/// <summary>
		/// 
		/// </summary>
		/// <param name="daoManager"></param>
		/// <param name="factory"></param>
		public NHibernateDaoSession(DaoManager daoManager, ISessionFactory factory):base(daoManager)
		{			
			_factory = factory;
		}
		#endregion

		#region Methods
		
		/// <summary>
		/// Complete (commit) a transaction
		/// </summary>
		/// <remarks>
		/// Use in 'using' syntax.
		/// </remarks>
		public override void Complete()
		{
			this.Consistent = true;
		}

		/// <summary>
		/// Opens a database connection.
		/// </summary>
		public override void OpenConnection()
		{
			_session = _factory.OpenSession();
		}

		/// <summary>
		/// Closes the connection
		/// </summary>
		public override void CloseConnection()
		{
			_session.Flush();// or Close ?
		}

		/// <summary>
		/// Begins a transaction.
		/// </summary>
		public override void BeginTransaction()
		{
			try 
			{
				_session = _factory.OpenSession();
				_transaction = _session.BeginTransaction();
			} 
			catch (HibernateException e) 
			{
				throw new DataAccessException("Error starting Hibernate transaction.  Cause: " + e, e);
			}
		}

		/// <summary>
		/// Begins a database transaction
		/// </summary>
		/// <param name="openConnection">Open a connection.</param>
		public override void BeginTransaction(bool openConnection)
		{
			if (openConnection)
			{
				this.BeginTransaction();
			}
			else
			{
				if (_session == null)
				{
					throw new DataAccessException("NHibernateDaoSession could not invoke BeginTransaction(). A Connection must be started. Call OpenConnection() first.");
				}
				try 
				{
					_transaction = _session.BeginTransaction();
				}
				catch (HibernateException e) 
				{
					throw new DataAccessException("Error starting Hibernate transaction.  Cause: " + e, e);
				}
			}
		}

		/// <summary>
		/// Begins a transaction at the data source with the specified IsolationLevel value.
		/// </summary>
		/// <param name="isolationLevel">The transaction isolation level for this connection.</param>
		public override void BeginTransaction(IsolationLevel isolationLevel)
		{
			throw new DataAccessException("IsolationLevel is not supported with Hibernate transaction.");
		}

		/// <summary>
		/// Begins a transaction on the current connection
		/// with the specified IsolationLevel value.
		/// </summary>
		/// <param name="isolationLevel">The transaction isolation level for this connection.</param>
		/// <param name="openConnection">Open a connection.</param>
		public override void BeginTransaction(bool openConnection, IsolationLevel isolationLevel)
		{
			throw new DataAccessException("IsolationLevel is not supported with Hibernate transaction.");
		}

		/// <summary>
		/// Commits the database transaction.
		/// </summary>
		/// <remarks>
		/// Will close the session.
		/// </remarks>
		public override void CommitTransaction()
		{
			try 
			{
				_transaction.Commit();
				_session.Close();
			} 
			catch (HibernateException e) 
			{
				throw new DataAccessException("Error committing Hibernate transaction.  Cause: " + e);
			}
		}

		/// <summary>
		/// Commits the database transaction.
		/// </summary>
		/// <param name="closeConnection">Close the session</param>
		public override void CommitTransaction(bool closeConnection)
		{
			try 
			{
				_transaction.Commit();
				if(closeConnection)
				{
					_session.Close();
				}
			} 
			catch (HibernateException e) 
			{
				throw new DataAccessException("Error committing Hibernate transaction.  Cause: " + e);
			}
		}

		/// <summary>
		/// Rolls back a transaction from a pending state.
		/// </summary>
		/// <remarks>
		/// Will close the session.
		/// </remarks>
		public override void RollBackTransaction()
		{
			try 
			{
				_transaction.Rollback();
				_session.Close();
			} 
			catch (HibernateException e) 
			{
				throw new DataAccessException("Error ending Hibernate transaction.  Cause: " + e);
			}		
		}

		/// <summary>
		/// Rolls back a transaction from a pending state.
		/// </summary>
		/// <param name="closeConnection">Close the connection</param>
		public override void RollBackTransaction(bool closeConnection)
		{
			try 
			{
				_transaction.Rollback();
				if(closeConnection)
				{
					_session.Close();
				}
			} 
			catch (HibernateException e) 
			{
				throw new DataAccessException("Error ending Hibernate transaction.  Cause: " + e);
			}		
		}

		/// <summary>
		/// 
		/// </summary>
		/// <param name="commandType"></param>
		/// <returns></returns>
		public override IDbCommand CreateCommand(CommandType commandType)
		{
			throw new DataAccessException("CreateCommand is not supported with Hibernate.");
		}

		/// <summary>
		/// 
		/// </summary>
		/// <returns></returns>
		public override IDataParameter CreateDataParameter()
		{
			throw new DataAccessException("CreateDataParameter is not supported with Hibernate.");
		}

		/// <summary>
		/// 
		/// </summary>
		/// <returns></returns>
		public override IDbDataAdapter CreateDataAdapter()
		{
			throw new DataAccessException("CreateDataAdapter is not supported with Hibernate.");
		}

		/// <summary>
		/// 
		/// </summary>
		/// <param name="command"></param>
		/// <returns></returns>
		public override IDbDataAdapter CreateDataAdapter(IDbCommand command)
		{
			throw new DataAccessException("CreateDataAdapter is not supported with Hibernate.");
		}
		#endregion

		#region IDisposable Members
		/// <summary>
		/// Releasing, or resetting resources.
		/// </summary>
		public override void Dispose()
		{
			if (_consistent)
			{
				this.CommitTransaction();
			}
			else
			{
				this.RollBackTransaction();
			}
			_session.Dispose();
		}
		#endregion

	}
}
