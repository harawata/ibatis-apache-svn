package com.ibatis.sqlmap.client;

import com.ibatis.sqlmap.engine.builder.xml.*;

import java.io.*;
import java.util.*;

/**
 * Builds SqlMapClient instances from a supplied resource (e.g. XML configuration file)
 * <p>
 * The SqlMapClientBuilder class is responsible for parsing configuration documents
 * and building the SqlMapClient instance.  Its current implementation works with
 * XML configuration files (e.g. sql-map-config.xml).
 * <p>
 * Example:
 * <pre>
 * Reader reader = Resources.getResourceAsReader("properties/sql-map-config.xml");
 * SqlMapClient client = SqlMapClientBuilder.buildSqlMapClient (reader);
 * </pre>
 * <p>
 * Examples of the XML document structure used by SqlMapClientBuilder can
 * be found at the links below.
 * <p>
 * Note: They might look big, but they're mostly comments!
 * <ul>
 *   <li> <a href="sql-map-config.txt">The SQL Map Config File</a>
 *   <li> <a href="sql-map.txt">An SQL Map File</a>
 * <ul>
 * <p>
  * Date: Sep 5, 2003 4:26:32 PM
 * @author Clinton Begin
 */
public class SqlMapClientBuilder {

  /**
   * No instantiation allowed.
   */
  private SqlMapClientBuilder() {
  }

  /**
   * Builds an SqlMapClient using the specified reader.
   *
   * @param reader A Reader instance that reads an sql-map-config.xml file.
   * The reader should read an well formed sql-map-config.xml file.
   * @return An SqlMapClient instance.
   */
  public static SqlMapClient buildSqlMapClient(Reader reader) {
    return new XmlSqlMapClientBuilder().buildSqlMap(reader);
  }

  /**
   * Builds an SqlMapClient using the specified reader and properties file.
   * <p>
   *
   * @param reader A Reader instance that reads an sql-map-config.xml file.
   * The reader should read an well formed sql-map-config.xml file.
   * @param props Properties to be used to provide values to dynamic property tokens
   * in the sql-map-config.xml configuration file.  This provides an easy way to
   * achieve some level of programmatic configuration.
   * @return An SqlMapClient instance.
   */
  public static SqlMapClient buildSqlMapClient(Reader reader, Properties props) {
    return new XmlSqlMapClientBuilder().buildSqlMap(reader, props);
  }

}
