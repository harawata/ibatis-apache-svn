package com.ibatis.common.util;

import com.ibatis.common.exception.*;

import java.util.*;

/**
 * User: Clinton Begin
 * Date: Jan 4, 2004
 * Time: 12:01:36 PM
 */
public class ThrottledPool {

  private Throttle throttle;
  private Class type;
  private LinkedList pool = new LinkedList();

  public ThrottledPool(Class type, int size) {
    this.type = type;
    this.throttle = new Throttle(size);
  }

  public Object pop() {
    throttle.increment();
    Object object;
    synchronized (pool) {
      if (pool.size() > 0) {
        object = pool.removeFirst();
      } else {
        try {
          object = type.newInstance();
        } catch (Exception e) {
          throw new NestedRuntimeException("Error instantiating class.  Cause: " + e, e);
        }
      }
    }
    return object;
  }

  public void push(Object o) {
    synchronized (pool) {
      if (o != null) pool.addLast(o);
    }
    throttle.decrement();
  }


}
