package ch.aoz.maps;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.http.*;

import ch.aoz.maps.Language;

@SuppressWarnings("serial")
public class Maps_LanguageServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        HashMap<String, Language> languages = Language.getAllLanguages();
        
        StringBuilder response = new StringBuilder();
        response.append("/* version 0.5 */\n");
        response.append("function LanguageMenuCtrl($scope) {\n");
        response.append("  $scope.languages = [\n");
        for (Language lang : languages.values()) {
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
}
