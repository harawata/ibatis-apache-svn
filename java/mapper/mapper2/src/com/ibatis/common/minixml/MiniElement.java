package com.ibatis.common.minixml;

import java.util.*;

/**
 * User: Clinton Begin
 * Date: Dec 30, 2003
 * Time: 7:51:49 PM
 */
public class MiniElement {

  private String name;
  private String bodyContent;
  private MiniElement parent;
  private List attributeList = new ArrayList(1);
  private List elementList = new ArrayList();

  private Map attributeMap = new HashMap(1);
  private Map elementMap = new HashMap();

  public MiniElement() {
  }

  public MiniElement(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getBodyContent() {
    return bodyContent;
  }

  public void setBodyContent(String bodyContent) {
    this.bodyContent = bodyContent;
  }

  public MiniElement getParent() {
    return parent;
  }

  public void setParent(MiniElement parent) {
    this.parent = parent;
  }

  public void addAttribute(MiniAttribute attribute) {
    attributeList.add(attribute);
    attributeMap.put(attribute.getName(), attribute);
  }

  public MiniAttribute getAttribute(int i) {
    return (MiniAttribute) attributeList.get(i);
  }

  public MiniAttribute getAttribute(String name) {
    return (MiniAttribute) attributeMap.get(name);
  }

  public MiniAttribute removeAttribute(int i) {
    return (MiniAttribute) attributeList.remove(i);
  }

  public int getAttributeCount() {
    return attributeList.size();
  }

  public void addElement(MiniElement element) {
    elementList.add(element);
    element.setParent(this);
    elementMap.put(element.getName(), element);
  }

  public MiniElement getElement(int i) {
    return (MiniElement) elementList.get(i);
  }

  public MiniElement getElement(String name) {
    return (MiniElement) elementMap.get(name);
  }

  public MiniElement removeElement(int i) {
    return (MiniElement) elementList.remove(i);
  }

  public int getElementCount() {
    return elementList.size();
  }


}
