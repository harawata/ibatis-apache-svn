package org.apache.ibatis.migration;

import java.math.BigDecimal;

public class Change {

  private BigDecimal id;
  private String description;

  public Change() {
  }

  public Change(BigDecimal id, String description) {
    this.id = id;
    this.description = description;
  }

  public BigDecimal getId() {
    return id;
  }

  public void setId(BigDecimal id) {
    this.id = id;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String toString() {
    StringBuilder idstring = new StringBuilder(id.toString());
    idstring.insert(12,":");
    idstring.insert(10,":");
    idstring.insert(8," ");
    idstring.insert(6,"-");
    idstring.insert(4,"-");
    return id + " [" + idstring + "] " + description;
  }
}
