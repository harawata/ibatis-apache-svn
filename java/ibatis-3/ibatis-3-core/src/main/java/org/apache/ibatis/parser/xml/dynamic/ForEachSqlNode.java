package org.apache.ibatis.parser.xml.dynamic;

public class ForEachSqlNode implements SqlNode {
  private ExpressionEvaluator evaluator;
  private String collectionExpression;
  private MixedSqlNode contents;
  private String open;
  private String close;
  private String separator;

  public ForEachSqlNode(String collectionExpression, String open, String close, String separator, MixedSqlNode contents) {
    this.evaluator = new ExpressionEvaluator();
    this.collectionExpression = collectionExpression;
    this.contents = contents;
    this.open = open;
    this.close = close;
    this.separator = separator;
  }

  public boolean apply(DynamicContext builder) {
    final Iterable iterable = evaluator.evaluateIterable(collectionExpression, builder.getParameterObject());
    boolean first = true;
    applyOpen(builder);
    for (Object o : iterable) {
      first = applySeparator(builder, first);
      contents.apply(builder);
    }
    applyClose(builder);
    return true;
  }

  private void applyOpen(DynamicContext builder) {
    if (open != null) {
      builder.appendSql(open);
    }
  }

  private boolean applySeparator(DynamicContext builder, boolean first) {
    if (first) {
      first = false;
    } else {
      if (separator != null) {
        builder.appendSql(separator);
      }
    }
    return first;
  }

  private void applyClose(DynamicContext builder) {
    if (close != null) {
      builder.appendSql(close);
    }
  }

}
