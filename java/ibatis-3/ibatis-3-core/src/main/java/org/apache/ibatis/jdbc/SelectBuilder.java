package org.apache.ibatis.jdbc;

import java.util.ArrayList;
import java.util.List;

public class SelectBuilder {
  private static final String AND = ") \nAND (";
  private static final String OR = ") \nOR (";


  private static final ThreadLocal<Query> localQuery = new ThreadLocal<Query>();

  static {
    localQuery.set(new Query());
  }

  private static class Query {
    List<String> select = new ArrayList<String>();
    List<String> from = new ArrayList<String>();
    List<String> join = new ArrayList<String>();
    List<String> innerJoin = new ArrayList<String>();
    List<String> outerJoin = new ArrayList<String>();
    List<String> leftOuterJoin = new ArrayList<String>();
    List<String> rightOuterJoin = new ArrayList<String>();
    List<String> where = new ArrayList<String>();
    List<String> having = new ArrayList<String>();
    List<String> groupBy = new ArrayList<String>();
    List<String> orderBy = new ArrayList<String>();
    List<String> lastList = new ArrayList<String>();
  }

  private static Query query() {
    return localQuery.get();
  }

  public static String SQL() {
    try {
      StringBuilder builder = new StringBuilder();
      sqlClause(builder, "SELECT", query().select, "", "", ", ");
      sqlClause(builder, "FROM", query().from, "", "", ", ");
      sqlClause(builder, "JOIN", query().join, "", "", "JOIN");
      sqlClause(builder, "INNER JOIN", query().innerJoin, "", "", "\nINNER JOIN ");
      sqlClause(builder, "OUTER JOIN", query().outerJoin, "", "", "\nOUTER JOIN ");
      sqlClause(builder, "LEFT OUTER JOIN", query().leftOuterJoin, "", "", "\nLEFT OUTER JOIN ");
      sqlClause(builder, "RIGHT OUTER JOIN", query().rightOuterJoin, "", "", "\nRIGHT OUTER JOIN ");
      sqlClause(builder, "WHERE", query().where, "(", ")", " AND ");
      sqlClause(builder, "GROUP BY", query().groupBy, "", "", ", ");
      sqlClause(builder, "HAVING", query().having, "(", ")", " AND ");
      sqlClause(builder, "ORDER BY", query().orderBy, "", "", ", ");
      return builder.toString();
    } finally {
      localQuery.set(new Query());
    }
  }

  private static void sqlClause(StringBuilder builder, String keyword, List<String> parts, String open, String close, String conjunction) {
    if (!parts.isEmpty()) {
      if (builder.length() > 0) builder.append("\n");
      builder.append(keyword);
      builder.append(" ");
      builder.append(open);
      String last = "________";
      for (int i = 0, n = parts.size(); i < n; i++) {
        String part = parts.get(i);
        if (i > 0 && !part.equals(AND) && !part.equals(OR)&& !last.equals(AND) && !last.equals(OR)) {
          builder.append(conjunction);
        }
        builder.append(part);
        last = part;
      }
      builder.append(close);
    }
  }

  public static void SELECT(String columns) {
    query().select.add(columns);
  }

  public static void FROM(String table) {
    query().from.add(table);
  }

  public static void JOIN(String join) {
    query().join.add(join);
  }

  public static void INNER_JOIN(String join) {
    query().innerJoin.add(join);
  }

  public static void LEFT_OUTER_JOIN(String join) {
    query().leftOuterJoin.add(join);
  }

  public static void RIGHT_OUTER_JOIN(String join) {
    query().rightOuterJoin.add(join);
  }

  public static void OUTER_JOIN(String join) {
    query().outerJoin.add(join);
  }

  public static void WHERE(String conditions) {
    query().where.add(conditions);
    query().lastList = query().where;
  }

  public static void OR() {
    query().lastList.add(OR);
  }

  public static void AND() {
    query().lastList.add(AND);
  }

  public static void GROUP_BY(String columns) {
    query().groupBy.add(columns);
  }

  public static void HAVING(String conditions) {
    query().having.add(conditions);
    query().lastList = query().having;
  }

  public static void ORDER_BY(String columns) {
    query().orderBy.add(columns);
  }


}
