package org.apache.ibatis.builder;

import org.apache.ibatis.mapping.*;
import org.apache.ibatis.xml.*;
import org.apache.ibatis.type.TypeHandler;

import java.util.*;

public class SqlSourceParser extends BaseParser {

  public SqlSourceParser(MonarchConfiguration configuration) {
    this.configuration = configuration;
    this.typeAliasRegistry = configuration.getTypeAliasRegistry();
    this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
  }

  public SqlSource parse(NodeletContext context) {
    ParameterMappingTokenHandler handler = new ParameterMappingTokenHandler();
    GenericTokenParser parser = new GenericTokenParser("#{", "}", handler);
    String sql = parser.parse(context.getStringBody());
    return new InlineSqlSource(sql, handler.getParameterMappings());
  }

  private class ParameterMappingTokenHandler implements GenericTokenParser.TokenHandler {

    private List<ParameterMapping> parameterMappings = new ArrayList<ParameterMapping>();

    public List<ParameterMapping> getParameterMappings() {
      return parameterMappings;
    }

    public String handleToken(String content) {
      parameterMappings.add(buildParameterMapping(content));
      return "?";
    }
    private ParameterMapping buildParameterMapping(String content) {
      StringTokenizer parameterMappingParts = new StringTokenizer(content,", ");
      String property = parameterMappingParts.nextToken();
      ParameterMapping.Builder builder = new ParameterMapping.Builder(configuration, property, Object.class);
      while (parameterMappingParts.hasMoreTokens()) {
        String attribute = parameterMappingParts.nextToken();
        StringTokenizer attributeParts = new StringTokenizer(attribute,"=");
        if (attributeParts.countTokens() == 2) {
          String name = attributeParts.nextToken();
          String value = attributeParts.nextToken();
          if ("javaType".equals(name)) {
            builder.javaType(resolveClass(value));
          } else if ("jdbcType".equals(name)) {
            builder.jdbcType(resolveJdbcType(value));
          } else if ("mode".equals(name)) {
            builder.mode(resolveParameterMode(value));
          } else if ("numericScale".equals(name)) {
            builder.numericScale(Integer.valueOf(value));
          } else if ("resultMap".equals(name)) {
            builder.resultMapId(value);
          } else if ("typeHandler".equals(name)) {
            builder.typeHandler((TypeHandler) resolveInstance(value));
          }
        } else {
          throw new BuilderException("Improper inline parameter map format.  Should be: #{propName,attr1=val1,attr2=val2}");
        }
      }
      return builder.build();
    }
  }

}
