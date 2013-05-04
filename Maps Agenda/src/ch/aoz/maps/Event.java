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
import java.util.HashMap;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ch.aoz.maps.Language;

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
    for (Entity item : items) {
      events.add(new Event(item));
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
  
	/**
	 * Render this Event into an XML tag.
	 * 
	 * @return the generated XML tag without headers.
	 */
	public static String getXML(int yearFrom, int monthFrom, int dayFrom,
			int yearTo, int monthTo, int dayTo) {
		// Set up range to export.
		Calendar from = Calendar.getInstance();
		from.clear();
		from.set(yearFrom, monthFrom, dayFrom);
		Calendar to = Calendar.getInstance();
		to.clear();
		to.set(yearTo, monthTo, dayTo);

		// Header.
		String xml = new String("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		xml += "<Root>\n";
		xml += "  <Tag1>\n";

		Map<String, Language> languages = Language.getAllLanguages();
		Map<Event, String> image_text = new HashMap<Event, String>();
		List<Event> topicOfMonth = new ArrayList<Event>(); // TODO pass in, or retrieve.

		for (Language language : languages.values()) {
			xml += "    <" + language.getCode() + ">\n";
			xml += "      <inh>\n";

			for (Event event : topicOfMonth) {
				Translation translation;
				try {
					translation = getTranslation(event, language);
				} catch (EntityNotFoundException e) {
					continue;
				}
				xml += getXMLEntry(translation, event, language, true, true, false);
			}

			// Log the date to detect when the day changes.
			Calendar currentDay = Calendar.getInstance();
			currentDay.clear();
			currentDay.set(yearFrom - 1, monthFrom - 1, dayFrom - 1); 

			// Each entry.
			for (Event event : GetEventListForTimespan(from, to)) {
				Translation translation;
				try {
					translation = getTranslation(event, language);
				} catch (EntityNotFoundException e) {
					continue;
				}

				// Find out if the date changed between events.
				Calendar newDay = Calendar.getInstance();
				newDay.clear();
				newDay.setTime(event.getDate());
				boolean dateChanged = newDay.get(Calendar.DAY_OF_MONTH) != currentDay
						.get(Calendar.DAY_OF_MONTH)
						|| newDay.get(Calendar.MONTH) != currentDay
								.get(Calendar.MONTH)
						|| newDay.get(Calendar.YEAR) != currentDay
								.get(Calendar.YEAR);

				boolean enlarge = true; // TODO Pass in, or retrieve.
						
				// Add the entry for this event.
				xml += getXMLEntry(translation, event, language, dateChanged, enlarge, true);
				
				// Log the new date.
				currentDay.setTime(newDay.getTime());

				// Add image title if desired.
				if (true) { // TODO Pass in, or retrieve.
					String image_title = new String(
							"            <b_titel aid:cstyle=\"bildtitel"
									+ language.getXMLFormatSupplement() + "\">"
									+ translation.getDesc() + "</b_titel>\n");
					if (image_text.containsKey(event)) {
						image_text
								.put(event,
										image_text.get(event)
												+ "            <space aid:cstyle=\"space\" > </space>"
												+ image_title);
					} else {
						image_text.put(event, image_title);
					}
				}
			}
			xml += "      </inh>\n";
			xml += "    </" + language.getCode() + ">\n";
		}
		xml += "  </Tag1>\n";

		if (image_text.size() > 0) {
			xml += "  <bildtexte>\n";
			for (Entry<Event, String> image : image_text.entrySet()) {
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(image.getKey().getDate());
				String day = String.format("%02d",
						calendar.get(Calendar.DAY_OF_MONTH));
				String month = String.format("%02d",
						calendar.get(Calendar.MONTH));
				xml += "    <bild_" + day + "_" + month + ">\n";
				xml += getXMLImageTag(day, month, image.getValue());
				xml += "    </bild_" + day + "_" + month + ">\n";
			}

			xml += "    <individuell>\n";
			xml += getXMLImageTag("03", "12", "Text text text...");
			xml += "    </individuell>\n";
			xml += "    </bildtexte>\n";
		}

		xml += "</Root>\n";

		return xml;
	}

	private static Translation getTranslation(Event event, Language language)
			throws EntityNotFoundException {
		Translation translation;
		// TODO assign the translation of this event to the given
		// language.
		translation = Translation.getGermanTranslationForEvent(event);
		return translation;
	}
	
	private static String getXMLImageTag(String day, String month,
			String bTitelTag) {
		String xml = new String();
		xml += "      <Table_main xmlns:aid5=\"http://ns.adobe.com/AdobeInDesign/5.0/\" aid5:tablestyle=\"ts_main\" xmlns:aid=\"http://ns.adobe.com/AdobeInDesign/4.0/\" aid:table=\"table\" aid:trows=\"1\" aid:tcols=\"1\">\n";
		xml += "        <Tag_main aid:table=\"cell\" aid:crows=\"1\" aid:ccols=\"1\" aid:ccolwidth=\"450\" aid5:cellstyle=\"cs_bildtitel\">\n";
		xml += "          <bilddatum aid:pstyle=\"bilddatum\"> " + day + "."
				+ month + ". </bilddatum>\n";
		xml += "          <p_titel  aid:pstyle=\"bildtitel\">\n";
		xml += bTitelTag;
		xml += "          </p_titel>\n";
		xml += "        </Tag_main>\n";
		xml += "      </Table_main>\n";
		return xml;
	}

	private static String getXMLEntry(Translation translation, Event event,
			Language language, boolean dateChanged, boolean enlarge, boolean printDay) {
		String xml = new String();

		if (!enlarge) {
			xml += "<Table_inside xmlns:aid5=\"http://ns.adobe.com/AdobeInDesign/5.0/\" aid5:tablestyle=\"ts_inside\" xmlns:aid=\"http://ns.adobe.com/AdobeInDesign/4.0/\" aid:table=\"table\" aid:trows=\"1\" aid:tcols=\"3\">\n";
			if (!language.isRightToLeft()) {
				xml += getXMLDayOfWeek(translation, event, language, enlarge, printDay);
				xml += getXMLDate(translation, event, enlarge, dateChanged);
				xml += getXMLSmallContents(translation, language, 358);
			} else {
				xml += getXMLSmallContents(translation, language, 374);
				xml += getXMLDate(translation, event, enlarge, dateChanged);
				xml += getXMLDayOfWeek(translation, event, language, enlarge, printDay);
			}
		} else {
			xml += "<Table_inside xmlns:aid5=\"http://ns.adobe.com/AdobeInDesign/5.0/\" aid5:tablestyle=\"ts_inside\" xmlns:aid=\"http://ns.adobe.com/AdobeInDesign/4.0/\" aid:table=\"table\" aid:trows=\"2\" aid:tcols=\"3\">\n";
			if (!language.isRightToLeft()) {
				xml += getXMLDayOfWeek(translation, event, language, enlarge, printDay);
				xml += getXMLDate(translation, event, enlarge, false);
				xml += getXMLTitle(translation, language, 303);
			} else {
				xml += getXMLTitle(translation, language, 374);
				xml += getXMLDate(translation, event, enlarge, false);
				xml += getXMLDayOfWeek(translation, event, language, enlarge, printDay);
			}
			xml += "  <Tag_inside aid:table=\"cell\" aid:crows=\"1\" aid:ccols=\"3\" aid5:cellstyle=\"cs_desc_gross\">\n";
			xml += "    <Inhalttag aid:pstyle=\"inhalt_gross"
					+ language.getXMLFormatSupplement() + "\">\n";
			xml += "      " + translation.getDesc();
			xml += "    </Inhalttag>\n";
			xml += getXMLLocation(translation, language, enlarge);
			xml += "  </Tag_inside>\n";
		}

		xml += "</Table_inside>";
		return xml;
	}

	private static String getXMLSmallContents(Translation translation,
			Language language, int width) {
		String xml = new String();
		xml += "  <Tag_inside aid:table=\"cell\" aid:crows=\"1\" aid:ccols=\"1\" aid:ccolwidth=\"374\" aid5:cellstyle=\"cs_desc\">\n";
		xml += "    <title aid:pstyle=\"titel"
				+ language.getXMLFormatSupplement() + "\">\n";
		xml += "      " + translation.getTitle() + "\n";
		xml += "    </title>\n";
		xml += "    <Inhalttag aid:pstyle=\"inhalt"
				+ language.getXMLFormatSupplement() + "\">\n";
		xml += "      " + translation.getDesc();
		xml += "    </Inhalttag>\n";
		xml += getXMLLocation(translation, language, false);
		xml += "  </Tag_inside>\n";
		return xml;
	}

	private static String getXMLDayOfWeek(Translation translation, Event event,
			Language language, boolean enlarge, boolean printDay) {
		String xml = new String();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(event.getDate());
		xml += "  <Tag_inside aid:table=\"cell\" aid:crows=\"1\" aid:ccols=\"1\" aid:ccolwidth=\""
				+ (language.isRightToLeft() ? "45" : "52")
				+ "\" aid5:cellstyle=\""
				+ (enlarge ? "cs_gross" : "cs_datum")
				+ "\" aid:pstyle=\"wochentag"
				+ language.getXMLFormatSupplement() + "\">\n";
		xml += "    <Wochentag>\n";
		if (printDay) {
			xml += "      "
					+ language.getDayOfTheWeek(calendar
							.get(Calendar.DAY_OF_WEEK) - 1) + "\n";
		}
		xml += "    </Wochentag>\n";
		xml += "  </Tag_inside>\n";
		return xml;
	}

	private static String getXMLDate(Translation translation, Event event,
			Boolean enlarge, Boolean date_changed) {
		String xml = new String();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(event.getDate());
		xml += "  <Tag_inside aid:table=\"cell\" aid:crows=\"1\" aid:ccols=\"1\" aid:ccolwidth=\"40\" aid5:cellstyle=\""
				+ (enlarge ? "cs_gross" : "cs_datum")
				+ "\" aid:pstyle=\"datum\">\n";
		if (!enlarge || date_changed) {
			xml += "    "
					+ String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH))
					+ "." + String.format("%02d", calendar.get(Calendar.MONTH))
					+ ".\n";
		}
		xml += "  </Tag_inside>\n";
		return xml;
	}

	private static String getXMLTitle(Translation translation,
			Language language, int width) {
		String xml = new String();
		xml += "  <Tag_inside aid:table=\"cell\" aid:crows=\"1\" aid:ccols=\"1\" aid:ccolwidth=\""
				+ Integer.toString(width)
				+ "\" aid5:cellstyle=\"cs_titel_gross\">\n";
		xml += "    <title aid:pstyle=\"titel"
				+ language.getXMLFormatSupplement() + "\">\n";
		xml += "      " + translation.getTitle() + "\n";
		xml += "    </title>\n";
		xml += "  </Tag_inside>\n";
		return xml;
	}

	private static String getXMLLocation(Translation translation,
			Language language, Boolean enlarge) {
		String xml = new String();
		if (translation.getLocation() != "") {
			if (enlarge) {
				xml += "    <Orttag aid:pstyle=\"ort_gross";
			} else {
				if (language.isRightToLeft()) {
					xml += "    <Orttag aid:pstyle=\"ort";
				} else {
					xml += "    <Orttag aid:pstyle=\"ort_rtl";
				}
			}
			// TODO remove special hack for _ru.
			xml += (language.getXMLFormatSupplement() == "_ru" ? language
					.getXMLFormatSupplement() : "") + "\">\n";
			xml += "      " + translation.getLocation() + "\n";

			if (translation.getUrl() != "") {
				// TODO remove this replacement hack.
				xml += "      "
						+ translation.getUrl().replace("www", "http://www")
						+ "\n";
			}
			xml += "    </Orttag>\n";
		}
		return xml;
	}

	/**
	 * @return the date
	 */
	public Date getDate() {
		return date;
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

	/**
	 * Validates or invalidates an Event.
	 * 
	 * @param ok
	 *            the validation status to set
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
