package ch.aoz.maps;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

/** 
 * This class bundles all the phrases for a given language.
 * */
public class Phrases implements java.io.Serializable {
  public static final String entityKind = "Phrases";
  public static final char RS = 0x1e;  // Record separator.
  private static final long serialVersionUID = 161719L;

  private String lang;
  private Map<String, Phrase> phrases;
  private boolean isOk;
  private String debug;
  
  public Phrases(String lang) {
    this.phrases = new HashMap<String, Phrase>();
    this.lang = lang;
    isOk = true;
    debug = "ok";
  }
  
  public Phrases(Collection<Phrase> phrases) {
    this.phrases = new HashMap<String, Phrase>();
    isOk = false;
    if (phrases == null || phrases.isEmpty()) {
      debug = phrases == null ? "null" : "empty";
      return;
    }    
    lang = phrases.iterator().next().getLang();
    for (Phrase phrase : phrases) {
      if (!phrase.getLang().equals(lang)) {
        debug = "diff langs: " + lang + " != " + phrase.getLang();
        return;
      }
      if (this.phrases.containsKey(phrase.getKey())) {
        if (phrase.getPhrase().isEmpty()) {
          // Skip this value.
          continue;
        } else {
          Phrase p = this.phrases.get(phrase.getKey()); 
          if (!p.getPhrase().isEmpty() &&
              !p.getPhrase().equals(phrase.getPhrase())) {
            // Problem: two non-empty phrases. Which one to choose?
            if (!p.getGroup().isEmpty() && phrase.getGroup().isEmpty()) {
              continue;
            } else if (p.getGroup().isEmpty() == phrase.getGroup().isEmpty()) {
              debug = "dup keys: " + phrase.getKey() + " (" + phrase.getGroup() + ")";
              return;
            }
          }
        }
      }
      this.phrases.put(phrase.getKey(), phrase);
    }
    debug = "ok";
    isOk = true;
  }
  
  private Phrases(Entity entity) {
    phrases = new HashMap<String, Phrase>();
    lang = entity.getKey().getName();
    for (String key : entity.getProperties().keySet()) {
      String s = (String)entity.getProperty(key);
      Phrase p = extractPhrase(lang, key, s);
      if (p != null) {
        phrases.put(key, p);
      }
    }
    debug = "ok";
    isOk = true;
    addToCache();
  }

  /**
   * Returns the Entity representation of this Phrases.
   *
   * @return an Entity with the properties of this Phrases.
   */
  private Entity toEntity() {
    Entity phrases = new Entity(entityKind, this.lang);
    for (Phrase p : this.phrases.values()) {
      phrases.setUnindexedProperty(p.getKey(), packPhrase(p));
    }
    return phrases;
  }

  /**
   * Add this Phrases to the datastore.
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
    addToCache();
    return true;
  }
 
  /** 
   * Returns the Phrases object for the specified language. The Phrases object
   * contains all the phrases available in that language.
   * 
   * @param language The language in which the phrases should be in. 
   * @return The phrases object corresponding to the requested language.
   */
  public static Phrases GetPhrasesForLanguage(String language) {
    MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
    if (syncCache.contains(entityKind + "_" + language)) {
      return (Phrases)syncCache.get(entityKind + "_" + language);
    }
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    try {
     Entity e = datastore.get(KeyFactory.createKey(entityKind, language));
     return new Phrases(e);
    } catch (EntityNotFoundException e) {
      return new Phrases(language);
    }
  }

  /**
   * Returns a map of all the phrases defined in the system in a given language.
   * If a phrase is not yet translated in the requested language, the German version
   * of the phrase is provided.
   * 
   * @param lang Language in which to get the phrases.
   * @return The map of phrase's key to phrase. If a phrase is not found in the
   *         requested language, the German version is provided instead.
   */
  public static Map<String, Phrase> getMergedPhrases(String lang) {
    Map<String, Phrase> phrases = new HashMap<String, Phrase>();
    Phrases langPhrases = Phrases.GetPhrasesForLanguage(lang);
    if (langPhrases != null) {
      for (Phrase phrase : langPhrases.getPhrases()) {
        if (phrase.getPhrase().length() > 0) {
          phrases.put(phrase.getKey(), phrase);
        }
      }
    }
    if (lang != "de") {
      Phrases dePhrases = Phrases.GetPhrasesForLanguage("de");
      if (dePhrases != null) {
        for (Phrase phrase : dePhrases.getPhrases()) {
          if (!phrases.containsKey(phrase.getKey())) {
            phrases.put(phrase.getKey(), phrase);
          }
        }
      }
    }
    return phrases;
  }

  public static boolean deleteKey(String key) {
    Languages langs = Languages.GetLanguages();
    if (langs == null)
      return false;
    boolean allDeleted = true;
    for (Language l : langs.getSortedLanguages()) {
      Phrases phrases = Phrases.GetPhrasesForLanguage(l.getCode());
      if (phrases == null)
        continue;
      phrases.phrases.remove(key);
      if (!phrases.addToStore())
        allDeleted = false;
    }
    return allDeleted;
  }
  
  /** 
   * Extracts a Phrase from the packed representation in the database.
   * 
   * @param lang language the phrase is in
   * @param key key of the phrase
   * @param value string of the phrase in the language
   * @return a fully constructed Phrase
   */
  private static Phrase extractPhrase(String lang, String key, String value) {
    String[] values = value.split("" + RS, 3);
    if (values.length != 3) return null;
    boolean isTag = values[0].equals("tag");
    return new Phrase(key, lang, values[2], values[1], isTag);
  }
  
  /**
   * Packs the provided Phrase into its database representation.
   * 
   * @param p the phrase to pack in its database representation.
   * @return the packed representation.
   */
  private static String packPhrase(Phrase p) {
    StringBuilder s = new StringBuilder();
    s.append((p.isTag() ? "tag" : "notag") + RS);
    s.append(p.getGroup() + RS);
    s.append(p.getPhrase());
    return s.toString();
  }

  private String getCacheKey() {
    return entityKind + "_" + lang;
  }
  
  private void addToCache() {
    MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
    syncCache.put(getCacheKey(), this);    
  }

  /** Only setters and getters below. */
  public Collection<Phrase> getPhrases() {
    return phrases.values();
  }
  public String getLang() {
    return lang;
  }
  public boolean isOk() {
    return isOk;
  }
  public String debug() {
    return debug;
  }
  public boolean addPhrase(Phrase p) {
    if (!lang.equals(p.getLang())) 
      return false;
    phrases.put(p.getKey(), p);
    return true;
  }
}
