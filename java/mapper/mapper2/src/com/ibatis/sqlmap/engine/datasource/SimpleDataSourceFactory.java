/*
 * User: Clinton Begin
 * Date: Sep 23, 2002
 * Time: 8:56:22 PM
 */
package com.ibatis.sqlmap.engine.datasource;

import com.ibatis.common.jdbc.SimpleDataSource;

import javax.sql.DataSource;
import java.util.Map;

/**
 * DataSourceFactory implementation for the iBATIS SimpleDataSource
 */
public class SimpleDataSourceFactory implements DataSourceFactory {

  private DataSource dataSource;

  public void initialize(Map map) {
    dataSource = new SimpleDataSource(map);
  }

  public DataSource getDataSource() {
    return dataSource;
  }

}
