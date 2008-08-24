package org.apache.ibatis.migration;

import java.math.BigDecimal;

public class Change {

  private BigDecimal id;
  private String description;
  private String appliedTimestamp;

  public Change() {
  }

  public Change(BigDecimal id, String appliedTimestamp, String description) {
    this.id = id;
    this.appliedTimestamp = appliedTimestamp;
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

  public String getAppliedTimestamp() {
    return appliedTimestamp;
  }

  public void setAppliedTimestamp(String appliedTimestamp) {
    this.appliedTimestamp = appliedTimestamp;
  }

  public String toString() {
    return id + " " + appliedTimestamp + " " + description;
  }

  private String formattedId() {
    StringBuilder idString = new StringBuilder(id.toString());
    idString.insert(12,":");
    idString.insert(10,":");
    idString.insert(8," ");
    idString.insert(6,"-");
    idString.insert(4,"-");
    return idString.toString();
  }
}
