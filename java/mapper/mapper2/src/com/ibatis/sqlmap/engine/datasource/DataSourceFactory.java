/*
 * User: Clinton Begin
 * Date: Sep 21, 2002
 * Time: 1:00:10 PM
 */
package com.ibatis.sqlmap.engine.datasource;

import javax.sql.DataSource;
import java.util.Map;

public interface DataSourceFactory {

  public void initialize(Map map);

  public DataSource getDataSource();

}
