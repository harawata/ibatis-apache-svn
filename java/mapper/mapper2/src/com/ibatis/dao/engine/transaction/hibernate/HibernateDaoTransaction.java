package com.ibatis.dao.engine.transaction.hibernate;

import com.ibatis.dao.client.DaoTransaction;
import com.ibatis.dao.client.DaoException;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.Transaction;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;

/**
 *
 *
 * <p>
 * Date: Jan 27, 2004 10:49:12 PM
 * @author Clinton Begin
 */
public class HibernateDaoTransaction implements DaoTransaction {

  private Session session;
  private Transaction transaction;

  public HibernateDaoTransaction(SessionFactory factory) {
    try {
      this.session = factory.openSession();
      this.transaction = session.beginTransaction();
    } catch (HibernateException e) {
      throw new DaoException("Error starting Hibernate transaction.  Cause: " + e, e);
    }
  }

  public void commit() {
    try {
      transaction.commit();
      session.close();
    } catch (HibernateException e) {
      throw new DaoException("Error committing Hibernate transaction.  Cause: " + e);
    }
  }

  public void rollback() {
    try {
      transaction.rollback();
      session.close();
    } catch (HibernateException e) {
      throw new DaoException("Error ending Hibernate transaction.  Cause: " + e);
    }
  }

  public Session getSession() {
    return session;
  }

}
