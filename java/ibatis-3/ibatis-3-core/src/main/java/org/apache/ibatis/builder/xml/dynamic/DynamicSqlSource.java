package org.apache.ibatis.builder.xml.dynamic;

import org.apache.ibatis.builder.SqlSourceParser;
import org.apache.ibatis.mapping.*;

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
    Class parameterType = parameterObject == null ? Object.class : parameterObject.getClass();
    SqlSource sqlSource = sqlSourceParser.parse(context.getSql(), parameterType);
    return sqlSource.getBoundSql(parameterObject);
  }

}
