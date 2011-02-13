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

import org.isatools.gui.datamanager.ColumnFilterRenderer;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;


/**
 * The extended JList provides a way of creating a list which can be filtered using a JTextField
 *
 * @author Majority of code and concepts from Marinacci, J, Adamson, C., Swing Hacks, O'Reilly 2005.
 */
public class ExtendedJList extends JList implements ListSelectionListener {
    private FilterField filterField;
    private String currentTerm;

    public ExtendedJList() {
        this(new ColumnFilterRenderer());
    }

    public ExtendedJList(ListCellRenderer renderer) {
        super();

        setModel(new ListFilterModel());

        setCellRenderer(renderer);
        addListSelectionListener(this);
        filterField = new FilterField();

        filterField.addPropertyChangeListener("filterEvent", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                fireUpdateToListeners();
            }
        });
    }

    private void fireUpdateToListeners() {
        firePropertyChange("update", "", "none");
    }

    /**
     * Add an item to the JList
     *
     * @param s Item to add
     */
    public void addItem(Object s) {
        ((ListFilterModel) getModel()).addElement(s);
    }

    public void removeItem(Object s) {
        ((ListFilterModel) getModel()).removeElement(s);
    }

    public List<Object> getItems() {
        return ((ListFilterModel) getModel()).getItems();
    }

    public List<Object> getFilteredItems() {
        return ((ListFilterModel) getModel()).getFilterItems();
    }

    public void clearItems() {
        ((ListFilterModel) getModel()).clearItems();
    }

    /**
     * Return the filterField
     *
     * @return JTextField
     */
    public JTextField getFilterField() {
        return filterField;
    }

    /**
     * Return the selected Term
     *
     * @return String
     */
    public String getSelectedTerm() {
        return currentTerm;
    }

    /**
     * Set the JList's ListModel to be a new one
     *
     * @param lm - new ListFilterModel
     */
    public void setModel(ListFilterModel lm) {
        ListModel model = (lm == null) ? new DefaultListModel() : lm;
        super.setModel(model);
    }

    public void updateContents(Object[] newContents) {

        ((ListFilterModel) getModel()).clearItems();

        if (newContents != null) {
            for (Object s : newContents) {
                addItem(s);
            }
        }

        updateUI();
    }

    /**
     * On a ListSelectEvent, it can be assumed that a new term has been selected.
     * Therefore set the currentTerm to be the selected value as a String.
     *
     * @param event - ListSelectionEvent.
     */
    public void valueChanged(ListSelectionEvent event) {
        if (getSelectedValue() != null) {
            currentTerm = getSelectedValue().toString();
            firePropertyChange("itemSelected", "", getSelectedValue());
        }
    }

    /**
     * The FilterFields which implements the DocumentListener class. Calls updates on the JList as and
     * when modifications occur in the textfield as a result of user insertion, deletion, or update.
     */
    class FilterField extends JTextField implements DocumentListener {
        public FilterField() {
            super();
            getDocument().addDocumentListener(this);
        }

        public void changedUpdate(DocumentEvent event) {
            ((ListFilterModel) getModel()).refilter();
            firePropertyChange("filterEvent", "", "uyt");
        }

        public void insertUpdate(DocumentEvent event) {
            ((ListFilterModel) getModel()).refilterOnFilteredList();
            firePropertyChange("filterEvent", "", "sdf");
        }

        public void removeUpdate(DocumentEvent event) {
            ((ListFilterModel) getModel()).refilter();

            firePropertyChange("filterEvent", "", "hgf");
        }
    }

    /**
     * The ListFilterModel provides the logic to filter the list given the values entered in FilterField
     * textfield.
     */
    class ListFilterModel extends AbstractListModel {
        List<Object> filterItems;
        List<Object> items;

        public ListFilterModel() {
            super();
            items = new ArrayList<Object>();
            filterItems = new ArrayList<Object>();
        }

        /**
         * Add an element to the list of items, and then refilter the list in case the new item is being shown when it
         * shouldn't be.
         *
         * @param s - String to be added
         */
        public void addElement(Object s) {
            items.add(s);
            refilter();
        }

        public void removeElement(Object o) {
            items.remove(o);
            refilter();
        }

        public List<Object> getFilterItems() {
            return filterItems;
        }

        public List<Object> getItems() {
            return items;
        }

        /**
         * Clear the items in the list
         */
        public void clearItems() {
            items.clear();
        }

        /**
         * Get the element at an int i in the list
         *
         * @param i - index of item to fetch.
         * @return String if the item exists, null otherwise.
         */
        public Object getElementAt(int i) {

            if (i < filterItems.size()) {
                return filterItems.get(i);
            }


            return null;
        }

        /**
         * Get the index in the list for a specific item.
         *
         * @param item - item to search for.
         * @return Integer - index of item if it exists, -1 if it doesn't.
         */
        public int getIndexForItem(String item) {
            int itemSize = items.size();

            Object[] itemsAsArray = items.toArray(new Object[items.size()]);

            for (int i = 0; i < itemSize; i++) {
                if (itemsAsArray[i].toString().equalsIgnoreCase(item)) {
                    return i;
                }
            }

            return -1;
        }

        /**
         * Get the size of the filterItems list.
         *
         * @return Integer - the size of the list.
         */
        public int getSize() {
            return filterItems.size();
        }

        /**
         * Refilter method clears the previously filtered items and then using the value typed into
         * the FilterField JTextField, items which contain the value typed into the field are added to
         * the filterItems list of terms.
         */
        public void refilter() {
            filterItems.clear();

            String term = getFilterField().getText().toLowerCase();

            for (Object s : items) {
                if (s.toString().toLowerCase().contains(term)) {
                    filterItems.add(s);
                }

                fireContentsChanged(this, 0, getSize());
                clearSelection();
            }

        }

        /**
         * Slight performance enhancement is instead of performing a complete refilter everytime, perform
         * a filter on the filtered items, whose size is inevitable going to be less, but at worse equal to the
         * the already filtered list.
         */
        public void refilterOnFilteredList() {
            String term = getFilterField().getText().toLowerCase();
            List<Object> toRemove = new ArrayList<Object>();

            for (Object s : filterItems) {
                if (!s.toString().toLowerCase().contains(term)) {
                    toRemove.add(s);
                }
            }

            // items are removed after. otherwise there is concurrent access on the ArrayList, which is
            // not Thread safe.
            for (Object lo : toRemove) {
                filterItems.remove(lo);
            }

            fireContentsChanged(this, 0, getSize());
            clearSelection();
        }
    }
}
