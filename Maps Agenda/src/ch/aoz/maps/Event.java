package ch.aoz.maps;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * A MAPS event.
 */
public class Event {
  public static final String entityKind = "Event";
  private boolean hasKey;
  private long key;
  private Date date;
  private Translation germanTranslation;
  private boolean ok;
  
  /**
   * Create a new Event with the specified parameters and key.
   *
   * @param date day at which the event takes place
   */
  public Event(Date date, Translation germanTranslation, long key) {
    this.date = date;
    this.germanTranslation = germanTranslation;
    this.key = key;
    this.ok = (date != null);
    hasKey = true;
  }

  /**
   * Create a new Event with the specified parameters.
   *
   * @param date day at which the event takes place
   * @param title the German title of the event
   */
  public Event(Date date, Translation germanTranslation) {
    this.date = date;
    this.germanTranslation = germanTranslation;
    this.ok = (date != null);
    this.key = 0;
    hasKey = false;
  }

  /**
   * Parse an Entity into a new Event. Check isOk() if all fields could be populated.
   *
   * @param entity the entity to parse
   */
  public Event(Entity entity) {
    key = entity.getKey().getId();
    hasKey = true; 
    
    ok = true;
    if (entity.hasProperty("date")) {
      date = (Date) entity.getProperty("date");
    } else {
      Calendar.getInstance();
      try {
        date = new SimpleDateFormat("yyyy-MM-dd").parse("1900-01-01");
      } catch (Exception e) {}
      ok = false;
    }
    
    try {
      germanTranslation = Translation.getGermanTranslationForEvent(this);
    } catch (EntityNotFoundException e) {
      germanTranslation = new Translation(entity.getKey(), "de", "", "", "", "");
      ok = false;
    }
  }

  public boolean addToStore() {
    if (!this.isOk() || !this.germanTranslation.isOk()) {
      return false;
    }

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Key key;
    try {
      key = datastore.put(this.toEntity());
      if (!hasKey()) {
        this.key = key.getId();
        hasKey = true;
      }
    } catch (Exception ex) {
      return false;
    }

    if (germanTranslation.getEventID() == null)
      germanTranslation.setEventID(key);
    return germanTranslation.addToStore();
  }

  /**
   * Export this Event into an Entity.
   *
   * @return the generated Entity.
   */
  public Entity toEntity() {
    Entity result = null;
    if (hasKey())
      result = new Entity(entityKind, getKey());
    else
      result = new Entity(entityKind);

    result.setProperty("date", date);
    return result;
  }

  public static List<Event> GetAllEvents() {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Query query = new Query(entityKind).addSort("date", SortDirection.ASCENDING);
    List<Entity> items = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
    
    ArrayList<Event> events = new ArrayList<Event>();
    for (Entity item : items) {
      events.add(new Event(item));
    }
    return events;
  }

  public static List<Event> GetEventListForMonth(int year, int month) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    // Set up the range filter.
    Calendar c = Calendar.getInstance();
    c.set(year, month, 1);
    Filter minimumFilter = new FilterPredicate(
        "date", Query.FilterOperator.GREATER_THAN_OR_EQUAL, c.getTime());

    c.add(month, 1);
    Filter maximumFilter = new FilterPredicate(
        "date", Query.FilterOperator.LESS_THAN, c.getTime());
    
    Filter rangeFilter = CompositeFilterOperator.and(minimumFilter, maximumFilter);

    Query query = new Query(entityKind).setFilter(rangeFilter)
        .addSort("date", SortDirection.ASCENDING);
    List<Entity> items = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
    
    ArrayList<Event> events = new ArrayList<Event>();
    for (Entity item : items) {
      events.add(new Event(item));
    }
    return events;
  }
  
  /**
   * Render this Event into an XML tag.
   *
   * @return the generated XML tag without headers.
   */
  /*
  public static String getXML(
      int yearFrom,
      int monthFrom,
      int dayFrom,
      int yearTo,
      int monthTo,
      int dayTo) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    // Set up the range filter.
    Key smallest = KeyFactory.createKey(entityKind, CreateKey(yearFrom, monthFrom, dayFrom, ""));
    Key largest =
        KeyFactory.createKey(entityKind, Event.CreateKey(yearTo, monthTo, dayTo + 1, ""));
    Filter minimumFilter = new FilterPredicate(
        Entity.KEY_RESERVED_PROPERTY, Query.FilterOperator.GREATER_THAN_OR_EQUAL, smallest);
    Filter maximumFilter =
        new FilterPredicate(Entity.KEY_RESERVED_PROPERTY, Query.FilterOperator.LESS_THAN, largest);
    Filter rangeFilter = CompositeFilterOperator.and(minimumFilter, maximumFilter);

    Query query = new Query().setFilter(rangeFilter)
        .addSort(Entity.KEY_RESERVED_PROPERTY, SortDirection.ASCENDING);
    List<Entity> items = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());

    if (items.size() == 0) {
      return "";
    }

    // Generate the XML.
    String xml = new String("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    int year = 0;
    int month = 0;
    int day = 0;
    int index = 0;
    Iterator<Entity> iterator = items.iterator();
    while (iterator.hasNext()) {
      // Detect the entity type, and perform the appropriate actions.
      Entity entity = iterator.next();
      
      if (entity.getKey().getKind().equals(Event.entityKind)) {
        Event event = new Event(entity);
        year = event.getYear();
        month = event.getMonth();
        day = event.getDay();
        continue;
      }

      if (entity.getKey().getKind().equals(Translation.entityKind)) {
        Translation translation = new Translation(entity);
        xml += "\n" + translation.toXML(year, month, day);
      }
    }
    return xml;
  }
  
  public static String getXMLForMonth(int year, int month) {
    return getXML(year, month, 0, year, month + 1, 0);
  }
  */

  /**
   * @return the date
   */
  public Date getDate() {
    return date;
  }

  /**
   * @param date the date of this event
   */
  public void setDate(Date date) {
    this.date = date;
  }

  /**
   * @return the key
   */
  public long getKey() {
    return key;
  }

  /**
   * @return If true, this entity already has a key. 
   */
  public boolean hasKey() {
    return hasKey;
  }
  
  /**
   * @return the validation status of this Event
   */
  public boolean isOk() {
    return ok;
  }

  /**
   * Validates or invalidates an Event.
   *
   * @param ok the validation status to set
   */
  public void setOk(boolean ok) {
    this.ok = ok;
  }

  public Translation getGermanTranslation() {
    return germanTranslation;
  }

  public void setGermanTranslation(Translation germanTranslation) {
    this.germanTranslation = germanTranslation;
  }
  
  /**
   * Queries the store for a comprehensive list of translations of this event.
   *
   * @return a list with language identifiers.
   */
  /*
  public List<String> getLanguages() {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Key key = KeyFactory.createKey(entityKind, CreateKey(year, month, day, title));
    Query translationQuery = new Query().setAncestor(key).setFilter(
        new FilterPredicate(Entity.KEY_RESERVED_PROPERTY, Query.FilterOperator.GREATER_THAN, key))
        .setKeysOnly();
    List<Entity> translations =
        datastore.prepare(translationQuery).asList(FetchOptions.Builder.withDefaults());

    // Extract the languages.
    Iterator<Entity> iterator = translations.iterator();
    List<String> languages = new ArrayList<String>();
    while (iterator.hasNext()) {
      languages.add(iterator.next().getKey().getName());
    }
    return languages;
  }
  */
}
