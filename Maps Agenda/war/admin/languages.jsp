<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="ch.aoz.maps.Language" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<html>
<head>
<style type="text/css">
#new-lang {
  margin-top: 30px;
}
#new-lang p {
  padding: 0px;
  margin: 2px;
}
.msg-red {
  background-color: #a00;
  border-radius: 5px;
  height: 50px;
  padding: 5px;
  margin-bottom: 20px;
}
.msg-green {
  background-color: #0a0;
  border-radius: 5px;
  height: 50px;
  padding: 5px;
  margin-bottom: 20px;
}
.title {
  font-size: x-large;
  background-color: #ba7;
  margin-bottom: 10px;
}
table {
  border-collapse: collapse;
  border: 1px solid black;
}
th, td {
  border: 1px solid black;
  padding: 5px;
}
</style>
<script>
function validateForm() {
    if(document.newLang.code.value.length != 2) {
      alert("Please fill in the language code.");
      document.newLang.code.focus();
      return false;
    }
    if(document.newLang.name.value == "") {
      alert("Please fill in the original name.");
      document.newLang.name.focus();
      return false;
    }
    if(document.newLang.gname.value == "") {
      alert("Please fill in the german name.");
      document.newLang.gname.focus();
      return false;
    }
    if(document.newLang.sun.value == "") {
      alert("Please fill in the abbreviation for Sunday.");
      document.newLang.sun.focus();
      return false;
    }
    if(document.newLang.mon.value == "") {
      alert("Please fill in the abbreviation for Monday.");
      document.newLang.mon.focus();
      return false;
    }
    if(document.newLang.tue.value == "") {
      alert("Please fill in the abbreviation for Tuesday.");
      document.newLang.tue.focus();
      return false;
    }
    if(document.newLang.wed.value == "") {
      alert("Please fill in the abbreviation for Wednesday.");
      document.newLang.wed.focus();
      return false;
    }
    if(document.newLang.thu.value == "") {
      alert("Please fill in the abbreviation for Thursday.");
      document.newLang.thu.focus();
      return false;
    }
    if(document.newLang.fri.value == "") {
      alert("Please fill in the abbreviation for Friday.");
      document.newLang.fri.focus();
      return false;
    }
    if(document.newLang.sat.value == "") {
      alert("Please fill in the abbreviation for Saturday.");
      document.newLang.sat.focus();
      return false;
    }
    // Send the form
    return true;
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
  if (languages.containsKey(lang.getCode())) {
    out.println("<div class='msg-red'><p>There already exists a language with the code '" 
                + lang.getCode() + "'</p></div>");
  } else if (!lang.isOk()) {
    out.println("<div class='msg-red'><p>The language is not valid. Please verify your input.</p></div>");
  } else if (!Language.AddLanguage(lang)){
    out.println("<div class='msg-red'><p>A problem occurred when trying to store the new language. Try later?</p></div>");
  } else {
    out.println("<div class='msg-green'><p>New language correctly stored.</p></div>");
    languages.put(lang.getCode(), lang);
  }
}

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
      </tr>
<% } %>
    </table>
  </div>
<%
}
%>
  <div id="new-lang">
    <div class="title">Add a new language</div>
    <form name="newLang" 
          method="POST" 
          action="" 
          onSubmit="return validateForm()"
          target="content-frame">
      <p>Language code: <input type="text" name="code" maxlength="2"></p>
      <p>Original name: <input type="text" name="name"></p>
      <p>German name: <input type="text" name="gname"></p>
      <p><input type="checkbox" name="rtl" value="true">Is right to left?</p>
      <p><input type="checkbox" name="isIn" value="true">Is in the Agenda?</p>
      <p><input type="checkbox" name="format" value="true">Requires specific formatting in the agenda?</p>
      <p>Abbreviations for the days of the week</p>
      <table>
        <tr>
          <td>Sunday</td>
          <td>Monday</td>
          <td>Tuesday</td>
          <td>Wednesday</td>
          <td>Thursday</td>
          <td>Friday</td>
          <td>Saturday</td>
        </tr>
        <tr>
          <td><input type="text" name="sun"></td>
          <td><input type="text" name="mon"></td>
          <td><input type="text" name="tue"></td>
          <td><input type="text" name="wed"></td>
          <td><input type="text" name="thu"></td>
          <td><input type="text" name="fri"></td>
          <td><input type="text" name="sat"></td>
        </tr>
      </table>    
      <p><input type="submit" value="Add language"></p>
    </form>
  </div>
</body>
</html>