/*
 *  Copyright 2004 Clinton Begin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.ibatis.sqlmap.engine.mapping.result.loader;

import com.ibatis.common.beans.ClassInfo;
import com.ibatis.common.exception.NestedRuntimeException;
import com.ibatis.sqlmap.engine.impl.ExtendedSqlMapClient;
import com.ibatis.sqlmap.engine.type.DomTypeMarker;
import com.ibatis.sqlmap.engine.type.XmlTypeMarker;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.InvocationHandler;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/**
 * <p/>
 * Date: Jan 11, 2004 8:06:19 PM
 *
 * @author Clinton Begin
 */
public class EnhancedLazyResultLoader {

  private static final Class[] INTERFACES = new Class[]{List.class};
  private Object loader;


  public EnhancedLazyResultLoader(ExtendedSqlMapClient client, String statementName, Object parameterObject, Class targetType) {
    loader = new EnhancedLazyResultLoaderImpl(client, statementName, parameterObject, targetType);
  }

  public Object loadResult() throws SQLException {
    return ((EnhancedLazyResultLoaderImpl) loader).loadResult();
  }


  private static class EnhancedLazyResultLoaderImpl implements InvocationHandler {


    protected ExtendedSqlMapClient client;
    protected String statementName;
    protected Object parameterObject;
    protected Class targetType;

    protected boolean loaded;
    protected Object resultObject;

    public EnhancedLazyResultLoaderImpl(ExtendedSqlMapClient client, String statementName, Object parameterObject, Class targetType) {
      this.client = client;
      this.statementName = statementName;
      this.parameterObject = parameterObject;
      this.targetType = targetType;
    }

    public Object loadResult() throws SQLException {
      if (DomTypeMarker.class.isAssignableFrom(targetType)) {
        return ResultLoader.getResult(client, statementName, parameterObject, targetType);
      } else if (XmlTypeMarker.class.isAssignableFrom(targetType)) {
        return ResultLoader.getResult(client, statementName, parameterObject, targetType);
      } else if (Collection.class.isAssignableFrom(targetType)) {
        return Enhancer.create(Object.class, INTERFACES, this);
      } else if (targetType.isArray() || ClassInfo.isKnownType(targetType)) {
        return ResultLoader.getResult(client, statementName, parameterObject, targetType);
      } else {
        return Enhancer.create(targetType, this);
      }
    }

    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
      if ("finalize".hashCode() == method.getName().hashCode()
          && "finalize".equals(method.getName())) {
        return null;
      } else {
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


}
