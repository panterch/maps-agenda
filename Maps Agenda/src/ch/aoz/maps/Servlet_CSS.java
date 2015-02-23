package ch.aoz.maps;

import java.io.IOException;
import javax.servlet.http.*;

@SuppressWarnings("serial")
public class Servlet_CSS extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
      String color = BackgroundColor.fetchFromStore().getColor();
      String image = BackgroundImage.fetchFromStore().getUrl();
      if (image == null || image.equals("")) {
        image = "/maps/images/temp-bg.png";
      } else {
        image += "=s1280";
      }
      
      resp.setContentType("text/css");
      resp.getWriter().println(".background-color {");
      resp.getWriter().println("  background-color: #" + color + ";");
      resp.getWriter().println("}");
      resp.getWriter().println(".background-screen {");
      resp.getWriter().println("  background-image: url('" + image + "');");
      resp.getWriter().println("  background-position-x: center;");
      resp.getWriter().println("  background-size: cover;");
      resp.getWriter().println("  height: 100%;");
      resp.getWriter().println("  opacity: 1.0;");
      resp.getWriter().println("  position: fixed;");
      resp.getWriter().println("  width: 100%;");
      resp.getWriter().println("  z-index: -1;");
      resp.getWriter().println("}");
    }
}
