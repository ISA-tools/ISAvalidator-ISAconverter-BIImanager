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

package org.isatools.fileutils;

import org.isatools.effects.UIHelper;
import org.isatools.gui.TitlePanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * FileViewer presents a text file in an easy to use interface for display only, no editing.
 * It's main purpose is for the presentation of the log file in a cross platform environment!
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 * @date Oct 27, 2009
 */


public class FileViewer extends JFrame {
    private File filePath;

    public FileViewer(File filePath) {
        this.filePath = filePath;
    }

    public void createGUI() {
        TitlePanel titlePanel = new FileViewerTitlePanel();

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(450, 400));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setUndecorated(true);

        add(titlePanel, BorderLayout.NORTH);
        titlePanel.installListeners();

        add(createDisplay(), BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new GridLayout(1, 1));
        southPanel.setPreferredSize(new Dimension(450, 20));
        add(southPanel, BorderLayout.SOUTH);

        pack();
        setVisible(true);
    }

    private JPanel createDisplay() {

        JPanel container = new JPanel();

        container.setPreferredSize(new Dimension(450, getContentPane().getHeight() - 35));
        container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));
        container.setBackground(Color.WHITE);

        JLabel viewingInfoLab = UIHelper.createLabel("viewing " + filePath.getName(), UIHelper.VER_12_BOLD, UIHelper.GREY_COLOR, SwingConstants.RIGHT);
        viewingInfoLab.setVerticalAlignment(SwingUtilities.TOP);

        container.add(UIHelper.wrapComponentInPanel(viewingInfoLab));

        JEditorPane viewer = new JEditorPane();
        viewer.setEditable(false);

        try {
            viewer.read(new FileInputStream(filePath), "log file");
        } catch (IOException e) {
            viewer.setText("unable to process file...\n" + e.getMessage());
        }
        viewer.setFont(UIHelper.VER_11_PLAIN);
        viewer.setForeground(UIHelper.GREY_COLOR);

        JScrollPane viewScroller = new JScrollPane(viewer, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        viewScroller.setBorder(new EmptyBorder(0, 0, 0, 0));
        viewScroller.getViewport().setBackground(Color.white);

        container.add(viewScroller);

        return container;
    }


    class FileViewerTitlePanel extends TitlePanel {
        private Image grip = new ImageIcon(getClass().getResource("/images/common/log_grip_active.png")).getImage();
        private Image inactiveGrip = new ImageIcon(getClass().getResource("/images/common/log_grip_inactive.png")).getImage();
        private Image title = new ImageIcon(getClass().getResource("/images/common/log_file_title_active.png")).getImage();

        protected void drawGrip(Graphics2D g2d, boolean active) {
            g2d.drawImage(active ? grip : inactiveGrip, 0, 0, null);

        }

        protected void drawTitle(Graphics2D g2d) {
            g2d.drawImage(title, getWidth() / 2 - title.getWidth(null) / 2, 0, null);
        }
    }
}
