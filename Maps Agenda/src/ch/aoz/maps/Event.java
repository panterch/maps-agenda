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
  public static final String entityKind = "Event";
  
  /**
   * Create a new Event with the specified parameters.
   *
   * @param year
   * @param month
   * @param day
   * @param title the German title of the event
   */
  public Event(int year, int month, int day, String title) {
    this.year = year;
    this.month = month;
    this.day = day;
    this.title = title;
    this.ok = true;
  }

  /**
   * Parse an Entity into a new Event. Check isOk() if all fields could be populated.
   *
   * @param entity the entity to parse
   */
  public Event(Entity entity) {
    ok = true;
    if (entity.hasProperty("year")) {
      year = ((Long) entity.getProperty("year")).intValue();
    } else {
      year = 1900;
      ok = false;
    }

    if (entity.hasProperty("month")) {
      month = ((Long) entity.getProperty("month")).intValue();
    } else {
      month = 0;
    }

    if (entity.hasProperty("day")) {
      day = ((Long) entity.getProperty("day")).intValue();
    } else {
      day = 0;
    }

    if (entity.hasProperty("title")) {
      title = (String) entity.getProperty("title");
    } else {
      title = "";
      ok = false;
    }
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
    Entity result = new Entity(entityKind, CreateKey(year, month, day, title));
    result.setProperty("year", year);
    if (month > 0) {
      result.setProperty("month", month);
    }
    if (day > 0) {
      result.setProperty("day", day);
    }
    result.setProperty("title", title);
    return result;
  }

  /**
   * Render this Event into an XML tag.
   *
   * @return the generated XML tag without headers.
   */
  public static String getXML(int yearFrom,
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

  /**
   * Generate a datastore key from given dates.
   *
   * @param year the year to use
   * @param month the month to use, or 0 if full-year event
   * @param day the day to use, or 0 if full-month event
   * @param title the German title of the event
   * @return the generated key
   */
  public static String CreateKey(int year, int month, int day, String title) {
    return String.format("%04d-%02d-%02d:%s", year, month, day, title);
  }

  /**
   * @return the year
   */
  public int getYear() {
    return year;
  }

  /**
   * @param year the year to set
   */
  public void setYear(int year) {
    this.year = year;
  }

  /**
   * @return the month
   */
  public int getMonth() {
    return month;
  }

  /**
   * @param month the month to set
   */
  public void setMonth(int month) {
    this.month = month;
  }

  /**
   * @return the day
   */
  public int getDay() {
    return day;
  }

  /**
   * @param day the day to set
   */
  public void setDay(int day) {
    this.day = day;
  }
  
  /**
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * @param title the title to set
   */
  public void setTitle(String title) {
    this.title = title;
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

  private String title;
  private int year;
  private int month;
  private int day;
  private boolean ok;
}
