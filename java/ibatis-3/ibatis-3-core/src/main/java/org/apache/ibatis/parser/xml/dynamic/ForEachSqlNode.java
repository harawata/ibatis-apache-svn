package org.apache.ibatis.parser.xml.dynamic;

public class ForEachSqlNode implements SqlNode {
  private ExpressionEvaluator evaluator;
  private String collectionExpression;
  private SqlNode contents;
  private String open;
  private String close;
  private String separator;
  private String item;
  private String index;

  public ForEachSqlNode(SqlNode contents, String collectionExpression, String index, String item, String open, String close, String separator) {
    this.evaluator = new ExpressionEvaluator();
    this.collectionExpression = collectionExpression;
    this.contents = contents;
    this.open = open;
    this.close = close;
    this.separator = separator;
    this.index = index;
    this.item = item;
  }

  public boolean apply(DynamicContext context) {
    final Iterable iterable = evaluator.evaluateIterable(collectionExpression, context.getBindings());
    boolean first = true;
    applyOpen(context);
    int i = 0;
    for (Object o : iterable) {
      first = applySeparator(context, first);
      i = applyIndex(context, i);
      applyItem(context, o);
      contents.apply(context);
    }
    applyClose(context);
    return true;
  }

  private int applyIndex(DynamicContext context, int i) {
    if (index != null) {
      context.bind(index, i++);
    }
    return i;
  }

  private void applyItem(DynamicContext context, Object o) {
    if (item != null) {
      context.bind(item, o);
    }
  }

  private void applyOpen(DynamicContext context) {
    if (open != null) {
      context.appendSql(open);
    }
  }

  private boolean applySeparator(DynamicContext context, boolean first) {
    if (first) {
      first = false;
    } else {
      if (separator != null) {
        context.appendSql(separator);
      }
    }
    return first;
  }

  private void applyClose(DynamicContext context) {
    if (close != null) {
      context.appendSql(close);
    }
  }

}
