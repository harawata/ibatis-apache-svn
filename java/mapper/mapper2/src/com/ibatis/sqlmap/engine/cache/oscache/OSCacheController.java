/**
 * User: Clinton Begin
 * Date: Aug 21, 2003
 * Time: 1:41:27 PM
 */
package com.ibatis.sqlmap.engine.cache.oscache;

import com.ibatis.sqlmap.engine.cache.CacheController;
import com.ibatis.sqlmap.engine.cache.CacheModel;
import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.general.GeneralCacheAdministrator;

import java.util.Properties;

/**
 * Cache implementation for using OSCache with iBATIS 
 */
public class OSCacheController implements CacheController {

    private static final GeneralCacheAdministrator CACHE = new GeneralCacheAdministrator();

    public void flush(CacheModel cacheModel) {
        synchronized (cacheModel) {
            CACHE.flushGroup(cacheModel.getId());
        }
    }

    public Object getObject(CacheModel cacheModel, Object key) {
        String keyString = key.toString();
        try {
            synchronized (cacheModel) {
                int refreshPeriod = (int) (cacheModel.getFlushIntervalSeconds());
                return CACHE.getFromCache(keyString, refreshPeriod);
            }
        } catch (NeedsRefreshException e) {
            CACHE.cancelUpdate(keyString);
            return null;
        }
    }

    public synchronized Object removeObject(CacheModel cacheModel, Object key) {
        Object result;
        String keyString = key.toString();
        try {
            synchronized (cacheModel) {
                int refreshPeriod = (int) (cacheModel.getFlushIntervalSeconds());
                Object value = CACHE.getFromCache(keyString, refreshPeriod);
                if (value != null) {
                    CACHE.flushEntry(keyString);
                }
                result = value;
            }
        } catch (NeedsRefreshException e) {
            try {
                CACHE.flushEntry(keyString);
            } finally {
                CACHE.cancelUpdate(keyString);
                result = null;
            }
        }
        return result;
    }

    public void putObject(CacheModel cacheModel, Object key, Object object) {
        String keyString = key.toString();
        synchronized (cacheModel) {
            CACHE.putInCache(keyString, object, new String[]{cacheModel.getId()});
        }
    }

    public void configure(Properties props) {
    }

}
