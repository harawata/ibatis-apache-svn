package com.ibatis.dao.client.template;

import com.ibatis.sqlmap.client.SqlMapExecutor;
import com.ibatis.dao.engine.transaction.sqlmap.SqlMapDaoTransaction;
import com.ibatis.dao.client.DaoManager;

/**
 * A DaoTemplate for SQL Map implementations that provides a 
 * convenient method to access the SqlMapExecutor.
 *
 * <p/>
 * Date: Feb 22, 2004 3:26:53 PM
 * 
 * @author Clinton Begin
 */
public abstract class SqlMapDaoTemplate extends DaoTemplate {

  /**
   * The DaoManager that manages this Dao instance will be passed
   * in as the parameter to this constructor automatically upon
   * instantiation.
   *
    * @param daoManager
   */
  public SqlMapDaoTemplate(DaoManager daoManager) {
    super(daoManager);
  }

  /**
   * Gets the SQL Map Executor associated with the current
   * DaoTransaction that this Dao is working under.  The SqlMapExecutor
   * interface declares a number of methods for executing statements
   * via an SqlMapClient instance.
   *
   * @return A SqlMapExecutor instance.
   */
  protected SqlMapExecutor getSqlMapExecutor() {
    SqlMapDaoTransaction trans = (SqlMapDaoTransaction) daoManager.getTransaction(this);
    return trans.getSqlMap();
  }

}
