/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package font_chooser;

import java.awt.Color;
import javax.swing.DefaultComboBoxModel;

/**
 *
 * @author Patrice
 */
public class ColorComboBoxModel extends DefaultComboBoxModel<Color> {


  public ColorComboBoxModel() {
    int[] values = new int[]{0, 128, 192, 255};
    setElements(values);
  }

  public ColorComboBoxModel(int[] values) {
    setElements(values);
  }

  private void setElements(int[] values) {
    for (int r = 0; r < values.length; r++) {
      for (int g = 0; g < values.length; g++) {
        for (int b = 0; b < values.length; b++) {
          addElement(new Color(values[r%256], values[g%256], values[b%256]));
        }
      }
    }
  }

}
