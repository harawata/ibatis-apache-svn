package org.apache.ibatis.mapping;

import org.apache.ibatis.type.*;

public class ParameterMapping {

  private String property;
  private ParameterMode mode;
  private Class javaType = Object.class;
  private JdbcType jdbcType;
  private Integer numericScale;
  private TypeHandler typeHandler;
  private String resultMapId;

  private ParameterMapping() {
  }

  public static class Builder {
    private ParameterMapping parameterMapping = new ParameterMapping();

    public Builder(String property, TypeHandler typeHandler) {
      parameterMapping.property = property;
      parameterMapping.typeHandler = typeHandler;
      parameterMapping.mode = ParameterMode.IN;
    }

    public Builder mode(ParameterMode mode) {
      parameterMapping.mode = mode;
      return this;
    }

    public Builder javaType(Class javaType) {
      parameterMapping.javaType = javaType;
      return this;
    }

    public Builder jdbcType(JdbcType jdbcType) {
      parameterMapping.jdbcType = jdbcType;
      return this;
    }

    public Builder numericScale(Integer numericScale) {
      parameterMapping.numericScale = numericScale;
      return this;
    }

    public Builder resultMapId(String resultMapId) {
      parameterMapping.resultMapId = resultMapId;
      return this;
    }

    public Builder typeHandler(TypeHandler typeHandler) {
      parameterMapping.typeHandler = typeHandler;
      return this;
    }

    public ParameterMapping build() {
      return parameterMapping;
    }

  }

  public String getProperty() {
    return property;
  }

  public ParameterMode getMode() {
    return mode;
  }

  public Class getJavaType() {
    return javaType;
  }

  public JdbcType getJdbcType() {
    return jdbcType;
  }

  public Integer getNumericScale() {
    return numericScale;
  }

  public TypeHandler getTypeHandler() {
    return typeHandler;
  }

  public String getResultMapId() {
    return resultMapId;
  }

}
