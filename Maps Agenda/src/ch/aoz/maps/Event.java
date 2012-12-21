package ch.aoz.maps;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A MAPS event.
 */
public class Event {
  /**
   * Create a new Event with the specified parameters.
   *
   * @param year
   * @param month may be 0 for year-long events.
   * @param day may be 0 for month-long events.
   * @param index is the index of the event, starting with 0.
   */
  public Event(Long year, Long month, Long day, Long index) {
    this.year = year;
    this.month = month;
    this.day = day;
    this.index = index;
    this.key = KeyFactory.createKey("Event", CreateKey(year, month, day, index));
    this.ok = true;
  }

  /**
   * Parse an Entity Longo a new Event. Check isOk() if all fields could be populated.
   *
   * @param entity the entity to parse
   */
  public Event(Entity entity) {
    ok = true;
    if (entity.hasProperty("year")) {
      year = (Long) entity.getProperty("year");
    } else {
      year = 1900L;
      ok = false;
    }

    if (entity.hasProperty("month")) {
      month = (Long) entity.getProperty("month");
    } else {
      month = 0L;
    }

    if (entity.hasProperty("day")) {
      day = (Long) entity.getProperty("day");
    } else {
      day = 0L;
    }

    if (entity.hasProperty("index")) {
      index = (Long) entity.getProperty("index");
    } else {
      index = 0L;
      ok = false;
    }

    key = entity.getKey();
  }

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
    return true;
  }

  /**
   * Export this Event into an Entity.
   *
   * @return the generated Entity.
   */
  public Entity toEntity() {
    Entity result = new Entity("Event", key);
    result.setProperty("year", year);
    if (month > 0) {
      result.setProperty("month", month);
    }
    if (day > 0) {
      result.setProperty("day", day);
    }
    result.setProperty("index", index);
    return result;
  }

  /**
   * Render this Event into an XML tag.
   *
   * @return the generated XML tag without headers.
   */
  public static String getXML(Long yearFrom,
      Long monthFrom,
      Long dayFrom,
      Long yearTo,
      Long monthTo,
      Long dayTo) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    // Set up the range filter.
    Key smallest = KeyFactory.createKey("Event", CreateKey(yearFrom, monthFrom, dayFrom, 0L));
    Key largest =
        KeyFactory.createKey("Event", Event.CreateKey(yearTo, monthTo, dayTo, Long.MAX_VALUE));
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
    Long year = 0L;
    Long month = 0L;
    Long day = 0L;
    Long index = 0L;
    Key eventID = null;
    Iterator<Entity> iterator = items.iterator();
    while (iterator.hasNext()) {
      // Detect the entity type, and perform the appropriate actions.
      Entity entity = iterator.next();
      
      if (entity.getKey().getKind().equals("Event")) {
        Event event = new Event(entity);
        year = event.getYear();
        month = event.getMonth();
        day = event.getDay();
        eventID = entity.getKey();
        continue;
      }

      if (entity.getKey().getKind().equals("Translation")) {
        Translation translation = new Translation(entity);
        // Check that the translation has the right eventID.
        if (translation.getEventID() != eventID) {
          //continue;
        }
        xml += "\n" + translation.toXML(year, month, day);
      }
    }
    return xml;
  }

  public static String getXMLForMonth(Long year, Long month) {
    return getXML(year, month, 0L, year, month + 1L, 0L);
  }

  /**
   * Generate a datastore key from given dates.
   *
   * @param year the year to use
   * @param month the month to use, or 0 if full-year event
   * @param day the day to use, or 0 if full-month event
   * @param index the index of the event within the day
   * @return the generated key
   */
  public static String CreateKey(Long year, Long month, Long day, Long index) {
    String key = Long.toString(year);
    if (month > 0) {
      key += String.format("-%02d", month);
      if (day > 0) {
        key += String.format("-%02d", day);
      }
    }
    // The index always starts with 0 to ensure that these entries are output before the monthly or
    // yearly results if they are sorted by key.
    key += String.format("-0%05d", index);
    return key;
  }

  /**
   * Returns the next free index for a particular date that is not set.
   *
   * @param year
   * @param month may be 0 for year-long events
   * @param day may be 0 for month-long events
   * @return the last free index
   */
  public static Long GetNextFreeIndex(Long year, Long month, Long day) {
    // TODO handle race condition (read - modify - write) between here and addToStore.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Key smallest = KeyFactory.createKey("Event", CreateKey(year, month, day, 0L));
    Key largest = KeyFactory.createKey("Event", CreateKey(year, month, day, Long.MAX_VALUE));

    Filter minimum = new FilterPredicate(
        Entity.KEY_RESERVED_PROPERTY, Query.FilterOperator.GREATER_THAN_OR_EQUAL, smallest);
    Filter maximum =
        new FilterPredicate(Entity.KEY_RESERVED_PROPERTY, Query.FilterOperator.LESS_THAN, largest);
    Filter range = CompositeFilterOperator.and(minimum, maximum);

    Query query = new Query("Event").setFilter(range)
        .addSort(Entity.KEY_RESERVED_PROPERTY, SortDirection.ASCENDING);
    List<Entity> events = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());

    if (events.size() > 0) {
      return (new Event(events.get(events.size() - 1)).getIndex());
    } else {
      return 0L;
    }
  }

  /**
   * @return the year
   */
  public Long getYear() {
    return year;
  }

  /**
   * @param year the year to set
   */
  public void setYear(Long year) {
    this.year = year;
  }

  /**
   * @return the month
   */
  public Long getMonth() {
    return month;
  }

  /**
   * @param month the month to set
   */
  public void setMonth(Long month) {
    this.month = month;
  }

  /**
   * @return the day
   */
  public Long getDay() {
    return day;
  }

  /**
   * @param day the day to set
   */
  public void setDay(Long day) {
    this.day = day;
  }

  /**
   * @return the index
   */
  public Long getIndex() {
    return index;
  }

  /**
   * @param index the index to set
   */
  public void setIndex(Long index) {
    this.index = index;
  }

  /**
   * @return the key
   */
  public Key getKey() {
    return key;
  }

  /**
   * @param key the key to set
   */
  public void setKey(Key key) {
    this.key = key;
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

  /**
   * Queries the store for a comprehensive list of translations of this event.
   *
   * @return a list with language identifiers.
   */
  public List<String> getLanguages() {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
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

  private Long year;
  private Long month;
  private Long day;
  private Long index;
  private Key key;
  private boolean ok;
}
