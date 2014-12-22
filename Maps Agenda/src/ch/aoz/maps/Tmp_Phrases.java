package ch.aoz.maps;

import java.io.IOException;

import javax.servlet.http.*;

import ch.aoz.maps.Language;

@SuppressWarnings("serial")
public class Tmp_Phrases extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
      resp.setContentType("text/plain");
      for (Language l : Language.getAllLanguages()) {
        // answer(resp, l.getCode());
        resp.getWriter().println(l.getCode());      
      }
    }
   
    public void answer(HttpServletResponse resp, String lang)
            throws IOException {
      Phrases p = Phrases.GetPhrasesForLanguage(lang);
      
      StringBuilder response = new StringBuilder();
      response.append(lang);
      response.append(": ");
      response.append(p.addToStore());     
      resp.getWriter().println(response.toString());      
    }
}
