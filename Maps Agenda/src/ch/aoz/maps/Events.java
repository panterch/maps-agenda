package ch.aoz.maps;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

public class Events implements java.io.Serializable {
  private static final long serialVersionUID = 161726L;
  public static final String entityKind = "Events";
  public static final String monthProperty = "Month";
  public static final String yearProperty = "Year";
  public static final char RS = 0x1e;  // Record separator.

  private int year;
  private int month;
  private long nextEventKey;
  private SortedSet<Event> events;
  private boolean isOk;
  private String debug;

  public Events(int year, int month) {
    this.year = year;
    this.month = month;
    this.nextEventKey = 0;
    events = new TreeSet<Event>();
    debug = "ok";
    isOk = true;
  }
  
  public Events(int year, int month, Set<Event> events) {
    this(year, month);
    for (Event e : events) {
      this.events.add(e);
      if (e.hasKey() && e.getKey() > nextEventKey) {
        nextEventKey = e.getKey() + 1;
      }
    }
  }
  
  private Events(int year, int month, Entity entity) {
    this(year, month);
    for (String key : entity.getProperties().keySet()) {
      String s = (String)entity.getProperty(key);
      Event e = extractEvent(key, s);
      if (e != null) {
        events.add(e);
        if (e.hasKey() && e.getKey() > nextEventKey) {
          nextEventKey = e.getKey() + 1;
        }
      }
    }
    debug = "ok";
    isOk = true;
    addToCache();
  }
  
  private static String getKey(int year, int month) {
    return String.format("%4d-%2d", year, month);
  }
  private static String getMemcacheKey(int year, int month) {
    return String.format("%s-%s", entityKind, getKey(year, month));
  }
  
  /** 
   * Returns the Languages object. It contains all the languages defined in the application.
   */
  public static Events getEvents(int year, int month) {
    MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
    if (syncCache.contains(getMemcacheKey(year, month))) {
      return (Events)syncCache.get(getMemcacheKey(year, month));
    }
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    try {
      Entity e = datastore.get(KeyFactory.createKey(entityKind, getKey(year, month)));
      return new Events(year, month, e);
    } catch (EntityNotFoundException e) {
      return new Events(year, month);
    }
  }
  
  public static boolean addEvent(Event e) {
    if (e == null || !e.isOk()) 
      return false;
    
    Events events = getEvents(e.getDate().getYear(), e.getDate().getMonth());
    if (events == null) 
      return false;
    if (events.events.contains(e)) {
      if (!events.events.remove(e))
        return false;
    }
    events.events.add(e);
    return events.addToStore();
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
    Entity events = new Entity(entityKind, getKey(year, month));
    for (Event e : this.events) {
      if (!e.hasKey())
        e.setKey(nextEventKey++);
      events.setUnindexedProperty(Long.toString(e.getKey()), packEvent(e));
    }
    return events;
  }

  /** 
   * Extracts a Language from the packed representation in the database.
   * 
   * @param packed string representation of a Language in the database.
   * @return a fully constructed Language
   */
  private static Event extractEvent(String key, int year, int month, String packed) {
    String[] fields = packed.split("" + RS);
    if (fields.length < 4) return null;
    int date;
    try {
      date = Integer.parseInt(fields[0]);
    } catch (NumberFormatException e) {
      return null;
    }
    
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.YEAR, year);
    cal.set(Calendar.MONTH, month);
    cal.set(Calendar.DATE, date);
    cal.set(Calendar.HOUR, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);

    return new Event(cal.getTime(), key, fields[1], 
            Arrays.asList(fields[2], fields[3], fields[4], fields[5], fields[6], fields[7], fields[8]),
            fields[9].equals("Y"), fields[10].equals("Y"), fields[11].equals("Y"));
  }
  
  /**
   * Packs the provided Language into its database representation.
   * 
   * @param l the language to pack in its database representation.
   * @return the packed representation.
   */
  private static String packEvent(Event e) {
    StringBuilder s = new StringBuilder();
    s.append(e.getDate().getDate() + RS);
    s.append(e.getLocation() + RS);
    s.append(e.getTransit() + RS);
    s.append(e.getUrl() + RS);
    for (String tag : e.getTags()) {
      s.append(tag + RS);
    }
    s.deleteCharAt(s.length() - 1);
    return s.toString();
  }

  private void addToCache() {
    MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
    syncCache.put(getMemcacheKey(year, month), this);    
  }
  
  /** Only setters and getters below. */  
  public SortedSet<Event> getSortedEvents() {
    return events;
  }
    
  public boolean isOk() {
    return isOk;
  }
  public String debug() {
    return debug;
  }
}
