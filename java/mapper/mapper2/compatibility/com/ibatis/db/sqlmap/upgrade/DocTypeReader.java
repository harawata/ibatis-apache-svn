package com.ibatis.db.sqlmap.upgrade;

import java.io.*;

/**
 * This class strips the doctype from an XML stream
 * because java.xml.transform can't disable validation
 * and/or loading of the DTD in a standard way, which
 * causes problems for those running without a network.
 * <p>
 * Line terminators are converted to a \n
 * <p>
 * Date: Jan 17, 2004 6:30:40 AM
 * @author Clinton Begin
 */
public class DocTypeReader extends Reader {

  private Reader reader;
  private String docType;

  public DocTypeReader(Reader in) throws IOException {
    BufferedReader lineReader = new BufferedReader(in);
    StringBuffer buffer = new StringBuffer();
    StringBuffer docBuffer = new StringBuffer();
    String line = null;
    while ((line = lineReader.readLine()) != null) {
      if (line.indexOf("<!DOCTYPE") > -1) {
        docBuffer.append(line);
        while (line.indexOf(">") < 0) {
          line = lineReader.readLine();
          docBuffer.append(" ");
          docBuffer.append(line.trim());
        }
        line = lineReader.readLine();
      }
      buffer.append(line);
      buffer.append("\n");
    }
    reader = new StringReader(buffer.toString());
    docType = docBuffer.toString();
  }

  public String getDocType() {
    return docType;
  }

  public int read(char cbuf[], int off, int len) throws IOException {
    return reader.read (cbuf, off, len);
  }

  public void close() throws IOException {
    reader.close();
  }

}
