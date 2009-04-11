package org.apache.ibatis.parser.xml.dynamic;

public class DynamicContext {

  private Object parameterObject;
  private StringBuilder sqlBuilder = new StringBuilder();

  public DynamicContext(Object parameterObject) {
    this.parameterObject = parameterObject;
  }

  public Object getParameterObject() {
    return parameterObject;
  }

  public void appendSql(String sql) {
    sqlBuilder.append(sql);
    sqlBuilder.append(" ");
  }

  public String getSql() {
    return sqlBuilder.toString().trim();
  }

}
