/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javaglyphs;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.event.MouseInputAdapter;
import javax.swing.text.BadLocationException;

/**
 *
 * @author Patrice
 */
public class GlyphPanel extends JPanel {

  private int[] i;
  private int zoom;
  private JTextArea textArea;
  private Path2D.Double path;
  private PathIterator pathIterator;
  private Shape tipShape;
  private HashMap<Shape, String> toCoors;
  private String text;
  private boolean changed, freePath;
  private double Ox, Oy;
  private AffineTransform at;

  /**
   * @param freePath true si le chemin est dans la zone de texte
   * @param textArea la zone de texte associée située sous le GlyphPanel
   */
  public GlyphPanel(final boolean freePath, final JTextArea textArea) {
    this.textArea = textArea;
    this.freePath = freePath;
    toCoors = new HashMap<Shape, String>();  // point actif et coordonnées associées
    path = new Path2D.Double();
    setToolTipText("");
    changed = false;
    MouseInputAdapter inputAdapter = new MouseInputAdapter() {
      int X, Y;
      @Override
      public void mouseClicked(MouseEvent e) {
        if(textArea instanceof TextPath && ((TextPath)textArea).command_active) {
          X = e.getX();
          Y = e.getY();
          try {
            Point2D.Double pSrc = new Point2D.Double(e.getX(), e.getY()), pDst = pSrc;
            at.inverseTransform(pSrc, pDst);
            double x = Math.round(pDst.getX()*10000)/10000.0;
            double y = Math.round(pDst.getY()*10000)/10000.0;
            ((TextPath) textArea).fillCoords(x, y);
            path.append(new Line2D.Double(x, y, x, y), false); // ajoute le point
            repaint();
          } catch (NoninvertibleTransformException ex) {
            Logger.getLogger(GlyphPanel.class.getName()).log(Level.WARNING, null, ex);
          } catch (BadLocationException ex) {}
        }
      }
      @Override
      public void mouseDragged(MouseEvent e) {
        double dx = Ox - e.getX(), dy = Oy - e.getY();
        if (dx < 6 && -dx < 6 && dy < 6 && -dy < 6) {
          Ox = e.getX();
          Oy = e.getY();
          repaint();
        }
        else if(textArea instanceof TextPath) {
          //TODO : modififier éventuellement toCoors pour faire une HashMap vers le texte
          // par exemple [0,1,2,3.15]=polygon

        }
      }
    };
    addMouseMotionListener(inputAdapter);
    addMouseListener(inputAdapter);
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    if (Ox == 0 && Oy == 0) {
      Ox = getWidth() / 2;
      Oy = getHeight() / 2;  // origine du tracé
    }
    at = AffineTransform.getTranslateInstance(Ox, Oy);
    at.concatenate(AffineTransform.getScaleInstance(getZoom(), getZoom()));
    Path2D.Double axes = new Path2D.Double(new Line2D.Double(-500, 0, 500, 0));
    axes.append(new Line2D.Double(0, -500, 0, 500), false);
    g2.setColor(Color.LIGHT_GRAY);
    Shape shape = axes.createTransformedShape(at);
    g2.draw(shape);
    if (!freePath && getI() != null) {
      FontRenderContext frc = g2.getFontRenderContext();
      GlyphVector gv = getFont().createGlyphVector(frc, getI());
      path.reset();
      for (int k = 0; k < gv.getNumGlyphs(); k++) {
        path.append(gv.getGlyphOutline(k), false);
      }
    }
    pathIterator = path.getPathIterator(null);
    double[] c = new double[6];              // contient les coordonnées des points suivants
    int end = 1;                             // la fin utilisable de c
    text = "";                               // le texte du chemin
    toCoors.clear();  // point actif et coordonnées associées
    while (!pathIterator.isDone()) {
      Path2D.Double npath = new Path2D.Double(pathIterator.getWindingRule());
      g2.setColor(Color.black);
      if (end != -1) {
        shape = shapePoint(c[end - 1], c[end], 0.3, at);
        toCoors.put(shape, "(" + c[end - 1] + "," + c[end] + ")");
        npath.moveTo(c[end - 1], c[end]);
        g2.fill(shape);
      }
      switch (pathIterator.currentSegment(c)) {
        case PathIterator.SEG_LINETO:
          g2.setColor(Color.BLUE);
          npath.lineTo(c[0], c[1]);
          text += "lineTo(" + c[0] + "," + c[1] + ")\n";
          end = 1;
          break;
        case PathIterator.SEG_MOVETO:
          npath.moveTo(c[0], c[1]);
          text += "moveTo(" + c[0] + "," + c[1] + ")\n";
          end = 1;
          break;
        case PathIterator.SEG_CLOSE:
          npath.closePath();
          text += "close\n";
          end = -1;
          break;
        case PathIterator.SEG_QUADTO:
          g2.setColor(Color.RED);
          npath.quadTo(c[0], c[1], c[2], c[3]);
          shape = shapePoint(c[0], c[1], 0.3, at);
          toCoors.put(shape, c[0] + "," + c[1]);
          g2.fill(shape);
          text += "quadTo(" + c[0] + "," + c[1] + "," + c[2] + "," + c[3] + ")\n";
          end = 3;
          break;
        case PathIterator.SEG_CUBICTO:
          g2.setColor(Color.GREEN);
          npath.curveTo(c[0], c[1], c[2], c[3], c[4], c[5]);
          shape = shapePoint(c[0], c[1], 0.3, at);
          toCoors.put(shape, c[0] + "," + c[1]);
          g2.fill(shape);
          shape = shapePoint(c[2], c[3], 0.3, at);
          toCoors.put(shape, c[2] + "," + c[3]);
          g2.fill(shape);
          text += "curveTo(" + c[0] + "," + c[1] + "," + c[2] + "," + c[3] + ","
                  + c[4] + "," + c[5] + ")\n";
          end = 5;
          break;
        default:
          throw new AssertionError(pathIterator.currentSegment(c));
      }
      shape = npath.createTransformedShape(at);
      g2.draw(shape);
      pathIterator.next();
    }
    if (changed) {
      textArea.setText(text);
      textArea.setCaretPosition(0);
      changed = false;
    }
  }

  /**
   *
   * @param x abscisse du point
   * @param y ordonnée
   * @param scale agrandissement
   * @param at translation à l'origine
   * @return
   */
  private Shape shapePoint(double x, double y, double w, AffineTransform at) {
    return at.createTransformedShape(new Ellipse2D.Double(x - w, y - w, 2 * w, 2 * w));
  }

  public Path2D.Double getPath() {
    return path;
  }

  public void setPath(Path2D.Double path) {
    this.path = path;
  }

  @Override
  public String getToolTipText(MouseEvent e) {
    changed = false;
    String tip;
    Point2D.Double pSrc = new Point2D.Double(e.getX(), e.getY()), pDst = pSrc;
    if(textArea instanceof TextPath) {
      try {
        at.inverseTransform(pSrc, pDst);
      } catch (NoninvertibleTransformException ex) {}
      pDst.x = Math.round(pDst.x*100000)/100000.0;
      pDst.y = Math.round(pDst.y*100000)/100000.0;
      tip = pDst.x + "," + pDst.y;
    }
    else {
      tip = toCoors.get(tipShape);
    }
    return tip;
  }

  @Override
  public boolean contains(int x, int y) {
    boolean ret = false;
    if(textArea instanceof TextPath) {
      ret = true;
    }
    Rectangle r = new Rectangle(x - 5, y - 5, 10, 10);
    for (Shape shape : toCoors.keySet()) {
      if (shape.intersects(r)) {
        tipShape = shape;
        ret = true;
        break;
      }
    }
    return ret;
  }

  public int[] getI() {
    return i;
  }

  public void setI(int[] i) {
    this.i = i;
    changed = true;
  }

  public int getZoom() {
    return zoom;
  }

  public void setZoom(int zoom) {
    this.zoom = zoom;
  }
}
