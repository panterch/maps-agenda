<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>

<!DOCTYPE html>
<html ng-app="adminApp">
  <head>
    <meta charset="utf-8">
    <link href="css/admin.css" rel="stylesheet" type="text/css"></link>
    <script src="scripts/angular.min.js"></script>
    <script src="scripts/angular-ui-router.min.js"></script>
    <script src="scripts/admin.js"></script>
    <title>Maps Agenda Admin Console</title>
  </head>
  <body>
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
        <div class="menu_item" id="translators"><a ui-sref="translators">Translators</a></div>
        <div class="menu_item" id="subscribers"><a ui-sref="subscribers">Newsletter subscribers</a></div>
        <div class="menu_item" id="languages"><a ui-sref="languages">Supported languages</a></div>
        <div class="menu_item" id="phrases"><a ui-sref="phrases">Translations</a></div>
        <div class="menu_item" id="events"><a ui-sref="events">Events</a></div>
        <div class="menu_item" id="generate"><a ui-sref="generate">Generate XML</a></div>
        <div class="menu_item" id="send_newsletter"><a ui-sref="newsletter">Send newsletter</a></div>
      </div>
      <div id="content">
        <section ui-view>
          Welcome to the Maps Agenda admin console!
        </section>
      </div>
    </div>
  </body>
</html>
