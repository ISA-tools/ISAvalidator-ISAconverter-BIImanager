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

import org.isatools.effects.UIHelper;
import org.isatools.gui.AppContainer;
import org.isatools.gui.Globals;
import org.isatools.gui.ViewingPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * StudyAccessibilityModification
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 * @date Nov 16, 2009
 */


public class StudyAccessibilityModificationUI extends ViewingPane {

    private static final int OWNERSHIP = 0;
    private static final int PRIVACY = 1;

    private String[] studies;
    private String[] users;

    private JLabel studyPrivacyItem;
    private JLabel studyUserItem;

    private StudyOwnershipUI studyOwnership;
    private StudyPrivacyUI studyPrivacy;

    private JPanel swappableUIContainer;

    private int currentlySelectedPane;


    public StudyAccessibilityModificationUI(AppContainer container, String[] studies, String[] users) {
        super(container);
        this.studies = studies;
        this.users = users;
        swappableUIContainer = new JPanel();
        swappableUIContainer.setOpaque(false);
    }

    /**
     * We'll be creating a tabbed pane to include the StudyOwnershipUI & the StudyPrivacyUI
     */
    public void createGUI(boolean showBack) {
        setLayout(new BorderLayout());
        setOpaque(false);

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setOpaque(false);
        northPanel.add(new JLabel(new ImageIcon(getClass().getResource("/images/DataManager/study_management_header.png")), SwingConstants.RIGHT), BorderLayout.EAST);

        northPanel.add(createMenuPanel(), BorderLayout.SOUTH);

        add(northPanel, BorderLayout.NORTH);

        studyOwnership = new StudyOwnershipUI(container, studies, users);
        studyOwnership.createGUI();

        studyPrivacy = new StudyPrivacyUI(container, studies);
        studyPrivacy.createGUI();

        swappableUIContainer.add(studyOwnership);
        currentlySelectedPane = OWNERSHIP;

        updateButtonUI();

        add(swappableUIContainer, BorderLayout.CENTER);

        JPanel buttonCont = new JPanel(new BorderLayout());
        buttonCont.setOpaque(false);

        if (showBack) {

            final JLabel backMenu = new JLabel(Globals.BACK_MAIN, JLabel.LEFT);

            backMenu.addMouseListener(new MouseAdapter() {

                public void mousePressed(MouseEvent event) {
                    backMenu.setIcon(Globals.BACK_MAIN);
                    firePropertyChange("doShowMenu", "", "privacy");
                }

                public void mouseEntered(MouseEvent event) {
                    backMenu.setIcon(Globals.BACK_MAIN_OVER);
                }

                public void mouseExited(MouseEvent event) {
                    backMenu.setIcon(Globals.BACK_MAIN);
                }
            });

            buttonCont.add(backMenu, BorderLayout.WEST);
        }

        final JLabel setPermission = new JLabel(Globals.SET_PERMISSION);
        setPermission.addMouseListener(new MouseAdapter() {

            public void mouseEntered(MouseEvent mouseEvent) {
                setPermission.setIcon(Globals.SET_PERMISSION_OVER);
            }

            public void mouseExited(MouseEvent mouseEvent) {
                setPermission.setIcon(Globals.SET_PERMISSION);
            }

            public void mousePressed(MouseEvent mouseEvent) {
                setPermission.setIcon(Globals.SET_PERMISSION);
                firePropertyChange("doPrivacySettings", "", "privacy");
            }

        });

        buttonCont.add(setPermission, BorderLayout.EAST);

        add(buttonCont, BorderLayout.SOUTH);

    }

    private void updateButtonUI() {

        if (currentlySelectedPane == OWNERSHIP) {
            studyUserItem.setForeground(UIHelper.LIGHT_GREEN_COLOR);
            studyPrivacyItem.setForeground(UIHelper.LIGHT_GREY_COLOR);
        } else if (currentlySelectedPane == PRIVACY) {
            studyUserItem.setForeground(UIHelper.LIGHT_GREY_COLOR);
            studyPrivacyItem.setForeground(UIHelper.LIGHT_GREEN_COLOR);
        }
    }

    private JPanel createMenuPanel() {
        JPanel menuPanel = new JPanel(new FlowLayout());
        menuPanel.setOpaque(false);

        studyUserItem = createMenuLabel("modify ownership", new ImageIcon(getClass().getResource("/images/DataManager/tab_user_study_assoc.png")));
        studyUserItem.addMouseListener(new MouseAdapter() {

            public void mouseEntered(MouseEvent mouseEvent) {
                studyUserItem.setForeground(UIHelper.LIGHT_GREEN_COLOR);
            }

            public void mouseExited(MouseEvent mouseEvent) {
                studyUserItem.setForeground(setExitColour(StudyAccessibilityModificationUI.OWNERSHIP));
            }

            public void mousePressed(MouseEvent mouseEvent) {
                changeContentPane(studyOwnership);
            }

        });

        studyPrivacyItem = createMenuLabel("modify privacy", new ImageIcon(getClass().getResource("/images/DataManager/tab_study_privacy.png")));
        studyPrivacyItem.addMouseListener(new MouseAdapter() {

            public void mouseEntered(MouseEvent mouseEvent) {
                studyPrivacyItem.setForeground(UIHelper.LIGHT_GREEN_COLOR);
            }

            public void mouseExited(MouseEvent mouseEvent) {
                studyPrivacyItem.setForeground(setExitColour(StudyAccessibilityModificationUI.PRIVACY));
            }

            public void mousePressed(MouseEvent mouseEvent) {
                changeContentPane(studyPrivacy);
            }

        });

        menuPanel.add(studyUserItem);
        menuPanel.add(studyPrivacyItem);

        return menuPanel;
    }

    private Color setExitColour(int type) {
        if (currentlySelectedPane == type) {
            return UIHelper.LIGHT_GREEN_COLOR;
        }
        return UIHelper.LIGHT_GREY_COLOR;

    }

    private JLabel createMenuLabel(String itemName, ImageIcon itemImage) {
        JLabel lab = new JLabel(itemName, itemImage, SwingConstants.LEFT);
        UIHelper.renderComponent(lab, UIHelper.VER_12_BOLD, UIHelper.LIGHT_GREY_COLOR, false);

        return lab;
    }

    private void changeContentPane(final Container pane) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (pane instanceof StudyOwnershipUI) {
                    currentlySelectedPane = StudyAccessibilityModificationUI.OWNERSHIP;
                } else if (pane instanceof StudyPrivacyUI) {
                    currentlySelectedPane = StudyAccessibilityModificationUI.PRIVACY;
                }

                swappableUIContainer.removeAll();
                swappableUIContainer.add(pane);
                swappableUIContainer.revalidate();
                swappableUIContainer.repaint();
                updateButtonUI();
            }
        });
    }

    public StudyOwnershipUI getStudyOwnership() {
        return studyOwnership;
    }

    public StudyPrivacyUI getStudyPrivacy() {
        return studyPrivacy;
    }

}
