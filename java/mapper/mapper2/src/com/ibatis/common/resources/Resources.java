package com.ibatis.common.resources;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

/**
 * A class to simplify access to resources through the classloader.
 *
 * @author clinton_begin
 */
public class Resources extends Object {

  private static ClassLoader defaultClassLoader = Resources.class.getClassLoader();

  private Resources() {
  }

  public static void setDefaultClassLoader(ClassLoader loader) {
    if (loader == null) {
      Resources.defaultClassLoader = Resources.class.getClassLoader();
    } else {
      Resources.defaultClassLoader = loader;
    }
  }

  /**
   * Returns the URL of the resource on the classpath
   *
   * @param resource The resource to find
   * @return The resource
   * @throws IOException If the resource cannot be found or read
   */
  public static URL getResourceURL(String resource) throws IOException {
    URL url = null;
    ClassLoader loader = defaultClassLoader;
    if (loader != null) url = loader.getResource(resource);
    if (url == null) url = ClassLoader.getSystemResource(resource);
    if (url == null) throw new IOException("Could not find resource " + resource);
    return url;
  }

  /**
   * Returns the URL of the resource on the classpath
   *
   * @param loader   The classloader used to load the resource
   * @param resource The resource to find
   * @return The resource
   * @throws IOException If the resource cannot be found or read
   */
  public static URL getResourceURL(ClassLoader loader, String resource) throws IOException {
    URL url = null;
    if (loader != null) url = loader.getResource(resource);
    if (url == null) url = ClassLoader.getSystemResource(resource);
    if (url == null) throw new IOException("Could not find resource " + resource);
    return url;
  }

  /**
   * Returns a resource on the classpath as a Stream object
   *
   * @param resource The resource to find
   * @return The resource
   * @throws IOException If the resource cannot be found or read
   */
  public static InputStream getResourceAsStream(String resource) throws IOException {
    InputStream in = null;
    ClassLoader loader = defaultClassLoader;
    if (loader != null) in = loader.getResourceAsStream(resource);
    if (in == null) in = ClassLoader.getSystemResourceAsStream(resource);
    if (in == null) throw new IOException("Could not find resource " + resource);
    return in;
  }

  /**
   * Returns a resource on the classpath as a Stream object
   *
   * @param loader   The classloader used to load the resource
   * @param resource The resource to find
   * @return The resource
   * @throws IOException If the resource cannot be found or read
   */
  public static InputStream getResourceAsStream(ClassLoader loader, String resource) throws IOException {
    InputStream in = null;
    if (loader != null) in = loader.getResourceAsStream(resource);
    if (in == null) in = ClassLoader.getSystemResourceAsStream(resource);
    if (in == null) throw new IOException("Could not find resource " + resource);
    return in;
  }

  /**
   * Returns a resource on the classpath as a Properties object
   *
   * @param resource The resource to find
   * @return The resource
   * @throws IOException If the resource cannot be found or read
   */
  public static Properties getResourceAsProperties(String resource)
      throws IOException {
    Properties props = new Properties();
    InputStream in = null;
    String propfile = resource;
    in = getResourceAsStream(propfile);
    props.load(in);
    in.close();
    return props;
  }

  /**
   * Returns a resource on the classpath as a Properties object
   *
   * @param loader   The classloader used to load the resource
   * @param resource The resource to find
   * @return The resource
   * @throws IOException If the resource cannot be found or read
   */
  public static Properties getResourceAsProperties(ClassLoader loader, String resource)
      throws IOException {
    Properties props = new Properties();
    InputStream in = null;
    String propfile = resource;
    in = getResourceAsStream(loader, propfile);
    props.load(in);
    in.close();
    return props;
  }

  /**
   * Returns a resource on the classpath as a Reader object
   *
   * @param resource The resource to find
   * @return The resource
   * @throws IOException If the resource cannot be found or read
   */
  public static Reader getResourceAsReader(String resource) throws IOException {
    return new InputStreamReader(getResourceAsStream(resource));
  }

  /**
   * Returns a resource on the classpath as a Reader object
   *
   * @param loader   The classloader used to load the resource
   * @param resource The resource to find
   * @return The resource
   * @throws IOException If the resource cannot be found or read
   */
  public static Reader getResourceAsReader(ClassLoader loader, String resource) throws IOException {
    return new InputStreamReader(getResourceAsStream(loader, resource));
  }

  /**
   * Returns a resource on the classpath as a File object
   *
   * @param resource The resource to find
   * @return The resource
   * @throws IOException If the resource cannot be found or read
   */
  public static File getResourceAsFile(String resource) throws IOException {
    return new File(getResourceURL(resource).getFile());
  }

  /**
   * Returns a resource on the classpath as a File object
   *
   * @param loader   The classloader used to load the resource
   * @param resource The resource to find
   * @return The resource
   * @throws IOException If the resource cannot be found or read
   */
  public static File getResourceAsFile(ClassLoader loader, String resource) throws IOException {
    return new File(getResourceURL(loader, resource).getFile());
  }

  public static InputStream getUrlAsStream(String urlString) throws IOException {
    URL url = new URL(urlString);
    URLConnection conn = url.openConnection();
    return conn.getInputStream();
  }

  public static Reader getUrlAsReader(String urlString) throws IOException {
    return new InputStreamReader(getUrlAsStream(urlString));
  }

  public static Properties getUrlAsProperties(String urlString) throws IOException {
    Properties props = new Properties();
    InputStream in = null;
    String propfile = urlString;
    in = getUrlAsStream(propfile);
    props.load(in);
    in.close();
    return props;
  }

  public static Class classForName(String className) throws ClassNotFoundException {
    Class clazz = null;
    try {
      clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
    } catch (Exception e) {
      // Ignore.  Failsafe below.
    }
    if (clazz == null) {
      clazz = Class.forName(className);
    }
    return clazz;
  }

  public static Object instantiate(String className)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException {
    return instantiate(classForName(className));
  }

  public static Object instantiate(Class clazz)
      throws InstantiationException, IllegalAccessException {
    return clazz.newInstance();
  }

  public static void main(String[] args) {
    ClassLoaderResolver.getClassLoader(0);
  }

}
