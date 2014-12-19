package ch.aoz.maps;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.*;

import ch.aoz.maps.Language;
import ch.aoz.maps.Phrase;

@SuppressWarnings("serial")
public class Tmp_Phrases extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
      resp.setContentType("text/plain");
      
      Map<String, Language> langs = Language.getAllLanguages();
      for (String lang : langs.keySet()) {
        answer(resp, lang);
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
