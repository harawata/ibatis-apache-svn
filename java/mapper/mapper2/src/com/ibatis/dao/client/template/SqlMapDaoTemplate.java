package com.ibatis.dao.client.template;

import com.ibatis.dao.client.DaoManager;
import com.ibatis.dao.engine.transaction.sqlmap.SqlMapDaoTransaction;
import com.ibatis.sqlmap.client.SqlMapExecutor;
import com.ibatis.sqlmap.client.SqlMapTransactionManager;

/**
 * A DaoTemplate for SQL Map implementations that provides a
 * convenient method to access the SqlMapExecutor.
 * <p/>
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

  /**
   * Gets the SQL Map Transaction Manager associated with the current
   * DaoTransaction that this Dao is working under.  The SqlMapExecutor
   * interface declares a number of methods for executing statements
   * via an SqlMapClient instance.
   * <p/>
   * NOTE: It is rare to require this in a DAO.  Only very special
   * cases of DAO implementations will require access to the
   * SqlMapTransactionManager.  Messing with transactions at this
   * level might be dangerous to your data integrity (e.g. committing
   * too early).
   *
   * @return A SqlMapTransactionManager instance.
   */
  protected SqlMapTransactionManager getSqlMapTransactionManager() {
    SqlMapDaoTransaction trans = (SqlMapDaoTransaction) daoManager.getTransaction(this);
    return trans.getSqlMap();
  }

}
