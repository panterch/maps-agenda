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
        response.append("function LanguageMenuCtrl($scope) {\n");
        response.append("  $scope.languages = [\n");
        for (Language lang : languages.values()) {
          response.append("    {label: '");
          response.append(lang.getName());
          response.append("', code: '");
          response.append(lang.getCode());
          response.append("'},\n");
        }
        response.append("  ];\n");
        response.append("};");
                
        resp.setContentType("text/javascript");
        resp.getWriter().println(response.toString());
    }
    
    public String toUnicode(String s) {
      StringBuilder b = new StringBuilder();
      for (char c : s.toCharArray()) {
        if (c < 128) {
          b.append(c);
        } else {
          b.append("\\u").append(Integer.toHexString(c));
        }
      }
      return b.toString();      
    }
}
