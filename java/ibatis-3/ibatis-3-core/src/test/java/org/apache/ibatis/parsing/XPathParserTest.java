package org.apache.ibatis.parsing;

import static org.junit.Assert.*;
import org.junit.Test;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.parsing.XPathParser;

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
  }

}
