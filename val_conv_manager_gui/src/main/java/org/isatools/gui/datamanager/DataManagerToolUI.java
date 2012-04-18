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

package org.isatools.gui.datamanager;

import org.isatools.effects.SmallLoader;
import org.isatools.gui.*;
import org.isatools.gui.datamanager.studyaccess.*;
import org.isatools.tablib.exceptions.TabException;
import org.isatools.tablib.utils.BIIObjectStore;
import org.jdesktop.fuse.InjectedResource;
import org.jdesktop.fuse.ResourceInjector;
import uk.ac.ebi.bioinvindex.model.Study;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

/**
 * InterfaceUtils
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 * @date Nov 25, 2009
 */


public class DataManagerToolUI extends CommonUI {

    @InjectedResource
    private ImageIcon unloadingSuccess, unloadingFailure, loadButton, loadButtonOver;

    private StudyAccessionGUI studyAccessionGUI;
    private StudyAccessibilityModificationUI studyPermissionManagement;

    private JPanel loaderMenu;
    private final Reindexer reindexer = new Reindexer(this);
    private final DBLoader dbLoader = new DBLoader(this);
    private final PermissionManagement permissionManagement = new PermissionManagement(this);
    private final ExportISATab exportISATab = new ExportISATab(this);

    public DataManagerToolUI(final AppContainer appContainer, ApplicationType useAs, String[] options) {
        super(appContainer, useAs, options);
        ResourceInjector.get("datamanager-package.style").inject(this);
    }

    protected void createLoaderMenu() {

        loaderMenu = new JPanel();
        loaderMenu.setLayout(new GridLayout(2, 1));
        loaderMenu.setOpaque(false);

        final JLabel loadIsatabButton = new JLabel(Globals.LOAD_ISATAB);
        loadIsatabButton.setToolTipText("<html><strong>Load ISAtab/ISArchive</strong>" +
                "<p>Load ISAtab files or an ISAarchive into the BII</p></html>");
        final JLabel unloadStudyButton = new JLabel(Globals.UNLOAD_STUDY);
        unloadStudyButton.setToolTipText("<html><strong>Unload Studies</strong>" +
                "<p>Unload a study or multiple studies from the database</p></html>");
        final JLabel securityButton = new JLabel(Globals.SECURITY);
        securityButton.setToolTipText("<html><strong>Security settings</strong>" +
                "<p>Modify <strong>study ownership<strong> and study <strong>visibility<strong></p></html>");
        final JLabel exportISAtabButton = new JLabel(Globals.EXPORT_ISA);
        exportISAtabButton.setToolTipText("<html><strong>Export ISAtab</strong>" +
                "<p>Export studies along with data files (if you want to) from the database to ISAtab</p></html>");
        final JLabel reindexButton = new JLabel(Globals.REINDEX);
        reindexButton.setToolTipText("<html><strong>Reindex database</strong>" +
                "<p>Recreate the Lucene index from the database contents</p></html>");

        MouseAdapter buttonMouseListener = new MouseAdapter() {

            public void mousePressed(MouseEvent mouseEvent) {

                if (mouseEvent.getSource() == loadIsatabButton) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            buttonImage = loadButton;
                            buttonImageOver = loadButtonOver;
                            instantiateLoadIsaPanel();
                            loadIsatabButton.setIcon(Globals.LOAD_ISATAB);
                            appContainer.setGlassPanelContents(selectISATABUI);
                        }
                    });

                } else if (mouseEvent.getSource() == unloadStudyButton) {

                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            unloadStudyButton.setIcon(Globals.UNLOAD_STUDY);
                            try {

                                studyAccessionGUI = new StudyAccessionGUI(appContainer);
                                studyAccessionGUI.addPropertyChangeListener("unloadSuccessful", new PropertyChangeListener() {
                                    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                                        appContainer.setGlassPanelContents(createResultPanel(unloadingSuccess, Globals.BACK_MAIN, Globals.BACK_MAIN_OVER, Globals.EXIT, Globals.EXIT_OVER, propertyChangeEvent.getNewValue().toString()));
                                        appContainer.validate();
                                    }
                                });

                                studyAccessionGUI.addPropertyChangeListener("errorOccurred", new PropertyChangeListener() {
                                    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                                        appContainer.setGlassPanelContents(createResultPanel(unloadingFailure, Globals.UNLOAD_ANOTHER, Globals.UNLOAD_ANOTHER_OVER, Globals.EXIT, Globals.EXIT_OVER, "<p>error when unloading study: </p><p>" + propertyChangeEvent.getNewValue() + "</p>"));
                                        appContainer.validate();
                                    }
                                });

                                studyAccessionGUI.addPropertyChangeListener("toMenu", new PropertyChangeListener() {
                                    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                                        appContainer.setGlassPanelContents(loaderMenu);
                                        appContainer.validate();
                                    }
                                });


                                appContainer.setGlassPanelContents(studyAccessionGUI);
                            }
                            catch (TabException ex) {
                                System.out.println("tab exception encountered : " + ex.getMessage());
                            }
                        }
                    });

                } else if (mouseEvent.getSource() == securityButton) {
                    securityButton.setIcon(Globals.SECURITY);
                    permissionManagement.createSecurityChangeInterface();

                } else if (mouseEvent.getSource() == exportISAtabButton) {
                    exportISAtabButton.setIcon(Globals.EXPORT_ISA);
                    exportISATab.createExportISATabInterface();
                } else if (mouseEvent.getSource() == reindexButton) {
                    reindexButton.setIcon(Globals.REINDEX);
                    reindexer.performReindexing();

                }
            }

            public void mouseEntered(MouseEvent mouseEvent) {
                if (mouseEvent.getSource() == loadIsatabButton) {
                    loadIsatabButton.setIcon(Globals.LOAD_ISATAB_OVER);
                } else if (mouseEvent.getSource() == securityButton) {
                    securityButton.setIcon(Globals.SECURITY_OVER);
                } else if (mouseEvent.getSource() == unloadStudyButton) {
                    unloadStudyButton.setIcon(Globals.UNLOAD_STUDY_OVER);
                } else if (mouseEvent.getSource() == exportISAtabButton) {
                    exportISAtabButton.setIcon(Globals.EXPORT_ISA_OVER);
                } else if (mouseEvent.getSource() == reindexButton) {
                    reindexButton.setIcon(Globals.REINDEX_OVER);
                }
            }

            public void mouseExited(MouseEvent mouseEvent) {
                if (mouseEvent.getSource() == loadIsatabButton) {
                    loadIsatabButton.setIcon(Globals.LOAD_ISATAB);
                } else if (mouseEvent.getSource() == securityButton) {
                    securityButton.setIcon(Globals.SECURITY);
                } else if (mouseEvent.getSource() == unloadStudyButton) {
                    unloadStudyButton.setIcon(Globals.UNLOAD_STUDY);
                } else if (mouseEvent.getSource() == exportISAtabButton) {
                    exportISAtabButton.setIcon(Globals.EXPORT_ISA);
                } else if (mouseEvent.getSource() == reindexButton) {
                    reindexButton.setIcon(Globals.REINDEX);
                }
            }


        };
        loadIsatabButton.addMouseListener(buttonMouseListener);
        unloadStudyButton.addMouseListener(buttonMouseListener);
        securityButton.addMouseListener(buttonMouseListener);
        exportISAtabButton.addMouseListener(buttonMouseListener);
        reindexButton.addMouseListener(buttonMouseListener);

        Box topMenu = Box.createHorizontalBox();

        topMenu.add(loadIsatabButton);
        topMenu.add(Box.createHorizontalStrut(5));
        topMenu.add(unloadStudyButton);
        topMenu.add(Box.createHorizontalStrut(5));
        topMenu.add(securityButton);

        Box bottomMenu = Box.createHorizontalBox();

        bottomMenu.add(Box.createHorizontalStrut(45));
        bottomMenu.add(reindexButton);
        bottomMenu.add(Box.createHorizontalStrut(5));
        bottomMenu.add(exportISAtabButton);
        bottomMenu.add(Box.createHorizontalStrut(45));

        loaderMenu.add(topMenu);
        loaderMenu.add(bottomMenu);

    }

    public Map<String, Set<String>> getAddedItems(Map<String, Set<String>> original, Map<String, Set<String>> modified) {
        Map<String, Set<String>> difference = new HashMap<String, Set<String>>();

        for (String studyAcc : modified.keySet()) {
            if (!original.containsKey(studyAcc)) {
                difference.put(studyAcc, modified.get(studyAcc));
            } else {
                difference.put(studyAcc, new HashSet<String>());

                for (String modifiedContents : modified.get(studyAcc)) {
                    if (!original.get(studyAcc).contains(modifiedContents)) {
                        difference.get(studyAcc).add(modifiedContents);
                    }
                }
            }
        }

        return difference;
    }

    public Map<String, Set<String>> getRemovedItems(Map<String, Set<String>> original, Map<String, Set<String>> modified) {
        Map<String, Set<String>> difference = new HashMap<String, Set<String>>();
        for (String studyAcc : modified.keySet()) {
            difference.put(studyAcc, new HashSet<String>());
            if (original.containsKey(studyAcc)) {
                for (String originalContents : original.get(studyAcc)) {
                    if (modified.containsKey(studyAcc)) {
                        if (!modified.get(studyAcc).contains(originalContents)) {
                            difference.get(studyAcc).add(originalContents);
                        }
                    }
                }
            }
        }
        return difference;
    }


    protected void loadToDatabase(BIIObjectStore store, String isatabSubmissionPath, final String report) {
        dbLoader.loadToDatabase(store, isatabSubmissionPath, report);
    }

    protected void createUserManagement(final String report) {

        permissionManagement.createUserManagement(report);
    }

    protected void showLoaderMenu() {
        appContainer.setGlassPanelContents(loaderMenu);
        appContainer.validate();
    }

    public AppContainer getAppContainer() {
        return appContainer;
    }

    String[] getLoadedStudies() {
        java.util.List<Study> studies = isatabValidator.getStudiesInSubmission();
        String[] studyAccessions = new String[studies.size()];
        for (int i = 0; i < studies.size(); i++) {
            Study study = studies.get(i);
            studyAccessions[i] = study.getAcc();
        }
        return studyAccessions;
    }

    protected void createLoginScreen() {
        login = new LoginUI(appContainer);
        login.createGUI();

        login.addPropertyChangeListener("doLogin", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                createLoaderMenu();
                appContainer.setGlassPanelContents(loaderMenu);
            }
        });
    }

    protected void instantiateConversionUtilPanel(String fileLoc) {
        // do nothing here.
    }


    public SmallLoader getProgressIndicator() {
        return progressIndicator;
    }

    public Container getLoaderMenu() {
        return loaderMenu;
    }

    public SelectISATABUI getSelectISATABUI() {
        return selectISATABUI;
    }

    public StudyAccessibilityModificationUI getStudyPermissionManagement() {
        return studyPermissionManagement;
    }

    public void setStudyPermissionManagement(StudyAccessibilityModificationUI studyPermissionManagement) {
        this.studyPermissionManagement = studyPermissionManagement;
    }

    public LoginUI getLogin() {
        return login;
    }


}
