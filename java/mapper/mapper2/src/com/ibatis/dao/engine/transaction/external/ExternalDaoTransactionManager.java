package com.ibatis.dao.engine.transaction.external;

import com.ibatis.dao.engine.transaction.DaoTransactionManager;
import com.ibatis.dao.client.DaoTransaction;

import java.util.Map;

/**
 * <p/>
 * Date: Feb 22, 2004 12:10:36 PM
 * 
 * @author Clinton Begin
 */
public class ExternalDaoTransactionManager implements DaoTransactionManager {

  public void configure(Map properties) {
    // Do nothing
  }

  public DaoTransaction startTransaction() {
    return new ExternalDaoTransaction();
  }

  public void commitTransaction(DaoTransaction trans) {
    ((ExternalDaoTransaction)trans).commit();
  }

  public void rollbackTransaction(DaoTransaction trans) {
    ((ExternalDaoTransaction)trans).rollback();
  }

}
