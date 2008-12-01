package org.apache.ibatis.builder;

import org.apache.ibatis.mapping.Configuration;
import org.apache.ibatis.api.defaults.DefaultSqlSessionFactory;
import org.apache.ibatis.api.SqlSessionFactory;

import java.io.Reader;
import java.util.Properties;

public class SqlMapperBuilder {

  public SqlSessionFactory build(Reader reader) {
    return build(reader,null,null);
  }

  public SqlSessionFactory build(Reader reader, String environment) {
    return build(reader,environment,null);
  }

  public SqlSessionFactory build(Reader reader, String environment, Properties props) {
    MapperConfigParser parser = new MapperConfigParser(reader,environment,props);
    Configuration config = parser.parse();
    return new DefaultSqlSessionFactory(config);
  }
  
}