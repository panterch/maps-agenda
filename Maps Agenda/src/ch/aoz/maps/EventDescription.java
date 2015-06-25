package ch.aoz.maps;

/**
 * Describes an event (title + description) in a given language.
 */
public class EventDescription implements java.io.Serializable {
  private static final long serialVersionUID = 161731L;
  public static final String entityKind = "EventDescription";

  /** Language the description is in */
  private String lang;
  /** Title of the event */
  private String title;
  /** Description of the event */
  private String desc;
  /** True if the language is set */
  private boolean isOk;
  
  public EventDescription(String lang, String title, String desc) {
    this.lang = lang;
    
    if (title == null || title.isEmpty()) {
	this.title = "";
    } else {
	this.title = Utils.replaceDoubleQuotes(title.trim());
    }
    if (desc == null || desc.isEmpty()) {
	this.desc = "";
    } else {
	this.desc = Utils.replaceDoubleQuotes(desc.trim());
    }
    
    isOk = (lang != null && !lang.isEmpty());
  }
  
  /** Only getters below */
  
  public String getLang() {
    return lang;
  }
  public String getTitle() {
    return title;
  }
  public String getDesc() {
    return desc;
  }
  public boolean isOk() {
    return isOk;
  }
}
