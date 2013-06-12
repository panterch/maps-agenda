<%@ page contentType="text/html;charset=UTF-8" language="java"
%><%@ page import="ch.aoz.maps.Event"
%><%@ page import="ch.aoz.maps.NewsletterExport"
%><%@ page import="ch.aoz.maps.Subscriber"
%><%@ page import="java.util.List"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<%
// Read arguments.
if (request.getParameter("lang") == null ||
    request.getParameter("month") == null) {
  out.println("Bad URL");
  return;
}
String language = request.getParameter("lang");
int month;
try {
  month = Integer.parseInt(request.getParameter("month"));
} catch (NumberFormatException e) {
  out.println("Bad month parameter: " + request.getParameter("month"));
  return;
}
if (month < 0 || month > 11) {
  out.println("Bad month parameter: " + request.getParameter("month"));
  return;  
}

int year;
try {
  year = Integer.parseInt(request.getParameter("year"));
} catch (NumberFormatException e) {
  out.println("Bad year parameter: " + request.getParameter("year"));
  return;
}

String baseUrl = "localhost".equals(request.getServerName()) ? 
    "http://localhost:8888" : "http://maps-agenda.appspot.com";

String themeId = "1";
List<Event> events = Event.GetEventListForMonth(year, month);

NewsletterExport exporter = new NewsletterExport(
    events, language,
    baseUrl, themeId,
    year, month,
    null /* subscriber, none for public render. */);
out.println(exporter.render());
%>
