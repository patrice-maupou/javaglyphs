/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javaglyphs;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import javax.swing.JLabel;
import javax.swing.border.EtchedBorder;

/**
 *
 * @author Patrice
 */
public class GlyphLabel extends JLabel {

  private int[] i;
  private boolean clicked = false;


  public GlyphLabel(int n) {
    this.i = new int[]{n};
    setBorder(new EtchedBorder());
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    FontRenderContext frc = g2.getFontRenderContext();
    GlyphVector gv = getFont().createGlyphVector(frc, getI());
    Rectangle2D r2 = gv.getLogicalBounds();
    float x = (float) ((getWidth() - r2.getWidth()) / 2);
    float y = (float) ((getHeight() - r2.getHeight()) / 2 - r2.getY());
    g2.drawGlyphVector(gv, x, y);
  }

  public int[] getI() {
    return i;
  }
  public void setI(int[] i) {
    this.i = i;
  }

  public boolean isClicked() {
    return clicked;
  }

  public void setClicked(boolean clicked) {
    this.clicked = clicked;
  }
}
