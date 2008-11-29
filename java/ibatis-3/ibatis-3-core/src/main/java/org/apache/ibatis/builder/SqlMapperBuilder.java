package org.apache.ibatis.builder;

import org.apache.ibatis.mapping.Configuration;
import org.apache.ibatis.api.defaults.DefaultSqlMapper;
import org.apache.ibatis.api.SqlMapper;

import java.io.Reader;
import java.util.Properties;

public class SqlMapperBuilder {

  public SqlMapper build(Reader reader) {
    return build(reader,null,null);
  }

  public SqlMapper build(Reader reader, String environment) {
    return build(reader,environment,null);
  }

  public SqlMapper build(Reader reader, String environment, Properties props) {
    MapperConfigParser parser = new MapperConfigParser(reader,environment,props);
    Configuration config = parser.parse();
    return new DefaultSqlMapper(config);
  }
  
}
