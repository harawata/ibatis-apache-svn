package com.ibatis.dao.engine.transaction;

import com.ibatis.dao.client.*;

import java.util.*;

/**
 *
 *
 * <p>
 * Date: Jan 27, 2004 10:41:00 PM
 * @author Clinton Begin
 */
public interface DaoTransactionManager {

  public void configure(Map properties);

  public DaoTransaction startTransaction();

  public void commitTransaction(DaoTransaction trans);

  public void rollbackTransaction(DaoTransaction trans);

}
