package org.apache.ibatis.parser.xml.dynamic;

import org.apache.ibatis.mapping.*;
import org.apache.ibatis.parser.SqlSourceParser;

public class DynamicSqlSource implements SqlSource {

  private Configuration configuration;
  private SqlNode rootSqlNode;

  public DynamicSqlSource(Configuration configuration, SqlNode rootSqlNode) {
    this.configuration = configuration;
    this.rootSqlNode = rootSqlNode;
  }

  public BoundSql getBoundSql(Object parameterObject) {
    DynamicContext context = new DynamicContext(parameterObject);
    rootSqlNode.apply(context);
    SqlSourceParser sqlSourceParser = new SqlSourceParser(configuration);
    SqlSource sqlSource = sqlSourceParser.parse(context.getSql());
    return sqlSource.getBoundSql(parameterObject);
  }

}
