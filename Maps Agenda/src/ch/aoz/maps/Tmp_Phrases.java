package ch.aoz.maps;

import com.google.appengine.api.datastore.EntityNotFoundException;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.*;

@SuppressWarnings("serial")
public class Tmp_Phrases extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
      resp.setContentType("text/plain");
      
      Set<String> languages = Language.getAllLanguagesAsMap().keySet();
      Map<String, Calendar> dates = new TreeMap<String, Calendar>();
      Map<String, String> langs = new TreeMap<String, String>();
      Map<String, Set<Event>> events = new TreeMap<String, Set<Event>>();
      Map<String, Set<Event>> descriptions = new TreeMap<String, Set<Event>>();
      for (Event e : Event.GetAllEvents()) {
        String key = getKey(e.getCalendar());
        if (!events.containsKey(key)) {
          events.put(key, new HashSet<Event>());
          dates.put(key, (Calendar)e.getCalendar().clone());
        }
        for (String lang : languages) {
          try {
            Translation t = Translation.getTranslationForEvent(e, lang);
            EventDescription d = new EventDescription(lang, t.getTitle(), t.getDesc());
            Event e2 = e.clone();
            e2.setDescription(d);
            String dKey = getKey(lang, e.getCalendar());
            if (!descriptions.containsKey(dKey)) {
              descriptions.put(dKey, new HashSet<Event>());
              langs.put(dKey, lang);
            }
            if (!descriptions.get(dKey).add(e2)) {
              resp.getWriter().println("Failed to add description: " + dKey + "-" + e.getCalendar().get(Calendar.DATE) + " " + e.getGermanTranslation().getTitle());
            }
          } catch (EntityNotFoundException ex) {}          
        }
        if (!events.get(key).add(e)) {
          resp.getWriter().println("Failed to add event: " + key + "-" + e.getCalendar().get(Calendar.DATE) + " " + e.getGermanTranslation().getTitle());
        }
      }
      
      resp.getWriter().println("");      
      resp.getWriter().println("Stored events:");      
      for (String key : events.keySet()) {
        Events e = new Events(dates.get(key), events.get(key));
        String prefix = "- " + key + "-" + e.getCalendar().get(Calendar.DATE) + ": ";
        if (!e.isOk())
          resp.getWriter().println(prefix + "not ok - " + e.debug());
        else if (!e.addToStore())
          resp.getWriter().println(prefix + "failed to add - " + e.debug());
        else
          resp.getWriter().println(prefix + "done");
      }

      resp.getWriter().println("");      
      resp.getWriter().println("Stored descriptions:");
      for (String dKey : descriptions.keySet()) {
        EventDescriptions ds = new EventDescriptions(langs.get(dKey), dates.get(dKey.subSequence(0,  7)), descriptions.get(dKey));
        StringBuilder msg = new StringBuilder("- " + dKey + ": ");
        if (!ds.addToStore())
          msg.append("failed to add");
        else
          msg.append("done");
        msg.append(" (debug: ");
        msg.append(ds.debug());
        msg.append(")");
        resp.getWriter().println(msg.toString());
      }
    }
   
    public void answer(HttpServletResponse resp, String lang)
            throws IOException {
      Phrases p = Phrases.GetPhrasesForLanguage(lang);
      
      StringBuilder response = new StringBuilder();
      response.append(lang);
      response.append(": ");
      response.append(p.addToStore());     
      resp.getWriter().println(response.toString());      
    }

    private static String getKey(Calendar c) {
      return String.format("%04d-%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH));
    }
    private static String getKey(String lang, Calendar month) {
      return String.format("%04d-%02d-%s", month.get(Calendar.YEAR), 
                                           month.get(Calendar.MONTH), 
                                           lang);
    }
}
