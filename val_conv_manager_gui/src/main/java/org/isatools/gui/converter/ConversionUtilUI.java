/**

 The ISAconverter, ISAvalidator & BII Management Tool are components of the ISA software suite (http://www.isa-tools.org)

 Exhibit A
 The ISAconverter, ISAvalidator & BII Management Tool are licensed under the Mozilla Public License (MPL) version
 1.1/GPL version 2.0/LGPL version 2.1

 "The contents of this file are subject to the Mozilla Public License
 Version 1.1 (the "License"). You may not use this file except in compliance with the License.
 You may obtain copies of the Licenses at http://www.mozilla.org/MPL/MPL-1.1.html.

 Software distributed under the License is distributed on an "AS IS"
 basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 License for the specific language governing rights and limitations
 under the License.

 The Original Code is the ISAconverter, ISAvalidator & BII Management Tool.

 The Initial Developer of the Original Code is the ISA Team (Eamonn Maguire, eamonnmag@gmail.com;
 Philippe Rocca-Serra, proccaserra@gmail.com; Susanna-Assunta Sansone, sa.sanson@gmail.com;
 http://www.isa-tools.org). All portions of the code written by the ISA Team are Copyright (c)
 2007-2011 ISA Team. All Rights Reserved.

 Contributor(s):
 Rocca-Serra P, Brandizi M, Maguire E, Sklyar N, Taylor C, Begley K, Field D,
 Harris S, Hide W, Hofmann O, Neumann S, Sterk P, Tong W, Sansone SA. ISA software suite:
 supporting standards-compliant experimental annotation and enabling curation at the community level.
 Bioinformatics 2010;26(18):2354-6.

 Alternatively, the contents of this file may be used under the terms of either the GNU General
 Public License Version 2 or later (the "GPL") - http://www.gnu.org/licenses/gpl-2.0.html, or
 the GNU Lesser General Public License Version 2.1 or later (the "LGPL") -
 http://www.gnu.org/licenses/lgpl-2.1.html, in which case the provisions of the GPL
 or the LGPL are applicable instead of those above. If you wish to allow use of your version
 of this file only under the terms of either the GPL or the LGPL, and not to allow others to
 use your version of this file under the terms of the MPL, indicate your decision by deleting
 the provisions above and replace them with the notice and other provisions required by the
 GPL or the LGPL. If you do not delete the provisions above, a recipient may use your version
 of this file under the terms of any one of the MPL, the GPL or the LGPL.

 Sponsors:
 The ISA Team and the ISA software suite have been funded by the EU Carcinogenomics project
 (http://www.carcinogenomics.eu), the UK BBSRC (http://www.bbsrc.ac.uk), the UK NERC-NEBC
 (http://nebc.nerc.ac.uk) and in part by the EU NuGO consortium (http://www.nugo.org/everyone).

 */

package org.isatools.gui.converter;

import org.isatools.effects.RoundedBorder;
import org.isatools.effects.UIHelper;
import org.isatools.gui.AppContainer;
import org.isatools.gui.Globals;
import org.isatools.gui.ViewingPane;
import org.isatools.isatab.gui_invokers.AllowedConversions;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * ConversionUtilGUI
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 * @date Nov 12, 2009
 */


public class ConversionUtilUI extends ViewingPane {
    private ImageIcon buttonIm;
    private ImageIcon buttonImOver;
    private AppContainer appCont;
    private Map<String, JCheckBox> conversions;

    public ConversionUtilUI(ImageIcon buttonIm, ImageIcon buttonImOver, AppContainer appCont) {
        super(appCont);
        this.buttonIm = buttonIm;
        this.buttonImOver = buttonImOver;
        this.appCont = appCont;
    }

    public void createGUI() {

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        setOpaque(false);

        JPanel conversionOutputDirPanel = new JPanel();
        conversionOutputDirPanel.setLayout(new BoxLayout(conversionOutputDirPanel, BoxLayout.LINE_AXIS));
        conversionOutputDirPanel.setOpaque(false);

        JLabel fileLocLab = UIHelper.createLabel("select output location", UIHelper.VER_12_BOLD, UIHelper.LIGHT_GREY_COLOR);
        conversionOutputDirPanel.add(fileLocLab);
        conversionOutputDirPanel.add(Box.createHorizontalStrut(10));

        final JTextField conversionOutputDir = new JTextField("conversion_output", 10);
        conversionOutputDir.setForeground(UIHelper.LIGHT_GREY_COLOR);
        conversionOutputDir.setOpaque(false);
        conversionOutputDir.setEditable(false);
        conversionOutputDir.setBorder(new EtchedBorder(UIHelper.LIGHT_GREY_COLOR, UIHelper.LIGHT_GREY_COLOR));
        conversionOutputDir.setPreferredSize(new Dimension(150, 20));

        conversionOutputDirPanel.add(conversionOutputDir);
        conversionOutputDirPanel.add(Box.createHorizontalStrut(10));

        final JLabel selectOutputDir = new JLabel(new ImageIcon(getClass().getResource("/images/common/select_file.png")));
        conversionOutputDirPanel.add(selectOutputDir);

        final JFileChooser jfc = UIHelper.createFileChooser("Choose conversion output directory", "", JFileChooser.DIRECTORIES_ONLY);

        selectOutputDir.addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent event) {
                // open file chooser!
                int returnVal = jfc.showOpenDialog(appCont);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    conversionOutputDir.setText(jfc.getSelectedFile().getAbsolutePath());
                }
            }

            public void mouseEntered(MouseEvent event) {
                selectOutputDir.setIcon(new ImageIcon(getClass().getResource("/images/common/select_file_over.png")));
            }

            public void mouseExited(MouseEvent event) {
                selectOutputDir.setIcon(new ImageIcon(getClass().getResource("/images/common/select_file.png")));
            }
        });

        add(conversionOutputDirPanel);
        add(Box.createVerticalStrut(10));
        JPanel availableConversionSelector = new JPanel();
        availableConversionSelector.setLayout(new BorderLayout());
        availableConversionSelector.setBorder(new TitledBorder(new RoundedBorder(UIHelper.LIGHT_GREEN_COLOR, 7), "available conversions", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.ABOVE_TOP, UIHelper.VER_12_BOLD, UIHelper.LIGHT_GREEN_COLOR));
        availableConversionSelector.setOpaque(false);

        JLabel information = UIHelper.createLabel("<html>you may convert the loaded isatab into the following formats</html>", UIHelper.VER_12_BOLD, UIHelper.LIGHT_GREY_COLOR);
        information.setVerticalAlignment(JLabel.TOP);
        information.setPreferredSize(new Dimension(250, 40));

        availableConversionSelector.add(information, BorderLayout.NORTH);

        JPanel allowedConversionCont = new JPanel();
        allowedConversionCont.setOpaque(false);
        allowedConversionCont.setLayout(new BoxLayout(allowedConversionCont, BoxLayout.PAGE_AXIS));

        conversions = new HashMap<String, JCheckBox>();

        ButtonGroup buttonGroup = new ButtonGroup();

        for (AllowedConversions ac : AllowedConversions.values()) {
            JPanel checkBoxContainer = new JPanel(new GridLayout(1, 1));
            checkBoxContainer.setOpaque(false);

            JCheckBox checkBox = new JCheckBox(ac.getType());
            UIHelper.renderComponent(checkBox, UIHelper.VER_12_BOLD, UIHelper.LIGHT_GREY_COLOR, false);

            conversions.put(ac.getType(), checkBox);

            buttonGroup.add(checkBox);

            checkBoxContainer.add(checkBox);
            allowedConversionCont.add(checkBoxContainer);
        }

        availableConversionSelector.add(allowedConversionCont);

        add(availableConversionSelector);

        final JLabel infoLab = UIHelper.createLabel("", UIHelper.VER_12_BOLD, UIHelper.LIGHT_GREEN_COLOR, JLabel.CENTER);

        JScrollPane infoScroller = new JScrollPane(infoLab, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        infoScroller.setBorder(new EmptyBorder(0, 0, 0, 0));
        infoScroller.setOpaque(false);
        infoScroller.getViewport().setOpaque(false);

        infoScroller.setPreferredSize(new Dimension(250, 70));

        add(infoScroller);

        // and need a convert button!
        JPanel conversionButtonCont = new JPanel(new BorderLayout());
        conversionButtonCont.setOpaque(false);

        final JLabel convertButton = new JLabel(buttonIm, JLabel.RIGHT);

        convertButton.addMouseListener(new MouseAdapter() {

            public void mouseEntered(MouseEvent mouseEvent) {
                convertButton.setIcon(buttonImOver);
            }

            public void mouseExited(MouseEvent mouseEvent) {
                convertButton.setIcon(buttonIm);
            }

            public void mousePressed(MouseEvent mouseEvent) {
                final String outputDirStr = conversionOutputDir.getText().trim().equals("") ? "conversion_output" : conversionOutputDir.getText().trim();

                firePropertyChange("doConversion", "", outputDirStr);

            }
        });
        conversionButtonCont.add(convertButton, BorderLayout.EAST);

        final JLabel backToMenu = new JLabel(Globals.BACK_MAIN);
        backToMenu.addMouseListener(new MouseAdapter() {

            public void mouseEntered(MouseEvent mouseEvent) {
                backToMenu.setIcon(Globals.BACK_MAIN_OVER);
            }

            public void mouseExited(MouseEvent mouseEvent) {
                backToMenu.setIcon(Globals.BACK_MAIN);
            }

            public void mousePressed(MouseEvent mouseEvent) {
                backToMenu.setIcon(Globals.BACK_MAIN);
                firePropertyChange("toMenu", "", "back");

            }

        });

        conversionButtonCont.add(backToMenu, BorderLayout.WEST);

        add(conversionButtonCont);
    }


    public Map<String, JCheckBox> getConversions() {
        return conversions;
    }
}
