package org.apache.ibatis.parser.xml.dynamic;

import java.util.List;

public class ChooseSqlNode implements SqlNode {
  private MixedSqlNode defaultSqlNode;
  private List<IfSqlNode> ifSqlNodes;

  public ChooseSqlNode(List<IfSqlNode> ifSqlNodes, MixedSqlNode defaultSqlNode) {
    this.ifSqlNodes = ifSqlNodes;
    this.defaultSqlNode = defaultSqlNode;
  }

  public boolean apply(DynamicContext context) {
    for (SqlNode sqlNode : ifSqlNodes) {
      if (sqlNode.apply(context)) {
        return true;
      }
    }
    if (defaultSqlNode != null) {
      defaultSqlNode.apply(context);
      return true;
    }
    return false;
  }
}
