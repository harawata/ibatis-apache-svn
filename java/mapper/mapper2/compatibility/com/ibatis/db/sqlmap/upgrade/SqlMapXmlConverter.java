package com.ibatis.db.sqlmap.upgrade;

import com.ibatis.common.resources.*;
import com.ibatis.common.exception.*;
import com.ibatis.sqlmap.client.*;
import com.ibatis.sqlmap.engine.builder.xml.*;

import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import java.io.*;

/**
 * User: Clinton Begin
 * Date: Dec 1, 2003
 * Time: 10:42:18 PM
 */
public class SqlMapXmlConverter implements XmlConverter {

  public void convertXml(Reader reader, Writer writer) {
    try {
      DocTypeReader xml = new DocTypeReader(reader);
      String docType = xml.getDocType();
      Reader xsl = null;
      if (docType == null) {
        throw new SqlMapException ("Could not convert document because DOCTYPE was null.");
      } else {
        if (docType.indexOf("sql-map-config") > -1) {
          xsl = Resources.getResourceAsReader("com/ibatis/db/sqlmap/upgrade/SqlMapConfig.xsl");
        } else if (docType.indexOf("sql-map") > -1) {
          xsl = Resources.getResourceAsReader("com/ibatis/db/sqlmap/upgrade/SqlMap.xsl");
        } else {
          throw new SqlMapException ("Could not convert document because DOCTYPE was not recognized: " + docType);
        }
      }
      transformXml(xsl, xml, writer);
    } catch (IOException e) {
      throw new NestedRuntimeException("Error.  Cause: " + e, e);
    } catch (TransformerException e) {
      throw new NestedRuntimeException("Error.  Cause: " + e, e);
    }
  }

  public Reader convertXml(Reader reader) {
    StringWriter out = new StringWriter();
    convertXml(reader, out);
    return new StringReader(out.getBuffer().toString());
  }

  public void convertFile(String fromFileName, String toFileName) throws IOException {
    File fromFile = new File(fromFileName);
    File toFile = new File(toFileName);
    convertFile(fromFile, toFile);
  }

  public void convertFile(File fromFile, File toFile) throws IOException {
    Reader reader = new FileReader(fromFile);
    Writer writer = new FileWriter(toFile);
    convertXml(reader, writer);
    writer.flush();
    writer.close();
    reader.close();
  }

  protected void transformXml(Reader xslReader, Reader xmlReader, Writer xmlWriter) throws TransformerException {
    StreamSource xslSource = new StreamSource(xslReader);
    StreamSource xmlSource = new StreamSource(xmlReader);
    StreamResult xmlResult = new StreamResult(xmlWriter);

    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer(xslSource);
    transformer.setParameter("http://xml.org/sax/features/validation",new Boolean(false));
    transformer.setParameter("http://apache.org/xml/features/nonvalidating/load-dtd",new Boolean(false));

    transformer.transform(xmlSource, xmlResult);
  }

}

