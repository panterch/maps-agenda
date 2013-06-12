<%@ page contentType="text/html;charset=UTF-8" language="java"
%><%@ page import="ch.aoz.maps.Event"
%><%@ page import="ch.aoz.maps.NewsletterExport"
%><%@ page import="ch.aoz.maps.Subscriber"
%><%@ page import="ch.aoz.maps.Translation"
%><%@ page import="java.io.UnsupportedEncodingException"
%><%@ page import="java.util.ArrayList"
%><%@ page import="java.util.Calendar"
%><%@ page import="java.util.Date"
%><%@ page import="java.util.HashMap"
%><%@ page import="java.util.Iterator"
%><%@ page import="java.util.List"
%><%@ page import="java.util.Map"
%><%@ page import="java.util.Properties"
%><%@ page import="javax.mail.Message"
%><%@ page import="javax.mail.MessagingException"
%><%@ page import="javax.mail.Session"
%><%@ page import="javax.mail.Transport"
%><%@ page import="javax.mail.internet.AddressException"
%><%@ page import="javax.mail.internet.InternetAddress"
%><%@ page import="javax.mail.internet.MimeMessage"
%><%@ page import="com.google.appengine.api.datastore.Key"
%><%@ page import="com.google.appengine.api.datastore.KeyFactory"
%>

<%!
public static boolean send(String toAddress, 
                           String subject, 
                           String msgBody) {
  Properties props = new Properties();
  Session session = Session.getDefaultInstance(props, null);

  try {
    Message msg = new MimeMessage(session);
    msg.setFrom(new InternetAddress("no.reply.maps.agenda@gmail.com",
                                    "MAPS Agenda (AOZ)"));
    InternetAddress to = new InternetAddress(toAddress);
    msg.addRecipient(Message.RecipientType.TO, to);
    msg.setSubject(subject);
    msg.setContent(msgBody, "text/html; charset=utf-8");
    Transport.send(msg, new InternetAddress[] { to });
  } catch (Exception e) {
    return false;
  }  
  return true;
}

%>

<%
String[] months = new String[]{
    "Januar",
    "Februar",
    "März",
    "April",
    "Mai",
    "Juni",
    "Juli",
    "August",
    "September",
    "Oktober",
    "November",
    "Dezember"
};

String[] paramEventKeys = request.getParameterValues("XMLExports");
ArrayList<Key> eventKeys = new ArrayList<Key>();
if (paramEventKeys != null) {
  for (int i = 0; i < paramEventKeys.length; i++) {
    eventKeys.add(KeyFactory.createKey(Event.entityKind, Long.parseLong(paramEventKeys[i])));
  }
}
List<Event> events = Event.GetEventListFromKeyList(eventKeys);

if (events.size() == 0) {
  out.println("No event is selected.");
  return;
}

Calendar c = Calendar.getInstance();
c.setTime(events.get(0).getDate());
String subject = months[c.get(Calendar.MONTH)] + " Kultur- und Freizeitangebote";

HashMap<String, NewsletterExport> exporters = new HashMap<String, NewsletterExport>();
int num_emails_sent = 0;
for (Subscriber subscriber : Subscriber.getAllSubscribers().values()) {
  String language = subscriber.getLanguage();
  if (!exporters.containsKey(language)) {
    exporters.put(language, new NewsletterExport(events, language));
  }
  if (send(subscriber.getEmail(),
           subject,
           exporters.get(language).render())) {
    num_emails_sent++;
  }
}
out.println(String.format("%d email have been sent.", num_emails_sent));
%>
