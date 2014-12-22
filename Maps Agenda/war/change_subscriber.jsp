<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Collection" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="ch.aoz.maps.Language" %>
<%@ page import="ch.aoz.maps.Subscriber" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%!
public static String createLanguageForm(String formName, String hash, String current_language, Collection<Language> languages) {
	StringBuilder form = new StringBuilder();
	form.append("<div><div class='title'>Please choose your preferred language of correspondence:</div>");
	form.append("<form><table>");
	form.append("<tr><th></th><th>Code</th><th>Name</th><th>German name</th></tr>");
	for (Language language : languages) {
		form.append("<tr><td><input type='radio' name='language' value='" + language.getCode() + 
			(language.getCode().equals(current_language) ? "' checked>" : "'>"));
		form.append("<td>" + language.getCode() + "</td>");
		form.append("<td>" + language.getName() + "</td>");
		form.append("<td>" + language.getGermanName() + "</td>");
		form.append("</tr>");
	}
	form.append("<input type='hidden' name='hash' value='"+hash+"'>");
	form.append("</table>");
	form.append("<p><input type='submit' value='Set language'></p></form></div>");
	
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
</script>
</head>
<body>
<%
Subscriber currentSubscriber = null;
Collection<Language> languages;
Boolean sane = Boolean.TRUE;

// Basic sanitation
if (request.getParameter("hash") == null)
	sane = Boolean.FALSE;

if (sane) {
	currentSubscriber = Subscriber.getSubscriberByHash(request.getParameter("hash"));
	if (currentSubscriber == null) {
		sane = Boolean.FALSE;
		out.println("Could not identify any subscriber with that hash. Do you have the correct link ?");		
	}
}

languages = Language.getAllLanguages();
if (languages == null) {
	sane = Boolean.FALSE;
}

if (sane && request.getParameter("language") != null ) {
	Language l = Language.GetByCode(request.getParameter("language"));
	currentSubscriber.setLanguage(l.getCode());
	Subscriber.AddSubscriber(currentSubscriber);
	out.println("<div><div class='title'>Thank you ! Your language has been set to " + l.getName() + "</div>");
} else if (sane) {
	out.println(createLanguageForm("foo", request.getParameter("hash"), currentSubscriber.getLanguage(), languages));
}

%>

</body>
</html>