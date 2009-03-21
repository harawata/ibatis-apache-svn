package org.apache.ibatis.transaction.managed;

import static org.junit.Assert.*;
import org.junit.Test;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.BaseDataTest;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;

import java.util.Properties;
import java.sql.Connection;

public class ManagedTransactionFactoryTest extends BaseDataTest {

  protected Mockery mockery = new Mockery() {
    {
      setImposteriser(ClassImposteriser.INSTANCE);
    }
  };

  protected final Connection conn = mockery.mock(Connection.class);

  @Test
  public void shouldEnsureThatCallsToManagedTransactionAPIDoNotForwardToManagedConnections() throws Exception {
    TransactionFactory tf = new ManagedTransactionFactory();
    tf.setProperties(new Properties());
    Transaction tx = tf.newTransaction(conn, false);
    assertEquals(conn, tx.getConnection());
    tx.commit();
    tx.rollback();
    tx.close();
    mockery.assertIsSatisfied();
  }

}
