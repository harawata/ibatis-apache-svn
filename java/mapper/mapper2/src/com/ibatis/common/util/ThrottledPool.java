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

import java.util.LinkedList;

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
