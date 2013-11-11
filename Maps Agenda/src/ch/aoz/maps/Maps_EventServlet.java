package ch.aoz.maps;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.*;

import ch.aoz.maps.Event;
import ch.aoz.maps.Language;

@SuppressWarnings("serial")
public class Maps_EventServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
      String lang = req.getParameter("lang");
      if (lang == null) {
        lang = "de";
      }
      
      Calendar monday = Calendar.getInstance();
      String requested_date = req.getParameter("startDate");
      if (requested_date != null) {
        try {
          monday.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(requested_date));
        } catch (Exception e) {
        }
      }
      if (monday.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
        monday.setFirstDayOfWeek(Calendar.MONDAY);
        monday.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);  
      }
      int year = monday.get(Calendar.YEAR);
      int month = monday.get(Calendar.MONTH);
      Language l = Language.GetByCode(lang);
      List<Event> events = Event.GetEventListForMonth(year, month);
      
      StringBuilder response = new StringBuilder();
      response.append("[");
      for (Event e : events) {
        Translation t = e.getTranslation(l);
        if (t != null) {
          response.append("{");
          response.append("\"date\":\"").append(dateToString(e.getDate())).append("\",");
          response.append("\"title\":\"").append(toUnicode(t.getTitle())).append("\",");
          // TODO: We cannot replace newlines by anything reasonable as it seems. The template mechanism ignores br, p, or \\n tags...
          response.append("\"description\":\"").append(toUnicode(t.getDesc().replace("\n", "").replace("\r", ""))).append("\",");
          response.append("\"url\":\"").append(toUnicode(t.getUrl())).append("\"");
          response.append("},");
        }
      }
      if (response.charAt(response.length() - 1) == ',') {
        response.deleteCharAt(response.length() - 1);  // remove the last ,
      }
      response.append("]");
      
      resp.setContentType("application/json");
      resp.getWriter().println(response.toString());
    }

    public String dateToString(Date d) {      
      Calendar c = Calendar.getInstance();
      c.setTime(d);
      return new StringBuilder()
          .append(c.get(Calendar.MONTH) + 1)
          .append('/')
          .append(c.get(Calendar.DAY_OF_MONTH))
          .append('/')
          .append(c.get(Calendar.YEAR))
          .toString();
    }
    
    public String toUnicode(String s) {
      StringBuilder b = new StringBuilder();
      for (char c : s.toCharArray()) {
        if (c < 128 && c != '\'' && c != '"' && c != '\n') {
          b.append(c);
        } else {
          b.append("\\u");
          
          String hex = Integer.toHexString(c);
          if (hex.length() < 4) {
            for (int i = hex.length(); i < 4; ++i) {
              b.append('0');
            }
          }
          b.append(hex);
        }
      }
      return b.toString();      
    }
}
