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
package com.ibatis.sqlmap.engine.builder.xml;

import java.io.Reader;
import java.io.Writer;

/**
 * User: Clinton Begin
 * Date: Dec 1, 2003
 * Time: 10:41:00 PM
 */
public interface XmlConverter {

  public Reader convertXml(Reader reader);

  public void convertXml(Reader reader, Writer writer);

}
