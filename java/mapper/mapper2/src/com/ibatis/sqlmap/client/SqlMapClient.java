package com.ibatis.sqlmap.client;

/**
 * A thread safe client for working with your SQL Maps (Start Here).  This interface inherits transaction control
 * and execution methods from the SqlMapTransactionManager and SqlMapExecutor interfaces.
 * <p>
 * The SqlMapClient is the central class for working with SQL Maps.  This class will allow you
 * to run mapped statements (select, insert, update, delete etc.), and also demarcate
 * transactions and work with batches.  Once you have an SqlMapClient instance, everything
 * you need to work with SQL Maps is easily available.
 * <p>
 * The SqlMapClient can either
 * be worked with directly as a multi-threaded client (internal session management), or you can get a single threaded
 * session and work with that.  There may be a slight performance increase if you explicitly
 * get a session (using the openSession() method), as it saves the SqlMapClient from having
 * to manage threads contexts.  But for most cases it won't make much of a difference, so
 * choose whichever paradigm suits your needs or preferences.
 * <p>
 * An SqlMapClient instance can be safely made <i>static</i> or applied as a <i>Singleton</i>.
 * Generally it's a good idea to make a simple configuration class that will configure the
 * instance (using SqlMapClientBuilder) and provide access to it.
 * <p>
 * <b>The following example will demonstrate the use of SqlMapClient.</b>
 * <pre>
 * <i><font color="green">
 * //
 * // autocommit simple query --these are just examples...not patterns
 * //
 * </font></i>
 * Employee emp = (Employee) <b>sqlMap.queryForObject("getEmployee", new Integer(1))</b>;
 * <i><font color="green">
 * //
 * // transaction --these are just examples...not patterns
 * //
 * </font></i>
 * try {
 *   <b>sqlMap.startTransaction()</b>
 *   Employee emp2 = new Employee();
 *   // ...set emp2 data
 *   Integer generatedKey = (Integer) <b>sqlMap.insert ("insertEmployee", emp2)</b>;
 *   emp2.setFavouriteColour ("green");
 *   <b>sqlMap.update("updateEmployee", emp2)</b>;
 *   <b>sqlMap.commitTransaction()</b>;
 * } finally {
 *   <b>sqlMap.endTransaction()</b>;
 * }
 * <i><font color="green">
 * //
 * // session --these are just examples...not patterns
 * //
 * </font></i>
 * try {
 *   <b>SqlMapSession session = sqlMap.openSession()</b>
 *   <b>session.startTransaction()</b>
 *   Employee emp2 = new Employee();
 *   // ...set emp2 data
 *   Integer generatedKey = (Integer) <b>session.insert ("insertEmployee", emp2)</b>;
 *   emp2.setFavouriteColour ("green");
 *   <b>session.update("updateEmployee", emp2)</b>;
 *   <b>session.commitTransaction()</b>;
 * } finally {
 *   try {
 *     <b>session.endTransaction()</b>;
 *   } finally {
 *     <b>session.close()</b>;
 *   }
 *   // Generally your session scope would be in a wider context and therefore the
 *   // ugly nested finally block above would not be there.  Realize that sessions
 *   // MUST be closed if explicitly opened (via openSession()).
 * }
 * <i><font color="green">
 * //
 * // batch --these are just examples...not patterns
 * //
 * </font></i>
 * try {
 *   <b>sqlMap.startTransaction()</b>
 *   List list = (Employee) <b>sqlMap.queryForList("getFiredEmployees", null)</b>;
 *   <b>sqlMap.startBatch ()</b>;
 *   for (int i=0, n=list.size(); i < n; i++) {
 *     <b>sqlMap.delete ("deleteEmployee", list.get(i))</b>;
 *   }
 *   <b>sqlMap.executeBatch()</b>;
 *   <b>sqlMap.commitTransaction()</b>;
 * } finally {
 *   <b>sqlMap.endTransaction()</b>;
 * }
 * </pre>
 * Date: Sep 5, 2003 9:23:34 PM
 * @author Clinton Begin
 * @see SqlMapClientBuilder, SqlMapSession, SqlMapExecutor
 */
public interface SqlMapClient extends SqlMapExecutor, SqlMapTransactionManager {

  /**
   * Generally would return a single threaded SqlMapSession implementation for use by
   * one user.  Remember though, that SqlMapClient itself is a thread safe SqlMapSession
   * implementation, so you can also just work directly with it.  If you do get a session
   * explicitly using this method <b>be sure to close it!</b>  You can close a session using
   * the sqlMapSession.close() method.
   * <p>
   * @return An SqlMapSession instance.
   */
  public SqlMapSession openSession();

  /**
   * @deprecated Use openSession() instead.  THIS METHOD WILL BE REMOVED BEFORE
   * FINAL RELEASE.
   * @return
   */
  public SqlMapSession getSession();

  /**
   * Flushes the data cache for each caching statement.
   */
  public void flushDataCache();

}
