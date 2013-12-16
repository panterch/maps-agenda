package ch.aoz.maps;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.*;

import ch.aoz.maps.Language;

@SuppressWarnings("serial")
public class Maps_LanguageServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {        
        StringBuilder response = new StringBuilder();
        response.append("/* version 0.5 */\n");
        response.append("function LanguageMenuCtrl($scope) {\n");
        response.append("  $scope.languages = [\n");
        for (Language lang : getLanguages()) {
          response.append("    {label: '");
          response.append(toUnicode(lang.getName()));
          response.append("', code: '");
          response.append(toUnicode(lang.getCode()));
          response.append("'},\n");
        }
        response.append("  ];\n");
        response.append("};");
                
        resp.setContentType("application/javascript");
        resp.getWriter().println(response.toString());
    }
    
    public String toUnicode(String s) {
      StringBuilder b = new StringBuilder();
      for (char c : s.toCharArray()) {
        if (c < 128) {
          b.append(c);
        } else {
          b.append("\\u");
          
          String hex = Integer.toHexString(c);
          if (hex.length() < 4) {
            for (int i = hex.length(); i < 4; ++i) {
              b.append('0');
            }
          }
          b.append(hex);
        }
      }
      return b.toString();      
    }
    
    public List<Language> getLanguages() {
      HashMap<String, Language> languages = Language.getAllLanguages();
      ArrayList<Language> langs = new ArrayList<Language>(languages.size());
      langs.add(languages.remove("de"));
      ArrayList<String> keys = new ArrayList<String>(languages.keySet());
      Collections.sort(keys);
      for (String lang : keys) {
        langs.add(languages.remove(lang));
      }
      return langs;
    }
}
