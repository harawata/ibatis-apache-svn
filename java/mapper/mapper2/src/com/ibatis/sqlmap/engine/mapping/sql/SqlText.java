package com.ibatis.sqlmap.engine.mapping.sql;

import com.ibatis.sqlmap.engine.mapping.parameter.*;
import com.ibatis.sqlmap.engine.mapping.sql.*;

/**
 * User: Clinton Begin
 * Date: Nov 18, 2003
 * Time: 10:17:53 PM
 */
public class SqlText implements SqlChild {

  private String text;
  private boolean isWhiteSpace;

  private ParameterMapping[] parameterMappings;

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
    this.isWhiteSpace = text.trim().length() == 0;
  }

  public boolean isWhiteSpace() {
    return isWhiteSpace;
  }

  public ParameterMapping[] getParameterMappings() {
    return parameterMappings;
  }

  public void setParameterMappings(ParameterMapping[] parameterMappings) {
    this.parameterMappings = parameterMappings;
  }

}

