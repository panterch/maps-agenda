package ch.aoz.maps;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;

/** 
 * This class bundles all the phrases for a given language.
 * */
public class Phrases {
  public static final String entityKind = "Phrases";

  private String lang;
  private List<Phrase> phrases;
  private boolean isOk;
  private String debug;
  
  public Phrases(List<Phrase> phrases) {
    this.phrases = phrases;
    isOk = false;
    if (phrases.isEmpty()) {
      debug = "empty";
      return;
    }
    lang = phrases.get(0).getLang();
    HashSet<String> keys = new HashSet<String>();
    for (Phrase phrase : phrases) {
      if (!phrase.getLang().equals(lang)) {
        debug = "diff langs" + lang + " != " + phrase.getLang();
        return;
      }
      if (keys.contains(phrase.getKey())) {
        debug = "dup keys";
        return;
      }
      keys.add(phrase.getKey());
    }
    isOk = true;
  }
  
  public Phrases(Entity entity) {
    phrases = new ArrayList<Phrase>();
    lang = entity.getKey().getName();
    for (String key : entity.getProperties().keySet()) {
      String s = (String)entity.getProperty(key);
      Phrase p = extractPhrase(lang, key, s);
      if (p != null) {
        phrases.add(p);
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
    for (Phrase p : this.phrases) {
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
  
  public List<Phrase> getPhrases() {
    return phrases;
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
