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

package org.isatools.gui.datamanager.exportisa;

import org.isatools.effects.CustomRoundedBorder;
import org.isatools.effects.UIHelper;
import org.jdesktop.fuse.InjectedResource;
import org.jdesktop.fuse.ResourceInjector;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * FileSelectionUtil
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 * @date Mar 17, 2010
 */


public class FileSelectionUtil extends JPanel {

    public static final int OPEN = 0;
    public static final int SAVE = 1;

    @InjectedResource
    private ImageIcon selectFileIcon, selectFileIconOver, warningIcon;

    private JLabel warningIndicator;
    private JTextField fileToUse;
    private String text;
    private JFileChooser fileChooser;
    private Font textFont;
    private Color textColor;


    public FileSelectionUtil(String text, JFileChooser fileChooser, Font textFont, Color textColor) {
        this.text = text;

        if (fileChooser == null) {
            this.fileChooser = new JFileChooser();
        } else {
            this.fileChooser = fileChooser;
        }

        this.textFont = textFont;
        this.textColor = textColor;

        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        setOpaque(false);

        ResourceInjector.get("optionselector-package.style").inject(this);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createGUI();
            }
        });
    }

    private void createGUI() {

//		add(UIHelper.createLabel(text, textFont, textColor));

        // create field for viewing the file location selected (uneditable) and the label representing a file selection action
        JPanel fileSelectionUtil = new JPanel();
        fileSelectionUtil.setOpaque(false);
        fileSelectionUtil.setLayout(new BoxLayout(fileSelectionUtil, BoxLayout.LINE_AXIS));

        warningIndicator = new JLabel("", warningIcon, JLabel.RIGHT);
        warningIndicator.setVisible(false);

        fileSelectionUtil.add(warningIndicator);

        fileToUse = new JTextField(text);
        fileToUse.setEditable(false);
        fileToUse.setBorder(new CustomRoundedBorder(UIHelper.LIGHT_GREY_COLOR, false));
        UIHelper.renderComponent(fileToUse, textFont, textColor, UIHelper.LIGHT_GREY_COLOR);

        fileSelectionUtil.add(fileToUse);

        final JLabel selectFileButton = new JLabel(selectFileIcon);
        selectFileButton.addMouseListener(new MouseAdapter() {

            public void mouseEntered(MouseEvent event) {
                selectFileButton.setIcon(selectFileIconOver);
            }

            public void mouseExited(MouseEvent event) {
                selectFileButton.setIcon(selectFileIcon);
            }

            public void mousePressed(MouseEvent event) {
                selectFileButton.setIcon(selectFileIcon);

                if (fileChooser.showOpenDialog(getInstance()) == JFileChooser.APPROVE_OPTION
                        && fileChooser.getSelectedFile() != null) {
                    fileToUse.setText(fileChooser.getSelectedFile().getPath());
                }
            }
        });
        fileSelectionUtil.add(selectFileButton);
        add(fileSelectionUtil);
    }

    public void setBackgroundToError() {
        // sets the background to the transparent red factor
        fileToUse.setBackground(new Color(255, 222, 23));
        fileToUse.setForeground(UIHelper.GREY_COLOR);
        fileToUse.setBorder(new LineBorder(UIHelper.LIGHT_GREY_COLOR, 1));
        fileToUse.setText("please choose a valid directory");
        warningIndicator.setVisible(true);
    }

    public String getSelectedFilePath() {
        return fileToUse.getText();
    }

    private JPanel getInstance() {
        return this;
    }
}