package com.ibatis.sqlmap.engine.mapping.sql.dynamic;

import com.ibatis.sqlmap.engine.mapping.parameter.*;
import com.ibatis.sqlmap.engine.mapping.result.*;
import com.ibatis.sqlmap.engine.mapping.sql.*;
import com.ibatis.sqlmap.engine.mapping.sql.simple.*;
import com.ibatis.sqlmap.engine.mapping.sql.dynamic.elements.*;
import com.ibatis.sqlmap.engine.mapping.statement.*;
import com.ibatis.sqlmap.engine.builder.xml.*;

import com.ibatis.sqlmap.engine.scope.*;

import java.util.*;
import java.io.*;

/**
 * User: Clinton Begin
 * Date: Sep 13, 2003
 * Time: 6:50:06 PM
 */
public class DynamicSql implements Sql, DynamicParent {

  private List children = new ArrayList();

  public String getSql(RequestScope request, Object parameterObject) {
    String sql = request.getDynamicSql();
    if (sql == null) {
      process(request, parameterObject);
      sql = request.getDynamicSql();
    }
    return sql;
  }

  public ParameterMap getParameterMap(RequestScope request, Object parameterObject) {
    ParameterMap map = request.getDynamicParameterMap();
    if (map == null) {
      process(request, parameterObject);
      map = request.getDynamicParameterMap();
    }
    return map;
  }

  public ResultMap getResultMap(RequestScope request, Object parameterObject) {
    return request.getResultMap();
  }

  public void cleanup(RequestScope request) {
    request.setDynamicSql(null);
    request.setDynamicParameterMap(null);
  }

  private void process(RequestScope request, Object parameterObject) {
    SqlTagContext ctx = new SqlTagContext();
    List localChildren = children;
    processBodyChildren(request, ctx, parameterObject, localChildren.iterator());

    BasicParameterMap map = new BasicParameterMap();
    map.setId(request.getStatement().getId() + "-InlineParameterMap");
    map.setParameterClass(((GeneralStatement) request.getStatement()).getParameterClass());
    map.setParameterMappingList(ctx.getParameterMappings());

    String dynSql = ctx.getBodyText();

    // Processes $substitutions$ after DynamicSql
    if (SimpleDynamicSql.isSimpleDynamicSql(dynSql)) {
      dynSql = new SimpleDynamicSql(dynSql).getSql(request, parameterObject);
    }

    request.setDynamicSql(dynSql);
    request.setDynamicParameterMap(map);
  }

  private void processBodyChildren(RequestScope request, SqlTagContext ctx, Object parameterObject, Iterator localChildren) {
    PrintWriter out = ctx.getWriter();
    processBodyChildren(request, ctx, parameterObject, localChildren, out);
  }

  private void processBodyChildren(RequestScope request, SqlTagContext ctx, Object parameterObject, Iterator localChildren, PrintWriter out) {
    while (localChildren.hasNext()) {
      SqlChild child = (SqlChild) localChildren.next();
      if (child instanceof SqlText) {
        SqlText sqlText = (SqlText) child;
        String sqlStatement = sqlText.getText();
        if (sqlText.isWhiteSpace()) {
          out.print(sqlStatement);
        } else {

// -- Allows for dynamically generated statements (code supplied as parameter).
// -- Fails within iterate tag...deemed less valuable than iterative $substitutions[]$
//          if (SimpleDynamicSql.isSimpleDynamicSql(sqlStatement)) {
//            sqlStatement = new SimpleDynamicSql(sqlStatement).getSql(request, parameterObject);
//            sqlText = XmlSqlMapClientBuilder.parseInlineParameterMap(sqlStatement);
//            sqlStatement = sqlText.getText();
//          }

          // BODY OUT
          out.print(sqlStatement);

          ParameterMapping[] mappings = sqlText.getParameterMappings();
          if (mappings != null) {
            for (int i = 0, n = mappings.length; i < n; i++) {
              ctx.addParameterMapping(mappings[i]);
            }
          }
        }
      } else if (child instanceof SqlTag) {
        SqlTag tag = (SqlTag) child;
        SqlTagHandler handler = tag.getHandler();
        int response = SqlTagHandler.INCLUDE_BODY;
        do {
          StringWriter sw = new StringWriter();
          PrintWriter pw = new PrintWriter(sw);

          response = handler.doStartFragment(ctx, tag, parameterObject);
          if (response != SqlTagHandler.SKIP_BODY) {
            if (ctx.isOverridePrepend()
                && ctx.getFirstNonDynamicTagWithPrepend() == null
                && tag.isPrependAvailable()
                && !(tag.getHandler() instanceof DynamicTagHandler)) {
              ctx.setFirstNonDynamicTagWithPrepend(tag);
            }

            processBodyChildren(request, ctx, parameterObject, tag.getChildren(), pw);
            pw.flush();
            pw.close();
            StringBuffer body = sw.getBuffer();
            response = handler.doEndFragment(ctx, tag, parameterObject, body);
            handler.doPrepend(ctx, tag, parameterObject, body);
            if (response != SqlTagHandler.SKIP_BODY) {
              if (body.length() > 0) {
                // BODY OUT

                if (handler.isPostParseRequired()) {
                  SqlText sqlText = XmlSqlMapClientBuilder.parseInlineParameterMap(body.toString());
                  out.print(sqlText.getText());
                  ParameterMapping[] mappings = sqlText.getParameterMappings();
                  if (mappings != null) {
                    for (int i = 0, n = mappings.length; i < n; i++) {
                      ctx.addParameterMapping(mappings[i]);
                    }
                  }
                } else {
                  out.print(body.toString());
                }
                if (tag.isPrependAvailable() && tag == ctx.getFirstNonDynamicTagWithPrepend()) {
                  ctx.setOverridePrepend(false);
                }
              }
            }
          }
        } while (response == SqlTagHandler.REPEAT_BODY);
      }
    }
  }

  public void addChild(SqlChild child) {
    children.add(child);
  }

}
