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
package com.ibatis.sqlmap.engine.mapping.result;

import com.ibatis.common.beans.ClassInfo;
import com.ibatis.common.exception.NestedRuntimeException;
import com.ibatis.sqlmap.client.SqlMapException;
import com.ibatis.sqlmap.engine.impl.SqlMapExecutorDelegate;
import com.ibatis.sqlmap.engine.scope.RequestScope;
import com.ibatis.sqlmap.engine.type.DomTypeMarker;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An automatic result map for simple stuff
 */
public class AutoResultMap extends BasicResultMap {

  private boolean allowRemapping = false;

  /**
   * Constructor to pass in the SqlMapExecutorDelegate 
   * 
   * @param delegate - the delegate
   */
  public AutoResultMap(SqlMapExecutorDelegate delegate, boolean allowRemapping) {
    super(delegate);
    this.allowRemapping = allowRemapping;
  }

  public synchronized Object[] getResults(RequestScope request, ResultSet rs)
      throws SQLException {
    if (allowRemapping || resultMappings == null) {
      initialize(rs);
    }
    return super.getResults(request, rs);
  }

  private void initialize(ResultSet rs) {
    if (resultClass == null) {
      throw new SqlMapException("The automatic ResultMap named " + this.getId() + " had a null result class (not allowed).");
    } else if (Map.class.isAssignableFrom(resultClass)) {
      initializeMapResults(rs);
    } else if (getDelegate().getTypeHandlerFactory().getTypeHandler(resultClass) != null) {
      initializePrimitiveResults(rs);
    } else if (DomTypeMarker.class.isAssignableFrom(resultClass)) {
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
          resultMapping.setTypeHandler(getDelegate().getTypeHandlerFactory().getTypeHandler(type));
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
        resultMapping.setTypeHandler(getDelegate().getTypeHandlerFactory().getTypeHandler(String.class));
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
        resultMapping.setTypeHandler(getDelegate().getTypeHandlerFactory().getTypeHandler(Object.class));
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
      resultMapping.setTypeHandler(getDelegate().getTypeHandlerFactory().getTypeHandler(resultClass));

      List resultMappingList = new ArrayList();
      resultMappingList.add(resultMapping);

      setResultMappingList(resultMappingList);

    } catch (SQLException e) {
      throw new NestedRuntimeException("Error automapping columns. Cause: " + e);
    }
  }

}

