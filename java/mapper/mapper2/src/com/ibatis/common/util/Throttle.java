/**
 * User: Clinton Begin
 * Date: Apr 20, 2003
 * Time: 5:13:16 PM
 */
package com.ibatis.common.util;

import com.ibatis.common.exception.*;

public class Throttle {

  private final Object LOCK = new Object();

  private int count;
  private int limit;
  private long maxWait;

  public Throttle(int limit) {
    this.limit = limit;
    this.maxWait = 0;
  }

  public Throttle(int limit, long maxWait) {
    this.limit = limit;
    this.maxWait = maxWait;
  }

  public void increment() {
    synchronized (LOCK) {
      if (count >= limit) {
        if (maxWait > 0) {
          long waitTime = System.currentTimeMillis();
          try {
            LOCK.wait(maxWait);
          } catch (InterruptedException e) {
            //ignore
          }
          waitTime = System.currentTimeMillis() - waitTime;
          if (waitTime > maxWait) {
            throw new NestedRuntimeException("Throttle waited too long (" + waitTime + ") for lock.");
          }
        } else {
          try {
            LOCK.wait();
          } catch (InterruptedException e) {
            //ignore
          }
        }
      }
      count++;
    }
  }

  public void decrement() {
    synchronized (LOCK) {
      count--;
      LOCK.notify();
    }
  }
}
