package com.ibatis.sqlmap.engine.builder.xml;

import com.ibatis.sqlmap.engine.mapping.statement.*;
import com.ibatis.sqlmap.engine.mapping.result.*;
import com.ibatis.sqlmap.engine.mapping.parameter.*;
import com.ibatis.sqlmap.engine.mapping.sql.dynamic.DynamicSql;
import com.ibatis.sqlmap.engine.mapping.sql.dynamic.elements.*;
import com.ibatis.sqlmap.engine.mapping.sql.*;
import com.ibatis.sqlmap.engine.mapping.sql.stat.StaticSql;
import com.ibatis.sqlmap.engine.mapping.sql.simple.SimpleDynamicSql;
import com.ibatis.sqlmap.engine.cache.CacheModel;
import com.ibatis.sqlmap.client.SqlMapException;
import com.ibatis.common.resources.Resources;
import com.ibatis.common.xml.NodeletUtils;
import com.ibatis.common.beans.*;
import org.w3c.dom.*;

import java.util.*;
import java.sql.ResultSet;

public class SqlStatementParser extends BaseParser {

  private static final Probe PROBE = ProbeFactory.getProbe();

  private static final InlineParameterMapParser PARAM_PARSER = new InlineParameterMapParser();

  public SqlStatementParser(Variables vars) {
    super (vars);
  }

  public MappedStatement parseGeneralStatement(Node node, GeneralStatement statement) {
    Properties attributes = NodeletUtils.parseAttributes(node, vars.properties);

    String id = attributes.getProperty("id");

    if (vars.useStatementNamespaces) {
      id = applyNamespace(id);
    }

    String parameterMapName = applyNamespace(attributes.getProperty("parameterMap"));
    String parameterClassName = attributes.getProperty("parameterClass");
    String resultMapName = applyNamespace(attributes.getProperty("resultMap"));
    String resultClassName = attributes.getProperty("resultClass");
    String cacheModelName = applyNamespace(attributes.getProperty("cacheModel"));
    String xmlResultName = attributes.getProperty("xmlResultName");
    String resultSetType = attributes.getProperty("resultSetType");
    String fetchSize = attributes.getProperty("fetchSize");
    String allowRemapping = attributes.getProperty("remapResults");

    parameterClassName = vars.typeHandlerFactory.resolveAlias(parameterClassName);
    resultClassName = vars.typeHandlerFactory.resolveAlias(resultClassName);

    Class parameterClass = null;
    Class resultClass = null;

    // get parameter and result maps

    BasicResultMap resultMap = null;
    if (resultMapName != null) {
      resultMap = (BasicResultMap) vars.delegate.getResultMap(resultMapName);
    }

    BasicParameterMap parameterMap = null;
    if (parameterMapName != null) {
      parameterMap = (BasicParameterMap) vars.delegate.getParameterMap(parameterMapName);
    }

    statement.setId(id);
    statement.setParameterMap(parameterMap);
    statement.setResultMap(resultMap);
    statement.setResource(vars.currentResource);

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
    if (parameterMap == null) {
      try {
        if (parameterClassName != null) {
          parameterClass = Resources.classForName(parameterClassName);
          statement.setParameterClass(parameterClass);
        }
      } catch (ClassNotFoundException e) {
        throw new SqlMapException("Error.  Could not set parameter class.  Cause: " + e, e);
      }
    } else {
      statement.setParameterClass(parameterMap.getParameterClass());
    }

    try {
      if (resultClassName != null) {
        resultClass = Resources.classForName(resultClassName);
      }
    } catch (ClassNotFoundException e) {
      throw new SqlMapException("Error.  Could not set result class.  Cause: " + e, e);
    }

    // process SQL statement, including inline parameter maps
    processSqlStatement(node, statement);

    // set up either null result map or automatic result mapping
    if (resultMap == null && resultClass == null) {
      statement.setResultMap(null);
    } else if (resultMap == null) {
      resultMap = new AutoResultMap(vars.delegate, "true".equals(allowRemapping));
      resultMap.setId(statement.getId() + "-AutoResultMap");
      resultMap.setResultClass(resultClass);
      resultMap.setXmlName(xmlResultName);
      resultMap.setResource(statement.getResource());
      statement.setResultMap(resultMap);

    }

    statement.setSqlMapClient(vars.client);
    if (cacheModelName != null && cacheModelName.length() > 0 && vars.delegate.isCacheModelsEnabled()) {
      CacheModel cacheModel = vars.delegate.getCacheModel(cacheModelName);
      return new CachingStatement(statement, cacheModel);
    } else {
      return statement;
    }

  }

  private SelectKeyStatement parseSelectKey(Node node, GeneralStatement insertStatement) {
    // get attributes
    Properties attributes = NodeletUtils.parseAttributes(node, vars.properties);
    String keyPropName = attributes.getProperty("keyProperty");
    String resultClassName = attributes.getProperty("resultClass");
    resultClassName = vars.typeHandlerFactory.resolveAlias(resultClassName);
    Class resultClass = null;

    // get parameter and result maps
    SelectKeyStatement selectKeyStatement = new SelectKeyStatement();
    selectKeyStatement.setSqlMapClient(vars.client);

    selectKeyStatement.setId(insertStatement.getId() + "-SelectKey");
    selectKeyStatement.setResource(vars.currentResource);
    selectKeyStatement.setKeyProperty(keyPropName);

    try {
      if (resultClassName != null) {
        resultClass = Resources.classForName(resultClassName);
      } else {
        Class parameterClass = insertStatement.getParameterClass();
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
    processSqlStatement(node, selectKeyStatement);

    BasicResultMap resultMap;
    resultMap = new AutoResultMap(vars.delegate, false);
    resultMap.setId(selectKeyStatement.getId() + "-AutoResultMap");
    resultMap.setResultClass(resultClass);
    resultMap.setResource(selectKeyStatement.getResource());
    selectKeyStatement.setResultMap(resultMap);

    return selectKeyStatement;

  }

  private void processSqlStatement(Node n, GeneralStatement statement) {

    boolean isDynamic = false;
    DynamicSql dynamic = new DynamicSql(vars.delegate);
    StringBuffer sqlBuffer = new StringBuffer();

    isDynamic = parseDynamicTags(n, dynamic, sqlBuffer, isDynamic, false);
    if (statement instanceof InsertStatement) {
      InsertStatement insertStatement = ((InsertStatement) statement);
      SelectKeyStatement selectKeyStatement = findAndParseSelectKeyStatement(n, statement);
      insertStatement.setSelectKeyStatement(selectKeyStatement);
    }

    String sqlStatement = sqlBuffer.toString();
    if (isDynamic) {
      statement.setSql(dynamic);
    } else {
      applyInlineParameterMap(statement, sqlStatement);
    }

  }

  private boolean parseDynamicTags(Node node, DynamicParent dynamic, StringBuffer sqlBuffer, boolean isDynamic, boolean postParseRequired) {

    NodeList children = node.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      if (child.getNodeType() == Node.CDATA_SECTION_NODE
          || child.getNodeType() == Node.TEXT_NODE) {

        String data = ((CharacterData) child).getData();
        data = NodeletUtils.parsePropertyTokens(data, vars.properties);

        SqlText sqlText;
        if (postParseRequired) {
          sqlText = new SqlText();
          sqlText.setText(data.toString());
        } else {
          sqlText = PARAM_PARSER.parseInlineParameterMap(vars.delegate.getTypeHandlerFactory(), data.toString(), null);
        }

        dynamic.addChild(sqlText);

        sqlBuffer.append(data);

      } else {
        String nodeName = child.getNodeName();
        SqlTagHandler handler = SqlTagHandlerFactory.getSqlTagHandler(nodeName);
        if (handler != null) {
          isDynamic = true;

          SqlTag tag = new SqlTag();
          tag.setName(nodeName);
          tag.setHandler(handler);

          Properties attributes = NodeletUtils.parseAttributes(node, vars.properties);

          tag.setPrependAttr(attributes.getProperty("prepend"));
          tag.setPropertyAttr(attributes.getProperty("property"));

          tag.setOpenAttr(attributes.getProperty("open"));
          tag.setCloseAttr(attributes.getProperty("close"));

          tag.setComparePropertyAttr(attributes.getProperty("compareProperty"));
          tag.setCompareValueAttr(attributes.getProperty("compareValue"));
          tag.setConjunctionAttr(attributes.getProperty("conjunction"));

          dynamic.addChild(tag);

          if (child.hasChildNodes()) {
            isDynamic = parseDynamicTags(child, tag, sqlBuffer, isDynamic, handler.isPostParseRequired());
          }
        }
      }
    }
    return isDynamic;
  }

  private SelectKeyStatement findAndParseSelectKeyStatement(Node n, GeneralStatement insertStatement) {
    SelectKeyStatement selectKeyStatement = null;

    boolean foundTextFirst = false;

    NodeList children = n.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      if (child.getNodeType() == Node.CDATA_SECTION_NODE
          || child.getNodeType() == Node.TEXT_NODE) {
        String data = ((CharacterData) child).getData();
        if (data.trim().length() > 0) {
          foundTextFirst = true;
        }
      } else if (child.getNodeType() == Node.ELEMENT_NODE
          && "selectKey".equals(child.getNodeName())) {
        selectKeyStatement = parseSelectKey(child, insertStatement);
        break;
      }
    }
    if (selectKeyStatement != null) {
      selectKeyStatement.setAfter(foundTextFirst);
    }
    return selectKeyStatement;
  }

  private void applyInlineParameterMap(GeneralStatement statement, String sqlStatement) {
    String newSql = sqlStatement;

    ParameterMap parameterMap = statement.getParameterMap();

    if (parameterMap == null) {

      BasicParameterMap map;
      map = new BasicParameterMap(vars.delegate);

      map.setId(statement.getId() + "-InlineParameterMap");
      map.setParameterClass(statement.getParameterClass());
      map.setResource(statement.getResource());
      statement.setParameterMap(map);

      SqlText sqlText = PARAM_PARSER.parseInlineParameterMap(vars.delegate.getTypeHandlerFactory(), newSql, statement.getParameterClass());
      newSql = sqlText.getText();
      List mappingList = Arrays.asList(sqlText.getParameterMappings());

      map.setParameterMappingList(mappingList);

    }

    Sql sql = null;
    if (SimpleDynamicSql.isSimpleDynamicSql(newSql)) {
      sql = new SimpleDynamicSql(vars.delegate, newSql);
    } else {
      sql = new StaticSql(newSql);
    }
    statement.setSql(sql);
  }


}
