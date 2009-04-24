package org.apache.ibatis.xpath;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.*;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.namespace.QName;
import java.io.Reader;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;

public class XPathParser {

  private Document document;
  private boolean validation;
  private EntityResolver entityResolver;
  private Properties variables;
  private XPath xpath;

  public XPathParser(Reader reader, boolean validation, EntityResolver entityResolver, Properties variables) {
    this.validation = validation;
    this.entityResolver = entityResolver;
    this.variables = variables;
    this.document = createDocument(reader);
    XPathFactory factory = XPathFactory.newInstance();
    this.xpath = factory.newXPath();
  }

  public String getString(String expression) {
    String result = (String) evaluate(expression, XPathConstants.STRING);
    result = PropertyParser.parse(result, variables);
    return result;
  }

  public Boolean getBoolean(String expression) {
    return (Boolean)evaluate(expression, XPathConstants.BOOLEAN);
  }

  public Double getDouble(String expression) {
    return (Double)evaluate(expression, XPathConstants.NUMBER);
  }

  public List<XNode> getNodes(String expression) {
    List<XNode> xnodes = new ArrayList<XNode>();
    NodeList nodes = (NodeList) evaluate(expression, XPathConstants.NODESET);
    for(int i = 0; i < nodes.getLength(); i++) {
      xnodes.add(new XNode(nodes.item(i),variables));
    }
    return xnodes;
  }

  public XNode getNode(String expression) {
    Node node = (Node) evaluate(expression, XPathConstants.NODE);
    return new XNode(node,variables);
  }

  private Object evaluate(String expression, QName returnType) {
    try {
      return xpath.evaluate(expression, document, returnType);
    } catch (Exception e) {
      throw new RuntimeException("Error evaluating XPath.  Cause: " + e, e);
    }
  }

  private Document createDocument(Reader reader) {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setValidating(validation);

      factory.setNamespaceAware(false);
      factory.setIgnoringComments(true);
      factory.setIgnoringElementContentWhitespace(false);
      factory.setCoalescing(false);
      factory.setExpandEntityReferences(true);

      DocumentBuilder builder = factory.newDocumentBuilder();
      builder.setEntityResolver(entityResolver);
      builder.setErrorHandler(new ErrorHandler() {
        public void error(SAXParseException exception) throws SAXException {
          throw exception;
        }

        public void fatalError(SAXParseException exception) throws SAXException {
          throw exception;
        }

        public void warning(SAXParseException exception) throws SAXException {
        }
      });
      return builder.parse(new InputSource(reader));
    } catch (Exception e) {
      throw new RuntimeException("Error creating document instance.  Cause: " + e, e);
    }
  }


}
