package com.ibatis.sqlmap.engine.conifg;

import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.extensions.TypeHandlerCallback;
import com.ibatis.sqlmap.engine.impl.ExtendedSqlMapClient;
import com.ibatis.sqlmap.engine.impl.SqlMapExecutorDelegate;
import com.ibatis.sqlmap.engine.mapping.result.BasicResultMap;
import com.ibatis.sqlmap.engine.mapping.result.BasicResultMapping;
import com.ibatis.sqlmap.engine.mapping.result.Discriminator;
import com.ibatis.sqlmap.engine.mapping.result.ResultMapping;
import com.ibatis.sqlmap.engine.scope.ErrorContext;
import com.ibatis.sqlmap.engine.type.CustomTypeHandler;
import com.ibatis.sqlmap.engine.type.TypeHandler;
import com.ibatis.sqlmap.engine.type.TypeHandlerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public class ResultMapConfig {
  private SqlMapConfiguration config;
  private ErrorContext errorContext;
  private ExtendedSqlMapClient client;
  private SqlMapExecutorDelegate delegate;
  private TypeHandlerFactory typeHandlerFactory;
  private BasicResultMap resultMap;
  private List resultMappingList;
  private int resultMappingIndex;
  private Discriminator discriminator;

  ResultMapConfig(SqlMapConfiguration config, String id, String resultClassName, String xmlName, String groupBy, String extended) {
    this.config = config;
    this.errorContext = config.getErrorContext();
    this.client = config.getClient();
    this.delegate = config.getDelegate();
    this.typeHandlerFactory = config.getTypeHandlerFactory();
    this.resultMap = new BasicResultMap(client.getDelegate());
    this.resultMappingList = new ArrayList();
    errorContext.setActivity("building a result map");
    resultClassName = typeHandlerFactory.resolveAlias(resultClassName);
    errorContext.setObjectId(id + " result map");
    resultMap.setId(id);
    resultMap.setXmlName(xmlName);
    resultMap.setResource(errorContext.getResource());
    if (groupBy != null && groupBy.length() > 0) {
      StringTokenizer parser = new StringTokenizer(groupBy, ", ", false);
      while (parser.hasMoreTokens()) {
        resultMap.addGroupByProperty(parser.nextToken());
      }
    }
    Class resultClass;
    try {
      errorContext.setMoreInfo("Check the result class.");
      resultClass = Resources.classForName(resultClassName);
    } catch (Exception e) {
      throw new RuntimeException("Error configuring Result.  Could not set ResultClass.  Cause: " + e, e);
    }
    resultMap.setResultClass(resultClass);
    errorContext.setMoreInfo("Check the extended result map.");
    if (extended != null) {
      BasicResultMap extendedResultMap = (BasicResultMap) client.getDelegate().getResultMap(extended);
      ResultMapping[] resultMappings = extendedResultMap.getResultMappings();
      for (int i = 0; i < resultMappings.length; i++) {
        resultMappingList.add(resultMappings[i]);
      }
      List nestedResultMappings = extendedResultMap.getNestedResultMappings();
      if (nestedResultMappings != null) {
        Iterator iter = nestedResultMappings.iterator();
        while (iter.hasNext()) {
          resultMap.addNestedResultMappings((ResultMapping) iter.next());
        }
      }
      if (groupBy == null || groupBy.length() == 0) {
        if (extendedResultMap.hasGroupBy()) {
          Iterator i = extendedResultMap.groupByProps();
          while (i.hasNext()) {
            resultMap.addGroupByProperty((String) i.next());
          }
        }
      }
    }
    errorContext.setMoreInfo("Check the result mappings.");
    resultMappingIndex = resultMappingList.size();
  }

  public void setDiscriminator(String callback, String javaType, String jdbcType, String columnName, String nullValue, String columnIndex) {
    callback = typeHandlerFactory.resolveAlias(callback);
    javaType = typeHandlerFactory.resolveAlias(javaType);
    TypeHandler handler;
    if (callback != null) {
      errorContext.setMoreInfo("Check the result mapping typeHandler attribute '" + callback + "' (must be a TypeHandlerCallback implementation).");
      try {
        Object impl = Resources.instantiate(callback);
        if (impl instanceof TypeHandlerCallback) {
          handler = new CustomTypeHandler((TypeHandlerCallback) impl);
        } else if (impl instanceof TypeHandler) {
          handler = (TypeHandler) impl;
        } else {
          throw new RuntimeException("The class '' is not a valid implementation of TypeHandler or TypeHandlerCallback");
        }
      } catch (Exception e) {
        throw new RuntimeException("Error occurred during custom type handler configuration.  Cause: " + e, e);
      }
    } else {
      errorContext.setMoreInfo("Check the result mapping property type or name.");
      handler = config.resolveTypeHandler(client.getDelegate().getTypeHandlerFactory(), resultMap.getResultClass(), "", javaType, jdbcType, true);
    }
    BasicResultMapping mapping = new BasicResultMapping();
    mapping.setColumnName(columnName);
    mapping.setJdbcTypeName(jdbcType);
    mapping.setTypeHandler(handler);
    mapping.setNullValue(nullValue);
    try {
      if (javaType != null && javaType.length() > 0) {
        mapping.setJavaType(Resources.classForName(javaType));
      }
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Error setting javaType on result mapping.  Cause: " + e);
    }
    if (columnIndex != null && columnIndex.length() > 0) {
      mapping.setColumnIndex(Integer.parseInt(columnIndex));
    }
    discriminator = new Discriminator(delegate, mapping);
  }

  public void addSubMap(String value, String resultMap) {
    if (discriminator == null) {
      throw new RuntimeException("The discriminator is null, but somehow a subMap was reached.  This is a bug.");
    }
    discriminator.addSubMap(value, resultMap);
  }

  public void addResultMapping(String callback, String javaType, String propertyName, String jdbcType, String columnName, String nullValue, String statementName, String resultMapName, String columnIndex) {
    callback = typeHandlerFactory.resolveAlias(callback);
    javaType = typeHandlerFactory.resolveAlias(javaType);
    errorContext.setObjectId(propertyName + " mapping of the " + resultMap.getId() + " result map");
    TypeHandler handler;
    if (callback != null) {
      errorContext.setMoreInfo("Check the result mapping typeHandler attribute '" + callback + "' (must be a TypeHandler or TypeHandlerCallback implementation).");
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
      errorContext.setMoreInfo("Check the result mapping property type or name.");
      handler = config.resolveTypeHandler(client.getDelegate().getTypeHandlerFactory(), resultMap.getResultClass(), propertyName, javaType, jdbcType, true);
    }
    BasicResultMapping mapping = new BasicResultMapping();
    mapping.setPropertyName(propertyName);
    mapping.setColumnName(columnName);
    mapping.setJdbcTypeName(jdbcType);
    mapping.setTypeHandler(handler);
    mapping.setNullValue(nullValue);
    mapping.setStatementName(statementName);
    mapping.setNestedResultMapName(resultMapName);
    if (resultMapName != null && resultMapName.length() > 0) {
      resultMap.addNestedResultMappings(mapping);
    }
    try {
      if (javaType != null && javaType.length() > 0) {
        mapping.setJavaType(Resources.classForName(javaType));
      }
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Error setting javaType on result mapping.  Cause: " + e);
    }
    if (columnIndex != null && columnIndex.length() > 0) {
      mapping.setColumnIndex(Integer.parseInt(columnIndex));
    } else {
      resultMappingIndex++;
      mapping.setColumnIndex(resultMappingIndex);
    }
    resultMappingList.add(mapping);
  }

  public void saveResultMap() {
    if (resultMappingList.size() == 0) {
      throw new RuntimeException("resultMap " + resultMap.getId() + " must have at least one result mapping");
    }
    resultMap.setResultMappingList(resultMappingList);
    resultMap.setDiscriminator(discriminator);
    discriminator = null;
    client.getDelegate().addResultMap(resultMap);
    errorContext.setMoreInfo(null);
    errorContext.setObjectId(null);
  }

}
