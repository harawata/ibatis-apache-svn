/*
 *  Copyright 2004 Clinton Begin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.ibatis.sqlmap.engine.mapping.result;

import com.ibatis.sqlmap.engine.type.JdbcTypeRegistry;
import com.ibatis.sqlmap.engine.type.TypeHandler;

public class BasicResultMapping implements ResultMapping {

  private String propertyName;
  private String columnName;
  private int columnIndex;
  private TypeHandler typeHandler;
  private int jdbcType;
  private String jdbcTypeName;
  private String nullValue;
  private String statementName;
  private Class javaType;

  private String errorString;

  public String getPropertyName() {
    return propertyName;
  }

  public void setPropertyName(String propertyName) {
    this.errorString = "Check the result mapping for the '" + propertyName + "' property.";
    this.propertyName = propertyName;
  }

  public String getErrorString() {
    return errorString;
  }

  public String getColumnName() {
    return columnName;
  }

  public void setColumnName(String columnName) {
    this.columnName = columnName;
  }

  public int getColumnIndex() {
    return columnIndex;
  }

  public void setColumnIndex(int columnIndex) {
    this.columnIndex = columnIndex;
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

  public String getNullValue() {
    return nullValue;
  }

  public void setNullValue(String nullValue) {
    this.nullValue = nullValue;
  }

  public String getStatementName() {
    return statementName;
  }

  public void setStatementName(String statementName) {
    this.statementName = statementName;
  }

}
