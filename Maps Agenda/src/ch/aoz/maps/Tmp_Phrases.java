package ch.aoz.maps;

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
      
      Map<String, Calendar> dates = new TreeMap<String, Calendar>();
      Map<String, Set<Event>> events = new TreeMap<String, Set<Event>>();
      for (Event e : Event.GetAllEvents()) {
        String key = getKey(e.getCalendar());
        if (!events.containsKey(key)) {
          events.put(key, new HashSet<Event>());
          dates.put(key, (Calendar)e.getCalendar().clone());
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
}
