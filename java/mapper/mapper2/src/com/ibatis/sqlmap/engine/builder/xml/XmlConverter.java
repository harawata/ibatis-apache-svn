package com.ibatis.sqlmap.engine.builder.xml;

import java.io.*;

/**
 * User: Clinton Begin
 * Date: Dec 1, 2003
 * Time: 10:41:00 PM
 */
public interface XmlConverter {

  public Reader convertXml(Reader reader);

  public void convertXml(Reader reader, Writer writer);

}
