package ch.aoz.maps;

import java.io.IOException;
import javax.servlet.http.*;

@SuppressWarnings("serial")
public class Maps_SubscriberServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
      String name = req.getParameter("name");
      String lang = req.getParameter("lang");
      if (lang == null) {
        lang = "de";
      }

      String email = req.getParameter("email");
      // TODO: also check if email is valid.
      if (email == null) {
        Send(buildError("A valid email address must be specified."), resp);
        return;
      }
      
      // Check if the subscriber already exists.
      Subscriber subscriber = Subscriber.GetByEmail(email);
      if (subscriber != null) {
        Send(buildError("The provided email address is already subscribed."), resp);
        return;
      }

      // Subscribe the new address.
      subscriber = new Subscriber(email, name, lang);
      if (!Subscriber.AddSubscriber(subscriber)) {
        Send(buildError("An error occurred. Please try again later."), resp);
        return;
      }
      Send(buildSuccess("Successfully subscribed " + email), resp);
      return;
    }
    public void Send(String response, HttpServletResponse resp) throws IOException {
      resp.setContentType("application/json");
      resp.getWriter().println(response);
    }
    public String buildError(String message) {
      return "{ \"status\":\"error\", \"message\":\"" + Utils.toUnicode(message) + "\"}";
    }
    public String buildSuccess(String message) {
      return "{ \"status\":\"ok\", \"message\":\"" + Utils.toUnicode(message) + "\"}";
    }
}
