package com.ibatis.sqlmap.engine.exchange;

import com.ibatis.sqlmap.engine.mapping.parameter.*;

import com.ibatis.sqlmap.engine.scope.*;
import com.ibatis.sqlmap.engine.cache.*;

/**
 * User: Clinton Begin
 * Date: Nov 24, 2003
 * Time: 9:54:28 PM
 */
public abstract class BaseDataExchange implements DataExchange {

  public CacheKey getCacheKey(RequestScope request, ParameterMap parameterMap, Object parameterObject) {
    CacheKey key = new CacheKey();
    Object[] data = getData(request, parameterMap, parameterObject);
    for (int i = 0; i < data.length; i++) {
      if (data[i] != null) {
        key.update(data[i]);
      }
    }
    return key;
  }

}
