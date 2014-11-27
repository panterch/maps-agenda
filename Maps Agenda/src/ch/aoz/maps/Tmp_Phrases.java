package ch.aoz.maps;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.*;

@SuppressWarnings("serial")
public class Tmp_Phrases extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
      Map<String, Language> langs = Language.getAllLanguages();
      Languages languages = new Languages(langs.values());      
      answer(resp, languages.addToStore());
    }
    
    public void answer(HttpServletResponse resp, boolean added) 
            throws IOException {
      StringBuilder response = new StringBuilder();
      response.append("added");
      response.append(": ");
      response.append(added);
      response.append("\n");
      resp.setContentType("text/plain");
      resp.getWriter().println(response.toString());      
    }
}
