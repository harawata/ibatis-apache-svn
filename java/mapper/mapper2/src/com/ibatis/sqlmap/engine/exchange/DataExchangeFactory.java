package com.ibatis.sqlmap.engine.exchange;

import com.ibatis.sqlmap.engine.type.DomTypeMarker;
import com.ibatis.sqlmap.engine.type.TypeHandlerFactory;
import com.ibatis.sqlmap.engine.type.XmlTypeMarker;

import java.util.List;
import java.util.Map;

/**
 * User: Clinton Begin
 * Date: Nov 16, 2003
 * Time: 10:36:10 PM
 */
public class DataExchangeFactory {

  private final DataExchange domDataExchange;
  private final DataExchange xmlDataExchange;
  private final DataExchange listDataExchange;
  private final DataExchange mapDataExchange;
  private final DataExchange primitiveDataExchange;
  private final DataExchange complexDataExchange;

  private TypeHandlerFactory typeHandlerFactory;

  public DataExchangeFactory(TypeHandlerFactory typeHandlerFactory) {
    this.typeHandlerFactory = typeHandlerFactory;
    domDataExchange = new DomDataExchange(this);
    xmlDataExchange = new XmlDataExchange(this);
    listDataExchange = new ListDataExchange(this);
    mapDataExchange = new ComplexDataExchange(this);
    primitiveDataExchange = new PrimitiveDataExchange(this);
    complexDataExchange = new ComplexDataExchange(this);
  }

  public TypeHandlerFactory getTypeHandlerFactory() {
    return typeHandlerFactory;
  }

  public DataExchange getDataExchangeForClass(Class clazz) {
    DataExchange dataExchange = null;
    if (clazz == null) {
      dataExchange = complexDataExchange;
    } else if (DomTypeMarker.class.isAssignableFrom(clazz)) {
      dataExchange = domDataExchange;
    } else if (XmlTypeMarker.class.isAssignableFrom(clazz)) {
      dataExchange = xmlDataExchange;
    } else if (List.class.isAssignableFrom(clazz)) {
      dataExchange = listDataExchange;
    } else if (Map.class.isAssignableFrom(clazz)) {
      dataExchange = mapDataExchange;
    } else if (typeHandlerFactory.getTypeHandler(clazz) != null) {
      dataExchange = primitiveDataExchange;
    } else {
      dataExchange = new JavaBeanDataExchange(this);
    }
    return dataExchange;
  }

}
