/**
 * User: Clinton Begin
 * Date: Mar 30, 2003
 * Time: 9:20:49 PM
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
      String prop = tag.getPropertyAttr();
      Object collection;
      if (prop != null) {
        collection = PROBE.getObject(parameterObject, tag.getPropertyAttr());
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
      return INCLUDE_BODY;
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

