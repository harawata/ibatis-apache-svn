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
package com.ibatis.sqlmap.client.event;

import java.util.List;

/**
 * Event handler for row by row processing.
 * <p/>
 * The RowHandler interface is used by the SqlMapSession.queryWithRowHandler() method.
 * Generally a RowHandler implementation will perform some row-by-row processing logic
 * in cases where there are too many rows to efficiently load into memory.
 * <p/>
 * Example:
 * <pre>
 * sqlMap.queryWithRowHandler ("findAllEmployees", null, new MyRowHandler()));
 * </pre>
 * Date: Sep 5, 2003 4:46:44 PM
 *
 * @author Clinton Begin
 */
public interface RowHandler {

  /**
   * Handles a single row of a result set.
   * <p/>
   * This method will be called for each row in a result set.  For each row the result map
   * will be applied to build the value object, which is then passed in as the valueObject
   * parameter.
   *
   * @param valueObject The object representing a single row from the query.
   * @see com.ibatis.sqlmap.client.SqlMapSession
   */
  void handleRow(Object valueObject);


  /**
   * TODO : Deprecated and will be removed.
   *
   * @see RowHandler#handleRow(Object)
   * @deprecated THIS WILL BE REMOVED BY FINAL RELEASE
   *             <p/>
   *             Use RowHandler.handleRow(Object) instead.
   *             <p/>
   *             queryForList(String,Object,RowHandler) has been renamed BACK to queryWithRowHandler(String,Object,RowHandler).
   *             The new signature is:
   *             <p/>
   *             void queryWithRowHandler(String id, Object parameterObject, RowHandler rowHandler)
   *             throws SQLException;
   *             <p/>
   *             The RowHandler interface will also be changed back to the original strategy used in 1.x. That is, there will no
   *             longer be a List parameter in the method signature of the handleRow() method.  The new signature is:
   *             <p/>
   *             public void handleRow(Object valueObject);
   *             <p/>
   *             This was necessary to make the API more flexible.  Many people complained about the change because they did not
   *             want to return a list.  They may have been using a RowHandler to build a Map, or even aggregate into a single
   *             result object.  In these cases the List parameter and return type on the RowHandler API was unecessary, confusing and extra overhead.
   *             <p/>
   *             WHAT SHOULD YOU DO?
   *             <p/>
   *             Well, basically your RowHandlers will need to be changed to handle the List themselves.  For example:
   *             <pre>
   *                                     <br/>
   *                                     <br/> ----------------------------
   *                                     <br/> // OLD WAY
   *                                     <br/>
   *                                     <br/> public class MyRowHandler imlements RowHandler {
   *                                     <br/>    public void handleRow(Object valueObject, List list) {
   *                                     <br/>        list.add(valueObject);
   *                                     <br/>    }
   *                                     <br/> }
   *                                     <br/>
   *                                     <br/> // NEW WAY
   *                                     <br/>
   *                                     <br/> public class MyNewRowHandler imlements RowHandler {
   *                                     <br/>    private List list = new ArrayList();
   *                                     <br/>    public void handleRow(Object valueObject) {
   *                                     <br/>        list.add(valueObject);
   *                                     <br/>    }
   *                                     <br/>    public List getList () {
   *                                     <br/>        return list;
   *                                     <br/>    }
   *                                     <br/> }
   *                                     <br/> ----------------------------
   *                                     <br/>
   *                                     <br/> Obviously the calling code will need to be changed too.  For example:
   *                                     <br/>
   *                                     <br/> ----------------------------
   *                                     <br/> // OLD WAY
   *                                     <br/> RowHandler rowHandler = new MyRowHandler();
   *                                     <br/> List list = sqlMap.queryForList("name", param, rowHandler);
   *                                     <br/>
   *                                     <br/> // NEW WAY
   *                                     <br/>
   *                                     <br/> RowHandler rowHandler = new MyNewRowHandler();
   *                                     <br/> sqlMap.queryWithRowHandler("name", param, rowHandler);
   *                                     <br/> List list = rowHandler.getList();
   *                                     <br/> ----------------------------
   *                                     <br/>
   *                                     <br/> The cost is a few extra lines of code, but the benefit is a great deal of
   *                                     <br/> flexibility and eliminated redundancy.
   *                                     <br/>
   *                                     </pre>
   */
  void handleRow(Object valueObject, List list);

}
