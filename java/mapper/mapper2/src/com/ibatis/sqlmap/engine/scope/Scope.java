package com.ibatis.sqlmap.engine.scope;

/**
 * User: Clinton Begin
 * Date: Jan 2, 2004
 * Time: 9:06:13 AM
 */
public interface Scope {

  public Object getAttribute(Object key);

  public void setAttribute(Object key, Object value);

  public void reset();
  
}
