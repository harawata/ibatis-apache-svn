package com.ibatis.common.minixml;

/**
 * User: Clinton Begin
 * Date: Dec 30, 2003
 * Time: 7:51:42 PM
 */
public class MiniAttribute {

  private String name;
  private String value;

  public MiniAttribute() {
  }

  public MiniAttribute(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

}
