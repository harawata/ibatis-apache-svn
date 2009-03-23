package org.apache.ibatis.parser;

import org.apache.ibatis.mapping.Configuration;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.mapping.ResultSetType;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.apache.ibatis.type.TypeHandlerRegistry;

public class BaseParser {
  protected final Configuration configuration;
  protected final TypeAliasRegistry typeAliasRegistry;
  protected final TypeHandlerRegistry typeHandlerRegistry;

  public BaseParser(Configuration configuration) {
    this.configuration = configuration;
    this.typeAliasRegistry = this.configuration.getTypeAliasRegistry();
    this.typeHandlerRegistry = this.configuration.getTypeHandlerRegistry();
  }

  public Configuration getConfiguration() {
    return configuration;
  }

  protected String stringValueOf(String value, String defaultValue) {
    return value == null ? defaultValue : value;
  }

  protected Boolean booleanValueOf(String value, Boolean defaultValue) {
    return value == null ? defaultValue : Boolean.valueOf(value);
  }

  protected Integer integerValueOf(String value, Integer defaultValue) {
    return value == null ? defaultValue : Integer.valueOf(value);
  }

  protected JdbcType resolveJdbcType(String alias) {
    if (alias == null) return null;
    try {
      return JdbcType.valueOf(resolveAlias(alias));
    } catch (IllegalArgumentException e) {
      throw new ParserException("Error resolving JdbcType. Cause: " + e, e);
    }
  }

  protected ResultSetType resolveResultSetType(String alias) {
    if (alias == null) return null;
    try {
      return ResultSetType.valueOf(resolveAlias(alias));
    } catch (IllegalArgumentException e) {
      throw new ParserException("Error resolving ResultSetType. Cause: " + e, e);
    }
  }

  protected ParameterMode resolveParameterMode(String alias) {
    if (alias == null) return null;
    try {
      return ParameterMode.valueOf(resolveAlias(alias));
    } catch (IllegalArgumentException e) {
      throw new ParserException("Error resolving ParameterMode. Cause: " + e, e);
    }
  }

  protected Class resolveClass(String alias) {
    if (alias == null) return null;
    try {
      return Class.forName(resolveAlias(alias));
    } catch (ClassNotFoundException e) {
      throw new ParserException("Error resolving class . Cause: " + e, e);
    }
  }

  protected Object resolveInstance(String alias) {
    if (alias == null) return null;
    try {
      Class type = resolveClass(alias);
      return type.newInstance();
    } catch (Exception e) {
      throw new ParserException("Error instantiating class. Cause: " + e, e);
    }
  }

  protected Object resolveInstance(Class type) {
    if (type == null) return null;
    try {
      return type.newInstance();
    } catch (Exception e) {
      throw new ParserException("Error instantiating class. Cause: " + e, e);
    }
  }

  protected String resolveAlias(String alias) {
    return typeAliasRegistry.resolveAlias(alias);
  }
}
