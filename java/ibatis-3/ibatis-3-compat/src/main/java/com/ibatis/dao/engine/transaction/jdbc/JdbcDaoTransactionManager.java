package com.ibatis.dao.engine.transaction.jdbc;

import com.ibatis.common.jdbc.*;
import com.ibatis.dao.client.*;
import com.ibatis.dao.engine.transaction.DaoTransactionManager;

import javax.naming.*;
import javax.sql.DataSource;
import java.util.*;

/**
 * DaoTransactionManager implementation for JDBC
 */
public class JdbcDaoTransactionManager implements DaoTransactionManager {

  private DataSource dataSource;

  public void configure(Properties properties) {
    if (properties.containsKey("DataSource")) {
      String type = (String) properties.get("DataSource");
      if ("SIMPLE".equals(type)) {
        configureSimpleDataSource(properties);
      } else if ("DBCP".equals(type)) {
        configureDbcp(properties);
      } else if ("JNDI".equals(type)) {
        configureJndi(properties);
      } else {
        throw new DaoException("DAO Transaction Manager properties must include a value for 'DataSource' of SIMPLE, DBCP or JNDI.");
      }
    } else {
      throw new DaoException("DAO Transaction Manager properties must include a value for 'DataSource' of SIMPLE, DBCP or JNDI.");
    }
  }

  public DaoTransaction startTransaction() {
    return new JdbcDaoTransaction(dataSource);
  }

  private void configureSimpleDataSource(Map properties) {
    dataSource = new SimpleDataSource(properties);
  }

  private void configureDbcp(Map properties) {
    DbcpConfiguration dbcp = new DbcpConfiguration(properties);
    dataSource = dbcp.getDataSource();
  }

  private void configureJndi(Map properties) {
    try {
      Hashtable contextProps = getContextProperties(properties);
      InitialContext initCtx = null;
      if (contextProps == null) {
        initCtx = new InitialContext();
      } else {
        initCtx = new InitialContext(contextProps);
      }
      dataSource = (DataSource) initCtx.lookup((String) properties.get("DBJndiContext"));
    } catch (NamingException e) {
      throw new DaoException("There was an error configuring the DataSource from JNDI.  Cause: " + e, e);
    }
  }

  public void commitTransaction(DaoTransaction trans) {
    ((JdbcDaoTransaction) trans).commit();
  }

  public void rollbackTransaction(DaoTransaction trans) {
    ((JdbcDaoTransaction) trans).rollback();
  }

  private static Hashtable getContextProperties(Map allProps) {
    final String PREFIX = "context.";
    Hashtable contextProperties = null;
    Iterator keys = allProps.keySet().iterator();
    while (keys.hasNext()) {
      String key = (String) keys.next();
      String value = (String) allProps.get(key);
      if (key.startsWith(PREFIX)) {
        if (contextProperties == null) {
          contextProperties = new Properties();
        }
        contextProperties.put(key.substring(PREFIX.length()), value);
      }
    }
    return contextProperties;
  }
}
