<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="ch.aoz.maps.Translator" %>
<%@ page import="ch.aoz.maps.Translation" %>
<%@ page import="ch.aoz.maps.Language" %>
<%@ page import="ch.aoz.maps.Event" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

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

public static String formatTranslation(Translation translation, String cls) {
  String result = "";
  result += "<div class='translation " + cls + "'>";
  if (translation == null) {
    result += "<div class='no-translation'>No translation</div>";
  } else { // edit ==  false
    result += "<div class='title'>&quot;" + translation.getTitle() + "&quot;</div>";
    result += "<div class='desc'>" + translation.getDesc() + "</div>";
    result += "<div class='location'>Location: " + translation.getLocation() + "</div>";
    result += "<div class='url'>URL: " + translation.getUrl() + "</div>";
  }
  result += "</div>";
  return result;
}

public static String formatTranslationForm(
    Translation translation, long event_id, String div_name) {
  String result = "";
  String title = "";
  String desc = ""; 
  String location = ""; 
  String url = ""; 
  if (translation != null) {
    title = translation.getTitle();
    desc = translation.getDesc();
    location = translation.getLocation();
    url = translation.getUrl();
  }
  String form_name = "form-" + event_id;
  result += "<div class='translation form'>";
  result += "<form name='" + form_name + "' id='" + form_name + "' method='POST'>";
  result += "<input type='hidden' name='event_id' value='" + event_id + "'>";
  result += "<div class='title'><input type='text' name='title' value='" + escapeHTML(title) + "' placeholder='Title'></input></div>";
  result += "<div class='desc'><textarea rows='4' cols='50' name='desc' placeholder='Description'>" + escapeHTML(desc) + "</textarea></div>";
  result += "<div class='location'><input type='text' name='location' value='" + escapeHTML(location) + "' placeholder='Location'></input></div>";
  result += "<div class='url'><input type='text' name='url' value='" + escapeHTML(url) + "' placeholder='URL'></input></div>";
  result += "<div class='form-actions'><span class='save button' onclick='save(\""
    + form_name + "\");'>Save</span><span class='cancel button' onclick='mode(\"" 
    + div_name + "\", \"expanded\");'>Cancel</span></div>";
  result += "</form>";
  result += "</div>";
  return result;
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
      function changeLanguage(sel) {
        var language = sel.options[sel.selectedIndex].value;
        window.open("?lang=" + language, "_self");
      }
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
  long active_event_id = -1;
  if (request.getParameter("event_id") != null) {
    try {
      long event_id = Long.parseLong(request.getParameter("event_id"));
      active_event_id = event_id;
      String title = request.getParameter("title");
      String desc = request.getParameter("desc");
      String location = request.getParameter("location");
      String url = request.getParameter("url");
      Event event = Event.GetByKey(event_id);
      if (event == null) {
        throw new Exception("Unknown event");
      }
      Language language = Language.GetByCode(selected_language);
      if (language == null) {
        throw new Exception("Unknown language");
      }
      Translation translation = event.getTranslation(language);
      if (translation == null) {
        translation = new Translation(event, language);
      }
      translation.setTitle(title);
      translation.setDesc(desc);
      translation.setLocation(location);
      translation.setUrl(url);
      if (!translation.addToStore()) {
        out.print("<div class='error'>");
        out.print("Error saving translation:<br>");
        for (String error : translation.getErrors()) {
          out.print(error + "<br>");
        } 
        out.print("</div>");
      }
    } catch (Exception ex) {
      out.print("<div class='error'>Error saving translation: " + ex.toString() + "</div>");
    }
  }
  %>
  <div>
    Select language:
    <select name='language' onchange='changeLanguage(this)'>
    <%
    for (String language_code : translator.getLanguages()) {
      Language language = Language.GetByCode(language_code);
      out.print("<option value='" + language_code + 
        ((language_code.equals(selected_language)) ? "' selected>" : "'>") +
        (language == null ? "Unknown language: " + language_code 
                          : language.getName()) + "</option>");
    }
    %>
    </select>
  </div>
  <div>
    Events:
    <%
      Language language = Language.GetByCode(selected_language);
      if (language == null) {
        out.print("<div>Unknown language: " + selected_language + "</div>");
      } else {
        List<Event> events = Event.GetAllEvents();
        for (Event event : events) {
          long event_id = event.getKey();
          String div_id = "event-" + event_id;
          Translation german = event.getGermanTranslation();
          Translation local = event.getTranslation(language);
          out.print("<div class='event'>");
          out.print("<div class='collapsed' id='" + div_id + "'>");
          out.print(formatTranslation(german, "original"));
          out.print(formatTranslation(local, "translated"));
          out.print(formatTranslationForm(local, event_id, div_id));
          out.print("<div class='actions'>");
          out.print("<span class='edit button' onclick='mode(\"" + div_id + "\", \"edit\");'>Edit</span>");
          out.print("<span class='more button' onclick='mode(\"" + div_id + "\", \"expanded\");'>Expand</span>");
          out.print("<span class='less button' onclick='mode(\"" + div_id + "\", \"collapsed\");'>Collapse</span>");
          out.print("</div>"); // actions        
          out.print("</div>"); // collapsed
          out.print("</div>"); // event        
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
