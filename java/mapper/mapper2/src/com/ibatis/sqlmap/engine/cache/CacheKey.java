package com.ibatis.sqlmap.engine.cache;

import java.util.*;

/**
 *
 *
 * <p>
 * Date: Jan 18, 2004 8:24:01 AM
 * @author Clinton Begin
 */
public class CacheKey {

  private static final int DEFAULT_MULTIPLYER = 37;
  private static final int DEFAULT_HASHCODE = 17;

  private int multiplier;
  private int hashcode;
  private long checksum;
  private int count;

  public CacheKey() {
    hashcode = DEFAULT_HASHCODE;
    multiplier = DEFAULT_MULTIPLYER;
    count = 0;
  }

  public CacheKey(int initialNonZeroOddNumber) {
    hashcode = initialNonZeroOddNumber;
    multiplier = DEFAULT_MULTIPLYER;
    count = 0;
  }

  public CacheKey(int initialNonZeroOddNumber, int multiplierNonZeroOddNumber) {
    hashcode = initialNonZeroOddNumber;
    multiplier = multiplierNonZeroOddNumber;
    count = 0;
  }

  public CacheKey update (int x) {
    count++;
    x *= count;
    hashcode = multiplier * hashcode + (x ^ (x >>> 32));
    checksum += x;
    return this;
  }

  public CacheKey update (Object object) {
    update(object.hashCode());
    return this;
  }

  public boolean equals(Object object) {
    if (this == object) return true;
    if (!(object instanceof CacheKey)) return false;

    final CacheKey cacheKey = (CacheKey) object;

    if (hashcode != cacheKey.hashcode) return false;
    if (checksum != cacheKey.checksum) return false;

    return true;
  }

  public int hashCode() {
    return hashcode;
  }

}
