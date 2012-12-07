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

Key langKey = KeyFactory.createKey("People", "Translators");
Query query = new Query("People",  langKey);
List<Entity> translators = 
    datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
if (translators.isEmpty()) {
  out.println("No translator is yet defined.");
} else {
  out.println("There are " + translators.size() + " translators.");
}
%>
  </body>
</html>