package org.apache.ibatis.migration;

import java.io.*;
import java.sql.*;

public class ScriptRunner {

  private static final String DEFAULT_DELIMITER = ";";

  private Connection connection;

  private boolean stopOnError;
  private boolean autoCommit;

  private PrintWriter logWriter = new PrintWriter(System.out);
  private PrintWriter errorLogWriter = new PrintWriter(System.err);

  private String delimiter = DEFAULT_DELIMITER;
  private boolean fullLineDelimiter = false;

  public ScriptRunner(Connection connection) {
    this.connection = connection;
  }

  public void setStopOnError(boolean stopOnError) {
    this.stopOnError = stopOnError;
  }

  public void setAutoCommit(boolean autoCommit) {
    this.autoCommit = autoCommit;
  }

  public void setLogWriter(PrintWriter logWriter) {
    this.logWriter = logWriter;
  }

  public void setErrorLogWriter(PrintWriter errorLogWriter) {
    this.errorLogWriter = errorLogWriter;
  }

  public void setDelimiter(String delimiter) {
    this.delimiter = delimiter;
  }

  public void setFullLineDelimiter(boolean fullLineDelimiter) {
    this.fullLineDelimiter = fullLineDelimiter;
  }

  public void runScript(Reader reader) throws IOException, SQLException {
    try {
      runScriptWithConnection(connection, reader);
    } finally {
      flush();
    }
  }

  public void closeConnection() {
    try {
      connection.close();
    } catch(Exception e) {
      // ignore
    }
  }

  /**
   * Runs an SQL script (read in using the Reader parameter) using the connection passed in
   *
   * @param conn   - the connection to use for the script
   * @param reader - the source of the script
   * @throws java.sql.SQLException if any SQL errors occur
   * @throws java.io.IOException   if there is an error reading from the Reader
   */
  private void runScriptWithConnection(Connection conn, Reader reader)
      throws IOException, SQLException {
    StringBuffer command = null;
    try {
      BufferedReader lineReader = new BufferedReader(reader);
      String line;
      while ((line = lineReader.readLine()) != null) {
        if (command == null) {
          command = new StringBuffer();
        }
        String trimmedLine = line.trim();
        if (trimmedLine.length() < 1) {
          // do nothing
        } else if (trimmedLine.startsWith("//") || trimmedLine.startsWith("--")) {
          println(trimmedLine);
        } else if (!fullLineDelimiter && trimmedLine.endsWith(delimiter)
            || fullLineDelimiter && trimmedLine.equals(delimiter)) {
          command.append(line.substring(0, line.lastIndexOf(delimiter)));
          command.append(" ");
          Statement statement = conn.createStatement();

          println(command);

          boolean hasResults = false;
          if (stopOnError) {
            hasResults = statement.execute(command.toString());
          } else {
            try {
              hasResults = statement.execute(command.toString());
            } catch (SQLException e) {
              e.fillInStackTrace();
              printlnError("Error executing: " + command);
              printlnError(e);
            }
          }

          if (autoCommit && !conn.getAutoCommit()) {
            conn.commit();
          }

          if (hasResults) {
            ResultSet rs = statement.getResultSet();
            if (rs != null) {
              ResultSetMetaData md = rs.getMetaData();
              int cols = md.getColumnCount();
              for (int i = 0; i < cols; i++) {
                String name = md.getColumnLabel(i + 1);
                print(name + "\t");
              }
              println("");
              while (rs.next()) {
                for (int i = 0; i < cols; i++) {
                  String value = rs.getString(i + 1);
                  print(value + "\t");
                }
                println("");
              }
            }
          }

          command = null;
          try {
            statement.close();
          } catch (Exception e) {
            // Ignore to workaround a bug in Jakarta DBCP
          }
          Thread.yield();
        } else {
          command.append(line);
          command.append(" ");
        }
      }
      if (!autoCommit && !conn.getAutoCommit()) {
        conn.commit();
      }
    } catch (SQLException e) {
      e.fillInStackTrace();
      printlnError("Error executing: " + command);
      printlnError(e);
      throw e;
    } catch (IOException e) {
      e.fillInStackTrace();
      printlnError("Error executing: " + command);
      printlnError(e);
      throw e;
    } finally {
      conn.rollback();
      flush();
    }
  }

  private void print(Object o) {
    if (logWriter != null) {
      logWriter.print(o);
    }
  }

  private void println(Object o) {
    if (logWriter != null) {
      logWriter.println(o);
    }
  }

  private void printlnError(Object o) {
    if (errorLogWriter != null) {
      errorLogWriter.println(o);
    }
  }

  private void flush() {
    if (logWriter != null) {
      logWriter.flush();
    }
    if (errorLogWriter != null) {
      errorLogWriter.flush();
    }
  }

}
