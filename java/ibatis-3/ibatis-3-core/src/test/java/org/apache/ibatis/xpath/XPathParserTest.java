package org.apache.ibatis.xpath;

import static org.junit.Assert.*;
import org.junit.Test;
import org.apache.ibatis.io.Resources;

import java.io.Reader;

public class XPathParserTest {

  @Test
  public void shouldTestXPathParserMethods() throws Exception {
    String resource = "resources/nodelet_test.xml";
    Reader reader = Resources.getResourceAsReader(resource);
    XPathParser parser = new XPathParser(reader, false, null, null);
    assertEquals(5.8d,parser.getDouble("/employee/height"));
    assertEquals("id_var",parser.getString("/employee/@id"));
    assertEquals(Boolean.TRUE,parser.getBoolean("/employee/active"));
    assertEquals("<id>id_var</id>", parser.getNode("/employee/@id").toString().trim());
    assertEquals(7, parser.getNodes("/employee/*").size());
  }

}
