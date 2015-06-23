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
    return s.replace('\u00ab', '"')   // <<
	    .replace('\u00bb', '"')   // >>
	    .replace('\u201c', '"')   // " left high
	    .replace('\u201d', '"')   // " right high
	    .replace('\u201e', '"')   // " left low
	    .replace('\u2039', '"')   // <
	    .replace('\u203A', '"')   // >
	    .replace('\u201f', '"')   // " bold
	    .replace('\u301e', '"')   // " high asian
	    .replace('\u301f', '"')   // " low asian
	    .replace('\u301d', '"')   // " bold asian
	    .replace('\uff02', '"');  // " full width
  }
}