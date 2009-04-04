package org.apache.ibatis.parser.xml.dynamic;

public class ForEachSqlNode implements SqlNode {
  private Iterable collection;
  private MixedSqlNode contents;

  public ForEachSqlNode(Iterable collection, MixedSqlNode contents) {
    this.collection = collection;
    this.contents = contents;
  }

  public boolean apply(DynamicContext builder) {
    for (Object o : collection) {
      contents.apply(builder);
    }
    return true;
  }

}
