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
package com.ibatis.db.sqlmap;

import java.util.List;

/**
 * User: Clinton Begin
 * Date: Nov 29, 2003
 * Time: 12:46:44 PM
 */
public class RowHandlerAdapter implements com.ibatis.sqlmap.client.event.RowHandler {

  private RowHandler rowHandler;

  RowHandlerAdapter(RowHandler rowHandler) {
    this.rowHandler = rowHandler;
  }

  public void handleRow(Object valueObject) {
    rowHandler.handleRow(valueObject);
  }

  /**
   * TODO : DEPRECATED
   *
   * @deprecated Use handleRow(Object)
   */
  public void handleRow(Object valueObject, List list) {
    throw new UnsupportedOperationException("DEPRECATED: This should never be called internally.");
  }

}
