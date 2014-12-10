package font_chooser;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Patrice
 */
public class RegexSamples {

  private Pattern pattern;
  private String[] parts;

  public RegexSamples() {
  }

  private void match(String text) {
    pattern = Pattern.compile(parts[0] + parts[1] + parts[2]);
    Matcher m = pattern.matcher(text);
    for (int j = 0; j < 3; j++) {
      if (m.lookingAt()) {
      System.out.println("trouvé : ");
      System.out.println("a: " + m.group("a") + "   op: " + m.group("op") + "    b: " + m.group("b"));
      }
      //m.usePattern(Pattern.compile("(?<a>[^\\+-][\\+-]{1}[^\\+-])" + parts[1] + "(?<b>.+)"));
    }
  }

  public void setParts(String[] parts) {
    this.parts = parts;
  }

  public static void main(String[] args) {
    RegexSamples regexSamples = new RegexSamples();
    regexSamples.setParts(new String[]{"(?<a>.+)", "(?<op>[\\+-])", "(?<b>.+)"});
    regexSamples.match("3-4-(5-6)");


  }
}
