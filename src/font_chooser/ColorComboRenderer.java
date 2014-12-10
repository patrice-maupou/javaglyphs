/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package font_chooser;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;

/**
 *
 * 
 */
public class ColorComboRenderer  extends JPanel implements ListCellRenderer {
    private Color color = Color.black;

    public ColorComboRenderer() {
      super();
      setBorder(new CompoundBorder(new MatteBorder(2, 10, 2, 10,
          Color.white), new LineBorder(Color.black)));
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object obj,
        int row, boolean sel, boolean hasFocus) {
      if (obj instanceof Color)
        color = (Color) obj;
      return this;
    }

    @Override
    public void paint(Graphics g) {
      setBackground(color);
      super.paint(g);
    }

  }
