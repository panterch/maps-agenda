package ch.aoz.maps;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

public class MailChimpCredentials implements java.io.Serializable {
  private static final long serialVersionUID = 161733L;
  public static final String entityKind = "MailChimpCredentials";
  public static final char RS = 0x1e;  // Record separator.

  private String listId;
  private String apiKey;
  private String debug;
  
  public MailChimpCredentials() {
    this.listId = "";
    this.apiKey = "";
    this.debug = "invalid";
  }  
  
  public MailChimpCredentials(String list_id, String api_key) {
    this.listId = list_id;
    this.apiKey = api_key;
    this.debug = "ok";
  }  
  
  private static String getDatastoreKey() {
    return entityKind;
  }
  
  private static String getMemcacheKey() {
    return entityKind;
  }

  /** 
   * Fetches the MailChimpCredentials from the store.
   */
  public static MailChimpCredentials fetchFromStore() {
    MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
    if (syncCache.contains(getMemcacheKey())) {
      return (MailChimpCredentials)syncCache.get(getMemcacheKey());
    }
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    try {
      Entity e = datastore.get(KeyFactory.createKey(entityKind, getDatastoreKey()));
      return fromEntity(e);
    } catch (EntityNotFoundException e) {
      return new MailChimpCredentials("", "");
    }
  }
  
  /**
   * Replace the MailChimpCredentials in the datastore with this instance.
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
   * Returns the Entity representation of this MailChimpCredentials.
   *
   * @return an Entity with the properties of this MailChimpCredentials.
   */
  private Entity toEntity() {
    StringBuilder s = new StringBuilder();
    s.append(getListId() + RS);
    s.append(getApiKey());

    Entity entity = new Entity(entityKind, getDatastoreKey());
    entity.setUnindexedProperty(getDatastoreKey(), s.toString());
    return entity;
  }

  /** 
   * Extracts a MailChimpCredentials from the packed representation in the database.
   * 
   * @param entity representation of a MailChimpCredentials in the database.
   * @return a fully constructed MailChimpCredentials
   */
  private static MailChimpCredentials fromEntity(Entity entity) {
    String[] fields = ((String)entity.getProperty(getDatastoreKey())).split("" + RS);
    if (fields.length < 2) return new MailChimpCredentials();
    return new MailChimpCredentials(fields[0], fields[1]);
  }

  private void addToCache() {
    MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
    syncCache.put(getMemcacheKey(), this);    
  }
  
  /** Only setters and getters below. */  
  public String getListId() {
    return listId;
  }

  public String getApiKey() {
    return apiKey;
  }
  
  public String debug() {
    return debug;
  }

}
