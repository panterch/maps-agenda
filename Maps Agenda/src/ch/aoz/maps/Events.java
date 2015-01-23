package ch.aoz.maps;

import java.util.Calendar;
import java.util.HashSet;
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

  private Calendar calendar;
  private long nextEventKey;
  private SortedSet<Event> events;
  private boolean isOk;
  private String debug;

  public Events(Calendar c) {
    this.calendar = (Calendar) c.clone();
    this.calendar.set(Calendar.DATE, 1);
    this.calendar.clear(Calendar.HOUR);
    this.calendar.clear(Calendar.MINUTE);
    this.calendar.clear(Calendar.SECOND);
    this.calendar.clear(Calendar.MILLISECOND);    
    this.nextEventKey = 0;
    events = new TreeSet<Event>();
    debug = "ok";
    isOk = true;
  }
  
  public Events(Calendar c, Set<Event> events) {
    this(c);
    for (Event e : events) {
      this.events.add(e);
      if (e.hasKey() && e.getKey() > nextEventKey) {
        nextEventKey = e.getKey() + 1;
      }
    }
  }
  
  private Events(Calendar c, Entity entity) {
    this(c);
    for (String key : entity.getProperties().keySet()) {
      String s = (String)entity.getProperty(key);
      Event e = extractEvent(key, c, s);
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
  
  private static String getKey(Calendar c) {
    return String.format("%04d-%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH));
  }
  private static String getMemcacheKey(Calendar c) {
    return String.format("%s-%s", entityKind, getKey(c));
  }
  
  /** 
   * Returns the Events object. It contains all the events for the month 
   * specified in the calendar.
   */
  public static Events getEvents(Calendar c) {
    MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
    if (syncCache.contains(getMemcacheKey(c))) {
      return (Events)syncCache.get(getMemcacheKey(c));
    }
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    try {
      Entity e = datastore.get(KeyFactory.createKey(entityKind, getKey(c)));
      return new Events(c, e);
    } catch (EntityNotFoundException e) {
      return new Events(c);
    }
  }
  
  public static boolean addEvent(Event e) {
    if (e == null || !e.isOk()) 
      return false;
    
    Events events = getEvents(e.getCalendar());
    if (events == null) {
      return false;
    }
    if (events.events.contains(e)) {
      if (!events.events.remove(e)) {
        return false;
      }
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
   * Returns the Entity representation of this Languages.
   *
   * @return an Entity with the properties of this Languages.
   */
  private Entity toEntity() {
    Entity events = new Entity(entityKind, getKey(this.calendar));
    for (Event e : this.events) {
      if (!e.hasKey())
        e.setKey(nextEventKey++);
      events.setUnindexedProperty(Long.toString(e.getKey()), packEvent(e));
    }
    return events;
  }

  /** 
   * Extracts an Event from the packed representation in the database.
   * 
   * @param packed string representation of a Event in the database.
   * @return a fully constructed Event
   */
  private static Event extractEvent(String keyStr, Calendar c, String packed) {
    String[] fields = packed.split("" + RS);
    if (fields.length < 4) return null;
    int date;
    long key;
    try {
      date = Integer.parseInt(fields[0]);
      key = Long.parseLong(keyStr);
    } catch (NumberFormatException e) {
      return null;
    }
    
    Calendar cal = Event.toCalendar(c.getTime());
    cal.set(Calendar.DATE, date);
    Set<String> tags = new HashSet<String>();
    for (int i = 4; i < fields.length; ++i) {
      tags.add(fields[i]);
    }
    return new Event(cal, key, fields[1], fields[2], fields[3], tags);
  }
  
  /**
   * Packs the provided Event into its database representation.
   * 
   * @param e the event to pack in its database representation.
   * @return the packed representation.
   */
  private static String packEvent(Event e) {
    StringBuilder s = new StringBuilder();
    s.append(e.getCalendar().get(Calendar.DATE) + RS);
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
    syncCache.put(getMemcacheKey(this.calendar), this);    
  }
  
  /** Only setters and getters below. */  
  public SortedSet<Event> getSortedEvents() {
    return events;
  }
  public Calendar getCalendar() {
    return calendar;
  }
    
  public boolean isOk() {
    return isOk;
  }
  public String debug() {
    return debug;
  }
}
