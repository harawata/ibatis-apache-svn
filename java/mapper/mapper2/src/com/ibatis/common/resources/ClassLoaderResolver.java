package com.ibatis.common.resources;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This non-instantiable non-subclassable class acts as the global point for
 * choosing a ClassLoader for dynamic class/resource loading at any point
 * in an application.
 * <p/>
 * CONSIDER PERFORMANCE WHEN USING THIS CLASS
 * <p/>
 * Adapted from code by Vlad Roubtsov, as originally documented in the article
 * <a href="http://www.javaworld.com/columns/jw-qna-index.shtml">Find a way out of the ClassLoader maze</a>
 */
public abstract class ClassLoaderResolver {

  private static final Log log = LogFactory.getLog(ClassLoaderResolver.class);

  private static final int CALL_CONTEXT_OFFSET = 3; // accounts for this APIs call stack
  private static CallerResolver callerResolver; // set in <clinit>

  static {
    try {
      // this can fail if the current SecurityManager does not allow
      // RuntimePermission ("createSecurityManager"):
      callerResolver = new CallerResolver();
    } catch (Throwable t) {
      callerResolver = null;
      // Tolerate error and keep running.
      // Enhanced classloading is optional.
      log.error("ClassLoaderResolver: Could not initialize custom security manager. Need RuntimePermission (\"createSecurityManager\"). Cause: + " + t, t);
    }

  }

  /**
   * Prevent subclassing and instantiation.
   */
  private ClassLoaderResolver() {
  }

  /**
   * This method selects the "best" classloader instance to be used for
   * class/resource loading by whoever calls this method. The decision
   * involves choosing between the caller's current, thread context,
   * system, and other classloaders in the JVM.
   *
   * @return classloader to be used by the caller ['null' indicates the
   *         primordial loader]
   */
  public static ClassLoader getClassLoader(int callStackOffset) {

    Class caller = getCallerClass(callStackOffset);

    final ClassLoader callerLoader = caller.getClassLoader();
    final ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();

    ClassLoader result;

    // if 'callerLoader' and 'contextLoader' are in a parent-child
    // relationship, always choose the child:

    if (isChild(contextLoader, callerLoader)) {
      result = callerLoader;
    } else if (isChild(callerLoader, contextLoader)) {
      result = contextLoader;
    } else {
      // this else branch could be merged into the previous one,
      // but I show it here to emphasize the ambiguous case:
      result = contextLoader;
    }

    final ClassLoader systemLoader = ClassLoader.getSystemClassLoader();

    // precaution for when deployed as a bootstrap or extension class:
    if (isChild(result, systemLoader)) {
      result = systemLoader;
    }

    return result;
  }

  /*
   * Indexes into the current method call context with a given
   * offset.
   */
  private static Class getCallerClass(final int callerOffset) {
    if (callerResolver != null) {
      return callerResolver.getClassContext()[CALL_CONTEXT_OFFSET + callerOffset];
    } else {
      return ClassLoaderResolver.class;
    }
  }

  /**
   * Returns 'true' if 'loader2' is a delegation child of 'loader1' [or if
   * 'loader1'=='loader2']. Of course, this works only for classloaders that
   * set their parent pointers correctly. 'null' is interpreted as the
   * primordial loader [i.e., everybody's parent].
   */
  private static boolean isChild(final ClassLoader loader1, ClassLoader loader2) {
    if (loader1 == loader2) {
      return true;
    }
    if (loader2 == null) {
      return false;
    }
    if (loader1 == null) {
      return true;
    }

    while (loader2 != null) {
      if (loader2 == loader1) {
        return true;
      }
      loader2 = loader2.getParent();
    }

    return false;
  }


  /**
   * A helper class to get the call context. It subclasses SecurityManager
   * to make getClassContext() accessible. An instance of CallerResolver
   * only needs to be created, not installed as an actual security
   * manager.
   */
  private static final class CallerResolver extends SecurityManager {
    protected Class[] getClassContext() {
      return super.getClassContext();
    }

  }

}

