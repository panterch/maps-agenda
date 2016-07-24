package ch.aoz.maps;

import java.io.IOException;

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
        for (Language lang : Language.getAllLanguages()) {
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
}
