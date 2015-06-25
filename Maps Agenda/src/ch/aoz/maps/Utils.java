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
  
  /**
   * Replaces all non-standard double quote characters by "programmer's
   * double quotes". To be used in event titles.
   * @param s text
   * @return s with all alternative double quotes replaced with "
   */
  public static String replaceDoubleQuotes(String s) {
    if (s == null || s.isEmpty()) {
	return "";
    }
    
    /* In order:
     * U+00ab  <<
     * U+00bb  >>
     * U+201c  " left high
     * U+201d  " right high
     * U+201e  " left low
     * U+2039  <
     * U+203A  >
     * U+201f  " bold
     * U+301e  " high asian
     * U+301f  " low asian
     * U+301d  " bold asian
     * U+ff02  " full width
     */
    return s.replaceAll("\u00ab|\u00bb|\u201c|\u201d|\u201e|\u2039|\u203a|" +
                        "\u201f|\u301e|\u301f|\u301d|\uff02",
                        "\"");
  }
}