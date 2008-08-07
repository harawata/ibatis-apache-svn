package org.apache.ibatis.jdbc;

import org.apache.ibatis.BaseDataTest;
import org.junit.*;

import java.sql.Connection;
import java.util.*;

public class SimpleDataSourceTest extends BaseDataTest {

  @Test
  public void shouldProperlyMaintainPoolOf3ActiveAnd2IdleConnections() throws Exception {
    SimpleDataSource ds = createSimpleDataSource(JPETSTORE_PROPERTIES);
    try {
      runScript(ds, JPETSTORE_DDL);
      ds.setJdbcDefaultAutoCommit(false);
      ds.setJdbcDriverProperties(new Properties() {
        {
          setProperty("username", "sa");
          setProperty("password", "");
        }
      });
      ds.setPoolMaximumActiveConnections(3);
      ds.setPoolMaximumIdleConnections(2);
      ds.setPoolMaximumCheckoutTime(10000);
      ds.setPoolPingConnectionsNotUsedFor(1);
      ds.setPoolPingEnabled(true);
      ds.setPoolPingQuery("SELECT * FROM PRODUCT");
      ds.setPoolTimeToWait(10000);
      ds.setLogWriter(null);
      List<Connection> connections = new ArrayList<Connection>();
      for (int i = 0; i < 3; i++) {
        connections.add(ds.getConnection());
      }
      Assert.assertEquals(3, ds.getActiveConnectionCount());
      for (Connection c : connections) {
        c.close();
      }
      Assert.assertEquals(2, ds.getIdleConnectionCount());
      Assert.assertEquals(4, ds.getRequestCount());
      Assert.assertEquals(0, ds.getBadConnectionCount());
      Assert.assertEquals(0, ds.getHadToWaitCount());
      Assert.assertEquals(0, ds.getAverageOverdueCheckoutTime());
      Assert.assertEquals(0, ds.getClaimedOverdueConnectionCount());
      Assert.assertEquals(0, ds.getAverageWaitTime());
      Assert.assertNotNull(ds.getStatus());
    } finally {
      ds.forceCloseAll();
    }
  }

}
