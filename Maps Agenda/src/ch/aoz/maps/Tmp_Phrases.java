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
      Map<String, Language> langs = Language.getAllLanguages();
      Map<String, String> added = new HashMap<String, String>();
      for (String lang : langs.keySet()) {
        List<Phrase> laPhrases = Phrase.GetPhrasesForLanguage(lang);
        Phrases p;
        if (laPhrases == null) {
          p = new Phrases(lang);
        } else {
          p = new Phrases(laPhrases);
        }
        if (!p.isOk()) {
          added.put(lang, p.debug());
        } else {
          added.put(lang, Boolean.toString(p.addToStore()));
        }
      }
      answer(resp, added);
    }
    
    public void answer(HttpServletResponse resp, Map<String, String> added) 
            throws IOException {
      StringBuilder response = new StringBuilder();
      for (String key : added.keySet()) {
        response.append(key);
        response.append(": ");
        response.append(added.get(key));
        response.append("\n");
      }
      resp.setContentType("text/plain");
      resp.getWriter().println(response.toString());      
    }
}
