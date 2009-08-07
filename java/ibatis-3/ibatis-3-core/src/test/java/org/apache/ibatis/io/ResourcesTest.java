package org.apache.ibatis.io;

import org.apache.ibatis.BaseDataTest;
import static org.junit.Assert.*;
import org.junit.Test;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Properties;

import sun.nio.cs.US_ASCII;

public class ResourcesTest extends BaseDataTest {

  private static final ClassLoader CLASS_LOADER = ResourcesTest.class.getClassLoader();

  @Test
  public void shouldGetUrlForResource() throws Exception {
    URL url = Resources.getResourceURL(JPETSTORE_PROPERTIES);
    assertTrue(url.toString().endsWith("jpetstore/jpetstore-hsqldb.properties"));
  }

  @Test
  public void shouldGetUrlAsProperties() throws Exception {
    URL url = Resources.getResourceURL(CLASS_LOADER, JPETSTORE_PROPERTIES);
    Properties props = Resources.getUrlAsProperties(url.toString());
    assertNotNull(props.getProperty("driver"));
  }

  @Test
  public void shouldGetResourceAsProperties() throws Exception {
    Properties props = Resources.getResourceAsProperties(CLASS_LOADER, JPETSTORE_PROPERTIES);
    assertNotNull(props.getProperty("driver"));
  }

  @Test
  public void shouldGetUrlAsStream() throws Exception {
    URL url = Resources.getResourceURL(CLASS_LOADER, JPETSTORE_PROPERTIES);
    InputStream in = Resources.getUrlAsStream(url.toString());
    assertNotNull(in);
    in.close();
  }

  @Test
  public void shouldGetUrlAsReader() throws Exception {
    URL url = Resources.getResourceURL(CLASS_LOADER, JPETSTORE_PROPERTIES);
    Reader in = Resources.getUrlAsReader(url.toString());
    assertNotNull(in);
    in.close();
  }

  @Test
  public void shouldGetResourceAsStream() throws Exception {
    InputStream in = Resources.getResourceAsStream(CLASS_LOADER, JPETSTORE_PROPERTIES);
    assertNotNull(in);
    in.close();
  }

  @Test
  public void shouldGetResourceAsReader() throws Exception {
    Reader in = Resources.getResourceAsReader(CLASS_LOADER, JPETSTORE_PROPERTIES);
    assertNotNull(in);
    in.close();
  }

  @Test
  public void shouldGetResourceAsFile() throws Exception {
    File file = Resources.getResourceAsFile(JPETSTORE_PROPERTIES);
    assertTrue(file.getAbsolutePath().endsWith("jpetstore/jpetstore-hsqldb.properties"));
  }

  @Test
  public void shouldGetResourceAsFileWithClassloader() throws Exception {
    File file = Resources.getResourceAsFile(CLASS_LOADER, JPETSTORE_PROPERTIES);
    assertTrue(file.getAbsolutePath().endsWith("jpetstore/jpetstore-hsqldb.properties"));
  }

  @Test
  public void shouldGetResourceAsPropertiesWithOutClassloader() throws Exception {
    Properties file = Resources.getResourceAsProperties(JPETSTORE_PROPERTIES);
    assertNotNull(file);
  }

  @Test
  public void shouldGetResourceAsPropertiesWithClassloader() throws Exception {
    Properties file = Resources.getResourceAsProperties(CLASS_LOADER, JPETSTORE_PROPERTIES);
    assertNotNull(file);
  }

  @Test
  public void shouldAllowDefaultClassLoaderToBeSet() {
    Resources.setDefaultClassLoader(this.getClass().getClassLoader());
    assertEquals(this.getClass().getClassLoader(), Resources.getDefaultClassLoader());
  }

  @Test
  public void shouldAllowDefaultCharsetToBeSet() {
    Resources.setCharset(Charset.defaultCharset());
    assertEquals(Charset.defaultCharset(), Resources.getCharset());
  }

  @Test
  public void shouldGetClassForName() throws Exception {
    Class clazz = Resources.classForName(ResourcesTest.class.getName());
    assertNotNull(clazz);
  }

  @Test(expected = ClassNotFoundException.class)
  public void shouldNotFindThisClass() throws ClassNotFoundException {
    Resources.classForName("some.random.class.that.does.not.Exist");
  }

  @Test
  public void shouldGetReader() throws IOException {

    // save the value
    Charset charset = Resources.getCharset();

    // charset
    Resources.setCharset(new US_ASCII());
    assertNotNull(Resources.getResourceAsReader(JPETSTORE_PROPERTIES));

    // no charset
    Resources.setCharset(null);
    assertNotNull(Resources.getResourceAsReader(JPETSTORE_PROPERTIES));

    // clean up
    Resources.setCharset(charset);

  }

  @Test
  public void shouldGetReaderWithClassLoader() throws IOException {

    // save the value
    Charset charset = Resources.getCharset();

    // charset
    Resources.setCharset(new US_ASCII());
    assertNotNull(Resources.getResourceAsReader(getClass().getClassLoader(), JPETSTORE_PROPERTIES));

    // no charset
    Resources.setCharset(null);
    assertNotNull(Resources.getResourceAsReader(getClass().getClassLoader(), JPETSTORE_PROPERTIES));

    // clean up
    Resources.setCharset(charset);

  }
  
  @Test
  public void stupidJustForCoverage() {
    assertNotNull(new Resources());
  }
}
