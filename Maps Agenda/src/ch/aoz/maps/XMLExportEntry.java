package ch.aoz.maps;

import java.util.Calendar;

public class XMLExportEntry {
	private Event event;
	private Language language;
	private EventDescription description;
	private boolean dateChanged;
	private boolean enlarge;
	private boolean topicOfMonth;

	/**
	 * Creates a new XMLExportEntry using the given configuration.
	 * 
	 * @param event
	 *            The event to render.
	 * @param language
	 *            The language to use.
	 * @param description
	 *            The description of event in language.
	 * @param dateChanged
	 *            A flag that specifies if the date has changed and the full
	 *            date must be printed.
	 * @param enlarge
	 *            A flag that specifies if the entry should be printed larger
	 *            than the others.
	 * @param topicOfMonth
	 *            A flag that specifies if this is the topic of the month.
	 */
	public XMLExportEntry(Event event, Language language,
	        EventDescription description, boolean dateChanged, boolean enlarge,
			boolean topicOfMonth) {
		this.event = event;
		this.language = language;
		this.description = description;
		this.dateChanged = dateChanged;
		this.enlarge = enlarge;
		this.topicOfMonth = topicOfMonth;
	}

	/**
	 * @return The XML description of the event in the chosen language.
	 */
	public String getXML() {
		String xml = new String();

		if (!enlarge) {
			xml += "<Table_inside xmlns:aid5=\"http://ns.adobe.com/AdobeInDesign/5.0/\" aid5:tablestyle=\"ts_inside\" xmlns:aid=\"http://ns.adobe.com/AdobeInDesign/4.0/\" aid:table=\"table\" aid:trows=\"1\" aid:tcols=\"3\">";
			if (!language.isRightToLeft()) {
				xml += getXMLDayOfWeek();
				xml += getXMLDate();
				xml += getXMLSmallContents(358);
			} else {
				xml += getXMLSmallContents(374);
				xml += getXMLDate();
				xml += getXMLDayOfWeek();
			}
		} else {
			xml += "<Table_inside xmlns:aid5=\"http://ns.adobe.com/AdobeInDesign/5.0/\" aid5:tablestyle=\"ts_inside\" xmlns:aid=\"http://ns.adobe.com/AdobeInDesign/4.0/\" aid:table=\"table\" aid:trows=\"2\" aid:tcols=\"3\">";
			if (!language.isRightToLeft()) {
				xml += getXMLDayOfWeek();
				xml += getXMLDate();
				xml += getXMLTitle(303);
			} else {
				xml += getXMLTitle(374);
				xml += getXMLDate();
				xml += getXMLDayOfWeek();
			}
			xml += "<Tag_inside aid:table=\"cell\" aid:crows=\"1\" aid:ccols=\"3\" aid5:cellstyle=\"cs_desc_gross\">";
			xml += "<Inhalttag aid:pstyle=\"inhalt_gross"
					+ language.getXMLFormatSupplement() + "\">";
			xml += escapeXML(description.getDesc());
			xml += "</Inhalttag>\n";
			xml += getXMLLocation();
			xml += "</Tag_inside>";
		}

		xml += "</Table_inside>";
		return xml;
	}

	private String getXMLSmallContents(int width) {
		String xml = new String();
		xml += "<Tag_inside aid:table=\"cell\" aid:crows=\"1\" aid:ccols=\"1\" aid:ccolwidth=\"" + width + "\" aid5:cellstyle=\"cs_desc\">";
		xml += "<title aid:pstyle=\"titel" + language.getXMLFormatSupplement()
				+ "\">";
		xml += escapeXML(description.getTitle());
		xml += "</title>\n";
		xml += "<Inhalttag aid:pstyle=\"inhalt"
				+ language.getXMLFormatSupplement() + "\">";
		xml += escapeXML(description.getDesc());
		xml += "</Inhalttag>\n";
		xml += getXMLLocation();
		xml += "</Tag_inside>";
		return xml;
	}

	private String getXMLDayOfWeek() {
		String xml = new String();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(event.getDate());
		xml += "<Tag_inside aid:table=\"cell\" aid:crows=\"1\" aid:ccols=\"1\" aid:ccolwidth=\""
				+ (language.isRightToLeft() ? "45" : "52")
				+ "\" aid5:cellstyle=\""
				+ (enlarge ? "cs_gross" : "cs_datum")
				+ "\" aid:pstyle=\"wochentag"
				+ language.getXMLFormatSupplement() + "\">";
		xml += "<Wochentag>";
		if (topicOfMonth) {
			xml += language
					.getDayOfTheWeek(calendar.get(Calendar.DAY_OF_WEEK) - 1);
		}
		xml += "</Wochentag>";
		xml += "</Tag_inside>";
		return xml;
	}

	private String getXMLDate() {
		String xml = new String();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(event.getDate());
		xml += "<Tag_inside aid:table=\"cell\" aid:crows=\"1\" aid:ccols=\"1\" aid:ccolwidth=\"40\" aid5:cellstyle=\""
				+ (enlarge ? "cs_gross" : "cs_datum")
				+ "\" aid:pstyle=\"datum\">";
		if (!enlarge || dateChanged) {
			xml += " " + String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH))
					+ "." + String.format("%02d", calendar.get(Calendar.MONTH) + 1)
					+ ". ";
		}
		xml += "</Tag_inside>";
		return xml;
	}

	private String getXMLTitle(int width) {
		String xml = new String();
		xml += "<Tag_inside aid:table=\"cell\" aid:crows=\"1\" aid:ccols=\"1\" aid:ccolwidth=\""
				+ Integer.toString(width)
				+ "\" aid5:cellstyle=\"cs_titel_gross\">";
		xml += "<title aid:pstyle=\"titel" + language.getXMLFormatSupplement()
				+ "\">";
		xml += escapeXML(description.getTitle());
		xml += "</title>";
		xml += "</Tag_inside>";
		return xml;
	}

	private String getXMLLocation() {
		String xml = new String();
		if (event.getLocation() != "") {
			if (enlarge) {
				if (language.isRightToLeft()) {
					xml += "<Orttag aid:pstyle=\"ort_gross_rtl";
				} else {
					xml += "<Orttag aid:pstyle=\"ort_gross";
				}
			} else {
				if (language.isRightToLeft()) {
					xml += "<Orttag aid:pstyle=\"ort_rtl";
				} else {
					xml += "<Orttag aid:pstyle=\"ort";
				}
			}
			// TODO remove special hack for _ru.
			xml += (language.getXMLFormatSupplement() == "_ru" ? language
					.getXMLFormatSupplement() : "") + "\">";
			xml += escapeXML(event.getLocation().trim()) + " ";
			if (event.getTransit() != null && event.getTransit() != "") {
			    xml += escapeXML(event.getTransit().trim()) + " ";
			}

			if (event.getUrl() != null && event.getUrl() != "") {
				xml += event.getUrl().replace("http://www", "www").trim();
			}
			xml += "</Orttag>";
		}
		return xml;
	}
	
    /**
     * Replaces all characters that Indesign cannot interpret by their proper
     * XML representations.
     * 
     * @param content
     *            is the content to sanitize
     * @return the sanitized content
     */
    public static final String escapeXML(String content) {
	if (content != null) {
	    // Because Indesign may interpret things on its own way, we do not
	    // replace anything else than the things that are known to cause
	    // problems. Please supplement this list with other failing
	    // characters.
	    content = content.replace("&", "&amp;");
	    content = content.replace("<", "&lt;");
	    content = content.replace(">", "&gt;");
	    content = content.replace("\"", "&quot;");
	}
	return content;
    }    
}
