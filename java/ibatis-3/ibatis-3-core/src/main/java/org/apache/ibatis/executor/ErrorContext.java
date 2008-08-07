package org.apache.ibatis.executor;

public class ErrorContext {

  private String resource;
  private String activity;
  private String objectId;
  private String moreInfo;
  private Throwable cause;

  public void set(String resource, String activity, String objectId, String moreInfo) {
    this.resource = resource;
    this.activity = activity;
    this.objectId = objectId;
    this.moreInfo = moreInfo;
  }

  public void set(String activity, String objectId, String moreInfo) {
    this.activity = activity;
    this.objectId = objectId;
    this.moreInfo = moreInfo;
  }

  public void set(String objectId, String moreInfo) {
    this.objectId = objectId;
    this.moreInfo = moreInfo;
  }

  public void set(String moreInfo) {
    this.moreInfo = moreInfo;
  }

  public void set(Throwable cause) {
    this.cause = cause;
  }

  public String toString() {
    StringBuffer message = new StringBuffer();

    // resource
    if (resource != null) {
      message.append("  \n*** The error occurred in ");
      message.append(resource);
      message.append(".");
    }

    // activity
    if (activity != null) {
      message.append("  \n*** The error occurred while ");
      message.append(activity);
      message.append(".");
    }

    // object
    if (objectId != null) {
      message.append("  \n*** Check ");
      message.append(objectId);
      message.append(".");
    }

    // more info
    if (moreInfo != null) {
      message.append("  \n*** ");
      message.append(moreInfo);
    }

    // cause
    if (cause != null) {
      message.append("  \n*** Cause: ");
      message.append(cause.toString());
    }

    return message.toString();
  }

  public void reset() {
    resource = null;
    activity = null;
    objectId = null;
    moreInfo = null;
    cause = null;
  }

}
