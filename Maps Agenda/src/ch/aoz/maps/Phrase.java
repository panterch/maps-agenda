package ch.aoz.maps;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;

/** 
 * A phrase is a word, an abbreviation, or even a full sentence in a given language. 
 * The various translations of a phrase are all grouped using the same key.
 * */
public class Phrase {
  public static final String entityKind = "Phrase";
  
  /** Key that groups all the translations of the same phrase. */
  private String key;
  public static final String keyProperty = "key";
  
  /** Language this phrase is in. */
  private String lang;
  public static final String langProperty = "lang";
  
  /** The actual phrase. */
  private String phrase;  
  public static final String phraseProperty = "phrase";
  
  /** Some phrases are used as tags for events. */
  private boolean isTag;
  public static final String isTagProperty = "isTag";
  
  /** A phrase must have a key and a language defined. Otherwise, it is not valid. */
  private boolean isOk;
  
  /** Create a new Phrase */
  public Phrase(String key, String lang, String phrase, boolean isTag) {
    this.key = key;
    this.lang = lang;
    this.phrase = (phrase == null ? "" : phrase);
    this.isTag = isTag;
    this.isOk = (key != null && key.length() > 0 &&
                 lang != null && lang.length() > 0);  
  }

  public Phrase(Entity entity) {
    this((entity.hasProperty(keyProperty) ? (String)entity.getProperty(keyProperty) : null),
         (entity.hasProperty(langProperty) ? (String)entity.getProperty(langProperty) : null),
         (entity.hasProperty(phraseProperty) ? (String)entity.getProperty(phraseProperty) : null),
         (entity.hasProperty(isTagProperty) ? (Boolean)entity.getProperty(isTagProperty) : false));
  }

  /**
   * Returns the Entity representation of this Phrase.
   *
   * @return an Entity with the properties of this Phrase.
   */
  public Entity toEntity() {
    Entity result = new Entity(entityKind);
    result.setProperty(keyProperty, key);
    result.setProperty(langProperty, lang);
    result.setProperty(phraseProperty, phrase);
    result.setProperty(isTagProperty, isTag);
    return result;
  }
  
  /**
   * Add this Phrase to the datastore.
   *
   * @return true if this operation succeeded.
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

  public static List<Phrase> GetPhrasesForLanguage(String language) {
    // Create the language filter.
    Filter langFilter = new FilterPredicate(langProperty,
        Query.FilterOperator.EQUAL, language);
    return GetPhrases(langFilter);
  }

  public static List<Phrase> GetPhrasesForKey(String key) {
    // Create the key filter.
    Filter keyFilter = new FilterPredicate(langProperty,
        Query.FilterOperator.EQUAL, key);
    return GetPhrases(keyFilter);
  }

  private static List<Phrase> GetPhrases(Filter f) {
    DatastoreService datastore = DatastoreServiceFactory
        .getDatastoreService();
    Query query = new Query(entityKind).setFilter(f);
    List<Entity> items = datastore.prepare(query).asList(
        FetchOptions.Builder.withDefaults());
    ArrayList<Phrase> phrases = new ArrayList<Phrase>();
    for (Entity item : items) {
      phrases.add(new Phrase(item));
    }
    return phrases;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getLang() {
    return lang;
  }

  public void setLang(String lang) {
    this.lang = lang;
  }

  public String getPhrase() {
    return phrase;
  }

  public void setPhrase(String phrase) {
    this.phrase = phrase;
  }

  public boolean isTag() {
    return isTag;
  }

  public void setTag(boolean isTag) {
    this.isTag = isTag;
  }

  public boolean isOk() {
    return isOk;
  }

  public void setOk(boolean isOk) {
    this.isOk = isOk;
  }
}
