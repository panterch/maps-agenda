package ch.aoz.maps;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

public class BackgroundColor implements java.io.Serializable {
  private static final long serialVersionUID = 161730L;
  public static final String entityKind = "BackgroundColor";
  public static final char RS = 0x1e;  // Record separator.

  private String color;
  private String debug;
  
  public BackgroundColor() {
    this.color = "";
    this.debug = "invalid";
  }  
  
  public BackgroundColor(String color) {
    this.color = color;
    this.debug = "ok";
  }  
  
  private static String getDatastoreKey() {
    return entityKind;
  }
  
  private static String getMemcacheKey() {
    return entityKind;
  }

  /** 
   * Fetches the BackgroundColor from the store.
   */
  public static BackgroundColor fetchFromStore() {
    MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
    if (syncCache.contains(getMemcacheKey())) {
      return (BackgroundColor)syncCache.get(getMemcacheKey());
    }
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    try {
      Entity e = datastore.get(KeyFactory.createKey(entityKind, getDatastoreKey()));
      return fromEntity(e);
    } catch (EntityNotFoundException e) {
      return new BackgroundColor("08a");
    }
  }
  
  /**
   * Replace the BackgroundColor in the datastore with this instance.
   *
   * @return true if this operation succeeded.
   */
  public boolean addToStore() {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    try {
      datastore.put(this.toEntity());
    } catch (Exception ex) {
      ex.printStackTrace();
      debug = "exception (datastore null? " + (datastore == null) + "): " + ex.toString();
      return false;
    }
    addToCache();
    return true;
  }
  
  /** Utilities for storing and caching  */
  
  /**
   * Returns the Entity representation of this BackgroundColor.
   *
   * @return an Entity with the properties of this BackgroundColor.
   */
  private Entity toEntity() {
    Entity entity = new Entity(entityKind, getDatastoreKey());
    entity.setUnindexedProperty(getDatastoreKey(), getColor());
    return entity;
  }

  /** 
   * Extracts a BackgroundColor from the packed representation in the database.
   * 
   * @param entity representation of a BackgroundColor in the database.
   * @return a fully constructed BackgroundColor
   */
  private static BackgroundColor fromEntity(Entity entity) {
    String[] fields = ((String)entity.getProperty(getDatastoreKey())).split("" + RS);
    if (fields.length < 1) return new BackgroundColor();
    return new BackgroundColor(fields[0]);
  }

  private void addToCache() {
    MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
    syncCache.put(getMemcacheKey(), this);    
  }
  
  /** Only setters and getters below. */  
  public String getColor() {
    return color;
  }

  public String debug() {
    return debug;
  }
}