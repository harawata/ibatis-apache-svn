package org.apache.ibatis.parser.xml.dynamic;

import org.apache.ibatis.ognl.*;
import org.apache.ibatis.parser.ParserException;

import java.math.BigDecimal;
import java.util.Arrays;

public class ExpressionEvaluator {

  public boolean isTrue(String expression, Object parameterObject) {
    try {
      Object value = Ognl.getValue(expression, parameterObject);
      if (value instanceof Boolean) return (Boolean) value;
      if (value instanceof Number) return !new BigDecimal(String.valueOf(value)).equals(BigDecimal.ZERO);
      return value != null;
    } catch (OgnlException e) {
      throw new ParserException("Error evaluating expression '"+expression+"'. Cause: " + e, e);
    }
  }

  public Iterable getIterable(String expression, Object parameterObject) {
    try {
      Object value = Ognl.getValue(expression, parameterObject);
      if (value instanceof Iterable) return (Iterable) value;
      if (value.getClass().isArray()) return Arrays.asList((Object[])value);
      throw new ParserException("Error evaluating expression '"+expression+"'.  Return value ("+value+") was not iterable.");
    } catch (OgnlException e) {
      throw new ParserException("Error evaluating expression '"+expression+"'. Cause: " + e, e);
    }
  }


}
