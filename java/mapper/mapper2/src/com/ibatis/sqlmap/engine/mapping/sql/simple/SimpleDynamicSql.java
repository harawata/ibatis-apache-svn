package com.ibatis.sqlmap.engine.mapping.sql.simple;

import com.ibatis.common.beans.Probe;
import com.ibatis.common.beans.ProbeFactory;
import com.ibatis.sqlmap.client.SqlMapException;
import com.ibatis.sqlmap.engine.mapping.parameter.ParameterMap;
import com.ibatis.sqlmap.engine.mapping.result.ResultMap;
import com.ibatis.sqlmap.engine.mapping.sql.Sql;
import com.ibatis.sqlmap.engine.scope.RequestScope;
import com.ibatis.sqlmap.engine.type.TypeHandlerFactory;

import java.util.StringTokenizer;

/**
 * User: Clinton Begin
 * Date: Sep 13, 2003
 * Time: 6:50:19 PM
 */
public class SimpleDynamicSql implements Sql {

  private static final Probe PROBE = ProbeFactory.getProbe();

  private static final String ELEMENT_TOKEN = "$";

  private String sqlStatement;

  public SimpleDynamicSql(String sqlStatement) {
    this.sqlStatement = sqlStatement.replace('\r', ' ').replace('\n', ' ');
  }

  public String getSql(RequestScope request, Object parameterObject) {
    return processDynamicElements(sqlStatement, parameterObject);
  }

  public ParameterMap getParameterMap(RequestScope request, Object parameterObject) {
    return request.getParameterMap();
  }

  public ResultMap getResultMap(RequestScope request, Object parameterObject) {
    return request.getResultMap();
  }

  public void cleanup(RequestScope request) {
  }

  public static boolean isSimpleDynamicSql(String sql) {
    return sql != null && sql.indexOf(ELEMENT_TOKEN) > -1;
  }

  private String processDynamicElements(String sql, Object parameterObject) {
    StringTokenizer parser = new StringTokenizer(sql, ELEMENT_TOKEN, true);
    StringBuffer newSql = new StringBuffer();

    String token = null;
    String lastToken = null;
    while (parser.hasMoreTokens()) {
      token = parser.nextToken();

      if (ELEMENT_TOKEN.equals(lastToken)) {
        if (ELEMENT_TOKEN.equals(token)) {
          newSql.append(ELEMENT_TOKEN);
          token = null;
        } else {

          Object value = null;
          if (parameterObject != null) {
            if (TypeHandlerFactory.hasTypeHandler(parameterObject.getClass())) {
              value = parameterObject;
            } else {
              value = PROBE.getObject(parameterObject, token);
            }
          }
          if (value != null) {
            newSql.append(String.valueOf(value));
          }

          token = parser.nextToken();
          if (!ELEMENT_TOKEN.equals(token)) {
            throw new SqlMapException("Unterminated dynamic element in sql (" + sql + ").");
          }
          token = null;
        }
      } else {
        if (!ELEMENT_TOKEN.equals(token)) {
          newSql.append(token);
        }
      }

      lastToken = token;
    }

    return newSql.toString();
  }


}

