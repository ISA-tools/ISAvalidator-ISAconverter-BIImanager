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

package org.isatools.gui;

import org.isatools.effects.UIHelper;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class SelectISATABUI extends JPanel implements MouseListener {

    private JTextField fileLocationTxt;
    private JLabel genericButton;
    private ImageIcon buttonImage;
    private ImageIcon buttonImageOver;
    private JFileChooser fileChooser;
    private ApplicationType useAs;

    public SelectISATABUI(ImageIcon buttonImage, ImageIcon buttonImageOver, JFileChooser fileChooser, ApplicationType useAs) {
        this.buttonImage = buttonImage;
        this.buttonImageOver = buttonImageOver;
        this.fileChooser = fileChooser;
        this.useAs = useAs;
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        setOpaque(false);
        instantiatePanel();
    }

    private void instantiatePanel() {

        final JPanel buttonCont = new JPanel(new BorderLayout());
        buttonCont.setOpaque(false);

        JPanel fileLocPanel = new JPanel();
        fileLocPanel.setLayout(new BoxLayout(fileLocPanel, BoxLayout.LINE_AXIS));
        fileLocPanel.setOpaque(false);

        JLabel fileLocLab = new JLabel(new ImageIcon(getClass().getResource("/images/common/isatab_location.png")));
        fileLocPanel.add(fileLocLab);
        fileLocPanel.add(Box.createHorizontalStrut(10));


        fileLocationTxt = new JTextField(10);
        fileLocationTxt.setForeground(UIHelper.LIGHT_GREY_COLOR);
        fileLocationTxt.setOpaque(false);
        fileLocationTxt.setEditable(false);
        fileLocationTxt.setBorder(new EtchedBorder(UIHelper.LIGHT_GREY_COLOR, UIHelper.LIGHT_GREY_COLOR));
        fileLocationTxt.setPreferredSize(new Dimension(150, 20));

        fileLocPanel.add(fileLocationTxt);
        fileLocPanel.add(Box.createHorizontalStrut(10));

        final JLabel selectFileLoc = new JLabel(new ImageIcon(getClass().getResource("/images/common/select_file.png")));
        fileLocPanel.add(selectFileLoc);

        selectFileLoc.addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent event) {
                // open file chooser!
                int returnVal = fileChooser.showOpenDialog(getCurrentInstance());
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    fileLocationTxt.setText(fileChooser.getSelectedFile().getAbsolutePath());
                    genericButton.setVisible(true);
                    buttonCont.revalidate();
                }
            }

            public void mouseEntered(MouseEvent event) {
                selectFileLoc.setIcon(new ImageIcon(getClass().getResource("/images/common/select_file_over.png")));
            }

            public void mouseExited(MouseEvent event) {
                selectFileLoc.setIcon(new ImageIcon(getClass().getResource("/images/common/select_file.png")));
            }
        });

        add(fileLocPanel);

        genericButton = new JLabel(buttonImage);
        genericButton.addMouseListener(this);
        genericButton.setVisible(false);


        buttonCont.add(genericButton, BorderLayout.EAST);

        if (useAs == ApplicationType.MANAGER) {
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
                    firePropertyChange("toMenu", "", "tm");
                }

            });

            buttonCont.add(backToMenu, BorderLayout.WEST);
        }

        add(Box.createVerticalStrut(30));
        add(buttonCont);
    }

    private JPanel getCurrentInstance() {
        return this;
    }

    /**
     * Check directory to determine if an investigation file exists (given naming convention of i_<<name>>.txt) <- simply a preliminary check!
     *
     * @param dir - Directory to be searched
     * @return boolean determining if the current directory contains an investigation file
     */
    private boolean checkDirectoryForISATAB(String dir) {


        File candidateFile = new File(dir);

        if (candidateFile.isDirectory()) {
            File[] directoryContents = candidateFile.listFiles();


            for (File f : directoryContents) {
                if (f.getName().toLowerCase().startsWith("i_")) {
                    return true;
                }
            }

            return false;
        } else {
            try {
                return zipPreviewForInvestigationFile(candidateFile);
            } catch (IOException e) {
                System.out.println(" problem encountered unzipping file!");
            }
        }

        return false;

    }

    public boolean zipPreviewForInvestigationFile(File f) throws IOException {
        ZipFile zf = new ZipFile(f);

        Enumeration<? extends ZipEntry> e = zf.entries();

        while (e.hasMoreElements()) {
            ZipEntry zfEntry = e.nextElement();
            System.out.println(zfEntry.getName());
            if (zfEntry.getName().toLowerCase().contains("i_")) {
                return true;
            }
        }

        return false;
    }

    public void mouseClicked(MouseEvent event) {

    }

    public void mouseEntered(MouseEvent event) {
        genericButton.setIcon(buttonImageOver);
    }

    public void mouseExited(MouseEvent event) {
        genericButton.setIcon(buttonImage);
    }

    public void mousePressed(MouseEvent event) {
        genericButton.setIcon(buttonImage);
        if (checkDirectoryForISATAB(fileLocationTxt.getText())) {
            // proceed to validation!
            firePropertyChange("doValidation", "", "new");

        } else {
            // show a message informing the user that the currently selected directory does not contain an
            // ISATAB!
            new InformationWindow(getCurrentInstance(), "<strong> | warning</strong>" +
                    "<p>the selected directory does not contain an investigation following the naming convention: " +
                    "<strong>i_name-of-investigation.txt</strong></p>");
        }
    }

    public void mouseReleased(MouseEvent event) {

    }

    public String getSelectedFile() {
        return fileLocationTxt.getText();
    }
}
