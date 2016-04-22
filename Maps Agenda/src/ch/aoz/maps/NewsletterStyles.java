package ch.aoz.maps;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * CSS styles for use in the Newsletter. Due to limitations on Email formatting,
 * all CSS must be inline rather than through class='' attributes.
 * See:  http://www.campaignmonitor.com/css/
 */
public class NewsletterStyles {
  // Converting event Dates into visual date strings:
  public static final DateFormat DATE_FORMATTER =
      new SimpleDateFormat("d.M.yyyy");
  
  // Escape event data to be safe in HTML:
  public static final String ESCAPE_ATTRIBUTE(String attr) {
    return ESCAPE_TEXT(attr)
        .replaceAll("\"", "&quot;")
        .replaceAll("\'", "&#39;");
  }
  
  public static final String ESCAPE_TEXT(String text) {
    return text
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;");
  }
  
  public static final String MAKE_ABSOLUTE_LINK(String url) {
    // Assume that links must be 'http' or 'https'. Anything else, prepend with
    // http:// to make sure it doesn't link relative to appspot.com.
    if (url.startsWith("http://") || url.startsWith("https://")) {
      return url;
    }
    return "http://" + url;
  }
  
  // CSS for styling the HTML:
  
  public static final String PREHEADER_CSS = join(
      "background-color:#FAFAFA",
      "border:1px solid #FAFAFA",
      "color:#505050",
      "font-family:Arial",
      "font-size:10px",
      "line-height:100%",
      "padding:6px",
      "text-align:left");

  // Styles the section which contains the header, events, and footer.
  public static final String CONTAINER_CSS = join(
      "border:1px solid #DDDDDD",
      "font-family:Arial");
  
  public static final String HEADER_IMG_CSS = join(
      "margin-bottom: -5px");
  
  public static final String WHATS_UP_CSS = join(
      "font-style:italic",
      "font-weight:bold",
      "font-size:18px",
      "padding: 15px 20px 0px 20px");
  
  public static final String EVENT_CSS = join(
      "margin:10px 0 15px 0");
  
  public static final String EVENT_LEFT_CSS = join(
      "padding: 0 20px 5px 20px",
      "width: 250px",
      "float: left");
  
  // Same CSS for left and right columns for now.
  public static final String EVENT_RIGHT_CSS = EVENT_LEFT_CSS;
  
  public static final String EVENT_SINGLE_CSS = join(
      "padding: 0 20px 5px 20px",
      "width: 540px");
  
  public static final String FOOTER_CSS = join(
      "background-color:#FAFAFA",
      "border-top:0",
      "color:#707070",
      "font-size:12px",
      "line-height:125%",
      "margin-top:8px",
      "padding:8px",
      "text-align:left");
  
  public static final String DISCLAIMER_CSS = join(
      "font-size:10px");
  
  public static final String RIGHT_ALIGN_CSS = "text-align: right;";
  
  public static final String RTL_CSS = join(
      "direction: rtl",
      "text-align: right");

  // Rules for styling the sections of each single event 
  public static final String DATE_CSS = join(
      "border-top: 1px solid #BBB",
      "color:#202020",
      "font-size:20px",
      "line-height:100%",
      "padding-top:9px",
      "margin:15px 0 10px");
  public static final String TITLE_CSS = join(
      "color:#202020",
      "display:block",
      "font-size:14px",
      "line-height:120%",
      "margin: 0",
      "padding: 0",
      "text-transform: uppercase");
  public static final String DESC_CSS = join(
      "color:#505050",
      "display:block",
      "font-size:13px",
      "margin:5px 0 0 0");
  public static final String LOCATION_CSS = join(
      "font-style:italic",
      "margin:7px 0 0 0",
      "padding:0",
      "font-size:12px");
  public static final String TRANSIT_CSS = join(
      "font-style:italic",
      "margin:7px 0 0 0",
      "padding:0",
      "font-size:12px");
  public static final String URL_CSS = join(
      "margin: 7px 0 0 0",
      "padding: 0",
      "font-size:12px");

  // Build multiple CSS rules into a style attribute.
  private static final String join(String... rules) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < rules.length; i++) {
      result.append(rules[i]);
      result.append(";");
    }
    return result.toString();
  }
}
