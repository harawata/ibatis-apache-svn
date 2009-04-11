package org.apache.ibatis.parser.xml.dynamic;

public class ForEachSqlNode implements SqlNode {
  private ExpressionEvaluator evaluator;
  private String collectionExpression;
  private MixedSqlNode contents;

  public ForEachSqlNode(String collectionExpression, MixedSqlNode contents) {
    this.evaluator = new ExpressionEvaluator();
    this.collectionExpression = collectionExpression;
    this.contents = contents;
  }

  public boolean apply(DynamicContext builder) {
    final Iterable iterable = evaluator.evaluateIterable(collectionExpression, builder.getParameterObject());
    for (Object o : iterable) {
      contents.apply(builder);
    }
    return true;
  }

}
