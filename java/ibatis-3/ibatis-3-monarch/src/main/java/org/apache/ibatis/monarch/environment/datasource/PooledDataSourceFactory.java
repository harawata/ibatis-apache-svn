package org.apache.ibatis.monarch.environment.datasource;

import org.apache.ibatis.jdbc.PooledDataSource;

public class PooledDataSourceFactory extends UnpooledDataSourceFactory {

  public PooledDataSourceFactory() {
    this.dataSource = new PooledDataSource();
  }

}
