package com.ibatis.dao.engine.impl;

import com.ibatis.dao.client.*;

import java.util.*;

/**
 *
 *
 * <p>
 * Date: Jan 27, 2004 10:47:28 PM
 * @author Clinton Begin
 */
public class StandardDaoManager implements DaoManager {

  private static final String DAO_EXPLICIT_TX = "__DAO_EXPLICIT_TX";

  private ThreadLocal transactionMode = new ThreadLocal();

  private Map typeContextMap = new HashMap();
  private Map daoImplMap = new HashMap();

  private List contextInTransactionList = new ArrayList();

  public void addContext(DaoContext context) {
    Iterator i = context.getDaoImpls();
    while (i.hasNext()) {
      DaoImpl daoImpl = (DaoImpl) i.next();
      typeContextMap.put(daoImpl.getDaoInterface(), context);
      daoImplMap.put(daoImpl.getProxy(), daoImpl);
      daoImplMap.put(daoImpl.getDaoInstance(), daoImpl);
    }
  }

  public Dao getDao(Class iface) {
    DaoContext context = (DaoContext) typeContextMap.get(iface);
    if (context == null) {
      throw new DaoException ("There is no DAO implementation found for " + iface + " in any context.");
    }
    return context.getDao(iface);
  }

  public void startTransaction() {
    transactionMode.set(DAO_EXPLICIT_TX);
  }

  public void commitTransaction() {
    Iterator i = contextInTransactionList.iterator();
    while (i.hasNext()) {
      DaoContext context = (DaoContext) i.next();
      context.commitTransaction();
    }
  }

  public void endTransaction() {
    try {
      Iterator i = contextInTransactionList.iterator();
      while (i.hasNext()) {
        DaoContext context = (DaoContext) i.next();
        context.endTransaction();
      }
    } finally {
      transactionMode.set(null);
      contextInTransactionList.clear();
    }
  }

  public DaoTransaction getTransaction(Dao dao) {
    DaoImpl impl = (DaoImpl)daoImplMap.get(dao);
    return impl.getDaoContext().getTransaction();
  }

  public boolean isExplicitTransaction() {
    return DAO_EXPLICIT_TX.equals(transactionMode.get());
  }

  public void addContextInTransaction (DaoContext ctx) {
    if (!contextInTransactionList.contains(ctx)) {
      contextInTransactionList.add(ctx);
    }
  }

}


