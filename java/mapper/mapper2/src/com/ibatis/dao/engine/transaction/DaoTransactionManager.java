package com.ibatis.dao.engine.transaction;

import com.ibatis.dao.client.DaoTransaction;

import java.util.Properties;

/**
 * <p/>
 * Date: Jan 27, 2004 10:41:00 PM
 *
 * @author Clinton Begin
 */
public interface DaoTransactionManager {

  public void configure(Properties properties);

  public DaoTransaction startTransaction();

  public void commitTransaction(DaoTransaction trans);

  public void rollbackTransaction(DaoTransaction trans);

}
