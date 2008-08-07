package javax.util;

import java.io.*;
import java.lang.ref.*;
import java.util.*;

/**
 * <H1>DISCLAIMER</H1>
 * <p>
 * The following is code copied from the JDK 1.6 runtime libraries (rt.jar).
 * Since Sun makes it practically impossible to implement anythin useful with
 * AbstractMap outside of the java.util package (think default scope), and
 * since they failed to provide a SoftHashMap, this was the most reliable way
 * to create a Soft Reference HashMap implementation that worked consistently
 * with WeakHashMap.
 * </p>
 * <p>Consider this approach our official request for a SoftHashMap.</p>
 * <p/>
 * A hashtable-based <tt>Map</tt> implementation with <em>weak keys</em>.
 * An entry in a <tt>SoftHashMap</tt> will automatically be removed when
 * its key is no longer in ordinary use.  More precisely, the presence of a
 * mapping for a given key will not prevent the key from being discarded by the
 * garbage collector, that is, made finalizable, finalized, and then reclaimed.
 * When a key has been discarded its entry is effectively removed from the map,
 * so this class behaves somewhat differently than other <tt>Map</tt>
 * implementations.
 * <p/>
 * <p> Both null values and the null key are supported. This class has
 * performance characteristics similar to those of the <tt>HashMap</tt>
 * class, and has the same efficiency parameters of <em>initial capacity</em>
 * and <em>fetch factor</em>.
 * <p/>
 * <p> Like most collection classes, this class is not synchronized.  A
 * synchronized <tt>SoftHashMap</tt> may be constructed using the
 * <tt>Collections.synchronizedMap</tt> method.
 * <p/>
 * <p> This class is intended primarily for use with key objects whose
 * <tt>equals</tt> methods test for object identity using the
 * <tt>==</tt> operator.  Once such a key is discarded it can never be
 * recreated, so it is impossible to do a lookup of that key in a
 * <tt>SoftHashMap</tt> at some later time and be surprised that its entry
 * has been removed.  This class will work perfectly well with key objects
 * whose <tt>equals</tt> methods are not based upon object identity, such
 * as <tt>String</tt> instances.  With such recreatable key objects,
 * however, the automatic removal of <tt>SoftHashMap</tt> entries whose
 * keys have been discarded may prove to be confusing.
 * <p/>
 * <p> The behavior of the <tt>SoftHashMap</tt> class depends in part upon
 * the actions of the garbage collector, so several familiar (though not
 * required) <tt>Map</tt> invariants do not hold for this class.  Because
 * the garbage collector may discard keys at any time, a
 * <tt>SoftHashMap</tt> may behave as though an unknown thread is silently
 * removing entries.  In particular, even if you synchronize on a
 * <tt>SoftHashMap</tt> instance and invoke none of its mutator methods, it
 * is possible for the <tt>size</tt> method to return smaller values over
 * time, for the <tt>isEmpty</tt> method to return <tt>false</tt> and
 * then <tt>true</tt>, for the <tt>containsKey</tt> method to return
 * <tt>true</tt> and later <tt>false</tt> for a given key, for the
 * <tt>get</tt> method to return a value for a given key but later return
 * <tt>null</tt>, for the <tt>put</tt> method to return
 * <tt>null</tt> and the <tt>remove</tt> method to return
 * <tt>false</tt> for a key that previously appeared to be in the map, and
 * for successive examinations of the key set, the value set, and the entry set
 * to yield successively smaller numbers of elements.
 * <p/>
 * <p> Each key object in a <tt>SoftHashMap</tt> is stored indirectly as
 * the referent of a weak reference.  Therefore a key will automatically be
 * removed only after the weak references to it, both inside and outside of the
 * map, have been cleared by the garbage collector.
 * <p/>
 * <p> <strong>Implementation note:</strong> The value objects in a
 * <tt>SoftHashMap</tt> are held by ordinary strong references.  Thus care
 * should be taken to ensure that value objects do not strongly refer to their
 * own keys, either directly or indirectly, since that will prevent the keys
 * from being discarded.  Note that a value object may refer indirectly to its
 * key via the <tt>SoftHashMap</tt> itself; that is, a value object may
 * strongly refer to some other key object whose associated value object, in
 * turn, strongly refers to the key of the first value object.  One way
 * to deal with this is to wrap values themselves within
 * <tt>SoftReferences</tt> before
 * inserting, as in: <tt>m.put(key, new SoftReference(value))</tt>,
 * and then unwrapping upon each <tt>get</tt>.
 * <p/>
 * <p>The iterators returned by all of this class's "collection view methods"
 * are <i>fail-fast</i>: if the map is structurally modified at any time after
 * the iterator is created, in any way except through the iterator's own
 * <tt>remove</tt> or <tt>add</tt> methods, the iterator will throw a
 * <tt>ConcurrentModificationException</tt>.  Thus, in the face of concurrent
 * modification, the iterator fails quickly and cleanly, rather than risking
 * arbitrary, non-deterministic behavior at an undetermined time in the
 * future.
 * <p/>
 * <p>Note that the fail-fast behavior of an iterator cannot be guaranteed
 * as it is, generally speaking, impossible to make any hard guarantees in the
 * presence of unsynchronized concurrent modification.  Fail-fast iterators
 * throw <tt>ConcurrentModificationException</tt> on a best-effort basis.
 * Therefore, it would be wrong to write a program that depended on this
 * exception for its correctness:  <i>the fail-fast behavior of iterators
 * should be used only to detect bugs.</i>
 * <p/>
 * <p>This class is a member of the
 * <a href="{@docRoot}/../guide/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @author Doug Lea
 * @author Josh Bloch
 * @author Mark Reinhold
 * @version 1.30, 02/19/04
 * @see java.util.HashMap
 * @see java.lang.ref.SoftReference
 * @since 1.2
 */
public class SoftHashMap<K, V>
    extends InernalAbstractMap<K, V>
    implements Map<K, V> {

  /**
   * The default initial capacity -- MUST be a power of two.
   */
  private static final int DEFAULT_INITIAL_CAPACITY = 16;

  /**
   * The maximum capacity, used if a higher value is implicitly specified
   * by either of the constructors with arguments.
   * MUST be a power of two <= 1<<30.
   */
  private static final int MAXIMUM_CAPACITY = 1 << 30;

  /**
   * The fetch fast used when none specified in constructor.
   */
  private static final float DEFAULT_LOAD_FACTOR = 0.75f;

  /**
   * The table, resized as necessary. Length MUST Always be a power of two.
   */
  private Entry[] table;

  /**
   * The number of key-value mappings contained in this weak hash map.
   */
  private int size;

  /**
   * The next size value at which to resize (capacity * fetch factor).
   */
  private int threshold;

  /**
   * The fetch factor for the hash table.
   */
  private final float loadFactor;

  /**
   * Reference queue for cleared SoftEntries
   */
  private final ReferenceQueue<K> queue = new ReferenceQueue<K>();

  /**
   * The number of times this HashMap has been structurally modified
   * Structural modifications are those that change the number of mappings in
   * the HashMap or otherwise modify its internal structure (e.g.,
   * rehash).  This field is used to make iterators on Collection-views of
   * the HashMap fail-fast.  (See ConcurrentModificationException).
   */
  private volatile int modCount;

  /**
   * Constructs a new, empty <tt>SoftHashMap</tt> with the given initial
   * capacity and the given fetch factor.
   *
   * @param initialCapacity The initial capacity of the <tt>SoftHashMap</tt>
   * @param loadFactor      The fetch factor of the <tt>SoftHashMap</tt>
   * @throws IllegalArgumentException If the initial capacity is negative,
   *                                  or if the fetch factor is nonpositive.
   */
  public SoftHashMap(int initialCapacity, float loadFactor) {
    if (initialCapacity < 0)
      throw new IllegalArgumentException("Illegal Initial Capacity: " +
          initialCapacity);
    if (initialCapacity > MAXIMUM_CAPACITY)
      initialCapacity = MAXIMUM_CAPACITY;

    if (loadFactor <= 0 || Float.isNaN(loadFactor))
      throw new IllegalArgumentException("Illegal Load factor: " +
          loadFactor);
    int capacity = 1;
    while (capacity < initialCapacity)
      capacity <<= 1;
    table = new Entry[capacity];
    this.loadFactor = loadFactor;
    threshold = (int) (capacity * loadFactor);
  }

  /**
   * Constructs a new, empty <tt>SoftHashMap</tt> with the given initial
   * capacity and the default fetch factor, which is <tt>0.75</tt>.
   *
   * @param initialCapacity The initial capacity of the <tt>SoftHashMap</tt>
   * @throws IllegalArgumentException If the initial capacity is negative.
   */
  public SoftHashMap(int initialCapacity) {
    this(initialCapacity, DEFAULT_LOAD_FACTOR);
  }

  /**
   * Constructs a new, empty <tt>SoftHashMap</tt> with the default initial
   * capacity (16) and the default fetch factor (0.75).
   */
  public SoftHashMap() {
    this.loadFactor = DEFAULT_LOAD_FACTOR;
    threshold = (int) (DEFAULT_INITIAL_CAPACITY);
    table = new Entry[DEFAULT_INITIAL_CAPACITY];
  }

  /**
   * Constructs a new <tt>SoftHashMap</tt> with the same mappings as the
   * specified <tt>Map</tt>.  The <tt>SoftHashMap</tt> is created with
   * default fetch factor, which is <tt>0.75</tt> and an initial capacity
   * sufficient to hold the mappings in the specified <tt>Map</tt>.
   *
   * @param t the map whose mappings are to be placed in this map.
   * @throws NullPointerException if the specified map is null.
   * @since 1.3
   */
  public SoftHashMap(Map<? extends K, ? extends V> t) {
    this(Math.max((int) (t.size() / DEFAULT_LOAD_FACTOR) + 1, 16),
        DEFAULT_LOAD_FACTOR);
    putAll(t);
  }

  // internal utilities

  /**
   * Value representing null keys inside tables.
   */
  private static final Object NULL_KEY = new Object();

  /**
   * Use NULL_KEY for key if it is null.
   */
  private static Object maskNull(Object key) {
    return (key == null ? NULL_KEY : key);
  }

  /**
   * Return internal representation of null key back to caller as null
   */
  private static <K> K unmaskNull(Object key) {
    return (K) (key == NULL_KEY ? null : key);
  }

  /**
   * Check for equality of non-null reference x and possibly-null y.  By
   * default uses Object.equals.
   */
  static boolean eq(Object x, Object y) {
    return x == y || x.equals(y);
  }

  /**
   * Return index for hash code h.
   */
  static int indexFor(int h, int length) {
    return h & (length - 1);
  }

  /**
   * Expunge stale entries from the table.
   */
  private void expungeStaleEntries() {
    Entry<K, V> e;
    while ((e = (Entry<K, V>) queue.poll()) != null) {
      int h = e.hash;
      int i = indexFor(h, table.length);

      Entry<K, V> prev = table[i];
      Entry<K, V> p = prev;
      while (p != null) {
        Entry<K, V> next = p.next;
        if (p == e) {
          if (prev == e)
            table[i] = next;
          else
            prev.next = next;
          e.next = null;  // Help GC
          e.value = null; //  "   "
          size--;
          break;
        }
        prev = p;
        p = next;
      }
    }
  }

  /**
   * Return the table after first expunging stale entries
   */
  private Entry[] getTable() {
    expungeStaleEntries();
    return table;
  }

  /**
   * Returns the number of key-value mappings in this map.
   * This result is a snapshot, and may not reflect unprocessed
   * entries that will be removed before next attempted access
   * because they are no longer referenced.
   */
  public int size() {
    if (size == 0)
      return 0;
    expungeStaleEntries();
    return size;
  }

  /**
   * Returns <tt>true</tt> if this map contains no key-value mappings.
   * This result is a snapshot, and may not reflect unprocessed
   * entries that will be removed before next attempted access
   * because they are no longer referenced.
   */
  public boolean isEmpty() {
    return size() == 0;
  }

  /**
   * Returns the value to which the specified key is mapped in this weak
   * hash map, or <tt>null</tt> if the map contains no mapping for
   * this key.  A return value of <tt>null</tt> does not <i>necessarily</i>
   * indicate that the map contains no mapping for the key; it is also
   * possible that the map explicitly maps the key to <tt>null</tt>. The
   * <tt>containsKey</tt> method may be used to distinguish these two
   * cases.
   *
   * @param key the key whose associated value is to be returned.
   * @return the value to which this map maps the specified key, or
   *         <tt>null</tt> if the map contains no mapping for this key.
   * @see #put(Object, Object)
   */
  public V get(Object key) {
    Object k = maskNull(key);
    int h = InernalHashMap.hash(k);
    Entry[] tab = getTable();
    int index = indexFor(h, tab.length);
    Entry<K, V> e = tab[index];
    while (e != null) {
      if (e.hash == h && eq(k, e.get()))
        return e.value;
      e = e.next;
    }
    return null;
  }

  /**
   * Returns <tt>true</tt> if this map contains a mapping for the
   * specified key.
   *
   * @param key The key whose presence in this map is to be tested
   * @return <tt>true</tt> if there is a mapping for <tt>key</tt>;
   *         <tt>false</tt> otherwise
   */
  public boolean containsKey(Object key) {
    return getEntry(key) != null;
  }

  /**
   * Returns the entry associated with the specified key in the HashMap.
   * Returns null if the HashMap contains no mapping for this key.
   */
  Entry<K, V> getEntry(Object key) {
    Object k = maskNull(key);
    int h = InernalHashMap.hash(k);
    Entry[] tab = getTable();
    int index = indexFor(h, tab.length);
    Entry<K, V> e = tab[index];
    while (e != null && !(e.hash == h && eq(k, e.get())))
      e = e.next;
    return e;
  }

  /**
   * Associates the specified value with the specified key in this map.
   * If the map previously contained a mapping for this key, the old
   * value is replaced.
   *
   * @param key   key with which the specified value is to be associated.
   * @param value value to be associated with the specified key.
   * @return previous value associated with specified key, or <tt>null</tt>
   *         if there was no mapping for key.  A <tt>null</tt> return can
   *         also indicate that the HashMap previously associated
   *         <tt>null</tt> with the specified key.
   */
  public V put(K key, V value) {
    K k = (K) maskNull(key);
    int h = InernalHashMap.hash(k);
    Entry[] tab = getTable();
    int i = indexFor(h, tab.length);

    for (Entry<K, V> e = tab[i]; e != null; e = e.next) {
      if (h == e.hash && eq(k, e.get())) {
        V oldValue = e.value;
        if (value != oldValue)
          e.value = value;
        return oldValue;
      }
    }

    modCount++;
    Entry<K, V> e = tab[i];
    tab[i] = new Entry<K, V>(k, value, queue, h, e);
    if (++size >= threshold)
      resize(tab.length * 2);
    return null;
  }

  /**
   * Rehashes the contents of this map into a new array with a
   * larger capacity.  This method is called automatically when the
   * number of keys in this map reaches its threshold.
   * <p/>
   * If current capacity is MAXIMUM_CAPACITY, this method does not
   * resize the map, but sets threshold to Integer.MAX_VALUE.
   * This has the effect of preventing future calls.
   *
   * @param newCapacity the new capacity, MUST be a power of two;
   *                    must be greater than current capacity unless current
   *                    capacity is MAXIMUM_CAPACITY (in which case value
   *                    is irrelevant).
   */
  void resize(int newCapacity) {
    Entry[] oldTable = getTable();
    int oldCapacity = oldTable.length;
    if (oldCapacity == MAXIMUM_CAPACITY) {
      threshold = Integer.MAX_VALUE;
      return;
    }

    Entry[] newTable = new Entry[newCapacity];
    transfer(oldTable, newTable);
    table = newTable;

    /*
    * If ignoring null elements and processing ref queue caused massive
    * shrinkage, then restore old table.  This should be rare, but avoids
    * unbounded expansion of garbage-filled tables.
    */
    if (size >= threshold / 2) {
      threshold = (int) (newCapacity * loadFactor);
    } else {
      expungeStaleEntries();
      transfer(newTable, oldTable);
      table = oldTable;
    }
  }

  /**
   * Transfer all entries from src to dest tables
   */
  private void transfer(Entry[] src, Entry[] dest) {
    for (int j = 0; j < src.length; ++j) {
      Entry<K, V> e = src[j];
      src[j] = null;
      while (e != null) {
        Entry<K, V> next = e.next;
        Object key = e.get();
        if (key == null) {
          e.next = null;  // Help GC
          e.value = null; //  "   "
          size--;
        } else {
          int i = indexFor(e.hash, dest.length);
          e.next = dest[i];
          dest[i] = e;
        }
        e = next;
      }
    }
  }

  /**
   * Copies all of the mappings from the specified map to this map These
   * mappings will replace any mappings that this map had for any of the
   * keys currently in the specified map.<p>
   *
   * @param m mappings to be stored in this map.
   * @throws NullPointerException if the specified map is null.
   */
  public void putAll(Map<? extends K, ? extends V> m) {
    int numKeysToBeAdded = m.size();
    if (numKeysToBeAdded == 0)
      return;

    /*
    * Expand the map if the map if the number of mappings to be added
    * is greater than or equal to threshold.  This is conservative; the
    * obvious condition is (m.size() + size) >= threshold, but this
    * condition could result in a map with twice the appropriate capacity,
    * if the keys to be added overlap with the keys already in this map.
    * By using the conservative calculation, we subject ourself
    * to at most one extra resize.
    */
    if (numKeysToBeAdded > threshold) {
      int targetCapacity = (int) (numKeysToBeAdded / loadFactor + 1);
      if (targetCapacity > MAXIMUM_CAPACITY)
        targetCapacity = MAXIMUM_CAPACITY;
      int newCapacity = table.length;
      while (newCapacity < targetCapacity)
        newCapacity <<= 1;
      if (newCapacity > table.length)
        resize(newCapacity);
    }

    for (Iterator<? extends Map.Entry<? extends K, ? extends V>> i = m.entrySet().iterator(); i.hasNext();) {
      Map.Entry<? extends K, ? extends V> e = i.next();
      put(e.getKey(), e.getValue());
    }
  }

  /**
   * Removes the mapping for this key from this map if present.
   *
   * @param key key whose mapping is to be removed from the map.
   * @return previous value associated with specified key, or <tt>null</tt>
   *         if there was no mapping for key.  A <tt>null</tt> return can
   *         also indicate that the map previously associated <tt>null</tt>
   *         with the specified key.
   */
  public V remove(Object key) {
    Object k = maskNull(key);
    int h = InernalHashMap.hash(k);
    Entry[] tab = getTable();
    int i = indexFor(h, tab.length);
    Entry<K, V> prev = tab[i];
    Entry<K, V> e = prev;

    while (e != null) {
      Entry<K, V> next = e.next;
      if (h == e.hash && eq(k, e.get())) {
        modCount++;
        size--;
        if (prev == e)
          tab[i] = next;
        else
          prev.next = next;
        return e.value;
      }
      prev = e;
      e = next;
    }

    return null;
  }


  /**
   * Special version of remove needed by Entry set
   */
  Entry<K, V> removeMapping(Object o) {
    if (!(o instanceof Map.Entry))
      return null;
    Entry[] tab = getTable();
    Map.Entry entry = (Map.Entry) o;
    Object k = maskNull(entry.getKey());
    int h = InernalHashMap.hash(k);
    int i = indexFor(h, tab.length);
    Entry<K, V> prev = tab[i];
    Entry<K, V> e = prev;

    while (e != null) {
      Entry<K, V> next = e.next;
      if (h == e.hash && e.equals(entry)) {
        modCount++;
        size--;
        if (prev == e)
          tab[i] = next;
        else
          prev.next = next;
        return e;
      }
      prev = e;
      e = next;
    }

    return null;
  }

  /**
   * Removes all mappings from this map.
   */
  public void clear() {
    // clear out ref queue. We don't need to expunge entries
    // since table is getting cleared.
    while (queue.poll() != null)
      ;

    modCount++;
    Entry[] tab = table;
    for (int i = 0; i < tab.length; ++i)
      tab[i] = null;
    size = 0;

    // Allocation of array may have caused GC, which may have caused
    // additional entries to go stale.  Removing these entries from the
    // reference queue will make them eligible for reclamation.
    while (queue.poll() != null)
      ;
  }

  /**
   * Returns <tt>true</tt> if this map maps one or more keys to the
   * specified value.
   *
   * @param value value whose presence in this map is to be tested.
   * @return <tt>true</tt> if this map maps one or more keys to the
   *         specified value.
   */
  public boolean containsValue(Object value) {
    if (value == null)
      return containsNullValue();

    Entry[] tab = getTable();
    for (int i = tab.length; i-- > 0;)
      for (Entry e = tab[i]; e != null; e = e.next)
        if (value.equals(e.value))
          return true;
    return false;
  }

  /**
   * Special-case code for containsValue with null argument
   */
  private boolean containsNullValue() {
    Entry[] tab = getTable();
    for (int i = tab.length; i-- > 0;)
      for (Entry e = tab[i]; e != null; e = e.next)
        if (e.value == null)
          return true;
    return false;
  }

  /**
   * The entries in this hash table extend SoftReference, using its main ref
   * field as the key.
   */
  private static class Entry<K, V> extends SoftReference<K> implements Map.Entry<K, V> {
    private V value;
    private final int hash;
    private Entry<K, V> next;

    /**
     * Create new entry.
     */
    Entry(K key, V value,
          ReferenceQueue<K> queue,
          int hash, Entry<K, V> next) {
      super(key, queue);
      this.value = value;
      this.hash = hash;
      this.next = next;
    }

    public K getKey() {
      return SoftHashMap.<K>unmaskNull(get());
    }

    public V getValue() {
      return value;
    }

    public V setValue(V newValue) {
      V oldValue = value;
      value = newValue;
      return oldValue;
    }

    public boolean equals(Object o) {
      if (!(o instanceof Map.Entry))
        return false;
      Map.Entry e = (Map.Entry) o;
      Object k1 = getKey();
      Object k2 = e.getKey();
      if (k1 == k2 || (k1 != null && k1.equals(k2))) {
        Object v1 = getValue();
        Object v2 = e.getValue();
        if (v1 == v2 || (v1 != null && v1.equals(v2)))
          return true;
      }
      return false;
    }

    public int hashCode() {
      Object k = getKey();
      Object v = getValue();
      return ((k == null ? 0 : k.hashCode()) ^
          (v == null ? 0 : v.hashCode()));
    }

    public String toString() {
      return getKey() + "=" + getValue();
    }
  }

  private abstract class HashIterator<T> implements Iterator<T> {
    int index;
    Entry<K, V> entry = null;
    Entry<K, V> lastReturned = null;
    int expectedModCount = modCount;

    /**
     * Strong reference needed to avoid disappearance of key
     * between hasNext and next
     */
    Object nextKey = null;

    /**
     * Strong reference needed to avoid disappearance of key
     * between nextEntry() and any use of the entry
     */
    Object currentKey = null;

    HashIterator() {
      index = (size() != 0 ? table.length : 0);
    }

    public boolean hasNext() {
      Entry[] t = table;

      while (nextKey == null) {
        Entry<K, V> e = entry;
        int i = index;
        while (e == null && i > 0)
          e = t[--i];
        entry = e;
        index = i;
        if (e == null) {
          currentKey = null;
          return false;
        }
        nextKey = e.get(); // hold on to key in strong ref
        if (nextKey == null)
          entry = entry.next;
      }
      return true;
    }

    /**
     * The common parts of next() across different types of iterators
     */
    protected Entry<K, V> nextEntry() {
      if (modCount != expectedModCount)
        throw new ConcurrentModificationException();
      if (nextKey == null && !hasNext())
        throw new NoSuchElementException();

      lastReturned = entry;
      entry = entry.next;
      currentKey = nextKey;
      nextKey = null;
      return lastReturned;
    }

    public void remove() {
      if (lastReturned == null)
        throw new IllegalStateException();
      if (modCount != expectedModCount)
        throw new ConcurrentModificationException();

      SoftHashMap.this.remove(currentKey);
      expectedModCount = modCount;
      lastReturned = null;
      currentKey = null;
    }

  }

  private class ValueIterator extends HashIterator<V> {
    public V next() {
      return nextEntry().value;
    }
  }

  private class KeyIterator extends HashIterator<K> {
    public K next() {
      return nextEntry().getKey();
    }
  }

  private class EntryIterator extends HashIterator<Map.Entry<K, V>> {
    public Map.Entry<K, V> next() {
      return nextEntry();
    }
  }

  // Views

  private transient Set<Map.Entry<K, V>> entrySet = null;

  /**
   * Returns a set view of the keys contained in this map.  The set is
   * backed by the map, so changes to the map are reflected in the set, and
   * vice-versa.  The set supports element removal, which removes the
   * corresponding mapping from this map, via the <tt>Iterator.remove</tt>,
   * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt>, and
   * <tt>clear</tt> operations.  It does not support the <tt>add</tt> or
   * <tt>addAll</tt> operations.
   *
   * @return a set view of the keys contained in this map.
   */
  public Set<K> keySet() {
    Set<K> ks = keySet;
    return (ks != null ? ks : (keySet = new KeySet()));
  }

  private class KeySet extends AbstractSet<K> {
    public Iterator<K> iterator() {
      return new KeyIterator();
    }

    public int size() {
      return SoftHashMap.this.size();
    }

    public boolean contains(Object o) {
      return containsKey(o);
    }

    public boolean remove(Object o) {
      if (containsKey(o)) {
        SoftHashMap.this.remove(o);
        return true;
      } else
        return false;
    }

    public void clear() {
      SoftHashMap.this.clear();
    }

    public Object[] toArray() {
      Collection<K> c = new ArrayList<K>(size());
      for (Iterator<K> i = iterator(); i.hasNext();)
        c.add(i.next());
      return c.toArray();
    }

    public <T> T[] toArray(T[] a) {
      Collection<K> c = new ArrayList<K>(size());
      for (Iterator<K> i = iterator(); i.hasNext();)
        c.add(i.next());
      return c.toArray(a);
    }
  }

  /**
   * Returns a collection view of the values contained in this map.  The
   * collection is backed by the map, so changes to the map are reflected in
   * the collection, and vice-versa.  The collection supports element
   * removal, which removes the corresponding mapping from this map, via the
   * <tt>Iterator.remove</tt>, <tt>Collection.remove</tt>,
   * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt> operations.
   * It does not support the <tt>add</tt> or <tt>addAll</tt> operations.
   *
   * @return a collection view of the values contained in this map.
   */
  public Collection<V> values() {
    Collection<V> vs = values;
    return (vs != null ? vs : (values = new Values()));
  }

  private class Values extends AbstractCollection<V> {
    public Iterator<V> iterator() {
      return new ValueIterator();
    }

    public int size() {
      return SoftHashMap.this.size();
    }

    public boolean contains(Object o) {
      return containsValue(o);
    }

    public void clear() {
      SoftHashMap.this.clear();
    }

    public Object[] toArray() {
      Collection<V> c = new ArrayList<V>(size());
      for (Iterator<V> i = iterator(); i.hasNext();)
        c.add(i.next());
      return c.toArray();
    }

    public <T> T[] toArray(T[] a) {
      Collection<V> c = new ArrayList<V>(size());
      for (Iterator<V> i = iterator(); i.hasNext();)
        c.add(i.next());
      return c.toArray(a);
    }
  }

  /**
   * Returns a collection view of the mappings contained in this map.  Each
   * element in the returned collection is a <tt>Map.Entry</tt>.  The
   * collection is backed by the map, so changes to the map are reflected in
   * the collection, and vice-versa.  The collection supports element
   * removal, which removes the corresponding mapping from the map, via the
   * <tt>Iterator.remove</tt>, <tt>Collection.remove</tt>,
   * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt> operations.
   * It does not support the <tt>add</tt> or <tt>addAll</tt> operations.
   *
   * @return a collection view of the mappings contained in this map.
   * @see Map.Entry
   */
  public Set<Map.Entry<K, V>> entrySet() {
    Set<Map.Entry<K, V>> es = entrySet;
    return (es != null ? es : (entrySet = new EntrySet()));
  }

  private class EntrySet extends AbstractSet<Map.Entry<K, V>> {
    public Iterator<Map.Entry<K, V>> iterator() {
      return new EntryIterator();
    }

    public boolean contains(Object o) {
      if (!(o instanceof Map.Entry))
        return false;
      Map.Entry e = (Map.Entry) o;
      Object k = e.getKey();
      Entry candidate = getEntry(e.getKey());
      return candidate != null && candidate.equals(e);
    }

    public boolean remove(Object o) {
      return removeMapping(o) != null;
    }

    public int size() {
      return SoftHashMap.this.size();
    }

    public void clear() {
      SoftHashMap.this.clear();
    }

    public Object[] toArray() {
      Collection<Map.Entry<K, V>> c = new ArrayList<Map.Entry<K, V>>(size());
      for (Iterator<Map.Entry<K, V>> i = iterator(); i.hasNext();)
        c.add(new InernalAbstractMap.SimpleEntry<K, V>(i.next()));
      return c.toArray();
    }

    public <T> T[] toArray(T[] a) {
      Collection<Map.Entry<K, V>> c = new ArrayList<Map.Entry<K, V>>(size());
      for (Iterator<Map.Entry<K, V>> i = iterator(); i.hasNext();)
        c.add(new InernalAbstractMap.SimpleEntry<K, V>(i.next()));
      return c.toArray(a);
    }
  }
}

/*
* @(#)HashMap.java	1.68 06/06/27
*
* Copyright 2006 Sun Microsystems, Inc. All rights reserved.
* SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
*/

/**
 * Hash table based implementation of the <tt>Map</tt> interface.  This
 * implementation provides all of the optional map operations, and permits
 * <tt>null</tt> values and the <tt>null</tt> key.  (The <tt>HashMap</tt>
 * class is roughly equivalent to <tt>Hashtable</tt>, except that it is
 * unsynchronized and permits nulls.)  This class makes no guarantees as to
 * the order of the map; in particular, it does not guarantee that the order
 * will remain constant over time.
 * <p/>
 * <p>This implementation provides constant-time performance for the basic
 * operations (<tt>get</tt> and <tt>put</tt>), assuming the hash function
 * disperses the elements properly among the buckets.  Iteration over
 * collection views requires time proportional to the "capacity" of the
 * <tt>HashMap</tt> instance (the number of buckets) plus its size (the number
 * of key-value mappings).  Thus, it's very important not to set the initial
 * capacity too high (or the fetch factor too low) if iteration performance is
 * important.
 * <p/>
 * <p>An instance of <tt>HashMap</tt> has two parameters that affect its
 * performance: <i>initial capacity</i> and <i>fetch factor</i>.  The
 * <i>capacity</i> is the number of buckets in the hash table, and the initial
 * capacity is simply the capacity at the time the hash table is created.  The
 * <i>fetch factor</i> is a measure of how full the hash table is allowed to
 * get before its capacity is automatically increased.  When the number of
 * entries in the hash table exceeds the product of the fetch factor and the
 * current capacity, the capacity is roughly doubled by calling the
 * <tt>rehash</tt> method.
 * <p/>
 * <p>As a general rule, the default fetch factor (.75) offers a good tradeoff
 * between time and space costs.  Higher values decrease the space overhead
 * but increase the lookup cost (reflected in most of the operations of the
 * <tt>HashMap</tt> class, including <tt>get</tt> and <tt>put</tt>).  The
 * expected number of entries in the map and its fetch factor should be taken
 * into account when setting its initial capacity, so as to minimize the
 * number of <tt>rehash</tt> operations.  If the initial capacity is greater
 * than the maximum number of entries divided by the fetch factor, no
 * <tt>rehash</tt> operations will ever occur.
 * <p/>
 * <p>If many mappings are to be stored in a <tt>HashMap</tt> instance,
 * creating it with a sufficiently large capacity will allow the mappings to
 * be stored more efficiently than letting it perform automatic rehashing as
 * needed to grow the table.
 * <p/>
 * <p><b>Note that this implementation is not synchronized.</b> If multiple
 * threads access this map concurrently, and at least one of the threads
 * modifies the map structurally, it <i>must</i> be synchronized externally.
 * (A structural modification is any operation that adds or deletes one or
 * more mappings; merely changing the value associated with a key that an
 * instance already contains is not a structural modification.)  This is
 * typically accomplished by synchronizing on some object that naturally
 * encapsulates the map.  If no such object exists, the map should be
 * "wrapped" using the <tt>Collections.synchronizedMap</tt> method.  This is
 * best done at creation time, to prevent accidental unsynchronized access to
 * the map: <pre> Map m = Collections.synchronizedMap(new HashMap(...));
 * </pre>
 * <p/>
 * <p>The iterators returned by all of this class's "collection view methods"
 * are <i>fail-fast</i>: if the map is structurally modified at any time after
 * the iterator is created, in any way except through the iterator's own
 * <tt>remove</tt> or <tt>add</tt> methods, the iterator will throw a
 * <tt>ConcurrentModificationException</tt>.  Thus, in the face of concurrent
 * modification, the iterator fails quickly and cleanly, rather than risking
 * arbitrary, non-deterministic behavior at an undetermined time in the
 * future.
 * <p/>
 * <p>Note that the fail-fast behavior of an iterator cannot be guaranteed
 * as it is, generally speaking, impossible to make any hard guarantees in the
 * presence of unsynchronized concurrent modification.  Fail-fast iterators
 * throw <tt>ConcurrentModificationException</tt> on a best-effort basis.
 * Therefore, it would be wrong to write a program that depended on this
 * exception for its correctness: <i>the fail-fast behavior of iterators
 * should be used only to detect bugs.</i>
 * <p/>
 * <p>This class is a member of the
 * <a href="{@docRoot}/../guide/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @author Doug Lea
 * @author Josh Bloch
 * @author Arthur van Hoff
 * @author Neal Gafter
 * @version 1.65, 03/03/05
 * @see Map
 * @see TreeMap
 * @see Hashtable
 * @see Object#hashCode()
 * @see Collection
 * @since 1.2
 */

class InernalHashMap<K, V>
    extends InernalAbstractMap<K, V>
    implements Map<K, V>, Cloneable, Serializable {

  /**
   * The default initial capacity - MUST be a power of two.
   */
  static final int DEFAULT_INITIAL_CAPACITY = 16;

  /**
   * The maximum capacity, used if a higher value is implicitly specified
   * by either of the constructors with arguments.
   * MUST be a power of two <= 1<<30.
   */
  static final int MAXIMUM_CAPACITY = 1 << 30;

  /**
   * The fetch factor used when none specified in constructor.
   */
  static final float DEFAULT_LOAD_FACTOR = 0.75f;

  /**
   * The table, resized as necessary. Length MUST Always be a power of two.
   */
  transient Entry[] table;

  /**
   * The number of key-value mappings contained in this identity hash map.
   */
  transient int size;

  /**
   * The next size value at which to resize (capacity * fetch factor).
   *
   * @serial
   */
  int threshold;

  /**
   * The fetch factor for the hash table.
   *
   * @serial
   */
  final float loadFactor;

  /**
   * The number of times this HashMap has been structurally modified
   * Structural modifications are those that change the number of mappings in
   * the HashMap or otherwise modify its internal structure (e.g.,
   * rehash).  This field is used to make iterators on Collection-views of
   * the HashMap fail-fast.  (See ConcurrentModificationException).
   */
  transient volatile int modCount;

  /**
   * Constructs an empty <tt>HashMap</tt> with the specified initial
   * capacity and fetch factor.
   *
   * @param initialCapacity The initial capacity.
   * @param loadFactor      The fetch factor.
   * @throws IllegalArgumentException if the initial capacity is negative
   *                                  or the fetch factor is nonpositive.
   */
  public InernalHashMap(int initialCapacity, float loadFactor) {
    if (initialCapacity < 0)
      throw new IllegalArgumentException("Illegal initial capacity: " +
          initialCapacity);
    if (initialCapacity > MAXIMUM_CAPACITY)
      initialCapacity = MAXIMUM_CAPACITY;
    if (loadFactor <= 0 || Float.isNaN(loadFactor))
      throw new IllegalArgumentException("Illegal fetch factor: " +
          loadFactor);

    // Find a power of 2 >= initialCapacity
    int capacity = 1;
    while (capacity < initialCapacity)
      capacity <<= 1;

    this.loadFactor = loadFactor;
    threshold = (int) (capacity * loadFactor);
    table = new Entry[capacity];
    init();
  }

  /**
   * Constructs an empty <tt>HashMap</tt> with the specified initial
   * capacity and the default fetch factor (0.75).
   *
   * @param initialCapacity the initial capacity.
   * @throws IllegalArgumentException if the initial capacity is negative.
   */
  public InernalHashMap(int initialCapacity) {
    this(initialCapacity, DEFAULT_LOAD_FACTOR);
  }

  /**
   * Constructs an empty <tt>HashMap</tt> with the default initial capacity
   * (16) and the default fetch factor (0.75).
   */
  public InernalHashMap() {
    this.loadFactor = DEFAULT_LOAD_FACTOR;
    threshold = (int) (DEFAULT_INITIAL_CAPACITY * DEFAULT_LOAD_FACTOR);
    table = new Entry[DEFAULT_INITIAL_CAPACITY];
    init();
  }

  /**
   * Constructs a new <tt>HashMap</tt> with the same mappings as the
   * specified <tt>Map</tt>.  The <tt>HashMap</tt> is created with
   * default fetch factor (0.75) and an initial capacity sufficient to
   * hold the mappings in the specified <tt>Map</tt>.
   *
   * @param m the map whose mappings are to be placed in this map.
   * @throws NullPointerException if the specified map is null.
   */
  public InernalHashMap(Map<? extends K, ? extends V> m) {
    this(Math.max((int) (m.size() / DEFAULT_LOAD_FACTOR) + 1,
        DEFAULT_INITIAL_CAPACITY), DEFAULT_LOAD_FACTOR);
    putAllForCreate(m);
  }

  // internal utilities

  /**
   * Initialization hook for subclasses. This method is called
   * in all constructors and pseudo-constructors (clone, readObject)
   * after HashMap has been initialized but before any entries have
   * been inserted.  (In the absence of this method, readObject would
   * require explicit knowledge of subclasses.)
   */
  void init() {
  }

  /**
   * Value representing null keys inside tables.
   */
  static final Object NULL_KEY = new Object();

  /**
   * Returns internal representation for key. Use NULL_KEY if key is null.
   */
  static <T> T maskNull(T key) {
    return key == null ? (T) NULL_KEY : key;
  }

  /**
   * Returns key represented by specified internal representation.
   */
  static <T> T unmaskNull(T key) {
    return (key == NULL_KEY ? null : key);
  }

  /**
   * Whether to prefer the old supplemental hash function, for
   * compatibility with broken applications that rely on the
   * internal hashing order.
   * <p/>
   * Set to true only by hotspot when invoked via
   * -XX:+UseNewHashFunction or -XX:+AggressiveOpts
   */
  private static final boolean useNewHash;

  static {
    useNewHash = false;
  }

  private static int oldHash(int h) {
    h += ~(h << 9);
    h ^= (h >>> 14);
    h += (h << 4);
    h ^= (h >>> 10);
    return h;
  }

  private static int newHash(int h) {
    // This function ensures that hashCodes that differ only by
    // constant multiples at each bit position have a bounded
    // number of collisions (approximately 8 at default fetch factor).
    h ^= (h >>> 20) ^ (h >>> 12);
    return h ^ (h >>> 7) ^ (h >>> 4);
  }

  /**
   * Applies a supplemental hash function to a given hashCode, which
   * defends against poor quality hash functions.  This is critical
   * because HashMap uses power-of-two length hash tables, that
   * otherwise encounter collisions for hashCodes that do not differ
   * in lower bits.
   */
  static int hash(int h) {
    return useNewHash ? newHash(h) : oldHash(h);
  }

  static int hash(Object key) {
    return hash(key.hashCode());
  }

  /**
   * Check for equality of non-null reference x and possibly-null y.
   */
  static boolean eq(Object x, Object y) {
    return x == y || x.equals(y);
  }

  /**
   * Returns index for hash code h.
   */
  static int indexFor(int h, int length) {
    return h & (length - 1);
  }

  /**
   * Returns the number of key-value mappings in this map.
   *
   * @return the number of key-value mappings in this map.
   */
  public int size() {
    return size;
  }

  /**
   * Returns <tt>true</tt> if this map contains no key-value mappings.
   *
   * @return <tt>true</tt> if this map contains no key-value mappings.
   */
  public boolean isEmpty() {
    return size == 0;
  }

  /**
   * Returns the value to which the specified key is mapped in this identity
   * hash map, or <tt>null</tt> if the map contains no mapping for this key.
   * A return value of <tt>null</tt> does not <i>necessarily</i> indicate
   * that the map contains no mapping for the key; it is also possible that
   * the map explicitly maps the key to <tt>null</tt>. The
   * <tt>containsKey</tt> method may be used to distinguish these two cases.
   *
   * @param key the key whose associated value is to be returned.
   * @return the value to which this map maps the specified key, or
   *         <tt>null</tt> if the map contains no mapping for this key.
   * @see #put(Object, Object)
   */
  public V get(Object key) {
    if (key == null)
      return getForNullKey();
    int hash = hash(key.hashCode());
    for (Entry<K, V> e = table[indexFor(hash, table.length)];
         e != null;
         e = e.next) {
      Object k;
      if (e.hash == hash && ((k = e.key) == key || key.equals(k)))
        return e.value;
    }
    return null;
  }

  private V getForNullKey() {
    int hash = hash(NULL_KEY.hashCode());
    int i = indexFor(hash, table.length);
    Entry<K, V> e = table[i];
    while (true) {
      if (e == null)
        return null;
      if (e.key == NULL_KEY)
        return e.value;
      e = e.next;
    }
  }

  /**
   * Returns <tt>true</tt> if this map contains a mapping for the
   * specified key.
   *
   * @param key The key whose presence in this map is to be tested
   * @return <tt>true</tt> if this map contains a mapping for the specified
   *         key.
   */
  public boolean containsKey(Object key) {
    Object k = maskNull(key);
    int hash = hash(k.hashCode());
    int i = indexFor(hash, table.length);
    Entry e = table[i];
    while (e != null) {
      if (e.hash == hash && eq(k, e.key))
        return true;
      e = e.next;
    }
    return false;
  }

  /**
   * Returns the entry associated with the specified key in the
   * HashMap.  Returns null if the HashMap contains no mapping
   * for this key.
   */
  Entry<K, V> getEntry(Object key) {
    Object k = maskNull(key);
    int hash = hash(k.hashCode());
    int i = indexFor(hash, table.length);
    Entry<K, V> e = table[i];
    while (e != null && !(e.hash == hash && eq(k, e.key)))
      e = e.next;
    return e;
  }

  /**
   * Associates the specified value with the specified key in this map.
   * If the map previously contained a mapping for this key, the old
   * value is replaced.
   *
   * @param key   key with which the specified value is to be associated.
   * @param value value to be associated with the specified key.
   * @return previous value associated with specified key, or <tt>null</tt>
   *         if there was no mapping for key.  A <tt>null</tt> return can
   *         also indicate that the HashMap previously associated
   *         <tt>null</tt> with the specified key.
   */
  public V put(K key, V value) {
    if (key == null)
      return putForNullKey(value);
    int hash = hash(key.hashCode());
    int i = indexFor(hash, table.length);
    for (Entry<K, V> e = table[i]; e != null; e = e.next) {
      Object k;
      if (e.hash == hash && ((k = e.key) == key || key.equals(k))) {
        V oldValue = e.value;
        e.value = value;
        e.recordAccess(this);
        return oldValue;
      }
    }

    modCount++;
    addEntry(hash, key, value, i);
    return null;
  }

  private V putForNullKey(V value) {
    int hash = hash(NULL_KEY.hashCode());
    int i = indexFor(hash, table.length);

    for (Entry<K, V> e = table[i]; e != null; e = e.next) {
      if (e.key == NULL_KEY) {
        V oldValue = e.value;
        e.value = value;
        e.recordAccess(this);
        return oldValue;
      }
    }

    modCount++;
    addEntry(hash, (K) NULL_KEY, value, i);
    return null;
  }

  /**
   * This method is used instead of put by constructors and
   * pseudoconstructors (clone, readObject).  It does not resize the table,
   * check for comodification, etc.  It calls createEntry rather than
   * addEntry.
   */
  private void putForCreate(K key, V value) {
    K k = maskNull(key);
    int hash = hash(k.hashCode());
    int i = indexFor(hash, table.length);

    /**
     * Look for preexisting entry for key.  This will never happen for
     * clone or deserialize.  It will only happen for construction if the
     * input Map is a sorted map whose ordering is inconsistent w/ equals.
     */
    for (Entry<K, V> e = table[i]; e != null; e = e.next) {
      if (e.hash == hash && eq(k, e.key)) {
        e.value = value;
        return;
      }
    }

    createEntry(hash, k, value, i);
  }

  void putAllForCreate(Map<? extends K, ? extends V> m) {
    for (Iterator<? extends Map.Entry<? extends K, ? extends V>> i = m.entrySet().iterator(); i.hasNext();) {
      Map.Entry<? extends K, ? extends V> e = i.next();
      putForCreate(e.getKey(), e.getValue());
    }
  }

  /**
   * Rehashes the contents of this map into a new array with a
   * larger capacity.  This method is called automatically when the
   * number of keys in this map reaches its threshold.
   * <p/>
   * If current capacity is MAXIMUM_CAPACITY, this method does not
   * resize the map, but sets threshold to Integer.MAX_VALUE.
   * This has the effect of preventing future calls.
   *
   * @param newCapacity the new capacity, MUST be a power of two;
   *                    must be greater than current capacity unless current
   *                    capacity is MAXIMUM_CAPACITY (in which case value
   *                    is irrelevant).
   */
  void resize(int newCapacity) {
    Entry[] oldTable = table;
    int oldCapacity = oldTable.length;
    if (oldCapacity == MAXIMUM_CAPACITY) {
      threshold = Integer.MAX_VALUE;
      return;
    }

    Entry[] newTable = new Entry[newCapacity];
    transfer(newTable);
    table = newTable;
    threshold = (int) (newCapacity * loadFactor);
  }

  /**
   * Transfer all entries from current table to newTable.
   */
  void transfer(Entry[] newTable) {
    Entry[] src = table;
    int newCapacity = newTable.length;
    for (int j = 0; j < src.length; j++) {
      Entry<K, V> e = src[j];
      if (e != null) {
        src[j] = null;
        do {
          Entry<K, V> next = e.next;
          int i = indexFor(e.hash, newCapacity);
          e.next = newTable[i];
          newTable[i] = e;
          e = next;
        } while (e != null);
      }
    }
  }

  /**
   * Copies all of the mappings from the specified map to this map
   * These mappings will replace any mappings that
   * this map had for any of the keys currently in the specified map.
   *
   * @param m mappings to be stored in this map.
   * @throws NullPointerException if the specified map is null.
   */
  public void putAll(Map<? extends K, ? extends V> m) {
    int numKeysToBeAdded = m.size();
    if (numKeysToBeAdded == 0)
      return;

    /*
    * Expand the map if the map if the number of mappings to be added
    * is greater than or equal to threshold.  This is conservative; the
    * obvious condition is (m.size() + size) >= threshold, but this
    * condition could result in a map with twice the appropriate capacity,
    * if the keys to be added overlap with the keys already in this map.
    * By using the conservative calculation, we subject ourself
    * to at most one extra resize.
    */
    if (numKeysToBeAdded > threshold) {
      int targetCapacity = (int) (numKeysToBeAdded / loadFactor + 1);
      if (targetCapacity > MAXIMUM_CAPACITY)
        targetCapacity = MAXIMUM_CAPACITY;
      int newCapacity = table.length;
      while (newCapacity < targetCapacity)
        newCapacity <<= 1;
      if (newCapacity > table.length)
        resize(newCapacity);
    }

    for (Iterator<? extends Map.Entry<? extends K, ? extends V>> i = m.entrySet().iterator(); i.hasNext();) {
      Map.Entry<? extends K, ? extends V> e = i.next();
      put(e.getKey(), e.getValue());
    }
  }

  /**
   * Removes the mapping for this key from this map if present.
   *
   * @param key key whose mapping is to be removed from the map.
   * @return previous value associated with specified key, or <tt>null</tt>
   *         if there was no mapping for key.  A <tt>null</tt> return can
   *         also indicate that the map previously associated <tt>null</tt>
   *         with the specified key.
   */
  public V remove(Object key) {
    Entry<K, V> e = removeEntryForKey(key);
    return (e == null ? null : e.value);
  }

  /**
   * Removes and returns the entry associated with the specified key
   * in the HashMap.  Returns null if the HashMap contains no mapping
   * for this key.
   */
  Entry<K, V> removeEntryForKey(Object key) {
    Object k = maskNull(key);
    int hash = hash(k.hashCode());
    int i = indexFor(hash, table.length);
    Entry<K, V> prev = table[i];
    Entry<K, V> e = prev;

    while (e != null) {
      Entry<K, V> next = e.next;
      if (e.hash == hash && eq(k, e.key)) {
        modCount++;
        size--;
        if (prev == e)
          table[i] = next;
        else
          prev.next = next;
        e.recordRemoval(this);
        return e;
      }
      prev = e;
      e = next;
    }

    return e;
  }

  /**
   * Special version of remove for EntrySet.
   */
  Entry<K, V> removeMapping(Object o) {
    if (!(o instanceof Map.Entry))
      return null;

    Map.Entry<K, V> entry = (Map.Entry<K, V>) o;
    Object k = maskNull(entry.getKey());
    int hash = hash(k.hashCode());
    int i = indexFor(hash, table.length);
    Entry<K, V> prev = table[i];
    Entry<K, V> e = prev;

    while (e != null) {
      Entry<K, V> next = e.next;
      if (e.hash == hash && e.equals(entry)) {
        modCount++;
        size--;
        if (prev == e)
          table[i] = next;
        else
          prev.next = next;
        e.recordRemoval(this);
        return e;
      }
      prev = e;
      e = next;
    }

    return e;
  }

  /**
   * Removes all mappings from this map.
   */
  public void clear() {
    modCount++;
    Entry[] tab = table;
    for (int i = 0; i < tab.length; i++)
      tab[i] = null;
    size = 0;
  }

  /**
   * Returns <tt>true</tt> if this map maps one or more keys to the
   * specified value.
   *
   * @param value value whose presence in this map is to be tested.
   * @return <tt>true</tt> if this map maps one or more keys to the
   *         specified value.
   */
  public boolean containsValue(Object value) {
    if (value == null)
      return containsNullValue();

    Entry[] tab = table;
    for (int i = 0; i < tab.length; i++)
      for (Entry e = tab[i]; e != null; e = e.next)
        if (value.equals(e.value))
          return true;
    return false;
  }

  /**
   * Special-case code for containsValue with null argument
   */
  private boolean containsNullValue() {
    Entry[] tab = table;
    for (int i = 0; i < tab.length; i++)
      for (Entry e = tab[i]; e != null; e = e.next)
        if (e.value == null)
          return true;
    return false;
  }

  /**
   * Returns a shallow copy of this <tt>HashMap</tt> instance: the keys and
   * values themselves are not cloned.
   *
   * @return a shallow copy of this map.
   */
  public Object clone() {
    InernalHashMap<K, V> result = null;
    try {
      result = (InernalHashMap<K, V>) super.clone();
    } catch (CloneNotSupportedException e) {
      // assert false;
    }
    result.table = new Entry[table.length];
    result.entrySet = null;
    result.modCount = 0;
    result.size = 0;
    result.init();
    result.putAllForCreate(this);

    return result;
  }

  static class Entry<K, V> implements Map.Entry<K, V> {
    final K key;
    V value;
    final int hash;
    Entry<K, V> next;

    /**
     * Create new entry.
     */
    Entry(int h, K k, V v, Entry<K, V> n) {
      value = v;
      next = n;
      key = k;
      hash = h;
    }

    public K getKey() {
      return InernalHashMap.<K>unmaskNull(key);
    }

    public V getValue() {
      return value;
    }

    public V setValue(V newValue) {
      V oldValue = value;
      value = newValue;
      return oldValue;
    }

    public boolean equals(Object o) {
      if (!(o instanceof Map.Entry))
        return false;
      Map.Entry e = (Map.Entry) o;
      Object k1 = getKey();
      Object k2 = e.getKey();
      if (k1 == k2 || (k1 != null && k1.equals(k2))) {
        Object v1 = getValue();
        Object v2 = e.getValue();
        if (v1 == v2 || (v1 != null && v1.equals(v2)))
          return true;
      }
      return false;
    }

    public int hashCode() {
      return (key == NULL_KEY ? 0 : key.hashCode()) ^
          (value == null ? 0 : value.hashCode());
    }

    public String toString() {
      return getKey() + "=" + getValue();
    }

    /**
     * This method is invoked whenever the value in an entry is
     * overwritten by an invocation of put(k,v) for a key k that's already
     * in the HashMap.
     */
    void recordAccess(InernalHashMap<K, V> m) {
    }

    /**
     * This method is invoked whenever the entry is
     * removed from the table.
     */
    void recordRemoval(InernalHashMap<K, V> m) {
    }
  }

  /**
   * Add a new entry with the specified key, value and hash code to
   * the specified bucket.  It is the responsibility of this
   * method to resize the table if appropriate.
   * <p/>
   * Subclass overrides this to alter the behavior of put method.
   */
  void addEntry(int hash, K key, V value, int bucketIndex) {
    Entry<K, V> e = table[bucketIndex];
    table[bucketIndex] = new Entry<K, V>(hash, key, value, e);
    if (size++ >= threshold)
      resize(2 * table.length);
  }

  /**
   * Like addEntry except that this version is used when creating entries
   * as part of Map construction or "pseudo-construction" (cloning,
   * deserialization).  This version needn't worry about resizing the table.
   * <p/>
   * Subclass overrides this to alter the behavior of HashMap(Map),
   * clone, and readObject.
   */
  void createEntry(int hash, K key, V value, int bucketIndex) {
    Entry<K, V> e = table[bucketIndex];
    table[bucketIndex] = new Entry<K, V>(hash, key, value, e);
    size++;
  }

  private abstract class HashIterator<E> implements Iterator<E> {
    Entry<K, V> next;  // next entry to return
    int expectedModCount;  // For fast-fail
    int index;    // current slot
    Entry<K, V> current;  // current entry

    HashIterator() {
      expectedModCount = modCount;
      Entry[] t = table;
      int i = t.length;
      Entry<K, V> n = null;
      if (size != 0) { // advance to first entry
        while (i > 0 && (n = t[--i]) == null)
          ;
      }
      next = n;
      index = i;
    }

    public boolean hasNext() {
      return next != null;
    }

    Entry<K, V> nextEntry() {
      if (modCount != expectedModCount)
        throw new ConcurrentModificationException();
      Entry<K, V> e = next;
      if (e == null)
        throw new NoSuchElementException();

      Entry<K, V> n = e.next;
      Entry[] t = table;
      int i = index;
      while (n == null && i > 0)
        n = t[--i];
      index = i;
      next = n;
      return current = e;
    }

    public void remove() {
      if (current == null)
        throw new IllegalStateException();
      if (modCount != expectedModCount)
        throw new ConcurrentModificationException();
      Object k = current.key;
      current = null;
      InernalHashMap.this.removeEntryForKey(k);
      expectedModCount = modCount;
    }

  }

  private class ValueIterator extends HashIterator<V> {
    public V next() {
      return nextEntry().value;
    }
  }

  private class KeyIterator extends HashIterator<K> {
    public K next() {
      return nextEntry().getKey();
    }
  }

  private class EntryIterator extends HashIterator<Map.Entry<K, V>> {
    public Map.Entry<K, V> next() {
      return nextEntry();
    }
  }

  // Subclass overrides these to alter behavior of views' iterator() method
  Iterator<K> newKeyIterator() {
    return new KeyIterator();
  }

  Iterator<V> newValueIterator() {
    return new ValueIterator();
  }

  Iterator<Map.Entry<K, V>> newEntryIterator() {
    return new EntryIterator();
  }

  // Views

  private transient Set<Map.Entry<K, V>> entrySet = null;

  /**
   * Returns a set view of the keys contained in this map.  The set is
   * backed by the map, so changes to the map are reflected in the set, and
   * vice-versa.  The set supports element removal, which removes the
   * corresponding mapping from this map, via the <tt>Iterator.remove</tt>,
   * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt>, and
   * <tt>clear</tt> operations.  It does not support the <tt>add</tt> or
   * <tt>addAll</tt> operations.
   *
   * @return a set view of the keys contained in this map.
   */
  public Set<K> keySet() {
    Set<K> ks = keySet;
    return (ks != null ? ks : (keySet = new KeySet()));
  }

  private class KeySet extends AbstractSet<K> {
    public Iterator<K> iterator() {
      return newKeyIterator();
    }

    public int size() {
      return size;
    }

    public boolean contains(Object o) {
      return containsKey(o);
    }

    public boolean remove(Object o) {
      return InernalHashMap.this.removeEntryForKey(o) != null;
    }

    public void clear() {
      InernalHashMap.this.clear();
    }
  }

  /**
   * Returns a collection view of the values contained in this map.  The
   * collection is backed by the map, so changes to the map are reflected in
   * the collection, and vice-versa.  The collection supports element
   * removal, which removes the corresponding mapping from this map, via the
   * <tt>Iterator.remove</tt>, <tt>Collection.remove</tt>,
   * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt> operations.
   * It does not support the <tt>add</tt> or <tt>addAll</tt> operations.
   *
   * @return a collection view of the values contained in this map.
   */
  public Collection<V> values() {
    Collection<V> vs = values;
    return (vs != null ? vs : (values = new Values()));
  }

  private class Values extends AbstractCollection<V> {
    public Iterator<V> iterator() {
      return newValueIterator();
    }

    public int size() {
      return size;
    }

    public boolean contains(Object o) {
      return containsValue(o);
    }

    public void clear() {
      InernalHashMap.this.clear();
    }
  }

  /**
   * Returns a collection view of the mappings contained in this map.  Each
   * element in the returned collection is a <tt>Map.Entry</tt>.  The
   * collection is backed by the map, so changes to the map are reflected in
   * the collection, and vice-versa.  The collection supports element
   * removal, which removes the corresponding mapping from the map, via the
   * <tt>Iterator.remove</tt>, <tt>Collection.remove</tt>,
   * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt> operations.
   * It does not support the <tt>add</tt> or <tt>addAll</tt> operations.
   *
   * @return a collection view of the mappings contained in this map.
   * @see Map.Entry
   */
  public Set<Map.Entry<K, V>> entrySet() {
    Set<Map.Entry<K, V>> es = entrySet;
    return (es != null ? es : (entrySet = (Set<Map.Entry<K, V>>) (Set) new EntrySet()));
  }

  private class EntrySet extends AbstractSet/*<Map.Entry<K,V>>*/ {
    public Iterator/*<Map.Entry<K,V>>*/ iterator() {
      return newEntryIterator();
    }

    public boolean contains(Object o) {
      if (!(o instanceof Map.Entry))
        return false;
      Map.Entry<K, V> e = (Map.Entry<K, V>) o;
      Entry<K, V> candidate = getEntry(e.getKey());
      return candidate != null && candidate.equals(e);
    }

    public boolean remove(Object o) {
      return removeMapping(o) != null;
    }

    public int size() {
      return size;
    }

    public void clear() {
      InernalHashMap.this.clear();
    }
  }

  /**
   * Save the state of the <tt>HashMap</tt> instance to a stream (i.e.,
   * serialize it).
   *
   * @serialData The <i>capacity</i> of the HashMap (the length of the
   * bucket array) is emitted (int), followed  by the
   * <i>size</i> of the HashMap (the number of key-value
   * mappings), followed by the key (Object) and value (Object)
   * for each key-value mapping represented by the HashMap
   * The key-value mappings are emitted in the order that they
   * are returned by <tt>entrySet().iterator()</tt>.
   */
  private void writeObject(java.io.ObjectOutputStream s)
      throws IOException {
    Iterator<Map.Entry<K, V>> i = entrySet().iterator();

    // Write out the threshold, loadfactor, and any hidden stuff
    s.defaultWriteObject();

    // Write out number of buckets
    s.writeInt(table.length);

    // Write out size (number of Mappings)
    s.writeInt(size);

    // Write out keys and values (alternating)
    while (i.hasNext()) {
      Map.Entry<K, V> e = i.next();
      s.writeObject(e.getKey());
      s.writeObject(e.getValue());
    }
  }

  private static final long serialVersionUID = 362498820763181265L;

  /**
   * Reconstitute the <tt>HashMap</tt> instance from a stream (i.e.,
   * deserialize it).
   */
  private void readObject(java.io.ObjectInputStream s)
      throws IOException, ClassNotFoundException {
    // Read in the threshold, loadfactor, and any hidden stuff
    s.defaultReadObject();

    // Read in number of buckets and allocate the bucket array;
    int numBuckets = s.readInt();
    table = new Entry[numBuckets];

    init();  // Give subclass a chance to do its thing.

    // Read in size (number of Mappings)
    int size = s.readInt();

    // Read the keys and values, and put the mappings in the HashMap
    for (int i = 0; i < size; i++) {
      K key = (K) s.readObject();
      V value = (V) s.readObject();
      putForCreate(key, value);
    }
  }

  // These methods are used when serializing HashSets
  int capacity() {
    return table.length;
  }

  float loadFactor() {
    return loadFactor;
  }
}

/*
* @(#)AbstractMap.java	1.42 04/02/19
*
* Copyright 2004 Sun Microsystems, Inc. All rights reserved.
* SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
*/

/**
 * This class provides a skeletal implementation of the <tt>Map</tt>
 * interface, to minimize the effort required to implement this interface. <p>
 * <p/>
 * To implement an unmodifiable map, the programmer needs only to extend this
 * class and provide an implementation for the <tt>entrySet</tt> method, which
 * returns a set-view of the map's mappings.  Typically, the returned set
 * will, in turn, be implemented atop <tt>AbstractSet</tt>.  This set should
 * not support the <tt>add</tt> or <tt>remove</tt> methods, and its iterator
 * should not support the <tt>remove</tt> method.<p>
 * <p/>
 * To implement a modifiable map, the programmer must additionally override
 * this class's <tt>put</tt> method (which otherwise throws an
 * <tt>UnsupportedOperationException</tt>), and the iterator returned by
 * <tt>entrySet().iterator()</tt> must additionally implement its
 * <tt>remove</tt> method.<p>
 * <p/>
 * The programmer should generally provide a void (no argument) and map
 * constructor, as per the recommendation in the <tt>Map</tt> interface
 * specification.<p>
 * <p/>
 * The documentation for each non-abstract methods in this class describes its
 * implementation in detail.  Each of these methods may be overridden if the
 * map being implemented admits a more efficient implementation.<p>
 * <p/>
 * This class is a member of the
 * <a href="{@docRoot}/../guide/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @author Josh Bloch
 * @author Neal Gafter
 * @version 1.42, 02/19/04
 * @see Map
 * @see Collection
 * @since 1.2
 */

abstract class InernalAbstractMap<K, V> implements Map<K, V> {
  /**
   * Sole constructor.  (For invocation by subclass constructors, typically
   * implicit.)
   */
  protected InernalAbstractMap() {
  }

  // Query Operations

  /**
   * Returns the number of key-value mappings in this map.  If the map
   * contains more than <tt>Integer.MAX_VALUE</tt> elements, returns
   * <tt>Integer.MAX_VALUE</tt>.<p>
   * <p/>
   * This implementation returns <tt>entrySet().size()</tt>.
   *
   * @return the number of key-value mappings in this map.
   */
  public int size() {
    return entrySet().size();
  }

  /**
   * Returns <tt>true</tt> if this map contains no key-value mappings. <p>
   * <p/>
   * This implementation returns <tt>size() == 0</tt>.
   *
   * @return <tt>true</tt> if this map contains no key-value mappings.
   */
  public boolean isEmpty() {
    return size() == 0;
  }

  /**
   * Returns <tt>true</tt> if this map maps one or more keys to this value.
   * More formally, returns <tt>true</tt> if and only if this map contains
   * at least one mapping to a value <tt>v</tt> such that <tt>(value==null ?
   * v==null : value.equals(v))</tt>.  This operation will probably require
   * time linear in the map size for most implementations of map.<p>
   * <p/>
   * This implementation iterates over entrySet() searching for an entry
   * with the specified value.  If such an entry is found, <tt>true</tt> is
   * returned.  If the iteration terminates without finding such an entry,
   * <tt>false</tt> is returned.  Note that this implementation requires
   * linear time in the size of the map.
   *
   * @param value value whose presence in this map is to be tested.
   * @return <tt>true</tt> if this map maps one or more keys to this value.
   */
  public boolean containsValue(Object value) {
    Iterator<Entry<K, V>> i = entrySet().iterator();
    if (value == null) {
      while (i.hasNext()) {
        Entry<K, V> e = i.next();
        if (e.getValue() == null)
          return true;
      }
    } else {
      while (i.hasNext()) {
        Entry<K, V> e = i.next();
        if (value.equals(e.getValue()))
          return true;
      }
    }
    return false;
  }

  /**
   * Returns <tt>true</tt> if this map contains a mapping for the specified
   * key. <p>
   * <p/>
   * This implementation iterates over <tt>entrySet()</tt> searching for an
   * entry with the specified key.  If such an entry is found, <tt>true</tt>
   * is returned.  If the iteration terminates without finding such an
   * entry, <tt>false</tt> is returned.  Note that this implementation
   * requires linear time in the size of the map; many implementations will
   * override this method.
   *
   * @param key key whose presence in this map is to be tested.
   * @return <tt>true</tt> if this map contains a mapping for the specified
   *         key.
   * @throws NullPointerException if the key is <tt>null</tt> and this map
   *                              does not permit <tt>null</tt> keys.
   */
  public boolean containsKey(Object key) {
    Iterator<Map.Entry<K, V>> i = entrySet().iterator();
    if (key == null) {
      while (i.hasNext()) {
        Entry<K, V> e = i.next();
        if (e.getKey() == null)
          return true;
      }
    } else {
      while (i.hasNext()) {
        Entry<K, V> e = i.next();
        if (key.equals(e.getKey()))
          return true;
      }
    }
    return false;
  }

  /**
   * Returns the value to which this map maps the specified key.  Returns
   * <tt>null</tt> if the map contains no mapping for this key.  A return
   * value of <tt>null</tt> does not <i>necessarily</i> indicate that the
   * map contains no mapping for the key; it's also possible that the map
   * explicitly maps the key to <tt>null</tt>.  The containsKey operation
   * may be used to distinguish these two cases. <p>
   * <p/>
   * This implementation iterates over <tt>entrySet()</tt> searching for an
   * entry with the specified key.  If such an entry is found, the entry's
   * value is returned.  If the iteration terminates without finding such an
   * entry, <tt>null</tt> is returned.  Note that this implementation
   * requires linear time in the size of the map; many implementations will
   * override this method.
   *
   * @param key key whose associated value is to be returned.
   * @return the value to which this map maps the specified key.
   * @throws NullPointerException if the key is <tt>null</tt> and this map
   *                              does not permit <tt>null</tt> keys.
   * @see #containsKey(Object)
   */
  public V get(Object key) {
    Iterator<Entry<K, V>> i = entrySet().iterator();
    if (key == null) {
      while (i.hasNext()) {
        Entry<K, V> e = i.next();
        if (e.getKey() == null)
          return e.getValue();
      }
    } else {
      while (i.hasNext()) {
        Entry<K, V> e = i.next();
        if (key.equals(e.getKey()))
          return e.getValue();
      }
    }
    return null;
  }

  // Modification Operations

  /**
   * Associates the specified value with the specified key in this map
   * (optional operation).  If the map previously contained a mapping for
   * this key, the old value is replaced.<p>
   * <p/>
   * This implementation always throws an
   * <tt>UnsupportedOperationException</tt>.
   *
   * @param key   key with which the specified value is to be associated.
   * @param value value to be associated with the specified key.
   * @return previous value associated with specified key, or <tt>null</tt>
   *         if there was no mapping for key.  (A <tt>null</tt> return can
   *         also indicate that the map previously associated <tt>null</tt>
   *         with the specified key, if the implementation supports
   *         <tt>null</tt> values.)
   * @throws UnsupportedOperationException if the <tt>put</tt> operation is
   *                                       not supported by this map.
   * @throws ClassCastException            if the class of the specified key or value
   *                                       prevents it from being stored in this map.
   * @throws IllegalArgumentException      if some aspect of this key or value *
   *                                       prevents it from being stored in this map.
   * @throws NullPointerException          if this map does not permit <tt>null</tt>
   *                                       keys or values, and the specified key or value is
   *                                       <tt>null</tt>.
   */
  public V put(K key, V value) {
    throw new UnsupportedOperationException();
  }

  /**
   * Removes the mapping for this key from this map if present (optional
   * operation). <p>
   * <p/>
   * This implementation iterates over <tt>entrySet()</tt> searching for an
   * entry with the specified key.  If such an entry is found, its value is
   * obtained with its <tt>getValue</tt> operation, the entry is removed
   * from the Collection (and the backing map) with the iterator's
   * <tt>remove</tt> operation, and the saved value is returned.  If the
   * iteration terminates without finding such an entry, <tt>null</tt> is
   * returned.  Note that this implementation requires linear time in the
   * size of the map; many implementations will override this method.<p>
   * <p/>
   * Note that this implementation throws an
   * <tt>UnsupportedOperationException</tt> if the <tt>entrySet</tt> iterator
   * does not support the <tt>remove</tt> method and this map contains a
   * mapping for the specified key.
   *
   * @param key key whose mapping is to be removed from the map.
   * @return previous value associated with specified key, or <tt>null</tt>
   *         if there was no entry for key.  (A <tt>null</tt> return can
   *         also indicate that the map previously associated <tt>null</tt>
   *         with the specified key, if the implementation supports
   *         <tt>null</tt> values.)
   * @throws UnsupportedOperationException if the <tt>remove</tt> operation
   *                                       is not supported by this map.
   */
  public V remove(Object key) {
    Iterator<Entry<K, V>> i = entrySet().iterator();
    Entry<K, V> correctEntry = null;
    if (key == null) {
      while (correctEntry == null && i.hasNext()) {
        Entry<K, V> e = i.next();
        if (e.getKey() == null)
          correctEntry = e;
      }
    } else {
      while (correctEntry == null && i.hasNext()) {
        Entry<K, V> e = i.next();
        if (key.equals(e.getKey()))
          correctEntry = e;
      }
    }

    V oldValue = null;
    if (correctEntry != null) {
      oldValue = correctEntry.getValue();
      i.remove();
    }
    return oldValue;
  }

  // Bulk Operations

  /**
   * Copies all of the mappings from the specified map to this map
   * (optional operation).  These mappings will replace any mappings that
   * this map had for any of the keys currently in the specified map.<p>
   * <p/>
   * This implementation iterates over the specified map's
   * <tt>entrySet()</tt> collection, and calls this map's <tt>put</tt>
   * operation once for each entry returned by the iteration.<p>
   * <p/>
   * Note that this implementation throws an
   * <tt>UnsupportedOperationException</tt> if this map does not support
   * the <tt>put</tt> operation and the specified map is nonempty.
   *
   * @param t mappings to be stored in this map.
   * @throws UnsupportedOperationException if the <tt>putAll</tt> operation
   *                                       is not supported by this map.
   * @throws ClassCastException            if the class of a key or value in the
   *                                       specified map prevents it from being stored in this map.
   * @throws IllegalArgumentException      if some aspect of a key or value in
   *                                       the specified map prevents it from being stored in this map.
   * @throws NullPointerException          if the specified map is <tt>null</tt>, or if
   *                                       this map does not permit <tt>null</tt> keys or values, and the
   *                                       specified map contains <tt>null</tt> keys or values.
   */
  public void putAll(Map<? extends K, ? extends V> t) {
    Iterator<? extends Entry<? extends K, ? extends V>> i = t.entrySet().iterator();
    while (i.hasNext()) {
      Entry<? extends K, ? extends V> e = i.next();
      put(e.getKey(), e.getValue());
    }
  }

  /**
   * Removes all mappings from this map (optional operation). <p>
   * <p/>
   * This implementation calls <tt>entrySet().clear()</tt>.
   * <p/>
   * Note that this implementation throws an
   * <tt>UnsupportedOperationException</tt> if the <tt>entrySet</tt>
   * does not support the <tt>clear</tt> operation.
   *
   * @throws UnsupportedOperationException clear is not supported
   *                                       by this map.
   */
  public void clear() {
    entrySet().clear();
  }

  // Views

  /**
   * Each of these fields are initialized to contain an instance of the
   * appropriate view the first time this view is requested.  The views are
   * stateless, so there's no reason to create more than one of each.
   */
  transient volatile Set<K> keySet = null;
  transient volatile Collection<V> values = null;

  /**
   * Returns a Set view of the keys contained in this map.  The Set is
   * backed by the map, so changes to the map are reflected in the Set,
   * and vice-versa.  (If the map is modified while an iteration over
   * the Set is in progress, the results of the iteration are undefined.)
   * The Set supports element removal, which removes the corresponding entry
   * from the map, via the Iterator.remove, Set.remove,  removeAll
   * retainAll, and clear operations.  It does not support the add or
   * addAll operations.<p>
   * <p/>
   * This implementation returns a Set that subclasses
   * AbstractSet.  The subclass's iterator method returns a "wrapper
   * object" over this map's entrySet() iterator.  The size method delegates
   * to this map's size method and the contains method delegates to this
   * map's containsKey method.<p>
   * <p/>
   * The Set is created the first time this method is called,
   * and returned in response to all subsequent calls.  No synchronization
   * is performed, so there is a slight chance that multiple calls to this
   * method will not all return the same Set.
   *
   * @return a Set view of the keys contained in this map.
   */
  public Set<K> keySet() {
    if (keySet == null) {
      keySet = new AbstractSet<K>() {
        public Iterator<K> iterator() {
          return new Iterator<K>() {
            private Iterator<Entry<K, V>> i = entrySet().iterator();

            public boolean hasNext() {
              return i.hasNext();
            }

            public K next() {
              return i.next().getKey();
            }

            public void remove() {
              i.remove();
            }
          };
        }

        public int size() {
          return InernalAbstractMap.this.size();
        }

        public boolean contains(Object k) {
          return InernalAbstractMap.this.containsKey(k);
        }
      };
    }
    return keySet;
  }

  /**
   * Returns a collection view of the values contained in this map.  The
   * collection is backed by the map, so changes to the map are reflected in
   * the collection, and vice-versa.  (If the map is modified while an
   * iteration over the collection is in progress, the results of the
   * iteration are undefined.)  The collection supports element removal,
   * which removes the corresponding entry from the map, via the
   * <tt>Iterator.remove</tt>, <tt>Collection.remove</tt>,
   * <tt>removeAll</tt>, <tt>retainAll</tt> and <tt>clear</tt> operations.
   * It does not support the <tt>add</tt> or <tt>addAll</tt> operations.<p>
   * <p/>
   * This implementation returns a collection that subclasses abstract
   * collection.  The subclass's iterator method returns a "wrapper object"
   * over this map's <tt>entrySet()</tt> iterator.  The size method
   * delegates to this map's size method and the contains method delegates
   * to this map's containsValue method.<p>
   * <p/>
   * The collection is created the first time this method is called, and
   * returned in response to all subsequent calls.  No synchronization is
   * performed, so there is a slight chance that multiple calls to this
   * method will not all return the same Collection.
   *
   * @return a collection view of the values contained in this map.
   */
  public Collection<V> values() {
    if (values == null) {
      values = new AbstractCollection<V>() {
        public Iterator<V> iterator() {
          return new Iterator<V>() {
            private Iterator<Entry<K, V>> i = entrySet().iterator();

            public boolean hasNext() {
              return i.hasNext();
            }

            public V next() {
              return i.next().getValue();
            }

            public void remove() {
              i.remove();
            }
          };
        }

        public int size() {
          return InernalAbstractMap.this.size();
        }

        public boolean contains(Object v) {
          return InernalAbstractMap.this.containsValue(v);
        }
      };
    }
    return values;
  }

  /**
   * Returns a set view of the mappings contained in this map.  Each element
   * in this set is a Map.Entry.  The set is backed by the map, so changes
   * to the map are reflected in the set, and vice-versa.  (If the map is
   * modified while an iteration over the set is in progress, the results of
   * the iteration are undefined.)  The set supports element removal, which
   * removes the corresponding entry from the map, via the
   * <tt>Iterator.remove</tt>, <tt>Set.remove</tt>, <tt>removeAll</tt>,
   * <tt>retainAll</tt> and <tt>clear</tt> operations.  It does not support
   * the <tt>add</tt> or <tt>addAll</tt> operations.
   *
   * @return a set view of the mappings contained in this map.
   */
  public abstract Set<Entry<K, V>> entrySet();

  // Comparison and hashing

  /**
   * Compares the specified object with this map for equality.  Returns
   * <tt>true</tt> if the given object is also a map and the two maps
   * represent the same mappings.  More formally, two maps <tt>t1</tt> and
   * <tt>t2</tt> represent the same mappings if
   * <tt>t1.keySet().equals(t2.keySet())</tt> and for every key <tt>k</tt>
   * in <tt>t1.keySet()</tt>, <tt> (t1.get(k)==null ? t2.get(k)==null :
   * t1.get(k).equals(t2.get(k))) </tt>.  This ensures that the
   * <tt>equals</tt> method works properly across different implementations
   * of the map interface.<p>
   * <p/>
   * This implementation first checks if the specified object is this map;
   * if so it returns <tt>true</tt>.  Then, it checks if the specified
   * object is a map whose size is identical to the size of this set; if
   * not, it returns <tt>false</tt>.  If so, it iterates over this map's
   * <tt>entrySet</tt> collection, and checks that the specified map
   * contains each mapping that this map contains.  If the specified map
   * fails to contain such a mapping, <tt>false</tt> is returned.  If the
   * iteration completes, <tt>true</tt> is returned.
   *
   * @param o object to be compared for equality with this map.
   * @return <tt>true</tt> if the specified object is equal to this map.
   */
  public boolean equals(Object o) {
    if (o == this)
      return true;

    if (!(o instanceof Map))
      return false;
    Map<K, V> t = (Map<K, V>) o;
    if (t.size() != size())
      return false;

    try {
      Iterator<Entry<K, V>> i = entrySet().iterator();
      while (i.hasNext()) {
        Entry<K, V> e = i.next();
        K key = e.getKey();
        V value = e.getValue();
        if (value == null) {
          if (!(t.get(key) == null && t.containsKey(key)))
            return false;
        } else {
          if (!value.equals(t.get(key)))
            return false;
        }
      }
    } catch (ClassCastException unused) {
      return false;
    } catch (NullPointerException unused) {
      return false;
    }

    return true;
  }

  /**
   * Returns the hash code value for this map.  The hash code of a map is
   * defined to be the sum of the hash codes of each entry in the map's
   * <tt>entrySet()</tt> view.  This ensures that <tt>t1.equals(t2)</tt>
   * implies that <tt>t1.hashCode()==t2.hashCode()</tt> for any two maps
   * <tt>t1</tt> and <tt>t2</tt>, as required by the general contract of
   * Object.hashCode.<p>
   * <p/>
   * This implementation iterates over <tt>entrySet()</tt>, calling
   * <tt>hashCode</tt> on each element (entry) in the Collection, and adding
   * up the results.
   *
   * @return the hash code value for this map.
   * @see Map.Entry#hashCode()
   * @see Object#hashCode()
   * @see Object#equals(Object)
   * @see Set#equals(Object)
   */
  public int hashCode() {
    int h = 0;
    Iterator<Entry<K, V>> i = entrySet().iterator();
    while (i.hasNext())
      h += i.next().hashCode();
    return h;
  }

  /**
   * Returns a string representation of this map.  The string representation
   * consists of a list of key-value mappings in the order returned by the
   * map's <tt>entrySet</tt> view's iterator, enclosed in braces
   * (<tt>"{}"</tt>).  Adjacent mappings are separated by the characters
   * <tt>", "</tt> (comma and space).  Each key-value mapping is rendered as
   * the key followed by an equals sign (<tt>"="</tt>) followed by the
   * associated value.  Keys and values are converted to strings as by
   * <tt>String.valueOf(Object)</tt>.<p>
   * <p/>
   * This implementation creates an empty string buffer, appends a left
   * brace, and iterates over the map's <tt>entrySet</tt> view, appending
   * the string representation of each <tt>map.entry</tt> in turn.  After
   * appending each entry except the last, the string <tt>", "</tt> is
   * appended.  Finally a right brace is appended.  A string is obtained
   * from the stringbuffer, and returned.
   *
   * @return a String representation of this map.
   */
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("{");

    Iterator<Entry<K, V>> i = entrySet().iterator();
    boolean hasNext = i.hasNext();
    while (hasNext) {
      Entry<K, V> e = i.next();
      K key = e.getKey();
      V value = e.getValue();
      if (key == this)
        buf.append("(this Map)");
      else
        buf.append(key);
      buf.append("=");
      if (value == this)
        buf.append("(this Map)");
      else
        buf.append(value);
      hasNext = i.hasNext();
      if (hasNext)
        buf.append(", ");
    }

    buf.append("}");
    return buf.toString();
  }

  /**
   * Returns a shallow copy of this <tt>AbstractMap</tt> instance: the keys
   * and values themselves are not cloned.
   *
   * @return a shallow copy of this map.
   */
  protected Object clone() throws CloneNotSupportedException {
    InernalAbstractMap<K, V> result = (InernalAbstractMap<K, V>) super.clone();
    result.keySet = null;
    result.values = null;
    return result;
  }

  /**
   * This should be made public as soon as possible.  It greatly simplifies
   * the task of implementing Map.
   */
  static class SimpleEntry<K, V> implements Entry<K, V> {
    K key;
    V value;

    public SimpleEntry(K key, V value) {
      this.key = key;
      this.value = value;
    }

    public SimpleEntry(Entry<K, V> e) {
      this.key = e.getKey();
      this.value = e.getValue();
    }

    public K getKey() {
      return key;
    }

    public V getValue() {
      return value;
    }

    public V setValue(V value) {
      V oldValue = this.value;
      this.value = value;
      return oldValue;
    }

    public boolean equals(Object o) {
      if (!(o instanceof Map.Entry))
        return false;
      Map.Entry e = (Map.Entry) o;
      return eq(key, e.getKey()) && eq(value, e.getValue());
    }

    public int hashCode() {
      return ((key == null) ? 0 : key.hashCode()) ^
          ((value == null) ? 0 : value.hashCode());
    }

    public String toString() {
      return key + "=" + value;
    }

    private static boolean eq(Object o1, Object o2) {
      return (o1 == null ? o2 == null : o1.equals(o2));
    }
  }
}


