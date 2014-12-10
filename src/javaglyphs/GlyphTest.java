/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javaglyphs;

/**
 *
 * @author Patrice Maupou
 */
import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import font_chooser.JFontChooser;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author Patrice
 */
public class GlyphTest extends JFrame {

  private Font font;
  JPanel glyphTable;
  JComboBox combo, comboShapes, comboNames;
  JSpinner spinner;
  SpinnerNumberModel model;
  JTextArea textArea;
  TextPath textWork;
  JSlider slider;
  GridBagConstraints gbc;
  GlyphPanel glyphPanel, glyphWork;
  int n = 10, zoom = 4;
  private JMenuBar menuBar;
  private JMenu Files, Glyphs;
  private JMenuItem Open, Save, AddGlyph, Fonts, DrawShape;
  private JFontChooser fontChooser;
  private JFileChooser fileDialog;
  private JDialogName dialogName;
  private HashMap<String, String> namesToShapes;



  public GlyphTest(){

    final Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
    fontChooser = new JFontChooser(this, true);
    fontChooser.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(java.awt.event.WindowEvent e) {
        if(fontChooser.getClosed_Option() == JOptionPane.OK_OPTION) {
          setFont(fontChooser.getFont());
          updateFont(getFont());
        }
      }
    });

    namesToShapes = new HashMap<String, String>();

    fileDialog = new JFileChooser(System.getProperty("user.dir")+"/formes");
    fileDialog.setFileSelectionMode( JFileChooser.FILES_ONLY );
    fileDialog.setFileFilter(new FileNameExtensionFilter("fichier formes(*.frm)", "frm"));

    dialogName = new JDialogName(this, true);

    menuBar = new JMenuBar();
    Files = new JMenu();
    Files.setText("Fichiers");
    Open = new JMenuItem("Ouvrir");
    Open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,Event.CTRL_MASK));
    Open.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        int returnVal = fileDialog.showOpenDialog(Open);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          File file = fileDialog.getSelectedFile();
          try {
            ObjectInputStream stream = new ObjectInputStream(new FileInputStream(file));
            namesToShapes = (HashMap<String, String>) stream.readObject();
            stream.close();
            for (String name : namesToShapes.keySet()) {
              comboNames.addItem(name);
            }
          } catch (Exception ex) {
            Logger.getLogger(GlyphTest.class.getName()).log(Level.SEVERE, null, ex);
          }
        }
      }
    });
    Files.add(Open);
    Save = new JMenuItem("Enregistrer");
    Save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,Event.CTRL_MASK));
    Save.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        int returnVal = fileDialog.showSaveDialog(Save);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          File file = fileDialog.getSelectedFile();
          try {
            ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(file));
            stream.writeObject(namesToShapes);
            stream.close();
          } catch (IOException ex) {
            Logger.getLogger(GlyphTest.class.getName()).log(Level.SEVERE, null, ex);
          }
        }
      }
    });
    Files.add(Save);
    menuBar.add(Files);

    Glyphs = new JMenu();
    Glyphs.setText("Glyphes");
    Fonts = new JMenuItem("Choisir une police");
    Fonts.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,Event.CTRL_MASK));
    Fonts.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        fontChooser.setLocation((screenSize.width-fontChooser.getWidth())/2,
                (screenSize.height-fontChooser.getHeight())/2);
        fontChooser.setVisible(true);
      }
    });
    Glyphs.add(Fonts);
    AddGlyph = new JMenuItem("Ajouter une forme");
    AddGlyph.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,Event.CTRL_MASK));
    AddGlyph.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        dialogName.setLocation((screenSize.width-dialogName.getWidth())/2,
            (screenSize.height-dialogName.getHeight())/2);
        dialogName.setVisible(true);
      }
    });
    dialogName.okButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        dialogName.dispose();
        String name = dialogName.textName.getText();
        namesToShapes.put(name, textWork.getText());
        comboNames.addItem(name);
      }
    });
    Glyphs.add(AddGlyph);
    DrawShape = new JMenuItem("Dessine la forme");
    DrawShape.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, Event.CTRL_MASK));
    DrawShape.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if(textWork.isVisible()) {
          try {
            glyphWork.setPath(textWork.textToPath());
            textWork.command_active = false;
            zoom = slider.getValue();
            paintGlyphPanel(-1, zoom, glyphWork);
          } catch (NumberFormatException numberFormatException) {
          } catch (AssertionError assertionError) {
          }
        }
      }
    });
    Glyphs.add(DrawShape);
    menuBar.add(Glyphs);
    setJMenuBar(menuBar);

    combo = new JComboBox();
    combo.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        setFont(new Font((String) combo.getSelectedItem(), Font.PLAIN, 18));
        populateComponents();
        paintGlyphPanel(n, zoom, glyphPanel);
      }
    });

    spinner = new javax.swing.JSpinner();
    model = new SpinnerNumberModel(n, 0, 5000, 1);
    spinner.setModel(model);
    spinner.setMaximumSize(new java.awt.Dimension(30, 20));
    spinner.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(javax.swing.event.ChangeEvent evt) {
        n = (Integer) model.getNumber();
        paintGlyphPanel(n, zoom, glyphPanel);
      }
    });

    slider = new JSlider(1, 10, 4);
    slider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        zoom = slider.getValue();
        paintGlyphPanel(n, zoom, glyphPanel);
        paintGlyphPanel(-1, zoom, glyphWork);
      }
    });
    comboShapes = new JComboBox(TextPath.names);
    comboShapes.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        textWork.append(comboShapes.getSelectedItem() + "()\n");
        textWork.setCaretPosition(textWork.getText().length()-2);
        textWork.requestFocus();
        textWork.command_active = true;
      }
    });
    comboNames = new JComboBox();
    comboNames.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        int index = comboNames.getSelectedIndex();
        if(index != -1 && comboNames.hasFocus()) {
          String name = comboNames.getItemAt(index).toString();
          textWork.append(namesToShapes.get(name));
        }
      }
    });

    JPanel commands = new JPanel(new GridBagLayout());   // north panel
    gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 0.5;
    commands.add(combo, gbc);
    gbc.gridx = 1;
    commands.add(new JLabel("   glyph : "), gbc);
    gbc.gridx = 2;
    commands.add(spinner, gbc);
    gbc.gridx = 3;
    commands.add(new JLabel("     zoom : "), gbc);
    gbc.gridx = 4;
    commands.add(slider, gbc);
    gbc.gridx = 0;
    gbc.gridy = 1;
    commands.add(new JLabel("    commandes : "), gbc);
    gbc.gridx = 1;
    commands.add(comboShapes, gbc);
    gbc.gridx = 2;
    commands.add(new JLabel("     formes:"), gbc);
    gbc.gridx = 3;
    commands.add(comboNames, gbc);


    JTabbedPane tabbedPane = new JTabbedPane();   // center panel

    // tab 1
    glyphTable = new JPanel(new GridBagLayout());
    gbc = new GridBagConstraints();
    gbc.weightx = 0;
    gbc.weighty = 0;
    gbc.insets = new Insets(1, 1, 1, 1);
    tabbedPane.addTab("police", new JScrollPane(glyphTable));

    // tab 2
    textArea = new JTextArea();
    glyphPanel = new GlyphPanel(false, textArea);
    glyphPanel.setFont(new Font((String) combo.getSelectedItem(), Font.PLAIN, 60));
    glyphPanel.setPreferredSize(new Dimension(1000,1000));
    JScrollPane scrollPane = new JScrollPane(glyphPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    scrollPane.getHorizontalScrollBar().setValues(200, 200, 0, 200);
    scrollPane.getVerticalScrollBar().setValues(200, 200, 0, 200);
    JScrollPane scrollText = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    scrollText.setPreferredSize(new Dimension(400, 70));
    JPanel detail = new JPanel(new BorderLayout());
    detail.add(scrollPane);
    detail.add(scrollText, BorderLayout.SOUTH);
    tabbedPane.addTab("détail", detail);

    // tab 3
    textWork = new TextPath();
    glyphWork = new GlyphPanel(true, textWork);
    glyphWork.setPreferredSize(new Dimension(1000,1000));
    JScrollPane scrollGlyphWork = new JScrollPane(glyphWork, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    scrollGlyphWork.getHorizontalScrollBar().setValues(200, 200, 0, 200);
    scrollGlyphWork.getVerticalScrollBar().setValues(200, 200, 0, 200);

    JScrollPane scrollTextWork = new JScrollPane(textWork, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    scrollTextWork.setPreferredSize(new Dimension(400, 70));
    JPanel work = new JPanel(new BorderLayout());
    work.add(scrollGlyphWork);
    work.add(scrollTextWork, BorderLayout.SOUTH);
    tabbedPane.add("formes", work);


    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    add(commands, "North");
    add(tabbedPane);
    setBounds((screenSize.width-816)/2, (screenSize.height-638)/2, 816, 638);
    setVisible(true);
    paintGlyphPanel(-1, zoom, glyphWork);
  }

/**
   * redessine les panels pour la nouvelle police corante
   * @param font la fonte courante
   */
  public void updateFont(Font font) {
    setFont(font);
    String name = font.getName();
    combo.addItem(name);
    combo.setSelectedItem(name);
    populateComponents();
    paintGlyphPanel(n, zoom, glyphPanel);
  }

  /**
   * dessine le contour du glyphe courant
   * @param n le rang du glyphe courant
   * @param zoom  l'agrandissement
   * @param glyphPanel le panel de dessin
   */
  private void paintGlyphPanel(int n, int zoom, GlyphPanel glyphPanel) {
    glyphPanel.setFont(new Font(getFont().getName(), getFont().getStyle(), 60));
    if(n != -1)glyphPanel.setI(new int[]{n});
    glyphPanel.setZoom(zoom);
    glyphPanel.revalidate();
  }

/**
   * Etablit les paramètres graphiques pour le dessin d'une police
   */
  private void populateComponents() {
    int numGlyphs = getFont().getNumGlyphs();
    FontRenderContext frc = new FontRenderContext(null, true, false);
    Rectangle2D r = getFont().getMaxCharBounds(frc);
    Dimension d = new Dimension();
    d.width = (int) Math.ceil(r.getWidth());
    d.height = (int) Math.ceil(r.getHeight());
    populatePanel(numGlyphs, d);
  }

  /**
   * dessine tous les glyphes de la fonte courante
   * @param numGlyphs nombre de glyphes
   * @param d dimension de chaque dessin
   */
  private void populatePanel(int numGlyphs, Dimension d) {
    glyphTable.removeAll();
    glyphTable.repaint();
    GlyphLabel gl;
    for (int j = 0; j < numGlyphs; j++) {
      gl = new GlyphLabel(j);
      gl.setFont(getFont());
      gl.setPreferredSize(d);
      if ((j + 1) % 16 == 0) {
        gbc.gridwidth = GridBagConstraints.REMAINDER;
      }
      else {
        gbc.gridwidth = 1;
      }
      gl.addMouseListener(new MouseAdapter() {

        @Override
        public void mouseClicked(MouseEvent e) {
          if (e.getSource() instanceof GlyphLabel) {
            GlyphLabel glyphLabel = (GlyphLabel) e.getSource();
            spinner.getModel().setValue(glyphLabel.getI()[0]);
            glyphLabel.setBackground(getBackground().darker());
            glyphLabel.setClicked(true);
          }
        }

        @Override
        public void mouseExited(MouseEvent e) {
          if (e.getSource() instanceof GlyphLabel) {
            GlyphLabel glyphLabel = (GlyphLabel) e.getSource();
            if (glyphLabel.isClicked()) {
              glyphLabel.setBackground(getBackground().brighter());
              glyphLabel.setClicked(false);
            }
          }
        }
      });
      glyphTable.add(gl, gbc);
    }
    glyphTable.revalidate();
  }


  /**
   * @param font the font to set
   */
  @Override
  public void setFont(Font font) {
    this.font = font;
  }

  @Override
  public Font getFont() {
    return font;
  }

  public static void main(String[] args) {
    java.awt.EventQueue.invokeLater(new Runnable() {

      @Override
      public void run() {
        GlyphTest glyphTest = new GlyphTest();
        glyphTest.addWindowListener(new java.awt.event.WindowAdapter() {
          @Override
          public void windowClosed(java.awt.event.WindowEvent e) {
            System.exit(0);
          }
        });
        glyphTest.setVisible(true);
      }
    });
  }

}
