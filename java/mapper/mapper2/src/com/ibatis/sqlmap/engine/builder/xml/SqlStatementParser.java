package com.ibatis.sqlmap.engine.builder.xml;

import com.ibatis.common.xml.NodeletUtils;
import com.ibatis.sqlmap.engine.mapping.statement.*;
import com.ibatis.sqlmap.engine.conifg.SqlMapConfiguration;
import org.w3c.dom.*;

import java.util.Properties;

public class SqlStatementParser {

  private SqlMapConfiguration config;

  public SqlStatementParser(SqlMapConfiguration config) {
    this.config = config;
  }

  public MappedStatement parseGeneralStatement(Node node, GeneralStatement statement) {

    // get attributes
    Properties attributes = NodeletUtils.parseAttributes(node, config.globalProps);
    String id = attributes.getProperty("id");
    String parameterMapName = config.applyNamespace(attributes.getProperty("parameterMap"));
    String parameterClassName = attributes.getProperty("parameterClass");
    String resultMapName = attributes.getProperty("resultMap");
    String resultClassName = attributes.getProperty("resultClass");
    String cacheModelName = config.applyNamespace(attributes.getProperty("cacheModel"));
    String xmlResultName = attributes.getProperty("xmlResultName");
    String resultSetType = attributes.getProperty("resultSetType");
    String fetchSize = attributes.getProperty("fetchSize");
    String allowRemapping = attributes.getProperty("remapResults");
    String timeout = attributes.getProperty("timeout");

    MappedStatement mappedStatement = config.prepareGeneralStatement(new XMLSqlSource(config, node), statement, id, resultMapName, parameterMapName, resultSetType, fetchSize, parameterClassName, resultClassName, allowRemapping, xmlResultName, timeout, cacheModelName);
    findAndParseSelectKey(node, statement);
    return mappedStatement;

  }

  private void findAndParseSelectKey(Node node, GeneralStatement statement) {
    if (statement instanceof InsertStatement) {
      config.getErrorContext().setActivity("parsing select key tags");

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
          Properties attributes = NodeletUtils.parseAttributes(child, config.globalProps);
          String keyPropName = attributes.getProperty("keyProperty");
          String resultClassName = attributes.getProperty("resultClass");
          String type = attributes.getProperty("type");
          selectKeyStatement = config.prepareSelectKeyStatement(new XMLSqlSource(config, child), resultClassName, statement.getId(), keyPropName, foundSQLFirst, type, statement.getParameterClass());
          break;
        }
      }
      config.getErrorContext().setMoreInfo(null);
      insertStatement.setSelectKeyStatement(selectKeyStatement);
    }
  }


}
