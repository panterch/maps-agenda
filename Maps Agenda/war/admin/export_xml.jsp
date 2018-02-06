<%@ page contentType="text/xml;charset=UTF-8" language="java"
%><%@ page import="ch.aoz.maps.Event"
%><%@ page import="ch.aoz.maps.EventDescription"
%><%@ page import="ch.aoz.maps.EventDescriptions"
%><%@ page import="ch.aoz.maps.Events"
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
%><%

	    // Read POST arguments.
      int year = Integer.parseInt(request.getParameter("year"));
      int month = Integer.parseInt(request.getParameter("month"));
      Calendar selected_month = Calendar.getInstance();
      selected_month.clear();
      selected_month.set(year, month, 1);
      
      String[] paramEventKeys = request.getParameterValues("XMLExports");
      ArrayList<Long> eventKeys = new ArrayList<Long>();
      if (paramEventKeys != null) {
        for (int i = 0; i < paramEventKeys.length; i++) {
          eventKeys.add(Long.parseLong(paramEventKeys[i]));
        }
      }

      paramEventKeys = request.getParameterValues("XMLExportsLarge");
      ArrayList<Long> eventKeysLarge = new ArrayList<Long>();
      if (paramEventKeys != null) {
        for (int i = 0; i < paramEventKeys.length; i++) {
          eventKeysLarge.add( Long.parseLong(paramEventKeys[i]));
        }
      }

      paramEventKeys = request.getParameterValues("XMLExportsTopicOfMonth");
      ArrayList<Long> eventKeysTopicOfMonth = new ArrayList<Long>();
      if (paramEventKeys != null) {
        for (int i = 0; i < paramEventKeys.length; i++) {
          eventKeysTopicOfMonth.add(Long.parseLong(paramEventKeys[i]));
        }
      }
      
      paramEventKeys = request.getParameterValues("XMLExportsImage");
      ArrayList<Long> eventKeysImage = new ArrayList<Long>();
      if (paramEventKeys != null) {
        for (int i = 0; i < paramEventKeys.length; i++) {
          eventKeysImage.add(Long.parseLong(paramEventKeys[i]));
        }
      }

      // Offer this as a downloadable file rather than as a displayable page.
      response.setHeader("Content-Disposition", "attachment; filename=MAPS_agenda.xml");

	  XMLExport export = new XMLExport(selected_month, eventKeys, eventKeysTopicOfMonth,
	                                   eventKeysImage, eventKeysLarge);

      out.print(export.getXML());%>