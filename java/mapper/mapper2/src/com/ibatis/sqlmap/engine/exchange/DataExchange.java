package com.ibatis.sqlmap.engine.exchange;

import com.ibatis.sqlmap.engine.mapping.parameter.*;
import com.ibatis.sqlmap.engine.mapping.result.*;

import com.ibatis.sqlmap.engine.scope.*;
import com.ibatis.sqlmap.engine.cache.*;

import java.util.*;

/**
 * User: Clinton Begin
 * Date: Sep 6, 2003
 * Time: 8:29:31 AM
 */
public interface DataExchange {

  /**
   * Initializes the data exchange instance.
   *
   * @param properties
   */
  public void initialize(Map properties);

  /**
   * Gets a data array from a parameter object.
   *
   * @param parameterMap
   * @param parameterObject
   * @return
   */
  public Object[] getData(RequestScope request, ParameterMap parameterMap, Object parameterObject);

  /**
   * Sets values from a data array into a result object.
   *
   * @param resultMap
   * @param resultObject
   * @param values
   * @return
   */
  public Object setData(RequestScope request, ResultMap resultMap, Object resultObject, Object[] values);


  /**
   *
   * @param parameterMap
   * @param parameterObject
   * @param values
   * @return
   */
  public Object setData(RequestScope request, ParameterMap parameterMap, Object parameterObject, Object[] values);

  /**
   * Returns an object capable of being a unique cache key for a parameter object.
   *
   * @param parameterMap
   * @param parameterObject
   * @return
   */
  public CacheKey getCacheKey(RequestScope request, ParameterMap parameterMap, Object parameterObject);

}
