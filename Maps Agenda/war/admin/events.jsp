<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Calendar" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="ch.aoz.maps.Event" %>
<%@ page import="ch.aoz.maps.Translation" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%!
public static String createEventDiv(Event e) {
  Translation de = e.getGermanTranslation();
  StringBuilder div = new StringBuilder();
  div.append("<div class='event'>");
  div.append("<div class='eedit'><a onclick=\"show_box('event-" + e.getKey() + "');\" href='javascript:void(0);'>Edit</a></div>");
  div.append("<div class='edate'>" + new SimpleDateFormat("yyyy-MMM-dd").format(e.getDate()) + "</div>");
  div.append("<div class='ebody'>");
  div.append("<div class='etitle'>\"" + de.getTitle() + "\"</div>");
  div.append("<div class='edesc'>" + de.getDesc() + "</div>");
  div.append("<div class='eloc'><b>@ </b>" + de.getLocation() + "</div>");
  div.append("<div class='eurl'><b>&#x21D2; </b><a href='" + de.getUrl() + "'>" + de.getUrl() + "</a></div>");
  div.append("</div></div>");
  return div.toString();
}

public static String createEventForm(String formName, Event e, Calendar selected_month) {
  Translation de = null;
  StringBuilder form = new StringBuilder();
  if (e == null) {
    form.append("<div id='event-new' class='eventdiv'>");
    form.append("<div class='title'>Add a new event ");
    form.append("<a onclick=\"hide_box('event-new'); show('new-event-link')\" href='javascript:void(0);''>(hide)</a></div>");
  } else {
    de = e.getGermanTranslation();
    form.append("<div id='event-" + e.getKey() + "' class='eventdiv'>");
    form.append("<div class='title'>Update " + de.getTitle() + " ");
    form.append("<a onclick=\"hide_box('event-" + e.getKey() + "')\" href='javascript:void(0);''>(hide)</a></div>");
  }
  form.append("<form name='" + formName + "' method='POST' target='content-frame'");
  form.append(" action='' onSubmit=\"return validateForm('" + formName + "')\">");
  if (selected_month != null) {
    form.append("<input type='hidden' name='eyear' value='" + selected_month.get(Calendar.YEAR) + "'></p>");    
    form.append("<input type='hidden' name='emonth' value='" + selected_month.get(Calendar.MONTH) + "'></p>");
  } else {
    selected_month = Calendar.getInstance();
  }
  if (e == null) {
    form.append("<input type='hidden' name='key' value=''></p>");    
    form.append("<input type='hidden' name='new' value='true'></p>");    
    form.append("<p>Year-Month-Day: <input type='text' name='year' value='" + selected_month.get(Calendar.YEAR) + "' maxlength='4' size='4'>");
    form.append("-<input type='text' name='month' value='" + selected_month.get(Calendar.MONTH) + "' maxlength='2' size='2'>");
    form.append("-<input type='text' name='day' value='' maxlength='2' size='2'></p>");
    form.append("<p>Title: <input type='text' name='title' value=''></p>");
    form.append("<p>Description:<p> <textarea rows='10' cols='50' name='desc'></textarea>");
    form.append("<p>Location: <input type='text' name='loc' value=''></p>");
    form.append("<p>Url: <input type='text' name='url' value=''></p>");
  } else {
    Calendar c = Calendar.getInstance();
    c.setTime(e.getDate());
    form.append("<input type='hidden' name='key' value='" + e.getKey() + "'></p>");    
    form.append("<input type='hidden' name='new' value='false'></p>");    
    form.append("<p>Year-Month-Day: <input type='text' name='year' value='" + c.get(Calendar.YEAR) + "' maxlength='4' size='4'>");
    form.append("-<input type='text' name='month' value='" + c.get(Calendar.MONTH) + "' maxlength='2' size='2'>");
    form.append("-<input type='text' name='day' value='" + c.get(Calendar.DAY_OF_MONTH) + "' maxlength='2' size='2'></p>");
    form.append("<p>Title: <input type='text' name='title' value='" + de.getTitle() + "'></p>");
    form.append("<p>Description:<p> <textarea rows='10' cols='50' name='desc'>" + de.getDesc() + "</textarea>");
    form.append("<p>Location: <input type='text' name='loc' value='" + de.getLocation() + "'></p>");
    form.append("<p>Url: <input type='text' name='url' value='" + de.getUrl() + "'></p>");
  }
  form.append("<p><input type='submit' value='" + (e == null ? "Add": "Update") + " event'></p>");
  form.append("</form></div>");
  return form.toString();
}

public static String createXMLForm() {
  StringBuilder form = new StringBuilder();
  form.append("<div id='event-xml' class='eventdiv'>");
  form.append("<div class='title'>Import an event ");
  form.append("<a onclick=\"hide_box('event-xml');\" href='javascript:void(0);''>(hide)</a></div>");
  form.append("<form name='xml' method='POST' target='content-frame' enctype='multipart/form-data'");
  form.append(" action='' onSubmit=\"return validateXMLForm()\">");
  form.append("<p><input type='file' name='file'></p>");
  form.append("<p><input type='submit' value='Import event'></p>");
  form.append("</form></div>");
  return form.toString();
}

public static String createSelectForm(Calendar selected_month) {
  if (selected_month == null) {
    selected_month = Calendar.getInstance();
  }
  StringBuilder form = new StringBuilder();
  form.append("<div id='event-date'>");
  form.append("<div class='title'>Select month to display</div>");
  form.append("<form name='date' method='GET' target='content-frame'>");
  form.append("<p>Year-Month: <input type='text' name='eyear' value='" + selected_month.get(Calendar.YEAR) + "' maxlength='4' size='4'>");
  form.append("-<input type='text' name='emonth' value='" + selected_month.get(Calendar.MONTH) + "' maxlength='2' size='2'>");
  form.append("<p><input type='submit' value='Show'></form>");
  form.append("<form name='date_all' method='GET' target='content-frame'><input type='submit' value='Show all'>");
  form.append("</form></p></div>");
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
#new-event-link {
  margin-bottom: 10px;
}
.eventdiv {
  display: none;
}
.event {
  background-color: #fff;
  border-radius: 5px;
  padding: 5px;
  margin-bottom: 10px;
}
.edate {
  float: left;
  overflow: auto;
  width: 100px;
}
.eedit {
  float: right;
  width: 30px;
}
.ebody {
  margin-left: 20px;
  background-color: #ddd;
  border-radius: 5px;
  overflow: auto;
  padding: 5px;
}
.etitle {
  font-size: large;
}
.edesc {
  border-bottom: 1px solid #888;
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
<script>
function validateXMLForm() {
  var form = document.forms["xml"];
  var reader = new FileReader();
  reader.onload = (function(f) { alert(f); });
  reader.readAsText(form.file);
}
function test(f) {
  alert(f);
}

function validateForm(formName) {
  var intRegex = /^\d+$/;

  var form = document.forms[formName];
  
  var year = form.year.value;
  if(year == "") {
    alert("Please fill in the year this event is happening.");
    form.year.focus();
    return false;
  }
  if(!intRegex.test(year) || year < 2000) {
    alert("The year should be a number bigger than 2000.");
    form.year.focus();
    return false;
  }
  
  var month = form.month.value;
  if(month == "") {
    alert("Please fill in the month this event is happening.");
    form.month.focus();
    return false;
  }
  if(!intRegex.test(month) || month < 0 || month > 11) {
    alert("The month should be a number between 0 and 11");
    form.month.focus();
    return false;
  }
  
  var day = form.day.value;
  if(day == "") {
    alert("Please fill in the day this event is happening.");
    form.day.focus();
    return false;
  }
  if(!intRegex.test(day) || day < 1 || day > 31) {
    alert("The day should be a number between 1 and 31");
    form.day.focus();
    return false;
  }

  if(form.title.value == "") {
    alert("Please fill in the title of the event.");
    form.title.focus();
    return false;
  }
  // Send the form
  return true;
}

shown_elem = null;

function show_box(elemId) {
  console.log("Showing: " + elemId);
  if (shown_elem != null) {
    hide(shown_elem);
  }
  show(elemId);
  shown_elem = elemId;
}

function hide_box(elemId) {
  console.log("Hiding: " + elemId);
  hide(elemId);
  shown_elem = null;
}

function show(elemId) {
  var elem = document.getElementById(elemId);
  elem.style.display = 'inline';
}

function hide(elemId) {
  var elem = document.getElementById(elemId);
  elem.style.display = 'none';
}
</script>
</head>
<body>
<%
List<Event> events;
Calendar selected_month = Calendar.getInstance();
if (request.getParameter("eyear") != null) {
  int year = Integer.parseInt(request.getParameter("eyear")); 
  int month = Integer.parseInt(request.getParameter("emonth"));
  events = Event.GetEventListForMonth(year, month);
  selected_month.set(year, month, 1);
} else {
  events = Event.GetAllEvents();
  selected_month = null;
}
if (request.getParameter("new") != null) {
  // An event is to be submitted.
  boolean isNew = Boolean.parseBoolean(request.getParameter("new"));
  Calendar c = Calendar.getInstance();
  c.set(
      Integer.parseInt(request.getParameter("year")), 
      Integer.parseInt(request.getParameter("month")),
      Integer.parseInt(request.getParameter("day")));
  
  Event event = null;
  if (isNew) {
    event = new Event(c.getTime(), new Translation("de",
                                                   request.getParameter("title"),
                                                   request.getParameter("desc"),
                                                   request.getParameter("loc"),
                                                   request.getParameter("url")));
  } else {
    long key = Long.parseLong(request.getParameter("key")); 
    for (Event e : events) {
      if (e.getKey() == key) {
        event = e;
      }
    }
    event.setDate(c.getTime());
    Translation de = event.getGermanTranslation();
    de.setTitle(request.getParameter("title"));
    de.setDesc(request.getParameter("desc"));
    de.setLocation(request.getParameter("loc"));
    de.setUrl(request.getParameter("url"));
  }
  
  if (!event.isOk()) {
    out.println("<div class='msg-red'><p>The event is not valid. Please verify your input.</p></div>");
  } else if (!event.addToStore()){
    out.println("<div class='msg-red'><p>A problem occurred when trying to store the new event. Try later?</p></div>");
  } else {
    out.println("<div class='msg-green'><p>Event correctly stored.</p></div>");
    // Add the event such that the list remains sorted.
    if (isNew)
      events.add(event);
  }
}

out.println(createSelectForm(selected_month));
for (Event e : events) {
  out.println(createEventForm("form-" + e.getKey(), e, selected_month));
}
out.println(createEventForm("newEvent", null, selected_month));
out.println(createXMLForm());

if (events.isEmpty()) {
  out.println("No event is yet defined.");
} else {
%>
  <div>
    <div class="title">Defined events:</div>
<% for (Event e : events) {
     out.println(createEventDiv(e)); 
   } }%>
<div id="new-event-link"><a onclick="show_box('event-new');" href="javascript:void(0);">Add a new event</a></div>
<div><a onclick="show_box('event-xml');" href="javascript:void(0);">Import an event</a></div>
</body>
</html>