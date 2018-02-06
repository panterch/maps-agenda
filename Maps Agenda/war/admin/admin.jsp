<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>

<!DOCTYPE html>
<html>
  <head>
    <title>Maps Agenda Admin Console</title>
    <link href="admin.css" rel="stylesheet" type="text/css"></link>
    <script src="admin.js"></script>
  </head>
  <body onLoad="onLoadAdmin()">
    <div id="title">
      <div class="left">Maps Agenda admin page</div>
      <div class="right">
        <%
          UserService userService = UserServiceFactory.getUserService();
          User user = userService.getCurrentUser();
          out.println(user.getNickname() + " ");
          out.println("(<a href=\"" 
                      + userService.createLogoutURL("/admin") 
                      + "\">log out</a>)");
        %>
      </div>
    </div>
    <div id="main">
      <div id="menu">
        <div id="menu_selector"></div>
        <div class="menu_item" id="events">Events</div>
      </div>
      <div id="content">
        <iframe id="content-frame" name="content-frame" src="" frameBorder="0"></iframe>
      </div>
    </div>
  </body>
</html>
