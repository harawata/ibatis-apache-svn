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
package com.ibatis.sqlmap.client;

import com.ibatis.common.util.PaginatedList;
import com.ibatis.sqlmap.client.event.RowHandler;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * This interface declares all methods involved with executing statements
 * and batches for an SQL Map.
 *
 * @see SqlMapSession
 * @see SqlMapClient
 */
public interface SqlMapExecutor {

  /**
   * Executes a mapped SQL INSERT statement.
   * Insert is a bit different from other update methods, as it
   * provides facilities for returning the primary key of the
   * newly inserted row (rather than the effected rows).  This
   * functionality is of course optional.
   * <p/>
   * The parameter object is generally used to supply the input
   * data for the INSERT values.
   *
   * @param id              The name of the statement to execute.
   * @param parameterObject The parameter object (e.g. JavaBean, Map, XML etc.).
   * @return The primary key of the newly inserted row.  This might be automatically
   *         generated by the RDBMS, or selected from a sequence table or other source.
   * @throws java.sql.SQLException If an error occurs.
   */
  Object insert(String id, Object parameterObject) throws SQLException;

  /**
   * Executes a mapped SQL UPDATE statement.
   * Update can also be used for any other update statement type,
   * such as inserts and deletes.  Update returns the number of
   * rows effected.
   * <p/>
   * The parameter object is generally used to supply the input
   * data for the UPDATE values as well as the WHERE clause parameter(s).
   *
   * @param id              The name of the statement to execute.
   * @param parameterObject The parameter object (e.g. JavaBean, Map, XML etc.).
   * @return The number of rows effected.
   * @throws java.sql.SQLException If an error occurs.
   */
  int update(String id, Object parameterObject) throws SQLException;

  /**
   * Executes a mapped SQL DELETE statement.
   * Delete returns the number of rows effected.
   * <p/>
   * The parameter object is generally used to supply the input
   * data for the WHERE clause parameter(s) of the DELETE statement.
   *
   * @param id              The name of the statement to execute.
   * @param parameterObject The parameter object (e.g. JavaBean, Map, XML etc.).
   * @return The number of rows effected.
   * @throws java.sql.SQLException If an error occurs.
   */
  int delete(String id, Object parameterObject) throws SQLException;

  /**
   * Executes a mapped SQL SELECT statement that returns data to populate
   * a single object instance.
   * <p/>
   * The parameter object is generally used to supply the input
   * data for the WHERE clause parameter(s) of the SELECT statement.
   *
   * @param id              The name of the statement to execute.
   * @param parameterObject The parameter object (e.g. JavaBean, Map, XML etc.).
   * @return The single result object populated with the result set data,
   *         or null if no result was found
   * @throws java.sql.SQLException If more than one result was found, or if any other error occurs.
   */
  Object queryForObject(String id, Object parameterObject) throws SQLException;

  /**
   * Executes a mapped SQL SELECT statement that returns data to populate
   * the supplied result object.
   * <p/>
   * The parameter object is generally used to supply the input
   * data for the WHERE clause parameter(s) of the SELECT statement.
   *
   * @param id              The name of the statement to execute.
   * @param parameterObject The parameter object (e.g. JavaBean, Map, XML etc.).
   * @param resultObject    The result object instance that should be populated with result data.
   * @return The single result object as supplied by the resultObject parameter, populated with the result set data,
   *         or null if no result was found
   * @throws java.sql.SQLException If more than one result was found, or if any other error occurs.
   */
  Object queryForObject(String id, Object parameterObject, Object resultObject) throws SQLException;

  /**
   * Executes a mapped SQL SELECT statement that returns data to populate
   * a number of result objects.
   * <p/>
   * The parameter object is generally used to supply the input
   * data for the WHERE clause parameter(s) of the SELECT statement.
   *
   * @param id              The name of the statement to execute.
   * @param parameterObject The parameter object (e.g. JavaBean, Map, XML etc.).
   * @return A List of result objects.
   * @throws java.sql.SQLException If an error occurs.
   */
  List queryForList(String id, Object parameterObject) throws SQLException;

  /**
   * Executes a mapped SQL SELECT statement that returns data to populate
   * a number of result objects within a certain range.
   * <p/>
   * The parameter object is generally used to supply the input
   * data for the WHERE clause parameter(s) of the SELECT statement.
   *
   * @param id              The name of the statement to execute.
   * @param parameterObject The parameter object (e.g. JavaBean, Map, XML etc.).
   * @param skip            The number of results to ignore.
   * @param max             The maximum number of results to return.
   * @return A List of result objects.
   * @throws java.sql.SQLException If an error occurs.
   */
  List queryForList(String id, Object parameterObject, int skip, int max) throws SQLException;

  /**
   * Executes a mapped SQL SELECT statement that returns a number of
   * result objects that will be handled one at a time by a
   * RowHandler.
   * <p/>
   * This is generally a good approach to take when dealing with large sets
   * of records (i.e. hundreds, thousands...) that need to be processed without
   * eating up all of the system resources.
   * <p/>
   * The parameter object is generally used to supply the input
   * data for the WHERE clause parameter(s) of the SELECT statement.
   *
   * @param id              The name of the statement to execute.
   * @param parameterObject The parameter object (e.g. JavaBean, Map, XML etc.).
   * @param rowHandler      A RowHandler instance
   * @throws java.sql.SQLException If an error occurs.
   */
  void queryWithRowHandler(String id, Object parameterObject, RowHandler rowHandler) throws SQLException;


  /**
   * TODO : Deprecated and will be removed.
   *
   * @see RowHandler
   * @deprecated Use queryWithRowHandler instead (requires RowHandler interface change) THIS WILL BE REMOVED BY FINAL 2.0 RELEASE
   */
  List queryForList(String id, Object parameterObject, RowHandler rowHandler) throws SQLException;

  /**
   * Executes a mapped SQL SELECT statement that returns data to populate
   * a number of result objects a page at a time.
   * <p/>
   * The parameter object is generally used to supply the input
   * data for the WHERE clause parameter(s) of the SELECT statement.
   *
   * @param id              The name of the statement to execute.
   * @param parameterObject The parameter object (e.g. JavaBean, Map, XML etc.).
   * @param pageSize        The maximum number of result objects each page can hold.
   * @return A PaginatedList of result objects.
   * @throws java.sql.SQLException If an error occurs.
   */
  PaginatedList queryForPaginatedList(String id, Object parameterObject, int pageSize) throws SQLException;

  /**
   * Executes a mapped SQL SELECT statement that returns data to populate
   * a number of result objects that will be keyed into a Map.
   * <p/>
   * The parameter object is generally used to supply the input
   * data for the WHERE clause parameter(s) of the SELECT statement.
   *
   * @param id              The name of the statement to execute.
   * @param parameterObject The parameter object (e.g. JavaBean, Map, XML etc.).
   * @param keyProp         The property to be used as the key in the Map.
   * @return A Map keyed by keyProp with values being the result object instance.
   * @throws java.sql.SQLException If an error occurs.
   */
  Map queryForMap(String id, Object parameterObject, String keyProp) throws SQLException;

  /**
   * Executes a mapped SQL SELECT statement that returns data to populate
   * a number of result objects from which one property will be keyed into a Map.
   * <p/>
   * The parameter object is generally used to supply the input
   * data for the WHERE clause parameter(s) of the SELECT statement.
   *
   * @param id              The name of the statement to execute.
   * @param parameterObject The parameter object (e.g. JavaBean, Map, XML etc.).
   * @param keyProp         The property to be used as the key in the Map.
   * @param valueProp       The property to be used as the value in the Map.
   * @return A Map keyed by keyProp with values of valueProp.
   * @throws java.sql.SQLException If an error occurs.
   */
  Map queryForMap(String id, Object parameterObject, String keyProp, String valueProp) throws SQLException;

  /**
   * Starts a batch in which update statements will be cached before being sent to
   * the database all at once. This can improve overall performance of updates update
   * when dealing with numerous updates (e.g. inserting 1:M related data).
   *
   * @throws java.sql.SQLException If the batch could not be started.
   */
  void startBatch() throws SQLException;

  /**
   * Executes (flushes) all statements currently batched.
   *
   * @return the number of rows updated in the batch
   * @throws java.sql.SQLException If the batch could not be executed or if any of the statements
   *                               fails.
   */
  int executeBatch() throws SQLException;
}
