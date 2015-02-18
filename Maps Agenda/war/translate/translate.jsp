<%@page contentType="text/html;charset=UTF-8" language="java"%>
<%@page import="java.util.List" %>
<%@page import="ch.aoz.maps.Event" %>
<%@page import="ch.aoz.maps.EventDescriptions"%>
<%@page import="ch.aoz.maps.EventDescription"%>
<%@page import="ch.aoz.maps.Events"%>
<%@page import="ch.aoz.maps.Translator" %>
<%@page import="ch.aoz.maps.Language" %>
<%@page import="ch.aoz.maps.Strings"%>
<%@page import="com.google.appengine.api.users.User" %>
<%@page import="com.google.appengine.api.users.UserService" %>
<%@page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@page import="java.util.Calendar" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%!
      
public static String escapeHTML(String raw) {
  if (raw == null) {
    return null;
  }
  return raw.replaceAll("&",  "&amp;")
             .replaceAll("\"", "&quot;")
             .replaceAll("<",  "&lt;")
             .replaceAll(">",  "&gt;");
}

public static String formatTranslation(
    EventDescription d, String cls, boolean rtl) {
  String result = "";
  if (rtl) {
    cls = cls + " rtl";
  }
  result += "<div class='translation " + cls + "'>";
  if (d == null) {
    result += "<div class='no-translation'>No translation</div>";
  } else { // edit ==  false
    result += "<div class='title'>&quot;" + d.getTitle() + "&quot;</div>";
    result += "<div class='desc'>" + d.getDesc() + "</div>";
  }
  result += "</div>";
  return result;
}

public static String formatTranslationForm(
    EventDescription d, long event_id, String div_name, boolean rtl) {
  String result = "";
  String title = "";
  String desc = ""; 
  if (d != null) {
    title = d.getTitle();
    desc = d.getDesc();
  }
  String form_name = "form-" + event_id;
  if (rtl) {
    result += "<div class='translation form rtl'>";
  } else {
    result += "<div class='translation form'>";
  }
  result += "<form name='" + form_name + "' id='" + form_name + "' method='POST'>";
  result += "<input type='hidden' name='event_id' value='" + event_id + "'>";
  result += "<div class='title'><input type='text' name='title' value='" + escapeHTML(title) + "' placeholder='Title'></input></div>";
  result += "<div class='desc'><textarea rows='4' cols='50' name='desc' placeholder='Description'>" + escapeHTML(desc) + "</textarea></div>";
  result += "<div class='form-actions'><span class='save button' onclick='save(\""
    + form_name + "\");'>Save</span><span class='cancel button' onclick='mode(\"" 
    + div_name + "\", \"expanded\");'>Cancel</span></div>";
  result += "</form>";
  result += "</div>";
  return result;
}

public static String createSelectForm(Calendar selected_month, String selected_lang, List<String> languages) {
  if (selected_month == null) {
    selected_month = Calendar.getInstance();
  }
  int month = selected_month.get(Calendar.MONTH);
  StringBuilder form = new StringBuilder();
  
  form.append("<div id='event-date'>");
  form.append("<form name='date' method='GET' style='display:inline'>");
  form.append("<p>Select language: ");
  form.append("<select name='lang'>");
  for (String language_code : languages) {
    Language language = Language.GetByCode(language_code);
    form.append("<option value='" + language_code + 
      ((language_code.equals(selected_lang)) ? "' selected>" : "'>") +
      (language == null ? "Unknown language: " + language_code 
                        : language.getName()) + "</option>");
  }  
  form.append("</select></p>");
  form.append("<p>Month to display (Year-Month): ");
  form.append("<input type='text' name='eyear' value='" + selected_month.get(Calendar.YEAR) + "' maxlength='4' size='4'>");
  form.append("-<select name='emonth'>");
  for (int i = 0; i < 12; ++i) {
    form.append("<option value='" + i + (i == month? "' selected>" : "'>") + Strings.months_de[i] + "</option>");
  }
  form.append("</select></p>");
  //form.append("<input type='hidden' name='lang' value='" + selected_lang + "'>");
  form.append("<p><input type='submit' value='Show'></p></form>");
  form.append("</div>");
  return form.toString();
}
%>

<%
UserService userService = UserServiceFactory.getUserService();
User user = userService.getCurrentUser();
%>

<html>
  <head>
    <title>Maps Agenda Translation Console</title>
    <link href="translate.css" rel="stylesheet" type="text/css"></link>
    <script>
      function mode(div_id, mode) {
        var div = document.getElementById(div_id);
        div.className = mode;
      }
      function save(form_id) {
        var form = document.getElementById(form_id);
        form.submit();
      }
    </script>
  </head>
  <body>
    <div id="title">
      <div class="left">Maps Agenda translate page</div>
      <div class="right">
<%
if (user == null) {
  out.println("Not logged in ");
  out.println("(<a href=\""
              + userService.createLoginURL("/translate")
              + "\">log in</a>)");
} else {
  out.println(user.getNickname() + " ");
  out.println("(<a href=\""
              + userService.createLogoutURL("/translate")
              + "\">log out</a>)");
}
%>
      </div>
    </div>
    <div id="main">
<%
if (user == null) {
  out.println("<p>You are not logged in. Please log in.");
} else if (!Translator.exists(user.getEmail())) {
  out.print("<p>You are not registered as a translator (");
  out.print(user.getEmail());
  out.println("). Please contact a Maps Agenda administrator</p>");
} else {
  Translator translator = Translator.GetByEmail(user.getEmail());
  List<String> languages = translator.getLanguages();
  String selected_language = request.getParameter("lang");
  if (selected_language == null) {
    selected_language = languages.get(0);
  }
  Calendar selected_month = Calendar.getInstance();
  if (request.getParameter("eyear") != null) {
    int year = Integer.parseInt(request.getParameter("eyear"));
    int month = Integer.parseInt(request.getParameter("emonth"));
    selected_month.clear();
    selected_month.set(year, month, 1);
  } else {
    selected_month.add(Calendar.MONTH, 1);
    int year = selected_month.get(Calendar.YEAR);
    int month = selected_month.get(Calendar.MONTH);
  }
  Events events = Events.getEvents(selected_month);
  long active_event_id = -1;
  if (request.getParameter("event_id") != null) {
    try {
      long event_id = Long.parseLong(request.getParameter("event_id"));
      active_event_id = event_id;
      String title = request.getParameter("title");
      String desc = request.getParameter("desc");
      Event event = null;
      for (Event e : events.getSortedEvents()) {
        if (e.getKey() == event_id) {
          event = e;
          break;
        }
      }
      if (event == null) {
        throw new Exception("Unknown event");
      }
      Language language = Language.GetByCode(selected_language);
      if (language == null) {
        throw new Exception("Unknown language");
      }
      EventDescription d = new EventDescription(language.getCode(), title, desc);
      event.setDescription(d);
      if (!EventDescriptions.addDescription(event)) {
        out.print("<div class='error'>");
        out.print("Error saving event description:<br>");
        out.print("</div>");
      }
    } catch (Exception ex) {
      out.print("<div class='error'>Error saving translation: " + ex.toString() + "</div>");
    }
  }
  EventDescriptions german_descriptions = EventDescriptions.getDescriptions("de", selected_month);
  EventDescriptions local_descriptions = EventDescriptions.getDescriptions(selected_language, selected_month);
  %>
  <div>
    <%
    out.println(createSelectForm(selected_month, selected_language, translator.getLanguages()));
    %>
  </div>
  <div>
    <% 
    if (events.getSortedEvents().size() == 0) {
    %>
    No events to translate for this month.
    <%
    } else {
    %>
    Events:
    <%
      Language language = Language.GetByCode(selected_language);
      if (language == null) {
        out.print("<div>Unknown language: " + selected_language + "</div>");
      } else {
        for (Event event : events.getSortedEvents()) {
          long event_id = event.getKey();
          String div_id = "event-" + event_id;
          EventDescription german = german_descriptions.getDescription(event.getKey());
          EventDescription local = local_descriptions.getDescription(event.getKey());
          out.print("<div class='event'>");
          out.print("<div class='collapsed' id='" + div_id + "'>");
          out.print(formatTranslation(german, "original", false));
          out.print(formatTranslation(local, "translated", language.isRightToLeft()));
          out.print(formatTranslationForm(local, event_id, div_id, language.isRightToLeft()));
          out.print("<div class='actions'>");
          out.print("<span class='edit button' onclick='mode(\"" + div_id + "\", \"edit\");'>Edit</span>");
          out.print("<span class='more button' onclick='mode(\"" + div_id + "\", \"expanded\");'>Expand</span>");
          out.print("<span class='less button' onclick='mode(\"" + div_id + "\", \"collapsed\");'>Collapse</span>");
          out.print("</div>"); // actions        
          out.print("</div>"); // collapsed
          out.print("</div>"); // event        
        }
      }
    }
    %>
  </div>
  <%
}
%>
    </div>
  </body>
</html>
