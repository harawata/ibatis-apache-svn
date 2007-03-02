package com.ibatis.sqlmap.engine.builder.xml;

import com.ibatis.common.xml.NodeletUtils;
import com.ibatis.sqlmap.engine.mapping.statement.*;
import org.w3c.dom.*;

import java.util.Properties;

public class SqlStatementParser {

  private XmlParserState state;

  public SqlStatementParser(XmlParserState config) {
    this.state = config;
  }

  public MappedStatement parseGeneralStatement(Node node, GeneralStatement statement) {

    // get attributes
    Properties attributes = NodeletUtils.parseAttributes(node, state.getGlobalProps());
    String id = attributes.getProperty("id");
    String parameterMapName = state.applyNamespace(attributes.getProperty("parameterMap"));
    String parameterClassName = attributes.getProperty("parameterClass");
    String resultMapName = attributes.getProperty("resultMap");
    String resultClassName = attributes.getProperty("resultClass");
    String cacheModelName = state.applyNamespace(attributes.getProperty("cacheModel"));
    String xmlResultName = attributes.getProperty("xmlResultName");
    String resultSetType = attributes.getProperty("resultSetType");
    String fetchSize = attributes.getProperty("fetchSize");
    String allowRemapping = attributes.getProperty("remapResults");
    String timeout = attributes.getProperty("timeout");

    if (state.isUseStatementNamespaces()) {
      id = state.applyNamespace(id);
    }
    String[] additionalResultMapNames = null;
    if (resultMapName != null) {
      additionalResultMapNames = state.getAllButFirstToken(resultMapName);
      resultMapName = state.getFirstToken(resultMapName);
      resultMapName = state.applyNamespace(resultMapName);
      for (int i = 0; i < additionalResultMapNames.length; i++) {
        additionalResultMapNames[i] = state.applyNamespace(additionalResultMapNames[i]);
      }
    }

    String[] additionalResultClasses = null;
    if (resultClassName != null) {
      additionalResultClasses = state.getAllButFirstToken(resultClassName);      
      resultClassName = state.getFirstToken(resultClassName);
    }
    MappedStatement mappedStatement = state.getConfig().prepareGeneralStatement(new XMLSqlSource(state, node), statement, id, resultMapName, additionalResultMapNames, parameterMapName, resultSetType, fetchSize, parameterClassName, resultClassName, additionalResultClasses, allowRemapping, xmlResultName, timeout, cacheModelName);

    findAndParseSelectKey(node, statement);
    return mappedStatement;

  }

  private void findAndParseSelectKey(Node node, GeneralStatement statement) {
    if (statement instanceof InsertStatement) {
      state.getConfig().getErrorContext().setActivity("parsing select key tags");

      InsertStatement insertStatement = ((InsertStatement) statement);

      SelectKeyStatement selectKeyStatement = null;

      boolean foundSQLFirst = false;

      NodeList children = node.getChildNodes();
      for (int i = 0; i < children.getLength(); i++) {
        Node child = children.item(i);
        if (child.getNodeType() == Node.CDATA_SECTION_NODE
            || child.getNodeType() == Node.TEXT_NODE) {
          String data = ((CharacterData) child).getData();
          if (data.trim().length() > 0) {
            foundSQLFirst = true;
          }
        } else if (child.getNodeType() == Node.ELEMENT_NODE
            && "selectKey".equals(child.getNodeName())) {
          Properties attributes = NodeletUtils.parseAttributes(child, state.getGlobalProps());
          String keyPropName = attributes.getProperty("keyProperty");
          String resultClassName = attributes.getProperty("resultClass");
          String type = attributes.getProperty("type");
          selectKeyStatement = state.getConfig().prepareSelectKeyStatement(new XMLSqlSource(state, child), resultClassName, statement.getId(), keyPropName, foundSQLFirst, type, statement.getParameterClass());
          break;
        }
      }
      state.getConfig().getErrorContext().setMoreInfo(null);
      insertStatement.setSelectKeyStatement(selectKeyStatement);
    }
  }


}
