package com.ibatis.sqlmap.engine.exchange;

import com.ibatis.common.beans.Probe;
import com.ibatis.common.beans.ProbeFactory;
import com.ibatis.sqlmap.client.SqlMapException;
import com.ibatis.sqlmap.engine.mapping.parameter.ParameterMap;
import com.ibatis.sqlmap.engine.mapping.parameter.ParameterMapping;
import com.ibatis.sqlmap.engine.mapping.result.BasicResultMap;
import com.ibatis.sqlmap.engine.mapping.result.ResultMap;
import com.ibatis.sqlmap.engine.mapping.result.ResultMapping;
import com.ibatis.sqlmap.engine.scope.RequestScope;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.Map;

/**
 * <p/>
 * Date: May 2, 2004 4:56:42 PM
 * 
 * @author Clinton Begin
 */
public class DomDataExchange extends BaseDataExchange implements DataExchange {

  public DomDataExchange(DataExchangeFactory dataExchangeFactory) {
    super(dataExchangeFactory);
  }

  public void initialize(Map properties) {
  }

  public Object[] getData(RequestScope request, ParameterMap parameterMap, Object parameterObject) {
    Probe probe = ProbeFactory.getProbe(parameterObject);

    ParameterMapping[] mappings = parameterMap.getParameterMappings();
    Object[] values = new Object[mappings.length];

    for (int i = 0; i < mappings.length; i++) {
      values[i] = probe.getObject(parameterObject, mappings[i].getPropertyName());
    }

    return values;
  }

  public Object setData(RequestScope request, ResultMap resultMap, Object resultObject, Object[] values) {

    String name = ((BasicResultMap) resultMap).getXmlName();
    if (name == null) {
      name = "result";
    }

    if (resultObject == null) {
      try {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        doc.appendChild(doc.createElement(name));
        resultObject = doc;
      } catch (ParserConfigurationException e) {
        throw new SqlMapException("Error creating new Document for DOM result.  Cause: " + e, e);
      }
    }

    Probe probe = ProbeFactory.getProbe(resultObject);

    ResultMapping[] mappings = resultMap.getResultMappings();

    for (int i = 0; i < mappings.length; i++) {
      if (values[i] != null) {
        probe.setObject(resultObject, mappings[i].getPropertyName(), values[i]);
      }
    }

    return resultObject;
  }

  public Object setData(RequestScope request, ParameterMap parameterMap, Object parameterObject, Object[] values) {
    Probe probe = ProbeFactory.getProbe(parameterObject);

    ParameterMapping[] mappings = parameterMap.getParameterMappings();

    for (int i = 0; i < mappings.length; i++) {
      if (values[i] != null) {
        probe.setObject(parameterObject, mappings[i].getPropertyName(), values[i]);
      }
    }

    return parameterObject;
  }


}
