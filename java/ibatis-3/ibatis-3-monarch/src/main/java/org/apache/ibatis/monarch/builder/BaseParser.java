package org.apache.ibatis.monarch.builder;

import org.apache.ibatis.type.*;
import org.apache.ibatis.xml.NodeletParser;
import org.apache.ibatis.mapping.Configuration;
import org.apache.ibatis.mapping.ResultSetType;
import org.apache.ibatis.mapping.ParameterMode;

import java.io.Reader;
import java.util.Properties;

public class BaseParser {

  protected Reader reader;
  protected NodeletParser parser;
  protected MonarchConfiguration configuration;
  protected TypeAliasRegistry typeAliasRegistry;
  protected TypeHandlerRegistry typeHandlerRegistry;

  public void parse() {
    assert reader != null;
    assert parser != null;
    assert configuration != null;
    assert typeAliasRegistry != null;
    assert typeHandlerRegistry != null;
    parser.parse(reader);
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
      throw new RuntimeException("Error resolving JdbcType. Cause: " + e, e);
    }
  }

  protected ResultSetType resolveResultSetType(String alias) {
    if (alias == null) return null;
    try {
      return ResultSetType.valueOf(resolveAlias(alias));
    } catch (IllegalArgumentException e) {
      throw new RuntimeException("Error resolving ResultSetType. Cause: " + e, e);
    }
  }

  protected ParameterMode resolveParameterMode(String alias) {
    if (alias == null) return null;
    try {
      return ParameterMode.valueOf(resolveAlias(alias));
    } catch (IllegalArgumentException e) {
      throw new RuntimeException("Error resolving ParameterMode. Cause: " + e, e);
    }
  }

  protected Class resolveClass(String alias) {
    if (alias == null) return null;
    try {
      return Class.forName(resolveAlias(alias));
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Error resolving class . Cause: " + e, e);
    }
  }

  protected Object resolveInstance(String alias) {
    if (alias == null) return null;
    try {
      Class type = resolveClass(alias);
      return type.newInstance();
    } catch (Exception e) {
      throw new RuntimeException("Error instantiating class. Cause: " + e, e);
    }
  }

  protected String resolveAlias(String alias) {
    return typeAliasRegistry.resolveAlias(alias);
  }

}
