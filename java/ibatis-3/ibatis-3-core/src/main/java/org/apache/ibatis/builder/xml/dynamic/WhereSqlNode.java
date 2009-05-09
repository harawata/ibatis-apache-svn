package org.apache.ibatis.builder.xml.dynamic;

public class WhereSqlNode extends PrefixSqlNode{

  public WhereSqlNode(SqlNode contents) {
    super(contents, "WHERE", "AND |OR ");
  }


}
