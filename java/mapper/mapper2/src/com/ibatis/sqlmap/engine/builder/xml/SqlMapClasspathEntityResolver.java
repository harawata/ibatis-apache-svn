package com.ibatis.sqlmap.engine.builder.xml;

import org.xml.sax.*;

import java.io.*;

import com.ibatis.common.resources.*;

/**
 * User: Clinton Begin
 * Date: Jan 5, 2004
 * Time: 11:52:33 PM
 */
public class SqlMapClasspathEntityResolver implements EntityResolver {

  private static final String SYSTEM_ID_SQL_MAP_CONFIG = "http://www.ibatis.com/dtd/sql-map-config-2.dtd";
  private static final String SYSTEM_ID_SQL_MAP = "http://www.ibatis.com/dtd/sql-map-2.dtd";
  private static final String DTD_PATH_SQL_MAP_CONFIG = "com/ibatis/sqlmap/engine/builder/xml/sql-map-config-2.dtd";
  private static final String DTD_PATH_SQL_MAP = "com/ibatis/sqlmap/engine/builder/xml/sql-map-2.dtd";

  /** Converts a public DTD into a local one
   * @param publicId Unused but required by EntityResolver interface
   * @param systemId The DTD that is being requested
   * @throws SAXException If anything goes wrong
   * @return The InputSource for the DTD
   */
  public InputSource resolveEntity(String publicId, String systemId)
      throws SAXException {
    InputSource source = null;

    try {
      if (systemId.equals(SYSTEM_ID_SQL_MAP_CONFIG)) {
        InputStream in = Resources.getResourceAsStream(DTD_PATH_SQL_MAP_CONFIG);
        source = new InputSource(in);
      } else if (systemId.equals(SYSTEM_ID_SQL_MAP)) {
        InputStream in = Resources.getResourceAsStream(DTD_PATH_SQL_MAP);
        source = new InputSource(in);
      } else {
        source = null;
      }
    } catch (Exception e) {
      throw new SAXException(e.toString());
    }

    return source;
  }

}
