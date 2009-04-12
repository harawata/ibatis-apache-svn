package org.apache.ibatis.parser.xml.dynamic;

import java.util.Map;

public class WhereSqlNode implements SqlNode {

  private MixedSqlNode contents;

  public WhereSqlNode(MixedSqlNode contents) {
    this.contents = contents;
  }

  public boolean apply(DynamicContext context) {

    return false;
  }


  private static class FilteredDynamicContext extends DynamicContext {
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
        final String filteredSql = sql.trim().toUpperCase();
        if (filteredSql.startsWith("AND")) {
        }
      }
      delegate.appendSql(sql);
      filtered = true;
    }

    public String getSql() {
      return delegate.getSql();
    }
  }

}
