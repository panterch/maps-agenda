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
        <div class="menu_item" id="translators" ui-sref="translators">Translators</div>
        <div class="menu_item" id="subscribers" ui-sref="subscribers">Newsletter subscribers</div>
        <div class="menu_item" id="languages" ui-sref="languages">Supported languages</div>
        <div class="menu_item" id="phrases" ui-sref="phrases({lang: 'de'})">Translations</div>
        <div class="menu_item" id="events" ui-sref="events">Events</div>
        <div class="menu_item" id="generate" ui-sref="generate">Generate XML</div>
        <div class="menu_item" id="send_newsletter" ui-sref="newsletter">Send newsletter</div>
      </div>
      <div id="content">
        <section ui-view>
          Welcome to the Maps Agenda admin console!
        </section>
      </div>
    </div>
  </body>
</html>
