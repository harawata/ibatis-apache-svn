package org.apache.ibatis.parsing;

import org.apache.ibatis.io.Resources;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.io.Reader;

public class XPathParserTest {

  @Test
  public void shouldTestXPathParserMethods() throws Exception {
    String resource = "resources/nodelet_test.xml";
    Reader reader = Resources.getResourceAsReader(resource);
    XPathParser parser = new XPathParser(reader, false, null, null);
    assertEquals(5.8d,parser.evalDouble("/employee/height"));
    assertEquals("${id_var}",parser.evalString("/employee/@id"));
    assertEquals(Boolean.TRUE,parser.evalBoolean("/employee/active"));
    assertEquals("<id>${id_var}</id>", parser.evalNode("/employee/@id").toString().trim());
    assertEquals(7, parser.evalNodes("/employee/*").size());
    XNode node = parser.evalNode("/employee/height");
    assertEquals("employee/height", node.getPath());
    assertEquals("employee[${id_var}]_height", node.getValueBasedIdentifier());
  }

}
