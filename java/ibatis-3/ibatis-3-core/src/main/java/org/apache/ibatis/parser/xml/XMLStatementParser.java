package org.apache.ibatis.parser.xml;

import org.apache.ibatis.xml.NodeletContext;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.parser.*;

public class XMLStatementParser extends BaseParser {

  protected SequentialMapperBuilder sequentialBuilder;

  public XMLStatementParser(Configuration configuration, SequentialMapperBuilder sequentialBuilder) {
    super(configuration);
    this.sequentialBuilder = sequentialBuilder;
  }

  public void parseStatementNode(NodeletContext context) {
    String id = context.getStringAttribute("id");
    String sql = context.getStringBody();
    Integer fetchSize = context.getIntAttribute("fetchSize", null);
    Integer timeout = context.getIntAttribute("timeout", null);
    boolean isSelect = "select".equals(context.getNode().getNodeName());
    boolean flushCache = context.getBooleanAttribute("flushCache", !isSelect);
    boolean useCache = context.getBooleanAttribute("useCache", isSelect);
    String parameterMap = context.getStringAttribute("parameterMap");
    String parameterType = context.getStringAttribute("parameterType");
    Class parameterTypeClass = resolveClass(parameterType);
    String resultMap = context.getStringAttribute("resultMap");
    String resultType = context.getStringAttribute("resultType");
    Class resultTypeClass = resolveClass(resultType);
    String resultSetType = context.getStringAttribute("resultSetType");
    StatementType statementType = StatementType.valueOf(context.getStringAttribute("statementType", StatementType.PREPARED.toString()));
    ResultSetType resultSetTypeEnum = resolveResultSetType(resultSetType);
    SqlSource sqlSource = new SqlSourceParser(configuration).parse(sql);
    sequentialBuilder.statement(id, sqlSource, fetchSize, timeout, parameterMap, parameterTypeClass,
        resultMap, resultTypeClass, resultSetTypeEnum, isSelect, flushCache, useCache, statementType);
  }

  

}
