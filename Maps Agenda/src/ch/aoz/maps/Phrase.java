package ch.aoz.maps;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A phrase is a word, an abbreviation, or even a full sentence in a given
 * language. The various translations of a phrase are all grouped using the same
 * key.
 * */
public class Phrase implements Comparable<Phrase>, java.io.Serializable {
  public static final String entityKind = "Phrase";
  private static final long serialVersionUID = 161718L;

  /** Key that groups all the translations of the same phrase. */
  private String key;

  /** Language this phrase is in. */
  private String lang;

  /** The actual phrase. */
  private String phrase;

  /** Phrases are grouped together in sections in the UI. */
  private String group;

  /** Some phrases are used as tags for events. */
  private boolean isTag;

  /**
   * A phrase must have a key and a language defined. Otherwise, it is not
   * valid.
   */
  private boolean isOk;

  /** Create a new Phrase */
  public Phrase(String key, String lang, String phrase, String group,
      boolean isTag) {
    this.key = key;
    this.lang = lang;
    this.phrase = (phrase == null ? "" : phrase);
    this.group = (group == null ? "" : group);
    this.isTag = isTag;
    this.isOk = (key != null && key.length() > 0 && lang != null && lang
        .length() > 0);
  }

  /** Create a new Phrase from a JSON representation. */
  public Phrase(JSONObject o) throws JSONException {
    this.key = o.getString("key");
    this.lang = o.getString("lang");
    this.phrase = o.getString("phrase");
    this.group = o.getString("group");
    this.isTag = o.getBoolean("isTag");
    if (phrase == null)
      phrase = "";
    if (group == null)
      group = "";
    this.isOk = (key != null && key.length() > 0 && lang != null && lang
        .length() > 0);
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

  public boolean addToStore() {
    Phrases phrases = Phrases.GetPhrasesForLanguage(lang);
    if (!phrases.addPhrase(this))
      return false;
    return phrases.addToStore();
  }

  public static List<Phrase> GetPhrasesForLanguage(String language) {
    Phrases phrases = Phrases.GetPhrasesForLanguage(language);
    if (phrases == null)
      return null;
    return new ArrayList<Phrase>(phrases.getPhrases());
  }

  public static List<String> GetKeysForTags() {
    Phrases phrases = Phrases.GetPhrasesForLanguage("de");
    if (phrases == null)
      return null;
    ArrayList<String> keys = new ArrayList<String>();
    for (Phrase p : phrases.getPhrases()) {
      if (p.isTag()) {
        keys.add(p.getKey());
      }
    }
    return keys;
  }

  /** Only setters and getters below. */
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

  public JSONObject toJSON() {
    JSONObject json = new JSONObject();
    json.put("key", key);
    json.put("lang", lang);
    json.put("phrase", phrase);
    json.put("group", group);
    json.put("isTag", isTag);
    return json;
  }
}
