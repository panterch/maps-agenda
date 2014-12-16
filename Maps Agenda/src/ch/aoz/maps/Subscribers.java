package ch.aoz.maps;

import java.util.Collection;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

public class Subscribers implements java.io.Serializable {
  public static final String entityKind = "Subscribers";
  public static final char RS = 0x1e;  // Record separator.
  private static final long serialVersionUID = 161725L;

  private SortedMap<String, Subscriber> subscribers;
  private boolean isOk;
  private String debug;

  public Subscribers() {
    subscribers = new TreeMap<String, Subscriber>();
    debug = "ok";
    isOk = true;
  }
  
  public Subscribers(Collection<Subscriber> subscribers) {
    this();
    for (Subscriber s : subscribers) {
      this.subscribers.put(s.getEmail(), s);
    }
  }
  
  private Subscribers(Entity entity) {
    this();
    for (String email : entity.getProperties().keySet()) {
      String str = (String)entity.getProperty(email);
      Subscriber s = extractSubscriber(email, str);
      if (s != null) {
        subscribers.put(email, s);
      }
    }
    debug = "ok";
    isOk = true;
    addToCache();
  }
  
  /** 
   * Returns the Subscribers object. It contains all the subscribers defined in the application.
   */
  public static Subscribers getSubscribers() {
    MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
    if (syncCache.contains(entityKind)) {
      return (Subscribers)syncCache.get(entityKind);
    }
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    try {
      Entity e = datastore.get(KeyFactory.createKey(entityKind, entityKind));
      return new Subscribers(e);
    } catch (EntityNotFoundException e) {
      return new Subscribers();
    }
  }

  public static boolean addSubscriber(Subscriber s) {
    if (s == null || !s.isOk()) 
      return false;
    Subscribers subs = getSubscribers();
    if (subs == null) 
      return false;
    subs.subscribers.put(s.getEmail(), s);
    return subs.addToStore();
  }
  
  public static boolean removeSubscriber(String email) {
    if (email == null)
      return false;
    Subscribers subs = getSubscribers();
    if (subs.subscribers.containsKey(email)) {
      subs.subscribers.remove(email);
      return subs.addToStore();
    }
    return true;
  }

  public static boolean exists(String email) {
    if (email == null)
      return false;
    Subscribers subs = getSubscribers();
    return subs.subscribers.containsKey(email);
  }

  /**
   * Add this Subscribers to the datastore.
   *
   * @return true if this operation succeeded.
   */
  public boolean addToStore() {
    if (!this.isOk()) {
      return false;
    }
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    try {
      datastore.put(this.toEntity());
    } catch (Exception ex) {
      return false;
    }
    addToCache();
    return true;
  }
  
  /** Utilities for storing and caching  */
  
  /**
   * Returns the Entity representation of this Subscribers.
   *
   * @return an Entity with the properties of this Subscribers.
   */
  private Entity toEntity() {
    Entity subscribers = new Entity(entityKind, entityKind);
    for (Subscriber s : this.subscribers.values()) {
      subscribers.setUnindexedProperty(s.getEmail(), packSubscriber(s));
    }
    return subscribers;
  }

  /** 
   * Extracts a Subscriber from the packed representation in the database.
   * 
   * @param packed string representation of a Subscriber in the database.
   * @return a fully constructed Subscriber
   */
  private static Subscriber extractSubscriber(String email, String packed) {
    String[] fields = packed.split("" + RS, 3);
    if (fields.length != 3) return null;
    return new Subscriber(email, fields[0], fields[1], fields[2]);
  }
  
  /**
   * Packs the provided Subscriber into its database representation.
   * 
   * @param s the Subscriber to pack in its database representation.
   * @return the packed representation.
   */
  private static String packSubscriber(Subscriber s) {
    StringBuilder str = new StringBuilder();
    str.append(s.getName() + RS);
    str.append(s.getLanguage() + RS);
    str.append(s.getHash() + RS);
    return str.toString();
  }

  private void addToCache() {
    MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
    syncCache.put(entityKind, this);    
  }
  
  /** Only setters and getters below. */
  public Set<String> getSortedEmails() {
    return subscribers.keySet();
  }
  
  public SortedMap<String, Subscriber> getSortedSubscribers() {
    return subscribers;
  }
  
  public Subscriber getSubscriber(String email) {
    if (subscribers.containsKey(email))
      return subscribers.get(email);
    else return null;
  }
  
  public Subscriber getSubscriberByHash(String hash) {
    if (hash == null || hash.equals(""))
      return null;
    for (Subscriber s : subscribers.values()) {
      if (s.getHash().equals(hash))
        return s;
    }
    return null;
  }

  public boolean isOk() {
    return isOk;
  }
  public String debug() {
    return debug;
  }
}
