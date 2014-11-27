package ch.aoz.maps;

import java.util.Arrays;
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

public class Languages implements java.io.Serializable {
  public static final String entityKind = "Languages";
  public static final char RS = 0x1e;  // Record separator.
  private static final long serialVersionUID = 161720L;

  private SortedMap<String, Language> languages;
  private boolean isOk;
  private String debug;

  public Languages() {
    languages = new TreeMap<String, Language>();
    debug = "ok";
    isOk = true;
  }
  
  public Languages(Collection<Language> languages) {
    this();
    for (Language l : languages) {
      this.languages.put(l.getCode(), l);
    }
  }
  
  private Languages(Entity entity) {
    languages = new TreeMap<String, Language>();
    for (String key : entity.getProperties().keySet()) {
      String s = (String)entity.getProperty(key);
      Language l = extractLanguage(key, s);
      if (l != null) {
        languages.put(key, l);
      }
    }
    debug = "ok";
    isOk = true;
    addToCache();
  }
  
  /** 
   * Returns the Languages object. It contains all the languages defined in the application.
   */
  public static Languages GetLanguages() {
    MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
    if (syncCache.contains(entityKind)) {
      return (Languages)syncCache.get(entityKind);
    }
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    try {
      Entity e = datastore.get(KeyFactory.createKey(entityKind, entityKind));
      return new Languages(e);
    } catch (EntityNotFoundException e) {
      return null;
    }
  }

  public static boolean addLanguage(Language l) {
    if (l == null || !l.isOk()) 
      return false;
    Languages langs = GetLanguages();
    if (langs == null) 
      return false;
    langs.languages.put(l.getCode(), l);
    return langs.addToStore();
  }
  
  /**
   * Add this Languages to the datastore.
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
   * Returns the Entity representation of this Languages.
   *
   * @return an Entity with the properties of this Languages.
   */
  private Entity toEntity() {
    Entity languages = new Entity(entityKind, entityKind);
    for (Language l : this.languages.values()) {
      languages.setProperty(l.getCode(), packLanguage(l));
    }
    return languages;
  }

  /** 
   * Extracts a Language from the packed representation in the database.
   * 
   * @param packed string representation of a Language in the database.
   * @return a fully constructed Language
   */
  private static Language extractLanguage(String code, String packed) {
    String[] fields = packed.split("" + RS, 12);
    if (fields.length != 12) return null;
    return new Language(
            code, fields[0], fields[1], 
            Arrays.asList(fields[2], fields[3], fields[4], fields[5], fields[6], fields[7], fields[8]),
            fields[9].equals("Y"), fields[10].equals("Y"), fields[11].equals("Y"));
  }
  
  /**
   * Packs the provided Language into its database representation.
   * 
   * @param l the language to pack in its database representation.
   * @return the packed representation.
   */
  private static String packLanguage(Language l) {
    StringBuilder s = new StringBuilder();
    s.append(l.getName() + RS);
    s.append(l.getGermanName() + RS);
    for (String day : l.getDaysOfTheWeek()) {
      s.append(day + RS);
    }
    s.append((l.isRightToLeft() ? "Y" : "N") + RS);
    s.append((l.isInAgenda() ? "Y" : "N") + RS);
    s.append((l.hasSpecificFormat() ? "Y" : "N"));
    return s.toString();
  }

  private void addToCache() {
    MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
    syncCache.put(entityKind, this);    
  }
  
  /** Only setters and getters below. */
  public Set<String> getSortedLanguageCodes() {
    return languages.keySet();
  }
  
  public SortedMap<String, Language> getSortedLanguages() {
    return languages;
  }
  
  public Language getLanguage(String code) {
    if (languages.containsKey(code))
      return languages.get(code);
    else return null;
  }
  
  public boolean isOk() {
    return isOk;
  }
  public String debug() {
    return debug;
  }
}
