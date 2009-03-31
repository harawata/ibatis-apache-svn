package org.apache.ibatis.parser;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.mapping.Configuration;

import java.util.List;

public class DynamicSqlSource implements SqlSource {

  private Configuration configuration;

  public DynamicSqlSource(Configuration configuration) {
    this.configuration = configuration;
  }

  public BoundSql getBoundSql(Object parameterObject) {
    String sql = "";
    SqlSourceParser sqlSourceParser = new SqlSourceParser(configuration);
    SqlSource sqlSource = sqlSourceParser.parse(sql);
    return sqlSource.getBoundSql(parameterObject);
  }

  public static interface SqlNode {
    String getString(Object parameterObject);
  }

  public static class StaticSqlNode implements SqlNode {
    private String sql;

    public StaticSqlNode(String sql) {
      this.sql = sql;
    }

    public String getString(Object parameterObject) {
      return sql;
    }
  }

  public static class ForEachSqlNode implements SqlNode {
    private List collection;
    private List<SqlNode> childSqlNodes;

    public ForEachSqlNode(List collection, List<SqlNode> childSqlNodes) {
      this.collection = collection;
      this.childSqlNodes = childSqlNodes;
    }

    public String getString(Object parameterObject) {
      StringBuilder builder = new StringBuilder();
      for (Object o : collection) {
        for (SqlNode sqlNode : childSqlNodes) {
          builder.append(sqlNode.getString(parameterObject));
        }
      }
      return builder.toString();
    }
  }

  public static class IfSqlNode implements SqlNode {
    private boolean test;
    private List<SqlNode> childSqlNodes;

    public IfSqlNode(boolean test, List<SqlNode> childSqlNodes) {
      this.test = test;
      this.childSqlNodes = childSqlNodes;
    }

    public String getString(Object parameterObject) {
      StringBuilder builder = new StringBuilder();
      if (test) {
        for (SqlNode sqlNode : childSqlNodes) {
          builder.append(sqlNode.getString(parameterObject));
        }
      }
      return builder.toString();
    }
  }


}
