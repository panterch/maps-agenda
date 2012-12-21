<%@ page contentType="text/xml;charset=UTF-8" language="java"
%><%@ page import="ch.aoz.maps.Event"
%><%@ page import="ch.aoz.maps.Translation"
%><%@ page import="java.util.Calendar"
%><%@ page import="java.util.Iterator"
%><%@ page import="java.util.List"
%><%@ page import="com.google.appengine.api.datastore.DatastoreServiceFactory"
%><%@ page import="com.google.appengine.api.datastore.DatastoreService"
%><%@ page import="com.google.appengine.api.datastore.Query"
%><%@ page import="com.google.appengine.api.datastore.Entity"
%><%@ page import="com.google.appengine.api.datastore.FetchOptions"
%><%@ page import="com.google.appengine.api.datastore.Key"
%><%@ page import="com.google.appengine.api.datastore.KeyFactory"
%><%@ page import="com.google.appengine.api.datastore.Query.CompositeFilter"
%><%@ page import="com.google.appengine.api.datastore.Query.CompositeFilterOperator"
%><%@ page import="com.google.appengine.api.datastore.Query.Filter"
%><%@ page import="com.google.appengine.api.datastore.Query.FilterPredicate"
%><%@ page import="com.google.appengine.api.datastore.Query.FilterOperator"
%><%@ page import="com.google.appengine.api.datastore.Query.SortDirection"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%><%
      int year = new Integer(request.getParameter("year"));
      Integer month;
      if (request.getParameter("month") != null) {
        month = new Integer(request.getParameter("month"));
      } else {
        month = 0;
      }

      // Offer this as a downloadable file rather than as a displayable page.
      response.setHeader("Content-Disposition", "attachment; filename="
          + String.format("%04d", year) + "-" + String.format("%02d", month) + ".xml");

      // TODO One-off
      /*
      Event event = new Event(2012, 12, 0, "hallo");
      event.addToStore();

      Translation en = new Translation(event.toEntity().getKey(),
          "en",
          "hello",
          "hello world",
          "worldwide",
          "http://maps.google.com");
      en.addToStore();
      Translation de = new Translation(event.toEntity().getKey(),
          "de",
          "hallo",
          "Hallo Welt",
          "weltweit",
          "http://maps.google.com");
      de.addToStore();
      */
      
      out.print(Event.getXMLForMonth(year, month));
%>