package com.ibatis.dao.client.template;

import com.ibatis.dao.engine.transaction.jta.JtaDaoTransaction;
import com.ibatis.dao.client.DaoManager;

import java.sql.Connection;

/**
 * A DaoTemplate for JTA implementations that provides a
 * convenient method to access the JDBC Connection.
 * 
 * <p/>
 * Date: Feb 22, 2004 3:25:54 PM
 * 
 * @author Clinton Begin
 */
public abstract class JtaDaoTemplate extends DaoTemplate {

  /**
   * The DaoManager that manages this Dao instance will be passed
   * in as the parameter to this constructor automatically upon
   * instantiation.
   *
    * @param daoManager
   */
  public JtaDaoTemplate(DaoManager daoManager) {
    super(daoManager);
  }

  /**
   * Gets the JDBC Connection associated with the current
   * DaoTransaction that this Dao is working under.   In the
   * case of JTA, the current transaction generally belongs to
   * a wider scope (global) transaction.
   *
   * @return A JDBC Connection instance.
   */
  protected Connection getConnection() {
    JtaDaoTransaction trans = (JtaDaoTransaction) daoManager.getTransaction(this);
    return trans.getConnection();
  }

}
