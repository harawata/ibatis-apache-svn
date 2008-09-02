package org.apache.ibatis.monarch.builder;

import org.xml.sax.*;
import org.apache.ibatis.io.Resources;

import java.io.*;
import java.util.*;

/**
 * Offline entity resolver for the iBATIS DTDs
 */
public class MapperEntityResolver implements EntityResolver {

  private static final String MAPPER_CONFIG_DTD_RESOURCE = "org/apache/ibatis/monarch/dtd/mapper-config.dtd";
  private static final String MAPPER_DTD_RESOURCE = "org/apache/ibatis/monarch/dtd/mapper.dtd";

  private static final Map<String, String> doctypeMap = new HashMap<String, String>();

  static {
    doctypeMap.put("http://ibatis.apache.org/dtd/mapper-config-3.dtd".toUpperCase(), MAPPER_CONFIG_DTD_RESOURCE);
    doctypeMap.put("-//ibatis.apache.org//DTD Mapper Config 2.0//EN".toUpperCase(), MAPPER_CONFIG_DTD_RESOURCE);

    doctypeMap.put("http://ibatis.apache.org/dtd/mapper-3.dtd".toUpperCase(), MAPPER_DTD_RESOURCE);
    doctypeMap.put("-//ibatis.apache.org//DTD Mapper 3.0//EN".toUpperCase(), MAPPER_DTD_RESOURCE);
  }

  /**
   * Converts a public DTD into a local one
   *
   * @param publicId Unused but required by EntityResolver interface
   * @param systemId The DTD that is being requested
   * @return The InputSource for the DTD
   * @throws org.xml.sax.SAXException If anything goes wrong
   */
  public InputSource resolveEntity(String publicId, String systemId)
      throws SAXException {

    if (publicId != null) publicId = publicId.toUpperCase();
    if (systemId != null) systemId = systemId.toUpperCase();

    InputSource source = null;
    try {
      String path = doctypeMap.get(publicId);
      source = getInputSource(path, source);
      if (source == null) {
        path = doctypeMap.get(systemId);
        source = getInputSource(path, source);
      }
    } catch (Exception e) {
      throw new SAXException(e.toString());
    }
    return source;
  }

  private InputSource getInputSource(String path, InputSource source) {
    if (path != null) {
      InputStream in;
      try {
        in = Resources.getResourceAsStream(path);
        source = new InputSource(in);
      } catch (IOException e) {
        // ignore, null is ok
      }
    }
    return source;
  }

}