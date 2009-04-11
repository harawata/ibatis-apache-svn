package org.apache.ibatis.parser.xml.dynamic;

import org.apache.ibatis.ognl.*;
import org.apache.ibatis.parser.ParserException;

import java.math.BigDecimal;

public class ExpressionEvaluator {

  public boolean evaluate(String expression, Object parameterObject) {
    try {
      Object value = Ognl.getValue(expression, parameterObject);
      if (value instanceof Boolean) return (Boolean) value;
      if (value instanceof Number) return !new BigDecimal(String.valueOf(value)).equals(BigDecimal.ZERO);
      return value != null;
    } catch (OgnlException e) {
      throw new ParserException("Error evaluating expression '"+expression+"'. Cause: " + e, e);
    }
  }


}
