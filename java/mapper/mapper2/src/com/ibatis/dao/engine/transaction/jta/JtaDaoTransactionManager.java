package com.ibatis.dao.engine.transaction.jta;

import com.ibatis.dao.engine.transaction.DaoTransactionManager;
import com.ibatis.dao.client.DaoTransaction;
import com.ibatis.dao.client.DaoException;

import javax.sql.DataSource;
import javax.transaction.UserTransaction;
import javax.naming.InitialContext;
import java.util.Map;

/**
 *
 *
 * <p>
 * Date: Jan 27, 2004 10:48:39 PM
 * @author Clinton Begin
 */
public class JtaDaoTransactionManager implements DaoTransactionManager {

  private DataSource dataSource;
  private UserTransaction userTransaction;

  public void configure(Map properties) {
    String utxName = null;
    String dsName = null;
    try {
      utxName = (String) properties.get("UserTransaction");
      InitialContext initCtx = new InitialContext();
      userTransaction = (UserTransaction) initCtx.lookup(utxName);
      dsName = (String) properties.get("DBJndiContext");
      dataSource = (DataSource) initCtx.lookup(dsName);
    } catch (Exception e) {
      throw new DaoException("Error initializing JTA transaction while looking up UserTransaction (" + utxName + ") or DataSource ("+dsName+").  Cause: " + e);
    }
  }

  public DaoTransaction startTransaction() {
    return new JtaDaoTransaction(userTransaction, dataSource);
  }

  public void commitTransaction(DaoTransaction trans) {
    ((JtaDaoTransaction)trans).commit();
  }

  public void rollbackTransaction(DaoTransaction trans) {
    ((JtaDaoTransaction)trans).rollback();
  }
}
