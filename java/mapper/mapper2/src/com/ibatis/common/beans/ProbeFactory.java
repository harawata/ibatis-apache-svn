package com.ibatis.common.beans;

import java.util.Map;

/**
 * <p/>
 * Date: Apr 23, 2004 11:26:44 PM
 */
public class ProbeFactory {

  private static final Probe DOM = new DomProbe();
  private static final Probe MAP = new MapProbe();
  private static final Probe BEAN = new JavaBeanProbe();
  private static final Probe GENERIC = new GenericProbe();
  private static final Probe LEGACY = new LegacyBeanProbe();

  /**
   * Factory method for getting a Probe object
   * 
   * @return An implementation of the Probe interface
   */
  public static Probe getProbe() {
    return GENERIC;
  }

  /**
   * Factory method for getting a Probe object that is
   * the best choice for the type of object supplied
   * by the object parameter.
   *
   * @param object The object to get a Probe for
   * @return An implementation of the Probe interface
   */
  public static Probe getProbe(Object object) {
    if (object instanceof Map) {
      return MAP;
    } else if (object instanceof org.w3c.dom.Document) {
      return DOM;
    } else if (object instanceof Class) {
      return LEGACY;
    } else {
      return BEAN;
    }
  }

}
