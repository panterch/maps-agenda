<%@ page contentType="text/xml;charset=UTF-8" language="java"
%><%@ page import="ch.aoz.maps.Event"
%><%@ page import="ch.aoz.maps.Translation"
%><%@ page import="ch.aoz.maps.XMLExport"
%><%@ page import="java.util.ArrayList"
%><%@ page import="java.util.Calendar"
%><%@ page import="java.util.Date"
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

	  // Read POST arguments.
      String[] paramEventKeys = request.getParameterValues("event");
      ArrayList<Key> eventKeys = new ArrayList<Key>();
      if (paramEventKeys != null) {
        for (int i = 0; i < paramEventKeys.length; i++) {
          eventKeys.add( KeyFactory.createKey(Event.entityKind, Long.parseLong(paramEventKeys[i])));
        }
      }

      // Offer this as a downloadable file rather than as a displayable page.
      response.setHeader("Content-Disposition", "attachment; filename=MAPS_agenda.xml");

      final List<Event> events = Event.GetEventListFromKeyList(eventKeys);
	  XMLExport export = new XMLExport(events);
	  //export.setImageList(GetEventListForTimespan(from, to));
	  //export.setTopicOfMonth(GetEventListForTimespan(from, to));
	  //export.setHighlighted(GetEventListForTimespan(from, to));

      out.print(export.getXML());%>