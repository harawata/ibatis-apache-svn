package org.apache.ibatis.io;

import org.apache.ibatis.BaseDataTest;
import org.junit.*;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Properties;

public class ResourcesTest extends BaseDataTest {

  private static final ClassLoader CLASS_LOADER = ResourcesTest.class.getClassLoader();

  @Test
  public void shouldGetUrlForResource() throws Exception {
    URL url = Resources.getResourceURL(JPETSTORE_PROPERTIES);
    Assert.assertTrue(url.toString().endsWith("jpetstore/jpetstore-hsqldb.properties"));
  }

  @Test
  public void shouldGetUrlAsProperties() throws Exception {
    URL url = Resources.getResourceURL(CLASS_LOADER, JPETSTORE_PROPERTIES);
    Properties props = Resources.getUrlAsProperties(url.toString());
    Assert.assertNotNull(props.getProperty("driver"));
  }

  @Test
  public void shouldGetResourceAsProperties() throws Exception {
    Properties props = Resources.getResourceAsProperties(CLASS_LOADER, JPETSTORE_PROPERTIES);
    Assert.assertNotNull(props.getProperty("driver"));
  }

  @Test
  public void shouldGetUrlAsStream() throws Exception {
    URL url = Resources.getResourceURL(CLASS_LOADER, JPETSTORE_PROPERTIES);
    InputStream in = Resources.getUrlAsStream(url.toString());
    Assert.assertNotNull(in);
    in.close();
  }

  @Test
  public void shouldGetUrlAsReader() throws Exception {
    URL url = Resources.getResourceURL(CLASS_LOADER, JPETSTORE_PROPERTIES);
    Reader in = Resources.getUrlAsReader(url.toString());
    Assert.assertNotNull(in);
    in.close();
  }

  @Test
  public void shouldGetResourceAsStream() throws Exception {
    InputStream in = Resources.getResourceAsStream(CLASS_LOADER, JPETSTORE_PROPERTIES);
    Assert.assertNotNull(in);
    in.close();
  }

  @Test
  public void shouldGetResourceAsReader() throws Exception {
    Reader in = Resources.getResourceAsReader(CLASS_LOADER, JPETSTORE_PROPERTIES);
    Assert.assertNotNull(in);
    in.close();
  }

  @Test
  public void shouldGetResourceAsFile() throws Exception {
    File file = Resources.getResourceAsFile(JPETSTORE_PROPERTIES);
    Assert.assertTrue(file.toURL().toString().endsWith("jpetstore/jpetstore-hsqldb.properties"));
  }

  @Test
  public void shouldGetResourceAsFileWithClassloader() throws Exception {
    File file = Resources.getResourceAsFile(CLASS_LOADER, JPETSTORE_PROPERTIES);
    Assert.assertTrue(file.toURL().toString().endsWith("jpetstore/jpetstore-hsqldb.properties"));
  }

  @Test
  public void shouldAllowDefaultClassLoaderToBeSet() {
    Resources.setDefaultClassLoader(this.getClass().getClassLoader());
    Assert.assertEquals(this.getClass().getClassLoader(), Resources.getDefaultClassLoader());
  }

  @Test
  public void shouldAllowDefaultCharsetToBeSet() {
    Resources.setCharset(Charset.defaultCharset());
    Assert.assertEquals(Charset.defaultCharset(), Resources.getCharset());
  }

  @Test
  public void shouldGetClassForName() throws Exception {
    Class clazz = Resources.classForName(ResourcesTest.class.getName());
    Assert.assertNotNull(clazz);
  }


}