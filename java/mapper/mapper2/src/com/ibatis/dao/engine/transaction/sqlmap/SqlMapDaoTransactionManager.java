package com.ibatis.dao.engine.transaction.sqlmap;

import com.ibatis.dao.engine.transaction.DaoTransactionManager;
import com.ibatis.dao.client.DaoTransaction;
import com.ibatis.dao.client.DaoException;
import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;

import java.util.Map;
import java.io.Reader;
import java.io.IOException;

/**
 *
 *
 * <p>
 * Date: Jan 27, 2004 10:48:21 PM
 * @author Clinton Begin
 */
public class SqlMapDaoTransactionManager implements DaoTransactionManager {

  private SqlMapClient client;

  public void configure(Map properties) {
    try {
      Reader reader = null;
      if (properties.containsKey("SqlMapConfigURL")) {
        reader = Resources.getUrlAsReader((String)properties.get("SqlMapConfigURL"));
      } else if (properties.containsKey("SqlMapConfigResource")) {
        reader = Resources.getResourceAsReader((String)properties.get("SqlMapConfigResource"));
      } else {
        throw new DaoException ("SQLMAP transaction manager requires either 'SqlMapConfigURL' or 'SqlMapConfigResource' to be specified as a property.");
      }
      client = SqlMapClientBuilder.buildSqlMapClient(reader);
    } catch (IOException e) {
      throw new DaoException("Error configuring SQL Map.  Cause: " + e);
    }
  }

  public DaoTransaction startTransaction() {
    return new SqlMapDaoTransaction(client);
  }

  public void commitTransaction(DaoTransaction trans) {
    ((SqlMapDaoTransaction)trans).commit();
  }

  public void rollbackTransaction(DaoTransaction trans) {
    ((SqlMapDaoTransaction)trans).rollback();
  }

}
