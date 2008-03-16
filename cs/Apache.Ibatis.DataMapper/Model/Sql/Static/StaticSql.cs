
#region Apache Notice
/*****************************************************************************
 * $Revision: 476843 $
 * $LastChangedDate$
 * $LastChangedBy$
 * 
 * iBATIS.NET Data Mapper
 * Copyright (C) 2006/2005 - The Apache Software Foundation
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

using Apache.Ibatis.DataMapper.Model.Statements;
using Apache.Ibatis.DataMapper.DataExchange;
using Apache.Ibatis.DataMapper.MappedStatements;
using Apache.Ibatis.DataMapper.Scope;
using Apache.Ibatis.DataMapper.Session;
using Apache.Ibatis.DataMapper.Data;
using Apache.Ibatis.Common.Contracts;

#endregion

namespace Apache.Ibatis.DataMapper.Model.Sql.Static
{
	/// <summary>
	/// Summary description for StaticSql.
	/// </summary>
	public sealed class StaticSql : ISql
	{
        private readonly IStatement statement = null;
        private PreparedStatement preparedStatement = null;
        private readonly DataExchangeFactory dataExchangeFactory = null;
        private readonly DBHelperParameterCache dbHelperParameterCache = null;

		#region Constructor (s) / Destructor

        /// <summary>
        /// Initializes a new instance of the <see cref="StaticSql"/> class.
        /// </summary>
        /// <param name="dataExchangeFactory">The data exchange factory.</param>
        /// <param name="dbHelperParameterCache">The db helper parameter cache.</param>
        /// <param name="statement">The statement.</param>
        public StaticSql(
            DataExchangeFactory dataExchangeFactory,
            DBHelperParameterCache dbHelperParameterCache,
            IStatement statement)
		{
            Contract.Require.That(dataExchangeFactory, Is.Not.Null).When("retrieving argument dataExchangeFactory in StaticSql constructor");
            Contract.Require.That(dbHelperParameterCache, Is.Not.Null).When("retrieving argument dbHelperParameterCache in StaticSql constructor");
            Contract.Require.That(statement, Is.Not.Null).When("retrieving argument statement in StaticSql constructor");

            this.statement = statement;
            this.dataExchangeFactory = dataExchangeFactory;
            this.dbHelperParameterCache = dbHelperParameterCache;
		}
		#endregion

		#region ISql Members

        /// <summary>
        /// Builds a new <see cref="RequestScope"/> and the sql command text to execute.
        /// </summary>
        /// <param name="mappedStatement">The <see cref="IMappedStatement"/>.</param>
        /// <param name="parameterObject">The parameter object (used in DynamicSql)</param>
        /// <param name="session">The current session</param>
        /// <returns>A new <see cref="RequestScope"/>.</returns>
		public RequestScope GetRequestScope(
            IMappedStatement mappedStatement,
			object parameterObject,
            ISession session)
		{
			RequestScope request = new RequestScope(dataExchangeFactory, session, statement);

			request.PreparedStatement = preparedStatement;
			request.MappedStatement = mappedStatement;

			return request;
		}

        /// <summary>
        /// Build the PreparedStatement
        /// </summary>
        /// <param name="session">The session.</param>
        /// <param name="sqlStatement">The SQL statement.</param>
		public void BuildPreparedStatement(ISession session, string sqlStatement)
		{
			RequestScope request = new RequestScope( dataExchangeFactory, session, statement);

            PreparedStatementFactory factory = new PreparedStatementFactory(session, dbHelperParameterCache, request, statement, sqlStatement);
			preparedStatement = factory.Prepare();
		}

		#endregion
	}
}
