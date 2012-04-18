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

package org.isatools.effects;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Collection;
import java.util.Vector;


public class UIHelper {

    public static final Color BG_COLOR = Color.WHITE;
    public static final Color DARK_GREEN_COLOR = new Color(0, 104, 56);
    public static final Color GREY_COLOR = new Color(51, 51, 51);
    public static final Color LIGHT_GREY_COLOR = new Color(51, 51, 51);
    public static final Color RED_COLOR = new Color(191, 30, 45);
    public static final Color TRANSPARENT_RED_COLOR = new Color(191, 30, 45, 50);
    public static final Color LIGHT_GREEN_COLOR = new Color(140, 198, 63);
    public static final Font VER_10_PLAIN = new Font("Verdana", Font.PLAIN, 10);
    public static final Font VER_10_BOLD = new Font("Verdana", Font.BOLD, 10);
    public static final Font VER_10_ITALIC = new Font("Verdana", Font.ITALIC, 10);
    public static final Font VER_11_PLAIN = new Font("Verdana", Font.PLAIN, 11);
    public static final Font VER_11_BOLD = new Font("Verdana", Font.BOLD, 11);
    public static final Font VER_11_ITALIC = new Font("Verdana", Font.ITALIC, 11);
    public static final Font VER_12_PLAIN = new Font("Verdana", Font.PLAIN, 12);
    public static final Font VER_12_BOLD = new Font("Verdana", Font.BOLD, 12);
    public static final Font VER_12_ITALIC = new Font("Verdana", Font.ITALIC, 12);
    public static final Font VER_14_PLAIN = new Font("Verdana", Font.PLAIN, 14);
    public static final Font VER_14_BOLD = new Font("Verdana", Font.BOLD, 14);
    public static final Font VER_14_ITALIC = new Font("Verdana", Font.ITALIC, 14);

    public static JLabel createLabel(String text) {
        JLabel newLab = new JLabel(text);
        newLab.setBackground(BG_COLOR);
        newLab.setFont(VER_12_BOLD);
        newLab.setForeground(DARK_GREEN_COLOR);

        return newLab;
    }

    public static JLabel createLabel(String text, Font f) {
        JLabel newLab = new JLabel(text);
        newLab.setBackground(BG_COLOR);
        newLab.setFont(f);
        newLab.setForeground(DARK_GREEN_COLOR);
        return newLab;
    }

    public static JLabel createLabel(String text, Font f, Color c) {
        return createLabel(text, f, c, JLabel.LEFT);
    }

    public static JLabel createLabel(String text, Font f, Color c, int position) {
        JLabel newLab = new JLabel(text, position);
        newLab.setBackground(BG_COLOR);
        newLab.setFont(f);
        newLab.setForeground(c);
        return newLab;
    }

    public static void renderComponent(JComponent comp, Font f, Color foregroundColor, boolean opaque) {
        comp.setForeground(foregroundColor);
        comp.setFont(f);
        comp.setBackground(BG_COLOR);
        comp.setOpaque(opaque);
    }

    public static void renderComponent(JComponent comp, Font f, Color foregroundColor, Color backgroundColor) {
        comp.setForeground(foregroundColor);
        comp.setFont(f);
        comp.setBackground(backgroundColor);
    }

    public static JOptionPane createOptionPane(String label, int messageType, Icon icon) {
        JOptionPane optionPane = new JOptionPane();

        JLabel lab = UIHelper.createLabel(label, UIHelper.VER_12_PLAIN, UIHelper.DARK_GREEN_COLOR);
        optionPane.setMessageType(messageType);
        optionPane.setMessage(lab);

        if (icon != null) {

            optionPane.setIcon(icon);
        }

        applyOptionPaneBackground(optionPane, UIHelper.BG_COLOR);

        return optionPane;
    }

    /**
     * Work around to set the optionpane color. Otherwise components only have their default values!
     *
     * @param optionPane - Optionpane to change the backgroudn color of
     * @param color      - Color to change the background to
     */
    public static void applyOptionPaneBackground(JOptionPane
            optionPane, Color color) {
        optionPane.setBackground(color);
        for (Component component : getComponents(optionPane)) {
            if (component instanceof JPanel) {
                component.setBackground(color);
            }
        }
    }

    public static void applyBackgroundToSubComponents(Container
            container, Color color) {
        container.setBackground(color);
        for (Component component : getComponents(container)) {
            if (component instanceof JPanel) {
                component.setBackground(color);
            }
        }
    }


    /**
     * Gets the components contained in a given container
     *
     * @param container - container to retrieve the components contained within
     * @return - Collection of Components.
     */
    public static Collection<Component> getComponents(Container
            container) {
        Collection<Component> components = new Vector<Component>();
        Component[] comp = container.getComponents();
        for (int i = 0, n = comp.length; i < n; i++) {
            components.add(comp[i]);
            if (comp[i] instanceof Container) {
                components.addAll(getComponents((Container) comp
                        [i]));
            }
        }
        return components;
    }


    public static JPanel wrapComponentInPanel(Component c) {
        JPanel wrapperPanel = new JPanel(new GridLayout(1, 1));
        wrapperPanel.setOpaque(false);
        wrapperPanel.add(c);

        return wrapperPanel;
    }

    public static JFileChooser createFileChooser(String dialogText, String approveButtonText, int selectionType) {
        JFileChooser jfc = new JFileChooser();
        jfc.setDialogTitle(dialogText);
        jfc.setApproveButtonText(approveButtonText);
        jfc.setFileSelectionMode(selectionType);

        return jfc;
    }

    public static Dimension getScreenSize() {

        return Toolkit.getDefaultToolkit().getScreenSize();
    }

    public static JPanel createListContainer(javax.swing.JList list, String title, ListCellRenderer renderer) {
        list.setCellRenderer(renderer);

        JScrollPane listScroller = new JScrollPane(list,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        listScroller.setBorder(new EmptyBorder(0, 0, 0, 0));
        listScroller.getViewport().setOpaque(false);
        listScroller.setOpaque(false);

        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setOpaque(false);
        listPanel.setBorder(new TitledBorder(
                new RoundedBorder(UIHelper.LIGHT_GREEN_COLOR, 4),
                title, TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION, UIHelper.VER_12_BOLD,
                UIHelper.LIGHT_GREY_COLOR));

        if (list instanceof ExtendedJList) {
            ((ExtendedJList) list).getFilterField().setBorder(new LineBorder(UIHelper.LIGHT_GREY_COLOR, 1, true));
            ((ExtendedJList) list).getFilterField().setCaretColor(UIHelper.LIGHT_GREY_COLOR);
            ((ExtendedJList) list).getFilterField().setOpaque(false);
            UIHelper.renderComponent(((ExtendedJList) list).getFilterField(), UIHelper.VER_11_BOLD, UIHelper.LIGHT_GREY_COLOR, false);

            listPanel.add(((ExtendedJList) list).getFilterField(), BorderLayout.NORTH);
        }
        listPanel.add(listScroller, BorderLayout.CENTER);
        listPanel.setPreferredSize(new Dimension(250, 200));

        return listPanel;
    }

}
