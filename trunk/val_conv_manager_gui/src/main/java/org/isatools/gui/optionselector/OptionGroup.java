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

package org.isatools.gui.optionselector;

import org.apache.commons.collections15.map.ListOrderedMap;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * OptionGroup
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 * @date Mar 17, 2010
 */


public class OptionGroup<T> extends JPanel implements MouseListener {

    public static final int HORIZONTAL_ALIGNMENT = 0;
    public static final int VERTICAL_ALIGNMENT = 1;

    private Map<T, OptionItem> availableOptions;
    private boolean singleSelection;
    private int alignment;

    /**
     * OptionGroup contains a number of items which you can either select one or many of depending on the single selection parameter
     *
     * @param alignment       - e.g. OptionGroup.HORIZONTAL_ALIGNMENT to align the options in a horizontal arrangement.
     * @param singleSelection - whether or not this group should allow singular selection (true) or multiple option selection (false)
     */
    public OptionGroup(int alignment, boolean singleSelection) {
        this.singleSelection = singleSelection;
        this.alignment = alignment;

        availableOptions = new ListOrderedMap<T, OptionItem>();

        setLayout(new BoxLayout(this, alignment == HORIZONTAL_ALIGNMENT ? BoxLayout.LINE_AXIS : BoxLayout.PAGE_AXIS));
        setOpaque(false);
    }

    public void addOptionItem(T item) {
        addOptionItem(item, false);
    }

    public void addOptionItem(T item, boolean setSelected) {
        OptionItem<T> option = new OptionItem<T>(setSelected, item);
        option.addMouseListener(this);
        if (!availableOptions.containsKey(item)) {
            availableOptions.put(item, option);
            add(option);
            add(alignment == OptionGroup.HORIZONTAL_ALIGNMENT ? Box.createHorizontalStrut(5)
                    : Box.createVerticalStrut(5));
        }
    }

    /**
     * When singleSelection is enabled, this will return the selected option. If singleSelection is not enabled and multiple
     * items can be selected, this method will return the first selected item.
     *
     * @return String - either the selected item (if singleSelection == true) or the first selected item (if singleSelection == false)
     */
    public T getSelectedItem() {
        for (T option : availableOptions.keySet()) {
            if (availableOptions.get(option).getSelectedIcon()) {
                return option;
            }
        }
        return null;
    }

    /**
     * Returns all selected items in a Set of Strings
     *
     * @return Set<T> containing all of the selected items
     */
    public Set<T> getSelectedItems() {
        Set<T> selectedItems = new HashSet<T>();
        for (T option : availableOptions.keySet()) {
            if (availableOptions.get(option).getSelectedIcon()) {
                selectedItems.add(option);
            }
        }
        return selectedItems;
    }


    // mouse listener is used to determine items clicked and so forth

    public void mouseClicked(MouseEvent mouseEvent) {
    }

    public void mouseEntered(MouseEvent mouseEvent) {
        if (mouseEvent.getSource() instanceof OptionItem) {
            final OptionItem<T> item = (OptionItem<T>) mouseEvent.getSource();
            item.setForeground(item.getOverBackgroundColor());

            final Timer[] timers = new Timer[1];
            timers[0] = new Timer(750, new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    item.setForeground(item.getNormalBackground());
                    timers[0].stop();
                }
            });

            timers[0].start();
        }
    }

    public void mouseExited(MouseEvent mouseEvent) {
        if (mouseEvent.getSource() instanceof OptionItem) {
            OptionItem<T> item = (OptionItem<T>) mouseEvent.getSource();
            item.setOpaque(false);
        }
    }

    public void mousePressed(MouseEvent mouseEvent) {
        if (mouseEvent.getSource() instanceof OptionItem) {
            OptionItem<T> item = (OptionItem<T>) mouseEvent.getSource();
            if (singleSelection) {
                if (!item.getSelectedIcon()) {
                    clearSelections();
                    item.setSelectedIcon(!item.getSelectedIcon());
                    fireItemSelectionEventToListeners();
                }
            } else {
                if (checkForSelectedItems(item)) {
                    item.setSelectedIcon(!item.getSelectedIcon());
                    fireItemSelectionEventToListeners();
                }
            }

        }
    }

    public void mouseReleased(MouseEvent mouseEvent) {
    }

    private void fireItemSelectionEventToListeners() {
        firePropertyChange("optionSelectionChange", "", "optionGroupChange");
    }

    // add method to wipe clear all selections

    private void clearSelections() {
        for (T option : availableOptions.keySet()) {
            if (availableOptions.get(option).getSelectedIcon()) {
                availableOptions.get(option).setSelectedIcon(false);
            }
        }
    }

    // add method to check if there are items selected...used to ensure that there is always at least one item selected

    private boolean checkForSelectedItems(OptionItem<T> currentItem) {
        for (T option : availableOptions.keySet()) {
            if (availableOptions.get(option).getSelectedIcon() && availableOptions.get(option) != currentItem) {
                return true;
            }
        }
        return false;
    }
}
