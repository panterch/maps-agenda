package ch.aoz.maps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * TODO: Insert description here. (generated by tobulogic)
 */
public class Translator implements java.io.Serializable {
  public static final String entityKind = "Translator";
  private static final long serialVersionUID = 161722L;
  
  private final String email;
  public static final String emailProperty = "email";

  private final String name;
  public static final String nameProperty = "name";

  private final List<String> languages;
  public static final String languagesProperty = "languages";
  
  private final boolean ok;
  
  public Translator(String email, String name, List<String> languages) {
    this.email = email;
    this.name = name;
    this.languages = languages;
    boolean languagesOk = (languages != null && languages.size() > 0);
    if (languagesOk) {
      for (String lang : languages) {
        if (lang.length() != 2) {
          languagesOk = false;
          break;
        }
      }
    }
    this.ok = (email != null && name != null && languagesOk);
  }
  
  public static Map<String, Translator> getAllTranslators() {
    Translators translators = Translators.GetTranslators();
    if (translators == null) 
      return null;
    return translators.getSortedTranslators();
  }
  
  public static boolean AddTranslator(Translator t) {
    return Translators.addTranslator(t);
  }
  
  public static boolean exists(String email) {
    Translators translators = Translators.GetTranslators();
    if (translators == null) 
      return false;
    return translators.getSortedEmails().contains(email);
  }

  public String getLanguageString() {
    StringBuilder b = new StringBuilder();
    for (String lang : languages) {
      b.append(lang);
      b.append(",");
    }
    b.deleteCharAt(b.length() - 1);
    return b.toString();
  }

  public static List<String> parseLanguageString(String langs) { 
    if (langs == null)
      return new ArrayList<String>();
    List<String> list = Arrays.asList(langs.replace(" ", "").split(","));
    return list;
  }

  public static Translator GetByEmail(String email) {
    Translators translators = Translators.GetTranslators();
    if (translators == null) 
      return null;
    return translators.getTranslator(email);
  }
  
  public static boolean delete(String email) {
    return Translators.removeTranslator(email);
  }
  
  /**
   * @return the email
   */
  public String getEmail() {
    return email;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @return the languages
   */
  public List<String> getLanguages() {
    return languages;
  }

  /**
   * @return the ok
   */
  public boolean isOk() {
    return ok;
  }
}