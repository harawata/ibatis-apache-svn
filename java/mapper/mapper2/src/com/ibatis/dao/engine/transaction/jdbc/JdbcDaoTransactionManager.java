package com.ibatis.dao.engine.transaction.jdbc;

import com.ibatis.common.jdbc.SimpleDataSource;
import com.ibatis.dao.client.DaoException;
import com.ibatis.dao.client.DaoTransaction;
import com.ibatis.dao.engine.transaction.DaoTransactionManager;
import org.apache.commons.dbcp.BasicDataSource;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * TODO: Centralize configuration of DBCP datasource
 * <p/>
 * <p/>
 * Date: Jan 27, 2004 10:48:58 PM
 *
 * @author Clinton Begin
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
    try {
      String driver = (String) properties.get("JDBC.Driver");
      String url = (String) properties.get("JDBC.ConnectionURL");
      String username = (String) properties.get("JDBC.Username");
      String password = (String) properties.get("JDBC.Password");
      String validationQuery = (String) properties.get("Pool.ValidationQuery");
      String maxActive = (String) properties.get("Pool.MaximumActiveConnections");
      String maxIdle = (String) properties.get("Pool.MaximumIdleConnections");
      String maxWait = (String) properties.get("Pool.MaximumWait");
      String removeAbandoned = (String) properties.get("Pool.RemoveAbandoned");
      String removeAbandonedTimeout = (String) properties.get("Pool.RemoveAbandonedTimeout");
      String logAbandoned = (String) properties.get("Pool.LogAbandoned");

      BasicDataSource basicDataSource = new BasicDataSource();

      basicDataSource.setUrl(url);
      basicDataSource.setDriverClassName(driver);
      basicDataSource.setUsername(username);
      basicDataSource.setPassword(password);

      if (notEmpty(validationQuery)) {
        basicDataSource.setValidationQuery(validationQuery);
      }

      if (notEmpty(maxActive)) {
        basicDataSource.setMaxActive(Integer.parseInt(maxActive));
      }

      if (notEmpty(maxIdle)) {
        basicDataSource.setMaxIdle(Integer.parseInt(maxIdle));
      }

      if (notEmpty(maxWait)) {
        basicDataSource.setMaxWait(Integer.parseInt(maxWait));
      }

      if (notEmpty(removeAbandonedTimeout)) {
        basicDataSource.setRemoveAbandonedTimeout(Integer.parseInt(removeAbandonedTimeout));
      }

      if (notEmpty(removeAbandoned)) {
        basicDataSource.setRemoveAbandoned("true".equals(removeAbandoned));
      }

      if (notEmpty(logAbandoned)) {
        basicDataSource.setLogAbandoned("true".equals(logAbandoned));
      }

      dataSource = basicDataSource;

    } catch (Exception e) {
      throw new DaoException("Error initializing DBCP.  Cause: " + e, e);
    }
  }

  private void configureJndi(Map properties) {
    try {
      Properties contextProps = getContextProperties(properties);
      InitialContext initCtx = null;
      if (contextProps == null) {
        initCtx = new InitialContext();
      } else {
        initCtx = new InitialContext(contextProps);
      }
      dataSource = (DataSource) initCtx.lookup((String) properties.get("DataSource"));
    } catch (NamingException e) {
      throw new DaoException("There was an error configuring the DataSource from JNDI.  Cause: " + e, e);
    }
  }

  private boolean notEmpty(String s) {
    return s != null && s.length() > 0;
  }

  public void commitTransaction(DaoTransaction trans) {
    ((JdbcDaoTransaction) trans).commit();
  }

  public void rollbackTransaction(DaoTransaction trans) {
    ((JdbcDaoTransaction) trans).rollback();
  }

  private static Properties getContextProperties(Map allProps) {
    final String PREFIX = "context.";
    Properties contextProperties = null;
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
