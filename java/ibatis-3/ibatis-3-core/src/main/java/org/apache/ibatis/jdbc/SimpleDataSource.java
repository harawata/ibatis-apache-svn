package org.apache.ibatis.jdbc;

import org.apache.ibatis.logging.*;
import org.apache.ibatis.reflection.ExceptionUtil;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.*;
import java.sql.*;
import java.util.*;

/**
 * This is a simple, synchronous, thread-safe database connection pool.
 */
public class SimpleDataSource implements DataSource {

  private static final Log log = LogFactory.getLog(SimpleDataSource.class);

  private int expectedConnectionTypeCode;

  // FIELDS LOCKED BY POOL_LOCK
  private final Object POOL_LOCK = new Object();
  private List idleConnections = new ArrayList();
  private List activeConnections = new ArrayList();
  private long requestCount = 0;
  private long accumulatedRequestTime = 0;
  private long accumulatedCheckoutTime = 0;
  private long claimedOverdueConnectionCount = 0;
  private long accumulatedCheckoutTimeOfOverdueConnections = 0;
  private long accumulatedWaitTime = 0;
  private long hadToWaitCount = 0;
  private long badConnectionCount = 0;

  // REQUIRED CONFIGURATION FIELDS
  private String jdbcDriver;
  private String jdbcUrl;
  private String jdbcUsername;
  private String jdbcPassword;

  // OPTIONAL CONFIGURATION FIELDS
  private boolean jdbcDefaultAutoCommit = false;
  private Properties jdbcDriverProperties;
  private int poolMaximumActiveConnections = 10;
  private int poolMaximumIdleConnections = 5;
  private int poolMaximumCheckoutTime = 20000;
  private int poolTimeToWait = 20000;
  private String poolPingQuery = "NO PING QUERY SET";
  private boolean poolPingEnabled = false;
  private int poolPingConnectionsNotUsedFor = 0;

  /**
   * @see javax.sql.DataSource#getConnection()
   */
  public Connection getConnection() throws SQLException {
    return popConnection(jdbcUsername, jdbcPassword).getProxyConnection();
  }

  /**
   * @see javax.sql.DataSource#getConnection(String, String)
   */
  public Connection getConnection(String username, String password) throws SQLException {
    return popConnection(username, password).getProxyConnection();
  }

  /**
   * @see javax.sql.DataSource#setLoginTimeout(int)
   */
  public void setLoginTimeout(int loginTimeout) throws SQLException {
    DriverManager.setLoginTimeout(loginTimeout);
  }

  /**
   * @see javax.sql.DataSource#getLoginTimeout()
   */
  public int getLoginTimeout() throws SQLException {
    return DriverManager.getLoginTimeout();
  }

  /**
   * @see javax.sql.DataSource#setLogWriter(java.io.PrintWriter)
   */
  public void setLogWriter(PrintWriter logWriter) throws SQLException {
    DriverManager.setLogWriter(logWriter);
  }

  /**
   * @see javax.sql.DataSource#getLogWriter()
   */
  public PrintWriter getLogWriter() throws SQLException {
    return DriverManager.getLogWriter();
  }

  /**
   * Name of the JDBC driver class to be used.
   *
   * @param jdbcDriver The string name of the class
   */
  public void setJdbcDriver(String jdbcDriver) {
    this.jdbcDriver = jdbcDriver;
    try {
      Class driverType = Class.forName(jdbcDriver);
      DriverManager.registerDriver((Driver) driverType.newInstance());
    } catch (Exception e) {
      throw new RuntimeException("Error setting driver on SimpleDataSource. Cause: " + e, e);
    }
    forceCloseAll();
  }

  /**
   * The JDBC URL to use.
   *
   * @param jdbcUrl The JDBC URL
   */
  public void setJdbcUrl(String jdbcUrl) {
    this.jdbcUrl = jdbcUrl;
    forceCloseAll();
  }

  /**
   * The JDBC user name to use
   *
   * @param jdbcUsername The user name
   */
  public void setJdbcUsername(String jdbcUsername) {
    this.jdbcUsername = jdbcUsername;
    forceCloseAll();
  }

  /**
   * The JDBC password to use
   *
   * @param jdbcPassword The password
   */
  public void setJdbcPassword(String jdbcPassword) {
    this.jdbcPassword = jdbcPassword;
    forceCloseAll();
  }

  /**
   * Recommend leaving this false.  Basically disables transactions if set to true.
   *
   * @param jdbcDefaultAutoCommit
   */

  public void setJdbcDefaultAutoCommit(boolean jdbcDefaultAutoCommit) {
    this.jdbcDefaultAutoCommit = jdbcDefaultAutoCommit;
    forceCloseAll();
  }

  /**
   * Use alternate connection initializer with properties that may be specific
   * to your driver.
   *
   * @param driverProps The properties for your driver
   */
  public void setJdbcDriverProperties(Properties driverProps) {
    this.jdbcDriverProperties = driverProps;
    forceCloseAll();
  }

  /**
   * The maximum number of active connections
   *
   * @param poolMaximumActiveConnections The maximum number of active connections
   */
  public void setPoolMaximumActiveConnections(int poolMaximumActiveConnections) {
    this.poolMaximumActiveConnections = poolMaximumActiveConnections;
    forceCloseAll();
  }

  /**
   * The maximum number of idle connections
   *
   * @param poolMaximumIdleConnections The maximum number of idle connections
   */
  public void setPoolMaximumIdleConnections(int poolMaximumIdleConnections) {
    this.poolMaximumIdleConnections = poolMaximumIdleConnections;
    forceCloseAll();
  }

  /**
   * The maximum time a connection can be used before it *may* be
   * given away again.
   *
   * @param poolMaximumCheckoutTime The maximum time
   */
  public void setPoolMaximumCheckoutTime(int poolMaximumCheckoutTime) {
    this.poolMaximumCheckoutTime = poolMaximumCheckoutTime;
    forceCloseAll();
  }

  /**
   * The time to wait before retrying to get a connection
   *
   * @param poolTimeToWait The time to wait
   */
  public void setPoolTimeToWait(int poolTimeToWait) {
    this.poolTimeToWait = poolTimeToWait;
    forceCloseAll();
  }

  /**
   * The query to be used to check a connection
   *
   * @param poolPingQuery The query
   */
  public void setPoolPingQuery(String poolPingQuery) {
    this.poolPingQuery = poolPingQuery;
    forceCloseAll();
  }

  /**
   * Determines if the ping query should be used.
   *
   * @param poolPingEnabled True if we need to check a connection before using it
   */
  public void setPoolPingEnabled(boolean poolPingEnabled) {
    this.poolPingEnabled = poolPingEnabled;
    forceCloseAll();
  }

  /**
   * If a connection has not been used in this many milliseconds, ping the
   * database to make sure the connection is still good.
   *
   * @param milliseconds the number of milliseconds of inactivity that will trigger a ping
   */
  public void setPoolPingConnectionsNotUsedFor(int milliseconds) {
    this.poolPingConnectionsNotUsedFor = milliseconds;
    forceCloseAll();
  }

  public String getJdbcDriver() {
    return jdbcDriver;
  }

  public String getJdbcUrl() {
    return jdbcUrl;
  }

  public String getJdbcUsername() {
    return jdbcUsername;
  }

  public String getJdbcPassword() {
    return jdbcPassword;
  }

  public boolean isJdbcDefaultAutoCommit() {
    return jdbcDefaultAutoCommit;
  }

  public Properties getJdbcDriverProperties() {
    return jdbcDriverProperties;
  }

  public int getPoolMaximumActiveConnections() {
    return poolMaximumActiveConnections;
  }

  public int getPoolMaximumIdleConnections() {
    return poolMaximumIdleConnections;
  }

  public int getPoolMaximumCheckoutTime() {
    return poolMaximumCheckoutTime;
  }

  public int getPoolTimeToWait() {
    return poolTimeToWait;
  }

  public String getPoolPingQuery() {
    return poolPingQuery;
  }

  public boolean isPoolPingEnabled() {
    return poolPingEnabled;
  }

  public int getPoolPingConnectionsNotUsedFor() {
    return poolPingConnectionsNotUsedFor;
  }

  /**
   * Getter for the number of connection requests made
   *
   * @return The number of connection requests made
   */
  public long getRequestCount() {
    synchronized (POOL_LOCK) {
      return requestCount;
    }
  }

  /**
   * Getter for the average time required to get a connection to the database
   *
   * @return The average time
   */
  public long getAverageRequestTime() {
    synchronized (POOL_LOCK) {
      return requestCount == 0 ? 0 : accumulatedRequestTime / requestCount;
    }
  }

  /**
   * Getter for the average time spent waiting for connections that were in use
   *
   * @return The average time
   */
  public long getAverageWaitTime() {
    synchronized (POOL_LOCK) {
      return hadToWaitCount == 0 ? 0 : accumulatedWaitTime / hadToWaitCount;
    }
  }

  /**
   * Getter for the number of requests that had to wait for connections that were in use
   *
   * @return The number of requests that had to wait
   */
  public long getHadToWaitCount() {
    synchronized (POOL_LOCK) {
      return hadToWaitCount;
    }
  }

  /**
   * Getter for the number of invalid connections that were found in the pool
   *
   * @return The number of invalid connections
   */
  public long getBadConnectionCount() {
    synchronized (POOL_LOCK) {
      return badConnectionCount;
    }
  }

  /**
   * Getter for the number of connections that were claimed before they were returned
   *
   * @return The number of connections
   */
  public long getClaimedOverdueConnectionCount() {
    synchronized (POOL_LOCK) {
      return claimedOverdueConnectionCount;
    }
  }

  /**
   * Getter for the average age of overdue connections
   *
   * @return The average age
   */
  public long getAverageOverdueCheckoutTime() {
    synchronized (POOL_LOCK) {
      return claimedOverdueConnectionCount == 0 ? 0 : accumulatedCheckoutTimeOfOverdueConnections / claimedOverdueConnectionCount;
    }
  }


  /**
   * Getter for the average age of a connection checkout
   *
   * @return The average age
   */
  public long getAverageCheckoutTime() {
    synchronized (POOL_LOCK) {
      return requestCount == 0 ? 0 : accumulatedCheckoutTime / requestCount;
    }
  }


  public int getIdleConnectionCount() {
    synchronized (POOL_LOCK) {
      return idleConnections.size();
    }
  }

  public int getActiveConnectionCount() {
    synchronized (POOL_LOCK) {
      return activeConnections.size();
    }
  }


  /**
   * Returns the status of the connection pool
   *
   * @return The status
   */
  public String getStatus() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("\n===CONFINGURATION==============================================");
    buffer.append("\n jdbcDriver                     ").append(jdbcDriver);
    buffer.append("\n jdbcUrl                        ").append(jdbcUrl);
    buffer.append("\n jdbcUsername                   ").append(jdbcUsername);
    buffer.append("\n jdbcPassword                   ").append((jdbcPassword == null ? "NULL" : "************"));
    buffer.append("\n poolMaxActiveConnections       ").append(poolMaximumActiveConnections);
    buffer.append("\n poolMaxIdleConnections         ").append(poolMaximumIdleConnections);
    buffer.append("\n poolMaxCheckoutTime            ").append(poolMaximumCheckoutTime);
    buffer.append("\n poolTimeToWait                 ").append(poolTimeToWait);
    buffer.append("\n poolPingEnabled                ").append(poolPingEnabled);
    buffer.append("\n poolPingQuery                  ").append(poolPingQuery);
    buffer.append("\n poolPingConnectionsNotUsedFor  ").append(poolPingConnectionsNotUsedFor);
    buffer.append("\n ---STATUS-----------------------------------------------------");
    synchronized (POOL_LOCK) {
      buffer.append("\n activeConnections              ").append(getActiveConnectionCount());
      buffer.append("\n idleConnections                ").append(getIdleConnectionCount());
      buffer.append("\n requestCount                   ").append(getRequestCount());
      buffer.append("\n averageRequestTime             ").append(getAverageRequestTime());
      buffer.append("\n averageCheckoutTime            ").append(getAverageCheckoutTime());
      buffer.append("\n claimedOverdue                 ").append(getClaimedOverdueConnectionCount());
      buffer.append("\n averageOverdueCheckoutTime     ").append(getAverageOverdueCheckoutTime());
      buffer.append("\n hadToWait                      ").append(getHadToWaitCount());
      buffer.append("\n averageWaitTime                ").append(getAverageWaitTime());
      buffer.append("\n badConnectionCount             ").append(getBadConnectionCount());
    }
    buffer.append("\n===============================================================");
    return buffer.toString();
  }

  /**
   * Closes all active and idle connections in the pool
   */
  public void forceCloseAll() {
    synchronized (POOL_LOCK) {
      expectedConnectionTypeCode = assembleConnectionTypeCode(jdbcUrl, jdbcUsername, jdbcPassword);
      for (int i = activeConnections.size(); i > 0; i--) {
        try {
          SimplePooledConnection conn = (SimplePooledConnection) activeConnections.remove(i - 1);
          conn.invalidate();

          Connection realConn = conn.getRealConnection();
          if (!realConn.getAutoCommit()) {
            realConn.rollback();
          }
          realConn.close();
        } catch (Exception e) {
          // ignore
        }
      }
      for (int i = idleConnections.size(); i > 0; i--) {
        try {
          SimplePooledConnection conn = (SimplePooledConnection) idleConnections.remove(i - 1);
          conn.invalidate();

          Connection realConn = conn.getRealConnection();
          if (!realConn.getAutoCommit()) {
            realConn.rollback();
          }
          realConn.close();
        } catch (Exception e) {
          // ignore
        }
      }
    }
    if (log.isDebugEnabled()) {
      log.debug("SimpleDataSource forcefully closed/removed all connections.");
    }
  }

  private int assembleConnectionTypeCode(String url, String username, String password) {
    return ("" + url + username + password).hashCode();
  }

  private void pushConnection(SimplePooledConnection conn)
      throws SQLException {

    synchronized (POOL_LOCK) {
      activeConnections.remove(conn);
      if (conn.isValid()) {
        if (idleConnections.size() < poolMaximumIdleConnections && conn.getConnectionTypeCode() == expectedConnectionTypeCode) {
          accumulatedCheckoutTime += conn.getCheckoutTime();
          if (!conn.getRealConnection().getAutoCommit()) {
            conn.getRealConnection().rollback();
          }
          SimplePooledConnection newConn = new SimplePooledConnection(conn.getRealConnection(), this);
          idleConnections.add(newConn);
          newConn.setCreatedTimestamp(conn.getCreatedTimestamp());
          newConn.setLastUsedTimestamp(conn.getLastUsedTimestamp());
          conn.invalidate();
          if (log.isDebugEnabled()) {
            log.debug("Returned connection " + newConn.getRealHashCode() + " to pool.");
          }
          POOL_LOCK.notifyAll();
        } else {
          accumulatedCheckoutTime += conn.getCheckoutTime();
          if (!conn.getRealConnection().getAutoCommit()) {
            conn.getRealConnection().rollback();
          }
          conn.getRealConnection().close();
          if (log.isDebugEnabled()) {
            log.debug("Closed connection " + conn.getRealHashCode() + ".");
          }
          conn.invalidate();
        }
      } else {
        if (log.isDebugEnabled()) {
          log.debug("A bad connection (" + conn.getRealHashCode() + ") attempted to return to the pool, discarding connection.");
        }
        badConnectionCount++;
      }
    }
  }

  private SimplePooledConnection popConnection(String username, String password)
      throws SQLException {
    boolean countedWait = false;
    SimplePooledConnection conn = null;
    long t = System.currentTimeMillis();
    int localBadConnectionCount = 0;

    while (conn == null) {
      synchronized (POOL_LOCK) {
        if (idleConnections.size() > 0) {
          // Pool has available connection
          conn = (SimplePooledConnection) idleConnections.remove(0);
          if (log.isDebugEnabled()) {
            log.debug("Checked out connection " + conn.getRealHashCode() + " from pool.");
          }
        } else {
          // Pool does not have available connection
          if (activeConnections.size() < poolMaximumActiveConnections) {
            // Can create new connection
            if (jdbcDriverProperties != null) {
              jdbcDriverProperties.put("user", jdbcUsername);
              jdbcDriverProperties.put("password", jdbcPassword);
              conn = new SimplePooledConnection(DriverManager.getConnection(jdbcUrl, jdbcDriverProperties), this);
            } else {
              conn = new SimplePooledConnection(DriverManager.getConnection(jdbcUrl, jdbcUsername, jdbcPassword), this);
            }
            Connection realConn = conn.getRealConnection();
            if (realConn.getAutoCommit() != jdbcDefaultAutoCommit) {
              realConn.setAutoCommit(jdbcDefaultAutoCommit);
            }
            if (log.isDebugEnabled()) {
              log.debug("Created connection " + conn.getRealHashCode() + ".");
            }
          } else {
            // Cannot create new connection
            SimplePooledConnection oldestActiveConnection = (SimplePooledConnection) activeConnections.get(0);
            long longestCheckoutTime = oldestActiveConnection.getCheckoutTime();
            if (longestCheckoutTime > poolMaximumCheckoutTime) {
              // Can claim overdue connection
              claimedOverdueConnectionCount++;
              accumulatedCheckoutTimeOfOverdueConnections += longestCheckoutTime;
              accumulatedCheckoutTime += longestCheckoutTime;
              activeConnections.remove(oldestActiveConnection);
              if (!oldestActiveConnection.getRealConnection().getAutoCommit()) {
                oldestActiveConnection.getRealConnection().rollback();
              }
              conn = new SimplePooledConnection(oldestActiveConnection.getRealConnection(), this);
              oldestActiveConnection.invalidate();
              if (log.isDebugEnabled()) {
                log.debug("Claimed overdue connection " + conn.getRealHashCode() + ".");
              }
            } else {
              // Must wait
              try {
                if (!countedWait) {
                  hadToWaitCount++;
                  countedWait = true;
                }
                if (log.isDebugEnabled()) {
                  log.debug("Waiting as long as " + poolTimeToWait + " milliseconds for connection.");
                }
                long wt = System.currentTimeMillis();
                POOL_LOCK.wait(poolTimeToWait);
                accumulatedWaitTime += System.currentTimeMillis() - wt;
              } catch (InterruptedException e) {
                break;
              }
            }
          }
        }
        if (conn != null) {
          if (conn.isValid()) {
            if (!conn.getRealConnection().getAutoCommit()) {
              conn.getRealConnection().rollback();
            }
            conn.setConnectionTypeCode(assembleConnectionTypeCode(jdbcUrl, username, password));
            conn.setCheckoutTimestamp(System.currentTimeMillis());
            conn.setLastUsedTimestamp(System.currentTimeMillis());
            activeConnections.add(conn);
            requestCount++;
            accumulatedRequestTime += System.currentTimeMillis() - t;
          } else {
            if (log.isDebugEnabled()) {
              log.debug("A bad connection (" + conn.getRealHashCode() + ") was returned from the pool, getting another connection.");
            }
            badConnectionCount++;
            localBadConnectionCount++;
            conn = null;
            if (localBadConnectionCount > (poolMaximumIdleConnections + 3)) {
              if (log.isDebugEnabled()) {
                log.debug("SimpleDataSource: Could not get a good connection to the database.");
              }
              throw new SQLException("SimpleDataSource: Could not get a good connection to the database.");
            }
          }
        }
      }

    }

    if (conn == null) {
      if (log.isDebugEnabled()) {
        log.debug("SimpleDataSource: Unknown severe error condition.  The connection pool returned a null connection.");
      }
      throw new SQLException("SimpleDataSource: Unknown severe error condition.  The connection pool returned a null connection.");
    }

    return conn;
  }

  /**
   * Method to check to see if a connection is still usable
   *
   * @param conn - the connection to check
   * @return True if the connection is still usable
   */
  private boolean pingConnection(SimplePooledConnection conn) {
    boolean result = true;

    try {
      result = !conn.getRealConnection().isClosed();
    } catch (SQLException e) {
      if (log.isDebugEnabled()) {
        log.debug("Connection " + conn.getRealHashCode() + " is BAD: " + e.getMessage());
      }
      result = false;
    }

    if (result) {
      if (poolPingEnabled) {
        if (poolPingConnectionsNotUsedFor > 0 && conn.getTimeElapsedSinceLastUse() > poolPingConnectionsNotUsedFor) {
          try {
            if (log.isDebugEnabled()) {
              log.debug("Testing connection " + conn.getRealHashCode() + " ...");
            }
            Connection realConn = conn.getRealConnection();
            Statement statement = realConn.createStatement();
            ResultSet rs = statement.executeQuery(poolPingQuery);
            rs.close();
            statement.close();
            if (!realConn.getAutoCommit()) {
              realConn.rollback();
            }
            result = true;
            if (log.isDebugEnabled()) {
              log.debug("Connection " + conn.getRealHashCode() + " is GOOD!");
            }
          } catch (Exception e) {
            log.warn("Execution of ping query '" + poolPingQuery + "' failed: " + e.getMessage());
            try {
              conn.getRealConnection().close();
            } catch (Exception e2) {
              //ignore
            }
            result = false;
            if (log.isDebugEnabled()) {
              log.debug("Connection " + conn.getRealHashCode() + " is BAD: " + e.getMessage());
            }
          }
        }
      }
    }
    return result;
  }

  /**
   * Unwraps a pooled connection to get to the 'real' connection
   *
   * @param conn - the pooled connection to unwrap
   * @return The 'real' connection
   */
  public static Connection unwrapConnection(Connection conn) {
    if (conn instanceof SimplePooledConnection) {
      return ((SimplePooledConnection) conn).getRealConnection();
    } else {
      return conn;
    }
  }

  protected void finalize() throws Throwable {
    forceCloseAll();
  }

  /**
   * ---------------------------------------------------------------------------------------
   * SimplePooledConnection
   * ---------------------------------------------------------------------------------------
   */
  private static class SimplePooledConnection implements InvocationHandler {

    private static final String CLOSE = "close";
    private static final Class[] IFACES = new Class[]{Connection.class};

    private int hashCode = 0;
    private SimpleDataSource dataSource;
    private Connection realConnection;
    private Connection proxyConnection;
    private long checkoutTimestamp;
    private long createdTimestamp;
    private long lastUsedTimestamp;
    private int connectionTypeCode;
    private boolean valid;

    /**
     * Constructor for SimplePooledConnection that uses the Connection and SimpleDataSource passed in
     *
     * @param connection - the connection that is to be presented as a pooled connection
     * @param dataSource - the dataSource that the connection is from
     */
    public SimplePooledConnection(Connection connection, SimpleDataSource dataSource) {
      this.hashCode = connection.hashCode();
      this.realConnection = connection;
      this.dataSource = dataSource;
      this.createdTimestamp = System.currentTimeMillis();
      this.lastUsedTimestamp = System.currentTimeMillis();
      this.valid = true;

      proxyConnection = (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), IFACES, this);
    }

    /**
     * Invalidates the connection
     */
    public void invalidate() {
      valid = false;
    }

    /**
     * Method to see if the connection is usable
     *
     * @return True if the connection is usable
     */
    public boolean isValid() {
      return valid && realConnection != null && dataSource.pingConnection(this);
    }

    /**
     * Getter for the *real* connection that this wraps
     *
     * @return The connection
     */
    public Connection getRealConnection() {
      return realConnection;
    }

    /**
     * Getter for the proxy for the connection
     *
     * @return The proxy
     */
    public Connection getProxyConnection() {
      return proxyConnection;
    }

    /**
     * Gets the hashcode of the real connection (or 0 if it is null)
     *
     * @return The hashcode of the real connection (or 0 if it is null)
     */
    public int getRealHashCode() {
      if (realConnection == null) {
        return 0;
      } else {
        return realConnection.hashCode();
      }
    }

    /**
     * Getter for the connection type (based on url + user + password)
     *
     * @return The connection type
     */
    public int getConnectionTypeCode() {
      return connectionTypeCode;
    }

    /**
     * Setter for the connection type
     *
     * @param connectionTypeCode - the connection type
     */
    public void setConnectionTypeCode(int connectionTypeCode) {
      this.connectionTypeCode = connectionTypeCode;
    }

    /**
     * Getter for the time that the connection was created
     *
     * @return The creation timestamp
     */
    public long getCreatedTimestamp() {
      return createdTimestamp;
    }

    /**
     * Setter for the time that the connection was created
     *
     * @param createdTimestamp - the timestamp
     */
    public void setCreatedTimestamp(long createdTimestamp) {
      this.createdTimestamp = createdTimestamp;
    }

    /**
     * Getter for the time that the connection was last used
     *
     * @return - the timestamp
     */
    public long getLastUsedTimestamp() {
      return lastUsedTimestamp;
    }

    /**
     * Setter for the time that the connection was last used
     *
     * @param lastUsedTimestamp - the timestamp
     */
    public void setLastUsedTimestamp(long lastUsedTimestamp) {
      this.lastUsedTimestamp = lastUsedTimestamp;
    }

    /**
     * Getter for the time since this connection was last used
     *
     * @return - the time since the last use
     */
    public long getTimeElapsedSinceLastUse() {
      return System.currentTimeMillis() - lastUsedTimestamp;
    }

    /**
     * Getter for the age of the connection
     *
     * @return the age
     */
    public long getAge() {
      return System.currentTimeMillis() - createdTimestamp;
    }

    /**
     * Getter for the timestamp that this connection was checked out
     *
     * @return the timestamp
     */
    public long getCheckoutTimestamp() {
      return checkoutTimestamp;
    }

    /**
     * Setter for the timestamp that this connection was checked out
     *
     * @param timestamp the timestamp
     */
    public void setCheckoutTimestamp(long timestamp) {
      this.checkoutTimestamp = timestamp;
    }

    /**
     * Getter for the time that this connection has been checked out
     *
     * @return the time
     */
    public long getCheckoutTime() {
      return System.currentTimeMillis() - checkoutTimestamp;
    }

    private Connection getValidConnection() {
      if (!valid) {
        throw new RuntimeException("Error accessing SimplePooledConnection. Connection is invalid.");
      }
      return realConnection;
    }

    public int hashCode() {
      return hashCode;
    }

    /**
     * Allows comparing this connection to another
     *
     * @param obj - the other connection to test for equality
     * @see Object#equals(Object)
     */
    public boolean equals(Object obj) {
      if (obj instanceof SimplePooledConnection) {
        return realConnection.hashCode() == (((SimplePooledConnection) obj).realConnection.hashCode());
      } else if (obj instanceof Connection) {
        return hashCode == obj.hashCode();
      } else {
        return false;
      }
    }

    // **********************************
    // Implemented Connection Methods -- Now handled by proxy
    // **********************************

    /**
     * Required for InvocationHandler implementation.
     *
     * @param proxy  - not used
     * @param method - the method to be executed
     * @param args   - the parameters to be passed to the method
     * @see java.lang.reflect.InvocationHandler#invoke(Object, java.lang.reflect.Method, Object[])
     */
    public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable {
      String methodName = method.getName();
      if (CLOSE.hashCode() == methodName.hashCode() && CLOSE.equals(methodName)) {
        dataSource.pushConnection(this);
        return null;
      } else {
        try {
          return method.invoke(getValidConnection(), args);
        } catch (Throwable t) {
          throw ExceptionUtil.unwrapThrowable(t);
        }
      }
    }

    public Statement createStatement() throws SQLException {
      return getValidConnection().createStatement();
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
      return getValidConnection().prepareStatement(sql);
    }

    public CallableStatement prepareCall(String sql) throws SQLException {
      return getValidConnection().prepareCall(sql);
    }

    public String nativeSQL(String sql) throws SQLException {
      return getValidConnection().nativeSQL(sql);
    }

    public void setAutoCommit(boolean autoCommit) throws SQLException {
      getValidConnection().setAutoCommit(autoCommit);
    }

    public boolean getAutoCommit() throws SQLException {
      return getValidConnection().getAutoCommit();
    }

    public void commit() throws SQLException {
      getValidConnection().commit();
    }

    public void rollback() throws SQLException {
      getValidConnection().rollback();
    }

    public void close() throws SQLException {
      dataSource.pushConnection(this);
    }

    public boolean isClosed() throws SQLException {
      return getValidConnection().isClosed();
    }

    public DatabaseMetaData getMetaData() throws SQLException {
      return getValidConnection().getMetaData();
    }

    public void setReadOnly(boolean readOnly) throws SQLException {
      getValidConnection().setReadOnly(readOnly);
    }

    public boolean isReadOnly() throws SQLException {
      return getValidConnection().isReadOnly();
    }

    public void setCatalog(String catalog) throws SQLException {
      getValidConnection().setCatalog(catalog);
    }

    public String getCatalog() throws SQLException {
      return getValidConnection().getCatalog();
    }

    public void setTransactionIsolation(int level) throws SQLException {
      getValidConnection().setTransactionIsolation(level);
    }

    public int getTransactionIsolation() throws SQLException {
      return getValidConnection().getTransactionIsolation();
    }

    public SQLWarning getWarnings() throws SQLException {
      return getValidConnection().getWarnings();
    }

    public void clearWarnings() throws SQLException {
      getValidConnection().clearWarnings();
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
      return getValidConnection().createStatement(resultSetType, resultSetConcurrency);
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
      return getValidConnection().prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
      return getValidConnection().prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    public Map getTypeMap() throws SQLException {
      return getValidConnection().getTypeMap();
    }

    public void setTypeMap(Map map) throws SQLException {
      getValidConnection().setTypeMap(map);
    }

    // **********************************
    // JDK 1.4 JDBC 3.0 Methods below
    // **********************************

    public void setHoldability(int holdability) throws SQLException {
      getValidConnection().setHoldability(holdability);
    }

    public int getHoldability() throws SQLException {
      return getValidConnection().getHoldability();
    }

    public Savepoint setSavepoint() throws SQLException {
      return getValidConnection().setSavepoint();
    }

    public Savepoint setSavepoint(String name) throws SQLException {
      return getValidConnection().setSavepoint(name);
    }

    public void rollback(Savepoint savepoint) throws SQLException {
      getValidConnection().rollback(savepoint);
    }

    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
      getValidConnection().releaseSavepoint(savepoint);
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency,
                                     int resultSetHoldability) throws SQLException {
      return getValidConnection().createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType,
                                              int resultSetConcurrency, int resultSetHoldability)
        throws SQLException {
      return getValidConnection().prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public CallableStatement prepareCall(String sql, int resultSetType,
                                         int resultSetConcurrency,
                                         int resultSetHoldability) throws SQLException {
      return getValidConnection().prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
        throws SQLException {
      return getValidConnection().prepareStatement(sql, autoGeneratedKeys);
    }

    public PreparedStatement prepareStatement(String sql, int columnIndexes[])
        throws SQLException {
      return getValidConnection().prepareStatement(sql, columnIndexes);
    }

    public PreparedStatement prepareStatement(String sql, String columnNames[])
        throws SQLException {
      return getValidConnection().prepareStatement(sql, columnNames);
    }


  }
}
