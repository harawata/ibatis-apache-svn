package com.ibatis.sqlmap.engine.scope;

/**
 * User: Clinton Begin
 * Date: Sep 13, 2003
 * Time: 6:42:31 AM
 */
public class ThreadScope extends BaseScope {

  private static final ThreadLocal LOCAL_CONTEXT = new ThreadLocal();

  private ThreadScope() {
  }

  public static ThreadScope getInstance() {
    ThreadScope ctx = (ThreadScope) LOCAL_CONTEXT.get();
    if (ctx == null) {
      ctx = new ThreadScope();
      LOCAL_CONTEXT.set(ctx);
    }
    return ctx;
  }

}
