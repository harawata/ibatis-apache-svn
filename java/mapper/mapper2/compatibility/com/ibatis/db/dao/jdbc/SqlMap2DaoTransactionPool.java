package com.ibatis.db.dao.jdbc;

import com.ibatis.common.exception.NestedRuntimeException;
import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.db.dao.DaoException;
import com.ibatis.db.dao.DaoTransaction;
import com.ibatis.db.dao.DaoTransactionPool;

import java.io.Reader;
import java.util.Map;
import java.sql.SQLException;

/**
 * <p/>
 * Date: Feb 23, 2004 9:19:43 PM
 * 
 * @author Clinton Begin
 */
public class SqlMap2DaoTransactionPool implements DaoTransactionPool {

  private SqlMapClient sqlMap;

  public void configure(Map properties)
      throws DaoException {

    try {
      String xmlConfig = (String) properties.get("sql-map-config-file");

      Reader reader = Resources.getResourceAsReader(xmlConfig);
      sqlMap = SqlMapClientBuilder.buildSqlMapClient(reader);
    } catch (Exception e) {
      throw new NestedRuntimeException("Error configuring SqlMapClientDaoTransactionPool.  Cause: " + e, e);
    }

  }

  public DaoTransaction getTransaction()
      throws DaoException {
    try {
      sqlMap.startTransaction();
      return new SqlMap2DaoTransaction(sqlMap);
    } catch (SQLException e) {
      throw new DaoException("Error getting transaction. Cause: " + e, e);
    }
  }

  public void releaseTransaction(DaoTransaction trans)
      throws DaoException {
    // No implementation required.
  }

  public SqlMapClient getSqlMapClient() {
    return sqlMap;
  }


}
