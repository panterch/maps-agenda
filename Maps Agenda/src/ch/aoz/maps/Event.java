package ch.aoz.maps;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
/**
 * A MAPS event.
 */
public class Event implements Comparable<Event>, java.io.Serializable {
  private static final long serialVersionUID = 161727L;
  public static final String entityKind = "Event";
  
  // Whether this event has a key assigned to it. If not, it usually means this
  // is a new event that is not stored. This boolean is not stored.
  private boolean hasKey;
  
  // Used to discriminate between events at the same date.
  private long key;
  
  // When this event happens.
  private Date date;
  
  // Old: German translation for this event.
  private Translation germanTranslation;
  
  // New: translation for this event in the requested language.
  private Translation translation;

  // New: Stuff that is always the same for all the translations of the same event.
  private String location;
  private String transit;
  private String url;
  private Set<String> tags;
  
  // Debugging stuff, not stored.
  private boolean ok;
  private List<String> errors;
  
  // We only sort the items according to their date. There is no ordering for
  // events happening at the same date.
  @Override
  public int compareTo(Event other) {
    return getDate().compareTo(other.getDate());
  }
  
  // Two events are equal iff they happen at the same date and they have the
  // same key. Note that events they have no key assigned yet cannot be equal
  // to another event.
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Event)) return false;
    Event e = (Event)o;
    return this.date.equals(e.date) 
        && this.hasKey && e.hasKey  // Both must have a key assigned.
        && this.key == e.key;
  }
  
  /**
   * Create a new Event with all the info required for the Events object. 
   */
  public Event(Date date, long key, String location, String transit, String url, Set<String> tags) {
    this.date = date;
    this.key = key;
    this.hasKey = true;
    this.location = (location != null ? location : "");
    this.transit = (transit != null ? transit : "");
    this.url = (url != null ? url : "");
    this.tags = (tags != null ? tags : new HashSet<String>());
    germanTranslation = null;
    translation = null;
    this.ok = true;
    if (date == null) {
      addError("Date is not defined");
    }
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
      germanTranslation = new Translation(entity.getKey(), "de", "", "", "", "", "");
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

  public static String GetNextEvents(Calendar from, int pageSize, 
                                     String startCursor, List<Event> eventList) {
    eventList.clear();

    DatastoreService datastore = DatastoreServiceFactory
        .getDatastoreService();
    
    // Set up the query.
    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(pageSize);
    if (startCursor != null) {
      fetchOptions.startCursor(Cursor.fromWebSafeString(startCursor));
    }
    
    Filter minimumFilter = new FilterPredicate("date",
        Query.FilterOperator.GREATER_THAN_OR_EQUAL, from.getTime());
    Query query = new Query(entityKind)
        .setFilter(minimumFilter)
        .addSort("date", SortDirection.ASCENDING);
    
    // Make the query.
    QueryResultList<Entity> results = datastore.prepare(query).asQueryResultList(fetchOptions);
    if (results != null) {
        for (Entity item : results) {
            eventList.add(new Event(item));
        }
    }
    return results.getCursor().toWebSafeString();
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
     * Sets the key
     */
    public void setKey(long key) {
        this.key = key;
        this.hasKey = true;
    }

    /**
	 * @return If true, this entity already has a key.
	 */
	public boolean hasKey() {
		return hasKey;
	}
	
	public Translation getTranslation() {
	  return translation;
	}
    public void setTranslation(Translation translation) {
      this.translation = translation;
    }
	public String getLocation() {
	  return location;
	}
    public String getTransit() {
      return transit;
    }
    public String getUrl() {
      return url;
    }
    public Set<String> getTags() {
      return tags;
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
