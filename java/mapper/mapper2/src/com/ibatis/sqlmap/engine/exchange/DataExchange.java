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
package com.ibatis.sqlmap.engine.exchange;

import com.ibatis.sqlmap.engine.cache.CacheKey;
import com.ibatis.sqlmap.engine.mapping.parameter.ParameterMap;
import com.ibatis.sqlmap.engine.mapping.result.ResultMap;
import com.ibatis.sqlmap.engine.scope.RequestScope;

import java.util.Map;

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
