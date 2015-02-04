package ch.aoz.maps;

import java.util.Calendar;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

public class EventDescriptions implements java.io.Serializable {
  private static final long serialVersionUID = 161729L;
  public static final String entityKind = "EventDescriptions";
  public static final char RS = 0x1e;  // Record separator.

  /** Month in which all the events take place */
  private Calendar month;
  /** Descriptions of the events that take place in 'month' */
  private Map<Long, EventDescription> descriptions;
  /** Language the descriptions are in */
  private String lang;
  /** Debug stuff */
  private boolean isOk;
  private String debug;

  public EventDescriptions(String lang, Calendar c) {
    this.lang = lang;
    month = Calendar.getInstance();
    month.clear();
    month.set(Calendar.YEAR, c.get(Calendar.YEAR));
    month.set(Calendar.MONTH, c.get(Calendar.MONTH));    
    month.set(Calendar.DATE, 1);
    
    descriptions = new TreeMap<Long, EventDescription>();
    debug = "ok";
    isOk = true;
  }
  
  public EventDescriptions(String lang, Calendar c, Set<Event> events) {
    this(lang, c);
    for (Event e : events) {
      this.descriptions.put(e.getKey(), e.getDescription());
    }
  }
  
  private EventDescriptions(String lang, Calendar c, Entity entity) {
    this(lang, c);
    for (String keyStr : entity.getProperties().keySet()) {
      Long key;
      try {
        key = Long.parseLong(keyStr);
      } catch (NumberFormatException e) {
        // Should not happen...
        debug = "Got wrong key: " + keyStr;
        continue;
      }
      String s = (String)entity.getProperty(keyStr);
      EventDescription d = extractDescription(lang, s);
      if (d != null) {
        descriptions.put(key, d);
      }
    }
    isOk = true;
    addToCache();
  }
  
  private static String getKey(String lang, Calendar month) {
    return String.format("%04d-%02d-%s", month.get(Calendar.YEAR), 
                                         month.get(Calendar.MONTH), 
                                         lang);
  }
  private static String getMemcacheKey(String lang, Calendar c) {
    return String.format("%s-%s", entityKind, getKey(lang, c));
  }
  
  /** 
   * Returns the Events object. It contains all the events for the month 
   * specified in the calendar.
   */
  public static EventDescriptions getDescriptions(String lang, Calendar c) {
    MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
    if (syncCache.contains(getMemcacheKey(lang, c))) {
      return (EventDescriptions)syncCache.get(getMemcacheKey(lang, c));
    }
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    try {
      Entity e = datastore.get(KeyFactory.createKey(entityKind, getKey(lang, c)));
      return new EventDescriptions(lang, c, e);
    } catch (EntityNotFoundException e) {
      return new EventDescriptions(lang, c);
    }
  }
  
  public static boolean add(Event e) {
    if (e == null || !e.hasKey() || e.getDescription() == null || !e.getDescription().isOk()) 
      return false;
    
    Long key = e.getKey();
    EventDescription d = e.getDescription();
    if (d == null || !d.isOk())
      return false;
    
    EventDescriptions descriptions = getDescriptions(d.getLang(), e.getCalendar());
    if (descriptions == null) {
      return false;
    }
    if (descriptions.descriptions.containsKey(key)) {
      descriptions.descriptions.remove(key);
    }
    descriptions.descriptions.put(key, d);
    return descriptions.addToStore();
  }

  /**
   * Add this EventDescriptions to the datastore.
   *
   * @return true if this operation succeeded.
   */
  public boolean addToStore() {
    if (!this.isOk()) {
      // debug = "not ok";
      return false;
    }
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
   * Returns the Entity representation of this EventDescriptions.
   *
   * @return an Entity with the properties of this EventDescriptions.
   */
  private Entity toEntity() {
    Entity descriptions = new Entity(entityKind, getKey(this.lang, this.month));
    for (Long key : this.descriptions.keySet()) {
      EventDescription d = this.descriptions.get(key);
      descriptions.setUnindexedProperty(Long.toString(key), packDescription(d));
    }
    return descriptions;
  }

  /** 
   * Extracts an EventDescription from the packed representation in the database.
   * 
   * @param packed string representation of a EventDescription in the database.
   * @return a fully constructed EventDescription
   */
  private static EventDescription extractDescription(String lang, String packed) {
    String[] fields = packed.split("" + RS);
    if (fields.length != 2) 
      return null;
    return new EventDescription(lang, fields[0], fields[1]);
  }
  
  /**
   * Packs the provided EventDescription into its database representation.
   * 
   * @param d the EventDescription to pack in its database representation.
   * @return the packed representation.
   */
  private static String packDescription(EventDescription d) {
    return d.getTitle() + RS + d.getDesc();
  }

  private void addToCache() {
    MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
    syncCache.put(getMemcacheKey(this.lang, this.month), this);    
  }
  
  /** Only setters and getters below. */
  public EventDescription getDescription(Long eventKey) {
    // Returns null if the key is not in the map.
    return descriptions.get(eventKey);
  }
  public Map<Long, EventDescription> getAllDescriptions() {
    return descriptions;
  }
  public Calendar getMonth() {
    return month;
  }
  public String getLang() {
    return lang;
  }
  public boolean isOk() {
    return isOk;
  }
  public String debug() {
    return debug;
  }
}
