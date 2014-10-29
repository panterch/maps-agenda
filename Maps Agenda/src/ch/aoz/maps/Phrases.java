package ch.aoz.maps;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.Entity;

/** 
 * This class bundles all the phrases for a given language.
 * */
public class Phrases {
  public static final String entityKind = "Phrases";

  private String lang;
  private List<Phrase> phrases;
  private boolean isOk;
  
  public Phrases(List<Phrase> phrases) {
    this.phrases = phrases;
    isOk = false;
    if (phrases.isEmpty()) {
      return;
    }
    lang = phrases.get(0).getLang();
    for (Phrase phrase : phrases) {
      if (phrase.getLang() != lang) {
        return;
      }
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
  
  public static Phrase extractPhrase(String lang, String key, String value) {
    String[] values = value.split(",", 3);
    if (values.length != 3) return null;
    boolean isTag = values[0].equals("tag");
    return new Phrase(key, lang, values[2], values[1], isTag);
  }
  
  public List<Phrase> getPhrases() {
    return phrases;
  }
}
