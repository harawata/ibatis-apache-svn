package com.ibatis.sqlmap.engine.exchange;

import com.ibatis.common.beans.Probe;
import com.ibatis.common.beans.ProbeFactory;
import com.ibatis.common.exception.NestedRuntimeException;
import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.engine.mapping.parameter.ParameterMap;
import com.ibatis.sqlmap.engine.mapping.parameter.ParameterMapping;
import com.ibatis.sqlmap.engine.mapping.result.ResultMap;
import com.ibatis.sqlmap.engine.mapping.result.ResultMapping;
import com.ibatis.sqlmap.engine.scope.RequestScope;
import com.ibatis.sqlmap.engine.type.TypeHandlerFactory;

import java.util.Map;

/**
 * User: Clinton Begin
 * Date: Nov 22, 2003
 * Time: 7:12:57 PM
 */
public class ComplexDataExchange extends BaseDataExchange implements DataExchange {

  private static final Probe PROBE = ProbeFactory.getProbe();

  public ComplexDataExchange(DataExchangeFactory dataExchangeFactory) {
    super(dataExchangeFactory);
  }

  public void initialize(Map properties) {
  }

  public Object[] getData(RequestScope request, ParameterMap parameterMap, Object parameterObject) {
    TypeHandlerFactory typeHandlerFactory = getDataExchangeFactory().getTypeHandlerFactory();
    if (parameterObject == null) {
      return new Object[0];
    } else {
      if (typeHandlerFactory.hasTypeHandler(parameterObject.getClass())) {
        ParameterMapping[] mappings = parameterMap.getParameterMappings();
        Object[] data = new Object[mappings.length];
        for (int i = 0; i < mappings.length; i++) {
          data[i] = parameterObject;
        }
        return data;
      } else {
        Object[] data = new Object[parameterMap.getParameterMappings().length];
        ParameterMapping[] mappings = parameterMap.getParameterMappings();
        for (int i = 0; i < mappings.length; i++) {
          data[i] = PROBE.getObject(parameterObject, mappings[i].getPropertyName());
        }
        return data;
      }
    }
  }

  public Object setData(RequestScope request, ResultMap resultMap, Object resultObject, Object[] values) {
    TypeHandlerFactory typeHandlerFactory = getDataExchangeFactory().getTypeHandlerFactory();
    if (typeHandlerFactory.hasTypeHandler(resultMap.getResultClass())) {
      return values[0];
    } else {
      Object object = resultObject;
      if (object == null) {
        try {
          object = Resources.instantiate(resultMap.getResultClass());
        } catch (Exception e) {
          throw new NestedRuntimeException("JavaBeansDataExchange could not instantiate result class.  Cause: " + e, e);
        }
      }
      ResultMapping[] mappings = resultMap.getResultMappings();
      for (int i = 0; i < mappings.length; i++) {
        PROBE.setObject(object, mappings[i].getPropertyName(), values[i]);
      }
      return object;
    }
  }

  public Object setData(RequestScope request, ParameterMap parameterMap, Object parameterObject, Object[] values) {
    TypeHandlerFactory typeHandlerFactory = getDataExchangeFactory().getTypeHandlerFactory();
    if (typeHandlerFactory.hasTypeHandler(parameterMap.getParameterClass())) {
      return values[0];
    } else {
      Object object = parameterObject;
      if (object == null) {
        try {
          object = Resources.instantiate(parameterMap.getParameterClass());
        } catch (Exception e) {
          throw new NestedRuntimeException("JavaBeansDataExchange could not instantiate result class.  Cause: " + e, e);
        }
      }
      ParameterMapping[] mappings = parameterMap.getParameterMappings();
      for (int i = 0; i < mappings.length; i++) {
        PROBE.setObject(object, mappings[i].getPropertyName(), values[i]);
      }
      return object;
    }
  }

}
