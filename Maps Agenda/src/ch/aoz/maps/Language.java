package ch.aoz.maps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class encapsulating a language supported in the Maps Agenda.
 */
public class Language implements Comparable<Language>, java.io.Serializable {
  public static final String entityKind = "Language";
  private static final long serialVersionUID = 161721L;

  /**
   * Two-letter code for the language, such as en, de, or fr.
   */
  private final String code;

  /**
   * Name as written in the language.
   */
  private final String name;

  /**
   * Name as written in German.
   */
  private final String germanName;

  /**
   * List of the abbreviations of the 7 days of the week, starting with Sunday.
   */
  private final List<String> abbreviatedDays;

  /**
   * True if the language is written from right to left, false otherwise.
   */
  private final boolean rightToLeft;

  /**
   * True if the language appears in the Maps Agenda. This can be used to test
   * new languages and is used in the UI to pre-populate which languages are
   * selected by default.
   */
  private final boolean inAgenda;

  /**
   * True if this language requires specific formatting in the Maps Agenda.
   */
  private final boolean hasSpecificFormat;

  /**
   * True if this language is well defined, i.e. has all its fields properly
   * filled.
   */
  private final boolean ok;

  public Language(String code, String name, String germanName,
      List<String> abbreviatedDays, boolean rightToLeft, boolean inAgenda,
      boolean hasSpecificFormat) {
    this.code = code;
    this.name = name;
    this.germanName = germanName;
    this.abbreviatedDays = abbreviatedDays;
    this.rightToLeft = rightToLeft;
    this.inAgenda = inAgenda;
    this.hasSpecificFormat = hasSpecificFormat;
    this.ok = (code != null) && (name != null) && (germanName != null)
        && abbreviatedDays.size() == 7 && code.length() == 2;
  }

  public Language(JSONObject o) throws JSONException {
    this.code = o.getString("code");
    this.name = o.getString("name");
    this.germanName = o.getString("germanName");
    this.abbreviatedDays = new ArrayList<String>();
    for (int i = 0; i < o.getJSONArray("days").length(); i++) {
      this.abbreviatedDays.add(o.getJSONArray("days").getString(i));
    }
    this.rightToLeft = o.getBoolean("isRtl");
    this.inAgenda = o.getBoolean("inAgenda");
    this.hasSpecificFormat = o.getBoolean("specificFormat");
    this.ok = (code != null) && (name != null) && (germanName != null)
        && abbreviatedDays.size() == 7 && code.length() == 2;
  }

  public static Set<Language> getAllLanguages() {
    Languages languages = Languages.GetLanguages();
    if (languages == null)
      return null;
    return languages.getSortedLanguages();
  }

  public static Map<String, Language> getAllLanguagesAsMap() {
    Languages languages = Languages.GetLanguages();
    if (languages == null)
      return null;
    Map<String, Language> m = new HashMap<String, Language>();
    for (Language l : languages.getSortedLanguages()) {
      m.put(l.getCode(), l);
    }
    return m;
  }

  public static boolean AddLanguage(Language lang) {
    if (!lang.isOk())
      return false;
    return Languages.addLanguage(lang);
  }

  public static Language GetByCode(String key) {
    Languages languages = Languages.GetLanguages();
    if (languages == null)
      return null;
    return languages.getLanguage(key);
  }

  /**
   * @return the code
   */
  public String getCode() {
    return code;
  }

  /**
   * The web site (but not the XML export!) occasionally uses other codes than
   * the standard representation. This function translates the internal
   * representation into the standardized setting.
   * 
   * @return the standardized language code for the language.
   */
  public String getStandardizedLanguageCode() {
    Map<String, String> mapping = new HashMap<String, String>();
    mapping.put("al", "sq");
    mapping.put("bo", "sh");
    mapping.put("ma", "cjk");
    mapping.put("pe", "fa");
    mapping.put("po", "pt");
    mapping.put("tu", "tr");

    if (mapping.containsKey(code)) {
      return mapping.get(code);
    } else {
      return code;
    }
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

  public JSONObject toJSON() {
    JSONObject lang = new JSONObject();
    lang.put("code", code);
    lang.put("germanName", germanName);
    lang.put("name", name);
    for (String day : abbreviatedDays) {
      lang.append("days", day);
    }
    lang.put("isRtl", rightToLeft);
    lang.put("inAgenda", inAgenda);
    lang.put("specificFormat", hasSpecificFormat);
    return lang;
  }

  /**
   * @return a string supplement _xx if the language has a specific format, or
   *         the empty string otherwise
   */
  public String getXMLFormatSupplement() {
    if (hasSpecificFormat()) {
      return new String("_" + getStandardizedLanguageCode());
    } else {
      return new String();
    }
  }

  /**
   * MAPS use a manually predefined ordering of languages. The order needs to be
   * kept consistent on exports for the XML templates to work.
   * 
   * @return the ordering between the two elements
   */
  @Override
  public int compareTo(Language o) {
    if (this.getCode() == o.getCode()) {
      return 0;
    }

    // The manual ordering.
    Map<String, Integer> manualOrdering = new HashMap<String, Integer>();
    manualOrdering.put("de", 0);
    manualOrdering.put("al", 1);
    manualOrdering.put("ar", 2);
    manualOrdering.put("bo", 3);
    manualOrdering.put("en", 4);
    manualOrdering.put("fr", 5);
    manualOrdering.put("it", 6);
    manualOrdering.put("ma", 7);
    manualOrdering.put("pe", 8);
    manualOrdering.put("po", 9);
    manualOrdering.put("ru", 10);
    manualOrdering.put("so", 11);
    manualOrdering.put("es", 12);
    manualOrdering.put("ta", 13);
    manualOrdering.put("ti", 14);
    manualOrdering.put("tu", 15);

    Integer thisLang;
    if (manualOrdering.containsKey(this.getCode())) {
      thisLang = manualOrdering.get(this.getCode());
    } else {
      thisLang = Integer.MAX_VALUE;
    }

    Integer otherLang;
    if (manualOrdering.containsKey(o.getCode())) {
      otherLang = manualOrdering.get(o.getCode());
    } else {
      otherLang = Integer.MAX_VALUE;
    }

    return thisLang - otherLang;
  }
}
