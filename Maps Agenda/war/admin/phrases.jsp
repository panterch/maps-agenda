<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="ch.aoz.maps.Language" %>
<%@ page import="ch.aoz.maps.Phrase" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%!
public static String createLanguageForm(Map<String, Language> languages, String selected) {
  StringBuilder form = new StringBuilder();
  form.append("<div id='lang-form'>");
  form.append("<div class='title'>Select language to display</div>");
  form.append("<form name='lang' method='GET' target='content-frame'>");
  form.append("<p><select name='lang'>");
  for (Language l : languages.values()) {
    if (l.getCode() == selected) {
      form.append(l.getCode());
    }
    form.append("<option value='" + l.getCode() + (l.getCode().equals(selected) ? "' selected>" : "'>") + l.getGermanName() + "</option>");
  }
  form.append("</select></p><p><input type='submit' value='Show'></p></form>");
  return form.toString();
}

public static Map<String, Phrase> getPhrasesForLang(String lang) {
  List<Phrase> phrases = Phrase.GetPhrasesForLanguage("de");
  Map<String, Phrase> phrasesMap = new HashMap<String, Phrase>();
  for (Phrase p : phrases) {
    phrasesMap.put(p.getLang(), p);
  }
  return phrasesMap;
}

public static String createPhraseForm(String formName, Language lang, Phrase p_de, Phrase p_ln) {
  StringBuilder form = new StringBuilder();
  if (p_de == null) {
    form.append("<div id='p-new' class='pdiv'>");
    form.append("<div class='title'>Add a new translation ");
    form.append("<a onclick=\"hide_box('p-new'); show('new-p-link')\" href='javascript:void(0);''>(hide)</a></div>");
  } else {
    form.append("<div id='p-" + p_de.getKey() + "' class='pdiv'>");
    form.append("<div class='title'>Update " + p_de.getKey() + " ");
    form.append("<a onclick=\"hide_box('p-" + p_de.getKey() + "')\" href='javascript:void(0);''>(hide)</a></div>");
  }
  form.append("<form name='" + formName + "' method='POST' target='content-frame'");
  form.append(" action='' onSubmit=\"return validateForm('" + formName + "')\">");
  if (p_de == null) {
    form.append("<p>Translation key: <input type='text' name='key'></p>");
    form.append("<input type='hidden' name='new' value='true'></p>");    
  } else {
    form.append("<p>Key: " + p_de.getKey() + "<input type='hidden' name='key' value='" + p_de.getKey() + "'></p>");    
    form.append("<input type='hidden' name='new' value='false'></p>");    
  }
  form.append("<p>de: <input type='text' name='p_de' value='" + (p_de == null ? "" : p_de.getPhrase()) + "'></p>");
  if (p_ln != null && p_ln.getLang() != p_de.getLang()) {
    form.append("<p>" + lang.getCode() + ": <input type='text' name='p_ln' value='" + p_ln.getPhrase() + "'></p>");
  }
  form.append("<p><input type='checkbox' name='tag' value='true'" + (p_de != null && p_de.isTag()? " checked" : "") + ">Is Tag?</p>");
  form.append("<input type='hidden' name='lang' value='" + lang.getCode() + "'></p>");    
  form.append("<p><input type='submit' value='" + (p_de == null ? "Add": "Update") + " translation'></p>");
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
<script>
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
Map<String, Language> languages = Language.getAllLanguages();
Map<String, Phrase> phrasesDE = getPhrasesForLang("de");
Map<String, Phrase> phrasesOther;
String lang = request.getParameter("lang");
if (lang == null) {
  phrasesOther = phrasesDE;
  lang = "de";
} else {
  phrasesOther = getPhrasesForLang(lang);
}

out.println(createLanguageForm(languages, lang));
out.println(createPhraseForm("newPhrase", languages.get(lang), null, null));

if (phrasesDE.isEmpty()) {
  out.println("No phrase is yet defined.");
} else {
%>
  <div>
    <div class="title">Defined phrases:</div>
    <table>
      <tr>
        <th>Key</th>
        <th><% languages.get("de").getGermanName(); %></th>
        <th><% languages.get(lang).getGermanName(); %></th>
        <th>is tag?</th>
        <th></th>
      </tr>
<% for (Phrase p : phrasesDE.values()) {
     Phrase p2 = phrasesOther.get(p.getKey()); 
     out.println(createPhraseForm("form-" + p.getKey(), languages.get(lang), p, p2));
%>
      <tr>
        <td><% out.println(p.getKey()); %></td>
        <td><% out.println(p.getPhrase()); %></td>
        <td><% out.println(p2 == null ? "" : p2.getPhrase()); %></td>
        <td><% out.println(p.isTag() ? "&#10003;" : "&#10007;"); %></td>
      </tr>
<% } }%>
<div id="new-p-link"><a onclick="show_box('p-new');" href="javascript:void(0);">Add a new phrase</a></div>
</body>
</html>