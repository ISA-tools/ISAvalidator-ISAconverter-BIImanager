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

import org.isatools.effects.ExtendedJList;
import org.isatools.effects.UIHelper;
import org.isatools.gui.AppContainer;
import org.isatools.gui.Globals;
import org.isatools.gui.ViewingPane;
import uk.ac.ebi.bioinvindex.model.VisibilityStatus;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

/**
 * StudyPrivacyUI
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 * @date Nov 16, 2009
 */


public class StudyPrivacyUI extends ViewingPane {

    private String[] studies;
    private Map<String, VisibilityStatus> studyPrivacy;
    private ExtendedJList studyList;
    private PrivacyModifier modifier;

    public StudyPrivacyUI(AppContainer container, String[] studies) {
        super(container);
        studyPrivacy = new HashMap<String, VisibilityStatus>();
        this.studies = studies;
    }

    public void createGUI() {

        setPreferredSize(new Dimension(400, 250));
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        modifier = new PrivacyModifier();

        createAndAddList();
        add(Box.createHorizontalStrut(5));
        add(modifier);

    }

    private void createAndAddList() {

        studyList = new ExtendedJList();
        studyList.setOpaque(false);
        studyList.addPropertyChangeListener("itemSelected", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                setPrivacyChoice(studyList.getSelectedTerm());
            }
        });

        for (String study : studies) {
            studyList.addItem(study);
            studyPrivacy.put(study, VisibilityStatus.PUBLIC);
        }

        if (studies.length > 0) {
            studyList.setSelectedIndex(0);
        }

        add(UIHelper.createListContainer(studyList, "studies", new StudyPrivacyCellRenderer()));

        if (!studyList.isSelectionEmpty()) {
            setPrivacyChoice(studyList.getSelectedTerm());
        }

    }

    private void setPrivacyChoice(String study) {
        modifier.setChoice(studyPrivacy.get(study));
    }

    public Map<String, VisibilityStatus> getStudyPrivacySelections() {
        return studyPrivacy;
    }

    public void setStudyPrivacySelections(Map<String, VisibilityStatus> studyPrivacy) {
        this.studyPrivacy = studyPrivacy;
    }


    protected class PrivacyModifier extends JPanel implements ActionListener {

        private JLabel label;
        private JRadioButton privateButton;
        private JRadioButton publicButton;

        PrivacyModifier() {
            createGUI();
        }

        private void createGUI() {
            setLayout(new BorderLayout());
            setOpaque(false);

            label = UIHelper.createLabel("set privacy", UIHelper.VER_14_PLAIN, UIHelper.LIGHT_GREY_COLOR, SwingConstants.LEFT);
            label.setVerticalAlignment(SwingConstants.TOP);
            add(UIHelper.wrapComponentInPanel(label), BorderLayout.NORTH);

            JPanel optionContainer = new JPanel(new FlowLayout());
            optionContainer.setOpaque(false);

            privateButton = new JRadioButton(VisibilityStatus.PRIVATE.toString(), false);
            privateButton.setVerticalAlignment(SwingConstants.BOTTOM);
            UIHelper.renderComponent(privateButton, UIHelper.VER_12_BOLD, UIHelper.LIGHT_GREY_COLOR, false);
            publicButton = new JRadioButton(VisibilityStatus.PUBLIC.toString(), true);
            UIHelper.renderComponent(publicButton, UIHelper.VER_12_BOLD, UIHelper.LIGHT_GREEN_COLOR, false);
            publicButton.setVerticalAlignment(SwingConstants.BOTTOM);

            privateButton.addActionListener(this);
            publicButton.addActionListener(this);

            ButtonGroup bg = new ButtonGroup();
            bg.add(privateButton);
            bg.add(publicButton);

            optionContainer.add(publicButton);
            optionContainer.add(privateButton);


            add(optionContainer);
        }

        protected VisibilityStatus getChoice() {
            return privateButton.isSelected() ? VisibilityStatus.PRIVATE : VisibilityStatus.PUBLIC;
        }

        public void setChoice(VisibilityStatus status) {
            if (status == VisibilityStatus.PRIVATE) {
                privateButton.setSelected(true);
                publicButton.setSelected(false);
            } else {
                privateButton.setSelected(false);
                publicButton.setSelected(true);
            }
            updateButtons();
        }

        private void updateButtons() {
            if (privateButton.isSelected()) {
                privateButton.setForeground(UIHelper.RED_COLOR);
            } else {
                privateButton.setForeground(UIHelper.LIGHT_GREY_COLOR);
            }

            if (publicButton.isSelected()) {
                publicButton.setForeground(UIHelper.LIGHT_GREEN_COLOR);
            } else {
                publicButton.setForeground(UIHelper.LIGHT_GREY_COLOR);
            }
        }

        public void actionPerformed(ActionEvent actionEvent) {
            if (actionEvent.getSource() instanceof JRadioButton) {
                updateButtons();
                if (!studyList.isSelectionEmpty()) {
                    // update the map containing the mappings.
                    studyPrivacy.put(studyList.getSelectedValue().toString(), getChoice());
                }
            }
        }
    }

    class StudyPrivacyCellRenderer extends JComponent implements ListCellRenderer {

        private Font selectedFont = UIHelper.VER_11_BOLD;
        private Font unSelectedFont = UIHelper.VER_11_PLAIN;
        private Color privateColor = UIHelper.RED_COLOR;
        private Color publicColor = UIHelper.LIGHT_GREEN_COLOR;

        private DefaultListCellRenderer listCellRenderer;

        StudyPrivacyCellRenderer() {
            setLayout(new BorderLayout());
            setOpaque(false);

            listCellRenderer = new DefaultListCellRenderer();
            listCellRenderer.setOpaque(false);
            ImageIcon image = Globals.LIST_ICON_STUDY;
            JLabel imageLab = new JLabel(image);
            add(imageLab, BorderLayout.WEST);
            add(listCellRenderer, BorderLayout.CENTER);
            setBorder(null);
        }

        public Component getListCellRendererComponent(JList jList, Object val, int index, boolean selected, boolean cellGotFocus) {
            listCellRenderer.getListCellRendererComponent(jList, val, index,
                    selected, cellGotFocus);
            listCellRenderer.setBorder(null);

            //image.setSelected(selected);
            Component[] components = getComponents();

            for (Component c : components) {

                if (selected) {
                    c.setFont(selectedFont);
                } else {
                    c.setFont(unSelectedFont);
                }

                String valString = val.toString();
                if (studyPrivacy.containsKey(valString)) {
                    if (studyPrivacy.get(valString) == VisibilityStatus.PUBLIC) {
                        c.setForeground(publicColor);
                    } else {
                        c.setForeground(privateColor);
                    }
                }
            }

            return this;
        }
    }
}
