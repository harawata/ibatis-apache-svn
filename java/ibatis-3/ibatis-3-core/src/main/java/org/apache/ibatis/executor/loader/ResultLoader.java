package org.apache.ibatis.executor.loader;

import org.apache.ibatis.executor.*;
import org.apache.ibatis.mapping.MappedStatement;

import java.sql.SQLException;
import java.util.*;

public class ResultLoader {

  protected static final Class[] LIST_INTERFACES = new Class[]{List.class};
  protected static final Class[] SET_INTERFACES = new Class[]{Set.class};

  protected final Executor executor;
  protected final MappedStatement mappedStatement;
  protected final Object parameterObject;
  protected final Class targetType;

  protected boolean loaded;
  protected Object resultObject;

  public ResultLoader(Executor executor, MappedStatement mappedStatement, Object parameterObject, Class targetType) {
    this.executor = executor;
    this.mappedStatement = mappedStatement;
    this.parameterObject = parameterObject;
    this.targetType = targetType;
  }

  public Object loadResult() throws SQLException {
    List list = executor.query(mappedStatement, parameterObject, Executor.NO_ROW_OFFSET, Executor.NO_ROW_LIMIT, Executor.NO_RESULT_HANDLER);
    if (targetType != null && Set.class.isAssignableFrom(targetType)) {
      resultObject = new HashSet(list);
    } else if (targetType != null && Collection.class.isAssignableFrom(targetType)) {
      resultObject = list;
    } else if (targetType != null && targetType.isArray()) {
      resultObject = listToArray(list, targetType.getComponentType());
    } else {
      if (list.size() > 1) {
        throw new ExecutorException("Statement " + mappedStatement.getId() + " returned more than one row, where no more than one was expected.");
      } else if (list.size() == 1) {
        resultObject = list.get(0);
      }
    }
    return resultObject;
  }

  public boolean wasNull() {
    return resultObject == null;
  }

  private Object[] listToArray(List list, Class type) {
    Object array = java.lang.reflect.Array.newInstance(type, list.size());
    array = list.toArray((Object[]) array);
    return (Object[]) array;
  }

}
