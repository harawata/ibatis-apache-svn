package com.ibatis.db.dao.jdbc;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.db.dao.DaoException;
import com.ibatis.db.dao.DaoTransaction;

import java.sql.SQLException;

/**
 * <p/>
 * Date: Feb 23, 2004 9:19:59 PM
 * 
 * @author Clinton Begin
 */
public class SqlMap2DaoTransaction implements DaoTransaction {


  private SqlMapClient client;

  public SqlMap2DaoTransaction(SqlMapClient sqlMap) {
    this.client = sqlMap;
  }

  public void commit() throws DaoException {
    try {
      client.commitTransaction();
      client.endTransaction();
    } catch (SQLException e) {
      throw new DaoException("Error committing transaction. Cause: " + e, e);
    }
  }

  public void rollback() throws DaoException {
    try {
      client.endTransaction();
    } catch (SQLException e) {
      throw new DaoException("Error rolling back transaction. Cause: " + e, e);
    }
  }

  public void release() throws DaoException {
    // No implementation required.
  }

  public SqlMapClient getSqlMapClient() {
    return client;
  }

}
