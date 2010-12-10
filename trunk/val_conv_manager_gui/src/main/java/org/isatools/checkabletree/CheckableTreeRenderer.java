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

package org.isatools.checkabletree;

import org.isatools.effects.UIHelper;
import org.jdesktop.fuse.InjectedResource;
import org.jdesktop.fuse.ResourceInjector;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;

/**
 * CheckableTreeRenderer
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 * @date Mar 9, 2010
 */


public class CheckableTreeRenderer implements TreeCellRenderer {
    @InjectedResource
    private ImageIcon termNodeSelected, termNodeUnselected, rootNode, branchNodeExpandedUnselected,
            branchNodeExpandedSelected, branchNodeNotExpandedUnselected, branchNodeNotExpandedSelected,
            checked, unchecked;

    private JPanel contents;

    private JLabel expansionIndicator;
    private JLabel checkedIndicator;
    private JLabel text;

    static {

        ResourceInjector.addModule("org.jdesktop.fuse.swing.SwingModule");

        ResourceInjector.get("checkable-package.style").load(
                CheckableTreeRenderer.class.getResource("/dependency-injections/checkable-package.properties"));
    }


    public CheckableTreeRenderer() {
        ResourceInjector.get("checkable-package.style").inject(this);

        contents = new JPanel(new BorderLayout());
        contents.setOpaque(false);

        JPanel iconAndCheckContainer = new JPanel();
        iconAndCheckContainer.setLayout(new BoxLayout(iconAndCheckContainer, BoxLayout.LINE_AXIS));
        iconAndCheckContainer.setOpaque(false);

        expansionIndicator = new JLabel();
        checkedIndicator = new JLabel();
        text = UIHelper.createLabel("", UIHelper.VER_12_PLAIN, UIHelper.LIGHT_GREY_COLOR);

        iconAndCheckContainer.add(expansionIndicator);
        iconAndCheckContainer.add(checkedIndicator);

        contents.add(iconAndCheckContainer, BorderLayout.WEST);
        contents.add(text, BorderLayout.EAST);
    }

    /**
     * Sets all list values to have a white background and green foreground if not selected, and
     * a green background and white foregroud if selected.
     *
     * @param tree     - List to render
     * @param val      - value of list item being rendered.
     * @param index    - list index for value to be renderered.
     * @param selected - is the value selected?
     * @param hasFocus - has the cell got focus?
     * @return - The CustomListCellRendered Component.
     */
    public Component getTreeCellRendererComponent(JTree tree, Object val, boolean selected,
                                                  boolean expanded, boolean leaf, int index, boolean hasFocus) {
        text.setText(val.toString());

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) val;

        if (node.getUserObject() instanceof CheckableNode) {

            CheckableNode checkedNode = (CheckableNode) node.getUserObject();
            checkedIndicator.setIcon(checkedNode.isChecked() ? checked : unchecked);
            text.setForeground(checkedNode.isChecked() ? UIHelper.LIGHT_GREEN_COLOR : UIHelper.LIGHT_GREY_COLOR);
        } else {
            checkedIndicator.setIcon(rootNode);
        }

        text.setForeground(selected ? UIHelper.LIGHT_GREEN_COLOR : UIHelper.LIGHT_GREY_COLOR);

        if (leaf) {
            // leaf nodes...
            expansionIndicator.setIcon(selected ? termNodeSelected : termNodeUnselected);
        } else {
            expansionIndicator.setIcon(selected ?
                    expanded ? branchNodeExpandedSelected : branchNodeNotExpandedSelected
                    : expanded ? branchNodeExpandedUnselected : branchNodeNotExpandedUnselected);
        }

        // change text colour depending on selection
        text.setFont(leaf ? UIHelper.VER_12_BOLD : UIHelper.VER_12_PLAIN);
        return contents;
    }

}