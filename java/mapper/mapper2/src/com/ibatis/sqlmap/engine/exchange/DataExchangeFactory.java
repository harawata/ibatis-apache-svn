package com.ibatis.sqlmap.engine.exchange;

import com.ibatis.sqlmap.engine.type.*;

import java.util.*;

/**
 * User: Clinton Begin
 * Date: Nov 16, 2003
 * Time: 10:36:10 PM
 */
public class DataExchangeFactory {

  private static final DataExchange XML_DATA_EXCHANGE = new XmlDataExchange();
  private static final DataExchange LIST_DATA_EXCHANGE = new ListDataExchange();
  private static final DataExchange MAP_DATA_EXCHANGE = new ComplexDataExchange();
  private static final DataExchange PRIMITIVE_DATA_EXCHANGE = new PrimitiveDataExchange();
  private static final DataExchange COMPLEX_DATA_EXCHANGE = new ComplexDataExchange();

  private DataExchangeFactory() {
  }

  public static DataExchange getDataExchangeForClass(Class clazz) {
    DataExchange dataExchange = null;
    if (clazz == null) {
      dataExchange = COMPLEX_DATA_EXCHANGE;
    } else if (XmlTypeMarker.class.isAssignableFrom(clazz)) {
      dataExchange = XML_DATA_EXCHANGE;
    } else if (List.class.isAssignableFrom(clazz)) {
      dataExchange = LIST_DATA_EXCHANGE;
    } else if (Map.class.isAssignableFrom(clazz)) {
      dataExchange = MAP_DATA_EXCHANGE;
    } else if (TypeHandlerFactory.getTypeHandler(clazz) != null) {
      dataExchange = PRIMITIVE_DATA_EXCHANGE;
    } else {
      dataExchange = new JavaBeanDataExchange();
    }
    return dataExchange;
  }

}
