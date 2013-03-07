<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="ch.aoz.maps.Language" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%!
public static String createLanguageForm(String formName, Language l) {
  StringBuilder form = new StringBuilder();
  if (l == null) {
    form.append("<div id='lang-new' class='langdiv'>");
    form.append("<div class='title'>Add a new language ");
    form.append("<a onclick=\"hide_box('lang-new'); show('new-lang-link')\" href='javascript:void(0);''>(hide)</a></div>");
  } else {
    form.append("<div id='lang-" + l.getCode() + "' class='langdiv'>");
    form.append("<div class='title'>Update " + l.getGermanName() + " ");
    form.append("<a onclick=\"hide_box('lang-" + l.getCode() + "')\" href='javascript:void(0);''>(hide)</a></div>");
  }
  form.append("<form name='" + formName + "' method='POST' target='content-frame'");
  form.append(" action='' onSubmit=\"return validateForm('" + formName + "')\">");
  if (l == null) {
    form.append("<p>Language code: <input type='text' name='code' maxlength='2'></p>");
    form.append("<input type='hidden' name='new' value='true'></p>");    
  } else {
    form.append("<input type='hidden' name='code' value='" + l.getCode() + "'></p>");    
    form.append("<input type='hidden' name='new' value='false'></p>");    
  }
  form.append("<p>Original name: <input type='text' name='name' value='" + (l == null ? "" : l.getName()) + "'></p>");
  form.append("<p>German name: <input type='text' name='gname' value='" + (l == null ? "" : l.getGermanName()) + "'></p>");
  form.append("<p><input type='checkbox' name='rtl' value='true'" + (l != null && l.isRightToLeft()? " checked" : "") + ">Is right to left?</p>");
  form.append("<p><input type='checkbox' name='isIn' value='true'" + (l != null && l.isInAgenda()? " checked" : "") + ">Is in the Agenda?</p>");
  form.append("<p><input type='checkbox' name='format' value='true'" + (l != null && l.hasSpecificFormat()? " checked" : "") + ">Requires specific formatting in the agenda?</p>");
  form.append("<p>Abbreviations for the days of the week</p>");
  form.append("<table><tr><td>Sonntag</td><td>Montag</td><td>Dienstag</td>");
  form.append("<td>Mittwoch</td><td>Donnerstag</td><td>Freitag</td><td>Samstag</td>");
  form.append("</tr><tr><td><input type='text' name='sun' value='" + (l == null ? "" : l.getDayOfTheWeek(0)) + "'></td>");
  form.append("<td><input type='text' name='mon' value='" + (l == null ? "" : l.getDayOfTheWeek(1)) + "'></td><td><input type='text' name='tue' value='" + (l == null ? "" : l.getDayOfTheWeek(2)) + "'></td>");
  form.append("<td><input type='text' name='wed' value='" + (l == null ? "" : l.getDayOfTheWeek(3)) + "'></td><td><input type='text' name='thu' value='" + (l == null ? "" : l.getDayOfTheWeek(4)) + "'></td>");
  form.append("<td><input type='text' name='fri' value='" + (l == null ? "" : l.getDayOfTheWeek(5)) + "'></td><td><input type='text' name='sat' value='" + (l == null ? "" : l.getDayOfTheWeek(6)) + "'></td>");
  form.append("</tr></table>");
  form.append("<p><input type='submit' value='" + (l == null ? "Add": "Update") + " language'></p>");
  form.append("</form></div>");
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
#new-lang-link {
  margin-bottom: 10px;
}
.langdiv {
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
<script>
function validateForm(formName) {
  var form = document.forms[formName];
  if(form.code.value.length != 2) {
    alert("Please fill in the language code.");
    form.code.focus();
    return false;
  }
  if(form.name.value == "") {
    alert("Please fill in the original name.");
    form.name.focus();
    return false;
  }
  if(form.gname.value == "") {
    alert("Please fill in the german name.");
    form.gname.focus();
    return false;
  }
  if(form.sun.value == "") {
    alert("Please fill in the abbreviation for Sunday.");
    form.sun.focus();
    return false;
  }
  if(form.mon.value == "") {
    alert("Please fill in the abbreviation for Monday.");
    form.mon.focus();
    return false;
  }
  if(form.tue.value == "") {
    alert("Please fill in the abbreviation for Tuesday.");
    form.tue.focus();
    return false;
  }
  if(form.wed.value == "") {
    alert("Please fill in the abbreviation for Wednesday.");
    form.wed.focus();
    return false;
  }
  if(form.thu.value == "") {
    alert("Please fill in the abbreviation for Thursday.");
    form.thu.focus();
    return false;
  }
  if(form.fri.value == "") {
    alert("Please fill in the abbreviation for Friday.");
    form.fri.focus();
    return false;
  }
  if(form.sat.value == "") {
    alert("Please fill in the abbreviation for Saturday.");
    form.sat.focus();
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
Map<String, Language> languages = Language.getAllLanguages();
if (request.getParameter("code") != null) {
  // A new language is to be submitted.
  List<String> abbrevs = new ArrayList<String>();
  abbrevs.add(request.getParameter("sun"));
  abbrevs.add(request.getParameter("mon"));
  abbrevs.add(request.getParameter("tue"));
  abbrevs.add(request.getParameter("wed"));
  abbrevs.add(request.getParameter("thu"));
  abbrevs.add(request.getParameter("fri"));
  abbrevs.add(request.getParameter("sat"));
  Language lang = new Language(
      request.getParameter("code"), 
      request.getParameter("name"), 
      request.getParameter("gname"),
      abbrevs,
      request.getParameter("rtl") != null, 
      request.getParameter("isIn") != null, 
      request.getParameter("format") != null);
  boolean isNew = Boolean.parseBoolean(request.getParameter("new"));
  if (isNew && languages.containsKey(lang.getCode())) {
    out.println("<div class='msg-red'><p>Language " + lang.getGermanName() 
              + " is already defined.</p></div>");
  } else if (!lang.isOk()) {
    out.println("<div class='msg-red'><p>The language is not valid. Please verify your input.</p></div>");
  } else if (!Language.AddLanguage(lang)){
    out.println("<div class='msg-red'><p>A problem occurred when trying to store the new language. Try later?</p></div>");
  } else {
    out.println("<div class='msg-green'><p>Language correctly stored.</p></div>");
    languages.put(lang.getCode(), lang);
  }
}

for (Language l : languages.values()) {
  out.println(createLanguageForm("form-" + l.getCode(), l));
}
out.println(createLanguageForm("newLang", null));

if (languages.isEmpty()) {
  out.println("No language is yet defined.");
} else {
%>
  <div>
    <div class="title">Defined languages:</div>
    <table>
      <tr>
        <th>Code</th>
        <th>Name</th>
        <th>German name</th>
        <th>Su</th>
        <th>Mo</th>
        <th>Tu</th>
        <th>We</th>
        <th>Th</th>
        <th>Fr</th>
        <th>Sa</th>
        <th>RTL?</th>
        <th>In Agenda?</th>
        <th>Spec format?</th>
        <th></th>
      </tr>
<% for (Language l : languages.values()) { %> 
      <tr>
        <td><% out.println(l.getCode()); %></td>
        <td><% out.println(l.getName()); %></td>
        <td><% out.println(l.getGermanName()); %></td>
        <td><% out.println(l.getDayOfTheWeek(0)); %></td>
        <td><% out.println(l.getDayOfTheWeek(1)); %></td>
        <td><% out.println(l.getDayOfTheWeek(2)); %></td>
        <td><% out.println(l.getDayOfTheWeek(3)); %></td>
        <td><% out.println(l.getDayOfTheWeek(4)); %></td>
        <td><% out.println(l.getDayOfTheWeek(5)); %></td>
        <td><% out.println(l.getDayOfTheWeek(6)); %></td>
        <td><% out.println(l.isRightToLeft() ? "&#10003;" : "&#10007;"); %></td>
        <td><% out.println(l.isInAgenda()  ? "&#10003;" : "&#10007;"); %></td>
        <td><% out.println(l.hasSpecificFormat() ? "&#10003;" : "&#10007;"); %></td>
        <td><a id="lang-<% out.print(l.getCode()); %>-link" onclick="show_box('lang-<% out.print(l.getCode()); %>')" href="javascript:void(0);">edit</a></td>
      </tr>
<% } %>
    </table>
  </div>
  <div id="new-lang-link"><a onclick="show_box('lang-new');" href="javascript:void(0);">Add a new language</a></div>
<% } %>
</body>
</html>