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
  private ThreadLocal contextInTransactionList = new ThreadLocal();

  private Map typeContextMap = new HashMap();
  private Map daoImplMap = new HashMap();

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
    List ctxList = getContextInTransactionList();
    Iterator i = ctxList.iterator();
    while (i.hasNext()) {
      DaoContext context = (DaoContext) i.next();
      context.commitTransaction();
    }
  }

  public void endTransaction() {
    List ctxList = getContextInTransactionList();
    try {
      Iterator i = ctxList.iterator();
      while (i.hasNext()) {
        DaoContext context = (DaoContext) i.next();
        context.endTransaction();
      }
    } finally {
      transactionMode.set(null);
      ctxList.clear();
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
    List ctxList = getContextInTransactionList();
    if (!ctxList.contains(ctx)) {
      ctxList.add(ctx);
    }
  }

  private List getContextInTransactionList() {
    List list = (List) contextInTransactionList.get();
    if (list == null) {
      list = new ArrayList();
      contextInTransactionList.set(list);
    }
    return list;
  }

}


