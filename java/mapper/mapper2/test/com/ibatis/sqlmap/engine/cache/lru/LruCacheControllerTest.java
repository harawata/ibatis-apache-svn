/*
 * Created on Aug 31, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.ibatis.sqlmap.engine.cache.lru;

import junit.framework.TestCase;

import com.ibatis.sqlmap.engine.cache.CacheController;
import com.ibatis.sqlmap.engine.cache.CacheModel;

/**
 * @author lmeadors
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class LruCacheControllerTest extends TestCase {

  public static void main(String[] args) {
    junit.textui.TestRunner.run(LruCacheControllerTest.class);
  }
  protected CacheController getController(){
    return new LruCacheController();
  }
  public void testGetAndPutObject() {
    CacheController cc = getController();
    String testKey = "testKey";
    String testVal = "testVal";
    
    assertEquals(cc.getObject(null, testKey), null);
    
    cc.putObject(null, testKey, testVal);
    assertEquals(cc.getObject(null, testKey), testVal);
    
    cc.putObject(null, testKey, null);
    assertEquals(cc.getObject(null, testKey), null);
    
  }

  public void testRemoveObject() {
    CacheController cc = getController();
    String testKey = "testKey";
    String testVal = "testVal";
    
    assertEquals(cc.getObject(null, testKey), null);
    
    cc.putObject(null, testKey, testVal);
    assertEquals(cc.getObject(null, testKey), testVal);
    
    cc.removeObject(null, testKey);
    assertEquals(cc.getObject(null, testKey), null);
  }

  public void testFlush() {
    CacheController cc = getController();
    String testKey = "testKey";
    String testVal = "testVal";
    
    assertEquals(cc.getObject(null, testKey), null);
    
    cc.putObject(null, testKey, testVal);
    assertEquals(cc.getObject(null, testKey), testVal);
    
    cc.flush(null);
    assertEquals(cc.getObject(null, testKey), null);
  }
  
}
