package com.ibatis.sqlmap.engine.mapping.result.loader;

import com.ibatis.common.beans.ClassInfo;
import com.ibatis.common.exception.NestedRuntimeException;
import com.ibatis.sqlmap.engine.impl.ExtendedSqlMapClient;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/**
 * User: Clinton Begin
 * Date: Dec 29, 2003
 * Time: 7:28:03 PM
 */
public class LazyResultLoader implements InvocationHandler {

  private static final Class[] LIST_INTERFACES = new Class[]{List.class};

  protected ExtendedSqlMapClient client;
  protected String statementName;
  protected Object parameterObject;
  protected Class targetType;

  protected boolean loaded;
  protected Object resultObject;

  public LazyResultLoader(ExtendedSqlMapClient client, String statementName, Object parameterObject, Class targetType) {
    this.client = client;
    this.statementName = statementName;
    this.parameterObject = parameterObject;
    this.targetType = targetType;
  }

  public Object loadResult() throws SQLException {
    if (Collection.class.isAssignableFrom(targetType)) {
      InvocationHandler handler = new LazyResultLoader(client, statementName, parameterObject, targetType);
      ClassLoader cl = targetType.getClassLoader();
      return Proxy.newProxyInstance(cl, LIST_INTERFACES, handler);
    } else {
      return ResultLoader.getResult(client, statementName, parameterObject, targetType);
    }
  }

  public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
    loadObject();
    if (resultObject != null) {
      try {
        return method.invoke(resultObject, objects);
      } catch (Throwable t) {
        throw ClassInfo.unwrapThrowable(t);
      }
    } else {
      return null;
    }
  }

  private synchronized void loadObject() {
    if (!loaded) {
      try {
        loaded = true;
        resultObject = ResultLoader.getResult(client, statementName, parameterObject, targetType);
      } catch (SQLException e) {
        throw new NestedRuntimeException("Error lazy loading result. Cause: " + e, e);
      }
    }
  }

}
