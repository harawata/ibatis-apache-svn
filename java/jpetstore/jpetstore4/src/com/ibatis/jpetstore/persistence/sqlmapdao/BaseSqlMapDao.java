/**
 * User: Clinton Begin
 * Date: Jul 13, 2003
 * Time: 7:24:04 PM
 */
package com.ibatis.jpetstore.persistence.sqlmapdao;

import com.ibatis.common.util.PaginatedList;
import com.ibatis.dao.client.DaoException;
import com.ibatis.dao.client.DaoManager;
import com.ibatis.dao.client.template.SqlMapDaoTemplate;
import com.ibatis.sqlmap.client.SqlMapExecutor;

import java.sql.SQLException;
import java.util.List;

public class BaseSqlMapDao extends SqlMapDaoTemplate {

  protected static final int PAGE_SIZE = 4;

  public BaseSqlMapDao(DaoManager daoManager) {
    super(daoManager);
  }

  /**
   * Simple convenience method to wrap the SqlMap method of the same name.
   * Wraps the exception with a DaoException to isolate the SqlMap framework.
   *
   * @param statementName
   * @param parameterObject
   * @return
   * @
   */
  protected List executeQueryForList(String statementName, Object parameterObject) {
    SqlMapExecutor sqlMap = getSqlMapExecutor();
    try {
      return sqlMap.queryForList(statementName, parameterObject);
    } catch (SQLException e) {
      throw new DaoException("Error executing query for list.  Cause: " + e, e);
    }
  }

  /**
   * Simple convenience method to wrap the SqlMap method of the same name.
   * Wraps the exception with a DaoException to isolate the SqlMap framework.
   *
   * @param statementName
   * @param parameterObject
   * @return
   * @
   */
  protected List executeQueryForList(String statementName, Object parameterObject, int skipResults, int maxResults) {
    SqlMapExecutor sqlMap = getSqlMapExecutor();
    try {
      return sqlMap.queryForList(statementName, parameterObject, skipResults, maxResults);
    } catch (SQLException e) {
      throw new DaoException("Error executing query for list.  Cause: " + e, e);
    }
  }

  /**
   * Simple convenience method to wrap the SqlMap method of the same name.
   * Wraps the exception with a DaoException to isolate the SqlMap framework.
   *
   * @param statementName
   * @param parameterObject
   * @return
   * @
   */
  protected PaginatedList executeQueryForPaginatedList(String statementName, Object parameterObject, int pageSize) {
    SqlMapExecutor sqlMap = getSqlMapExecutor();
    try {
      return sqlMap.queryForPaginatedList(statementName, parameterObject, pageSize);
    } catch (SQLException e) {
      throw new DaoException("Error executing query for paginated list.  Cause: " + e, e);
    }
  }

  /**
   * Simple convenience method to wrap the SqlMap method of the same name.
   * Wraps the exception with a DaoException to isolate the SqlMap framework.
   *
   * @param statementName
   * @param parameterObject
   * @return
   * @
   */
  protected Object executeQueryForObject(String statementName, Object parameterObject) {
    SqlMapExecutor sqlMap = getSqlMapExecutor();
    try {
      return sqlMap.queryForObject(statementName, parameterObject);
    } catch (SQLException e) {
      throw new DaoException("Error executing query for object.  Cause: " + e, e);
    }
  }

  /**
   * Simple convenience method to wrap the SqlMap method of the same name.
   * Wraps the exception with a DaoException to isolate the SqlMap framework.
   *
   * @param statementName
   * @param parameterObject
   * @return
   * @
   */
  protected int executeUpdate(String statementName, Object parameterObject) {
    SqlMapExecutor sqlMap = getSqlMapExecutor();
    try {
      return sqlMap.update(statementName, parameterObject);
    } catch (SQLException e) {
      throw new DaoException("Error executing update.  Cause: " + e, e);
    }
  }


}
