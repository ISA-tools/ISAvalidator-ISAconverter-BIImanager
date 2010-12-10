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

package org.isatools.gui.errorprocessing;

import org.isatools.effects.UIHelper;
import org.jdesktop.fuse.InjectedResource;
import org.jdesktop.fuse.ResourceInjector;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * ErrorInformationUI
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 * @date Mar 19, 2010
 */


public class ErrorInformationUI extends JPanel {

    @InjectedResource
    private ImageIcon warningIcon, errorIcon;
    // icon to indicate whether this is an error or a warning
    private JLabel typeIcon;

    private JLabel type;
    private JTextArea message;

    public ErrorInformationUI() {
        ResourceInjector.get("gui-package.style").inject(this);

        createGUI();
    }

    private void createGUI() {
        setLayout(new BorderLayout());
        setOpaque(false);

        JPanel topPanel = new JPanel();
        topPanel.setOpaque(false);
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.LINE_AXIS));

        typeIcon = new JLabel();
        topPanel.add(typeIcon);
        topPanel.add(Box.createHorizontalStrut(10));

        type = UIHelper.createLabel("", UIHelper.VER_14_PLAIN, UIHelper.LIGHT_GREY_COLOR);
        topPanel.add(type);

        add(topPanel, BorderLayout.NORTH);

        message = new JTextArea("");
        UIHelper.renderComponent(message, UIHelper.VER_12_PLAIN, UIHelper.LIGHT_GREY_COLOR, false);
        message.setEditable(false);
        message.setWrapStyleWord(true);
        message.setLineWrap(true);

        JScrollPane messageScroller = new JScrollPane(message, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        messageScroller.setBorder(new EmptyBorder(1, 1, 1, 1));
        messageScroller.getViewport().setOpaque(false);
        messageScroller.setOpaque(false);

        messageScroller.setPreferredSize(new Dimension(370, 80));

        add(messageScroller, BorderLayout.CENTER);
    }

    public void updateView(String errorOrWarning, String errorOrWarningMessage, String location) {

        typeIcon.setIcon(errorOrWarning.equals(ValidationError.WARNING) ? warningIcon : errorIcon);
        type.setText(errorOrWarning + " in file " + location);
        message.setText(errorOrWarningMessage);

        revalidate();
        repaint();
    }
}
