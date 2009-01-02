package org.apache.ibatis.executor;

public class ErrorContext {

  private static final ThreadLocal<ErrorContext> local = new ThreadLocal<ErrorContext>();

  public static void set(String resource, String activity, String objectId, String moreInfo) {
    _local()._set(resource,activity,objectId,moreInfo);
  }

  public static void set(String activity, String objectId, String moreInfo) {
    _local()._set(activity,objectId,moreInfo);
  }

  public static void set(String objectId, String moreInfo) {
    _local()._set(objectId,moreInfo);
  }

  public static void set(String moreInfo) {
    _local()._set(moreInfo);
  }

  public static void set(Throwable cause) {
    _local()._set(cause);
  }

  public static String description() {
    return _local()._toString();
  }

  public static void reset() {
    _local()._reset();
  }

  private static ErrorContext _local() {
    ErrorContext context = local.get();
    if (context == null) {
      context = new ErrorContext();
      local.set(context);
    }
    return context;
  }

  private String resource;
  private String activity;
  private String objectId;
  private String moreInfo;
  private Throwable cause;

  private ErrorContext() {
  }

  private void _set(String resource, String activity, String objectId, String moreInfo) {
    this.resource = resource;
    this.activity = activity;
    this.objectId = objectId;
    this.moreInfo = moreInfo;
  }

  private void _set(String activity, String objectId, String moreInfo) {
    this.activity = activity;
    this.objectId = objectId;
    this.moreInfo = moreInfo;
  }

  private void _set(String objectId, String moreInfo) {
    this.objectId = objectId;
    this.moreInfo = moreInfo;
  }

  private void _set(String moreInfo) {
    this.moreInfo = moreInfo;
  }

  private void _set(Throwable cause) {
    this.cause = cause;
  }

  private String _toString() {
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

  private void _reset() {
    resource = null;
    activity = null;
    objectId = null;
    moreInfo = null;
    cause = null;
  }

}
