package org.apache.ibatis.parser.xml.dynamic;

import java.util.*;

public class SetSqlNode extends PrefixSqlNode {

  public SetSqlNode(SqlNode contents) {
    super(contents, "SET", new ArrayList<String>() {{
      add(",");
    }});
  }

}
