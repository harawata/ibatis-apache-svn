package org.apache.ibatis.parser.xml.dynamic;

import java.util.List;

public class ConditionalSqlNode implements SqlNode {
  private MixedSqlNode defaultSqlNode;
  private List<IfSqlNode> ifSqlNodes;

  public ConditionalSqlNode(List<IfSqlNode> ifSqlNodes, MixedSqlNode defaultSqlNode) {
    this.ifSqlNodes = ifSqlNodes;
    this.defaultSqlNode = defaultSqlNode;
  }

  public boolean apply(DynamicContext builder) {
    for(SqlNode sqlNode : ifSqlNodes) {
      if (sqlNode.apply(builder)) {
        return true;
      }
    }
    if (defaultSqlNode != null) {
      defaultSqlNode.apply(builder);
      return true;
    }
    return false;
  }
}
