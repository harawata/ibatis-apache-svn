package com.ibatis.sqlmap.engine.mapping.result;

import com.ibatis.sqlmap.engine.exchange.*;
import com.ibatis.sqlmap.engine.type.*;
import com.ibatis.sqlmap.engine.impl.*;

import com.ibatis.sqlmap.engine.mapping.statement.*;
import com.ibatis.sqlmap.engine.mapping.result.loader.*;
import com.ibatis.sqlmap.engine.mapping.sql.Sql;
import com.ibatis.sqlmap.engine.scope.*;
import com.ibatis.sqlmap.client.*;
import com.ibatis.common.jdbc.exception.*;
import com.ibatis.common.resources.*;
import com.ibatis.common.beans.*;
import com.ibatis.common.minixml.*;

import java.util.*;
import java.sql.*;

public class BasicResultMap implements ResultMap {

  protected String id;
  protected Class resultClass;

  protected ResultMapping[] resultMappings;
  protected DataExchange dataExchange;

  private String xmlName;

  private String resource;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Class getResultClass() {
    return resultClass;
  }

  public void setResultClass(Class resultClass) {
    this.resultClass = resultClass;
  }

  public DataExchange getDataExchange() {
    return dataExchange;
  }

  public void setDataExchange(DataExchange dataExchange) {
    this.dataExchange = dataExchange;
  }

  public String getXmlName() {
    return xmlName;
  }

  public void setXmlName(String xmlName) {
    this.xmlName = xmlName;
  }

  public String getResource() {
    return resource;
  }

  public void setResource(String resource) {
    this.resource = resource;
  }

  public ResultMapping[] getResultMappings() {
    return resultMappings;
  }

  public void setResultMappingList(List resultMappingList) {
    this.resultMappings = (BasicResultMapping[]) resultMappingList.toArray(new BasicResultMapping[resultMappingList.size()]);
    Map props = new HashMap();
    props.put("map", this);
    dataExchange = DataExchangeFactory.getDataExchangeForClass(resultClass);
    dataExchange.initialize(props);
  }

  public int getResultCount() {
    return this.resultMappings.length;
  }

  /**
   *
   * @param rs
   * @return
   * @throws java.sql.SQLException
   */
  public Object[] getResults(RequestScope request, ResultSet rs)
      throws SQLException {
    ErrorContext errorContext = request.getErrorContext();
    errorContext.setActivity("applying a result map");
    errorContext.setObjectId(this.getId());
    errorContext.setResource(this.getResource());
    errorContext.setMoreInfo("Check the result map.");

    Object[] columnValues = new Object[resultMappings.length];
    for (int i = 0; i < resultMappings.length; i++) {
      BasicResultMapping mapping = (BasicResultMapping) resultMappings[i];
      errorContext.setMoreInfo(mapping.getErrorString());
      if (mapping.getStatementName() == null) {
        columnValues[i] = getPrimitiveResultMappingValue(rs, mapping);
      } else {
        if (resultClass == null) {
          throw new SqlMapException ("The result class was null when trying to get results for ResultMap named " + getId() + ".");
        } else if (Map.class.isAssignableFrom(resultClass)) {
          columnValues[i] = getNestedResultMappingValue(request, rs, mapping, Object.class);
        } else if (XmlTypeMarker.class.isAssignableFrom(resultClass)) {
          Class javaType = mapping.getJavaType();
          if (javaType == null) {
            javaType = XmlTypeMarker.class;
          }
          columnValues[i] = getNestedResultMappingValue(request, rs, mapping, javaType);
        } else {
          ClassInfo info = ClassInfo.getInstance(resultClass);
          Class type = info.getSetterType(mapping.getPropertyName());
          columnValues[i] = getNestedResultMappingValue(request, rs, mapping, type);
        }
      }
    }

    return columnValues;
  }

  public Object setResultObjectValues(RequestScope request, Object resultObject, Object[] values) {
    return dataExchange.setData(request, this, resultObject, values);
  }

  protected Object getNestedResultMappingValue(RequestScope request, ResultSet rs, BasicResultMapping mapping, Class targetType)
      throws SQLException {
    try {

      String statementName = mapping.getStatementName();
      ExtendedSqlMapClient client = (ExtendedSqlMapClient) request.getSession().getSqlMapClient();

      MappedStatement mappedStatement = client.getMappedStatement(statementName);
      Class parameterType = mappedStatement.getParameterClass();
      Object parameterObject = null;

      if (parameterType == null) {
        parameterObject = prepareBeanParameterObject(rs, mapping, parameterType);
      } else if (TypeHandlerFactory.hasTypeHandler(parameterType)) {
        parameterObject = preparePrimitiveParameterObject(rs, mapping, parameterType);
      } else if (XmlTypeMarker.class.isAssignableFrom(parameterType)) {
        parameterObject = prepareXmlParameterObject(rs, mapping);
      } else {
        parameterObject = prepareBeanParameterObject(rs, mapping, parameterType);
      }

      Sql sql = mappedStatement.getSql();
      ResultMap resultMap = sql.getResultMap(request, parameterObject);
      Class resultClass = resultMap.getResultClass();
      if (resultClass != null && !XmlTypeMarker.class.isAssignableFrom(targetType)) {
        if (XmlCollectionTypeMarker.class.isAssignableFrom(resultClass)) {
          targetType = XmlCollectionTypeMarker.class;
        } else if (XmlTypeMarker.class.isAssignableFrom(resultClass)) {
          targetType = XmlTypeMarker.class;
        }
      }

      boolean setIgnoreDomRoot = false;
      if (XmlTypeMarker.class == (targetType)) {
        if (!request.getSession().isIgnoreDomRoot()) {
          request.getSession().setIgnoreDomRoot(true);
          setIgnoreDomRoot = true;
        }
      }

      Object result = ResultLoader.loadResult(client, statementName, parameterObject, targetType);

      if (setIgnoreDomRoot) {
        request.getSession().setIgnoreDomRoot(false);
      }

      if (XmlTypeMarker.class.isAssignableFrom(targetType)) {
        if (result instanceof List) {
          result = new XmlList((List) result);
        }
      }

      return result;

    } catch (InstantiationException e) {
      throw new NestedSQLException("Error setting nested bean property.  Cause: " + e, e);
    } catch (IllegalAccessException e) {
      throw new NestedSQLException("Error setting nested bean property.  Cause: " + e, e);
    }

  }

  private Object preparePrimitiveParameterObject(ResultSet rs, BasicResultMapping mapping, Class parameterType) throws SQLException {
    Object parameterObject;
    TypeHandler th = TypeHandlerFactory.getTypeHandler(parameterType);
    parameterObject = th.getResult(rs, mapping.getColumnName());
    return parameterObject;
  }

  private Object prepareXmlParameterObject(ResultSet rs, BasicResultMapping mapping) throws SQLException {

    Object parameterObject;

    MiniDom dom = new MiniDom("parameter");

    String complexName = mapping.getColumnName();

    TypeHandler stringTypeHandler = TypeHandlerFactory.getTypeHandler(String.class);
    if (complexName.indexOf('=') > -1) {
      // old 1.x style multiple params
      StringTokenizer parser = new StringTokenizer(complexName, "{}=, ", false);
      while (parser.hasMoreTokens()) {
        String propName = parser.nextToken();
        String colName = parser.nextToken();
        Object propValue = stringTypeHandler.getResult(rs, colName);
        dom.setValue(propName, propValue.toString());
      }
    } else {
      // single param
      Object propValue = stringTypeHandler.getResult(rs, complexName);
      dom.setValue("value", propValue.toString());
    }

    parameterObject = dom.toString();
    return parameterObject;
  }

  private Object prepareBeanParameterObject(ResultSet rs, BasicResultMapping mapping, Class parameterType)
      throws InstantiationException, IllegalAccessException, SQLException {

    Object parameterObject;
    if (parameterType == null) {
      parameterObject = new HashMap();
    } else {
      parameterObject = Resources.instantiate(parameterType);
    }
    String complexName = mapping.getColumnName();

    if (complexName.indexOf('=') > -1) {
      StringTokenizer parser = new StringTokenizer(complexName, "{}=, ", false);
      while (parser.hasMoreTokens()) {
        String propName = parser.nextToken();
        String colName = parser.nextToken();
        Class propType = BeanProbe.getPropertyTypeForSetter(parameterObject, propName);
        TypeHandler propTypeHandler = TypeHandlerFactory.getTypeHandler(propType);
        Object propValue = propTypeHandler.getResult(rs, colName);
        BeanProbe.setObject(parameterObject, propName, propValue);
      }
    } else {
      // single param
      TypeHandler propTypeHandler = TypeHandlerFactory.getTypeHandler(parameterType);
      parameterObject = propTypeHandler.getResult(rs, complexName);
    }

    return parameterObject;
  }

  protected Object getPrimitiveResultMappingValue(ResultSet rs, BasicResultMapping mapping) throws SQLException {
    TypeHandler typeHandler = mapping.getTypeHandler();
    String columnName = mapping.getColumnName();
    int columnIndex = mapping.getColumnIndex();
    String nullValue = mapping.getNullValue();
    Object value = null;
    if (columnName == null) {
      value = typeHandler.getResult(rs, columnIndex);
    } else {
      value = typeHandler.getResult(rs, columnName);
    }
    if (value == null && nullValue != null) {
      value = typeHandler.valueOf(nullValue);
    }
    return value;
  }

}

