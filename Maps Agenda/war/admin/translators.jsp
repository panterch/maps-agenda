<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.TreeMap" %>
<%@ page import="ch.aoz.maps.Translator" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%!
public static String createTranslatorForm(String formName, Translator t) {
  StringBuilder form = new StringBuilder();
  if (t == null) {
    form.append("<div id='t-new' class='tdiv'>");
    form.append("<div class='title'>Add a new translator ");
    form.append("<a onclick=\"hide('t-new'); show('new-t-link')\" href='javascript:void(0);''>(hide)</a></div>");
  } else {
    form.append("<div id='t-" + t.getEmail() + "' class='tdiv'>");
    form.append("<div class='title'>Update " + t.getName() + " ");
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
  form.append("<p>Translator name: <input type='text' name='name' value='" + (t == null ? "" : t.getName()) + "'></p>");
  form.append("<p>Languages handled: <input type='text' name='langs' value='" + (t == null ? "" : t.getLanguageString()) + "'></p>");
  form.append("<p><input type='submit' value='" + (t == null ? "Add": "Update") + " translator'></p>");
  form.append("</form></div>");
  return form.toString();
}

public static String deleteTranslatorForm(String email) {
  StringBuilder form = new StringBuilder();
  form.append("<form name='d-" + email + "' method='POST' target='content-frame'");
  form.append(" action='' onSubmit=\"return confirm('Are you sure you want to delete translator " + email + "?')\">");
  form.append("<input type='hidden' name='delete' value='true'>");    
  form.append("<input type='hidden' name='email' value='" + email + "'>");    
  form.append("<input type='submit' value='delete'>");
  form.append("</form>");
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
    alert("Please fill in the email of the translator.");
    form.email.focus();
    return false;
  }
  if(!validateEmail(form.email.value)) {
    alert("The provided email adresse is not valid. Please fix.");
    form.email.focus();
    return false;
  }
  if(form.name.value == "") {
    alert("Please fill in the name of the translator.");
    form.name.focus();
    return false;
  }
  if(form.langs.value == "") {
    alert("Please fill in the list of languages the translator can handle.");
    form.langs.focus();
    return false;
  }
  // Check if the languages are correctly specified.
  form.langs.value = form.langs.value.replace(/ /g,'');
  langs = form.langs.value.split(",");
  for (var langIndex in langs) {
    var lang = langs[langIndex];
    if (lang.length != 2) {
      alert("Each language code should be two characters long. " + lang);
      form.langs.focus();
      return false;
    }
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
if (Boolean.parseBoolean(request.getParameter("delete"))) {
  if (!Translator.delete(request.getParameter("email"))) {
    out.println("<div class='msg-red'><p>Failed to delete translator " 
                + request.getParameter("email") + ".</p></div>");
  }
}
Map<String, Translator> translators = Translator.getAllTranslators();
if (request.getParameter("email") != null) {
  // A new translator is to be submitted.
  Translator t = new Translator(
      request.getParameter("email"), 
      request.getParameter("name"), 
      Translator.parseLanguageString(request.getParameter("langs")));
  boolean isNew = Boolean.parseBoolean(request.getParameter("new"));
  // TODO: check that all the language codes actually exist.
  if (isNew && translators.containsKey(t.getEmail())) {
    out.println("<div class='msg-red'><p>Translator " + t.getName() 
              + " is already in the list.</p></div>");
  } else if (!t.isOk()) {
    out.println("<div class='msg-red'><p>This translator entry is not valid. Please verify your input.</p></div>");
  } else if (!Translator.AddTranslator(t)){
    out.println("<div class='msg-red'><p>A problem occurred when trying to store the new translator. Try later?</p></div>");
  } else {
    out.println("<div class='msg-green'><p>Translator correctly stored.</p></div>");
    translators.put(t.getEmail(), t);
  }
}

Map<String, Translator> sorted_translators = new TreeMap<String, Translator>();
for (Translator t : translators.values()) {
  out.println(createTranslatorForm("form-" + t.getEmail(), t));
  sorted_translators.put(t.getLanguageString() + t.getEmail().toLowerCase(), t);
}

if (translators.isEmpty()) {
  out.println("<div>No translator is yet defined.</div>");
} else {
  %>
  <div>
    <div class="title">List of translators:</div>
    <table>
      <tr>
        <th>Name</th>
        <th>Email</th>
        <th>Languages handled</th>
        <th></th>
        <th></th>
      </tr>
<% for (Translator t : sorted_translators.values()) { %> 
      <tr>
        <td><% out.println(t.getName()); %></td>
        <td><% out.println(t.getEmail()); %></td>
        <td><% out.println(t.getLanguageString()); %></td>
        <td><a id="t-<% out.print(t.getEmail()); %>-link" onclick="show('t-<% out.print(t.getEmail()); %>')" href="javascript:void(0);">edit</a></td>
        <td><% out.println(deleteTranslatorForm(t.getEmail())); %> </td>
      </tr>
<% } %>
    </table>
  </div>
<%
} 
out.println(createTranslatorForm("newTranslator", null));
%>
<div id="new-t-link"><a onclick="show('t-new'); hide('new-t-link');" href="javascript:void(0);">Add a new translator</a></div>
</body>
</html>