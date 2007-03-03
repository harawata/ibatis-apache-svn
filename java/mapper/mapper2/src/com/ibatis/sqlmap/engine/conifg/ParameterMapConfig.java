package com.ibatis.sqlmap.engine.conifg;

import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapException;
import com.ibatis.sqlmap.client.extensions.TypeHandlerCallback;
import com.ibatis.sqlmap.engine.impl.ExtendedSqlMapClient;
import com.ibatis.sqlmap.engine.mapping.parameter.BasicParameterMap;
import com.ibatis.sqlmap.engine.mapping.parameter.BasicParameterMapping;
import com.ibatis.sqlmap.engine.scope.ErrorContext;
import com.ibatis.sqlmap.engine.type.CustomTypeHandler;
import com.ibatis.sqlmap.engine.type.TypeHandler;
import com.ibatis.sqlmap.engine.type.TypeHandlerFactory;

import java.util.ArrayList;
import java.util.List;

public class ParameterMapConfig {
  private SqlMapConfiguration config;
  private ErrorContext errorContext;
  private ExtendedSqlMapClient client;
  private TypeHandlerFactory typeHandlerFactory;
  private BasicParameterMap parameterMap;
  private List parameterMappingList;

  ParameterMapConfig(SqlMapConfiguration config, String id, String parameterClassName) {
    this.config = config;
    this.errorContext = config.getErrorContext();
    this.client = config.getClient();
    this.typeHandlerFactory = config.getTypeHandlerFactory();
    errorContext.setActivity("building a parameter map");
    parameterMap = new BasicParameterMap(client.getDelegate());
    parameterClassName = typeHandlerFactory.resolveAlias(parameterClassName);
    parameterMap.setId(id);
    parameterMap.setResource(errorContext.getResource());
    errorContext.setObjectId(id + " parameter map");
    Class parameterClass;
    try {
      errorContext.setMoreInfo("Check the parameter class.");
      parameterClass = Resources.classForName(parameterClassName);
    } catch (Exception e) {
      throw new SqlMapException("Error configuring ParameterMap.  Could not set ParameterClass.  Cause: " + e, e);
    }
    parameterMap.setParameterClass(parameterClass);
    errorContext.setMoreInfo("Check the parameter mappings.");
    this.parameterMappingList = new ArrayList();
  }

  public void addParameterMapping(String callback, String javaType, String resultMap, String propertyName, String jdbcType, String type, String nullValue, String mode, String numericScale) {
    callback = typeHandlerFactory.resolveAlias(callback);
    javaType = typeHandlerFactory.resolveAlias(javaType);
    errorContext.setObjectId(propertyName + " mapping of the " + parameterMap.getId() + " parameter map");
    TypeHandler handler;
    if (callback != null) {
      errorContext.setMoreInfo("Check the parameter mapping typeHandler attribute '" + callback + "' (must be a TypeHandler or TypeHandlerCallback implementation).");
      try {
        Object impl = Resources.instantiate(callback);
        if (impl instanceof TypeHandlerCallback) {
          handler = new CustomTypeHandler((TypeHandlerCallback) impl);
        } else if (impl instanceof TypeHandler) {
          handler = (TypeHandler) impl;
        } else {
          throw new RuntimeException("The class '" + callback + "' is not a valid implementation of TypeHandler or TypeHandlerCallback");
        }
      } catch (Exception e) {
        throw new RuntimeException("Error occurred during custom type handler configuration.  Cause: " + e, e);
      }
    } else {
      errorContext.setMoreInfo("Check the parameter mapping property type or name.");
      handler = config.resolveTypeHandler(client.getDelegate().getTypeHandlerFactory(), parameterMap.getParameterClass(), propertyName, javaType, jdbcType);
    }
    BasicParameterMapping mapping = new BasicParameterMapping();
    mapping.setPropertyName(propertyName);
    mapping.setJdbcTypeName(jdbcType);
    mapping.setTypeName(type);
    mapping.setResultMapName(resultMap);
    mapping.setNullValue(nullValue);
    if (mode != null && mode.length() > 0) {
      mapping.setMode(mode);
    }
    mapping.setTypeHandler(handler);
    try {
      if (javaType != null && javaType.length() > 0) {
        mapping.setJavaType(Resources.classForName(javaType));
      }
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Error setting javaType on parameter mapping.  Cause: " + e);
    }
    if (numericScale != null) {
      try {
        Integer scale = Integer.valueOf(numericScale);
        if (scale.intValue() < 0) {
          throw new RuntimeException("Error setting numericScale on parameter mapping.  Cause: scale must be greater than or equal to zero");
        }
        mapping.setNumericScale(scale);
      } catch (NumberFormatException e) {
        throw new RuntimeException("Error setting numericScale on parameter mapping.  Cause: " + numericScale + " is not a valid integer");
      }
    }
    parameterMappingList.add(mapping);
  }

  public void saveParameterMap() {
    parameterMap.setParameterMappingList(parameterMappingList);
    client.getDelegate().addParameterMap(parameterMap);
    errorContext.setMoreInfo(null);
    errorContext.setObjectId(null);
  }


}
