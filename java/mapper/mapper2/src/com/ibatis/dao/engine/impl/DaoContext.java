package com.ibatis.dao.engine.impl;

import com.ibatis.dao.engine.transaction.*;
import com.ibatis.dao.client.*;

import java.util.*;

/**
 *
 *
 * <p>
 * Date: Jan 29, 2004 11:46:41 PM
 * @author Clinton Begin
 */
public class DaoContext {

  private StandardDaoManager daoManager;
  private DaoTransactionManager transactionManager;
  private ThreadLocal transaction = new ThreadLocal();
  private ThreadLocal state = new ThreadLocal();

  private Map typeDaoImplMap = new HashMap();

  public DaoContext() {
  }

  public StandardDaoManager getDaoManager() {
    return daoManager;
  }

  public void setDaoManager(StandardDaoManager daoManager) {
    this.daoManager = daoManager;
  }

  public DaoTransactionManager getTransactionManager() {
    return transactionManager;
  }

  public void setTransactionManager(DaoTransactionManager transactionManager) {
    this.transactionManager = transactionManager;
  }

  public void addDao (DaoImpl daoImpl) {
    typeDaoImplMap.put(daoImpl.getDaoInterface(), daoImpl);
  }

  public Dao getDao (Class iface) {
    DaoImpl impl = (DaoImpl) typeDaoImplMap.get(iface);
    if (impl == null) {
      throw new DaoException ("There is no DAO implementation found for " + iface + " in this context.");
    }
    return impl.getProxy();
  }

  public Iterator getDaoImpls () {
    return typeDaoImplMap.values().iterator();
  }

  public DaoTransaction getTransaction() {
    startTransaction();
    return (DaoTransaction)transaction.get();
  }

  public void startTransaction() {
    if (state.get() != DaoTransactionState.ACTIVE) {
      DaoTransaction trans = transactionManager.startTransaction();
      transaction.set(trans);
      state.set(DaoTransactionState.ACTIVE);
      daoManager.addContextInTransaction(this);
    }
  }

  public void commitTransaction() {
    DaoTransaction trans = (DaoTransaction)transaction.get();
    if (state.get() == DaoTransactionState.ACTIVE) {
      transactionManager.commitTransaction(trans);
      state.set(DaoTransactionState.COMMITTED);
    } else {
      state.set(DaoTransactionState.INACTIVE);
    }
  }

  public void endTransaction() {
    DaoTransaction trans = (DaoTransaction)transaction.get();
    if (state.get() == DaoTransactionState.ACTIVE) {
      try {
        transactionManager.rollbackTransaction(trans);
      } finally {
        state.set(DaoTransactionState.ROLLEDBACK);
        transaction.set(null);
      }
    } else {
      state.set(DaoTransactionState.INACTIVE);
      transaction.set(null);
    }
  }

}
