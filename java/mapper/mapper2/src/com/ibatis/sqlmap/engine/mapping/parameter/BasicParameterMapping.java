package com.ibatis.sqlmap.engine.mapping.parameter;

import com.ibatis.sqlmap.engine.type.*;

public class BasicParameterMapping implements ParameterMapping {

  private static final String MODE_INOUT = "INOUT";
  private static final String MODE_OUT = "OUT";
  private static final String MODE_IN = "IN";

  private String propertyName;
  private TypeHandler typeHandler;
  private int jdbcType;
  private String jdbcTypeName;
  private String nullValue;
  private String mode;
  private boolean inputAllowed;
  private boolean outputAllowed;
  private Class javaType;

  private String errorString;

  public BasicParameterMapping() {
    mode="IN";
    inputAllowed = true;
    outputAllowed = false;
  }

  public String getNullValue() {
    return nullValue;
  }

  public void setNullValue(String nullValue) {
    this.nullValue = nullValue;
  }

  public String getPropertyName() {
    return propertyName;
  }

  public void setPropertyName(String propertyName) {
    this.errorString = "Check the parameter mapping for the '" + propertyName + "' property.";
    this.propertyName = propertyName;
  }

  public String getErrorString() {
    return errorString;
  }

  public TypeHandler getTypeHandler() {
    return typeHandler;
  }

  public void setTypeHandler(TypeHandler typeHandler) {
    this.typeHandler = typeHandler;
  }

  public Class getJavaType() {
    return javaType;
  }

  public void setJavaType(Class javaType) {
    this.javaType = javaType;
  }

  public int getJdbcType() {
    return jdbcType;
  }

  public String getJdbcTypeName() {
    return jdbcTypeName;
  }

  public void setJdbcTypeName(String jdbcTypeName) {
    this.jdbcTypeName = jdbcTypeName;
    this.jdbcType = JdbcTypeRegistry.getType(jdbcTypeName);
  }

  public String getMode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
    inputAllowed = MODE_IN.equals(mode) || MODE_INOUT.equals(mode);
    outputAllowed = MODE_OUT.equals(mode) || MODE_INOUT.equals(mode);
  }

  public boolean isInputAllowed() {
    return inputAllowed;
  }

  public boolean isOutputAllowed() {
    return outputAllowed;
  }

}
