package org.apache.ibatis.migration;

import java.io.Reader;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.StringReader;

public class MigrationReader extends Reader {

  private Reader target;

  public MigrationReader(Reader source, boolean undo) throws IOException {
    try {
      BufferedReader reader = new BufferedReader(source);
      StringBuilder doBuilder = new StringBuilder();
      StringBuilder undoBuilder = new StringBuilder();
      StringBuilder currentBuilder = doBuilder;
      String line;
      while ((line = reader.readLine()) != null) {
        if (line != null) {
          if (line.trim().startsWith("--//@UNDO")) {
            currentBuilder = undoBuilder;
          }
          currentBuilder.append(line);
          currentBuilder.append("\n");
        }
      }
      if (undo) {
        target = new StringReader(undoBuilder.toString());
      } else {
        target = new StringReader(doBuilder.toString());
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
