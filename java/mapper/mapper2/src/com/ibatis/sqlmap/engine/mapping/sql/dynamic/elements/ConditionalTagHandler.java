/**
 * User: Clinton Begin
 * Date: Mar 3, 2003
 * Time: 7:41:24 PM
 */
package com.ibatis.sqlmap.engine.mapping.sql.dynamic.elements;

import com.ibatis.common.beans.Probe;
import com.ibatis.common.beans.ProbeFactory;
import com.ibatis.common.exception.NestedRuntimeException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class ConditionalTagHandler extends BaseTagHandler {

  private static final Probe PROBE = ProbeFactory.getProbe();

  public static final long NOT_COMPARABLE = Long.MIN_VALUE;
  private static final String DATE_MASK = "yyyy/MM/dd hh:mm:ss";
  private static final DateFormat DATE_FORMAT = new SimpleDateFormat(DATE_MASK);

  public abstract boolean isCondition(SqlTagContext ctx, SqlTag tag, Object parameterObject);

  public int doStartFragment(SqlTagContext ctx, SqlTag tag, Object parameterObject) {
    if (isCondition(ctx, tag, parameterObject)) {
      return SqlTagHandler.INCLUDE_BODY;
    } else {
      return SqlTagHandler.SKIP_BODY;
    }
  }

  public int doEndFragment(SqlTagContext ctx, SqlTag tag, Object parameterObject, StringBuffer bodyContent) {
    return SqlTagHandler.INCLUDE_BODY;
  }

  protected long compare(SqlTagContext ctx, SqlTag tag, Object parameterObject) {
    String propertyName = tag.getPropertyAttr();
    String comparePropertyName = tag.getComparePropertyAttr();
    String compareValue = tag.getCompareValueAttr();

    String prop = tag.getPropertyAttr();
    Object value1;
    Class type;
    if (prop != null) {
      value1 = PROBE.getObject(parameterObject, propertyName);
      type = PROBE.getPropertyTypeForGetter(parameterObject, propertyName);
    } else {
      value1 = parameterObject;
      if (value1 != null) {
        type = parameterObject.getClass();
      } else {
        type = Object.class;
      }
    }
    if (comparePropertyName != null) {
      Object value2 = PROBE.getObject(parameterObject, comparePropertyName);
      return compareValues(type, value1, value2);
    } else if (compareValue != null) {
      return compareValues(type, value1, compareValue);
    } else {
      throw new NestedRuntimeException("Error comparing in conditional fragment.  Uknown 'compare to' values.");
    }
  }

  protected long compareValues(Class type, Object value1, Object value2) {
    long result = NOT_COMPARABLE;

    if (value1 == null || value2 == null) {
      result = value1 == value2 ? 0 : NOT_COMPARABLE;
    } else {
      if (value2.getClass() != type) {
        value2 = convertValue(type, value2.toString());
      }
      if (value2 instanceof String && type != String.class) {
        value1 = value1.toString();
      }
      if (!(value1 instanceof Comparable && value2 instanceof Comparable)) {
        value1 = value1.toString();
        value2 = value2.toString();
      }
      result = ((Comparable) value1).compareTo(value2);
    }

    return result;
  }

  protected Object convertValue(Class type, String value) {
    if (type == String.class) {
      return value;
    } else if (type == Byte.class || type == byte.class) {
      return Byte.valueOf(value);
    } else if (type == Short.class || type == short.class) {
      return Short.valueOf(value);
    } else if (type == Character.class || type == char.class) {
      return new Character(value.charAt(0));
    } else if (type == Integer.class || type == int.class) {
      return Integer.valueOf(value);
    } else if (type == Long.class || type == long.class) {
      return Long.valueOf(value);
    } else if (type == Float.class || type == float.class) {
      return Float.valueOf(value);
    } else if (type == Double.class || type == double.class) {
      return Double.valueOf(value);
    } else if (type == Boolean.class || type == boolean.class) {
      return Boolean.valueOf(value);
    } else if (type == Date.class) {
      try {
        return DATE_FORMAT.parse(value);
      } catch (ParseException e) {
        throw new NestedRuntimeException("Error parsing date.  Cause: " + e, e);
      }
    } else if (type == BigInteger.class) {
      return new BigInteger(value);
    } else if (type == BigDecimal.class) {
      return new BigDecimal(value);
    } else {
      return value;
    }

  }

}
