package ch.aoz.maps;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.appengine.api.datastore.EntityNotFoundException;

public class XMLExport {
	private List<Event> events;
	
	/**
	 * Creates a new XML export of a given list of events.
	 * 
	 * @param events
	 */
	public XMLExport(List<Event> events) {
		this.events = events;
	}
	
	/**
	 * Render this Event into an XML tag.
	 * 
	 * @return the generated XML tag.
	 */
	public String getXML() {
		// Header.
		String xml = new String("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		xml += "<Root>\n";
		xml += "  <Tag1>\n";

		Map<String, Language> languages = Language.getAllLanguages();
		Map<Event, String> image_text = new HashMap<Event, String>();
		List<Event> topicOfMonth = new ArrayList<Event>(); // TODO pass in, or retrieve.

		for (Language language : languages.values()) {
			xml += "    <" + language.getCode() + ">\n";
			xml += "      <inh>\n";

			for (Event event : topicOfMonth) {
				Translation translation;
				try {
					translation = getTranslation(event, language);
				} catch (EntityNotFoundException e) {
					continue;
				}
				xml += new XMLExportEntry(event, language, translation, true, true, false).getXML();
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
				boolean dateChanged = newDay.get(Calendar.DAY_OF_MONTH) != currentDay
						.get(Calendar.DAY_OF_MONTH)
						|| newDay.get(Calendar.MONTH) != currentDay
								.get(Calendar.MONTH)
						|| newDay.get(Calendar.YEAR) != currentDay
								.get(Calendar.YEAR);

				boolean enlarge = true; // TODO Pass in, or retrieve.
						
				// Add the entry for this event.
				xml += new XMLExportEntry(event, language, translation, dateChanged, enlarge, true).getXML();
				
				// Log the new date.
				currentDay.setTime(newDay.getTime());

				// Add image title if desired.
				if (true) { // TODO Pass in, or retrieve.
					String image_title = new String(
							"            <b_titel aid:cstyle=\"bildtitel"
									+ language.getXMLFormatSupplement() + "\">"
									+ translation.getDesc() + "</b_titel>\n");
					if (image_text.containsKey(event)) {
						image_text
								.put(event,
										image_text.get(event)
												+ "            <space aid:cstyle=\"space\" > </space>"
												+ image_title);
					} else {
						image_text.put(event, image_title);
					}
				}
			}
			xml += "      </inh>\n";
			xml += "    </" + language.getCode() + ">\n";
		}
		xml += "  </Tag1>\n";

		if (image_text.size() > 0) {
			xml += "  <bildtexte>\n";
			for (Entry<Event, String> image : image_text.entrySet()) {
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(image.getKey().getDate());
				String day = String.format("%02d",
						calendar.get(Calendar.DAY_OF_MONTH));
				String month = String.format("%02d",
						calendar.get(Calendar.MONTH));
				xml += "    <bild_" + day + "_" + month + ">\n";
				xml += getXMLImageTag(day, month, image.getValue());
				xml += "    </bild_" + day + "_" + month + ">\n";
			}

			xml += "    <individuell>\n";
			xml += getXMLImageTag("03", "12", "Text text text...");
			xml += "    </individuell>\n";
			xml += "    </bildtexte>\n";
		}

		xml += "</Root>\n";

		return xml;
	}

	/**
	 * Retrieve the translation of an event in a particular language.
	 * 
	 * @param event
	 *            The event for which the translation is requested.
	 * @param language
	 *            The language which the translation should be in.
	 * @return The translation of event in language.
	 * @throws EntityNotFoundException
	 */
	private static Translation getTranslation(Event event, Language language)
			throws EntityNotFoundException {
		Translation translation;
		// TODO assign the translation of this event to the given
		// language.
		translation = Translation.getGermanTranslationForEvent(event);
		return translation;
	}
	
	/**
	 * Render an XML image tag tree into a string.
	 * 
	 * @param day
	 *            A two-digit string that describes the day of the month.
	 * @param month
	 *            A two-digit string that describes the month in the year.
	 * @param bTitelTag
	 *            The b_titel tag that goes into the innermost level.
	 * @return A string with the requested tag.
	 */
	private static String getXMLImageTag(String day, String month,
			String bTitelTag) {
		String xml = new String();
		xml += "      <Table_main xmlns:aid5=\"http://ns.adobe.com/AdobeInDesign/5.0/\" aid5:tablestyle=\"ts_main\" xmlns:aid=\"http://ns.adobe.com/AdobeInDesign/4.0/\" aid:table=\"table\" aid:trows=\"1\" aid:tcols=\"1\">\n";
		xml += "        <Tag_main aid:table=\"cell\" aid:crows=\"1\" aid:ccols=\"1\" aid:ccolwidth=\"450\" aid5:cellstyle=\"cs_bildtitel\">\n";
		xml += "          <bilddatum aid:pstyle=\"bilddatum\"> " + day + "."
				+ month + ". </bilddatum>\n";
		xml += "          <p_titel  aid:pstyle=\"bildtitel\">\n";
		xml += bTitelTag;
		xml += "          </p_titel>\n";
		xml += "        </Tag_main>\n";
		xml += "      </Table_main>\n";
		return xml;
	}
}
