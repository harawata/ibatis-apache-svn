/*
 *  Copyright 2004 Clinton Begin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.ibatis.sqlmap.engine.exchange;

import com.ibatis.sqlmap.engine.type.DomTypeMarker;
import com.ibatis.sqlmap.engine.type.TypeHandlerFactory;
import com.ibatis.sqlmap.engine.type.XmlTypeMarker;

import java.util.List;
import java.util.Map;

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
