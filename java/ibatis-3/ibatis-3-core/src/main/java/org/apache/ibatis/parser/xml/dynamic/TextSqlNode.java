package org.apache.ibatis.parser.xml.dynamic;

import org.apache.ibatis.xpath.GenericTokenParser;

public class TextSqlNode implements SqlNode {
  private String text;

  public TextSqlNode(String text) {
    this.text = text;
  }

  public boolean apply(DynamicContext context) {
    GenericTokenParser parser = new GenericTokenParser("${", "}", new BindingTokenParser(context));
    context.appendSql(parser.parse(text));
    return true;
  }

  private static class BindingTokenParser implements GenericTokenParser.TokenHandler {

    private DynamicContext context;

    public BindingTokenParser (DynamicContext context) {
      this.context = context;
    }

    public String handleToken(String content) {
      return String.valueOf(context.getBindings().get(content));
    }
  }


}

