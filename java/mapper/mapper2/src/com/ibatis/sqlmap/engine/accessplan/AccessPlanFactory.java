package com.ibatis.sqlmap.engine.accessplan;

import java.util.*;

/**
 * User: Clinton Begin
 * Date: Nov 27, 2003
 * Time: 9:05:34 PM
 */
public class AccessPlanFactory {

  private static boolean bytecodeEnhancementEnabled = false;

  private AccessPlanFactory() {
  }

  public static AccessPlan getAccessPlan(Class clazz, String[] propertyNames) {
    AccessPlan plan;

    boolean complex = false;

    if (clazz == null || propertyNames == null) {
      complex = true;
    } else {
      for (int i = 0; i < propertyNames.length; i++) {
        if (propertyNames[i].indexOf('[') > -1
            || propertyNames[i].indexOf('.') > -1) {
          complex = true;
          break;
        }
      }
    }

    if (complex) {
      plan = new ComplexAccessPlan(clazz, propertyNames);
    } else if (Map.class.isAssignableFrom(clazz)) {
      plan = new MapAccessPlan(clazz, propertyNames);
    } else {
      // Possibly causes bug 945746 --but the bug is unconfirmed (can't be reproduced)
      if (bytecodeEnhancementEnabled) {
        try {
          plan = new EnhancedPropertyAccessPlan(clazz, propertyNames);
        } catch (Throwable t) {
          try {
            plan = new PropertyAccessPlan(clazz, propertyNames);
          } catch (Throwable t2) {
            plan = new ComplexAccessPlan(clazz, propertyNames);
          }
        }
      } else {
        try {
          plan = new PropertyAccessPlan(clazz, propertyNames);
        } catch (Throwable t) {
          plan = new ComplexAccessPlan(clazz, propertyNames);
        }
      }
    }
    return plan;
  }

  public static boolean isBytecodeEnhancementEnabled() {
    return bytecodeEnhancementEnabled;
  }

  public static void setBytecodeEnhancementEnabled(boolean bytecodeEnhancementEnabled) {
    AccessPlanFactory.bytecodeEnhancementEnabled = bytecodeEnhancementEnabled;
  }

}
