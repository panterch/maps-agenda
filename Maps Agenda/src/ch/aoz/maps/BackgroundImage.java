package ch.aoz.maps;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

public class BackgroundImage implements java.io.Serializable {
  private static final long serialVersionUID = 161729L;
  public static final String entityKind = "BackgroundImage";
  public static final char RS = 0x1e;  // Record separator.

  private String key;
  private String url;
  private String debug;
  
  public BackgroundImage() {
    this.key = "";
    this.url = "";
    this.debug = "invalid";
  }  
  
  public BackgroundImage(String key, String url) {
    this.key = key;
    this.url = url;
    this.debug = "ok";
  }  
  
  private static String getDatastoreKey() {
    return entityKind;
  }
  
  private static String getMemcacheKey() {
    return entityKind;
  }

  /** 
   * Fetches the BackgroundImage from the store.
   */
  public static BackgroundImage fetchFromStore() {
    MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
    if (syncCache.contains(getMemcacheKey())) {
      return (BackgroundImage)syncCache.get(getMemcacheKey());
    }
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    try {
      Entity e = datastore.get(KeyFactory.createKey(entityKind, getDatastoreKey()));
      return fromEntity(e);
    } catch (EntityNotFoundException e) {
      return new BackgroundImage();
    }
  }
  
  /**
   * Replace the BackgroundImage in the datastore with this instance.
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
   * Returns the Entity representation of this BackgroundImage.
   *
   * @return an Entity with the properties of this BackgroundImage.
   */
  private Entity toEntity() {
    Entity entity = new Entity(entityKind, getDatastoreKey());
    entity.setUnindexedProperty(getDatastoreKey(), getKey() + RS + getUrl());
    return entity;
  }

  /** 
   * Extracts a BackgroundImage from the packed representation in the database.
   * 
   * @param entity representation of a BackgroundImage in the database.
   * @return a fully constructed BackgroundImage
   */
  private static BackgroundImage fromEntity(Entity entity) {
    String[] fields = ((String)entity.getProperty(getDatastoreKey())).split("" + RS);
    if (fields.length < 2) return new BackgroundImage();
    return new BackgroundImage(fields[0], fields[1]);
  }

  private void addToCache() {
    MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
    syncCache.put(getMemcacheKey(), this);    
  }
  
  /** Only setters and getters below. */  
  public String getKey() {
    return key;
  }
  
  public String getUrl() {
  	return url;
  }

  public String debug() {
    return debug;
  }
}
