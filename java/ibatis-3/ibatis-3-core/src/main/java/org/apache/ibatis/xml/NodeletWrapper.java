package org.apache.ibatis.xml;

import java.lang.reflect.Method;

public class NodeletWrapper {

  private Object nodeletTarget;
  private Method method;

  public NodeletWrapper(Object nodeletTarget, Method method) {
    this.nodeletTarget = nodeletTarget;
    this.method = method;
  }

  public void process(NodeletContext context) {
    try {
      method.invoke(nodeletTarget, new Object[]{context});
    } catch (Exception e) {
      throw new NodeletException("Error processing node " + context.getNode().getNodeName() + ". Cause: " + e, e);
    }
  }

}
