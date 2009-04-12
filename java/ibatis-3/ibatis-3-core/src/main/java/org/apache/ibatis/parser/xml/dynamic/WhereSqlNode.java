package org.apache.ibatis.parser.xml.dynamic;

import java.util.*;

public class WhereSqlNode extends PrefixSqlNode{

  public WhereSqlNode(SqlNode contents) {
    super(contents, "WHERE", new ArrayList<String>() {{
      add("AND ");
      add("OR ");
    }});
  }


}
