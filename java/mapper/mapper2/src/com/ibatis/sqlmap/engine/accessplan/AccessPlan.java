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
package com.ibatis.sqlmap.engine.accessplan;

/**
 * User: Clinton Begin
 * Date: Nov 22, 2003
 * Time: 7:03:25 PM
 */
public interface AccessPlan {

  /**
   * Sets all of the properties of a bean
   *
   * @param object - the bean
   * @param values - the property values
   */
  public void setProperties(Object object, Object[] values);

  /**
   * Gets all of the properties of a bean
   *
   * @param object - the bean
   * @return the properties
   */
  public Object[] getProperties(Object object);

}
