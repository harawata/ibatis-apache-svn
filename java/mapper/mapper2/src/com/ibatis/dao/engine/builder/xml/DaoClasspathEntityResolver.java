package com.ibatis.dao.engine.builder.xml;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.EntityResolver;

import java.io.InputStream;

import com.ibatis.common.resources.Resources;

/**
 * <p/>
 * Date: Feb 22, 2004 11:48:13 AM
 * 
 * @author Clinton Begin
 */
public class DaoClasspathEntityResolver implements EntityResolver {

  private static final String SYSTEM_ID_DAO = "http://www.ibatis.com/dtd/dao-2.dtd";
  private static final String DTD_PATH_DAO = "com/ibatis/dao/engine/builder/xml/dao-2.dtd";

  /** Converts a public DTD into a local one
   * @param publicId Unused but required by EntityResolver interface
   * @param systemId The DTD that is being requested
   * @throws org.xml.sax.SAXException If anything goes wrong
   * @return The InputSource for the DTD
   */
  public InputSource resolveEntity(String publicId, String systemId)
      throws SAXException {
    InputSource source = null;

    try {
      if (systemId.equals(SYSTEM_ID_DAO)) {
        InputStream in = Resources.getResourceAsStream(DTD_PATH_DAO);
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
