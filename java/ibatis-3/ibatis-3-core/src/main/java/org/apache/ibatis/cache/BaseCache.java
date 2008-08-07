package org.apache.ibatis.cache;

/**
 * Base Cache class implements a template method pattern for subclasses.
 */
public abstract class BaseCache implements Cache {

  protected String id;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public boolean equals(Object o) {
    if (getId() == null) throw new RuntimeException("Cache instances require an ID.");
    if (this == o) return true;
    if (!(o instanceof BaseCache)) return false;

    BaseCache baseCache = (BaseCache) o;

    if (!getId().equals(baseCache.getId())) return false;

    return true;
  }

  public int hashCode() {
    if (getId() == null) throw new RuntimeException("Cache instances require an ID.");
    return getId().hashCode();
  }
}