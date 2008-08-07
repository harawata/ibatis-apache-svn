package org.apache.ibatis.executor.result;

import java.util.*;

public class DefaultResultHandler implements ResultHandler {

  private final List list = new ArrayList();

  public void handleResult(Object resultObject) {
    list.add(resultObject);
  }

  public List getResultList() {
    return list;
  }

}
