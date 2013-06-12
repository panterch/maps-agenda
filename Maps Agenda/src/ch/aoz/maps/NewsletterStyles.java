package ch.aoz.maps;

/**
 * CSS styles for use in the Newsletter. Due to limitations on Email formatting,
 * all CSS must be inline rather than through class='' attributes.
 * See:  http://www.campaignmonitor.com/css/
 */
public class NewsletterStyles {
  
  public static final String PREHEADER_CSS = join(
      "background-color:#FAFAFA",
      "border:1px solid #FAFAFA",
      "color:#505050",
      "font-family:Arial",
      "font-size:10px",
      "line-height:100%",
      "padding:6px",
      "text-align:left");

  // Styles the section which contains the header, events, and zfooter.
  public static final String CONTAINER_CSS = join(
      "border:1px solid #DDDDDD",
      "font-family:Arial");
  
  public static final String EVENT_CSS = join(
      "display:block",
      "margin:10px 0 15px 0",
      "clear:left");
  
  public static final String EVENT_LEFT_CSS = join(
      "padding: 0 20px 5px 20px",
      "width: 250px;",
      "float: left");
  
  // Same CSS for left and right columns for now.
  public static final String EVENT_RIGHT_CSS = EVENT_LEFT_CSS;
  
  public static final String FOOTER_CSS = join(
      "background-color:#FAFAFA",
      "border-top:0",
      "color:#707070",
      "font-size:12px",
      "line-height:125%",
      "text-align:left");

  //
  // Rules for styling the sections of each single event
  //
  
  public static final String DATE_CSS = join(
      "border-top: 1px solid #BBB",
      "color:#202020",
      "font-size:20px",
      "line-height:100%",
      "padding-top:9px",
      "margin:15px 0 10px");
  public static final String TITLE_CSS = join(
      "color:#202020",
      "display:inline",
      "font-size:14px",
      "line-height:120%",
      "margin: 0",
      "padding: 0",
      "text-transform: uppercase");
  public static final String DESC_CSS = join(
      "color:#505050",
      "font-size:13px",
      "margin:5px 0 0 0");
  public static final String LOCATION_CSS = join(
      "font-style:italic",
      "margin:7px 0 0 25px",
      "padding:0",
      "font-size:12px");
  public static final String URL_CSS = join(
      "margin: 7px 0 0 25px",
      "padding: 0",
      "font-size:12px");

  
  private static final String join(String... rules) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < rules.length; i++) {
      if (i != 0) result.append(';');
      result.append(rules[i]);
    }
    return result.toString();
  }
}
