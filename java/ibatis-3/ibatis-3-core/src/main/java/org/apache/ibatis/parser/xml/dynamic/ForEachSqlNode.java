package org.apache.ibatis.parser.xml.dynamic;

public class ForEachSqlNode implements SqlNode {
  private ExpressionEvaluator evaluator;
  private String collectionExpression;
  private MixedSqlNode contents;
  private String open;
  private String close;
  private String separator;
  private String item;
  private String index;

  public ForEachSqlNode(String collectionExpression, String index, String item, String open, String close, String separator, MixedSqlNode contents) {
    this.evaluator = new ExpressionEvaluator();
    this.collectionExpression = collectionExpression;
    this.contents = contents;
    this.open = open;
    this.close = close;
    this.separator = separator;
    this.index = index;
    this.item = item;
  }

  public boolean apply(DynamicContext builder) {
    final Iterable iterable = evaluator.evaluateIterable(collectionExpression, builder.getBindings());
    boolean first = true;
    applyOpen(builder);
    int i = 0;
    for (Object o : iterable) {
      first = applySeparator(builder, first);
      i = applyIndex(builder, i);
      applyItem(builder, o);
      contents.apply(builder);
    }
    applyClose(builder);
    return true;
  }

  private int applyIndex(DynamicContext builder, int i) {
    if (index != null) {
      builder.bind(index, i++);
    }
    return i;
  }

  private void applyItem(DynamicContext builder, Object o) {
    if (item != null) {
      builder.bind(item, o);
    }
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
