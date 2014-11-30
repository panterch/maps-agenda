<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.TreeSet" %>
<%@ page import="ch.aoz.maps.Language" %>
<%@ page import="ch.aoz.maps.Phrase" %>
<%@ page import="ch.aoz.maps.Phrases" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%!
public static String createLanguageForm(Map<String, Language> languages, String selected) {
  StringBuilder form = new StringBuilder();
  form.append("<div id='lang-form'>");
  form.append("<div class='title'>Select language to display</div>");
  form.append("<form name='lang' method='GET' target='content-frame'>");
  form.append("<p><select name='lang'>");
  if (languages != null) {
    for (Language l : languages.values()) {
      if (l.getCode() == selected) {
        form.append(l.getCode());
      }
      form.append("<option value='" + l.getCode() + (l.getCode().equals(selected) ? "' selected>" : "'>") + l.getGermanName() + "</option>");
    }
  }
  form.append("</select></p><p><input type='submit' value='Show'></p></form>");
  return form.toString();
}

public static Map<String, Phrase> getPhrasesForLang(String lang) {
  Phrases phrases = Phrases.GetPhrasesForLanguage(lang);
  Map<String, Phrase> phrasesMap = new HashMap<String, Phrase>();
  if (phrases != null) {
    for (Phrase p : phrases.getPhrases()) {
      phrasesMap.put(p.getKey(), p);
    }
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
  form.append("<p>Group: <input type='text' name='p_group' value='" + (p_de == null ? "" : p_de.getGroup()) + "'></p>");
  form.append("<p>Deutsch: <input type='text' name='p_de' value='" + (p_de == null ? "" : p_de.getPhrase()) + "'></p>");
  if (p_ln != null && p_ln.getLang() != p_de.getLang()) {
    form.append("<p>" + lang.getGermanName() + ": <input type='text' name='p_ln' value='" + p_ln.getPhrase() + "'></p>");
  } else if (!lang.getCode().equals("de")) {
    form.append("<p>" + lang.getGermanName() + ": <input type='text' name='p_ln' value=''></p>");
  }
  form.append("<p><input type='checkbox' name='tag' value='true'" + (p_de != null && p_de.isTag()? " checked" : "") + ">Is Tag?</p>");
  form.append("<input type='hidden' name='lang' value='" + lang.getCode() + "'></p>");    
  form.append("<p><input type='submit' value='" + (p_de == null ? "Add": "Update") + " translation'></p>");
  form.append("</form></div>");
  return form.toString();
}

public static String deletePhraseForm(String key) {
  StringBuilder form = new StringBuilder();
  form.append("<form name='d-" + key + "' method='POST' target='content-frame'");
  form.append(" action='' onSubmit=\"return confirm('Are you sure you want to delete " + key + "?')\">");
  form.append("<input type='hidden' name='delete' value='true'>");    
  form.append("<input type='hidden' name='key' value='" + key + "'>");    
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
  var keyRegex = /^[0-9a-zA-Z]+$/;

  var form = document.forms[formName];
  var lang = form.lang.value;
  
  var key = form.key.value;
  if(key == "") {
    alert("Please fill in the key for this translation.");
    form.key.focus();
    return false;
  }
  if(!keyRegex.test(key)) {
    alert("The key should be a string containing only letters or numbers.");
    form.key.focus();
    return false;
  }
  
  var p_de = form.p_de.value;
  if(p_de == "") {
    alert("Please fill in the German version of the phrase.");
    form.p_de.focus();
    return false;
  }

  if (lang != "de") {  
	var p_ln = form.p_ln.value;
	if(p_ln == "") {
	  alert("Please fill in the translated version of the phrase.");
	  form.p_ln.focus();
	  return false;
	}
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
if (request.getParameter("delete") != null) {
  String key = request.getParameter("key");
  if (Phrases.deleteKey(key)) {
    out.println("<div class='msg-green'><p>Deleted " + key + "</p></div>");
  } else {
    out.println("<div class='msg-red'><p>Encountered some problems when deleting " + key + "</p></div>");
  }
}
Map<String, Language> languages = Language.getAllLanguages();
if (languages == null || languages.isEmpty()) {
  out.println("Please add a language first.");
  return;
}
Map<String, Phrase> phrasesDE = getPhrasesForLang("de");
Map<String, Phrase> phrasesOther;
String lang = request.getParameter("lang");
if (lang == null) {
  phrasesOther = phrasesDE;
  lang = "de";
} else {
  phrasesOther = getPhrasesForLang(lang);
}

if (request.getParameter("new") != null) {
  boolean isNew = Boolean.parseBoolean(request.getParameter("new"));
  String key = request.getParameter("key");
  
  // First handle the German phrase.  
  Phrase p_de;
  boolean update_de = false;
  if (isNew) {
    if (phrasesDE.containsKey(key)) {
      out.println("<div class='msg-red'><p>The key already exists. Please choose another key.</p></div>");
      p_de = null;
    } else {
      p_de = new Phrase(key, "de",
                        request.getParameter("p_de"),
                        request.getParameter("p_group"),
                        Boolean.parseBoolean(request.getParameter("tag")));
      update_de = true;
    }
  } else if (!phrasesDE.containsKey(key)) {
    out.println("<div class='msg-red'><p>Could not find the German phrase to update. Key " + key + " is missing.</p></div>");
    p_de = null;
  } else {
    p_de = phrasesDE.get(key);
    boolean new_tag = Boolean.parseBoolean(request.getParameter("tag"));
    String new_phrase = request.getParameter("p_de");
    String new_group = request.getParameter("p_group");
    if (!new_phrase.equals(p_de.getPhrase()) 
        || !new_group.equals(p_de.getGroup()) 
        || new_tag != p_de.isTag()) {
      p_de.setPhrase(new_phrase);
      p_de.setGroup(new_group);
      p_de.setTag(new_tag);
      update_de = true;
    }
  }
  if (p_de != null && update_de) {
    if (p_de.addToStore()) {
      out.println("<div class='msg-green'><p>German phrase correctly stored.</p></div>");
      phrasesDE.put(key, p_de);
    } else {
      out.println("<div class='msg-red'><p>An error occurred when trying to store the German phrase. Try again later?</p></div>");
      p_de = null;
    }
  }
  // Now check if there is a second language. Also skip this part of p_de is null, i.e. an error occurred.
  if (!lang.equals("de") && p_de != null) {
    Phrase p_ln;
    boolean update_ln = false;
    if (isNew || !phrasesOther.containsKey(key)) {
      // We have a new translated phrase.
      p_ln = new Phrase(key, lang,
                        request.getParameter("p_ln"),
                        request.getParameter("p_group"),
                        Boolean.parseBoolean(request.getParameter("tag")));
      update_ln = true;
    } else {
      p_ln = phrasesOther.get(key);
      boolean new_tag = Boolean.parseBoolean(request.getParameter("tag"));
      String new_group = request.getParameter("p_group");
      String new_phrase = request.getParameter("p_ln");
      if (!new_phrase.equals(p_ln.getPhrase()) 
          || !new_group.equals(p_ln.getGroup())
          || new_tag != p_ln.isTag()) {
        p_ln.setPhrase(new_phrase);
        p_ln.setGroup(new_group);
        p_ln.setTag(new_tag);
        update_ln = true;
      }
    }
    if (update_ln) {
      if (p_ln.addToStore()) {
        out.println("<div class='msg-green'><p>Translated phrase correctly stored.</p></div>");
        phrasesOther.put(key, p_ln);
      } else {
        out.println("<div class='msg-red'><p>An error occurred when trying to store the translated phrase. Try again later?</p></div>");
      }
    }
  }
}

out.println(createLanguageForm(languages, lang));
out.println(createPhraseForm("newPhrase", languages.get(lang), null, null));

if (phrasesDE != null || phrasesDE.isEmpty()) {
  out.println("No phrase is yet defined.");
} else {
%>
  <div>
    <div class="title">Defined phrases:</div>
    <table>
      <tr>
        <th>Key</th>
        <th><% out.print(languages.get("de").getGermanName()); %></th>
        <% if (!lang.equals("de")) { %>
          <th><% out.print(languages.get(lang).getGermanName()); %></th>
        <% } %>
        <th>is tag?</th>
        <th>Edit</th>
        <th>Delete</th>
      </tr>
<% String currentGroup = null;
   TreeSet<Phrase> set = new TreeSet<Phrase>();
   for (Phrase p : phrasesDE.values()) {
     set.add(p);
   }
   for (Phrase p : set) {
     if (currentGroup == null || !currentGroup.equals(p.getGroup().toLowerCase())) {
       currentGroup = p.getGroup().toLowerCase();
       if (currentGroup.equals("")) {
         out.println("<tr><td colspan='6' bgcolor='#0F0'>(unlisted)</td></tr>");
       } else {
         out.println("<tr><td colspan='6' bgcolor='#0F0'>" + currentGroup + "</td></tr>");
       }
     }
     Phrase p2 = phrasesOther.get(p.getKey()); 
     out.println(createPhraseForm("form-" + p.getKey(), languages.get(lang), p, p2));
%>
      <tr>
        <td><% out.println(p.getKey()); %></td>
        <td><% out.println(p.getPhrase()); %></td>
        <% if (!lang.equals("de")) { %>
          <td><% out.println(p2 == null ? "" : p2.getPhrase()); %></td>
        <% } %>
        <td><% out.println(p.isTag() ? "&#10003;" : "&#10007;"); %></td>
        <td><a onclick="show_box('p-<% out.print(p.getKey()); %>');" href='javascript:void(0);'>Edit</a></td>
        <td><% out.println(deletePhraseForm(p.getKey())); %> </td>
      </tr>
<% } } %>
<div id="new-p-link"><a onclick="show_box('p-new');" href="javascript:void(0);">Add a new phrase</a></div>
</body>
</html>