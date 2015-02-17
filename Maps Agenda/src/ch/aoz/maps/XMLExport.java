package ch.aoz.maps;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XMLExport {
  private Calendar selected_month;
  private List<Event> events;
  private List<Event> topicOfMonth;
  private List<Event> images;
  private List<Long> highlighted;
  private Map<String, EventDescriptions> descriptions;
  
  /**
   * Creates a new XML export of a given list of events.
   *
   * @param events
   */
  public XMLExport(Calendar selected_month, 
                   List<Long> events, 
                   List<Long> topicOfMonth,
                   List<Long> images,
                   List<Long> highlightedEvents) {
    this.selected_month = selected_month;
    Events all_events = Events.getEvents(selected_month);
    this.events = new ArrayList<Event>();
    for (Long key : events) {
      Event e = all_events.getEvent(key);
      if (e != null)
        this.events.add(e);
    }
    
    this.topicOfMonth = new ArrayList<Event>();
    for (Long key : topicOfMonth) {
      Event e = all_events.getEvent(key);
      if (e != null)
        this.topicOfMonth.add(e);
    }

    this.images = new ArrayList<Event>();
    for (Long key : images) {
      Event e = all_events.getEvent(key);
      if (e != null)
        this.images.add(e);
    }
    
    this.highlighted = highlightedEvents;
    if (highlighted == null)
      highlighted = new ArrayList<Long>();
    
    descriptions = new HashMap<String, EventDescriptions>();
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
    orderedLanguages.addAll(Language.getAllLanguages());
    Collections.sort(orderedLanguages);
    for (Language language : orderedLanguages) {
      // For the rendering of the languages, the code must be translated into
      // the Standardized representation.
      xml += "<" + language.getStandardizedLanguageCode() + ">";
      xml += "<inh>";

      // Topic of the month.
      for (Event event : topicOfMonth) {
        EventDescription description = getDescription(event, language);
        if (description == null)
          continue;
        xml += new XMLExportEntry(event, language, description, true, true, false).getXML();
      }

      // Log the date to detect when the day changes.
      Calendar currentDay = Calendar.getInstance();
      currentDay.clear();
      currentDay.set(1900, Calendar.JANUARY, 1);

      // Each entry.
      for (Event event : events) {
        EventDescription description = getDescription(event, language);
        if (description == null)
          continue;

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
            description,
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
   * Retrieve the description of an event in a particular language.
   *
   * @param event The event for which the description is requested.
   * @param language The language which the description should be in.
   * @return The description of event in language or null if no description is found
   */
  private EventDescription getDescription(Event event, Language language) {
    if (!descriptions.containsKey(language.getCode())) {
      descriptions.put(language.getCode(), EventDescriptions.getDescriptions(language.getCode(), selected_month));
    }
    return descriptions.get(language.getCode()).getDescription(event.getKey());
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
    for (Event event : images) {
      Calendar calendar = event.getCalendar();
      String day = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH));
      String month = String.format("%02d", calendar.get(Calendar.MONTH) + 1);
      xml += "<bild_" + day + "_" + month + ">";

      String imageText = new String();
      
      List<Language> orderedLanguages = new ArrayList<Language>();
      orderedLanguages.addAll(Language.getAllLanguages());
      Collections.sort(orderedLanguages);
      for (Language language : orderedLanguages) {
        EventDescription description = getDescription(event, language);
        if (description == null)
          continue;
        String translatedTitle = new String("<b_titel aid:cstyle=\"bildtitel"
            + language.getXMLFormatSupplement() + "\">" + XMLExportEntry.escapeXML(description.getTitle()) + "</b_titel>");
        if (imageText.length() > 0) {
          imageText += "<space aid:cstyle=\"space\" > </space>";
        }
        imageText += translatedTitle;
      }
      xml += getXMLImageTag(day, month, imageText);
      xml += "</bild_" + day + "_" + month + ">";
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
