package com.ibatis.common.beans;

/**
 * <p/>
 * Date: Apr 23, 2004 11:26:44 PM
 * 
 */
public class ProbeFactory {

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
   * Factory method for getting a Probe object
   *
   * @param object - the object to get a Probe for? 
   * @return An implementation of the Probe interface?
   */
  public static Probe getProbe(Object object) {
    return GENERIC;
  }

}
