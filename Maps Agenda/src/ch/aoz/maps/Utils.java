package ch.aoz.maps;

import java.util.Arrays;
import java.util.List;

public class Utils {
  public static String toUnicode(String s) {
    StringBuilder b = new StringBuilder();
    List<Character> forbiddenChars  = Arrays.asList('"', '\\', '/');
    if (s != null && s.length() > 0) {
      for (char c : s.toCharArray()) {
        if (c == '\n') {
          b.append("\\n");
        } else if (c == '\r') {
          b.append("\\r");
        } else if ((c > 32 && c < 128 && !forbiddenChars.contains(c)) || c == ' ') {
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
    }
    return b.toString();      
  }  
}