<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Collection" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="ch.aoz.maps.Language" %>
<%@ page import="ch.aoz.maps.Subscriber" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
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
Subscriber currentsubscriber = null;
Boolean sane = Boolean.TRUE;

// Basic sanitation
if (request.getParameter("hash") == null)
	sane = Boolean.FALSE;

if (sane) {
	currentsubscriber = Subscriber.getSubscriberByHash(request.getParameter("hash"));
	if (currentsubscriber == null) {
		sane = Boolean.FALSE;
		out.println("Could not identify any subscriber with that hash. Do you have the correct link ?");		
	}
}

if (sane && (request.getParameter("confirm") == null)) {
	// Ask the user if he is really sure.
	out.println("<div><div class='title'>You are about to be unsubscribed from the newsletter ! Are you sure ?</div>");
	out.println("<div><form>");
	out.println("<input type='hidden' name='hash' value='"+currentsubscriber.getHash()+"'>");
	out.println("<input type='hidden' name='confirm' value='yes'>");
	out.println("<p><input type='submit' value='YES'></p></form></div>");
} else if (sane && (request.getParameter("confirm").equals("yes"))) {
	// Perform the actual unsubscribe
	Subscriber.DeleteSubscriber(currentsubscriber);
	out.println("<div><div class='title'>Thank you. You have been unsubscribed from the newsletter.</div>");
}

%>

</body>
</html>