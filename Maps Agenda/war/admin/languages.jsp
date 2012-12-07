<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.google.appengine.api.datastore.DatastoreServiceFactory" %>
<%@ page import="com.google.appengine.api.datastore.DatastoreService" %>
<%@ page import="com.google.appengine.api.datastore.Query" %>
<%@ page import="com.google.appengine.api.datastore.Entity" %>
<%@ page import="com.google.appengine.api.datastore.FetchOptions" %>
<%@ page import="com.google.appengine.api.datastore.Key" %>
<%@ page import="com.google.appengine.api.datastore.KeyFactory" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<html>
  <body>
<%
DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

Key langKey = KeyFactory.createKey("Languages", "MapsAgenda");
Query query = new Query("Languages",  langKey);
List<Entity> languages = 
    datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
if (languages.isEmpty()) {
  out.println("No language is yet defined.");
} else {
  out.println("There are " + languages.size() + " defined languages.");
}
%>
  </body>
</html>