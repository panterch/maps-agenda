package ch.aoz.maps;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;

/** 
 * This class bundles all the phrases for a given language.
 * */
public class Phrases {
  public static final String entityKind = "Phrases";

  private String lang;
  private Map<String, Phrase> phrases;
  private boolean isOk;
  private String debug;
  
  public Phrases(Collection<Phrase> phrases) {
    this.phrases = new HashMap<String, Phrase>();
    isOk = false;
    if (phrases.isEmpty()) {
      debug = "empty";
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
    isOk = true;
  }
  
  public Phrases(Entity entity) {
    phrases = new HashMap<String, Phrase>();
    lang = entity.getKey().getName();
    for (String key : entity.getProperties().keySet()) {
      String s = (String)entity.getProperty(key);
      Phrase p = extractPhrase(lang, key, s);
      if (p != null) {
        phrases.put(key, p);
      }
    }
  }

  /**
   * Returns the Entity representation of this Phrases.
   *
   * @return an Entity with the properties of this Phrases.
   */
  public Entity toEntity() {
    Entity phrases = new Entity(entityKind, this.lang);
    for (Phrase p : this.phrases.values()) {
      phrases.setProperty(p.getKey(), packPhrase(p));
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
    return true;
  }
 
  public static Phrases GetPhrasesForLanguage(String language) {
    DatastoreService datastore = DatastoreServiceFactory
            .getDatastoreService();
    try {
     Entity e = datastore.get(KeyFactory.createKey(entityKind, language));
     return new Phrases(e);
    } catch (EntityNotFoundException e) {
      return null;
    }
  }

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

  /** 
   * Extracts a Phrase from the packed representation in the database.
   * 
   * @param lang language the phrase is in
   * @param key key of the phrase
   * @param value string of the phrase in the language
   * @return a fully constructed Phrase
   */
  public static Phrase extractPhrase(String lang, String key, String value) {
    String[] values = value.split(",", 3);
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
  public static String packPhrase(Phrase p) {
    StringBuilder s = new StringBuilder();
    s.append(p.isTag() ? "tag," : "notag,");
    s.append(p.getGroup());
    s.append(",");
    s.append(p.getPhrase());
    return s.toString();
  }
  
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
}
