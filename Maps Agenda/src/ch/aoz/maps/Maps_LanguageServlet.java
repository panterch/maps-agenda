package ch.aoz.maps;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.*;

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
          response.append(Utils.toUnicode(lang.getName()));
          response.append("', code: '");
          response.append(Utils.toUnicode(lang.getCode()));
          response.append("'},\n");
        }
        response.append("  ];\n");
        response.append("  common.setLanguages($scope.languages);\n");
        response.append("  $scope.setLanguage = function(language) {\n");
        response.append("    common.setSelectedLanguage(language.code);\n");
        response.append("  };\n");
        response.append("};");
                
        resp.setContentType("application/javascript");
        resp.getWriter().println(response.toString());
    }
        
    public List<Language> getLanguages() {
      HashMap<String, Language> languages = Language.getAllLanguages();
      ArrayList<Language> langs = new ArrayList<Language>(languages.size());
      langs.add(languages.remove("de"));
      Collections.sort(langs);
      ArrayList<String> keys = new ArrayList<String>(languages.keySet());
      for (String lang : keys) {
        langs.add(languages.remove(lang));
      }
      return langs;
    }
}
