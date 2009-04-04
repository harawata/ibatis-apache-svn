package org.apache.ibatis.parser.xml.dynamic;

public interface SqlNode {
  boolean apply(DynamicContext builder);
}
