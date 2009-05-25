package org.apache.ibatis.builder;

import org.apache.ibatis.mapping.*;
import org.apache.ibatis.parsing.GenericTokenParser;
import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.type.TypeHandler;

import java.util.*;

public class SqlSourceBuilder extends BaseBuilder {

  public SqlSourceBuilder(Configuration configuration) {
    super(configuration);
  }

  public SqlSource parse(String originalSql, Class parameterType) {
    ParameterMappingTokenHandler handler = new ParameterMappingTokenHandler(configuration, parameterType);
    GenericTokenParser parser = new GenericTokenParser("#{", "}", handler);
    String sql = parser.parse(originalSql);
    return new StaticSqlSource(sql, handler.getParameterMappings());
  }

  private static class ParameterMappingTokenHandler extends BaseBuilder implements GenericTokenParser.TokenHandler {

    private List<ParameterMapping> parameterMappings = new ArrayList<ParameterMapping>();
    private Class parameterType;

    public ParameterMappingTokenHandler(Configuration configuration,Class parameterType) {
      super(configuration);
      this.parameterType = parameterType;
    }

    public List<ParameterMapping> getParameterMappings() {
      return parameterMappings;
    }

    public String handleToken(String content) {
      parameterMappings.add(buildParameterMapping(content));
      return "?";
    }

    private ParameterMapping buildParameterMapping(String content) {
      StringTokenizer parameterMappingParts = new StringTokenizer(content, ", ");
      String property = parameterMappingParts.nextToken();
      Class propertyType;
      if (typeHandlerRegistry.hasTypeHandler(parameterType)) {
        propertyType = parameterType;
      } else if (Map.class.isAssignableFrom(parameterType)) {
        propertyType = Object.class;
      } else {
        propertyType = MetaClass.forClass(parameterType).getGetterType(property);
      }
      ParameterMapping.Builder builder = new ParameterMapping.Builder(configuration, property, propertyType);
      while (parameterMappingParts.hasMoreTokens()) {
        String attribute = parameterMappingParts.nextToken();
        StringTokenizer attributeParts = new StringTokenizer(attribute, "=");
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
          throw new BulderException("Improper inline parameter map format.  Should be: #{propName,attr1=val1,attr2=val2}");
        }
      }
      return builder.build();
    }
  }


}
