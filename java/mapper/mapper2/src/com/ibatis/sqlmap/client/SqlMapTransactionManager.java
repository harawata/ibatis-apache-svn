package com.ibatis.sqlmap.client;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.Connection;

/**
 * This interface declares methods for demarcating SQL Map transactions. 
 *
 * <p/>
 * Date: Mar 2, 2004 9:12:21 PM
 * 
 * @author Clinton Begin
 * @see SqlMapSession, SqlMapClient
 */
public interface SqlMapTransactionManager {

  /**
   * Demarcates the beginning of a transaction scope.  Transactions must be properly
   * committed or rolled back to be effective.  Use the following pattern when working
   * with transactions:
   * <pre>
   * try {
   *   sqlMap.startTransaction();
   *   // do work
   *   sqlMap.commitTransaction();
   * } finally {
   *   sqlMap.endTransaction();
   * }
   * </pre>
   *
   * Always call endTransaction() once startTransaction() has been called.
   *
   * @throws java.sql.SQLException If an error occurs while starting the transaction, or
   * the transaction could not be started.
   */
  public void startTransaction() throws SQLException;

  /**
   * Commits the currently started transaction.
   *
   * @throws SQLException If an error occurs while committing the transaction, or
   * the transaction could not be committed.
   */
  public void commitTransaction() throws SQLException;

  /**
   * Ends a transaction and rolls back if necessary.  If the transaction has
   * been started, but not committed, it will be rolled back upon calling
   * endTransaction().
   *
   * @throws SQLException If an error occurs during rollback or the transaction could
   * not be ended.
   */
  public void endTransaction() throws SQLException;

  /**
   * Allows the developer to easily use an externally supplied connection
   * when executing statements.
   * <p>
   * <b>Important:</b> Using a user supplied connection basically sidesteps the transaction manager,
   * so you are responsible for appropriately.  Here's a (very) simple example (throws SQLException):
   * <pre>
   * try {
   *   Connection myConnection = dataSource.getConnection();
   *   sqlMap.setUserConnection(myConnection);
   *   // do work
   *   myConnection.commit();
   * } catch (SQLException e) {
   *   myConnection.rollback();
   *   throw e;
   * } finally {
   *   myConnection.close();
   *   sqlMap.setUserConnection(null);
   * }
   * </pre>
   *
   * @param connnection
   * @throws SQLException
   */
  public void setUserConnection(Connection connnection) throws SQLException;

  /**
   * Returns the current user supplied connection as set by setUserConnection().
   *
   * @return The current user supplied connection.
   * @throws SQLException
   */
  public Connection getUserConnection() throws SQLException;

  /**
   * Returns the DataSource instance currently being used by the SqlMapSession.
   *
   * @return The DataSource instance currently being used by the SqlMapSession.
   */
  public DataSource getDataSource();


}
