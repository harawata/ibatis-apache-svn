package com.ibatis.sqlmap.engine.cache;

import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.engine.mapping.statement.ExecuteListener;
import com.ibatis.sqlmap.engine.mapping.statement.MappedStatement;
import net.sf.hibernate.exception.NestableRuntimeException;

import java.io.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

public class CacheModel implements ExecuteListener {

  //private static final String CTX_LOCKED_OBJECTS = "__CACHE_MODEL_LOCKED_OBJECTS";

  private Object STATS_LOCK = new Object();
  private int requests = 0;
  private int hits = 0;

  /**
   * Constant to turn off periodic cache flushes
   */
  private static final long NO_FLUSH_INTERVAL = -99999;

  private String id;

  private boolean readOnly;
  private boolean serialize;

  private long lastFlush;
  private long flushInterval;
  private long flushIntervalSeconds;
  private Set flushTriggerStatements;

  private CacheController controller;

  private String resource;

  public CacheModel() {
    this.flushInterval = NO_FLUSH_INTERVAL;
    this.flushIntervalSeconds = NO_FLUSH_INTERVAL;
    this.lastFlush = System.currentTimeMillis();
    this.flushTriggerStatements = new HashSet();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public boolean isReadOnly() {
    return readOnly;
  }

  public void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
  }

  public boolean isSerialize() {
    return serialize;
  }

  public void setSerialize(boolean serialize) {
    this.serialize = serialize;
  }

  public String getResource() {
    return resource;
  }

  public void setResource(String resource) {
    this.resource = resource;
  }

  public void setControllerClassName(String implementation)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException {
    Class clazz = Resources.classForName(implementation);
    controller = (CacheController) Resources.instantiate(clazz);
  }

  /**
   * Getter for flushInterval property
   *
   * @return The flushInterval (in milliseconds)
   */
  public long getFlushInterval() {
    return flushInterval;
  }

  /**
   * Getter for flushInterval property
   *
   * @return The flushInterval (in milliseconds)
   */
  public long getFlushIntervalSeconds() {
    return flushIntervalSeconds;
  }

  /**
   * Setter for flushInterval property
   *
   * @param flushInterval The new flushInterval (in milliseconds)
   */
  public void setFlushInterval(long flushInterval) {
    this.flushInterval = flushInterval;
    this.flushIntervalSeconds = flushInterval / 1000;
  }

  /**
   * Adds a flushTriggerStatment. When a flushTriggerStatment is executed, the
   * cache is flushed (cleared).
   *
   * @param statementName The statement to add.
   */
  public void addFlushTriggerStatement(String statementName) {
    flushTriggerStatements.add(statementName);
  }

  /**
   * Gets an Iterator containing all flushTriggerStatment objects for this cache.
   *
   * @return The Iterator
   */
  public Iterator getFlushTriggerStatementNames() {
    return flushTriggerStatements.iterator();
  }

  /**
   * ExecuteListener event.  This will be called by a MappedStatement
   * for which this cache is registered as a ExecuteListener.  It will
   * be called each time an executeXXXXXX method is called.  In the
   * case of the Cache class, it is registered in order to flush the
   * cache whenever a certain statement is executed.
   * (i.e. the flushOnExecute cache policy)
   *
   * @param statement The statement to execute
   */
  public void onExecuteStatement(MappedStatement statement) {
    flush();
  }

  public double getHitRatio() {
    return (double) hits / (double) requests;
  }

  /**
   * Configures the cache
   *
   * @param props
   */
  public void configure(Properties props) {
    controller.configure(props);
  }

  /**
   * Clears the cache
   */
  public void flush() {
    lastFlush = System.currentTimeMillis();
    controller.flush(this);
  }

  /**
   * Get an object out of the cache.
   * A side effect of this method is that is may clear the cache if it has not been
   * cleared in the flushInterval.
   *
   * @param key The key of the object to be returned
   * @return The cached object (or null)
   */
  public Object getObject(CacheKey key) {
    synchronized (this) {
      if (flushInterval != NO_FLUSH_INTERVAL
          && System.currentTimeMillis() - lastFlush > flushInterval) {
        flush();
      }
    }

    Object value = controller.getObject(this, key);

    if (serialize && !readOnly && value != null) {
      try {
        ByteArrayInputStream bis = new ByteArrayInputStream((byte[]) value);
        ObjectInputStream ois = new ObjectInputStream(bis);
        value = ois.readObject();
        ois.close();
      } catch (Exception e) {
        throw new NestableRuntimeException("Error caching serializable object.  Cause: " + e, e);
      }
    }


    synchronized (STATS_LOCK) {
      requests++;
      if (value != null) {
        hits++;
      }
    }

    return value;
  }

  /**
   * Add an object to the cache
   *
   * @param key   The key of the object to be cached
   * @param value The object to be cached
   */
  public void putObject(CacheKey key, Object value) {
    if (serialize && !readOnly && value != null) {
      try {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject((Serializable) value);
        oos.flush();
        oos.close();
        value = bos.toByteArray();
      } catch (IOException e) {
        throw new NestableRuntimeException("Error caching serializable object.  Cause: " + e, e);
      }
    }
    controller.putObject(this, key, value);
  }

}
