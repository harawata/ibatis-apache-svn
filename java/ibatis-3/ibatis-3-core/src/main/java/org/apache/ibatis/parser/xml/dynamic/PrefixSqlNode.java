package org.apache.ibatis.parser.xml.dynamic;

import java.util.*;

public class PrefixSqlNode implements SqlNode {

  private SqlNode contents;
  private List<String> stringsToRemove = new ArrayList<String>();
  private String stringToInsert;

  public PrefixSqlNode(SqlNode contents, String stringToInsert, List<String> stringsToRemove) {
    this.contents = contents;
    this.stringsToRemove = stringsToRemove;
    this.stringToInsert = stringToInsert;
  }

  public boolean apply(DynamicContext context) {
    return contents.apply(new FilteredDynamicContext(context));
  }


  private class FilteredDynamicContext extends DynamicContext {
    private DynamicContext delegate;
    private boolean filtered;

    public FilteredDynamicContext(DynamicContext delegate) {
      super(null);
      this.delegate = delegate;
      this.filtered = false;
    }

    public Map<String, Object> getBindings() {
      return delegate.getBindings();
    }

    public void bind(String name, Object value) {
      delegate.bind(name, value);
    }

    public void appendSql(String sql) {
      if (!filtered) {
        filtered = true;
        String filteredSql = sql.trim().toUpperCase();
        for(String toRemove : stringsToRemove) {
          if (filteredSql.startsWith(toRemove)) {
            sql = sql.trim().substring(toRemove.trim().length()).trim();
            break;
          }
        }
        if (stringToInsert != null) {
          delegate.appendSql(stringToInsert);
        }
        delegate.appendSql(sql);
      } else {
        delegate.appendSql(sql);
      }
    }

    public String getSql() {
      return delegate.getSql();
    }
  }

}
