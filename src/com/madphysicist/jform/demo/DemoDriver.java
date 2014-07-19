/*
 * DemoDriver.java (Class: com.madphysicist.jform.demo.DemoDriver)
 *
 * Mad Physicist JForm Project
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 by Joseph Fox-Rabinovitz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.madphysicist.jform.demo;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Map;
import java.util.MissingResourceException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import com.madphysicist.jform.translate.CheckBoxTranslator;
import com.madphysicist.jform.ConverterFactory;
import com.madphysicist.jform.JFormManager;
import com.madphysicist.jform.translate.ColorButtonTranslator;
import com.madphysicist.jform.translate.FileFieldTranslator;
import com.madphysicist.jform.translate.TextComponentTranslator;
import com.madphysicist.jform.translate.TranslatorException;
import com.madphysicist.tools.swing.JColorButton;
import com.madphysicist.tools.swing.JFileField;
import com.madphysicist.tools.swing.TogglePanel;

/**
 * A demo of the {@code joe.tools.config} package. The user can edit the values
 * in any of a selection of form fields. Entering invalid values is recommended
 * to demonstrate the markup features provided by {@code Converter}. The basic
 * output options provided by {@code JFormManager} are available as well. This
 * class also contains examples of the {@link JFileField}, {@link JColorButton}
 * and {@link TogglePanel} classes. A properties file with the same base name as
 * this class is required to be on the class path for the program to run.
 *
 * @author Joseph Fox-Rabinovitz
 * @version 1.0.0, 25 May 2013: Joseph Fox-Rabinovitz: Created
 */
public class DemoDriver extends JFrame
{
    /**
     * The version ID for serialization.
     *
     * @serial Increment the least significant three digits when compatibility
     * is not compromised by a structural change (e.g. adding a new field with
     * a sensible default value), and the upper digits when the change makes
     * serialized versions of of the class incompatible with previous releases.
     * @since 1.0.0
     */
    private static final long serialVersionUID = 1000L;

    /**
     * The form manager that is responsible for populating, managing and
     * exporting the form data.
     *
     * @serial
     * @since 1.0.0
     */
    private JFormManager formManager;

    /**
     * A {@code JPanel} that will contain most of the form components. The
     * output options ({@link #outputFile}, {@link #outputLabel}, {@link
     * #typeCombo} and {@link #okButton}) will be added directly to the window
     * rather than to this panel.
     *
     * @serial
     * @since 1.0.0
     */
    private JPanel formPanel;

    /**
     * A sample text field. This component is the editor for the "string"
     * property.
     *
     * @serial
     * @since 1.0.0
     */
    private JTextField stringText;

    /**
     * A sample file field that must contain a directory name. This component is
     * the editor for the "directory" property.
     *
     * @serial
     * @since 1.0.0
     */
    private JFileField directoryField;

    /**
     * A sample text field that must hold an integer value. This component is
     * the editor for the "integer" property.
     *
     * @serial
     * @since 1.0.0
     */
    private JTextField integerText;

    /**
     * A sample text field that must hold a floating point value. This component
     * is the editor for the "double" property.
     *
     * @serial
     * @since 1.0.0
     */
    private JTextField doubleText;

    /**
     * A sample check box. This component is the editor for the "boolean"
     * property.
     *
     * @serial
     * @since 1.0.0
     */
    private JCheckBox booleanCheck;

    /**
     * A sample color button. This component is the editor for the "color"
     * property.
     *
     * @serial
     * @since 1.0.0
     */
    private JColorButton colorButton;

    /**
     * A label for the "string" property editor.
     *
     * @see #stringText
     * @serial
     * @since 1.0.0
     */
    private JLabel stringLabel;

    /**
     * A label for the "directory" property editor.
     *
     * @see #directoryField
     * @serial
     * @since 1.0.0
     */
    private JLabel directoryLabel;

    /**
     * A label for the "integer" property editor.
     *
     * @see #integerText
     * @serial
     * @since 1.0.0
     */
    private JLabel integerLabel;

    /**
     * A label for the "double" property editor.
     *
     * @see #doubleText
     * @serial
     * @since 1.0.0
     */
    private JLabel doubleLabel;

    /**
     * A label for the "boolean" property editor.
     *
     * @see #booleanCheck
     * @serial
     * @since 1.0.0
     */
    private JLabel booleanLabel;

    /**
     * A label for the "color" property editor.
     *
     * @see #colorButton
     * @serial
     * @since 1.0.0
     */
    private JLabel colorLabel;

    /**
     * A file field containing the name of the output file. The form will be
     * exported to the selected file if an output format other than "stdout" is
     * selected.
     *
     * @serial
     * @since 1.0.0
     */
    private JFileField outputFile;

    /**
     * Allows the user to select an output type. Output may be to one of the
     * file types provided by {@code JFormManager} or a text dump to the command
     * line.
     *
     * @serial
     * @since 1.0.0
     */
    private JComboBox<String> typeCombo;

    /**
     * A label for the output file editor.
     *
     * @see #outputFile
     * @serial
     * @since 1.0.0
     */
    private JLabel outputLabel;

    /**
     * A button that exports properties to the selected output format and closes
     * the window. If editors contain invalid values, error markup will be
     * applied to the offending fields and the window will remain open.
     *
     * @serial
     * @since 1.0.0
     */
    private JButton okButton;

    /**
     * Constructs a new demo window, ready for display. A properties file with
     * the same base name as this class is assumed to exist on the class path.
     *
     * @throws TranslatorException if any of the values from the properties file
     * could not be loaded into the corresponding fields.
     * @throws MissingResourceException if the properties file could not be
     * found.
     * @since 1.0.0
     */
    public DemoDriver() throws TranslatorException
    {
        initComponents();
        initForm();
        initFrame();
    }

    /**
     * Initializes the GUI components of the form and adds them to their
     * respective containers. Components are given a name that will determine
     * the underlying property name rather than giving them explicit names in
     * {@link #initForm()}. The OK button is assigned a listener that performs
     * export if all of the fields contain valid values.
     *
     * @since 1.0.0
     */
    private void initComponents() throws TranslatorException
    {
        formManager = new JFormManager();

        setLayout(new GridBagLayout());

        stringText = new JTextField(20);
        stringText.setName("string");
        stringLabel = new JLabel("Enter a string:");
        stringLabel.setLabelFor(stringText);

        directoryField = new JFileField();
        directoryField.setName("directory");
        directoryField.setMode(JFileChooser.DIRECTORIES_ONLY);
        directoryLabel = new JLabel("Select a directory:");
        directoryLabel.setLabelFor(directoryField);

        TogglePanel stringPanel = new TogglePanel(new GridBagLayout());
        stringPanel.setContentBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED),
                "String Inputs", TitledBorder.CENTER, TitledBorder.CENTER));

        stringPanel.addContent(stringLabel, makeConstraints(2, 2, 0, 0, false));
        stringPanel.addContent(stringText, makeConstraints(2, 2, 0, 1, true));
        stringPanel.addContent(directoryLabel, makeConstraints(2, 2, 1, 0, false));
        stringPanel.addContent(directoryField, makeConstraints(2, 2, 1, 1, true));

        integerText = new JTextField(20);
        integerText.setName("integer");
        integerLabel = new JLabel("Enter an integer:");
        integerLabel.setLabelFor(integerText);

        doubleText = new JTextField(20);
        doubleText.setName("double");
        doubleLabel = new JLabel("Enter any number:");
        doubleLabel.setLabelFor(doubleText);

        TogglePanel numberPanel = new TogglePanel(new GridBagLayout());
        numberPanel.setContentBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED),
                "Number Inputs", TitledBorder.CENTER, TitledBorder.CENTER));

        numberPanel.addContent(integerLabel, makeConstraints(2, 2, 0, 0, false));
        numberPanel.addContent(integerText, makeConstraints(2, 2, 0, 1, true));
        numberPanel.addContent(doubleLabel, makeConstraints(2, 2, 1, 0, false));
        numberPanel.addContent(doubleText, makeConstraints(2, 2, 1, 1, true));

        booleanCheck = new JCheckBox();
        booleanCheck.setName("boolean");
        booleanLabel = new JLabel("Click to select:");
        booleanLabel.setLabelFor(booleanCheck);

        colorButton = new JColorButton(32, 32);
        colorButton.setName("color");
        colorLabel = new JLabel("Select Color:");
        colorLabel.setLabelFor(colorButton);

        TogglePanel miscPanel = new TogglePanel();
        miscPanel.setContentLayout(new BoxLayout(miscPanel.getContentPane(), BoxLayout.X_AXIS));
        miscPanel.setContentBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED),
                "Miscellaneous Inputs", TitledBorder.CENTER, TitledBorder.CENTER));

        miscPanel.addContent(Box.createHorizontalStrut(8));
        miscPanel.addContent(booleanLabel);
        miscPanel.addContent(Box.createHorizontalStrut(5));
        miscPanel.addContent(booleanCheck);
        miscPanel.addContent(Box.createHorizontalStrut(50));
        miscPanel.addContent(colorLabel);
        miscPanel.addContent(Box.createHorizontalStrut(5));
        miscPanel.addContent(colorButton);
        miscPanel.addContent(Box.createHorizontalStrut(8));

        formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(new TitledBorder("Sample Config"));

        formPanel.add(stringPanel);
        formPanel.add(numberPanel);
        formPanel.add(miscPanel);

        outputFile = new JFileField();
        outputFile.setButtonRight(false);
        outputFile.setEnabled(false);
        outputLabel = new JLabel("Output: ");
        outputLabel.setLabelFor(outputFile);

        typeCombo = new JComboBox<>(new String[] {"stdout", "stdout (Strings)", "Properties", "XML"});
        typeCombo.setEditable(false);
        typeCombo.addItemListener(new ItemListener() {
            @Override public void itemStateChanged(ItemEvent e) {
                if(typeCombo.getSelectedIndex() == 0)
                    outputFile.setEnabled(false);
                else
                    outputFile.setEnabled(true);
            }
        });

        okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    String selection = typeCombo.getSelectedItem().toString();
                    switch(selection) {
                        case "stdout":
                            for(Map.Entry<String, Object> entry : formManager.exportConfig().entrySet()) {
                                String key = entry.getKey();
                                Object value = entry.getValue();
                                String type = value.getClass().getSimpleName();
                                System.out.println(key + ": " + type + ": " + value.toString());
                            }
                            break;
                        case "stdout (Strings)":
                            for(Map.Entry<String, String> entry : formManager.exportStrings().entrySet()) {
                                String key = entry.getKey();
                                String value = entry.getValue();
                                System.out.println(key + ": " + value);
                            }
                            break;
                        case "Properties":
                            formManager.exportProperties(outputFile.getFile().getPath());
                            break;
                        case "XML":
                            formManager.exportXML(outputFile.getFile().getPath());
                            break;
                        default:
                            throw new IllegalArgumentException("Unrecognized option \"" + selection + "\"");
                    }
                } catch(Exception ex) {
                    ex.printStackTrace();
                    return;
                }
                System.exit(0);
            }
        });

        add(formPanel, new GridBagConstraints(0, 0, 4, 1, 1.0, 1.0,
                             GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                             new Insets(5, 8, 2, 8), 0, 0));
        add(outputLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                            GridBagConstraints.EAST, GridBagConstraints.NONE,
                            new Insets(5, 8, 5, 5), 0, 0));
        add(outputFile, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                            new Insets(5, 5, 5, 5), 0, 0));
        add(typeCombo, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
                            GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                            new Insets(5, 5, 5, 5), 0, 0));
        add(okButton, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0,
                            GridBagConstraints.EAST, GridBagConstraints.NONE,
                            new Insets(5, 5, 5, 8), 20, 0));
    }

    /**
     * Initializes the form components by registering them with {@link
     * #formManager}. Field values are imported from a properties file with the
     * same base name as this class. The file must be in the same package and
     * have writable values for each of the fields in the form. Additional
     * properties found in the file will appear as unbound strings in the
     * manager. This method assumes that all of the GUI components have been
     * initialized by {@link #initComponents()}.
     *
     * @throws TranslatorException if the properties file with the form data is
     * not on the path.
     * @throws MissingResourceException if a properties file with the same base
     * name as this class can not be found in the same package.
     * @since 1.0.0
     */
    private void initForm() throws TranslatorException
    {
        formManager.register(ConverterFactory.createConverter(
                stringText, TextComponentTranslator.String.getInstance(), stringLabel));
        formManager.register(ConverterFactory.createConverter(
                directoryField, new FileFieldTranslator.Directory(true, true, false), directoryLabel));
        formManager.register(ConverterFactory.createConverter(
                integerText, TextComponentTranslator.Integer.getInstance(), integerLabel));
        formManager.register(ConverterFactory.createConverter(
                doubleText, TextComponentTranslator.Double.getInstance(), doubleLabel));
        formManager.register(ConverterFactory.createConverter(
                booleanCheck, CheckBoxTranslator.getInstance(), booleanLabel));
        formManager.register(ConverterFactory.createConverter(
                colorButton, ColorButtonTranslator.getInstance(), colorLabel));

        formManager.importResource(getClass().getName(), true);
    }

    /**
     * Initializes the window for display. The window is given a title, made
     * resizeable and set to close when the "X" button is pressed. {@link
     * #okButton} is set as the default button for the frame. This method does
     * not make the window visible.
     *
     * @since 1.0.0
     */
    private void initFrame()
    {
        getRootPane().setDefaultButton(okButton);
        setTitle("Demo of JConfig v1.0");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(true);
        pack();
    }

    /**
     * Creates a default {@code GridBagConstraints} object for a grid of the
     * specified size at the specified position. The overall gird size is
     * provided to determine when an element is at the edge and needs a little
     * extra padding.
     *
     * @param rows the number of rows in the overall grid.
     * @param cols the number of columns in the overall gird.
     * @param row the row of the element.
     * @param col the column of the element.
     * @param stretch whether or not to stretch the component horizontally (set
     * the x-weight to 1.0).
     * @return a constraints object that will work assuming the element is of
     * size 1x1.
     * @since 1.0.0
     */
    private GridBagConstraints makeConstraints(int rows, int cols, int row, int col, boolean stretch)
    {
        int anchor;
        int left = (col == 0 || cols == 1) ? 8 : 5;
        int right = (col == cols - 1 || cols == 1) ? 8 : 5;
        int bottom = (row == rows - 1) ? 2 : 5;
        double weight = (stretch) ? 1.0 : 0.0;

        if(cols == 1) {
            anchor = GridBagConstraints.CENTER;
        } else {
            if(col == 0)
                anchor = GridBagConstraints.EAST;
            else if(col == cols - 1)
                anchor = GridBagConstraints.WEST;
            else
                anchor = GridBagConstraints.CENTER;
        }

        return  new GridBagConstraints(col, row, 1, 1, weight, 0.0,
                anchor, GridBagConstraints.HORIZONTAL,
                new Insets(2, left, bottom, right), 0, 0);
    }

    /**
     * Runs a demo of {@code JFormManager}. This method sets the window to have
     * the Nimbus look and feel.
     *
     * @param args the command line arguments, which are ignored.
     * @throws Exception if the application look-and-feel could not be loaded.
     * @since 1.0.0
     */
    public static void main(String args[]) throws Exception
    {
        // Set the Nimbus look and feel
        // If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
        // For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
        for(UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if("Nimbus".equals(info.getName())) {
                UIManager.setLookAndFeel(info.getClassName());
                break;
            }
        }

        // Create and display the form
        EventQueue.invokeLater(new Runnable() {
            public void run()
            {
                try {
                    new DemoDriver().setVisible(true);
                } catch(TranslatorException | MissingResourceException ex) {
                    System.err.println("Unable to initialize data based on properties file.");
                    ex.printStackTrace();
                }
            }
        });
    }
}
