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
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

public class EventDescriptions implements java.io.Serializable {
  private static final long serialVersionUID = 161732L;
  public static final String entityKind = "EventDescriptions";
  public static final char RS = 0x1e;  // Record separator.

  /** Month in which all the events take place */
  private Calendar month;
  /** Descriptions of the events that take place in 'month' */
  private Map<Long, EventDescription> descriptions;
  /** Language the descriptions are in */
  private String lang;
  /** Debug stuff */
  private StringBuilder debug;

  public EventDescriptions(String lang, Calendar c) {
    this.lang = lang;
    month = Calendar.getInstance();
    month.clear();
    month.set(Calendar.YEAR, c.get(Calendar.YEAR));
    month.set(Calendar.MONTH, c.get(Calendar.MONTH));    
    month.set(Calendar.DATE, 1);
    
    descriptions = new TreeMap<Long, EventDescription>();
    debug = new StringBuilder();
  }
  
  public EventDescriptions(String lang, Calendar c, Set<Event> events) {
    this(lang, c);
    for (Event e : events) {
      if (e.getDescription() != null || e.getDescription().getLang().equals(lang)) {
        this.descriptions.put(e.getKey(), e.getDescription());
      }
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
        debug.append("Got wrong key: " + keyStr + "; ");
        continue;
      }
      Text t = (Text)entity.getProperty(keyStr);
      EventDescription d = extractDescription(lang, t.getValue());
      if (d != null) {
        descriptions.put(key, d);
      } else {
        debug.append("Get null description for event " + key + "; ");
      }
    }
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
  
  public static boolean addDescription(Event e) {
    if (e == null || !e.hasKey()) 
      return false;
    
    Long key = e.getKey();
    EventDescription d = e.getDescription();
    if (d == null || !d.isOk())
      return false;
    
    EventDescriptions descriptions = getDescriptions(d.getLang(), e.getCalendar());
    descriptions.descriptions.put(key, d);
    return descriptions.addToStore();
  }

  // Remove the descriptions for this event in all the supported languages. 
  public static boolean removeDescriptions(Event e) {
    if (e == null || !e.hasKey())
      return false;
    
    Languages langs = Languages.GetLanguages();
    
    boolean allRemoved = true;
    for (Language l : langs.getSortedLanguages()) {
      EventDescriptions descriptions = getDescriptions(l.getCode(), e.getCalendar());
      if (descriptions.descriptions.containsKey(e.getKey())) {
        descriptions.descriptions.remove(e.getKey());
        if (!descriptions.addToStore())
          allRemoved = false;
      }
    }
    return allRemoved;
  }
  /**
   * Add this EventDescriptions to the datastore.
   *
   * @return true if this operation succeeded.
   */
  public boolean addToStore() {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    try {
      datastore.put(this.toEntity());
    } catch (Exception ex) {
      ex.printStackTrace();
      debug.append("exception (datastore null? ");
      debug.append(datastore == null);
      debug.append("): " + ex.toString());
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
      descriptions.setUnindexedProperty(Long.toString(key), new Text(packDescription(d)));
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
  public String debug() {
    if (debug.length() == 0)
      return "ok";
    else return debug.toString();
  }
}
