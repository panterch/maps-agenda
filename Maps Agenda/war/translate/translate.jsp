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
        window.open("?lang=" + language, "_self")
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
      <form name="translator">
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
  %>
  <div>
    Select language:
    <select name='language' onchange='changeLanguage(this)'>
    <%
    for (String language_code : translator.getLanguages()) {
      Language language = Language.GetByCode(language_code);
      out.print("<option value='" + language.getCode() + 
        ((language.getCode().equals(selected_language)) ? "' selected>" : "'>") +
        language.getName() + "</option>");
    }
    %>
    </select>
  </div>
  <div>
    Events:
    <%
      Language language = Language.GetByCode(selected_language);
      List<Event> events = Event.GetAllEvents();
      for (Event event : events) {
        Translation german = event.getGermanTranslation();
        Translation local = event.getTranslation(language);
        out.print("<div>");
        out.print(german.getTitle());
        out.print(", ");
        if (local == null) {
          out.print("not translated");
        } else {
          local.getTitle();
        }
        out.println("</div>");        
      }
    %>
  </div>
  <%
}
%>
      </form>
    </div>
  </body>
</html>
