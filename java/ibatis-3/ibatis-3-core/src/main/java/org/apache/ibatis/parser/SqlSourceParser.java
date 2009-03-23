package org.apache.ibatis.parser;

import org.apache.ibatis.mapping.Configuration;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.xml.GenericTokenParser;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class SqlSourceParser extends BaseParser {

  public SqlSourceParser(Configuration configuration) {
    super(configuration);
  }

  public SqlSource parse(String originalSql) {
    ParameterMappingTokenHandler handler = new ParameterMappingTokenHandler(configuration);
    GenericTokenParser parser = new GenericTokenParser("#{", "}", handler);
    String sql = parser.parse(originalSql);
    return new StaticSqlSource(sql, handler.getParameterMappings());
  }


}
