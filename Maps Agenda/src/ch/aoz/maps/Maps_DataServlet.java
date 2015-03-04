package ch.aoz.maps;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.*;

@SuppressWarnings("serial")
public class Maps_DataServlet extends HttpServlet {
  public static final int MAX_EVENTS = 15;
  public static final int MAX_DAYS = 15;

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String response = null;
    switch (req.getParameter("type")) {
    case "languages":
      response = getLanguages();
      break;
    case "events":
      response = getEvents(req);
      break;
    case "phrases":
      response = getPhrases(req);
      break;
    case "tags":
      response = getTags();
      break;
    case "subscribers":
      response = getSubscribers();
      break;
    case "translators":
      response = getTranslators();
      break;
    }
    if (response == null) {
      response = "";
    }
    resp.setContentType("application/json");
    resp.getWriter().println(response);
  }

  private String getTags() {
    List<String> tags = Phrase.GetKeysForTags();
    StringBuilder response = new StringBuilder();
    response.append("{ \"tags\": [");
    for (String tag : tags) {
      response.append("\"" + tag + "\",");
    }
    if (!tags.isEmpty()) {
      // Remove the last comma.
      response.deleteCharAt(response.length() - 1);
    }
    response.append("]}");
    return response.toString();
  }

  private String getPhrases(HttpServletRequest req) {
    String lang = req.getParameter("lang");
    Map<String, Phrase> phrases = Phrases.getMergedPhrases(lang == null ? "de" : lang);
    StringBuilder response = new StringBuilder();
    response.append("{ \"phrases\": {");
    for (String key : phrases.keySet()) {
      Phrase p = phrases.get(key);
      response.append("\"" + key + "\":");
      response.append("\"" + Utils.toUnicode(p.getPhrase()) + "\",");
    }
    if (!phrases.isEmpty()) {
      // Remove the last comma.
      response.deleteCharAt(response.length() - 1);
    }
    response.append("}}");
    return response.toString();
  }

  private String getEvents(HttpServletRequest req) {
    Language lang = Language.GetByCode(req.getParameter("lang"));
    if (lang == null) {
      lang = Language.GetByCode(req.getParameter("de"));
    }
    
    boolean forward = (req.getParameter("back") == null);
    Calendar start_date = Calendar.getInstance();
    String requested_date = req.getParameter("date");
    if (requested_date != null) {
      try {
        start_date.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(requested_date));
      } catch (Exception e) {
      }
    }
    // Set the time at midnight, so that the below query stays the same.
    start_date.set(Calendar.MILLISECOND, 0);
    start_date.set(Calendar.SECOND, 0);
    start_date.set(Calendar.MINUTE, 0);
    start_date.set(Calendar.HOUR_OF_DAY, 0);
    // Exclusive.
    Calendar end_date = (Calendar)start_date.clone();
    if (forward) {
      end_date.add(Calendar.DATE, MAX_DAYS);      
    } else {
      start_date.add(Calendar.DATE, 1 - MAX_DAYS);
      end_date.add(Calendar.DATE, 1);      
    }

    ArrayDeque<Event> eventList = new ArrayDeque<>();
    // Load the events of the month of start_date.
    Events events = Events.getEvents(start_date, lang.getCode());
    for (Event e : events.getSortedEvents()) {
      if (e.getCalendar().compareTo(start_date) >= 0 && e.getCalendar().compareTo(end_date) < 0) {
        eventList.addLast(e);
      }
    }
    // If necessary, load the events of the next month.
    if (start_date.get(Calendar.MONTH) != end_date.get(Calendar.MONTH) && 
            (!forward || eventList.size() < MAX_EVENTS)) {
      events = Events.getEvents(end_date, lang.getCode());
      for (Event e : events.getSortedEvents()) {
        if (e.getCalendar().compareTo(start_date) >= 0 && e.getCalendar().compareTo(end_date) < 0) {
          eventList.addLast(e);
        }
      }
    }
    while (eventList.size() > MAX_EVENTS) {
      if (forward) {
        eventList.removeLast();
      } else {
        eventList.removeFirst();
      }
    }

    StringBuilder response = new StringBuilder();
    response.append("{ \"events\": [");
    for (Event e : eventList) {
      EventDescription d = e.getDescription();
      if (d != null) {
        response.append("{");
        response.append("\"date\":\"").append(dateToString(e.getDate())).append("\",");
        response.append("\"title\":\"").append(Utils.toUnicode(d.getTitle())).append("\",");
        response.append("\"description\":\"").append(Utils.toUnicode(d.getDesc())).append("\",");
        response.append("\"location\":\"").append(Utils.toUnicode(e.getLocation())).append("\",");
        response.append("\"transit\":\"").append(Utils.toUnicode(e.getTransit())).append("\",");
        response.append("\"url\":\"").append(Utils.toUnicode(e.getUrl())).append("\"");
        response.append("},");
      }
    }
    if (response.charAt(response.length() - 1) == ',') {
      response.deleteCharAt(response.length() - 1); // remove the last ,
    }

    response.append("]}");
    return response.toString();
  }

  public String dateToString(Date d) {
    Calendar c = Calendar.getInstance();
    c.setTime(d);
    return new StringBuilder().append(c.get(Calendar.MONTH) + 1).append('/')
            .append(c.get(Calendar.DAY_OF_MONTH)).append('/').append(c.get(Calendar.YEAR))
            .toString();
  }

  public String getLanguages() {
    StringBuilder response = new StringBuilder();
    response.append("{ \"languages\": [");

    Set<Language> langs = Language.getAllLanguages();
    for (Language l : langs) {
      response.append("{\"code\":\"" + l.getCode() + "\",");
      response.append("\"germanName\":\"").append(Utils.toUnicode(l.getGermanName())).append("\",");
      response.append("\"name\":\"").append(Utils.toUnicode(l.getName())).append("\",");
      response.append("\"days\":[");
      for (String day : l.getDaysOfTheWeek()) {
        response.append("\"").append(Utils.toUnicode(day)).append("\",");
      }
      if (response.charAt(response.length() - 1) == ',') {
        response.deleteCharAt(response.length() - 1); // remove the last ,
      }
      response.append("],");
      response.append("\"isRtl\":").append(l.isRightToLeft()).append(",");
      response.append("\"inAgenda\":").append(l.isInAgenda()).append(",");
      response.append("\"specificFormat\":").append(l.hasSpecificFormat()).append("");
      response.append("},");
    }
    if (response.charAt(response.length() - 1) == ',') {
      response.deleteCharAt(response.length() - 1); // remove the last ,
    }

    response.append("]}");
    return response.toString();
  }

  public String getTranslators() {
    StringBuilder response = new StringBuilder();
    response.append("{ \"translators\": [");

    Map<String, Translator> translators = Translator.getAllTranslators();
    for (String email : translators.keySet()) {
      Translator t = translators.get(email);
      response.append("{");
      response.append("\"email\":\"").append(Utils.toUnicode(t.getEmail())).append("\",");
      response.append("\"name\":\"").append(Utils.toUnicode(t.getName())).append("\",");
      response.append("\"langs\":[");
      for (String l : t.getLanguages()) {
        response.append("\"").append(Utils.toUnicode(l)).append("\",");
      }
      if (response.charAt(response.length() - 1) == ',') {
        response.deleteCharAt(response.length() - 1); // remove the last ,
      }
      response.append("]},");
    }
    if (response.charAt(response.length() - 1) == ',') {
      response.deleteCharAt(response.length() - 1); // remove the last ,
    }
    response.append("]}");
    return response.toString();
  }

  public String getSubscribers() {
    StringBuilder response = new StringBuilder();
    response.append("{ \"subscribers\": [");

    Map<String, Subscriber> subscribers = Subscriber.getAllSubscribers();
    for (String email : subscribers.keySet()) {
      Subscriber s = subscribers.get(email);
      response.append("{");
      response.append("\"email\":\"").append(Utils.toUnicode(s.getEmail())).append("\",");
      response.append("\"name\":\"").append(Utils.toUnicode(s.getName())).append("\",");
      response.append("\"lang\":\"").append(Utils.toUnicode(s.getLanguage())).append("\",");
      response.append("\"hash\":\"").append(Utils.toUnicode(s.getHash())).append("\"},");
    }
    if (response.charAt(response.length() - 1) == ',') {
      response.deleteCharAt(response.length() - 1); // remove the last ,
    }
    response.append("]}");
    return response.toString();
  }
}
