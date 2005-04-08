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
package com.ibatis.sqlmap.engine.mapping.sql.dynamic.elements;

import com.ibatis.common.beans.Probe;
import com.ibatis.common.beans.ProbeFactory;
import com.ibatis.sqlmap.client.SqlMapException;

import java.lang.reflect.Array;
import java.util.*;

public class IterateTagHandler extends BaseTagHandler {

  private static final Probe PROBE = ProbeFactory.getProbe();

  public int doStartFragment(SqlTagContext ctx, SqlTag tag, Object parameterObject) {
    IterateContext iterate = (IterateContext) ctx.getAttribute(tag);
    if (iterate == null) {
      
      ctx.pushRemoveFirstPrependMarker(tag);
      
      Object collection;
      String prop = tag.getPropertyAttr();
      if (prop != null) {
        collection = PROBE.getObject(parameterObject, prop);
      } else {
        collection = parameterObject;
      }
      iterate = new IterateContext(collection);
      ctx.setAttribute(tag, iterate);
    }
    if (iterate != null && iterate.hasNext()) {
      return INCLUDE_BODY;
    } else {
      return SKIP_BODY;
    }
  }

  public int doEndFragment(SqlTagContext ctx, SqlTag tag, Object parameterObject, StringBuffer bodyContent) {
    IterateContext iterate = (IterateContext) ctx.getAttribute(tag);

    if (iterate.hasNext()) {
      iterate.next();

      String propName = tag.getPropertyAttr();
      if (propName == null) {
        propName = "";
      }

      String find = propName + "[]";
      String replace = propName + "[" + iterate.getIndex() + "]";
      replace(bodyContent, find, replace);

      if (iterate.isFirst()) {
        String open = tag.getOpenAttr();
        if (open != null) {
          bodyContent.insert(0, open);
        }
      }
      if (!iterate.isLast()) {
        String conj = tag.getConjunctionAttr();
        if (conj != null) {
          bodyContent.append(conj);
        }
      }

      if (iterate.isLast()) {
        String close = tag.getCloseAttr();
        if (close != null) {
          bodyContent.append(close);
        }
      }

      return REPEAT_BODY;
    } else {
      return super.doEndFragment(ctx,tag,parameterObject,bodyContent);
    }
  }

  public void doPrepend(SqlTagContext ctx, SqlTag tag, Object parameterObject, StringBuffer bodyContent) {
    IterateContext iterate = (IterateContext) ctx.getAttribute(tag);
    if (iterate.isFirst()) {
      super.doPrepend(ctx, tag, parameterObject, bodyContent);
    }
  }

  private static void replace(StringBuffer buffer, String find, String replace) {
    int pos = buffer.toString().indexOf(find);
    int len = find.length();
    while (pos > -1) {
      buffer.replace(pos, pos + len, replace);
      pos = buffer.toString().indexOf(find);
    }
  }

  private class IterateContext implements Iterator {

    private Iterator iterator;
    private int index = -1;

    public IterateContext(Object collection) {
      if (collection instanceof Collection) {
        this.iterator = ((Collection) collection).iterator();
      } else if (collection instanceof Iterator) {
        this.iterator = ((Iterator) collection);
      } else if (collection.getClass().isArray()) {
        List list = arrayToList(collection);
        this.iterator = list.iterator();
      } else {
        throw new SqlMapException("ParameterObject or property was not a Collection, Array or Iterator.");
      }
    }

    public boolean hasNext() {
      return iterator != null && iterator.hasNext();
    }

    public Object next() {
      index++;
      return iterator.next();
    }

    public void remove() {
      iterator.remove();
    }

    public int getIndex() {
      return index;
    }

    public boolean isFirst() {
      return index == 0;
    }

    public boolean isLast() {
      return iterator != null && !iterator.hasNext();
    }

  }

  public boolean isPostParseRequired() {
    return true;
  }

  private List arrayToList(Object array) {
    List list = null;
    if (array instanceof Object[]) {
      list = Arrays.asList((Object[]) array);
    } else {
      list = new ArrayList();
      for (int i = 0, n = Array.getLength(array); i < n; i++) {
        list.add(Array.get(array, i));
      }
    }
    return list;
  }

}

