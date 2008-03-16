
#region Apache Notice
/*****************************************************************************
 * $Header: $
 * $Revision: 513043 $
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

using System.Data;

namespace Apache.Ibatis.DataMapper.Session.Transaction
{
    /// <summary>
    /// Implement the <see cref="ITransactionManager" /> using ADO.NET <see cref="IDbTransaction"/>
    /// </summary>
    public class AdoTransactionManager : ITransactionManager
    {
        #region ITransactionManager Members

        /// <summary>
        /// Begin a transaction and return the associated <c>ITransaction</c> instance
        /// </summary>
        /// <param name="session"></param>
        /// <returns></returns>
        public ITransaction BeginTransaction(ISession session)
        {
            return BeginTransaction(session, IsolationLevel.Unspecified);
        }

        /// <summary>
        /// Begin a transaction with the specified isolation level and return
        /// the associated <c>ITransaction</c> instance
        /// </summary>
        /// <param name="session"></param>
        /// <param name="isolationLevel"></param>
        /// <returns></returns>
        public ITransaction BeginTransaction(ISession session, IsolationLevel isolationLevel)
        {
            ITransaction transaction = new AdoTransaction(session);
            transaction.Begin(isolationLevel);
            return transaction;
        }

        #endregion
    }
}
