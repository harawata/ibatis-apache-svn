/*
 *  Copyright 2004 Clinton Begin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.ibatis.common.util;

import com.ibatis.common.exception.NestedRuntimeException;

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
