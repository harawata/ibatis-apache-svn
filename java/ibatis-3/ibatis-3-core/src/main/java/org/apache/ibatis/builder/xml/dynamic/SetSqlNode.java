package org.apache.ibatis.builder.xml.dynamic;

public class SetSqlNode extends PrefixSqlNode {

  public SetSqlNode(SqlNode contents) {
    super(contents, "SET", ",");
  }

}
