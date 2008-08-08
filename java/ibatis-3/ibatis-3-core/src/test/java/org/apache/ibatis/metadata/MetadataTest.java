package org.apache.ibatis.metadata;

import org.apache.ibatis.BaseDataTest;
import org.junit.Test;
import org.junit.Before;

import javax.sql.DataSource;

import junit.framework.Assert;

import java.sql.Types;

public class MetadataTest extends BaseDataTest {

  private DataSource dataSource;

  @Before
  public void setup() throws Exception {
    dataSource = createSimpleDataSource(BaseDataTest.BLOG_PROPERTIES);
  }

  @Test
  public void testShouldGetAllTableNames() throws Exception {
    Database db = DatabaseFactory.newDatabase(dataSource,null,"APP");
    Assert.assertNotNull(db.getTable("blog"));
    Assert.assertNotNull(db.getTable("Author"));
    Assert.assertNotNull(db.getTable("tAg"));
    Assert.assertNotNull(db.getTable("PosT"));
  }

  @Test
  public void testShouldEnsureDatabasesTablesAndColumnsEqualityWorks() throws Exception {
    Database db = DatabaseFactory.newDatabase(dataSource, null, null);
    Database db2 = DatabaseFactory.newDatabase(dataSource, null, null);
    Assert.assertNotNull(db);
    Assert.assertTrue(db.equals(db2));
    Assert.assertTrue(db.hashCode() == db.hashCode());
    Table t = db.getTable("blog");
    Table t2 = db.getTable("author");
    Assert.assertNotNull(t);
    Assert.assertNotNull(t2);
    Assert.assertFalse(t.equals(t2));
    Assert.assertTrue(t.getCatalog().equals(t.getCatalog()));
    Assert.assertTrue(t.getSchema().equals(t.getSchema()));
    Assert.assertTrue(t.equals(t));
    Assert.assertTrue(t.hashCode() == t.hashCode());
    Assert.assertEquals("BLOG",t.getName());
    Assert.assertEquals(3, t.getColumnNames().length);
    Column c = t.getColumn("author_id");
    Column c2 = t.getColumn("id");
    Assert.assertNotNull(c);
    Assert.assertNotNull(c2);
    Assert.assertEquals(Types.INTEGER, c.getType());
    Assert.assertFalse(c.equals(c2));
    Assert.assertTrue(c.equals(c));
    Assert.assertTrue(c.hashCode() == c.hashCode());
  }

}
