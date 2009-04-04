package org.apache.ibatis.parser.xml.dynamic;

public class TextSqlNode implements SqlNode {
  private String text;

  public TextSqlNode(String text) {
    this.text = text;
  }

  public boolean apply(DynamicContext builder) {
    builder.appendSql(text);
    return true;
  }
}

