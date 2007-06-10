package com.ibatis.sqlmap.engine.conifg;

import com.ibatis.common.beans.*;
import com.ibatis.common.resources.*;
import com.ibatis.sqlmap.client.*;
import com.ibatis.sqlmap.engine.cache.*;
import com.ibatis.sqlmap.engine.impl.*;
import com.ibatis.sqlmap.engine.mapping.parameter.*;
import com.ibatis.sqlmap.engine.mapping.result.*;
import com.ibatis.sqlmap.engine.mapping.sql.*;
import com.ibatis.sqlmap.engine.mapping.sql.dynamic.*;
import com.ibatis.sqlmap.engine.mapping.sql.simple.*;
import com.ibatis.sqlmap.engine.mapping.sql.stat.*;
import com.ibatis.sqlmap.engine.mapping.statement.*;
import com.ibatis.sqlmap.engine.scope.*;
import com.ibatis.sqlmap.engine.type.*;

import java.sql.ResultSet;
import java.util.*;

public class MappedStatementConfig {
  private static final Probe PROBE = ProbeFactory.getProbe();
  private static final InlineParameterMapParser PARAM_PARSER = new InlineParameterMapParser();
  private ErrorContext errorContext;
  private ExtendedSqlMapClient client;
  private SqlMapExecutorDelegate delegate;
  private TypeHandlerFactory typeHandlerFactory;
  private MappedStatement mappedStatement;
  private MappedStatement rootStatement;

  MappedStatementConfig(SqlMapConfiguration config, String id, GeneralStatement statement, SqlSource processor, String parameterMapName, String parameterClassName, String resultMapName, String[] additionalResultMapNames, String resultClassName, String[] additionalResultClasses, String cacheModelName, String resultSetType, String fetchSize, String allowRemapping, String timeout, Integer defaultStatementTimeout, String xmlResultName) {
    this.errorContext = config.getErrorContext();
    this.client = config.getClient();
    this.delegate = client.getDelegate();
    this.typeHandlerFactory = config.getTypeHandlerFactory();
    errorContext.setActivity("parsing a mapped statement");
    errorContext.setObjectId(id + " statement");
    errorContext.setMoreInfo("Check the result map name.");
    if (resultMapName != null) {
      statement.setResultMap((BasicResultMap) client.getDelegate().getResultMap(resultMapName));
      if (additionalResultMapNames != null) {
        for (int i = 0; i < additionalResultMapNames.length; i++) {
          statement.addResultMap((BasicResultMap) client.getDelegate().getResultMap(additionalResultMapNames[i]));
        }
      }
    }
    errorContext.setMoreInfo("Check the parameter map name.");
    if (parameterMapName != null) {
      statement.setParameterMap((BasicParameterMap) client.getDelegate().getParameterMap(parameterMapName));
    }
    statement.setId(id);
    statement.setResource(errorContext.getResource());
    if (resultSetType != null) {
      if ("FORWARD_ONLY".equals(resultSetType)) {
        statement.setResultSetType(new Integer(ResultSet.TYPE_FORWARD_ONLY));
      } else if ("SCROLL_INSENSITIVE".equals(resultSetType)) {
        statement.setResultSetType(new Integer(ResultSet.TYPE_SCROLL_INSENSITIVE));
      } else if ("SCROLL_SENSITIVE".equals(resultSetType)) {
        statement.setResultSetType(new Integer(ResultSet.TYPE_SCROLL_SENSITIVE));
      }
    }
    if (fetchSize != null) {
      statement.setFetchSize(new Integer(fetchSize));
    }

    // set parameter class either from attribute or from map (make sure to match)
    ParameterMap parameterMap = statement.getParameterMap();
    if (parameterMap == null) {
      try {
        if (parameterClassName != null) {
          errorContext.setMoreInfo("Check the parameter class.");
          parameterClassName = typeHandlerFactory.resolveAlias(parameterClassName);
          Class parameterClass = Resources.classForName(parameterClassName);
          statement.setParameterClass(parameterClass);
        }
      } catch (ClassNotFoundException e) {
        throw new SqlMapException("Error.  Could not set parameter class.  Cause: " + e, e);
      }
    } else {
      statement.setParameterClass(parameterMap.getParameterClass());
    }

    // process SQL statement, including inline parameter maps
    errorContext.setMoreInfo("Check the SQL statement.");
    Sql sql = processor.getSql();
    setSqlForStatement(statement, sql);

    // set up either null result map or automatic result mapping
    BasicResultMap resultMap = (BasicResultMap) statement.getResultMap();
    if (resultMap == null && resultClassName == null) {
      statement.setResultMap(null);
    } else if (resultMap == null) {
      resultMap = buildAutoResultMap(allowRemapping, statement, resultClassName, xmlResultName);
      statement.setResultMap(resultMap);
      if (additionalResultClasses != null) {
        for (int i = 0; i < additionalResultClasses.length; i++) {
          statement.addResultMap(buildAutoResultMap(allowRemapping, statement, additionalResultClasses[i], xmlResultName));
        }
      }

    }
    statement.setTimeout(defaultStatementTimeout);
    if (timeout != null) {
      try {
        statement.setTimeout(Integer.valueOf(timeout));
      } catch (NumberFormatException e) {
        throw new SqlMapException("Specified timeout value for statement " + statement.getId() + " is not a valid integer");
      }
    }
    errorContext.setMoreInfo(null);
    errorContext.setObjectId(null);
    statement.setSqlMapClient(client);
    if (cacheModelName != null && cacheModelName.length() > 0 && client.getDelegate().isCacheModelsEnabled()) {
      CacheModel cacheModel = client.getDelegate().getCacheModel(cacheModelName);
      mappedStatement = new CachingStatement(statement, cacheModel);
    } else {
      mappedStatement = statement;
    }
    rootStatement = statement;
  }

  public void setSelectKeyStatement(SqlSource processor, String resultClassName, String keyPropName, boolean runAfterSQL, String type) {
    if (rootStatement instanceof InsertStatement) {
      InsertStatement insertStatement = ((InsertStatement) rootStatement);
      Class parameterClass = insertStatement.getParameterClass();
      errorContext.setActivity("parsing a select key");
      SelectKeyStatement selectKeyStatement = new SelectKeyStatement();
      resultClassName = typeHandlerFactory.resolveAlias(resultClassName);
      Class resultClass = null;

      // get parameter and result maps
      selectKeyStatement.setSqlMapClient(client);
      selectKeyStatement.setId(insertStatement.getId() + "-SelectKey");
      selectKeyStatement.setResource(errorContext.getResource());
      selectKeyStatement.setKeyProperty(keyPropName);
      selectKeyStatement.setRunAfterSQL(runAfterSQL);
      // process the type (pre or post) attribute
      if (type != null) {
        selectKeyStatement.setRunAfterSQL("post".equals(type));
      }
      try {
        if (resultClassName != null) {
          errorContext.setMoreInfo("Check the select key result class.");
          resultClass = Resources.classForName(resultClassName);
        } else {
          if (keyPropName != null && parameterClass != null) {
            resultClass = PROBE.getPropertyTypeForSetter(parameterClass, selectKeyStatement.getKeyProperty());
          }
        }
      } catch (ClassNotFoundException e) {
        throw new SqlMapException("Error.  Could not set result class.  Cause: " + e, e);
      }
      if (resultClass == null) {
        resultClass = Object.class;
      }

      // process SQL statement, including inline parameter maps
      errorContext.setMoreInfo("Check the select key SQL statement.");
      Sql sql = processor.getSql();
      setSqlForStatement(selectKeyStatement, sql);
      BasicResultMap resultMap;
      resultMap = new AutoResultMap(client.getDelegate(), false);
      resultMap.setId(selectKeyStatement.getId() + "-AutoResultMap");
      resultMap.setResultClass(resultClass);
      resultMap.setResource(selectKeyStatement.getResource());
      selectKeyStatement.setResultMap(resultMap);
      errorContext.setMoreInfo(null);
      insertStatement.setSelectKeyStatement(selectKeyStatement);
    } else {
      throw new SqlMapException("You cant set a select key statement on statement named " + rootStatement.getId() + " because it is not an InsertStatement.");
    }
  }

  public void saveMappedStatement() {
    delegate.addMappedStatement(mappedStatement);
  }

  private void setSqlForStatement(GeneralStatement statement, Sql sql) {
    if (sql instanceof DynamicSql) {
      statement.setSql(sql);
    } else {
      applyInlineParameterMap(statement, sql.getSql(null, null));
    }
  }

  private void applyInlineParameterMap(GeneralStatement statement, String sqlStatement) {
    String newSql = sqlStatement;
    errorContext.setActivity("building an inline parameter map");
    ParameterMap parameterMap = statement.getParameterMap();
    errorContext.setMoreInfo("Check the inline parameters.");
    if (parameterMap == null) {
      BasicParameterMap map;
      map = new BasicParameterMap(client.getDelegate());
      map.setId(statement.getId() + "-InlineParameterMap");
      map.setParameterClass(statement.getParameterClass());
      map.setResource(statement.getResource());
      statement.setParameterMap(map);
      SqlText sqlText = PARAM_PARSER.parseInlineParameterMap(client.getDelegate().getTypeHandlerFactory(), newSql, statement.getParameterClass());
      newSql = sqlText.getText();
      List mappingList = Arrays.asList(sqlText.getParameterMappings());
      map.setParameterMappingList(mappingList);
    }
    Sql sql;
    if (SimpleDynamicSql.isSimpleDynamicSql(newSql)) {
      sql = new SimpleDynamicSql(client.getDelegate(), newSql);
    } else {
      sql = new StaticSql(newSql);
    }
    statement.setSql(sql);

  }

  private BasicResultMap buildAutoResultMap(String allowRemapping, GeneralStatement statement, String firstResultClass, String xmlResultName) {
    BasicResultMap resultMap;
    resultMap = new AutoResultMap(client.getDelegate(), "true".equals(allowRemapping));
    resultMap.setId(statement.getId() + "-AutoResultMap");
    resultMap.setResultClass(resolveClass(firstResultClass));
    resultMap.setXmlName(xmlResultName);
    resultMap.setResource(statement.getResource());
    return resultMap;
  }

  private Class resolveClass(String resultClassName) {
    try {
      if (resultClassName != null) {
        errorContext.setMoreInfo("Check the result class.");
        return Resources.classForName(typeHandlerFactory.resolveAlias(resultClassName));
      } else {
        return null;
      }
    } catch (ClassNotFoundException e) {
      throw new SqlMapException("Error.  Could not set result class.  Cause: " + e, e);
    }
  }

  public MappedStatement getMappedStatement() {
    return mappedStatement;
  }
}
