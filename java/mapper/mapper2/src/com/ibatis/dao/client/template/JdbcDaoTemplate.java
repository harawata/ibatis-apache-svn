package com.ibatis.dao.client.template;

import com.ibatis.dao.client.Dao;
import com.ibatis.dao.client.DaoManager;
import com.ibatis.dao.engine.transaction.jdbc.JdbcDaoTransaction;

import java.sql.Connection;

/**
 * A DaoTemplate for JDBC implementations that provides a
 * convenient method to access the JDBC Connection.
 * 
 * <p/>
 * Date: Feb 22, 2004 3:21:25 PM
 * 
 * @author Clinton Begin
 */
public abstract class JdbcDaoTemplate extends DaoTemplate {

  /**
   * The DaoManager that manages this Dao instance will be passed
   * in as the parameter to this constructor automatically upon
   * instantiation.
   *
    * @param daoManager
   */
  public JdbcDaoTemplate(DaoManager daoManager) {
    super(daoManager);
  }

  /**
   * Gets the JDBC Connection associated with the current
   * DaoTransaction that this Dao is working under.
   *
   * @return A JDBC Connection instance.
   */ 
  protected Connection getConnection() {
    JdbcDaoTransaction trans = (JdbcDaoTransaction) daoManager.getTransaction(this);
    return trans.getConnection();
  }

}
