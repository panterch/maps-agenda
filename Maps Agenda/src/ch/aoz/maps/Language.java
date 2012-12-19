package ch.aoz.maps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;

/**
 * Class encapsulating a language supported in the Maps Agenda.
 */
public class Language {
  public static final String entityKind = "Language";
  
  /**
   * Two-letter code for the language, such as en, de, or fr.
   */
  private final String code;
  public static final String codeProperty = "code";
  /**
   * Name as written in the language.
   */
  private final String name;
  public static final String nameProperty = "name";
  /**
   * Name as written in German.
   */
  private final String germanName;
  public static final String germanNameProperty = "germanName";
  /**
   * List of the abbreviations of the 7 days of the week, starting with Sunday.
   */
  private final List<String> abbreviatedDays;
  public static final String abbreviatedDaysProperty = "abbreviatedDays";
  /**
   * True if the language is written from right to left, false otherwise. 
   */
  private final boolean rightToLeft;
  public static final String rightToLeftProperty = "rightToLeft";
  /**
   * True if the language appears in the Maps Agenda. This can be used to test
   * new languages and is used in the UI to pre-populate which languages are 
   * selected by default. 
   */
  private final boolean inAgenda;
  public static final String inAgendaProperty = "inAgenda";
  /**
   * True if this language requires specific formatting in the Maps Agenda.
   */
  private final boolean hasSpecificFormat;
  public static final String hasSpecificFormatProperty = "hasSpecificFormat";
  /**
   * True if this language is well defined, i.e. has all its fields properly
   * filled.
   */
  private final boolean ok;
  
  public Language(
      String code,
      String name,
      String germanName,
      List<String> abbreviatedDays,
      boolean rightToLeft,
      boolean inAgenda,
      boolean hasSpecificFormat) {
    this.code = code;
    this.name = name;
    this.germanName = germanName;
    this.abbreviatedDays = abbreviatedDays;
    this.rightToLeft = rightToLeft;
    this.inAgenda = inAgenda;
    this.hasSpecificFormat = hasSpecificFormat;
    this.ok = (code != null)
        && (name != null)
        && (germanName != null)
        && abbreviatedDays.size() == 7 
        && code.length() == 2;
  }

  @SuppressWarnings("unchecked")
  public Language(Entity entity) {
    boolean ok = true;
    if (entity.hasProperty(codeProperty)) {
      code = (String)entity.getProperty(codeProperty);
    } else {
      code = "--";
      ok = false;
    }
    if (entity.hasProperty(nameProperty)) {
      name = (String)entity.getProperty(nameProperty);
    } else {
      name = "Invalid";
      ok = false;
    }
    if (entity.hasProperty(germanNameProperty)) {
      germanName = (String)entity.getProperty(germanNameProperty);
    } else {
      germanName = "Invalid";
      ok = false;
    }
    if (entity.hasProperty(abbreviatedDaysProperty)) {
      abbreviatedDays = (List<String>)entity.getProperty(abbreviatedDaysProperty);
      if (abbreviatedDays.size() != 7) {
        ok = false;
      }
    } else {
      abbreviatedDays = new ArrayList<String>();
      ok = false;
    }
    if (entity.hasProperty(rightToLeftProperty)) {
      rightToLeft = (Boolean)entity.getProperty(rightToLeftProperty);
    } else {
      rightToLeft = false;
      ok = false;
    }
    if (entity.hasProperty(inAgendaProperty)) {
      inAgenda = (Boolean)entity.getProperty(inAgendaProperty);
    } else {
      inAgenda = false;
      ok = false;
    }
    if (entity.hasProperty(hasSpecificFormatProperty)) {
      hasSpecificFormat = (Boolean)entity.getProperty(hasSpecificFormatProperty);
    } else {
      hasSpecificFormat = false;
      ok = false;
    }
    this.ok = ok;
  }
  
  public static HashMap<String, Language> getAllLanguages() {
    HashMap<String, Language> langs = new HashMap<String, Language>();

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query(entityKind);
    List<Entity> entities = 
        datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
    for (Entity e : entities) {
      Language l = new Language(e);
      langs.put(l.getCode(), l);
    }
    return langs;
  }

  public static boolean AddLanguage(Language lang) {
    if (!lang.isOk())
      return false;
    Entity e = new Entity(entityKind);
    e.setProperty(codeProperty, lang.getCode());
    e.setProperty(nameProperty, lang.getName());
    e.setProperty(germanNameProperty, lang.getGermanName());
    e.setProperty(abbreviatedDaysProperty, lang.getDaysOfTheWeek());
    e.setProperty(rightToLeftProperty, lang.isRightToLeft());
    e.setProperty(inAgendaProperty, lang.isInAgenda());
    e.setProperty(hasSpecificFormatProperty, lang.hasSpecificFormat());

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    try {
      datastore.put(e);
    } catch (Exception ex) {
      return false;
    }
    return true;
  }
  
  /**
   * @return the code
   */
  public String getCode() {
    return code;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @return the germanName
   */
  public String getGermanName() {
    return germanName;
  }

  /**
   * @return the abbreviatedDays
   */
  public List<String> getDaysOfTheWeek() {
    return abbreviatedDays;
  }

  /**
   * @return the abbreviatedDays
   */
  public String getDayOfTheWeek(int i) {
    if (i < 0 && i >= abbreviatedDays.size())
      return "";
    return abbreviatedDays.get(i);
  }

  /**
   * @return the rightToLeft
   */
  public boolean isRightToLeft() {
    return rightToLeft;
  }

  /**
   * @return the inAgenda
   */
  public boolean isInAgenda() {
    return inAgenda;
  }

  /**
   * @return the hasSpecificFormat
   */
  public boolean hasSpecificFormat() {
    return hasSpecificFormat;
  }

  /**
   * @return the ok
   */
  public boolean isOk() {
    return ok;
  }
}