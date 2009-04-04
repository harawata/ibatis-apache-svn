package org.apache.ibatis.parser.xml.dynamic;

public class IfSqlNode implements SqlNode {
  private boolean test;
  private MixedSqlNode contents;

  public IfSqlNode(String test, MixedSqlNode contents) {
    this.test = Boolean.valueOf(test);
    this.contents = contents;
  }

  public boolean apply(DynamicContext builder) {
    if (test) {
      contents.apply(builder);
      return true;
    }
    return false;
  }

}
