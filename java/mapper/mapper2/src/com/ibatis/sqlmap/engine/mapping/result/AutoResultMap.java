package com.ibatis.sqlmap.engine.mapping.result;

import com.ibatis.common.beans.ClassInfo;
import com.ibatis.common.exception.NestedRuntimeException;
import com.ibatis.sqlmap.client.SqlMapException;
import com.ibatis.sqlmap.engine.scope.RequestScope;
import com.ibatis.sqlmap.engine.type.DomTypeMarker;
import com.ibatis.sqlmap.engine.type.TypeHandlerFactory;
import com.ibatis.sqlmap.engine.type.XmlTypeMarker;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: Clinton Begin
 * Date: Nov 9, 2003
 * Time: 10:24:07 PM
 */
public class AutoResultMap extends BasicResultMap {

  public synchronized Object[] getResults(RequestScope request, ResultSet rs)
      throws SQLException {
    if (resultMappings == null) {
      initialize(rs);
    }
    return super.getResults(request, rs);
  }

  private void initialize(ResultSet rs) {
    if (resultClass == null) {
      throw new SqlMapException("The automatic ResultMap named " + this.getId() + " had a null result class (not allowed).");
    } else if (Map.class.isAssignableFrom(resultClass)) {
      initializeMapResults(rs);
    } else if (TypeHandlerFactory.getTypeHandler(resultClass) != null) {
      initializePrimitiveResults(rs);
    } else if (DomTypeMarker.class.isAssignableFrom(resultClass)
        || XmlTypeMarker.class.isAssignableFrom(resultClass)) {
      initializeXmlResults(rs);
    } else {
      initializeBeanResults(rs);
    }
  }

  private void initializeBeanResults(ResultSet rs) {
    try {
      ClassInfo classInfo = ClassInfo.getInstance(resultClass);
      String[] propertyNames = classInfo.getWriteablePropertyNames();

      Map propertyMap = new HashMap();
      for (int i = 0; i < propertyNames.length; i++) {
        propertyMap.put(propertyNames[i].toUpperCase(), propertyNames[i]);
      }

      List resultMappingList = new ArrayList();
      ResultSetMetaData rsmd = rs.getMetaData();
      for (int i = 0, n = rsmd.getColumnCount(); i < n; i++) {
        String columnName = rsmd.getColumnLabel(i + 1);
        String upperColumnName = columnName.toUpperCase();
        String matchedProp = (String) propertyMap.get(upperColumnName);
        if (matchedProp != null) {
          BasicResultMapping resultMapping = new BasicResultMapping();
          resultMapping.setPropertyName(matchedProp);
          resultMapping.setColumnName(columnName);
          resultMapping.setColumnIndex(i + 1);
          Class type = classInfo.getSetterType(matchedProp);
          resultMapping.setTypeHandler(TypeHandlerFactory.getTypeHandler(type));
          resultMappingList.add(resultMapping);
        }
      }

      setResultMappingList(resultMappingList);

    } catch (SQLException e) {
      throw new NestedRuntimeException("Error automapping columns. Cause: " + e);
    }

  }

  private void initializeXmlResults(ResultSet rs) {
    try {
      List resultMappingList = new ArrayList();
      ResultSetMetaData rsmd = rs.getMetaData();
      for (int i = 0, n = rsmd.getColumnCount(); i < n; i++) {
        String columnName = rsmd.getColumnLabel(i + 1);
        BasicResultMapping resultMapping = new BasicResultMapping();
        resultMapping.setPropertyName(columnName);
        resultMapping.setColumnName(columnName);
        resultMapping.setColumnIndex(i + 1);
        resultMapping.setTypeHandler(TypeHandlerFactory.getTypeHandler(String.class));
        resultMappingList.add(resultMapping);
      }
      setResultMappingList(resultMappingList);
    } catch (SQLException e) {
      throw new NestedRuntimeException("Error automapping columns. Cause: " + e);
    }
  }

  private void initializeMapResults(ResultSet rs) {
    try {
      List resultMappingList = new ArrayList();
      ResultSetMetaData rsmd = rs.getMetaData();
      for (int i = 0, n = rsmd.getColumnCount(); i < n; i++) {
        String columnName = rsmd.getColumnLabel(i + 1);
        BasicResultMapping resultMapping = new BasicResultMapping();
        resultMapping.setPropertyName(columnName);
        resultMapping.setColumnName(columnName);
        resultMapping.setColumnIndex(i + 1);
        resultMapping.setTypeHandler(TypeHandlerFactory.getTypeHandler(Object.class));
        resultMappingList.add(resultMapping);
      }

      setResultMappingList(resultMappingList);

    } catch (SQLException e) {
      throw new NestedRuntimeException("Error automapping columns. Cause: " + e);
    }
  }

  private void initializePrimitiveResults(ResultSet rs) {
    try {
      ResultSetMetaData rsmd = rs.getMetaData();
      String columnName = rsmd.getColumnLabel(1);
      BasicResultMapping resultMapping = new BasicResultMapping();
      resultMapping.setPropertyName(columnName);
      resultMapping.setColumnName(columnName);
      resultMapping.setColumnIndex(1);
      resultMapping.setTypeHandler(TypeHandlerFactory.getTypeHandler(resultClass));

      List resultMappingList = new ArrayList();
      resultMappingList.add(resultMapping);

      setResultMappingList(resultMappingList);

    } catch (SQLException e) {
      throw new NestedRuntimeException("Error automapping columns. Cause: " + e);
    }
  }

}

