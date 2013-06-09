package ch.aoz.maps;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;

/**
 * A MAPS event.
 */
public class Event implements Comparable<Event> {
  public static final String entityKind = "Event";
  private boolean hasKey;
  private long key;
  private Date date;
  private Translation germanTranslation;
  private boolean ok;
  private List<String> errors;
  
  @Override
  public int compareTo(Event other) {
    return getDate().compareTo(other.getDate());
  }
  
  /**
   * Create a new Event with the specified parameters and key.
   *
   * @param date day at which the event takes place
   */
  public Event(Date date, Translation germanTranslation, long key) {
    this.ok = true;
    this.date = date;
    this.germanTranslation = germanTranslation;
    this.key = key;
    if (date == null) {
      addError("Date is not defined");
    }
    hasKey = true;
  }

  /**
   * Create a new Event with the specified parameters.
   *
   * @param date day at which the event takes place
   * @param germanTranslation the German translation of the event
   */
  public Event(Date date, Translation germanTranslation) {
    this.ok = true;
    this.date = date;
    this.germanTranslation = germanTranslation;
    if (date == null) {
      addError("Date is not defined");
    }
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
      try {
        date = new SimpleDateFormat("yyyy-MM-dd").parse("1900-01-01");
      } catch (Exception e) {}
      addError("Date is not defined.");
    }
    
    try {
      germanTranslation = Translation.getGermanTranslationForEvent(this);
    } catch (EntityNotFoundException e) {
      germanTranslation = new Translation(entity.getKey(), "de", "", "", "", "");
      addError("No German translation.");
    }
  }

  public boolean addToStore() {
    if (!this.germanTranslation.isOk()) {
      this.errors.addAll(this.germanTranslation.getErrors());
    }
    if (!this.isOk()) {
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
      addError(ex.getMessage());
      return false;
    }

    if (germanTranslation.getEventID() == null)
      germanTranslation.setEventID(key);
    boolean result = germanTranslation.addToStore();
    if (!result) {
      addError("Failed to save translation");
      this.errors.addAll(germanTranslation.getErrors());
    }
    return result;
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

  public static List<Event> GetEventListFromKeyList(List<Key> keyList) {
		DatastoreService datastore = DatastoreServiceFactory
				.getDatastoreService();

		Map<Key, Entity> items = datastore.get(keyList);
		ArrayList<Event> events = new ArrayList<Event>();
		if (items != null) {
			for (Entity item : items.values()) {
				events.add(new Event(item));
			}
		}
    Collections.sort(events);
		return events;
	}
  
  public static void DeleteEvent(long key) {
    DatastoreService datastore = DatastoreServiceFactory
        .getDatastoreService();
    datastore.delete(KeyFactory.createKey(entityKind, key));
  }

  public static List<Event> GetEventListForTimespan(Calendar from, Calendar to) {
    DatastoreService datastore = DatastoreServiceFactory
        .getDatastoreService();

    // Set up the range filter.
    Filter minimumFilter = new FilterPredicate("date",
        Query.FilterOperator.GREATER_THAN_OR_EQUAL, from.getTime());
    Filter maximumFilter = new FilterPredicate("date",
        Query.FilterOperator.LESS_THAN, to.getTime());
    Filter rangeFilter = CompositeFilterOperator.and(minimumFilter,
        maximumFilter);

    Query query = new Query(entityKind).setFilter(rangeFilter).addSort(
        "date", SortDirection.ASCENDING);
    List<Entity> items = datastore.prepare(query).asList(
        FetchOptions.Builder.withDefaults());

    ArrayList<Event> events = new ArrayList<Event>();
		if (items != null) {
			for (Entity item : items) {
				events.add(new Event(item));
			}
		}
    return events;
  }

  public static List<Event> GetEventListForMonth(int year, int month) {
    Calendar from = Calendar.getInstance();
    from.clear();
    from.set(year, month, 1);

    Calendar to = Calendar.getInstance();
    to.clear();
    to.setTime(from.getTime());
    to.add(Calendar.MONTH, 1);

    return GetEventListForTimespan(from, to);
  } 
  
  public static Event GetByKey(long key) throws EntityNotFoundException {
    DatastoreService datastore = DatastoreServiceFactory
        .getDatastoreService();
    Entity entity = datastore.get(KeyFactory.createKey(entityKind, key));
    return new Event(entity);
  } 
  
	public static String getXML(int yearFrom, int monthFrom, int dayFrom,
			int yearTo, int monthTo, int dayTo) {
		// DEPRECATED.
		
		// Set up range to export.
		Calendar from = Calendar.getInstance();
		from.clear();
		from.set(yearFrom, monthFrom, dayFrom);
		Calendar to = Calendar.getInstance();
		to.clear();
		to.set(yearTo, monthTo, dayTo);

		XMLExport export = new XMLExport(GetEventListForTimespan(from, to));
		export.setImageList(GetEventListForTimespan(from, to));
		export.setTopicOfMonth(GetEventListForTimespan(from, to));
		//export.setHighlighted(GetEventListForTimespan(from, to));
		return export.getXML();
	}
  
	/**
	 * @return the date
	 */
	public Date getDate() {
		return date;
	}
	
	private void addError(String error) {
      if (this.errors == null) {
        this.errors = new ArrayList<String>();
     }
	  this.errors.add(error);
	  this.ok = false;
	}
	
	public List<String> getErrors() {
	  if (this.errors == null) {
	    this.errors = new ArrayList<String>();
	    this.errors.add("No errors actually.");
	  }
	  return this.errors;
	}

	/**
	 * @param date
	 *            the date of this event
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

	public Translation getGermanTranslation() {
		return germanTranslation;
	}
	
	public Translation getTranslation(Language language) {
	    DatastoreService datastore = DatastoreServiceFactory
            .getDatastoreService();

	    Entity item;
        try {
          item = datastore.get(KeyFactory.createKey(
              KeyFactory.createKey(entityKind, getKey()),
              Translation.entityKind, 
              language.getCode()));
        } catch (EntityNotFoundException e) {
          return null;
        }  
        return new Translation(item);
	}

	public void setGermanTranslation(Translation germanTranslation) {
		this.germanTranslation = germanTranslation;
	}
	
	/**
	 * Queries the store for a comprehensive list of translations of this event.
	 * 
	 * @returns a list with language identifiers.
	 */
	/*
	 * public List<String> GetLanguages() { DatastoreService datastore =
	 * DatastoreServiceFactory.getDatastoreService(); Key key =
	 * KeyFactory.createKey(entityKind, CreateKey(year, month, day, title));
	 * Query translationQuery = new Query().setAncestor(key).setFilter( new
	 * FilterPredicate(Entity.KEY_RESERVED_PROPERTY,
	 * Query.FilterOperator.GREATER_THAN, key)) .setKeysOnly(); List<Entity>
	 * translations =
	 * datastore.prepare(translationQuery).asList(FetchOptions.Builder
	 * .withDefaults());
	 * 
	 * // Extract the languages. Iterator<Entity> iterator =
	 * translations.iterator(); List<String> languages = new
	 * ArrayList<String>(); while (iterator.hasNext()) {
	 * languages.add(iterator.next().getKey().getName()); } return languages; }
	 */
}
