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
package com.ibatis.sqlmap.engine.mapping.statement;

import com.ibatis.sqlmap.client.event.RowHandler;
import com.ibatis.sqlmap.engine.mapping.result.ResultMap;
import com.ibatis.sqlmap.engine.scope.RequestScope;

import java.sql.SQLException;

public class RowHandlerCallback {

  private RowHandler rowHandler;
  private ResultMap resultMap;
  private Object resultObject;

  public RowHandlerCallback(ResultMap resultMap, Object resultObject, RowHandler rowHandler) {
    this.rowHandler = rowHandler;
    this.resultMap = resultMap;
    this.resultObject = resultObject;
  }

  public void handleResultObject(RequestScope request, Object[] results)
      throws SQLException {
    Object object;
    object = resultMap.setResultObjectValues(request, resultObject, results);
    rowHandler.handleRow(object);
  }

}
