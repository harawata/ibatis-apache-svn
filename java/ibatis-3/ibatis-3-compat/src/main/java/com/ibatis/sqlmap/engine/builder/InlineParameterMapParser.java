package com.ibatis.sqlmap.engine.builder;

import com.ibatis.sqlmap.client.SqlMapException;
import com.ibatis.sqlmap.engine.mapping.sql.SqlText;
import org.apache.ibatis.mapping.Configuration;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.type.*;

import java.util.*;

public class InlineParameterMapParser {

  private TypeHandlerRegistry typeHandlerRegistry;
  private TypeAliasRegistry typeAliasRegistry;

  private static final String PARAMETER_TOKEN = "#";
  private static final String PARAM_DELIM = ":";

  public InlineParameterMapParser(Configuration configuration) {
    this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
    this.typeAliasRegistry = configuration.getTypeAliasRegistry();
  }

  public SqlText parseInlineParameterMap(String sqlStatement) {
    return parseInlineParameterMap(sqlStatement, null);
  }

  public SqlText parseInlineParameterMap(String sqlStatement, Class parameterClass) {

    String newSql;

    List mappingList = new ArrayList();

    StringTokenizer parser = new StringTokenizer(sqlStatement, PARAMETER_TOKEN, true);
    StringBuffer newSqlBuffer = new StringBuffer();

    String token;
    String lastToken = null;
    while (parser.hasMoreTokens()) {
      token = parser.nextToken();
      if (PARAMETER_TOKEN.equals(lastToken)) {
        if (PARAMETER_TOKEN.equals(token)) {
          newSqlBuffer.append(PARAMETER_TOKEN);
          token = null;
        } else {
          ParameterMapping mapping;
          if (token.indexOf(PARAM_DELIM) > -1) {
            mapping = oldParseMapping(token, parameterClass, typeHandlerRegistry);
          } else {
            mapping = newParseMapping(token, parameterClass);
          }

          mappingList.add(mapping);
          newSqlBuffer.append("?");
          token = parser.nextToken();
          if (!PARAMETER_TOKEN.equals(token)) {
            throw new SqlMapException("Unterminated inline parameter in mapped statement (" + "statement.getId()" + ").");
          }
          token = null;
        }
      } else {
        if (!PARAMETER_TOKEN.equals(token)) {
          newSqlBuffer.append(token);
        }
      }

      lastToken = token;
    }
    newSql = newSqlBuffer.toString();

    SqlText sqlText = new SqlText();
    sqlText.setText(newSql);
    sqlText.setParameterMappings(mappingList);
    return sqlText;
  }

  private ParameterMapping newParseMapping(String token, Class parameterClass) {

    // #propertyName,javaType=string,jdbcType=VARCHAR,mode=IN,nullValue=N/A,handler=string,numericScale=2#

    StringTokenizer paramParser = new StringTokenizer(token, "=, ", false);
    String propertyName = paramParser.nextToken();
    TypeHandler typeHandler = null;
    Class javaType = null;
    JdbcType jdbcType = null;
    ParameterMode parameterMode = null;
    Integer numericScale = null;

    while (paramParser.hasMoreTokens()) {
      String field = paramParser.nextToken();
      if (paramParser.hasMoreTokens()) {
        String value = paramParser.nextToken();
        if ("javaType".equals(field)) {
          value = typeAliasRegistry.resolveAlias(value);
          try {
            javaType = Class.forName(value);
          } catch (ClassNotFoundException e) {
            throw new SqlMapException("Error loading javaType class");
          }
        } else if ("jdbcType".equals(field)) {
          jdbcType = JdbcType.valueOf(value);
        } else if ("mode".equals(field)) {
          parameterMode = ParameterMode.valueOf(value);
        } else if ("nullValue".equals(field)) {
          throw new UnsupportedOperationException("iBATIS 3 does not support null value substitution.");
        } else if ("handler".equals(field)) {
          try {
            value = typeAliasRegistry.resolveAlias(value);
            Object impl = Resources.classForName(value).newInstance();
            typeHandler = ((TypeHandler) impl);
          } catch (Exception e) {
            throw new SqlMapException("Error loading class specified by handler field in " + token + ".  Cause: " + e, e);
          }
        } else if ("numericScale".equals(field)) {
          try {
            numericScale = Integer.valueOf(value);
            if (numericScale < 0) {
              throw new SqlMapException("Value specified for numericScale must be greater than or equal to zero");
            }
          } catch (NumberFormatException e) {
            throw new SqlMapException("Value specified for numericScale is not a valid Integer");
          }
        } else {
          throw new SqlMapException("Unrecognized parameter mapping field: '" + field + "' in " + token);
        }
      } else {
        throw new SqlMapException("Incorrect inline parameter map format (missmatched name=value pairs): " + token);
      }
    }

    if (typeHandler == null) {
      if (parameterClass == null) {
        typeHandler = typeHandlerRegistry.getUnkownTypeHandler();
      } else {
        String javaTypeString = javaType == null ? null : javaType.getName();
        typeHandler = resolveTypeHandler(parameterClass, propertyName, javaTypeString, jdbcType);
      }
    }

    if (propertyName != null && propertyName.startsWith("[")) {
      propertyName = "_collection" + propertyName;
    }

    ParameterMapping.Builder mapping = new ParameterMapping.Builder(propertyName, typeHandler);
    mapping.javaType(javaType);
    mapping.jdbcType(jdbcType);
    mapping.mode(parameterMode);
    mapping.numericScale(numericScale);

    return mapping.build();
  }

  private ParameterMapping oldParseMapping(String token, Class parameterClass, TypeHandlerRegistry typeHandlerRegistry) {
    if (token.indexOf(PARAM_DELIM) > -1) {
      StringTokenizer paramParser = new StringTokenizer(token, PARAM_DELIM, true);
      int n1 = paramParser.countTokens();
      if (n1 == 3) {
        String name = paramParser.nextToken();
        paramParser.nextToken(); //ignore ":"
        String type = paramParser.nextToken();
        TypeHandler handler;
        if (parameterClass == null) {
          handler = typeHandlerRegistry.getUnkownTypeHandler();
        } else {
          handler = resolveTypeHandler(parameterClass, name, null, JdbcType.valueOf(type));
        }
        ParameterMapping.Builder mapping = new ParameterMapping.Builder(name, handler);
        mapping.jdbcType(JdbcType.valueOf(type));
        return mapping.build();
      } else if (n1 >= 5) {
        throw new UnsupportedOperationException("iBATIS 3 does not support null value substitution.");
      } else {
        throw new SqlMapException("Incorrect inline parameter map format: " + token);
      }
    } else {
      TypeHandler handler;
      if (parameterClass == null) {
        handler = typeHandlerRegistry.getUnkownTypeHandler();
      } else {
        handler = resolveTypeHandler(parameterClass, token, null, null);
      }
      ParameterMapping.Builder mapping = new ParameterMapping.Builder(token, handler);
      return mapping.build();
    }
  }

  private TypeHandler resolveTypeHandler(Class clazz, String propertyName, String javaType, JdbcType jdbcType) {
    TypeHandler handler;
    if (clazz == null) {
      // Unknown
      handler = typeHandlerRegistry.getUnkownTypeHandler();
    } else if (java.util.Map.class.isAssignableFrom(clazz)) {
      // Map
      if (javaType == null) {
        handler = typeHandlerRegistry.getUnkownTypeHandler(); //BUG 1012591 - typeHandlerRegistry.getTypeHandler(java.lang.Object.class, jdbcType);
      } else {
        try {
          javaType = typeAliasRegistry.resolveAlias(javaType);
          Class javaClass = Resources.classForName(javaType);
          handler = typeHandlerRegistry.getTypeHandler(javaClass, jdbcType);
        } catch (Exception e) {
          throw new SqlMapException("Error.  Could not set TypeHandler.  Cause: " + e, e);
        }
      }
    } else if (typeHandlerRegistry.getTypeHandler(clazz, jdbcType) != null) {
      // Primitive
      handler = typeHandlerRegistry.getTypeHandler(clazz, jdbcType);
    } else {
      // JavaBean
      if (javaType == null) {

        Class type = MetaClass.forClass(clazz).getGetterType(propertyName);
        handler = typeHandlerRegistry.getTypeHandler(type, jdbcType);

      } else {
        try {
          javaType = typeAliasRegistry.resolveAlias(javaType);
          Class javaClass = Resources.classForName(javaType);
          handler = typeHandlerRegistry.getTypeHandler(javaClass, jdbcType);
        } catch (Exception e) {
          throw new SqlMapException("Error.  Could not set TypeHandler.  Cause: " + e, e);
        }
      }
    }
    return handler;
  }


}
