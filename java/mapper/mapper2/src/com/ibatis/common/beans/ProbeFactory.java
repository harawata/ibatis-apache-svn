package com.ibatis.common.beans;

/**
 * <p/>
 * Date: Apr 23, 2004 11:26:44 PM
 * 
 * @author Clinton Begin
 */
public class ProbeFactory {

  private static final Probe GENERIC = new GenericProbe();
  private static final Probe LEGACY = new LegacyBeanProbe();

  public static Probe getProbe() {
    return GENERIC;
  }

  public static Probe getProbe(Object object) {
    return GENERIC;
  }

}
