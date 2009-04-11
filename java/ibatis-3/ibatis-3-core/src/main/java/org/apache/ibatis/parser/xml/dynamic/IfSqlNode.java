package org.apache.ibatis.parser.xml.dynamic;

import org.apache.ibatis.ognl.*;
import org.apache.ibatis.parser.ParserException;

import java.math.BigDecimal;

public class IfSqlNode implements SqlNode {
  private ExpressionEvaluator evaluator;
  private String test;
  private MixedSqlNode contents;

  public IfSqlNode(String test, MixedSqlNode contents) {
    this.test = test;
    this.contents = contents;
    this.evaluator = new ExpressionEvaluator();
  }

  public boolean apply(DynamicContext builder) {
    if (evaluator.evaluate(test, builder.getParameterObject())) {
      contents.apply(builder);
      return true;
    }
    return false;
  }

}
