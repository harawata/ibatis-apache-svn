package com.ibatis.common.minixml;

import java.io.*;

import com.ibatis.common.exception.*;
import com.ibatis.common.io.*;
import org.xml.sax.helpers.*;
import org.xml.sax.*;

import javax.xml.parsers.*;

/**
 * User: Clinton Begin
 * Date: Dec 30, 2003
 * Time: 7:51:27 PM
 */
public class MiniParser extends DefaultHandler {

  private MiniDom dom;
  private MiniElement currentElement;

  public MiniParser(String string) {
    try {
      StringReader reader = new StringReader(string);
      parse(reader);
    } catch (Exception e) {
      throw new NestedRuntimeException("XmlDataExchange error parsing XML.  Cause: " + e, e);
    }
  }

  public MiniParser(Reader reader) {
    try {
      parse(reader);
    } catch (Exception e) {
      throw new NestedRuntimeException("XmlDataExchange error parsing XML.  Cause: " + e, e);
    }
  }

  public MiniDom getDom() {
    return dom;
  }

  public void startDocument() {
    dom = new MiniDom();
  }

  public void endDocument() {
  }

  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    MiniElement element = new MiniElement(qName);
    if (attributes != null) {
      for (int i = 0, n = attributes.getLength(); i < n; i++) {
        element.addAttribute(new MiniAttribute(attributes.getQName(i), attributes.getValue(i)));
      }
    }
    if (currentElement == null && dom.getRootElement() == null) {
      dom.setRootElement(element);
    } else {
      currentElement.addElement(element);
    }
    currentElement = element;
  }

  public void endElement(String uri, String localName, String qName) throws SAXException {
    if (currentElement != null && currentElement != dom.getRootElement()) {
      currentElement = currentElement.getParent();
    }
  }

  public void characters(char ch[], int start, int length) {
    if (currentElement != null) {
      StringBuffer buffer;
      String current = currentElement.getBodyContent();
      if (current == null) {
        buffer = new StringBuffer();
      } else {
        buffer = new StringBuffer(current);
      }
      buffer.append(ch, start, length);
      currentElement.setBodyContent(buffer.toString());
    }
  }

  

  public void fatalError(SAXParseException e) throws SAXException {
    throw new NestedRuntimeException("MiniXmlParser error parsing XML.  Cause: " + e, e);
  }

  private void parse(Reader reader) throws ParserConfigurationException, SAXException, IOException {
    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setValidating(false);
    factory.setNamespaceAware(false);
    SAXParser parser = factory.newSAXParser();
    parser.parse(new ReaderInputStream(reader), this);
  }

}
