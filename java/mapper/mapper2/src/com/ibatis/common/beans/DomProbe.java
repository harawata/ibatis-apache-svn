package com.ibatis.common.beans;

import org.w3c.dom.*;

/**
 * <p/>
 * Date: Apr 24, 2004 11:37:54 PM
 * 
 * @author Clinton Begin
 */
public class DomProbe extends BaseProbe {

  public String[] getReadablePropertyNames(Object object) {
    return new String[0];
  }

  public String[] getWriteablePropertyNames(Object object) {
    return new String[0];
  }

  public Class getPropertyTypeForSetter(Object object, String name) {
    return null;
  }

  public Class getPropertyTypeForGetter(Object object, String name) {
    return null;
  }

  public boolean hasWritableProperty(Object object, String propertyName) {
    return false;
  }

  public boolean hasReadableProperty(Object object, String propertyName) {
    return false;
  }

  public Object getObject(Object object, String name) {
    return null;
  }

  public void setObject(Object object, String name, Object value) {

  }

  protected void setProperty(Object object, String property, Object value) {
  }

  protected Object getProperty(Object object, String property) {
    return null;
  }

  private void setElementValue(Element element, String property, Object value, int index) {
    CharacterData data = null;

    Element prop = findNodeByName(element, property, index, true);

    // Find text child element
    NodeList texts = prop.getChildNodes();
    if (texts.getLength() == 1) {
      Node child = texts.item(0);
      if (child instanceof CharacterData) {
        // Use existing text.
        data = (CharacterData) child;
      } else {
        // Remove non-text, add text.
        prop.removeChild(child);
        Text text = prop.getOwnerDocument().createTextNode(String.valueOf(value));
        prop.appendChild(text);
        data = text;
      }
    } else if (texts.getLength() > 1) {
      // Remove all, add text.
      for (int i = texts.getLength() - 1; i >= 0; i--) {
        prop.removeChild(texts.item(i));
      }
      Text text = prop.getOwnerDocument().createTextNode(String.valueOf(value));
      prop.appendChild(text);
      data = text;
    } else {
      // Add text.
      Text text = prop.getOwnerDocument().createTextNode(String.valueOf(value));
      prop.appendChild(text);
      data = text;
    }

    // Set type attribute
    prop.setAttribute("type", value == null ? "null" : value.getClass().getName());

    data.setData(String.valueOf(value));
  }

  private Object getElementValue(Element element, String property, int index) {
    StringBuffer value = new StringBuffer();

    Element prop = findNodeByName(element, property, index, false);

    // Find text child elements
    NodeList texts = prop.getChildNodes();
    if (texts.getLength() > 0) {
      for (int i = 0; i < texts.getLength(); i++) {
        Node text = texts.item(i);
        if (text instanceof CharacterData) {
          value.append(((CharacterData) text).getData());
        }
      }
    } else {
      value = null;
    }

    //convert to proper type
    //value = convert(value.toString());

    return value.toString();
  }


  private Element findNodeByName(Element element, String name, int index, boolean create) {
    Element prop = null;

    // Find named property element
    NodeList props = element.getElementsByTagName(name);
    if (props.getLength() > index) {
      prop = (Element) props.item(index);
    } else {
      if (create) {
        for (int i = 0; i < index + 1; i++) {
          prop = element.getOwnerDocument().createElement(name);
          element.appendChild(prop);
        }
      }
    }
    return prop;
  }
}
