package com.ibatis.sqlmap.engine.builder.xml;

import com.ibatis.common.xml.NodeletUtils;
import com.ibatis.sqlmap.engine.mapping.statement.*;
import org.w3c.dom.Node;

import java.util.Properties;

public class SqlStatementParser {

  private ParserState state;

  public SqlStatementParser(ParserState state) {
    this.state = state;
  }

  public MappedStatement parseGeneralStatement(Node node, GeneralStatement statement) {

    // get attributes
    Properties attributes = NodeletUtils.parseAttributes(node, state.globalProps);
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

    return state.prepareGeneralStatement(new XMLStatementProcessor(state, node), statement, id, resultMapName, parameterMapName, resultSetType, fetchSize, parameterClassName, resultClassName, allowRemapping, xmlResultName, timeout, cacheModelName);

  }

}
