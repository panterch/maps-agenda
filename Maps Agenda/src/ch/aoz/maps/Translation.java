package ch.aoz.maps;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;

import java.util.ArrayList;
import java.util.List;

// TODO everywhere: We need to sanitize the text (this was part of the XML output, but I think this should go everywhere). PHP code for this:
/*
 * function edittext($text,$sprache){
 * $text=trim($text);
 * $text=str_replace("\n", "", $text);
 * if($sprache=="ta"){
 *   $text=str_replace("&#160;", "", $text);
 * } else {
 *   $text=str_replace("&#160;", " ", $text);
 * }
 * $text=strip_tags($text);
 * return $text;
 * }
 */

/**
 * Translations for a certain MAPS event.
 */
public class Translation {
  public static final String entityKind = "Translation";
  private Key eventID;
  private String lang;
  private String title;
  private String desc;
  private String location;
  private String url;
  private boolean ok;
  private List<String> errors;

  public Translation(
      Key parentKey,
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
  
  public Translation(Event event, Language language) {
    this.eventID = KeyFactory.createKey(Event.entityKind, event.getKey());
    this.lang = language.getCode();
    this.ok = true;
  }

  public Translation(
          String lang,
          String title,
          String desc,
          String location,
          String url) {
        this.eventID = null;
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
      addError("Language is unknown.");
    }

    if (entity.hasProperty("title")) {
      title = (String) entity.getProperty("title");
    } else {
      title = new String("");
    }

    if (entity.hasProperty("desc")) {
      Object desc_property = entity.getProperty("desc");
      if (desc_property instanceof Text) {
        desc = ((Text) desc_property).getValue();
      } else {
        // For compatibility
        desc = (String) desc_property;
      }
    } else {
      desc = new String("");
    }

    if (entity.hasProperty("location")) {
      location = (String) entity.getProperty("location");
    } else {
      location = new String("");
    }

    if (entity.hasProperty("url")) {
      url = (String) entity.getProperty("url");
    } else {
      url = new String("");
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
    result.setProperty("desc", new Text(desc));
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
    if (this.eventID == null) {
      addError("Event id not set for the translation.");
    }
    if (!this.isOk()) {
      return false;
    }

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    try {
      datastore.put(this.toEntity());
    } catch (Exception ex) {
      addError(ex.getMessage());
      return false;
    }
    return true;
  }

  /**
   * Fetches the German translation for the provided event. 
   * @param e An event
   * @return the German translation.
   * @throws EntityNotFoundException 
   */
  public static Translation getGermanTranslationForEvent(Event e) throws EntityNotFoundException {
    return getTranslationForEvent(e, "de");
  }
  
  /**
   * Fetches the translation for the provided event. 
   * @param e An event
   * @return the German translation.
   * @throws EntityNotFoundException 
   */
  public static Translation getTranslationForEvent(Event e, String lang) throws EntityNotFoundException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Key key = new KeyFactory.Builder(Event.entityKind, e.getKey())
        .addChild(Translation.entityKind, lang).getKey();
    Entity entity = datastore.get(key);
    
    // The location is usually untranslated. Fall back to the German version.
    Translation translation = new Translation(entity);
    if (translation.getLocation().isEmpty() && e.getGermanTranslation().getLocation() != null) {
    	translation.setLocation(e.getGermanTranslation().getLocation());
    }
    
    return translation;
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
}
