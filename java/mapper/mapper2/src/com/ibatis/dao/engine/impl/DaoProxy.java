package com.ibatis.dao.engine.impl;

import com.ibatis.dao.client.*;
import com.ibatis.common.beans.ClassInfo;

import java.lang.reflect.*;
import java.util.Set;
import java.util.HashSet;

/**
 * <p/>
 * Date: Jan 30, 2004 12:15:03 AM
 *
 * @author Clinton Begin
 */
public class DaoProxy implements InvocationHandler {

  private static final Set PASSTHROUGH_METHODS = new HashSet();

  private DaoImpl daoImpl;

  static {
    PASSTHROUGH_METHODS.add("equals");
    PASSTHROUGH_METHODS.add("getClass");
    PASSTHROUGH_METHODS.add("hashCode");
    PASSTHROUGH_METHODS.add("notify");
    PASSTHROUGH_METHODS.add("notifyAll");
    PASSTHROUGH_METHODS.add("toString");
    PASSTHROUGH_METHODS.add("wait");
  }

  public DaoProxy(DaoImpl daoImpl) {
    this.daoImpl = daoImpl;
  }

  public Object invoke(Object proxy, Method method, Object[] args)
      throws Throwable {
    Object result = null;
    if (PASSTHROUGH_METHODS.contains(method.getName())) {
      try {
        result = method.invoke(daoImpl.getDaoInstance(), args);
      } catch (Throwable t) {
        throw ClassInfo.unwrapThrowable(t);
      }
    } else {
      StandardDaoManager daoManager = daoImpl.getDaoManager();
      DaoContext context = daoImpl.getDaoContext();

      if (daoManager.isExplicitTransaction()) {
        // Just start the transaction (explicit)
        try {
          context.startTransaction();
          result = method.invoke(daoImpl.getDaoInstance(), args);
        } catch (Throwable t) {
          throw ClassInfo.unwrapThrowable(t);
        }
      } else {
        // Start, commit and end the transaction (autocommit)
        try {
          context.startTransaction();
          result = method.invoke(daoImpl.getDaoInstance(), args);
          context.commitTransaction();
        } catch (Throwable t) {
          throw ClassInfo.unwrapThrowable(t);
        } finally {
          context.endTransaction();
        }
      }

    }
    return result;
  }

  public static Dao newInstance(DaoImpl daoImpl) {
    return (Dao) Proxy.newProxyInstance(daoImpl.getDaoInterface().getClassLoader(),
        new Class[]{Dao.class, daoImpl.getDaoInterface()},
        new DaoProxy(daoImpl));
  }

}
