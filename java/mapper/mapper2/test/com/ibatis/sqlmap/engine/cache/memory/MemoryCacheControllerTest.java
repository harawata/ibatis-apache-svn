/*
 * Created on Aug 31, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.ibatis.sqlmap.engine.cache.memory;

import com.ibatis.sqlmap.engine.cache.CacheController;
import com.ibatis.sqlmap.engine.cache.lru.LruCacheControllerTest;

/**
 * @author lmeadors
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MemoryCacheControllerTest extends LruCacheControllerTest {

  public static void main(String[] args) {
    junit.textui.TestRunner.run(MemoryCacheControllerTest.class);
  }
  protected CacheController getController(){
    return new MemoryCacheController();
  }

}
