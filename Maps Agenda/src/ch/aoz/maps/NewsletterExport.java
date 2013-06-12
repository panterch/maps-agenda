package ch.aoz.maps;

import static ch.aoz.maps.NewsletterStyles.CONTAINER_CSS;
import static ch.aoz.maps.NewsletterStyles.DATE_CSS;
import static ch.aoz.maps.NewsletterStyles.DESC_CSS;
import static ch.aoz.maps.NewsletterStyles.EVENT_CSS;
import static ch.aoz.maps.NewsletterStyles.EVENT_LEFT_CSS;
import static ch.aoz.maps.NewsletterStyles.EVENT_RIGHT_CSS;
import static ch.aoz.maps.NewsletterStyles.FOOTER_CSS;
import static ch.aoz.maps.NewsletterStyles.LOCATION_CSS;
import static ch.aoz.maps.NewsletterStyles.PREHEADER_CSS;
import static ch.aoz.maps.NewsletterStyles.TITLE_CSS;
import static ch.aoz.maps.NewsletterStyles.URL_CSS;

import com.google.appengine.api.datastore.EntityNotFoundException;

import java.util.List;

import javax.annotation.Nullable;


/**
 * Generates the HTML for a newsletter of events in a given language.
 * 
 * Remaining:
 *   - get single-language working (german, and untranslated)
 *   - format the date.
 *   - escape the HTML
 *   - test RLT & other languages.
 *   - fill in footer copy text.
 */
public class NewsletterExport {
  private final List<Event> events;
  private final String lang;
  private final String urlRoot;
  private final String themeId;
  private final int year;
  private final int month;
  private final Subscriber subscriber;
  private StringBuilder out;
  
  /**
   * @param events Displayed events, should be translated in the given language.
   * @param lang Second (non-german) language for events, null for german.
   * @param urlRoot Root page that all served pages are relative to.
   * @param themeId ID of the theme, used for locating resources in static/themes.
   * @param year Year the newsletter is for.
   * @param month (0-based) Month the newsletter is for.
   * @param subscriber The subscriber this newsletter is for -
   *     or null for when rendering the public website version.
   */
  public NewsletterExport(List<Event> events, String lang,
      String urlRoot, String themeId,
      int year, int month,
      @Nullable Subscriber subscriber) {
    this.events = events;
    this.lang = lang;
    this.urlRoot = urlRoot;
    this.themeId = themeId;
    this.year = year;
    this.month = month;
    this.subscriber = subscriber;
  }
  
  public String render() {
    out = new StringBuilder();
    renderPreheader();
    
    out.append("<table border='0' cellpadding='0' cellspacing='0' width='600' style='" + CONTAINER_CSS + "'>");
    out.append("<tr>");
    out.append("<td align='center' valign='top'>");
    renderHeader();
    renderEvents();
    renderFooter();
    out.append("</td>");
    out.append("</tr>");
    out.append("</table>");
    
    String result = out.toString();
    out = null;
    return result;
  }
  
  /** Preheader HTML = top of page, above AOZ header. */
  private void renderPreheader() {
    out.append("<table border='0' cellpadding='10' cellspacing='0' width='600'>");
    out.append("<tr>");
    out.append("<td valign='top' style='padding: 0'>");
    out.append("<table border='0' cellpadding='10' cellspacing='0' width='600' style='" + PREHEADER_CSS + "'>");
    out.append("<tr>");
    out.append("<td valign='top'>");
    out.append("<div>");
    out.append("MAPS-AGENDA: Günstige Kultur- und Freizeitangebote</div>");
    out.append("</td>");
    out.append("<td valign='top' width='260'>");
    
    if (this.isEmail()) {
      out.append("<div>");
      out.append("Wird dieses E-Mail nicht korrekt angezeigt?<br>");
      addLink(monthPermalink(), "Öffnen Sie es im Broser.");
      out.append("</div>");
    }
    
    out.append("</td>");
    out.append("</tr>");
    out.append("</table>");
    out.append("</td>");
    out.append("</tr>");
    out.append("</table>");
  }
  
  /** Header HTML = Colored AOZ banner. */
  private void renderHeader() {
    String logoUrl = urlRoot + "/static/themes/" + themeId + "_header.png";
    
    out.append("<tr>");
    out.append("<td align='center' valign='top'>");
    out.append("<table border='0' cellpadding='0' cellspacing='0' width='600'>");
    out.append("<tr>");
    out.append("<td>");                                    
    out.append("<img src='" + logoUrl + "' style='width:100%' alt='MAPS Züri Agenda'>");
    out.append("</td>");
    out.append("</tr>");
    out.append("</table>");
    out.append("</td>");
    out.append("</tr>");
  }
  
  /** Event list, one row for each event. */
  private void renderEvents() {
    out.append("<td align='center' valign='top'>");
    
    out.append("<table border='0' cellpadding='0' cellspacing='0' width='600'>");
    out.append("<tr>");
    out.append("<td valign='top' width='280'>");

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
    
    out.append("<div style='" + EVENT_CSS + "'>");
    
    out.append("<div style='" + EVENT_LEFT_CSS + "'>");
    renderEventDetails(
        date,
        german.getTitle(),
        german.getDesc(),
        german.getLocation(),
        german.getUrl());
    out.append("</div>");
    
    out.append("<div style='" + EVENT_RIGHT_CSS + "'>");
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
    out.append(String.format("<h1 style='%s'>%s</h1>", DATE_CSS, date));
    out.append(String.format("<h2 style='%s'>%s</h2>", TITLE_CSS, title));
    out.append(String.format("<div style='%s'>%s</div>", DESC_CSS, description));
    out.append(String.format("<p style='%s'>%s</p>", LOCATION_CSS, location));
    out.append(String.format("<p style='%s'>", URL_CSS));
    out.append(String.format("<a onclick='this.target = '_blank';' href='%s'>%s</a>", url, url));
    out.append("</p>");
  }
  
  /** Foother HTML = Copy text and links to other pages. */
  private void renderFooter() {
    out.append("<tr>");
    out.append("<td align='center' valign='top'>");
    out.append("<table border='0' cellpadding='10' cellspacing='0' width='600'>");
    
    out.append("<tr>");
    out.append("<td valign='top' style='padding: 0'>");
    out.append("<table border='0' cellpadding='10' cellspacing='0' width='600' style='" + FOOTER_CSS + "'>");

    out.append("<tr>");
    out.append("<td valign='top' width='350'>");
    out.append("<div>");
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
    out.append("<div>");
    out.append("*|IF:REWARDS|* *|HTML:REWARDS|* *|END:IF|*");
    out.append("</div>");
    out.append("</td>");
    out.append("</tr>");

    if (this.isEmail()) {
      out.append("<tr>");
      out.append("<td colspan='2' valign='middle' id='utility'>");
      out.append("<div>");
      // addLink(shareLink(), "Weiterleiten");
      //out.append(" | ");
      addLink(unsubscribeLink(), "MAPS-Newsletter abbestellen");
      out.append(" | ");
      addLink(changeLanguageLink(), "Einstellungen");
      out.append("</div>");
      out.append("</td>");
      out.append("</tr>");
    }
    
    out.append("</table>");                            
    out.append("</td>");
    out.append("</tr>");
  }
  
  // Generators to build more complex properties based on the injected values.
  
  private boolean isEmail() {
    return subscriber != null;
  }
  
  private String unsubscribeLink() {
    return String.format("%s/unsubscribe.jsp?hash=%s",
        urlRoot, subscriber.getHash());
  }
  
  private String changeLanguageLink() {
    return String.format("%s/change_subscriber.jsp?hash=%s",
        urlRoot, subscriber.getHash());
  }
  
  private String monthPermalink() {
    return String.format("%s/newsletter.jsp?lang=%s&year=%s&month=%s",
        urlRoot, lang, year, month);
  }
  
  // HTML writing utilities
  
  private void addLink(String url, String text) {
    out.append("<a href='" + url + "' target='_blank'>");
    out.append(text);
    out.append("</a>");
  }
  
  // Utility - returns the first if provided, otherwise the second
  private static <T> T getOrDefault(T value, T defaultValue) {
    return value == null || value.toString().isEmpty() ? defaultValue : value;
  }
}
