<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Calendar" %>
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
    <form action="export_xml.jsp" method="post" target="_blank">
      Exportiere XML f&uuml;r Monat: <input type="number" name=month min=1 max=12 step=1 value=<%
      Calendar now = Calendar.getInstance();
      out.print(now.get(Calendar.MONTH)+1);
      %>> <input type="number" name=year min=1900 max=3000 value=<%
      out.print(now.get(Calendar.YEAR));
      %>> <input type="submit" value="Generate"><br><br>
    
      Zu exportierende Sprachen:<br>
      // TODO implement.
    </form>
  </body>
</html>
