package org.apache.ibatis.migration;

import java.io.*;
import java.util.Properties;

public class MigrationReader extends Reader {

  private Reader target;

  public MigrationReader(Reader source, boolean undo, Properties variables) throws IOException {
    try {
      BufferedReader reader = new BufferedReader(source);
      StringBuilder doBuilder = new StringBuilder();
      StringBuilder undoBuilder = new StringBuilder();
      StringBuilder currentBuilder = doBuilder;
      String line;
      while ((line = reader.readLine()) != null) {
        if (line != null) {
          if (line.trim().startsWith("--//") && line.contains("@UNDO")) {
            currentBuilder = undoBuilder;
          }
          currentBuilder.append(line);
          currentBuilder.append("\n");
        }
      }
      if (undo) {
        target = new StringReader(PropertyParser.parse(undoBuilder.toString(), variables));
      } else {
        target = new StringReader(PropertyParser.parse(doBuilder.toString(), variables));
      }
    } finally {
      source.close();
    }
  }

  public int read(char[] cbuf, int off, int len) throws IOException {
    return target.read(cbuf, off, len);
  }

  public void close() throws IOException {
    target.close();
  }

}