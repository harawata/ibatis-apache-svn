package com.ibatis.common.beans;

import java.util.List;

/**
 * <p/>
 * Date: Apr 23, 2004 9:34:47 PM
 * 
 * @author Clinton Begin
 */
public abstract class BaseProbe implements Probe {

  protected abstract void setProperty(Object object, String property, Object value);

  protected abstract Object getProperty(Object object, String property);

  /**
   * Returns an array of the readable properties exposed by an object
   *
   * @param object - the object
   * @return The array of property names
   */
  public abstract String[] getReadablePropertyNames(Object object);

  /**
   * Returns an array of the writeable properties exposed by an object
   *
   * @param object - the object
   * @return The array of property names
   */
  public abstract String[] getWriteablePropertyNames(Object object);

  protected Object getIndexedProperty(Object object, String indexedName) {

    Object value = null;

    try {
      String name = indexedName.substring(0, indexedName.indexOf("["));
      int i = Integer.parseInt(indexedName.substring(indexedName.indexOf("[") + 1, indexedName.indexOf("]")));
      value = getProperty(object, name);
      if (value instanceof List) {
        value = ((List) value).get(i);
      } else if (value instanceof Object[]) {
        value = ((Object[]) value)[i];
      } else if (value instanceof char[]) {
        value = new Character(((char[]) value)[i]);
      } else if (value instanceof boolean[]) {
        value = new Boolean(((boolean[]) value)[i]);
      } else if (value instanceof byte[]) {
        value = new Byte(((byte[]) value)[i]);
      } else if (value instanceof double[]) {
        value = new Double(((double[]) value)[i]);
      } else if (value instanceof float[]) {
        value = new Float(((float[]) value)[i]);
      } else if (value instanceof int[]) {
        value = new Integer(((int[]) value)[i]);
      } else if (value instanceof long[]) {
        value = new Long(((long[]) value)[i]);
      } else if (value instanceof short[]) {
        value = new Short(((short[]) value)[i]);
      } else {
        throw new ProbeException("The '" + name + "' property of the " + object.getClass().getName() + " class is not a List or Array.");
      }

    } catch (ProbeException e) {
      throw e;
    } catch (Exception e) {
      throw new ProbeException("Error getting ordinal value from JavaBean. Cause " + e, e);
    }

    return value;
  }

  protected void setIndexedProperty(Object object, String indexedName, Object value) {

    try {
      String name = indexedName.substring(0, indexedName.indexOf("["));
      int i = Integer.parseInt(indexedName.substring(indexedName.indexOf("[") + 1, indexedName.indexOf("]")));
      Object list = getProperty(object, name);
      if (list instanceof List) {
        ((List) list).set(i, value);
      } else if (value instanceof Object[]) {
        value = ((Object[]) value)[i];
      } else if (value instanceof char[]) {
        ((char[]) value)[i] = ((Character) value).charValue();
      } else if (value instanceof boolean[]) {
        ((boolean[]) value)[i] = ((Boolean) value).booleanValue();
      } else if (value instanceof byte[]) {
        ((byte[]) value)[i] = ((Byte) value).byteValue();
      } else if (value instanceof double[]) {
        ((double[]) value)[i] = ((Double) value).doubleValue();
      } else if (value instanceof float[]) {
        ((float[]) value)[i] = ((Float) value).floatValue();
      } else if (value instanceof int[]) {
        ((int[]) value)[i] = ((Integer) value).intValue();
      } else if (value instanceof long[]) {
        ((long[]) value)[i] = ((Long) value).longValue();
      } else if (value instanceof short[]) {
        ((short[]) value)[i] = ((Short) value).shortValue();
      } else {
        throw new ProbeException("The '" + name + "' property of the " + object.getClass().getName() + " class is not a List or Array.");
      }
    } catch (ProbeException e) {
      throw e;
    } catch (Exception e) {
      throw new ProbeException("Error getting ordinal value from JavaBean. Cause " + e, e);
    }
  }


}
