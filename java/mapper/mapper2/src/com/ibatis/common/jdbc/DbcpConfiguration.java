package com.ibatis.common.jdbc;

import com.ibatis.common.beans.BeanProbe;
import com.ibatis.common.exception.NestedRuntimeException;
import org.apache.commons.dbcp.BasicDataSource;

import javax.sql.DataSource;
import java.util.Map;

/**
 * <p/>
 * Date: Apr 19, 2004 7:03:58 PM
 * 
 * @author Clinton Begin
 */
public class DbcpConfiguration {

  private DataSource dataSource;

  public DbcpConfiguration(Map properties) {
    try {

      dataSource = legacyDbcpConfiguration(properties);
      if (dataSource == null) {
        dataSource = newDbcpConfiguration(properties);
      }

    } catch (Exception e) {
      throw new NestedRuntimeException("Error initializing DbcpDataSourceFactory.  Cause: " + e, e);
    }
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  private BasicDataSource newDbcpConfiguration(Map map) {
    BasicDataSource basicDataSource = new BasicDataSource();
    String[] props = BeanProbe.getWriteablePropertyNames(basicDataSource);
    for (int i = 0; i < props.length; i++) {
      String propertyName = props[i];
      if (map.containsKey(propertyName)) {
        String value = (String) map.get(propertyName);
        Object convertedValue = convertValue(basicDataSource, propertyName, value);
        BeanProbe.setObject(basicDataSource, propertyName, convertedValue);
      }
    }
    return basicDataSource;
  }

  private Object convertValue(Object object, String propertyName, String value) {
    Object convertedValue = value;
    Class targetType = BeanProbe.getPropertyTypeForSetter(object, propertyName);
    if (targetType == Integer.class || targetType == int.class) {
      convertedValue = Integer.valueOf(value);
    } else if (targetType == Long.class || targetType == long.class) {
      convertedValue = Long.valueOf(value);
    } else if (targetType == Boolean.class || targetType == boolean.class) {
      convertedValue = Boolean.valueOf(value);
    }
    return convertedValue;
  }

  private BasicDataSource legacyDbcpConfiguration(Map map) {
    BasicDataSource basicDataSource = null;
    if (map.containsKey("JDBC.Driver")) {
      basicDataSource = new BasicDataSource();
      String driver = (String) map.get("JDBC.Driver");
      String url = (String) map.get("JDBC.ConnectionURL");
      String username = (String) map.get("JDBC.Username");
      String password = (String) map.get("JDBC.Password");
      String validationQuery = (String) map.get("Pool.ValidationQuery");
      String maxActive = (String) map.get("Pool.MaximumActiveConnections");
      String maxIdle = (String) map.get("Pool.MaximumIdleConnections");
      String maxWait = (String) map.get("Pool.MaximumWait");

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

    }
    return basicDataSource;
  }

  private boolean notEmpty(String s) {
    return s != null && s.length() > 0;
  }


}
