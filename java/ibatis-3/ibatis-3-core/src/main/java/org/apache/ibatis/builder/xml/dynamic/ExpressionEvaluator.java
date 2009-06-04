package org.apache.ibatis.builder.xml.dynamic;

import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.ognl.*;

import java.math.BigDecimal;
import java.util.Arrays;

public class ExpressionEvaluator {

  public boolean evaluateBoolean(String expression, Object parameterObject) {
    try {
      Object value = Ognl.getValue(expression, parameterObject);
      if (value instanceof Boolean) return (Boolean) value;
      if (value instanceof Number) return !new BigDecimal(String.valueOf(value)).equals(BigDecimal.ZERO);
      return value != null;
    } catch (OgnlException e) {
      throw new BuilderException("Error evaluating expression '"+expression+"'. Cause: " + e, e);
    }
  }

  public Iterable evaluateIterable(String expression, Object parameterObject) {
    try {
      Object value = Ognl.getValue(expression, parameterObject);
      if (value instanceof Iterable) return (Iterable) value;
      if (value.getClass().isArray()) return Arrays.asList((Object[])value);
      throw new BuilderException("Error evaluating expression '"+expression+"'.  Return value ("+value+") was not iterable.");
    } catch (OgnlException e) {
      throw new BuilderException("Error evaluating expression '"+expression+"'. Cause: " + e, e);
    }
  }


}
