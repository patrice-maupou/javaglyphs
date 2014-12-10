/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javaglyphs;

import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

/**
 *
 * @author Patrice
 */
public class TextPath extends JTextArea {

  public static String[] names = new String[]{
    "lineTo", "moveTo", "close", "quadTo", "curveTo", "line", "polygon", "rectangle", "ellipse",
    "roundedRectangle", "arc", "chord", "pie"};
  public static int[] arcType = new int[]{Arc2D.Double.OPEN, Arc2D.Double.CHORD, Arc2D.Double.PIE};
  public static int[] ncoors = new int[]{3, 3, 0, 4, 6, Integer.MAX_VALUE, Integer.MAX_VALUE, 4, 4, 6, 6, 6};
  public CursorStatus status;
  public boolean command_active;

  public enum CursorStatus {

    NONE, LINE, RECTANGLE, ARC;
  }

  public TextPath() {
    super();
    setLineWrap(true);
    setWrapStyleWord(true);
    status = CursorStatus.NONE;
    command_active = false;
  }

  /**
   * insère des coordonnées à la place du curseur
   * @param x abscisse
   * @param y ordonnée
   * @return les deux coordonnées du dernier point s'il y a lieu
   * @throws BadLocationException
   */
  public double[] fillCoords(double x, double y) throws BadLocationException {
    List<String> shapes = Arrays.asList(names);
    double[] c = new double[0];
    int pos = getCaretPosition();
    int line = getLineOfOffset(pos);
    int start = getLineStartOffset(line);
    int end = getLineEndOffset(line);
    String cmd = getText(start, end - start - 1); // ce qui est déjà rempli
    Matcher m = Pattern.compile("([a-zA-Z]+)\\((.*)\\)").matcher(cmd);
    if (m.matches()) {
      String name = m.group(1);
      c = getCoords(m.group(2));
      int n = shapes.indexOf(name);
      if (n != -1) {
        if (c.length < ncoors[n] - 1 && getText(pos, 1).equals(")")) {
          if (c.length > 1) {
            status = (n < 7) ? CursorStatus.LINE : CursorStatus.RECTANGLE;
            if (c.length == 2) {
              if (n > 6) { // largeur et hauteur
                x -= c[0];
                y -= c[1];
                if(x < 0) {
                  x = -x;
                }
                if(y < 0) {
                  y = -y;
                }
              }
            }
          }
          String ajout = (c.length == 1) ? x + "," + y : "," + x + "," + y;
          insert(ajout, pos);
          if (c.length == ncoors[n] - 2) {
            setCaretPosition(getLineEndOffset(line)); // rempli
          }
        }
      }
    }
    return c;
  }

  /**
   *
   * @return un chemin décrit par le texte du composant
   * @throws NumberFormatException coordonnées mal écrites
   * @throws AssertionError rien trouvé
   */
  public Path2D.Double textToPath() throws NumberFormatException, AssertionError {
    Path2D.Double path = new Path2D.Double();
    path.moveTo(0, 0);
    List<String> shapes = Arrays.asList(names);
    String[] lines = getText().split("\n");
    Pattern p = Pattern.compile("([a-zA-Z]+)\\((.*)\\)");
    for (int k = 0; k < lines.length; k++) {
      Matcher m = p.matcher(lines[k]);
      if (m.matches()) {
        String name = m.group(1);
        double[] c = getCoords(m.group(2));
        int n = shapes.indexOf(name);
        switch (n) {
          case 0: // lineTo
          case 1: // moveTo
            if (c.length == 2) {
              if (n == 0) {
                path.lineTo(c[0], c[1]);
              }
              else {
                path.moveTo(c[0], c[1]);
              }
              break;
            }
          case 2: // close
            path.closePath();
            break;
          case 3: // quadTo
            if (c.length == 4) {
              path.quadTo(c[0], c[1], c[2], c[3]);
            }
            break;
          case 4: // curveTo
            if (c.length == 6) {
              path.curveTo(c[0], c[1], c[2], c[3], c[4], c[5]);
            }
            break;
          case 5: // line
          case 6: // polygon
            if (c.length % 2 == 0 && c.length > 3) {
              path.moveTo(c[0], c[1]);
              for (int j = 2; j < c.length; j += 2) {
                path.lineTo(c[j], c[j + 1]);
              }
              if (n == 6) {
                path.lineTo(c[0], c[1]);
                path.closePath();
              }
            }
            break;
          case 7: // rectangle
          case 8: // ellipse
            if (c.length == 4) {
              if (n == 7) {
                path.append(new Rectangle2D.Double(c[0], c[1], c[2], c[3]), false);
              }
              if (n == 8) {
                path.append(new Ellipse2D.Double(c[0], c[1], c[2], c[3]), false);
              }
            }
            break;
          case 9: // roundedRectangle
            if (c.length == 6) {
              path.append(new RoundRectangle2D.Double(c[0], c[1], c[2], c[3], c[4], c[5]), false);
            }
            break;
          case 10: // arc
          case 11: // chord
          case 12: // pie
            if (c.length == 6) {
              path.append(new Arc2D.Double(c[0], c[1], c[2], c[3], c[4], c[5], arcType[n - 10]), false);
            }
            break;
          default:
            throw new AssertionError();
        }
      }
    }
    return path;
  }

  /**
   * récupère les paramètres doubles pour le chemin
   * @param text le texte des paramètres
   * @return les doubles décrits par le texte
   * @throws NumberFormatException
   */
  private double[] getCoords(String text) throws NumberFormatException {
    String[] textCoords = text.split(",");
    int n = textCoords.length;
    double[] ret = new double[n];
    if (n != 0 && n % 2 == 0) {
      for (int i = 0; i < n; i++) {
        ret[i] = Double.parseDouble(textCoords[i]);
      }
    }
    return ret;
  }
}
