package com.ibatis.sqlmap.engine.exchange;

import com.ibatis.sqlmap.engine.mapping.parameter.*;
import com.ibatis.sqlmap.engine.mapping.result.*;

import com.ibatis.sqlmap.engine.scope.*;
import com.ibatis.sqlmap.engine.accessplan.*;
import com.ibatis.common.exception.*;
import com.ibatis.common.resources.*;

import java.util.*;

/**
 * User: Clinton Begin
 * Date: Sep 6, 2003
 * Time: 8:31:29 AM
 */
public class JavaBeanDataExchange extends BaseDataExchange implements DataExchange {

  private static final Object[] NO_DATA = new Object[0];

  private AccessPlan resultPlan;
  private AccessPlan parameterPlan;

  /**
   * Initializes the data exchange instance.
   *
   * @param properties
   */
  public void initialize(Map properties) {
    Object map = properties.get("map");
    if (map instanceof ParameterMap) {
      ParameterMap parameterMap = (ParameterMap) map;
      if (parameterMap != null) {
        ParameterMapping[] parameterMappings = parameterMap.getParameterMappings();
        String[] parameterPropNames = new String[parameterMappings.length];
        for (int i = 0; i < parameterPropNames.length; i++) {
          parameterPropNames[i] = parameterMappings[i].getPropertyName();
        }
        parameterPlan = AccessPlanFactory.getAccessPlan(parameterMap.getParameterClass(), parameterPropNames);
      }
    } else if (map instanceof ResultMap) {
      ResultMap resultMap = (ResultMap) map;
      if (resultMap != null) {
        ResultMapping[] resultMappings = resultMap.getResultMappings();
        String[] resultPropNames = new String[resultMappings.length];
        for (int i = 0; i < resultPropNames.length; i++) {
          resultPropNames[i] = resultMappings[i].getPropertyName();
        }
        resultPlan = AccessPlanFactory.getAccessPlan(resultMap.getResultClass(), resultPropNames);
      }
    }
  }

  public Object[] getData(RequestScope request, ParameterMap parameterMap, Object parameterObject) {
    if (parameterPlan != null) {
      return parameterPlan.getProperties(parameterObject);
    } else {
      return NO_DATA;
    }
  }

  public Object setData(RequestScope request, ResultMap resultMap, Object resultObject, Object[] values) {
    if (resultPlan != null) {
      Object object = resultObject;
      
      ErrorContext errorContext = request.getErrorContext();
      
      if (object == null) {
        errorContext.setMoreInfo("The error occured while instantiating the result object");
        try {
          object = Resources.instantiate(resultMap.getResultClass());
        } catch (Exception e) {
          throw new NestedRuntimeException("JavaBeansDataExchange could not instantiate result class.  Cause: " + e, e);
        }
      }
      errorContext.setMoreInfo("The error happened while setting a property on the result object.");
      resultPlan.setProperties(object, values);
      return object;
    } else {
      return null;
    }
  }

  public Object setData(RequestScope request, ParameterMap parameterMap, Object parameterObject, Object[] values) {
    if (parameterPlan != null) {
      Object object = parameterObject;
      if (object == null) {
        try {
          object = Resources.instantiate(parameterMap.getParameterClass());
        } catch (Exception e) {
          throw new NestedRuntimeException("JavaBeansDataExchange could not instantiate parameter class.  Cause: " + e, e);
        }
      }
      parameterPlan.setProperties(object, values);
      return object;
    } else {
      return null;
    }
  }

}
