/*
 *  Copyright 2004 Clinton Begin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.ibatis.dao.engine.transaction.jta;

import com.ibatis.dao.client.DaoException;
import com.ibatis.dao.client.DaoTransaction;
import com.ibatis.dao.engine.transaction.DaoTransactionManager;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;
import java.util.Properties;

/**
 * <p/>
 * Date: Jan 27, 2004 10:48:39 PM
 *
 * @author Clinton Begin
 */
public class JtaDaoTransactionManager implements DaoTransactionManager {

  private DataSource dataSource;
  private UserTransaction userTransaction;

  public void configure(Properties properties) {
    String utxName = null;
    String dsName = null;
    try {
      utxName = (String) properties.get("UserTransaction");
      InitialContext initCtx = new InitialContext();
      userTransaction = (UserTransaction) initCtx.lookup(utxName);
      dsName = (String) properties.get("DBJndiContext");
      dataSource = (DataSource) initCtx.lookup(dsName);
    } catch (Exception e) {
      throw new DaoException("Error initializing JTA transaction while looking up UserTransaction (" + utxName + ") or DataSource (" + dsName + ").  Cause: " + e);
    }
  }

  public DaoTransaction startTransaction() {
    return new JtaDaoTransaction(userTransaction, dataSource);
  }

  public void commitTransaction(DaoTransaction trans) {
    ((JtaDaoTransaction) trans).commit();
  }

  public void rollbackTransaction(DaoTransaction trans) {
    ((JtaDaoTransaction) trans).rollback();
  }
}
