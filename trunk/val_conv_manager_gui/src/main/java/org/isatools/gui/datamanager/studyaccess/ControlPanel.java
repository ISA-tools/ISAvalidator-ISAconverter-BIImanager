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

package org.isatools.gui.datamanager.studyaccess;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * ControlPanel
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 * @date Nov 17, 2009
 */


public class ControlPanel extends JPanel {

    private static final ImageIcon ADD_ICON = new ImageIcon(ControlPanel.class.getResource("/images/DataManager/add-icon.png"));
    private static final ImageIcon ADD_ICON_OVER = new ImageIcon(ControlPanel.class.getResource("/images/DataManager/add-icon-over.png"));

    private static final ImageIcon ADD_ICON_ALL = new ImageIcon(ControlPanel.class.getResource("/images/DataManager/add-all-icon.png"));
    private static final ImageIcon ADD_ICON_ALL_OVER = new ImageIcon(ControlPanel.class.getResource("/images/DataManager/add-all-icon-over.png"));

    private static final ImageIcon REMOVE_ICON = new ImageIcon(ControlPanel.class.getResource("/images/DataManager/remove-icon.png"));
    private static final ImageIcon REMOVE_ICON_OVER = new ImageIcon(ControlPanel.class.getResource("/images/DataManager/remove-icon-over.png"));

    private static final ImageIcon REMOVE_ICON_ALL = new ImageIcon(ControlPanel.class.getResource("/images/DataManager/remove-all-icon.png"));
    private static final ImageIcon REMOVE_ICON_ALL_OVER = new ImageIcon(ControlPanel.class.getResource("/images/DataManager/remove-all-icon-over.png"));

    public static final String ADD = "add";
    public static final String REMOVE = "remove";
    public static final String ADD_ALL = "add-all";
    public static final String REMOVE_ALL = "remove-all";

    private JLabel add;
    private JLabel remove;
    private JLabel addAll;
    private JLabel removeAll;

    public ControlPanel() {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        setOpaque(false);
    }

    public void createGUI() {

        add = new JLabel(ADD_ICON);
        add.addMouseListener(createMouseListenerForLabel(add, ADD_ICON, ADD_ICON_OVER, ADD));

        remove = new JLabel(REMOVE_ICON);
        remove.addMouseListener(createMouseListenerForLabel(remove, REMOVE_ICON, REMOVE_ICON_OVER, REMOVE));

        addAll = new JLabel(ADD_ICON_ALL);
        addAll.addMouseListener(createMouseListenerForLabel(addAll, ADD_ICON_ALL, ADD_ICON_ALL_OVER, ADD_ALL));

        removeAll = new JLabel(REMOVE_ICON_ALL);
        removeAll.addMouseListener(createMouseListenerForLabel(removeAll, REMOVE_ICON_ALL, REMOVE_ICON_ALL_OVER, REMOVE_ALL));


        add(add);
        add(Box.createVerticalStrut(5));
        add(remove);
        add(Box.createVerticalStrut(5));
        add(addAll);
        add(Box.createVerticalStrut(5));
        add(removeAll);

    }

    /**
     * Set a button to be enabled or not.
     *
     * @param type    - button type: @see ControlPanel.ADD, ControlPanel.REMOVE, ControlPanel.ADD_ALL, ContolPanel.REMOVE_ALL
     * @param enabled - whether or not the button should be enabled
     */
    public void setButtonEnabled(String type, boolean enabled) {
        if (type.equals(ADD)) {
            add.setEnabled(enabled);
        } else if (type.equals(ADD_ALL)) {
            addAll.setEnabled(enabled);
        } else if (type.equals(REMOVE)) {
            remove.setEnabled(enabled);
        } else if (type.equals(REMOVE_ALL)) {
            removeAll.setEnabled(enabled);
        }
    }

    private MouseListener createMouseListenerForLabel(final JLabel label, final ImageIcon icon,
                                                      final ImageIcon icon_over, final String eventCall) {

        return new MouseAdapter() {

            public void mouseEntered(MouseEvent mouseEvent) {
                if (label.isEnabled()) {
                    label.setIcon(icon_over);
                }
            }

            public void mouseExited(MouseEvent mouseEvent) {
                if (label.isEnabled()) {
                    label.setIcon(icon);
                }
            }

            public void mousePressed(MouseEvent mouseEvent) {
                if (label.isEnabled()) {
                    label.setIcon(icon);
                    firePropertyChange(eventCall, "", "event");
                }
            }
        };
    }
}
