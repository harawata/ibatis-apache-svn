package org.apache.ibatis.migration;


import javax.sql.DataSource;
import java.io.*;
import java.sql.*;

/**
 * Utility to run database scripts
 */
public class ScriptRunner {

  private static final String DEFAULT_DELIMITER = ";";

  private DataSource dataSource;
  private Connection connection;
  private String driver;
  private String url;
  private String username;
  private String password;

  private boolean stopOnError;
  private boolean autoCommit;

  private PrintWriter logWriter = new PrintWriter(System.out);
  private PrintWriter errorLogWriter = new PrintWriter(System.err);

  private String delimiter = DEFAULT_DELIMITER;
  private boolean fullLineDelimiter = false;

  public ScriptRunner(DataSource dataSource, boolean autoCommit, boolean stopOnError) {
    this.dataSource = dataSource;
    this.autoCommit = autoCommit;
    this.stopOnError = stopOnError;
  }

  public ScriptRunner(Connection connection, boolean autoCommit, boolean stopOnError) {
    this.connection = connection;
    this.autoCommit = autoCommit;
    this.stopOnError = stopOnError;
  }

  public ScriptRunner(String driver, String url, String username, String password, boolean autoCommit, boolean stopOnError) {
    this.driver = driver;
    this.url = url;
    this.username = username;
    this.password = password;
    this.autoCommit = autoCommit;
    this.stopOnError = stopOnError;
  }

  public void setDelimiter(String delimiter, boolean fullLineDelimiter) {
    this.delimiter = delimiter;
    this.fullLineDelimiter = fullLineDelimiter;
  }

  public void setLogWriter(PrintWriter logWriter) {
    this.logWriter = logWriter;
  }

  public void setErrorLogWriter(PrintWriter errorLogWriter) {
    this.errorLogWriter = errorLogWriter;
  }

  public void runScript(Reader reader) throws IOException, SQLException {
    try {
      if (dataSource != null) {
        connection = dataSource.getConnection();
        try {
          configureAutoCommitAndRun(reader);
        } finally {
          connection.close();
        }
      } else if (connection != null) {
        configureAutoCommitAndRun(reader);
      } else {
        Class driverType = Class.forName(driver);
        DriverManager.registerDriver((Driver) driverType.newInstance());
        connection = DriverManager.getConnection(url, username, password);
        try {
          configureAutoCommitAndRun(reader);
        } finally {
          try {
            connection.close();
          } finally {
            connection = null;
          }
        }
      }
    } catch (IOException e) {
      throw e;
    } catch (SQLException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException("Error running script.  Cause: " + e, e);
    }
  }

  private void configureAutoCommitAndRun(Reader reader) throws SQLException, IOException {
    boolean originalAutoCommit = connection.getAutoCommit();
    try {
      if (originalAutoCommit != this.autoCommit) {
        connection.setAutoCommit(this.autoCommit);
      }
      runScriptWithConnection(connection, reader);
    } finally {
      connection.setAutoCommit(originalAutoCommit);
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
      LineNumberReader lineReader = new LineNumberReader(reader);
      String line;
      while ((line = lineReader.readLine()) != null) {
        if (command == null) {
          command = new StringBuffer();
        }
        String trimmedLine = line.trim();
        if (trimmedLine.startsWith("--")) {
          println(trimmedLine);
        } else if (trimmedLine.length() < 1 || trimmedLine.startsWith("//")) {
          //Do nothing
        } else if (trimmedLine.length() < 1 || trimmedLine.startsWith("--")) {
          //Do nothing
        } else if (!fullLineDelimiter && trimmedLine.endsWith(getDelimiter())
            || fullLineDelimiter && trimmedLine.equals(getDelimiter())) {
          command.append(line.substring(0, line.lastIndexOf(getDelimiter())));
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

          ResultSet rs = statement.getResultSet();
          if (hasResults && rs != null) {
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
      if (!autoCommit) {
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

  private String getDelimiter() {
    return delimiter;
  }

  private void print(Object o) {
    if (logWriter != null) {
      System.out.print(o);
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