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

package org.isatools.gui.datamanager.exportisa;

import org.apache.commons.collections15.map.ListOrderedMap;
import org.isatools.checkabletree.CheckableNode;
import org.isatools.checkabletree.CheckableTree;
import org.isatools.effects.UIHelper;
import org.isatools.gui.AppContainer;
import org.isatools.gui.datamanager.StudyOperationCommon;
import org.isatools.gui.optionselector.OptionGroup;
import org.jdesktop.fuse.InjectedResource;
import org.jdesktop.fuse.ResourceInjector;
import uk.ac.ebi.bioinvindex.model.Investigation;
import uk.ac.ebi.bioinvindex.model.Study;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * ExportISAGUI
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 * @date Mar 9, 2010
 */


public class ExportISAGUI extends StudyOperationCommon {
    // will contain a JTree containing all of the studies and investigations in the database

    @InjectedResource
    private ImageIcon panelHeader, exportISAIcon, exportISAIconOver, warningIcon;
    // A Map from either a Study Accession to null or from an investigation accession to a List of study accessions.
    private Map<String, List<String>> availableStudies;
    private Collection<Study> studies;

    private CheckableTree studyAvailabilityTree;
    private FileSelectionUtil localDirectoryLocation;
    private OptionGroup<String> fileLocationOptionGroup;
    private OptionGroup<String> dataFileExportOptionGroup;
    private JLabel errorDisplay;

    public ExportISAGUI(AppContainer container) {
        super(container);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(450, 375));

        ResourceInjector.get("gui-package.style").inject(this);
    }

    public void createGUI() {
        // header image
        add(UIHelper.wrapComponentInPanel(new JLabel(panelHeader, SwingConstants.RIGHT)), BorderLayout.NORTH);

        // add a checkable jtree with investigations & studies...
        JPanel availableSubmissionsContainer = new JPanel(new BorderLayout());
        availableSubmissionsContainer.setOpaque(false);

        JPanel optionsContainer = new JPanel();
        optionsContainer.setLayout(new BoxLayout(optionsContainer, BoxLayout.LINE_AXIS));
        optionsContainer.setOpaque(false);

        JPanel optionsAndInformationPanel = new JPanel();
        optionsAndInformationPanel.setLayout(new BoxLayout(optionsAndInformationPanel, BoxLayout.PAGE_AXIS));
        optionsAndInformationPanel.setOpaque(false);

        JLabel fileInformation = UIHelper.createLabel(
                "<html><i>where</i> to save the isatab files?</html>", UIHelper.VER_12_BOLD, UIHelper.LIGHT_GREY_COLOR);
        fileInformation.setVerticalAlignment(JLabel.TOP);

        JPanel fileLocationOptionContainer = new JPanel();
        fileLocationOptionContainer.setLayout(new BoxLayout(fileLocationOptionContainer, BoxLayout.PAGE_AXIS));
        fileLocationOptionContainer.setOpaque(false);

        // add component for selection of output to repository or output to folder...
        fileLocationOptionGroup = new OptionGroup<String>(OptionGroup.HORIZONTAL_ALIGNMENT, true);
        fileLocationOptionGroup.addOptionItem("BII repository", true);
        fileLocationOptionGroup.addOptionItem("Local file system");

        localDirectoryLocation = new FileSelectionUtil("select output directory", createFileChooser(),
                UIHelper.VER_11_BOLD, UIHelper.GREY_COLOR);
        localDirectoryLocation.setVisible(false);
        localDirectoryLocation.setPreferredSize(new Dimension(150, 20));

        fileLocationOptionGroup.addPropertyChangeListener("optionSelectionChange", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                localDirectoryLocation.setVisible(fileLocationOptionGroup.getSelectedItem().equals("Local file system"));
                revalidate();
            }
        });

        fileLocationOptionContainer.add(UIHelper.wrapComponentInPanel(fileInformation));
        fileLocationOptionContainer.add(UIHelper.wrapComponentInPanel(fileLocationOptionGroup));
        fileLocationOptionContainer.add(Box.createVerticalStrut(5));
        fileLocationOptionContainer.add(UIHelper.wrapComponentInPanel(localDirectoryLocation));

        optionsContainer.add(fileLocationOptionContainer);

        JPanel dataFileExportOptionContainer = new JPanel();
        dataFileExportOptionContainer.setLayout(new BoxLayout(dataFileExportOptionContainer, BoxLayout.PAGE_AXIS));
        dataFileExportOptionContainer.setOpaque(false);

        JLabel dataFileExportInformation = UIHelper.createLabel(
                "<html>export data files?</html>", UIHelper.VER_12_BOLD, UIHelper.LIGHT_GREY_COLOR);
        dataFileExportInformation.setVerticalAlignment(JLabel.TOP);

        dataFileExportOptionGroup = new OptionGroup<String>(OptionGroup.HORIZONTAL_ALIGNMENT, true);
        dataFileExportOptionGroup.addOptionItem("yes", true);
        dataFileExportOptionGroup.addOptionItem("no");

        dataFileExportOptionContainer.add(UIHelper.wrapComponentInPanel(dataFileExportInformation));
        dataFileExportOptionContainer.add(UIHelper.wrapComponentInPanel(dataFileExportOptionGroup));
        dataFileExportOptionContainer.add(Box.createVerticalStrut(25));

        optionsContainer.add(dataFileExportOptionContainer);

        optionsAndInformationPanel.add(Box.createVerticalStrut(5));
        optionsAndInformationPanel.add(optionsContainer);
        optionsAndInformationPanel.add(Box.createVerticalStrut(10));

        JLabel information = UIHelper.createLabel(
                "<html>please <i>select</i> the submissions to be exported...</html>", UIHelper.VER_12_BOLD, UIHelper.LIGHT_GREY_COLOR);
        information.setVerticalAlignment(JLabel.TOP);

        optionsAndInformationPanel.add(UIHelper.wrapComponentInPanel(information));
        optionsAndInformationPanel.add(Box.createVerticalStrut(5));

        availableSubmissionsContainer.add(optionsAndInformationPanel, BorderLayout.NORTH);

        retrieveAndProcessStudyInformation();

        JScrollPane treeScroller = new JScrollPane(studyAvailabilityTree, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        treeScroller.getViewport().setOpaque(false);
        treeScroller.setOpaque(false);
        treeScroller.setBorder(new EmptyBorder(1, 1, 1, 1));

        treeScroller.setPreferredSize(new Dimension(380, 250));

        availableSubmissionsContainer.add(treeScroller, BorderLayout.CENTER);
        add(availableSubmissionsContainer, BorderLayout.CENTER);
        add(createSouthPanel(), BorderLayout.SOUTH);
    }

    private JFileChooser createFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("Select directory to output to");
        fileChooser.setApproveButtonText("Output here");

        return fileChooser;
    }

    private JPanel createSouthPanel() {
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setOpaque(false);

        errorDisplay = new JLabel(warningIcon);
        UIHelper.renderComponent(errorDisplay, UIHelper.VER_11_BOLD, UIHelper.LIGHT_GREY_COLOR, false);
        errorDisplay.setVisible(false);

        southPanel.add(UIHelper.wrapComponentInPanel(errorDisplay), BorderLayout.NORTH);

        southPanel.add(createBackToMainMenuButton(), BorderLayout.WEST);

        final JLabel exportISAButton = new JLabel(exportISAIcon);
        exportISAButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                exportISAButton.setIcon(exportISAIcon);

                if (!selectedOutputFolderExists() && !exportToRepository()) {
                    localDirectoryLocation.setBackgroundToError();
                } else {
                    if (studyAvailabilityTree.getCheckedStudies(studyAvailabilityTree.getRoot()).size() == 0) {
                        errorDisplay.setText("<html>please select <i>at least one study</i> to export!</html>");
                        errorDisplay.setVisible(true);
                    } else {
                        errorDisplay.setVisible(false);
                        firePropertyChange("doISATabExport", "", "doExport");
                    }
                }
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                exportISAButton.setIcon(exportISAIcon);
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                exportISAButton.setIcon(exportISAIconOver);
            }
        });

        southPanel.add(exportISAButton, BorderLayout.EAST);

        return southPanel;
    }

    private void retrieveAndProcessStudyInformation() {
        studies = loadStudiesFromDatabase();
        System.out.println("got studies, there are  : " + studies.size() + " of them!");
        if (studies == null) {
            createDummyTree();
        } else {
            processStudies(studies);
            System.out.println("available studies size is : " + availableStudies.size() + " and content is:");
            for (String studyAcc : availableStudies.keySet()) {
                System.out.println("\t" + studyAcc);
            }
            createTree();
        }

    }

    private void processStudies(Collection<Study> studies) {
        availableStudies = new ListOrderedMap<String, List<String>>();
        for (Study study : studies) {
//			System.out.println("study investigation is : " + study.getInvestigations().size());
            if (study.getInvestigations().size() > 0) {
                for (Investigation inv : study.getInvestigations()) {
                    if (!availableStudies.containsKey(inv.getAcc())) {
                        availableStudies.put(inv.getAcc(), new ArrayList<String>());
                    }
                    availableStudies.get(inv.getAcc()).add(study.getAcc());
                }
            } else {

                availableStudies.put(study.getAcc(), null);
            }
        }
    }

    private void createDummyTree() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("  no studies found");

        DefaultTreeModel model = new DefaultTreeModel(root);
        studyAvailabilityTree = new CheckableTree(model);

    }

    private void createTree() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("  available studies");

        for (String key : availableStudies.keySet()) {
            System.out.println("adding " + key + " to tree");
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(new CheckableNode(key));

            if (availableStudies.get(key) != null) {
                for (String study : availableStudies.get(key)) {
                    CheckableNode studyNode = new CheckableNode(study);
                    node.add(new DefaultMutableTreeNode(studyNode));
                }
            }

            root.add(node);
        }

        DefaultTreeModel model = new DefaultTreeModel(root);
        studyAvailabilityTree = new CheckableTree(model);

    }

    /**
     * Returns the studies checked by the user for export to ISATab.
     *
     * @return Collection<Study> containing the Studies to be exported
     */
    public Collection<Study> getSelectedStudiesForExport() {
        Set<String> checkedNodes = studyAvailabilityTree.getCheckedStudies(studyAvailabilityTree.getRoot());

        Collection<Study> toExport = new ArrayList<Study>();
        for (String checkedNode : checkedNodes) {
            Study toAdd = getStudyByAccession(checkedNode);
            if (toAdd != null) {
                toExport.add(toAdd);
            }
        }
        return toExport;
    }

    private Study getStudyByAccession(String accession) {
        for (Study study : studies) {
            if (study.getAcc().equalsIgnoreCase(accession)) {
                return study;
            }
        }
        return null;
    }

    private boolean selectedOutputFolderExists() {
        String filePath = localDirectoryLocation.getSelectedFilePath();

        if (filePath.equals("")) {
            return false;
        } else {
            if (!new File(filePath).exists()) {
                return false;
            }
        }
        return true;
    }

    /**
     * exportToRepository()
     *
     * @return true if the user has opted to export to the repository, false if they wish to export to the local file system
     */
    public boolean exportToRepository() {
        return fileLocationOptionGroup.getSelectedItem().equals("BII repository");
    }

    public boolean exportDataFiles() {
        return dataFileExportOptionGroup.getSelectedItem().equals("yes");
    }

    public String getLocalFileDirectory() {
        return localDirectoryLocation.getSelectedFilePath();
    }

    public String generateExportSuccessMessage() {
        StringBuffer message = new StringBuffer();

        message.append("<html>files have been <b>successfully</b> output to ");

        if (exportToRepository()) {
            message.append("<b> BII repository </b>");
        } else {
            message.append("<b> ").append(localDirectoryLocation.getSelectedFilePath()).append(" </b>");
        }

        if (exportDataFiles()) {
            message.append("<b>along with</b> associated data files.");
        } else {
            message.append("<b>without</b> data files.");
        }

        message.append("</html>");

        return message.toString();
    }

    public String generateExportErrorMessage(Exception error) {
        StringBuffer message = new StringBuffer();

        message.append("<html>An error occurred during export: <p/>" +
                "<p/>" + "<b>error:</b> ").append(error.getMessage()).append("</html>");

        return message.toString();
    }
}
