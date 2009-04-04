package org.apache.ibatis.parser.xml.dynamic;

public class DynamicContext {

  private StringBuilder sqlBuilder = new StringBuilder();

  public void appendSql(String sql) {
    sqlBuilder.append(sql);
  }

  public String getSql() {
    return sqlBuilder.toString();
  }
}
