package org.apache.ibatis.builder.xml.dynamic;

import java.util.*;

public class TrimSqlNode implements SqlNode {

  private SqlNode contents;
  private String stringToPrefixWith;
  private List<String> stringsToOverride = new ArrayList<String>();

  public TrimSqlNode(SqlNode contents, String with, String overrides) {
    this.contents = contents;
    this.stringToPrefixWith = with;
    this.stringsToOverride = parseOverrides(overrides);
  }

  public boolean apply(DynamicContext context) {
    return contents.apply(new FilteredDynamicContext(context));
  }

  private List<String> parseOverrides(String overrides) {
    if (overrides != null) {
      final StringTokenizer parser = new StringTokenizer(overrides, "|", false);
      return new ArrayList<String>() {{
          while (parser.hasMoreTokens()) {
            add(parser.nextToken());
          }}
      };
    }
    return Collections.EMPTY_LIST;
  }

  private class FilteredDynamicContext extends DynamicContext {
    private DynamicContext delegate;
    private boolean prefixApplied;

    public FilteredDynamicContext(DynamicContext delegate) {
      super(null);
      this.delegate = delegate;
      this.prefixApplied = false;
    }

    public Map<String, Object> getBindings() {
      return delegate.getBindings();
    }

    public void bind(String name, Object value) {
      delegate.bind(name, value);
    }

    public void appendSql(String sql) {
      if (!prefixApplied) {
        String filteredSql = sql.trim().toUpperCase();
        if (filteredSql.length() > 0) {
          prefixApplied = true;
          for (String toRemove : stringsToOverride) {
            if (filteredSql.startsWith(toRemove) || filteredSql.startsWith(toRemove.trim())) {
              sql = sql.trim().substring(toRemove.trim().length()).trim();
              break;
            }
          }
          if (stringToPrefixWith != null) {
            delegate.appendSql(stringToPrefixWith);
          }
          delegate.appendSql(sql);
        }
      } else {
        delegate.appendSql(sql);
      }
    }

    public String getSql() {
      return delegate.getSql();
    }

    @Override
    public int getUniqueNumber() {
      return delegate.getUniqueNumber();
    }
  }

}
