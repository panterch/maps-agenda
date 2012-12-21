package ch.aoz.maps;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

/**
 * Translations for a certain MAPS event.
 */
public class Translation {
  public static final String entityKind = "Translation";

  public Translation(Key parentKey,
      String lang,
      String title,
      String desc,
      String location,
      String url) {
    this.eventID = parentKey;
    this.lang = lang;
    this.title = title;
    this.desc = desc;
    this.location = location;
    this.url = url;
    this.ok = true;
  }

  /**
   * Creates a Translation out of its Entity representation.
   *
   * @param entity the entity to use for initialization.
   */
  public Translation(Entity entity) {
    ok = true;

    eventID = entity.getKey().getParent();

    if (entity.hasProperty("lang")) {
      lang = (String) entity.getProperty("lang");
    } else {
      lang = new String("");
      ok = false;
    }

    if (entity.hasProperty("title")) {
      title = (String) entity.getProperty("title");
    } else {
      title = new String("");
      ok = false;
    }

    if (entity.hasProperty("desc")) {
      desc = (String) entity.getProperty("desc");
    } else {
      desc = new String("");
      ok = false;
    }

    if (entity.hasProperty("location")) {
      location = (String) entity.getProperty("location");
    } else {
      location = new String("");
      ok = false;
    }

    if (entity.hasProperty("url")) {
      url = (String) entity.getProperty("url");
    } else {
      url = new String("");
      ok = false;
    }
  }

  /**
   * Returns the Entity representation of this Translation.
   *
   * @return an Entity with the properties of this Translation.
   */
  public Entity toEntity() {
    Entity result = new Entity(entityKind, lang, eventID);
    result.setProperty("lang", lang);
    result.setProperty("title", title);
    result.setProperty("desc", desc);
    result.setProperty("location", location);
    result.setProperty("url", url);
    return result;
  }

  /**
   * Add this translation to the datastore.
   *
   * @return if this operation succeeded.
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
    return true;
  }

  /**
   * Outputs the XML representation of this translation.
   *
   * @param year
   * @param month
   * @param day
   * @return a string containing this XML representation.
   */
  public String toXML(int year, int month, int day) {
    if (!ok) {
      return new String("");
    }

    String result = new String();
    result += "<event>\n";
    if (day == 0) {
      if (month == 0) {
        result += "<date>" + String.format("%4d", year) + "</date>\n";
      } else {
        result += "<date>" + String.format("%02d.%4d", month, year) + "</date>\n";
      }
    } else {
      result += "<date>" + String.format("%02d.%02d.%4d", day, month, year) + "</date>\n";
    }
    result += "<lang>" + lang + "</lang>\n";
    result += "<title><![CDATA[" + title + "]]></title>\n";
    result += "<desc><![CDATA[" + desc + "]]></desc>\n";
    result += "<location><![CDATA[" + location + "]]></location>\n";
    result += "<url><![CDATA[" + url + "]]></url>\n";
    result += "</event>";
    return result;
  }

  /**
   * @return the eventID
   */
  public Key getEventID() {
    return eventID;
  }

  /**
   * @param eventID the eventID to set
   */
  public void setEventID(Key eventID) {
    this.eventID = eventID;
  }

  /**
   * @return the lang
   */
  public String getLang() {
    return lang;
  }

  /**
   * @param lang the lang to set
   */
  public void setLang(String lang) {
    this.lang = lang;
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
   * @return the desc
   */
  public String getDesc() {
    return desc;
  }

  /**
   * @param desc the desc to set
   */
  public void setDesc(String desc) {
    this.desc = desc;
  }

  /**
   * @return the location
   */
  public String getLocation() {
    return location;
  }

  /**
   * @param location the location to set
   */
  public void setLocation(String location) {
    this.location = location;
  }

  /**
   * @return the url
   */
  public String getUrl() {
    return url;
  }

  /**
   * @param url the url to set
   */
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * @return the ok
   */
  public boolean isOk() {
    return ok;
  }

  /**
   * @param ok the ok to set
   */
  public void setOk(boolean ok) {
    this.ok = ok;
  }

  private Key eventID;
  private String lang;
  private String title;
  private String desc;
  private String location;
  private String url;
  private boolean ok;
}
