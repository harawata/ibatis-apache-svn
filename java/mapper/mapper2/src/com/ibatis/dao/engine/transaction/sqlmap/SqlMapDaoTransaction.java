package com.ibatis.dao.engine.transaction.sqlmap;

import com.ibatis.dao.client.DaoTransaction;
import com.ibatis.dao.client.DaoException;
import com.ibatis.sqlmap.client.SqlMapClient;

import java.sql.SQLException;

/**
 *
 *
 * <p>
 * Date: Jan 27, 2004 10:48:12 PM
 * @author Clinton Begin
 */
public class SqlMapDaoTransaction implements DaoTransaction {

  private SqlMapClient client;

  public SqlMapDaoTransaction(SqlMapClient client) {
    try {
      client.startTransaction();
      this.client = client;
    } catch (SQLException e) {
      throw new DaoException("Error starting SQL Map transaction.  Cause: " + e, e);
    }
  }

  public void commit() {
    try {
      client.commitTransaction();
      client.endTransaction();
    } catch (SQLException e) {
      throw new DaoException("Error committing SQL Map transaction.  Cause: " + e, e);
    }
  }

  public void rollback() {
    try {
      client.endTransaction();
    } catch (SQLException e) {
      throw new DaoException("Error ending SQL Map transaction.  Cause: " + e, e);
    }
  }

  public SqlMapClient getSqlMap () {
    return client;
  }

}
