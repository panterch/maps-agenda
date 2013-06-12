<%@ page contentType="text/html;charset=UTF-8" language="java"
%><%@ page import="ch.aoz.maps.Event"
%><%@ page import="ch.aoz.maps.Translation"
%><%@ page import="java.util.ArrayList"
%><%@ page import="java.util.Calendar"
%><%@ page import="java.util.Date"
%><%@ page import="java.util.Iterator"
%><%@ page import="java.util.List"
%><%@ page import="java.util.Properties"
%><%@ page import="javax.mail.Message"
%><%@ page import="javax.mail.MessagingException"
%><%@ page import="javax.mail.Session"
%><%@ page import="javax.mail.Transport"
%><%@ page import="javax.mail.internet.AddressException"
%><%@ page import="javax.mail.internet.InternetAddress"
%><%@ page import="javax.mail.internet.MimeMessage"
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
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<%!
/*
    // Read POST arguments.
      String[] paramEventKeys = request.getParameterValues("XMLExports");
      ArrayList<Key> eventKeys = new ArrayList<Key>();
      if (paramEventKeys != null) {
        for (int i = 0; i < paramEventKeys.length; i++) {
          eventKeys.add(KeyFactory.createKey(Event.entityKind, Long.parseLong(paramEventKeys[i])));
        }
      }

      paramEventKeys = request.getParameterValues("XMLExportsLarge");
      ArrayList<Key> eventKeysLarge = new ArrayList<Key>();
      if (paramEventKeys != null) {
        for (int i = 0; i < paramEventKeys.length; i++) {
          eventKeysLarge.add(KeyFactory.createKey(Event.entityKind, Long.parseLong(paramEventKeys[i])));
        }
      }

      paramEventKeys = request.getParameterValues("XMLExportsTopicOfMonth");
      ArrayList<Key> eventKeysTopicOfMonth = new ArrayList<Key>();
      if (paramEventKeys != null) {
        for (int i = 0; i < paramEventKeys.length; i++) {
          eventKeysTopicOfMonth.add(KeyFactory.createKey(Event.entityKind, Long.parseLong(paramEventKeys[i])));
        }
      }
      
      paramEventKeys = request.getParameterValues("XMLExportsImage");
      ArrayList<Key> eventKeysImage = new ArrayList<Key>();
      if (paramEventKeys != null) {
        for (int i = 0; i < paramEventKeys.length; i++) {
          eventKeysImage.add(KeyFactory.createKey(Event.entityKind, Long.parseLong(paramEventKeys[i])));
        }
      }

      // Offer this as a downloadable file rather than as a displayable page.
      response.setHeader("Content-Disposition", "attachment; filename=MAPS_agenda.xml");
*/

public static String send(String toAddress, 
                          String subject, 
                          String msgBody) {
  Properties props = new Properties();
  Session session = Session.getDefaultInstance(props, null);

  try {
    Message msg = new MimeMessage(session);
    msg.setFrom(new InternetAddress("no-reply@maps-agenda.com"));
    InternetAddress to = new InternetAddress(toAddress);
    msg.addRecipient(Message.RecipientType.TO, to);
    msg.setSubject(subject);
    msg.setText(msgBody);
    Transport.send(msg, new InternetAddress[] { to });
  } catch (AddressException addressException) {
    return "Address Exception , mail could not be sent: " + addressException.getMessage();
  } catch (MessagingException messageException) {
    return "Messaging Exception , mail could not be sent " + messageException.getMessage();
  }  
  return "Email sent!";
}

%>

<%
int num_events = 0;
if (request.getParameterValues("XMLExports") != null) {
  num_events = request.getParameterValues("XMLExports").length;
}
String result = send("tobulogic@gmail.com", 
                     "Test send newsletter", 
                     "Looks like " + num_events + " events are selected.");

out.println(result);
%>
