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

import javax.swing.*;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * CheckableTree is a JTree which allows for selection/deselection of nodes in the tree.
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 * @date Nov 2, 2010
 */


public class CheckableTree extends JTree implements MouseListener {

    public CheckableTree() {
        super();
        createGUI();
    }

    public CheckableTree(TreeModel treeModel) {
        super(treeModel);
        createGUI();
    }

    public void createGUI() {
        configureTreeLook();
    }

    private void configureTreeLook() {
        setCellRenderer(new CheckableTreeRenderer());

        BasicTreeUI ui = new BasicTreeUI() {
            @Override
            public Icon getCollapsedIcon() {
                return null;
            }

            @Override
            public Icon getExpandedIcon() {
                return null;
            }

            @Override
            protected boolean getShowsRootHandles() {
                return false;
            }
        };

        setUI(ui);
        setOpaque(false);
        addMouseListener(this);
    }


    /**
     * Finds and sends back the selected studies in the JTree
     *
     * @param topNode - Root node of tree
     * @return Set<String> containing the study accessions to be removed.
     */
    public Set<String> getCheckedStudies(TreeNode topNode) {
        Set<String> studies = new HashSet<String>();

        Enumeration children = topNode.children();
        while (children.hasMoreElements()) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) children.nextElement();

            if (childNode.isLeaf()) {
                if (childNode.getUserObject() instanceof CheckableNode) {
                    CheckableNode checkableChildNode = (CheckableNode) childNode.getUserObject();

                    if (checkableChildNode.isChecked()) {
                        studies.add(checkableChildNode.toString());
                    }
                }
            } else {
                studies.addAll(getCheckedStudies(childNode));
            }
        }
        return studies;
    }


    public void mouseClicked(MouseEvent mouseEvent) {
    }

    public void mousePressed(MouseEvent mouseEvent) {

        if (!isSelectionEmpty()) {
            TreePath selPath = getPathForLocation(mouseEvent.getX(), mouseEvent.getY());

            if (selPath != null) {

                DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath.getLastPathComponent();

                if (node.getUserObject() instanceof CheckableNode) {
                    CheckableNode nodeToCheck = (CheckableNode) node.getUserObject();
                    // set checked to the inverse.
                    nodeToCheck.setChecked(!nodeToCheck.isChecked());
                    if (!node.isLeaf()) {
                        tryChangeOfChildNodes(node);
                    } else {
                        tryChangeOfParentNode(node);
                    }
                }
            }
        }
    }

    public void mouseReleased(MouseEvent mouseEvent) {
    }

    public void mouseEntered(MouseEvent mouseEvent) {
    }

    public void mouseExited(MouseEvent mouseEvent) {
    }

    private void tryChangeOfChildNodes(DefaultMutableTreeNode parentNode) {
        if (parentNode.getUserObject() instanceof CheckableNode) {
            CheckableNode nodeToCheck = (CheckableNode) parentNode.getUserObject();
            // set checked to the inverse.
            Enumeration childNodes = parentNode.children();
            while (childNodes.hasMoreElements()) {
                DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) childNodes.nextElement();

                if (childNode.getUserObject() instanceof CheckableNode) {
                    CheckableNode checkableChildNode = (CheckableNode) childNode.getUserObject();
                    // set checked to the inverse.
                    checkableChildNode.setChecked(nodeToCheck.isChecked());
                }
            }
        }
    }

    private void tryChangeOfParentNode(DefaultMutableTreeNode childNode) {
        if (childNode.getUserObject() instanceof CheckableNode) {
            CheckableNode nodeToCheck = (CheckableNode) childNode.getUserObject();
            DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) childNode.getParent();

            if (parentNode.getUserObject() instanceof CheckableNode) {
                CheckableNode checkableParentNode = (CheckableNode) parentNode.getUserObject();
                if (!nodeToCheck.isChecked()) {
                    checkableParentNode.setChecked(nodeToCheck.isChecked());
                } else {

                    boolean allChecked = true;
                    Enumeration childNodes = parentNode.children();

                    while (childNodes.hasMoreElements()) {
                        if (allChecked) {
                            DefaultMutableTreeNode child = (DefaultMutableTreeNode) childNodes.nextElement();

                            if (child.getUserObject() instanceof CheckableNode) {
                                CheckableNode checkableChildNode = (CheckableNode) child.getUserObject();
                                // set checked to the inverse.
                                allChecked = checkableChildNode.isChecked();
                            }
                        } else {
                            break;
                        }
                    }
                    checkableParentNode.setChecked(allChecked);
                }
            }
        }
    }

    public TreeNode getRoot() {
        return (TreeNode) getModel().getRoot();
    }
}
