package org.apache.ibatis.datasource;

import org.apache.ibatis.jdbc.PooledDataSource;

public class PooledDataSourceFactory extends UnpooledDataSourceFactory {

  public PooledDataSourceFactory() {
    this.dataSource = new PooledDataSource();
  }

}
