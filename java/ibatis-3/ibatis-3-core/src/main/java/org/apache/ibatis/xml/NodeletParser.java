package org.apache.ibatis.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Method;
import java.util.*;

public class NodeletParser {

  private Map nodeletMap = new HashMap();

  private boolean validation;
  private EntityResolver entityResolver;
  private Properties variables;

  public NodeletParser() {
    setValidation(false);
    setVariables(new Properties());
    setEntityResolver(null);
  }

  /**
   * Registers a nodelet for the specified XPath.  Current XPaths supported
   * are:
   * <ul>
   * <li> Element Path - /rootElement/childElement/theElement
   * <li> Closing element - /rootElement/childElement/end()
   * <li> All Elements Named - //theElement
   * </ul>
   */
  public void addNodeletHandler(Object handler) {
    Class type = handler.getClass();
    Method[] methods = type.getMethods();
    for (Method m : methods) {
      Nodelet n = m.getAnnotation(Nodelet.class);
      if (n != null) {
        checkMethodApplicable(n, type, m);
        nodeletMap.put(n.value(), new NodeletWrapper(handler, m));
      }
    }
  }

  /**
   * Begins parsing from the provided Reader.
   */
  public void parse(Reader reader) throws NodeletException {
    try {
      Document doc = createDocument(reader);
      parse(doc.getLastChild());
    } catch (Exception e) {
      throw new NodeletException("Error parsing XML.  Cause: " + e, e);
    }
  }

  public void setVariables(Properties variables) {
    this.variables = variables;
  }

  public void setValidation(boolean validation) {
    this.validation = validation;
  }

  public void setEntityResolver(EntityResolver resolver) {
    this.entityResolver = resolver;
  }

  private void checkMethodApplicable(Nodelet n, Class type, Method m) {
    if (nodeletMap.containsKey(n.value())) {
      throw new NodeletException("This nodelet parser already has a handler for path " + n.value());
    }
    Class<?>[] params = m.getParameterTypes();
    if (params.length != 1 || params[0] != NodeletContext.class) {
      throw new NodeletException("The method " + m.getName() + " on " + type + " does not take a single parameter of type NodeletContext.");
    }
  }

  /**
   * Begins parsing from the provided Node.
   */
  private void parse(Node node) {
    Path path = new Path();
    processNodelet(node, "/");
    process(node, path);
  }

  /**
   * A recursive method that walkes the DOM tree, registers XPaths and
   * calls Nodelets registered under those XPaths.
   */
  private void process(Node node, Path path) {
    if (node instanceof Element) {
      // Element
      String elementName = node.getNodeName();
      path.add(elementName);
      processNodelet(node, path.toString());
      processNodelet(node, new StringBuffer("//").append(elementName).toString());

      // Children
      NodeList children = node.getChildNodes();
      for (int i = 0; i < children.getLength(); i++) {
        process(children.item(i), path);
      }
      path.add("end()");
      processNodelet(node, path.toString());
      path.remove();
      path.remove();
    }
  }

  private void processNodelet(Node node, String pathString) {
    NodeletWrapper nodelet = (NodeletWrapper) nodeletMap.get(pathString);
    if (nodelet != null) {
      try {
        nodelet.process(new NodeletContext(node, variables));
      } catch (Exception e) {
        throw new NodeletException("Error parsing XPath '" + pathString + "'.  Cause: " + e, e);
      }
    }
  }

  /**
   * Creates a JAXP Document from a reader.
   */
  private Document createDocument(Reader reader) throws ParserConfigurationException, FactoryConfigurationError, SAXException, IOException {
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
  }

  /**
   * Inner helper class that assists with building XPath paths.
   * <p/>
   * Note:  Currently this is a bit slow and could be optimized.
   */
  private static class Path {

    private List nodeList = new ArrayList();

    public Path() {
    }

    public void add(String node) {
      nodeList.add(node);
    }

    public void remove() {
      nodeList.remove(nodeList.size() - 1);
    }

    public String toString() {
      StringBuffer buffer = new StringBuffer("/");
      for (int i = 0; i < nodeList.size(); i++) {
        buffer.append(nodeList.get(i));
        if (i < nodeList.size() - 1) {
          buffer.append("/");
        }
      }
      return buffer.toString();
    }
  }

}
