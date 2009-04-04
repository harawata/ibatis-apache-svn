package org.apache.ibatis.parser.xml.dynamic;

import java.util.List;

public class MixedSqlNode implements SqlNode {
  private List<SqlNode> contents;

  public MixedSqlNode(List<SqlNode> contents) {
    this.contents = contents;
  }

  public boolean apply(DynamicContext builder) {
    for (SqlNode sqlNode : contents) {
      sqlNode.apply(builder);
    }
    return true;
  }
}
