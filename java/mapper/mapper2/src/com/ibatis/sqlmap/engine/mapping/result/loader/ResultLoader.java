package com.ibatis.sqlmap.engine.mapping.result.loader;

import com.ibatis.sqlmap.engine.impl.ExtendedSqlMapClient;
import com.ibatis.sqlmap.engine.type.DomCollectionTypeMarker;
import com.ibatis.sqlmap.engine.type.XmlCollectionTypeMarker;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/**
 * User: Clinton Begin
 * Date: Dec 29, 2003
 * Time: 7:45:26 PM
 */
public class ResultLoader {

  private ResultLoader() {
  }

  public static Object loadResult(ExtendedSqlMapClient client, String statementName, Object parameterObject, Class targetType)
      throws SQLException {
    Object value = null;


    if (client.isLazyLoadingEnabled()) {
      if (client.isEnhancementEnabled()) {
        EnhancedLazyResultLoader lazy = new EnhancedLazyResultLoader(client, statementName, parameterObject, targetType);
        value = lazy.loadResult();
      } else {
        LazyResultLoader lazy = new LazyResultLoader(client, statementName, parameterObject, targetType);
        value = lazy.loadResult();
      }
    } else {
      value = getResult(client, statementName, parameterObject, targetType);
    }

    return value;
  }

  protected static Object getResult(ExtendedSqlMapClient client, String statementName, Object parameterObject, Class targetType) throws SQLException {
    Object value = null;
    if (XmlCollectionTypeMarker.class.isAssignableFrom(targetType)) {
      value = client.queryForList(statementName, parameterObject);
    } else if (DomCollectionTypeMarker.class.isAssignableFrom(targetType)) {
      value = client.queryForList(statementName, parameterObject);
    } else if (Collection.class.isAssignableFrom(targetType)) {
      value = client.queryForList(statementName, parameterObject);
    } else if (targetType.isArray()) {
      List list = client.queryForList(statementName, parameterObject);
      value = listToArray(list, targetType.getComponentType());
    } else {
      value = client.queryForObject(statementName, parameterObject);
    }
    return value;
  }


  private static Object[] listToArray(List list, Class type) {
    Object array = java.lang.reflect.Array.newInstance(type, list.size());
    array = list.toArray((Object[]) array);
    return (Object[]) array;
  }

}
