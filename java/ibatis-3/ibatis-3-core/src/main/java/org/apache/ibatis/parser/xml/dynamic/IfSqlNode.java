package org.apache.ibatis.parser.xml.dynamic;

public class IfSqlNode implements SqlNode {
  private ExpressionEvaluator evaluator;
  private String test;
  private MixedSqlNode contents;

  public IfSqlNode(String test, MixedSqlNode contents) {
    this.test = test;
    this.contents = contents;
    this.evaluator = new ExpressionEvaluator();
  }

  public boolean apply(DynamicContext context) {
    if (evaluator.evaluateBoolean(test, context.getBindings())) {
      contents.apply(context);
      return true;
    }
    return false;
  }

}
