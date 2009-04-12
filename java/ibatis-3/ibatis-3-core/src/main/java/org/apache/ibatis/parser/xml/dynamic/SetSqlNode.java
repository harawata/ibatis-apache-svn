package org.apache.ibatis.parser.xml.dynamic;

import java.util.Map;

public class SetSqlNode implements SqlNode {

  private MixedSqlNode contents;

  public SetSqlNode(MixedSqlNode contents) {
    this.contents = contents;
  }

  public boolean apply(DynamicContext context) {
    return contents.apply(new FilteredDynamicContext(context));
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
        filtered = true;
        String filteredSql = sql.trim().toUpperCase();
        if (filteredSql.startsWith(",")) {
          sql = sql.trim().substring(1).trim();
        }
        delegate.appendSql("SET");
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
