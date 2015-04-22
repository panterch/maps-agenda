<%@ page contentType="text/html;charset=UTF-8" language="java"
%><%@ page import="ch.aoz.maps.Events"
%><%@ page import="ch.aoz.maps.Language"
%><%@ page import="ch.aoz.maps.NewsletterExport"
%><%@ page import="ch.aoz.maps.Subscriber"
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

public static String[] months = new String[]{"Januar", "Februar", "März", 
    "April", "Mai", "Juni", "Juli", "August", "September", "Oktober", 
    "November", "Dezember"};

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

public static String createForms(Calendar selected_month,
                                 Map<String, Language> languages, 
                                 String selected_lang) {
  if (selected_month == null) {
    selected_month = Calendar.getInstance();
  }
  int month = selected_month.get(Calendar.MONTH);
  StringBuilder form = new StringBuilder();
  form.append("<div id='event-date'>");
  form.append("<div class='title'>Select month and language: </div>");
  form.append("<form name='date' method='GET' target='content-frame'>");
  form.append("<p>Year-Month: <input type='text' name='eyear' value='" + selected_month.get(Calendar.YEAR) + "' maxlength='4' size='4'>");
  form.append("-<select name='emonth'>");
  for (int i = 0; i < 12; ++i) {
    form.append("<option value='" + i + (i == month? "' selected>" : "'>") + months[i] + "</option>");
  }
  form.append("</select></p>");
  form.append("<p>Language: <select name='elang'>");
  for (Language l : languages.values()) {
    if (l.getCode() == selected_lang) {
      form.append(l.getCode());
    }
    form.append("<option value='" + l.getCode() + (l.getCode().equals(selected_lang) ? "' selected>" : "'>") + l.getGermanName() + "</option>");
  }
  form.append("</select></p>");
  form.append("<p><input type='submit' value='Show Newsletter'></p></form>");
  form.append("<div id='send-form'>");
  form.append("<div class='title'>Send the newsletters:</div>");
  form.append("<form name='send' method='GET' target='content-frame'>");
  form.append("<input type='hidden' name='eyear' value='" + selected_month.get(Calendar.YEAR) + "'>");
  form.append("<input type='hidden' name='emonth' value='" + selected_month.get(Calendar.MONTH) + "'>");
  form.append("<input type='hidden' name='elang' value='" + selected_lang + "'>");
  form.append("<input type='hidden' name='send' value='send'>");
  form.append("<p><input type='submit' value='Send Newsletter'></p></form>");
  return form.toString();
}

%>

<html>
<head>
<style type="text/css">
form p {
  padding: 0px;
  margin: 2px;
}
#new-p-link {
  margin-bottom: 10px;
}
.pdiv {
  display: none;
}
.msg-red {
  background-color: #f33;
  border-radius: 5px;
  height: 50px;
  padding: 5px;
  margin-bottom: 20px;
}
.msg-green {
  background-color: #5a5;
  border-radius: 5px;
  height: 50px;
  padding: 5px;
  margin-bottom: 20px;
}
.title {
  font-size: x-large;
  border-radius: 5px;
  padding: 5px;
  background-color: #ba7;
  margin-bottom: 10px;
}
table {
  border-collapse: collapse;
  border: 1px solid black;
  margin-bottom: 10px;
}
th, td {
  border: 1px solid black;
  padding: 5px;
}
</style>
</head>
<body>
<%
Map<String, Language> languages = Language.getAllLanguagesAsMap();
Calendar selected_month = Calendar.getInstance();
if (request.getParameter("eyear") != null) {
  int year = Integer.parseInt(request.getParameter("eyear")); 
  int month = Integer.parseInt(request.getParameter("emonth"));
  selected_month.clear();
  selected_month.set(year, month, 1);
} else {
  selected_month.set(Calendar.DAY_OF_MONTH, 1);
}
String lang = request.getParameter("elang");
if (lang == null) {
  lang = "de";
}

Events eventsDe = Events.getEvents(selected_month, "de");
Events eventsLang = null;
if (!lang.equals("de")) {
  eventsLang = (Events)eventsDe.clone();
  eventsLang.loadDescriptions(lang);
}
int numEvents = eventsDe.getSortedEvents().size();

int year = selected_month.get(Calendar.YEAR);
int month = selected_month.get(Calendar.MONTH);
String subject = months[month] + " Kultur- und Freizeitangebote";

String baseUrl = "localhost".equals(request.getServerName()) ? 
    "http://localhost:8888" : "http://maps-agenda.appspot.com";

NewsletterExport exporter = null;
if (request.getParameter("send") != null && numEvents > 0) {
  int num_emails_sent = 0;
  for (Subscriber subscriber : Subscriber.getAllSubscribers().values()) {
    String language = subscriber.getLanguage();

    // One created for each subscriber.
    exporter = new NewsletterExport(
        eventsDe, eventsLang, language,
        baseUrl, year, month,
        subscriber);
    
    if (send(subscriber.getEmail(),
             subject,
             exporter.render())) {
      num_emails_sent++;
    }
  }
  out.println("<div class='msg-green'><p>" + 
              String.format("%d email have been sent.", num_emails_sent) + 
              "</p></div>");
}

out.println(createForms(selected_month, languages, lang));

if (numEvents == 0) {
  out.println("No event is selected.");
  return;
}

exporter = new NewsletterExport(
      eventsDe, eventsLang, lang,
      baseUrl, year, month,
      null);
out.println(exporter.render());

%>
