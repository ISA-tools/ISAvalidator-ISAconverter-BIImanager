package org.isatools.gui.datamanager.studyaccess;

import org.isatools.effects.ExtendedJList;
import org.isatools.effects.UIHelper;
import org.isatools.gui.Globals;
import org.isatools.gui.datamanager.ColumnFilterRenderer;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.List;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 18/04/2012
 *         Time: 14:35
 */
public class StudySelector extends JPanel {

    private ControlPanel controlPanel;
    private ExtendedJList availableStudiesList;
    private ExtendedJList selectedStudiesList;
    private Set<String> availableStudies;
    private Set<String> selectedStudies;
    
    private String selectedListTitle;

    public StudySelector(Set<String> availableStudies) {
        this(availableStudies, "studies to unload");
    }
    
    public StudySelector(Set<String> availableStudies, String selectedListTitle) {
        this.availableStudies = availableStudies;
        this.selectedListTitle = selectedListTitle;
        selectedStudies = new HashSet<String>();
        setPreferredSize(new Dimension(400, 250));
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        setOpaque(false);
        createGUI();
    }

    public void createGUI() {
        availableStudiesList = new ExtendedJList();
        availableStudiesList.setOpaque(false);

        populateStudiesList();
        
        selectedStudiesList = new ExtendedJList();
        selectedStudiesList.setOpaque(false);

        add(UIHelper.createListContainer(availableStudiesList, "available studies", new ColumnFilterRenderer(UIHelper.VER_11_BOLD,
                UIHelper.LIGHT_GREEN_COLOR, UIHelper.VER_11_PLAIN, UIHelper.LIGHT_GREY_COLOR,
                Globals.LIST_ICON_USER)));
        add(createControlPanel());

        // add selected user list to the UI
        add(UIHelper.createListContainer(selectedStudiesList, selectedListTitle, new ColumnFilterRenderer(UIHelper.VER_11_BOLD,
                UIHelper.LIGHT_GREEN_COLOR, UIHelper.VER_11_PLAIN, UIHelper.LIGHT_GREY_COLOR,
                Globals.LIST_ICON_USER)));
    }


    public Set<String> getSelectedStudies() {
        return selectedStudies;
    }

    private void populateStudiesList() {
        
        List<String> sortedStudies = new ArrayList<String>();
        sortedStudies.addAll(availableStudies);
        
        Collections.sort(sortedStudies);
        
        for (String study : sortedStudies) {
            availableStudiesList.addItem(study);
        }
    }

    /**
     * Update whether or not one may add or remove
     */
    private void updateButtonActivity() {
        boolean canAdd = availableStudiesList.getModel().getSize() != 0;
        controlPanel.setButtonEnabled(ControlPanel.ADD, canAdd);
        controlPanel.setButtonEnabled(ControlPanel.ADD_ALL, canAdd);

        boolean canRemove = selectedStudiesList.getModel().getSize() != 0;
        controlPanel.setButtonEnabled(ControlPanel.REMOVE, canRemove);
        controlPanel.setButtonEnabled(ControlPanel.REMOVE_ALL, canRemove);

        this.repaint();
    }

    private JPanel createControlPanel() {
        controlPanel = new ControlPanel();
        controlPanel.createGUI();

        controlPanel.addPropertyChangeListener(ControlPanel.ADD, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                if (!availableStudiesList.isSelectionEmpty()) {
                    String toAdd = availableStudiesList.getSelectedTerm();
                    selectedStudiesList.addItem(toAdd);
                    availableStudiesList.removeItem(toAdd);
                    selectedStudies.add(toAdd);
                }
                updateButtonActivity();
            }
        });

        controlPanel.addPropertyChangeListener(ControlPanel.REMOVE, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                if (!selectedStudiesList.isSelectionEmpty()) {
                    String toRemove = selectedStudiesList.getSelectedTerm();
                    selectedStudiesList.removeItem(toRemove);
                    availableStudiesList.addItem(toRemove);
                    selectedStudies.remove(toRemove);
                }
                updateButtonActivity();
            }
        });

        controlPanel.addPropertyChangeListener(ControlPanel.ADD_ALL, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                for(String availableStudy : availableStudies) {
                    if(!selectedStudies.contains(availableStudy)) {
                        selectedStudiesList.addItem(availableStudy);
                        availableStudiesList.removeItem(availableStudy);
                        selectedStudies.add(availableStudy);
                    }
                }
                updateButtonActivity();
            }
        });

        controlPanel.addPropertyChangeListener(ControlPanel.REMOVE_ALL, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                for(String selectedStudy : selectedStudies) {
                    selectedStudiesList.removeItem(selectedStudy);
                    availableStudiesList.addItem(selectedStudy);
                }
                
                selectedStudies.clear();
                updateButtonActivity();
            }
        });

        controlPanel.setButtonEnabled(ControlPanel.REMOVE, false);
        controlPanel.setButtonEnabled(ControlPanel.REMOVE_ALL, false);

        return controlPanel;
    }

    public static void main(String[] args) {
        JFrame testFrame = new JFrame("Testing list");
        
        testFrame.setBackground(UIHelper.BG_COLOR);
        
        Set<String> accessions = new HashSet<String>();
        accessions.add("bii-s-1");
        accessions.add("bii-s-2");
        accessions.add("bii-s-3");
        accessions.add("bii-s-4");
        
        testFrame.add(new StudySelector(accessions));
        testFrame.pack();
        testFrame.setVisible(true);
    }
}
