package ch.aoz.maps;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import com.google.appengine.api.datastore.EntityNotFoundException;

public class XMLExport {
  private List<Event> events;
  private List<Event> topicOfMonth;
  private List<Event> images;
  private List<Long> highlighted;

  /**
   * Creates a new XML export of a given list of events.
   *
   * @param events
   */
  public XMLExport(List<Event> events) {
    this.events = events;
    this.highlighted = new ArrayList<Long>();
  }

  public List<Event> getTopicOfMonth() {
    return topicOfMonth;
  }

  public void setTopicOfMonth(List<Event> topicOfMonth) {
    this.topicOfMonth = topicOfMonth;
  }

  public List<Event> getImages() {
    return images;
  }

  public void setImageList(List<Event> images) {
    this.images = images;
  }

  public void setHighlighted(List<Event> highlightedEvents) {
    for (Event event : highlightedEvents) {
      if (event.hasKey()) {
        highlighted.add(event.getKey());
      }
    }
  }
  
  /**
   * Filters a list of languages for the ones the InDesign template supports. Preserves order.
   * @param languages is the unfiltered list of languages.
   * @return the filtered version of languages in preserved order.
   */
    static public List<Language> FilterLanguagesForExport(
	    List<Language> languages) {
	List<Language> filteredLanguages = new ArrayList<Language>();
	for (Language language : languages) {
	    if (language.getCode() == "ma" || language.getCode() == "ti"
		    || language.getCode() == "so") {
		continue;
	    }
	    filteredLanguages.add(language);
	}
	return filteredLanguages;
    }

  /**
   * Render this Event into an XML tag.
   *
   * @return the generated XML tag.
   */
  public String getXML() {
    // Header.
    String xml = new String("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    xml += "<Root>";
    xml += "<Tag1>";

    List<Language> orderedLanguages = new ArrayList<Language>();
    orderedLanguages.addAll(Language.getAllLanguages().values());
    Collections.sort(orderedLanguages);
    orderedLanguages = FilterLanguagesForExport(orderedLanguages);
    for (Language language : orderedLanguages) {
      // For the rendering of the languages, the code must be translated into
      // the Standardized representation.
      xml += "<" + language.getStandardizedLanguageCode() + ">";
      xml += "<inh>";

      // Topic of the month.
      if (getTopicOfMonth() != null) {
        for (Event event : getTopicOfMonth()) {
          Translation translation;
          try {
            translation = getTranslation(event, language);
          } catch (EntityNotFoundException e) {
            continue;
          }
          xml += new XMLExportEntry(event, language, translation, true, true, false).getXML();
        }
      }

      // Log the date to detect when the day changes.
      Calendar currentDay = Calendar.getInstance();
      currentDay.clear();
      currentDay.set(1900, Calendar.JANUARY, 1);

      // Each entry.
      for (Event event : events) {
        Translation translation;
        try {
          translation = getTranslation(event, language);
        } catch (EntityNotFoundException e) {
          continue;
        }

        // Find out if the date changed between events.
        Calendar newDay = Calendar.getInstance();
        newDay.clear();
        newDay.setTime(event.getDate());
        boolean dateChanged =
            newDay.get(Calendar.DAY_OF_MONTH) != currentDay.get(Calendar.DAY_OF_MONTH)
            || newDay.get(Calendar.MONTH) != currentDay.get(Calendar.MONTH)
            || newDay.get(Calendar.YEAR) != currentDay.get(Calendar.YEAR);

        // Add the entry for this event.
        xml += new XMLExportEntry(event,
            language,
            translation,
            dateChanged,
            highlighted.contains(event.getKey()),
            true).getXML();

        // Log the new date.
        currentDay.setTime(newDay.getTime());
      }
      xml += "</inh>";
      xml += "</" + language.getStandardizedLanguageCode() + ">";
    }
    xml += "</Tag1>";

    // Images.
    xml += getXMLImages();

    xml += "</Root>";
    return xml;
  }

  /**
   * Retrieve the translation of an event in a particular language.
   *
   * @param event The event for which the translation is requested.
   * @param language The language which the translation should be in.
   * @return The translation of event in language.
   * @throws EntityNotFoundException
   */
  private static Translation getTranslation(Event event, Language language)
      throws EntityNotFoundException {
    Translation translation;
    // Attempt to fetch the appropriate translation, and fall back to the German version if it
    // cannot be found.
    try {
      translation = Translation.getTranslationForEvent(event, language.getCode());
    } catch (EntityNotFoundException e) {
      translation = Translation.getGermanTranslationForEvent(event);
    }
    return translation;
  }

  /**
   * Adds XML tags for image pages.
   *
   * @return The XML tag tree for all requested images, plus an "individual" item for manual
   *         editing.
   */
  private String getXMLImages() {
    String xml = new String();
    xml += "<bildtexte>";
    if (getImages() != null) {
      for (Event event : getImages()) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(event.getDate());
        String day = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH));
        String month = String.format("%02d", calendar.get(Calendar.MONTH) + 1);
        xml += "<bild_" + day + "_" + month + ">";

        String imageText = new String();
        
        List<Language> orderedLanguages = new ArrayList<Language>();
        orderedLanguages.addAll(Language.getAllLanguages().values());
        Collections.sort(orderedLanguages);
        orderedLanguages = FilterLanguagesForExport(orderedLanguages);
        for (Language language : orderedLanguages) {
          Translation translation;
          try {
            translation = getTranslation(event, language);
          } catch (EntityNotFoundException e) {
            continue;
          }
          String translatedTitle = new String("<b_titel aid:cstyle=\"bildtitel"
              + language.getXMLFormatSupplement() + "\">" + translation.getDesc() + "</b_titel>");
          if (imageText.length() > 0) {
            imageText += "<space aid:cstyle=\"space\" > </space>";
          }
          imageText += translatedTitle;
        }
        xml += getXMLImageTag(day, month, imageText);
        xml += "</bild_" + day + "_" + month + ">";
      }
    }

    xml += "<individuell>";
    xml +=
        getXMLImageTag("03", "12", "<b_titel aid:cstyle=\"bildtitel\">Text text text...</b_titel>");
    xml += "</individuell>";
    xml += "</bildtexte>";
    return xml;
  }

  /**
   * Render an XML image tag tree into a string.
   *
   * @param day A two-digit string that describes the day of the month.
   * @param month A two-digit string that describes the month in the year.
   * @param bTitelTag The b_titel tag that goes into the innermost level.
   * @return A string with the requested tag.
   */
  private static String getXMLImageTag(String day, String month, String bTitelTag) {
    String xml = new String();
    xml +=
        "<Table_main xmlns:aid5=\"http://ns.adobe.com/AdobeInDesign/5.0/\" aid5:tablestyle=\"ts_main\" xmlns:aid=\"http://ns.adobe.com/AdobeInDesign/4.0/\" aid:table=\"table\" aid:trows=\"1\" aid:tcols=\"1\">";
    xml +=
        "<Tag_main aid:table=\"cell\" aid:crows=\"1\" aid:ccols=\"1\" aid:ccolwidth=\"450\" aid5:cellstyle=\"cs_bildtitel\">";
    xml += "<bilddatum aid:pstyle=\"bilddatum\">" + day + "." + month + ".</bilddatum>\n";
    xml += "<p_titel aid:pstyle=\"bildtitel\">";
    xml += bTitelTag;
    xml += "</p_titel>";
    xml += "</Tag_main>";
    xml += "</Table_main>";
    return xml;
  }
}
