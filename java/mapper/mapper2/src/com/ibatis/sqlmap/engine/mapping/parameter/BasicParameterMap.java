package com.ibatis.sqlmap.engine.mapping.parameter;

import com.ibatis.sqlmap.engine.exchange.*;
import com.ibatis.sqlmap.engine.type.*;

import com.ibatis.sqlmap.engine.scope.*;
import com.ibatis.sqlmap.engine.cache.*;
import com.ibatis.sqlmap.engine.impl.SqlMapExecutorDelegate;

import java.util.*;
import java.sql.*;

public class BasicParameterMap implements ParameterMap {

  private String id;
  private Class parameterClass;

  private ParameterMapping[] parameterMappings;
  private DataExchange dataExchange;

  private String resource;

  private Map parameterMappingIndex = new HashMap();

  private SqlMapExecutorDelegate delegate;

  public BasicParameterMap(SqlMapExecutorDelegate delegate) {
    this.delegate = delegate;
  }

  public SqlMapExecutorDelegate getDelegate() {
    return delegate;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Class getParameterClass() {
    return parameterClass;
  }

  public void setParameterClass(Class parameterClass) {
    this.parameterClass = parameterClass;
  }

  public DataExchange getDataExchange() {
    return dataExchange;
  }

  public void setDataExchange(DataExchange dataExchange) {
    this.dataExchange = dataExchange;
  }

  public ParameterMapping[] getParameterMappings() {
    return parameterMappings;
  }

  public void setParameterMappingList(List parameterMappingList) {
    this.parameterMappings = (BasicParameterMapping[]) parameterMappingList.toArray(new BasicParameterMapping[parameterMappingList.size()]);
    for (int i = 0; i < parameterMappings.length; i++) {
      parameterMappingIndex.put(parameterMappings[i].getPropertyName(), new Integer(i));
    }
    Map props = new HashMap();
    props.put("map", this);

    dataExchange = delegate.getDataExchangeFactory().getDataExchangeForClass(parameterClass);
    dataExchange.initialize(props);
  }

  public int getParameterIndex(String propertyName) {
    Integer idx = null;
    idx = (Integer) parameterMappingIndex.get(propertyName);
    return idx == null ? -1 : idx.intValue();
  }

  public int getParameterCount() {
    return this.parameterMappings.length;
  }

  /**
   *
   * @param ps
   * @param parameters
   * @throws java.sql.SQLException
   */
  public void setParameters(RequestScope request, PreparedStatement ps, Object[] parameters)
      throws SQLException {

    ErrorContext errorContext = request.getErrorContext();
    errorContext.setActivity("applying a parameter map");
    errorContext.setObjectId(this.getId());
    errorContext.setResource(this.getResource());
    errorContext.setMoreInfo("Check the parameter map.");

    if (parameterMappings != null) {
      for (int i = 0; i < parameterMappings.length; i++) {
        BasicParameterMapping mapping = (BasicParameterMapping) parameterMappings[i];
        errorContext.setMoreInfo(mapping.getErrorString());
        if (mapping.isInputAllowed()) {
          setParameter(ps, mapping, parameters, i);
        }
      }
    }
  }

  public Object[] getParameterObjectValues(RequestScope request, Object parameterObject) {
    return dataExchange.getData(request, this, parameterObject);
  }

  public CacheKey getCacheKey(RequestScope request, Object parameterObject) {
    return dataExchange.getCacheKey(request, this, parameterObject);
  }

  public void refreshParameterObjectValues(RequestScope request, Object parameterObject, Object[] values) {
    dataExchange.setData(request, this, parameterObject, values);
  }

  public String getResource() {
    return resource;
  }

  public void setResource(String resource) {
    this.resource = resource;
  }

  protected void setParameter(PreparedStatement ps, BasicParameterMapping mapping, Object[] parameters, int i) throws SQLException {
    Object value = parameters[i];
    // Apply Null Value
    String nullValueString = mapping.getNullValue();
    if (nullValueString != null) {
      TypeHandler handler = mapping.getTypeHandler();
      if (handler.equals(value, nullValueString)) {
        value = null;
      }
    }

    // Set Parameter
    if (value != null) {
      TypeHandler typeHandler = mapping.getTypeHandler();
      typeHandler.setParameter(ps, i + 1, value, mapping.getJdbcTypeName());
    } else {
      int jdbcType = mapping.getJdbcType();
      if (jdbcType != JdbcTypeRegistry.UNKNOWN_TYPE) {
        ps.setNull(i + 1, jdbcType);
      } else {
        ps.setNull(i + 1, Types.OTHER);
      }
    }
  }

}
