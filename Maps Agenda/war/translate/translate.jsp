<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="ch.aoz.maps.Translator" %>
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
  out.println("<p>Welcome!</p>");
}
%>
    </div>
  </body>
</html>
