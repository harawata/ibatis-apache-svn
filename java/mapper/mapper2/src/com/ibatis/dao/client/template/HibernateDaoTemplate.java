package com.ibatis.dao.client.template;

import com.ibatis.dao.engine.transaction.hibernate.HibernateDaoTransaction;
import com.ibatis.dao.client.DaoManager;
import net.sf.hibernate.Session;

/**
 * A DaoTemplate for Hibernate implementations that provides a
 * convenient method to access the Hibernate Session.
 *
 * <p/>
 * Date: Feb 22, 2004 3:37:58 PM
 * 
 * @author Clinton Begin
 */
public abstract class HibernateDaoTemplate extends DaoTemplate {

  /**
   * The DaoManager that manages this Dao instance will be passed
   * in as the parameter to this constructor automatically upon
   * instantiation.
   *
    * @param daoManager
   */
  public HibernateDaoTemplate(DaoManager daoManager) {
    super(daoManager);
  }

  /**
   * Gets the Hibernate session associated with the current
   * DaoTransaction that this Dao is working under.
   *
   * @return A Hibernate Session instance.
   */ 
  protected Session getSession() {
    HibernateDaoTransaction trans = (HibernateDaoTransaction) daoManager.getTransaction(this);
    return trans.getSession();
  }

}
