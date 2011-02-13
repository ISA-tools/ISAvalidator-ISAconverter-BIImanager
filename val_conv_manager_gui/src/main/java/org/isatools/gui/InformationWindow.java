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
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class InformationWindow extends JWindow {
    private String message;
    private JEditorPane resultInfo;

    public InformationWindow(final Container parent, String message) {
        this.message = message;
        setLayout(new BorderLayout());
        setAlwaysOnTop(true);
        setBackground(new Color(51, 51, 51, 0));

        setLocationRelativeTo(parent);
        int WIDTH = 200;
        int HEIGHT = 228;
        setLocation(((parent.getX() + parent.getWidth()) / 2) - WIDTH / 2, ((parent.getY() + parent.getHeight()) / 2) - HEIGHT / 2);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createGUI();
                requestFocusInWindow();
            }
        });
    }

    public void createGUI() {
        createFrame();
        pack();
        setVisible(true);
        requestFocus();
    }

    private void createFrame() {
        JLabel topLabel = new JLabel(new ImageIcon(getClass().getResource("/images/common/info_top.png")));

        topLabel.addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent event) {
                setVisible(false);
                dispose();
            }
        });

        add(topLabel, BorderLayout.NORTH);
        add(new JLabel(new ImageIcon(getClass().getResource("/images/common/info_bottom.png"))), BorderLayout.SOUTH);
        add(createInfoPanel(), BorderLayout.CENTER);
    }

    private JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.LINE_AXIS));
        infoPanel.setPreferredSize(new Dimension(200, 150));
        infoPanel.setBackground(UIHelper.LIGHT_GREY_COLOR);

        resultInfo = new JEditorPane();
        resultInfo.setContentType("text/html");
        resultInfo.setEditable(false);
        resultInfo.setOpaque(false);

        showMessage(message);

        JScrollPane listScroller = new JScrollPane(resultInfo, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        listScroller.setBorder(null);
        listScroller.getViewport().setOpaque(false);
        listScroller.setOpaque(false);
        listScroller.setPreferredSize(new Dimension(170, 140));

        infoPanel.add(Box.createHorizontalStrut(5));

        infoPanel.add(listScroller);

        infoPanel.add(Box.createHorizontalStrut(5));

        return infoPanel;

    }

    private void showMessage(String errorMessage) {

        String header = "<html>" + "<head>" +
                "<style type=\"text/css\">" + "<!--" + ".bodyFont {" +
                "   font-family: Verdana;" + "   font-size: 9px;" +
                "   color: #414042;" + "}" + "-->" + "</style>" + "</head>" +
                "<body class=\"bodyFont\">";

        StringBuffer result = new StringBuffer();
        result.append(header);
        result.append("<div align=\"left" +
                "\">").append(errorMessage).append("</div>");

        result.append("</body></html>");


        resultInfo.setText(result.toString());
        resultInfo.setCaretPosition(0);

    }
}
