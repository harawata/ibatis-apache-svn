package com.ibatis.sqlmap.engine.cache;

/**
 * Hash value generator for cache keys
 */
public class CacheKey {

  private static final int DEFAULT_MULTIPLYER = 37;
  private static final int DEFAULT_HASHCODE = 17;

  private int multiplier;
  private int hashcode;
  private long checksum;
  private int count;

  /**
   * Default constructor
   */
  public CacheKey() {
    hashcode = DEFAULT_HASHCODE;
    multiplier = DEFAULT_MULTIPLYER;
    count = 0;
  }

  /**
   * Costructor that supplies an initial hashcode
   * @param initialNonZeroOddNumber - the hashcode to use
   */
  public CacheKey(int initialNonZeroOddNumber) {
    hashcode = initialNonZeroOddNumber;
    multiplier = DEFAULT_MULTIPLYER;
    count = 0;
  }

  /**
   * Costructor that supplies an initial hashcode and multiplier
   * @param initialNonZeroOddNumber - the hashcode to use
   * @param multiplierNonZeroOddNumber - the multiplier to use
   */
  public CacheKey(int initialNonZeroOddNumber, int multiplierNonZeroOddNumber) {
    hashcode = initialNonZeroOddNumber;
    multiplier = multiplierNonZeroOddNumber;
    count = 0;
  }

  /**
   * Updates this object with new information based on an int value
   * @param x - the int value
   * @return the cache key
   */
public CacheKey update(int x) {
    count++;
    x *= count;
    hashcode = multiplier * hashcode + (x ^ (x >>> 32));
    checksum += x;
    return this;
  }

  /**
   * Updates this object with new information based on an object 
   * @param object - the object
   * @return the cachekey
   */
  public CacheKey update(Object object) {
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

  public String toString() {
    return new StringBuffer().append(hashcode).append('|').append(checksum).toString();
  }

}
