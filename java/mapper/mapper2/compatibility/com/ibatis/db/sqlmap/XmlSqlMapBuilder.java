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
package com.ibatis.db.sqlmap;

import com.ibatis.db.sqlmap.upgrade.SqlMapXmlConverter;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.engine.builder.xml.XmlConverter;
import com.ibatis.sqlmap.engine.builder.xml.XmlSqlMapClientBuilder;

import java.io.Reader;
import java.util.Properties;

public class XmlSqlMapBuilder {

  private static final XmlConverter SQL_MAP_CONVERTER = new SqlMapXmlConverter();

  private XmlSqlMapBuilder() {
  }

  public static SqlMap buildSqlMap(Reader reader) {
    XmlSqlMapClientBuilder builder = new XmlSqlMapClientBuilder();
    SqlMapClient client = builder.buildSqlMap(reader, SQL_MAP_CONVERTER, SQL_MAP_CONVERTER);
    return new SqlMap(client);
  }

  public static SqlMap buildSqlMap(Reader reader, Properties props) {
    XmlSqlMapClientBuilder builder = new XmlSqlMapClientBuilder();
    SqlMapClient client = builder.buildSqlMap(reader, props, SQL_MAP_CONVERTER, SQL_MAP_CONVERTER);
    return new SqlMap(client);
  }

}
