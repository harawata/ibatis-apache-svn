package com.ibatis.sqlmap.engine.scope;

/**
 * An interface to simplify access to different scopes (contexts?)
 */
public interface Scope {

  /**
   * Gets a named object out of the scope
   * @param key - the name of the object to get
   * @return the object
   */
  public Object getAttribute(Object key);

  /**
   * Puts a named value into the scope
   * @param key - the name of the object to put
   * @param value - the value to associate with that name
   */
  public void setAttribute(Object key, Object value);

  /**
   * Clears all data out of the scope
   */
  public void reset();
  
}
