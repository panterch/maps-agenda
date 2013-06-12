package ch.aoz.maps;

import com.google.appengine.api.datastore.EntityNotFoundException;

import java.util.List;


/**
 * Generates the HTML for a newsletter of events in a given language.
 */
public class NewsletterExport {
  private final List<Event> events;
  private final String lang;
  private StringBuilder out;
  
  /**
   * @param events Displayed events, should be translated in the given language.
   * @param lang Second (non-german) language for events, null for german.
   */
  public NewsletterExport(List<Event> events, String lang) {
    this.events = events;
    this.lang = lang;
  }
  
  public String render() {
    out = new StringBuilder();
    renderPreheader();
    renderHeader();
    renderEvents();
    renderFooter();
    String result = out.toString();
    out = null;
    return result;
  }
  
  /** Preheader HTML = top of page, above AOZ header. */
  private void renderPreheader() {
    out.append("<div style='font-size: x-large; background-color: red; color: yellow'>IN PROGRESS</div>");
    out.append("<table border='0' cellpadding='10' cellspacing='0' width='600' id='templatePreheader'>");
    out.append("<tr>");
    out.append("<td valign='top' class='preheaderContent'>");
    out.append("<table border='0' cellpadding='10' cellspacing='0' width='100%'>");
    out.append("<tr>");
    out.append("<td valign='top'>");
    out.append("<div mc:edit='std_preheader_content'>");
    out.append("MAPS-AGENDA: Günstige Kultur- und Freizeitangebote</div>");
    out.append("</td>");
    out.append("<td valign='top' width='260'>");
    out.append("<div mc:edit='std_preheader_links'>");
    out.append("Wird dieses E-Mail nicht korrekt angezeigt?<br>");
    out.append("<a href='*|ARCHIVE|*' target='_blank'>Öffnen Sie es im Broser</a>.");
    out.append("</div>");
    out.append("</td>");
    out.append("</tr>");
    out.append("</table>");
    out.append("</td>");
    out.append("</tr>");
    out.append("</table>");
  }
  
  /** Header HTML = Colored AOZ banner. */
  private void renderHeader() {
    out.append("<tr>");
    out.append("<td align='center' valign='top'>");
    out.append("<table border='0' cellpadding='0' cellspacing='0' width='600' id='templateHeader'>");
    out.append("<tr>");
    out.append("<td class='headerContent'>");                                    
    out.append("<img src='header.gif' style='max-width:600px;' id='headerImage campaign-icon'>");
    out.append("</td>");
    out.append("</tr>");
    out.append("</table>");
    out.append("</td>");
    out.append("</tr>");
  }
  
  /** Event list, one row for each event. */
  private void renderEvents() {
    out.append("<td align='center' valign='top'>");
    
    out.append("<table border='0' cellpadding='0' cellspacing='0' width='600' id='templateBody'>");
    out.append("<tr>");
    out.append("<td valign='top' width='280' class='leftColumnContent'>");

    for (Event event : events) {
      renderEvent(event);
    }
    
    out.append("</td>");
    out.append("</tr>");
    out.append("</table>");
    
    out.append("</td>");
  }
  
  /** Renders a single event, in one or two languages. */
  private void renderEvent(Event event) {
    if (lang == null || "de".equals(lang)) {
      renderEventSingleLanguage(event);
    } else {
      try {
        renderEventDoubleLanguage(event);
      } catch (IllegalStateException e) {
        // Fall-back to just german when no translation is found.
        renderEventSingleLanguage(event);
      }
    }
  }
  
  /** Renders an event row just in German. */
  private void renderEventSingleLanguage(Event event) {
    Translation german = event.getGermanTranslation();
    // TODO
  }
  
  /** Renders an event row, in German plus the desired language. */ 
  private void renderEventDoubleLanguage(Event event) {
    Translation german = event.getGermanTranslation();
    Translation nonGerman = null;
    try {
      nonGerman = Translation.getTranslationForEvent(event, lang);
    } catch (EntityNotFoundException e) {
      throw new IllegalStateException(e);
    }

    String date = "TODO - date";
    
    out.append("<div class='event'>");
    out.append("<div class='event_left'>");
    
    renderEventDetails(
        date,
        german.getTitle(),
        german.getDesc(),
        german.getLocation(),
        german.getUrl());
    renderEventDetails(
        date,
        getOrDefault(nonGerman.getTitle(), german.getTitle()),
        getOrDefault(nonGerman.getDesc(), german.getDesc()),
        getOrDefault(nonGerman.getLocation(), german.getLocation()),
        getOrDefault(nonGerman.getUrl(), german.getUrl()));
    
    out.append("</div>");
    out.append("</div>");
  }
  
  /** Renders just the details for one event in one language. */
  private void renderEventDetails(
      String date, String title, String description, String location, String url) {
    out.append(String.format("<h1>%S</h1>", date));
    out.append(String.format("<h2>%S</h2>", title));
    out.append(String.format("<div class='desc'>%S</div>", description));
    out.append(String.format("<p class='location'>%S</p>", location));
    out.append("<p class='url'>");
    out.append(String.format("<a onclick='this.target = '_blank';' href='%S'>%S</a>", url));
    out.append("</p>");
  }
  
  /** Foother HTML = Copy text and links to other pages. */
  private void renderFooter() {
    out.append("<tr>");
    out.append("<td align='center' valign='top'>");
    out.append("<table border='0' cellpadding='10' cellspacing='0' width='600' id='templateFooter'>");
    out.append("<tr>");
    out.append("<td valign='top' class='footerContent'>");

    out.append("<table border='0' cellpadding='10' cellspacing='0' width='100%'>");
    out.append("<tr>");
    out.append("<td colspan='2' valign='middle' id='social'>");
    out.append("<div mc:edit='std_social'>");
    out.append("<a href='*|TWITTER:PROFILEURL|*'>folgen auf Twitter</a> |"); 
    out.append("<a href='*|FACEBOOK:PROFILEURL|*'>Freund werden auf Facebook</a> |");
    out.append("<a href='*|FORWARD|*'>Weiterleiten</a>");
    out.append("</div>");
    out.append("</td>");
    out.append("</tr>");

    out.append("<tr>");
    out.append("<td valign='top' width='350'>");
    out.append("<div mc:edit='std_footer'>");
    out.append("<em>Copyright &copy; *|CURRENT_YEAR|* *|LIST:COMPANY|*, Alle rechte vorbehalten.</em>");
    out.append("                <br>");
    out.append("*|IFNOT:ARCHIVE_PAGE|* *|LIST:DESCRIPTION|*");
    out.append("<br>");
    out.append("<strong>Unsere Mailadresse ist:</strong>");
    out.append("<br>");
    out.append("*|HTML:LIST_ADDRESS_HTML|**|END:IF|*"); 
    out.append("</div>");
    out.append("</td>");
    out.append("<td valign='top' width='190' id='monkeyRewards'>");
    out.append("<div mc:edit='monkeyrewards'>");
    out.append("*|IF:REWARDS|* *|HTML:REWARDS|* *|END:IF|*");
    out.append("</div>");
    out.append("</td>");
    out.append("</tr>");

    out.append("<tr>");
    out.append("<td colspan='2' valign='middle' id='utility'>");
    out.append("<div mc:edit='std_utility'>");
    out.append("<a href='*|UNSUB|*'>MAPS-Newsletter abbestellen</a> |"); 
    out.append("<a href='*|UPDATE_PROFILE|*'>Einstellungen</a>");
    out.append("</div>");
    out.append("</td>");
    out.append("</tr>");

    out.append("</table>");                            
    out.append("</td>");
    out.append("</tr>");
    out.append("</table>");
  }
  
  // Utility - returns the first if provided, otherwise the second
  private static <T> T getOrDefault(T value, T defaultValue) {
    return value != null ? value : defaultValue;
  }
}
