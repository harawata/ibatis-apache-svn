package com.ibatis.dao.engine.transaction.external;

import com.ibatis.dao.client.DaoTransaction;

/**
 * <p/>
 * Date: Feb 22, 2004 12:10:27 PM
 * 
 * @author Clinton Begin
 */
public class ExternalDaoTransaction implements DaoTransaction {

  public void commit() {
    // Do nothing
  }

  public void rollback() {
    // Do nothing
  }

}
