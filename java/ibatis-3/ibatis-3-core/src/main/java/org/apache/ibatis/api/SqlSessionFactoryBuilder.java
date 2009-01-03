package org.apache.ibatis.api;

import org.apache.ibatis.mapping.Configuration;
import org.apache.ibatis.api.defaults.DefaultSqlSessionFactory;
import org.apache.ibatis.api.exceptions.ExceptionFactory;
import org.apache.ibatis.parser.MapperConfigParser;

import java.io.Reader;
import java.util.Properties;

public class SqlSessionFactoryBuilder {

  public SqlSessionFactory build(Reader reader) {
    return build(reader, null, null);
  }

  public SqlSessionFactory build(Reader reader, String environment) {
    return build(reader, environment, null);
  }

  public SqlSessionFactory build(Reader reader, Properties properties) {
    return build(reader, null, properties);
  }

  public SqlSessionFactory build(Reader reader, String environment, Properties props) {
    try {
      MapperConfigParser parser = new MapperConfigParser(reader, environment, props);
      Configuration config = parser.parse();
      return new DefaultSqlSessionFactory(config);
    } catch (Exception e) {
      throw ExceptionFactory.wrapException("Error building SqlSession.", e);
    }
  }

}
