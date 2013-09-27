package ch.aoz.maps;

import java.io.IOException;
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
      // TODO: make these dynamic.
      // Calendar selected_month = Calendar.getInstance();
      int year = 2013;
      int month = 6; // July
      Language l = Language.GetByCode("de");
      List<Event> events = Event.GetEventListForMonth(year, month);
      
        
      StringBuilder response = new StringBuilder();
      response.append("/* version 0.1 */\n");
      response.append("var DAYS_OF_WEEK = [\n");
      // TODO: translate those too.
      response.append("  'Monday', 'Tuesday', 'Wednesday', 'Thursday',\n");
      response.append("  'Friday', 'Saturday', 'Sunday'\n");
      response.append("];\n");
      response.append("\n");
      response.append("function EventsCtrl($scope) {\n");
      response.append("  $scope.queryEvents = function() {\n");
      response.append("    $scope.events = [\n");
      for (Event e : events) {
        Translation t = e.getTranslation(l);
        response.append("      {\n");
        response.append("        date: '").append(dateToString(e.getDate())).append("',\n");
        response.append("        title: '").append(toUnicode(t.getTitle())).append("',\n");
        // TODO: new lines in the description break the system...
        response.append("        description: '").append(toUnicode(t.getDesc())).append("',\n");
        response.append("        url: '").append(toUnicode(t.getUrl())).append("'\n");
        response.append("      },\n");
        break;
      }
      response.deleteCharAt(response.length() - 2);  // remove the last ,
      response.append("    ];\n");
      response.append("  };\n");
      response.append("\n");
      response.append("  $scope.printDate = function(dateStr) {\n");
      response.append("    var date = new Date(dateStr);\n");
      response.append("    return date.getDate() + '. ' + (date.getMonth() + 1) + '.';\n");
      response.append("  };\n");
      response.append("\n");
      response.append("  $scope.printDay = function(dateStr) {\n");
      response.append("    var date = new Date(dateStr);\n");
      response.append("    return DAYS_OF_WEEK[date.getDay()];\n");
      response.append("  };\n");
      response.append("\n");
      response.append("};\n");
              
      resp.setContentType("application/javascript");
      resp.getWriter().println(response.toString());
    }

    public String dateToString(Date d) {      
      Calendar c = Calendar.getInstance();
      c.setTime(d);
      return new StringBuilder()
          .append(c.get(Calendar.MONTH))
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
