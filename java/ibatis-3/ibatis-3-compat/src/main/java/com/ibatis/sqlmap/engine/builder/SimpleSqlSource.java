package com.ibatis.sqlmap.engine.builder;

import com.ibatis.sqlmap.engine.mapping.sql.SqlText;
import com.ibatis.sqlmap.engine.mapping.sql.simple.SimpleDynamicSql;
import com.ibatis.sqlmap.engine.mapping.sql.statik.StaticSql;
import org.apache.ibatis.mapping.Configuration;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.xml.NodeletContext;
import org.w3c.dom.*;

import java.util.*;

public class SimpleSqlSource implements SqlSource {

  private Configuration configuration;
  private XmlSqlMapConfigParser configParser;
  private XmlSqlMapParser mapParser;

  private String sql = "";
  private List<ParameterMapping> parameterMappings = new ArrayList<ParameterMapping>();

  public SimpleSqlSource(XmlSqlMapParser mapParser, NodeletContext context) {
    this.configuration = mapParser.getConfigParser().getConfiguration();
    this.configParser = mapParser.getConfigParser();
    this.mapParser = mapParser;
    this.parseNodes(context);
  }

  public String getSql(Object parameterObject) {
    if (SimpleDynamicSql.isSimpleDynamicSql(sql)) {
      return new SimpleDynamicSql(sql, parameterMappings, configuration.getTypeHandlerRegistry()).getSql(parameterObject);
    }
    return new StaticSql(sql).getSql(parameterObject);
  }

  public List<ParameterMapping> getParameterMappings(Object parameterObject) {
    return parameterMappings;
  }

  private void parseNodes(NodeletContext node) {
    StringBuilder sqlBuffer = new StringBuilder(sql);
    NodeList children = node.getNode().getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      NodeletContext child = new NodeletContext(children.item(i), configuration.getVariables());
      String nodeName = child.getNode().getNodeName();
      if (child.getNode().getNodeType() == Node.CDATA_SECTION_NODE
          || child.getNode().getNodeType() == Node.TEXT_NODE) {
        String data = child.getStringBody();
        InlineParameterMapParser inlineParameterMapParser = new InlineParameterMapParser(configuration);
        SqlText sqlText = inlineParameterMapParser.parseInlineParameterMap(data);
        sqlText.setPostParseRequired(false);

        parameterMappings.addAll(sqlText.getParameterMappings());
        sqlBuffer.append(sqlText.getText());

      } else if ("include".equals(nodeName)) {
        String refid = child.getStringAttribute("refid");
        NodeletContext includeNode = configParser.getSqlFragment(refid);
        if (includeNode == null) {
          String nsrefid = mapParser.applyNamespace(refid);
          includeNode = configParser.getSqlFragment(nsrefid);
          if (includeNode == null) {
            throw new RuntimeException("Could not find SQL statement to include with refid '" + refid + "'");
          }
        }
        parseNodes(includeNode);
      }
    }
    sql = sqlBuffer.toString();
  }

}
