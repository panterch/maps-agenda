package ch.aoz.maps;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

/** 
 * A phrase is a word, an abbreviation, or even a full sentence in a given language. 
 * The various translations of a phrase are all grouped using the same key.
 * */
public class Phrase implements Comparable<Phrase> {
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
  
  /** Phrases are grouped together in sections in the UI. */
  private String group;
  public static final String groupProperty = "group";

  /** Some phrases are used as tags for events. */
  private boolean isTag;
  public static final String isTagProperty = "isTag";
  
  /** A phrase must have a key and a language defined. Otherwise, it is not valid. */
  private boolean isOk;
  
  /** Create a new Phrase */
  public Phrase(String key, String lang, String phrase, String group, boolean isTag) {
    this.key = key;
    this.lang = lang;
    this.phrase = (phrase == null ? "" : phrase);
    this.group = (group == null ? "" : group);
    this.isTag = isTag;
    this.isOk = (key != null && key.length() > 0 &&
                 lang != null && lang.length() > 0);  
  }

  public Phrase(Entity entity) {
    this((entity.hasProperty(keyProperty) ? (String)entity.getProperty(keyProperty) : null),
         (entity.hasProperty(langProperty) ? (String)entity.getProperty(langProperty) : null),
         (entity.hasProperty(phraseProperty) ? (String)entity.getProperty(phraseProperty) : null),
         (entity.hasProperty(groupProperty) ? (String)entity.getProperty(groupProperty) : null),
         (entity.hasProperty(isTagProperty) ? (Boolean)entity.getProperty(isTagProperty) : false));
  }

  @Override
  public int compareTo(Phrase p) {
    int group_compare = group.toLowerCase().compareTo(p.group.toLowerCase()); 
    if (group_compare == 0) {
      return key.toLowerCase().compareTo(p.key.toLowerCase());
    } else {
      return group_compare;
    }
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
    result.setProperty(groupProperty, group);
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

  public static List<String> GetKeysForTags() {
    // Create the language filter.
    Filter tagFilter = CompositeFilterOperator.and(
            FilterOperator.EQUAL.of(langProperty, "de"),
            FilterOperator.EQUAL.of(isTagProperty, true));
    List<Phrase> phrases = GetPhrases(tagFilter);
    ArrayList<String> keys = new ArrayList<String>();
    for (Phrase p : phrases) {
      keys.add(p.getKey());
    }
    return keys;
  }

  public static List<Phrase> GetPhrasesForKey(String key) {
    // Create the key filter.
    Filter keyFilter = new FilterPredicate(keyProperty,
        Query.FilterOperator.EQUAL, key);
    return GetPhrases(keyFilter);
  }

  public static Phrase GetPhrase(String language, String key) {
    Filter filter = CompositeFilterOperator.and(
        FilterOperator.EQUAL.of(langProperty, language),
        FilterOperator.EQUAL.of(keyProperty, key));

    List<Phrase> phrases = GetPhrases(filter);
    if (phrases.isEmpty()) {
      return null;
    } else {
      return phrases.get(0);
    }
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

  public static void deleteKey(String key) {
    Filter keyFilter = new FilterPredicate(keyProperty,
            Query.FilterOperator.EQUAL, key);
    DatastoreService datastore = DatastoreServiceFactory
            .getDatastoreService();
    Query query = new Query(entityKind).setFilter(keyFilter);
    List<Entity> items = datastore.prepare(query).asList(
        FetchOptions.Builder.withDefaults());
    ArrayList<Key> keys = new ArrayList<Key>();
    for (Entity item : items) {
      keys.add(item.getKey());
    }
    datastore.delete(keys);
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

  public String getGroup() {
    return group;
  }

  public void setGroup(String group) {
    this.group = group;
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
