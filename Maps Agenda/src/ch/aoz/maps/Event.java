package ch.aoz.maps;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
  private Calendar calendar;

  // New: description for this event (title and description in a given lang).
  private EventDescription description;

  // New: Stuff that is always the same for all the translations of the same event.
  private String location;
  private String transit;
  private String url;
  private Set<String> tags;

  // Debugging stuff, not stored.
  private boolean ok;
  private List<String> errors;

  // First sort according to their date. Then to key. Note that this method
  // should only return 0 if the two events have the same key.
  @Override
  public int compareTo(Event other) {
    int c = getDate().compareTo(other.getDate());
    if (c != 0) return c;

    if (this.hasKey() && other.hasKey())
      return Long.compare(this.key, other.key);
    if (this.hasKey())
      return -1;
    if (other.hasKey())
      return 1;
    
    // We now have to new events and two such events cannot be equal, so we need
    // to do our best to find a way to consistently order them. 
    c = this.location.compareTo(other.location);
    if (c != 0) return c;
    c = this.transit.compareTo(other.transit);
    if (c != 0) return c;
    c = this.url.compareTo(other.url);
    if (c != 0) return c;
    
    return Integer.compare(this.hashCode(), other.hashCode());
  }

  // Two events are equal if they have the same key. Note that events they have 
  // no key assigned yet cannot be equal to another event.
  @Override
  public boolean equals(Object o) {
    if (o == null || !(o instanceof Event)) {
      return false;
    }
    Event e = (Event) o;
    return this.hasKey && e.hasKey // Both must have a key assigned.
        && this.key == e.key;
  }

  /**
   * Create a new Event with all the info required for the Events object.
   */
  public Event(Calendar calendar, long key, String location, String transit,
               String url, Set<String> tags) {
    this.key = key;
    this.hasKey = true;
    this.location = (location != null ? location.trim() : "");
    this.transit = (transit != null ? transit.trim() : "");
    this.url = (url != null ? url.trim() : "");
    this.tags = new HashSet<String>();
    if (tags != null)
      this.tags.addAll(tags);
    description = null;
    this.ok = true;
    if (calendar == null) {
      this.calendar = null;
      addError("Date is not defined");
    } else {
      this.calendar = (Calendar) calendar.clone();
    }
  }

  /**
   * Create a new Event without key.
   */
  public Event(Calendar calendar, String location, String transit,
               String url, Set<String> tags, EventDescription d) {
    this.key = 0;
    this.hasKey = false;
    this.location = (location != null ? location.trim() : "");
    this.transit = (transit != null ? transit.trim() : "");
    this.url = (url != null ? url.trim() : "");
    this.tags = new HashSet<String>();
    if (tags != null)
      this.tags.addAll(tags);
    description = d;
    this.ok = true;
    if (calendar == null) {
      this.calendar = null;
      addError("Date is not defined");
    } else {
      this.calendar = (Calendar) calendar.clone();
    }
  }

  @Override
  public Event clone() {
    Event e = new Event(calendar, key, location, transit, url, tags);
    e.hasKey = hasKey;
    e.description = description;
    e.ok = ok;
    if (errors != null)
      e.errors.addAll(errors);    
    return e;
  }
    
  /**
   * @return the date
   */
  public Date getDate() {
    return calendar.getTime();
  }
  public Calendar getCalendar() {
    return calendar;
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

  public EventDescription getDescription() {
    return description;
  }

  public void setDescription(EventDescription description) {
    this.description = description;
  }
  
  public void clearLocationTransitUrl() {
    this.location = new String();
    this.transit = new String();
    this.url = new String();
  }

  public String getLocation() {
    return Utils.replaceDoubleQuotes(location);
  }

  public String getTransit() {
    return Utils.replaceDoubleQuotes(transit);
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

  public static Calendar toCalendar(Date d) {
    if (d == null) {
      return null;
    }
    Calendar c = Calendar.getInstance();
    c.setTime(d);
    c.clear(Calendar.HOUR);
    c.clear(Calendar.MINUTE);
    c.clear(Calendar.SECOND);
    c.clear(Calendar.MILLISECOND);
    return c;
  }
}
