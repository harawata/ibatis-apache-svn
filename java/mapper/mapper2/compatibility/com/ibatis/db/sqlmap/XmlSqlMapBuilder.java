package com.ibatis.db.sqlmap;

import com.ibatis.sqlmap.client.*;
import com.ibatis.sqlmap.engine.builder.xml.*;
import com.ibatis.db.sqlmap.upgrade.*;

import java.io.*;
import java.util.*;

/**
 * User: Clinton Begin
 * Date: Nov 18, 2003
 * Time: 9:47:32 PM
 */
public class XmlSqlMapBuilder {

  private static final XmlConverter SQL_MAP_CONVERTER = new SqlMapXmlConverter();

  private XmlSqlMapBuilder() {
  }

  public static SqlMap buildSqlMap(Reader reader) {
    XmlSqlMapClientBuilder builder = new XmlSqlMapClientBuilder();
    SqlMapClient client = builder.buildSqlMap(reader, SQL_MAP_CONVERTER, SQL_MAP_CONVERTER);
    return new SqlMap(client);
  }

  public static SqlMap buildSqlMap(Reader reader, Properties props) {
    XmlSqlMapClientBuilder builder = new XmlSqlMapClientBuilder();
    SqlMapClient client = builder.buildSqlMap(reader, props, SQL_MAP_CONVERTER, SQL_MAP_CONVERTER);
    return new SqlMap(client);
  }

}
