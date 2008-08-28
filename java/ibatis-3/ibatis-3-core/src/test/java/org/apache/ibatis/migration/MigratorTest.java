package org.apache.ibatis.migration;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.adhoc.AdHocExecutor;
import org.apache.ibatis.jdbc.SimpleDataSource;
import org.apache.ibatis.io.Resources;
import org.junit.*;

import java.io.*;
import java.net.URL;
import java.net.URISyntaxException;
import java.sql.Connection;

import junit.framework.Assert;

public class MigratorTest extends BaseDataTest {

  private static PrintStream out;
  private static StringOutputStream buffer;

  @BeforeClass
  public static void setup() throws Exception {
    out = System.out;
    buffer = new StringOutputStream();
    System.setOut(new PrintStream(buffer));

    SimpleDataSource ds = createSimpleDataSource(BLOG_PROPERTIES);
    Connection conn = ds.getConnection();
    AdHocExecutor executor = new AdHocExecutor(conn);
    safeRun(executor, "DROP TABLE comment");
    safeRun(executor, "DROP TABLE post_tag");
    safeRun(executor, "DROP TABLE tag");
    safeRun(executor, "DROP TABLE post");
    safeRun(executor, "DROP TABLE blog");
    safeRun(executor, "DROP TABLE author");
    safeRun(executor, "DROP PROCEDURE selectTwoSetsOfAuthors");
    safeRun(executor, "DROP PROCEDURE insertAuthor");
    safeRun(executor, "DROP PROCEDURE selectAuthorViaOutParams");
    safeRun(executor, "DROP TABLE changelog");
    conn.commit();
    conn.close();
  }

  @AfterClass
  public static void teardown() {
    System.setOut(out);
  }

  @Test
  public void shouldRunThroughFullMigrationUseCaseInOneTestToEnsureOrder() throws Exception {
    File f = getExampleDir();

    Migrator.main(args("--path="+f.getAbsolutePath(),"bootstrap", "--env=development"));
    Assert.assertTrue(buffer.toString().contains("--// Bootstrap.sql"));
    buffer.clear();

    Migrator.main(args("--path="+f.getAbsolutePath(),"status"));
    Assert.assertTrue(buffer.toString().contains("...pending..."));
    buffer.clear();

    Migrator.main(args("--path="+f.getAbsolutePath(),"up"));
    buffer.clear();

    Migrator.main(args("--path="+f.getAbsolutePath(),"status"));
    Assert.assertFalse(buffer.toString().contains("...pending..."));
    buffer.clear();

    Migrator.main(args("--path="+f.getAbsolutePath(),"down"));
    buffer.clear();

    Migrator.main(args("--path="+f.getAbsolutePath(),"status"));
    Assert.assertTrue(buffer.toString().contains("...pending..."));
    buffer.clear();

    Migrator.main(args("--path="+f.getAbsolutePath(),"version", "20080827200215"));
    buffer.clear();

    Migrator.main(args("--path="+f.getAbsolutePath(),"status"));
    Assert.assertFalse(buffer.toString().contains("...pending..."));
    buffer.clear();

    Migrator.main(args("--path="+f.getAbsolutePath(),"--help"));
    Assert.assertTrue(buffer.toString().contains("--help"));
    buffer.clear();


  }


  @Test
  public void shouldInitTempDirectory() throws Exception {
    File basePath = getTempDir();
    Migrator.main(args("--path="+basePath.getAbsolutePath(),"init"));
    Assert.assertNotNull(basePath.list());
    Assert.assertEquals(4,basePath.list().length);
    File scriptPath = new File(basePath.getCanonicalPath() + File.separator + "scripts");
    Assert.assertEquals(3,scriptPath.list().length);
    Migrator.main(args("--path="+basePath.getAbsolutePath(),"new","test new migration"));    
    Assert.assertEquals(4,scriptPath.list().length);
  }

  private String[] args(String... args) {
    return args;
  }

  private File getExampleDir() throws IOException, URISyntaxException {
    URL resourceURL = Resources.getResourceURL(getClass().getClassLoader(), "org/apache/ibatis/migration/example/");
    File f = new File(resourceURL.toURI());
    Assert.assertTrue(f.exists());
    Assert.assertTrue(f.isDirectory());
    return f;
  }

  private File getTempDir() throws IOException {
    File f = File.createTempFile("migration","test");
    Assert.assertTrue(f.delete());
    Assert.assertTrue(f.mkdir());
    Assert.assertTrue(f.exists());
    Assert.assertTrue(f.isDirectory());
    return f;
  }

  private static class StringOutputStream extends OutputStream {
    private StringBuilder builder = new StringBuilder();
    public void write(int b) throws IOException {
      builder.append((char)b);
//      out.write(b);
    }

    public String toString() {
      return builder.toString();
    }
    public void clear() {
      builder.setLength(0);
    }
  }

  private static void safeRun(AdHocExecutor executor, String sql) {
    try {
      executor.run(sql);
    } catch (Exception e) {
      //ignore
    }
  }
}
