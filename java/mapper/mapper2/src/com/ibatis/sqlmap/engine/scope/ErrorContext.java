/*
 *  Copyright 2004 Clinton Begin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.ibatis.sqlmap.engine.scope;

public class ErrorContext {

  private String resource;
  private String activity;
  private String objectId;
  private String moreInfo;
  private Throwable cause;

  public String getResource() {
    return resource;
  }

  public void setResource(String resource) {
    this.resource = resource;
  }

  public String getActivity() {
    return activity;
  }

  public void setActivity(String activity) {
    this.activity = activity;
  }

  public String getObjectId() {
    return objectId;
  }

  public void setObjectId(String objectId) {
    this.objectId = objectId;
  }

  public String getMoreInfo() {
    return moreInfo;
  }

  public void setMoreInfo(String moreInfo) {
    this.moreInfo = moreInfo;
  }

  public Throwable getCause() {
    return cause;
  }

  public void setCause(Throwable cause) {
    this.cause = cause;
  }

  public String toString() {
    StringBuffer message = new StringBuffer();

    // resource
    if (resource != null) {
      message.append("  \n--- The error occurred in ");
      message.append(resource);
      message.append(".");
    }

    // activity
    if (activity != null) {
      message.append("  \n--- The error occurred while ");
      message.append(activity);
      message.append(".");
    }

    // object
    if (objectId != null) {
      message.append("  \n--- Check the ");
      message.append(objectId);
      message.append(".");
    }

    // more info
    if (moreInfo != null) {
      message.append("  \n--- ");
      message.append(moreInfo);
    }

    // cause
    if (cause != null) {
      message.append("  \n--- Cause: ");
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
