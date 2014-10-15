<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="ch.aoz.maps.Language" %>
<%@ page import="ch.aoz.maps.Subscriber" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%!
public static String createSubscriberForm(String formName, Subscriber t, Map<String, Language> languages) {
  StringBuilder form = new StringBuilder();
  if (t == null) {
    form.append("<div id='t-new' class='tdiv'>");
    form.append("<div class='title'>Add a new subscriber ");
    form.append("<a onclick=\"hide('t-new'); show('new-t-link')\" href='javascript:void(0);''>(hide)</a></div>");
  } else {
    form.append("<div id='t-" + t.getEmail() + "' class='tdiv'>");
    form.append("<div class='title'>Update " + t.getEmail() + " ");
    form.append("<a onclick=\"hide('t-" + t.getEmail() + "')\" href='javascript:void(0);''>(hide)</a></div>");
  }
  form.append("<form name='" + formName + "' method='POST' target='content-frame'");
  form.append(" action='' onSubmit=\"return validateForm('" + formName + "')\">");
  if (t == null) {
    form.append("<p>Email: <input type='text' name='email'></p>");
    form.append("<input type='hidden' name='new' value='true'></p>");    
  } else {
    form.append("<input type='hidden' name='email' value='" + t.getEmail() + "'></p>");    
    form.append("<input type='hidden' name='new' value='false'></p>");    
  }
  form.append("<p>Name: <input type='text' name='name' value='" + (t == null ? "" : t.getName()) + "'></p>");

  form.append("<p>Language: <select name='language'>)");
  for (Language l : languages.values()) {
    boolean isSelected = (t != null && t.getLanguage().equals(l.getCode())); 
    form.append("<option value='" + l.getCode() + (isSelected ? "' selected>" : "'>") + l.getGermanName() + "</option>");
  }
  form.append("</select></p><p><input type='submit' value='" + (t == null ? "Add": "Update") + " subscriber'></p>");
  form.append("</form></div>");
  return form.toString();
}

public static String getLanguageName(Subscriber t, Map<String, Language> languages) {
  if (t == null || languages == null) {
    return "";
  }
  Language l = languages.get(t.getLanguage());  
  if (l == null) {
    return t.getLanguage();
  }
  return l.getGermanName();
}
%>

<html>
<head>
<style type="text/css">
form p {
  padding: 0px;
  margin: 2px;
}
.tdiv {
  margin-top: 30px;
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
  margin-bottom: 20px;
}
th, td {
  border: 1px solid black;
  padding: 5px;
}
</style>
<script>
function validateEmail(email) { 
    var re = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    return re.test(email);
} 

function validateForm(formName) {
  var form = document.forms[formName];
  if(form.email.value == "") {
    alert("Please fill in the email of the subscriber.");
    form.email.focus();
    return false;
  }
  if(!validateEmail(form.email.value)) {
    alert("The provided email address is not valid. Please fix.");
    form.email.focus();
    return false;
  }
  if(form.language.value == "") {
    alert("Please fill in the language of the subscriber.");
    form.language.focus();
    return false;
  }
  // Check if the languages are correctly specified.
  if(form.language.value.length != 2) {
      alert("Each language code should be two characters long. " + form.language.value.length);
      form.language.focus();
      return false;
  }
  // Send the form
  return true;
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
<body>
<%
Map<String, Subscriber> subscribers = Subscriber.getAllSubscribers();
Map<String, Language> languages = Language.getAllLanguages();
if (request.getParameter("email") != null) {
  // A new language is to be submitted.
  Subscriber t = new Subscriber(
      request.getParameter("email"), 
      request.getParameter("name"), 
      request.getParameter("language"));
  boolean isNew = Boolean.parseBoolean(request.getParameter("new"));
  // TODO: check that all the language codes actually exist.
  if (isNew && subscribers.containsKey(t.getEmail())) {
    out.println("<div class='msg-red'><p>Subscriber " + t.getEmail() 
              + " is already in the list.</p></div>");
  } else if (!t.isOk()) {
    out.println("<div class='msg-red'><p>This subscriber entry is not valid. Please verify your input.</p></div>");
  } else if (!Subscriber.AddSubscriber(t)){
    out.println("<div class='msg-red'><p>A problem occurred when trying to store the new subscriber. Try later?</p></div>");
  } else {
    out.println("<div class='msg-green'><p>Subscriber correctly stored.</p></div>");
    subscribers.put(t.getEmail(), t);
  }
}

if (subscribers.isEmpty()) {
  out.println("<div>No subscribers are defined yet.</div>");
} else {
  %>
  <div>
    <div class="title">List of subscribers:</div>
    <table>
      <tr>
        <th>Email address</th>
        <th>Name</th>
        <th>Language</th>
        <th>Hash</th>
        <th></th>
      </tr>
<% for (Subscriber t : subscribers.values()) { %> 
      <tr>
        <td><% out.println(t.getEmail()); %></td>
        <td><% out.println(t.getName()); %></td>
        <td><% out.println(getLanguageName(t, languages)); %></td>
        <td><% out.println(t.getHash()); %></td>
        <td><a id="t-<% out.print(t.getEmail()); %>-link" onclick="show('t-<% out.print(t.getEmail()); %>')" href="javascript:void(0);">edit</a></td>
      </tr>
<% } %>
    </table>
  </div>
<%
} 
for (Subscriber t : subscribers.values()) {
  out.println(createSubscriberForm("form-" + t.getEmail(), t, languages));
}
out.println(createSubscriberForm("newSubscriber", null, languages));
%>
<div id="new-t-link"><a onclick="show('t-new'); hide('new-t-link');" href="javascript:void(0);">Add a new subscriber</a></div>
</body>
</html>
