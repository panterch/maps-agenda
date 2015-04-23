package ch.aoz.maps;

import static ch.aoz.maps.NewsletterStyles.CONTAINER_CSS;
import static ch.aoz.maps.NewsletterStyles.DATE_CSS;
import static ch.aoz.maps.NewsletterStyles.DATE_FORMATTER;
import static ch.aoz.maps.NewsletterStyles.DESC_CSS;
import static ch.aoz.maps.NewsletterStyles.DISCLAIMER_CSS;
import static ch.aoz.maps.NewsletterStyles.DISCLAIMER_TEXT;
import static ch.aoz.maps.NewsletterStyles.ESCAPE_ATTRIBUTE;
import static ch.aoz.maps.NewsletterStyles.ESCAPE_TEXT;
import static ch.aoz.maps.NewsletterStyles.EVENT_CSS;
import static ch.aoz.maps.NewsletterStyles.EVENT_LEFT_CSS;
import static ch.aoz.maps.NewsletterStyles.EVENT_RIGHT_CSS;
import static ch.aoz.maps.NewsletterStyles.EVENT_SINGLE_CSS;
import static ch.aoz.maps.NewsletterStyles.FOOTER_CSS;
import static ch.aoz.maps.NewsletterStyles.HEADER_IMG_CSS;
import static ch.aoz.maps.NewsletterStyles.LOCATION_CSS;
import static ch.aoz.maps.NewsletterStyles.MAKE_ABSOLUTE_LINK;
import static ch.aoz.maps.NewsletterStyles.PREHEADER_CSS;
import static ch.aoz.maps.NewsletterStyles.RIGHT_ALIGN_CSS;
import static ch.aoz.maps.NewsletterStyles.RTL_CSS;
import static ch.aoz.maps.NewsletterStyles.TITLE_CSS;
import static ch.aoz.maps.NewsletterStyles.TRANSIT_CSS;
import static ch.aoz.maps.NewsletterStyles.URL_CSS;
import static ch.aoz.maps.NewsletterStyles.WHATS_UP_CSS;

import java.util.Map;

import javax.annotation.Nullable;

/**
 * Generates the HTML for a newsletter of events in a given language.
 * NOTE: *not* threadsafe. Each caller should create their own instance.
 *
 * Remaining:
 *   - ensure it looks ok in emails.
 */
public class NewsletterExport {
  // For field details, see the parameter docs in the constructor.
  private final Events eventsDe;
  private final Events eventsLang;
  private final Language language;
  private final String urlRoot;
  private final int year;
  private final int month;
  private final Subscriber subscriber;

  // Local variable used to stream the resulting HTML to.
  private StringBuilder out;
  
  /**
   * @param eventsDe Events object containing the events with German descriptions to display. 
   * @param eventsLang Events object containing the same events as eventDe, but in a different language.
   * @param lang Second (non-german) language for events, null for german.
   * @param urlRoot Root page that all served pages are relative to.
   * @param year Year the newsletter is for.
   * @param month (0-based) Month the newsletter is for.
   * @param subscriber The subscriber this newsletter is for -
   *     or null for when rendering the public website version.
   */
  public NewsletterExport(Events eventsDe, @Nullable Events eventsLang, String lang,
      String urlRoot, int year, int month,
      @Nullable Subscriber subscriber) {
    this.eventsDe = eventsDe;
    this.eventsLang = eventsLang;
    this.urlRoot = urlRoot;
    this.year = year;
    this.month = month;
    this.subscriber = subscriber;
    this.language = Language.GetByCode(lang);
  }
  
  /** Renders the entire newsletter. */
  public String render() {
    out = new StringBuilder();
    
    renderPreheader();
    
    startTable(CONTAINER_CSS);
      out.append("<tr>");
      out.append("<td align='center' valign='top'>");
      renderHeader();
      renderEvents();
      renderFooter();
      out.append("</td>");
      out.append("</tr>");
    endTable();
    
    String result = out.toString();
    out = null;
    return result;
  }
  
  /** Preheader HTML = top of page, above AOZ header. */
  private void renderPreheader() {
    startTable(null);
    out.append("<tr>");
    out.append("<td valign='top' style='padding: 0'>");
    
    startTable(PREHEADER_CSS);
    out.append("<tr>");
    
    out.append("<td valign='top'>");
    out.append("<div>MAPS Züri Agenda: Günstige Kultur- und Freizeitangebote</div>");
    out.append("</td>");
    
    out.append("<td valign='top'>");
    out.append("<div>");
    out.append("Wird dieses E-Mail nicht korrekt angezeigt?<br>");
    addLink(monthPermalink(), "Öffnen Sie es im Browser.");
    out.append("</div>");
    out.append("</td>");
    
    out.append("</tr>");
    endTable();

    out.append("</td>");
    out.append("</tr>");
    endTable();
  }
  
  /** Header HTML = Colored AOZ banner. */
  private void renderHeader() {
    String logoUrl = urlRoot + "/static/themes/transparent_header.png";
    String aozHeaderUrl = urlRoot + "/static/aoz-stadtzuerich.gif";
    
    out.append("<tr>");
    out.append("<td align='center' valign='top'>");

    startTable(null);
      out.append("<tr>");
      out.append("<td>");
      out.append("<img src='" + ESCAPE_ATTRIBUTE(aozHeaderUrl) +
          "' style='padding: 16px 16px 0' alt='AOZ'>");
      out.append("<div style='background-color: #{{background_color}};" +
          HEADER_IMG_CSS + "'>" +
          "<img src='" + ESCAPE_ATTRIBUTE(logoUrl) +
          "' style='" + HEADER_IMG_CSS + "' alt='MAPS Züri Agenda'></div>");
      out.append("</td>");
      out.append("</tr>");
    endTable();

    out.append("</td>");
    out.append("</tr>");
  }
  
  /** Event list, one row for each event. */
  private void renderEvents() {
    Map<String, Phrase> phrases = Phrases.getMergedPhrases(language.getCode());
    Phrase wasLauft = phrases.get("headNL");
    out.append("<tr>");
    out.append("<td align='center' valign='top'>");
    
    startTable(null);
      out.append("<tr>");
      out.append("<td valign='top'>");

      final String wasLauftGerman = "Was läuft in Zürich?";
      if (wasLauft == null || language.getCode().equals("de")) {
        out.append("<div style='margin-left: 25px;" + WHATS_UP_CSS + "'>" +
                   wasLauftGerman + "</div>");
      } else {
        out.append("<table style='" + EVENT_CSS + ";" + WHATS_UP_CSS +
            "'><tr>" +
            "<td style='" + EVENT_LEFT_CSS + "'>" +
            wasLauftGerman + "</td><td style='" +
            rightAlignCss(EVENT_RIGHT_CSS, language.isRightToLeft()) + "'>" +
            wasLauft.getPhrase() + "</td></tr></table>");
      }
      
      for (Event event : eventsDe.getSortedEvents()) {
        renderEvent(event);
      }
      out.append("</td>");
      out.append("</tr>");
    endTable();
    
    out.append("</td>");
    out.append("</tr>");
  }
  
  /** Renders a single event, in one or two languages. */
  private void renderEvent(Event eventDe) {
    if (eventsLang == null) {
      renderEventSingleLanguage(eventDe);
      return;
    }
    Event eventLang = eventsLang.getEvent(eventDe.getKey());
    if (eventLang == null || eventLang.getDescription() == null) {
      renderEventSingleLanguage(eventDe);
    } else {
      try {
        renderEventDoubleLanguage(eventDe, eventLang);
      } catch (IllegalStateException e) {
        // Fall-back to just german when no translation is found.
        renderEventSingleLanguage(eventDe);
      }
    }
  }
  
  /** Renders an event row just in German. */
  private void renderEventSingleLanguage(Event eventDe) {
    EventDescription desc = eventDe.getDescription();
    
    out.append("<table style='" + EVENT_CSS + "'>");
    
    out.append("<tr><td style='" + EVENT_SINGLE_CSS + "'>");
    renderEventDetails(
        DATE_FORMATTER.format(eventDe.getDate()),
        desc.getTitle(), false, // All false, german isn't RTL.
        desc.getDesc(), false,
        eventDe.getLocation(),
        eventDe.getTransit(),
        eventDe.getUrl());
    out.append("</td></tr>");
    
    out.append("</table>");
  }
  
  /** Renders an event row, in German plus the desired language. */ 
  private void renderEventDoubleLanguage(Event eventDe, Event eventLang) {
    EventDescription descDe = eventDe.getDescription();
    EventDescription descLang = eventLang.getDescription();
    String date = DATE_FORMATTER.format(eventDe.getDate());

    out.append("<tr style='" + EVENT_CSS + "'>");
    
    // Left column is always German translation.
    out.append("<td style='" + EVENT_LEFT_CSS + "'>");
    renderEventDetails(
        date,
        descDe.getTitle(), false, // All false, german isn't RTL.
        descDe.getDesc(), false,
        eventDe.getLocation(),
        eventDe.getTransit(),
        eventDe.getUrl());
    out.append("</td>");
    out.append("<td style='" +
        rightAlignCss(EVENT_RIGHT_CSS, language.isRightToLeft()) + "'>");
    renderEventDetails(
        date,
        descLang.getTitle(), language.isRightToLeft(),
        descLang.getDesc(), language.isRightToLeft(),
        eventDe.getLocation(),
        eventDe.getTransit(),
        eventDe.getUrl());
    out.append("</td>");
    
    out.append("</tr>");
  }
  
  /** Guesses a Google Maps link for the given location. 
   *  Escapes all plain text portions that are returned. */
  private String renderGoogleMapsLink(String location) {
    return String.format(
	"<a title='%s' href='https://www.google.ch/maps/search/%s,Zurich'>" +
        "%s</a>",
	ESCAPE_TEXT(location), ESCAPE_TEXT(location), ESCAPE_TEXT(location));
  }

  /** Renders just the details for one event in one language. */
  private void renderEventDetails(String date,
      String title, boolean isTitleRtl, 
      String desc, boolean isDescRtl, 
      String location, String transit, String url) {
    out.append(String.format("<h1 style='%s'>%s</h1>",
        DATE_CSS, ESCAPE_TEXT(date)));
    out.append(String.format("<h2 style='%s'>%s</h2>",
        rtlCss(TITLE_CSS, isTitleRtl), ESCAPE_TEXT(title)));
    out.append(String.format("<div style='%s'>%s</div>",
        rtlCss(DESC_CSS, isDescRtl), ESCAPE_TEXT(desc)));
    out.append(String.format("<p style='%s'>%s</p>",
        rtlCss(LOCATION_CSS, false), renderGoogleMapsLink(location)));
    out.append(String.format("<p style='%s'>%s</p>",
	rtlCss(TRANSIT_CSS, false), ESCAPE_TEXT(transit)));
    out.append(String.format("<p style='%s'>", 
        rtlCss(URL_CSS, false)));
    addLink(url, url);
    out.append("</p>");
  }
  
  /** Foother HTML = Copy text and links to other pages. */
  private void renderFooter() {
    out.append("<tr>");
    out.append("<td valign='top' style='padding: 0'>");
    
    startTable(FOOTER_CSS);
      out.append("<tr>");
      out.append("<td>");
      out.append("<span style='" + DISCLAIMER_CSS + "'>");
      out.append(DISCLAIMER_TEXT);
      out.append("</span>");
      out.append("</td>");
      out.append("</tr>");
  
      if (this.isEmail()) {
        out.append("<tr>");
        out.append("<td colspan='2' valign='middle' id='utility'>");
        out.append("<div style='text-align:center'>");
        // addLink(shareLink(), "Weiterleiten");
        //out.append(" | ");
        addLink(unsubscribeLink(), "MAPS-Newsletter abbestellen");
        out.append(" | ");
        addLink(changeLanguageLink(), "Einstellungen");
        out.append("</div>");
        out.append("</td>");
        out.append("</tr>");
      }
    endTable();

    out.append("</td>");
    out.append("</tr>");
  }
  
  // Generators to build more complex properties based on the injected values.
  
  /** @return Whether this is generating for email or web. */
  private boolean isEmail() {
    // When viewed via email, we know the receiver (= subscriber).
    // When viewed online, no subscriber is known, and the language is supplied.
    return subscriber != null;
  }

  /** @return URL the user should visit to unsubscribe from the newsletter. */ 
  private String unsubscribeLink() {
    return String.format("%s/unsubscribe.jsp?hash=%s",
        urlRoot, subscriber.getHash());
  }

  /** @return URL the user should visit to change their settings. */
  private String changeLanguageLink() {
    return String.format("%s/change_subscriber.jsp?hash=%s",
        urlRoot, subscriber.getHash());
  }

  /** @return URL to visit the web version of this rendering. */
  private String monthPermalink() {
    return String.format("%s/maps/#/%s/events?date=%04d-%02d-01",
        urlRoot, language.getCode(), year, month+1);
  }
  
  // HTML writing utilities
  
  private void addLink(@Nullable String url, String text) {
    if (url != null) {
      url = MAKE_ABSOLUTE_LINK(url);
      out.append("<a href='" + ESCAPE_ATTRIBUTE(url) + "' target='_blank'>");
      out.append(ESCAPE_TEXT(text));
      out.append("</a>");
    }
  }
  
  private void startTable(@Nullable String style) {
    // Each startTable should be paired with an endTable.
    out.append("<table border='0' cellpadding='0' cellspacing='0' width='600'");
    if (style != null) {
      out.append(" style='" + style + "'");
    }
    out.append(">");
  }
  private void endTable() {
    out.append("</table>");
  }
  
  private static String rtlCss(String CSS, boolean isRtl) {
    return isRtl ? RTL_CSS + CSS : CSS;
  }
  
  private static String rightAlignCss(String CSS, boolean isRightAlign) {
      return isRightAlign ? RIGHT_ALIGN_CSS + CSS : CSS;
  }
}
