package com.ibatis.sqlmap.engine.scope;

import java.util.*;

/**
 * User: Clinton Begin
 * Date: Jan 2, 2004
 * Time: 9:06:22 AM
 */
public abstract class BaseScope implements Scope {

  private HashMap map = new HashMap(0);

  public Object getAttribute(Object key) {
    return map.get(key);
  }

  public void setAttribute(Object key, Object value) {
    map.put(key, value);
  }

  public void reset() {
    map.clear();
  }

}
