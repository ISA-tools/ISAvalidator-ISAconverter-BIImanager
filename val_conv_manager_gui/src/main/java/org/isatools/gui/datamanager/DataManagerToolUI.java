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

import org.isatools.gui.*;
import org.isatools.gui.datamanager.exportisa.ExportISAGUI;
import org.isatools.gui.datamanager.exportisa.SuccessOrFailureGUI;
import org.isatools.gui.datamanager.studyaccess.*;
import org.isatools.gui.errorprocessing.ErrorReport;
import org.isatools.isatab.gui_invokers.GUIBIIReindex;
import org.isatools.isatab.gui_invokers.GUIISATABExporter;
import org.isatools.isatab.gui_invokers.GUIISATABLoader;
import org.isatools.isatab.gui_invokers.GUIInvokerResult;
import org.isatools.isatab.manager.UserManagementControl;
import org.isatools.tablib.exceptions.TabException;
import org.isatools.tablib.utils.BIIObjectStore;
import org.isatools.tablib.utils.logging.TabLoggingEventWrapper;
import org.jdesktop.fuse.InjectedResource;
import org.jdesktop.fuse.ResourceInjector;
import uk.ac.ebi.bioinvindex.model.Study;
import uk.ac.ebi.bioinvindex.model.VisibilityStatus;
import uk.ac.ebi.bioinvindex.model.security.User;

import javax.persistence.EntityTransaction;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * InterfaceUtils
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 * @date Nov 25, 2009
 */


public class DataManagerToolUI extends CommonUI {

    @InjectedResource
    private ImageIcon exportFailureImage, unloadingSuccess, unloadingFailure, loadButton,
            loadButtonOver;

    private StudyAccessionGUI studyAccessionGUI;
    private StudyAccessibilityModificationUI studyPermissionManagement;
    private ExportISAGUI exportISAGUI;
    private JPanel loaderMenu;
    // TODO: remove, we cannot have this over multiple operations, Hibernate problems
    // private UserManagementControl umControl;

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
        final JLabel mibbiButton = new JLabel(Globals.MIBBI_CHECKLIST);
        mibbiButton.setToolTipText("<html><strong>Set Minimum Information Compliance</strong><p>Set which studies meet which reporting standards.</p><p>See <a href=\"http://mibbi.org\">MIBBI</a> more information.</p></html>");

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
                    // todo change unload study UI to use same tree view as in export ISA UI
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
                    createSecurityChangeInterface();

                } else if (mouseEvent.getSource() == exportISAtabButton) {
                    exportISAtabButton.setIcon(Globals.EXPORT_ISA);
                    createExportISATabInterface();
                } else if (mouseEvent.getSource() == reindexButton) {
                    reindexButton.setIcon(Globals.REINDEX);
                    performReindexing();

                } else if (mouseEvent.getSource() == mibbiButton) {
                    // todo: update mibbi checklists and then show similar interface to the security
                    // interface

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
                } else if (mouseEvent.getSource() == mibbiButton) {
                    mibbiButton.setIcon(Globals.MIBBI_CHECKLIST_OVER);

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
                } else if (mouseEvent.getSource() == mibbiButton) {
                    mibbiButton.setIcon(Globals.MIBBI_CHECKLIST);
                }
            }


        };
        loadIsatabButton.addMouseListener(buttonMouseListener);
        unloadStudyButton.addMouseListener(buttonMouseListener);
        securityButton.addMouseListener(buttonMouseListener);
        exportISAtabButton.addMouseListener(buttonMouseListener);
        reindexButton.addMouseListener(buttonMouseListener);
        mibbiButton.addMouseListener(buttonMouseListener);

        Box topMenu = Box.createHorizontalBox();

        topMenu.add(loadIsatabButton);
        topMenu.add(Box.createHorizontalStrut(5));
        topMenu.add(unloadStudyButton);
        topMenu.add(Box.createHorizontalStrut(5));
        topMenu.add(securityButton);

        Box bottomMenu = Box.createHorizontalBox();

        bottomMenu.add(reindexButton);
        bottomMenu.add(Box.createHorizontalStrut(5));
        bottomMenu.add(mibbiButton);
        bottomMenu.add(Box.createHorizontalStrut(5));
        bottomMenu.add(exportISAtabButton);

        loaderMenu.add(topMenu);
        loaderMenu.add(bottomMenu);

    }

    private void performReindexing() {
        final Thread[] threads = new Thread[1];
        threads[0] = new Thread(new Runnable() {
            public void run() {
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            appContainer.setGlassPanelContents(createProgressScreen("reindexing..."));
                            appContainer.validate();
                            progressIndicator.start();
                        }
                    });

                    GUIBIIReindex reindexer = new GUIBIIReindex();

                    final SuccessOrFailureGUI successOrFailGUI;

                    if (reindexer.reindexDatabase() == GUIInvokerResult.SUCCESS) {
                        successOrFailGUI = new SuccessOrFailureGUI(appContainer, SuccessOrFailureGUI.INDEX_SUCCESS,
                                "<html>Reindexing of the BII database has completed successfully!</html>");

                    } else {
                        successOrFailGUI = new SuccessOrFailureGUI(appContainer, SuccessOrFailureGUI.INDEX_FAILURE, null);
                    }

                    successOrFailGUI.addPropertyChangeListener("toMenu", new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                            appContainer.setGlassPanelContents(loaderMenu);
                            appContainer.validate();
                        }
                    });

                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            appContainer.setGlassPanelContents(successOrFailGUI);
                            progressIndicator.stop();
                            appContainer.validate();
                        }
                    });


                } catch (final Exception e) {
                    e.printStackTrace();
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            SuccessOrFailureGUI successOrFailGUI = new SuccessOrFailureGUI(appContainer, SuccessOrFailureGUI.INDEX_FAILURE,
                                    e.getMessage());
                            appContainer.setGlassPanelContents(successOrFailGUI);
                            progressIndicator.stop();
                            appContainer.validate();
                        }
                    });
                }
            }
        });

        threads[0].start();
    }

    private void createSecurityChangeInterface() {

        final Thread[] threads = new Thread[1];
        threads[0] = new Thread(new Runnable() {
            public void run() {
                try {

                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            appContainer.setGlassPanelContents(createProgressScreen("finding studies & users"));
                            appContainer.validate();
                            progressIndicator.start();

                        }
                    });

                    final UserManagementControl umControl = new UserManagementControl();

                    List<User> users = umControl.getUsers();
                    Set<String> studies = umControl.getAllStudies();
                    Map<String, VisibilityStatus> studyToVisability = umControl.getAllStudyVisibilityStatus();

                    studyPermissionManagement = new StudyAccessibilityModificationUI(appContainer,
                            studies.toArray(new String[studies.size()]),
                            umControl.extractUserNamesFromUserList(users));

                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            studyPermissionManagement.createGUI(true);
                        }
                    });

                    // setting study to users here.
                    studyPermissionManagement.getStudyOwnership()
                            .setStudyToUser(new HashMap<String, Set<String>>(umControl.getStudiesAndAssociatedUsers()));
                    studyPermissionManagement.getStudyPrivacy().setStudyPrivacySelections(studyToVisability);

                    studyPermissionManagement.addPropertyChangeListener("doPrivacySettings", new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent propertyChangeEvent) {

                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    final Thread[] threads = new Thread[1];
                                    threads[0] = new Thread(new Runnable() {
                                        public void run() {
                                            appContainer.setGlassPanelContents(createProgressScreen("applying settings..."));
                                            appContainer.validate();
                                            progressIndicator.start();
                                            // perform associations and then show a screen telling the users what has been done :D
                                            try {
                                                modifyPrivacyAndAccessibility(umControl.getStudiesAndAssociatedUsers());
                                            } catch (final Exception e) {
                                                SwingUtilities.invokeLater(new Runnable() {
                                                    public void run() {
                                                        appContainer.setGlassPanelContents(createResultPanel(Globals.PRIVACY_MOD_FAILED, Globals.BACK_MAIN,
                                                                Globals.BACK_MAIN_OVER, Globals.EXIT, Globals.EXIT_OVER, e.getMessage()));
                                                        progressIndicator.stop();
                                                        appContainer.validate();
                                                    }
                                                });
                                            }

                                        }
                                    });
                                    threads[0].start();
                                }
                            });
                        }
                    });

                    studyPermissionManagement.addPropertyChangeListener("doShowMenu", new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent propertyChangeEvent) {

                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    appContainer.setGlassPanelContents(loaderMenu);
                                    appContainer.validate();
                                }
                            });
                        }
                    });

                    progressIndicator.stop();
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            appContainer.setGlassPanelContents(studyPermissionManagement);
                            appContainer.validate();
                        }
                    });
                } catch (final Exception e) {
                    e.printStackTrace();
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            appContainer.setGlassPanelContents(createResultPanel(Globals.PRIVACY_MOD_FAILED, Globals.BACK_MAIN,
                                    Globals.BACK_MAIN_OVER, Globals.EXIT, Globals.EXIT_OVER, e.getMessage()));
                            progressIndicator.stop();
                            appContainer.validate();
                        }
                    });
                }
            }
        }

        );
        threads[0].start();
    }

    private void createExportISATabInterface() {
        final Thread[] threads = new Thread[1];
        threads[0] = new Thread(new Runnable() {
            public void run() {
                try {

                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            appContainer.setGlassPanelContents(createProgressScreen("finding studies..."));
                            appContainer.validate();
                            progressIndicator.start();
                        }
                    });

                    exportISAGUI = new ExportISAGUI(appContainer);

                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            exportISAGUI.createGUI();
                        }
                    });

                    exportISAGUI.addPropertyChangeListener("toMenu", new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                            appContainer.setGlassPanelContents(loaderMenu);
                            appContainer.validate();
                        }
                    });

                    exportISAGUI.addPropertyChangeListener("doISATabExport", new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                            final Collection<Study> toExport = exportISAGUI.getSelectedStudiesForExport();

                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    final Thread[] threads = new Thread[1];
                                    threads[0] = new Thread(new Runnable() {
                                        public void run() {
                                            appContainer.setGlassPanelContents(createProgressScreen("exporting ISATab..."));
                                            appContainer.validate();
                                            progressIndicator.start();
                                            // perform associations and then show a screen telling the users what has been done :D
                                            try {
                                                if (exportISAGUI.exportToRepository()) {
                                                    exportSelectedStudiesToISATab(
                                                            toExport,
                                                            exportISAGUI.exportDataFiles());
                                                } else {
                                                    exportSelectedStudiesToISATab(
                                                            toExport,
                                                            exportISAGUI.getLocalFileDirectory(),
                                                            exportISAGUI.exportDataFiles());
                                                }
                                            } catch (final Exception e) {
                                                SwingUtilities.invokeLater(new Runnable() {
                                                    public void run() {
                                                        appContainer.setGlassPanelContents(createResultPanel(Globals.PRIVACY_MOD_FAILED, Globals.BACK_MAIN,
                                                                Globals.BACK_MAIN_OVER, Globals.EXIT, Globals.EXIT_OVER, e.getMessage()));
                                                        progressIndicator.stop();
                                                        appContainer.validate();
                                                    }
                                                });
                                            }
                                        }
                                    });
                                    threads[0].start();
                                }
                            });
                        }
                    });

                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            progressIndicator.stop();
                            appContainer.setGlassPanelContents(exportISAGUI);
                            appContainer.validate();
                        }
                    });

                } catch (final Exception e) {
                    e.printStackTrace();
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            appContainer.setGlassPanelContents(createResultPanel(Globals.PRIVACY_MOD_FAILED, Globals.BACK_MAIN,
                                    Globals.BACK_MAIN_OVER, Globals.EXIT, Globals.EXIT_OVER, e.getMessage()));
                            progressIndicator.stop();
                            appContainer.validate();
                        }
                    });
                }
            }
        });
        threads[0].start();
    }

    private void exportSelectedStudiesToISATab(Collection<Study> studies, boolean exportDataFiles) {
        exportSelectedStudiesToISATab(studies, null, exportDataFiles);
    }

    private void exportSelectedStudiesToISATab(Collection<Study> studies, String exportFileLocation, boolean exportDataFiles) {
        // show an export successful window telling the users which studies have been output and where they have
        // been output to.
        GUIISATABExporter exporter = new GUIISATABExporter();
        try {
            GUIInvokerResult result;
            if (exportFileLocation == null) {
                result = exporter.isatabExportToRepository(studies, !exportDataFiles);
            } else {
                result = exporter.isatabExportToPath(studies, exportFileLocation, !exportDataFiles);
            }

            if (result == GUIInvokerResult.ERROR) {
                createResultPanel(exportFailureImage, Globals.BACK_MAIN, Globals.BACK_MAIN_OVER, Globals.EXIT,
                        Globals.EXIT_OVER, null, new ErrorReport(exporter.getLog(), getAllowedLogLevels()));
            } else {
                SuccessOrFailureGUI successGUI = new SuccessOrFailureGUI(appContainer, SuccessOrFailureGUI.EXPORT_SUCCESS,
                        exportISAGUI.generateExportSuccessMessage());

                successGUI.addPropertyChangeListener("toMenu", new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                        appContainer.setGlassPanelContents(loaderMenu);
                        appContainer.validate();
                    }
                });

                appContainer.setGlassPanelContents(successGUI);
                progressIndicator.stop();
                appContainer.validate();

            }

        } catch (Exception e) {
            SuccessOrFailureGUI failureGUI = new SuccessOrFailureGUI(appContainer, SuccessOrFailureGUI.EXPORT_FAILURE,
                    exportISAGUI.generateExportErrorMessage(e));

            failureGUI.addPropertyChangeListener("toMenu", new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                    appContainer.setGlassPanelContents(loaderMenu);
                    appContainer.validate();
                }
            });

            appContainer.setGlassPanelContents(failureGUI);
            progressIndicator.stop();
            appContainer.validate();
        }
    }


    private void modifyPrivacyAndAccessibility(Map<String, Set<String>> originalOwnership) {
        EntityTransaction transaction = null;
        UserManagementControl umControl = null;

        try {
            umControl = new UserManagementControl();
            transaction = umControl.getEntityManager().getTransaction();
            transaction.begin();

            // do visibility changes
            Map<String, VisibilityStatus> studyToVisability =
                    studyPermissionManagement.getStudyPrivacy().getStudyPrivacySelections();

            for (String studyAcc : studyToVisability.keySet()) {
                umControl.changeStudyVisability(studyAcc, studyToVisability.get(studyAcc));
            }

            // do ownership modifications
            Map<String, Set<String>> addedUsers = getAddedItems(originalOwnership,
                    studyPermissionManagement.getStudyOwnership().getStudyToUser());


            Map<String, Set<String>> removedUsers = getRemovedItems(originalOwnership,
                    studyPermissionManagement.getStudyOwnership().getStudyToUser());

            for (String studyAcc : addedUsers.keySet()) {
                for (String username : addedUsers.get(studyAcc)) {
                    umControl.addUserToStudy(studyAcc, umControl.getUserByUsername(username));
                }
            }

            for (String studyAcc : removedUsers.keySet()) {
                for (String username : removedUsers.get(studyAcc)) {
                    User u = umControl.getUserByUsername(username);
                    umControl.removeUserFromStudy(studyAcc, u);
                }
            }

            final String message = UIUtils.getPrivacyModificationReport(studyToVisability, addedUsers, removedUsers).toString();

            transaction.commit();
            umControl.getEntityManager().clear();

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    JPanel resultPanel = createResultPanel(Globals.PRIVACY_MOD_SUCCESS, Globals.BACK_MAIN,
                            Globals.BACK_MAIN_OVER, Globals.EXIT, Globals.EXIT_OVER, message);
                    appContainer.setGlassPanelContents(resultPanel);
                    progressIndicator.stop();
                    appContainer.validate();
                }
            });

        }
        catch (final Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }

            if (umControl != null) {
                umControl.getEntityManager().clear();
            }
            e.printStackTrace();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    appContainer.setGlassPanelContents(createResultPanel(Globals.PRIVACY_MOD_FAILED, Globals.BACK_MAIN,
                            Globals.BACK_MAIN_OVER, Globals.EXIT, Globals.EXIT_OVER, e.getMessage()));
                    progressIndicator.stop();
                    appContainer.validate();
                }
            });
        }
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
        try {
            // will dispatch files to parent file
            File isatabLoadingDirector = new File(selectISATABUI.getSelectedFile());

            final String dispatchDirectory = isatabLoadingDirector.getPath();

            File dispatchDirCreate = new File(dispatchDirectory);
            if (!dispatchDirCreate.exists()) {
                dispatchDirCreate.mkdir();
            }

            final GUIISATABLoader isatabLoader = new GUIISATABLoader();

            GUIInvokerResult result = isatabLoader.persist(store, isatabSubmissionPath);

            // in the event of loading failing, tell the user why it didn't load.
            if (result == GUIInvokerResult.ERROR) {
                List<TabLoggingEventWrapper> logResult = isatabLoader.getLog();
                ErrorReport er = new ErrorReport(logResult, getAllowedLogLevels());
                appContainer.setGlassPanelContents(
                        createResultPanel(Globals.LOAD_FAILED, Globals.LOAD_ANOTHER,
                                Globals.LOAD_ANOTHER_OVER, Globals.EXIT, Globals.EXIT_OVER, null, er));
                appContainer.validate();
            } else {

                // show success screen
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {

                        progressIndicator.stop();
                        // should perhaps extract this to the create user management method...

                        final Thread[] threads = new Thread[1];
                        threads[0] = new Thread(new Runnable() {
                            public void run() {
                                appContainer.setGlassPanelContents(createProgressScreen("finding users..."));
                                appContainer.validate();
                                progressIndicator.start();

                                createUserManagement(report);
                            }
                        });
                        threads[0].start();
                    }
                });
            }
        } catch (final Exception e) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    progressIndicator.stop();
                    appContainer.setGlassPanelContents(
                            createResultPanel(Globals.LOAD_FAILED, Globals.LOAD_ANOTHER,
                                    Globals.LOAD_ANOTHER_OVER, Globals.EXIT, Globals.EXIT_OVER, e.getMessage()));
                    appContainer.validate();
                }
            });
        }
    }


    protected void performUsermanagementAssociations(final String report) {
        UserManagementControl umControl = new UserManagementControl();

        try {
            StudyPrivacyUI privacy = studyPermissionManagement.getStudyPrivacy();
            StudyOwnershipUI ownership = studyPermissionManagement.getStudyOwnership();

            EntityTransaction transaction = umControl.getEntityManager().getTransaction();
            transaction.begin();

            for (String studyAcc : privacy.getStudyPrivacySelections().keySet()) {
                umControl.changeStudyVisability(studyAcc, privacy.getStudyPrivacySelections().get(studyAcc));
            }

            for (String studyAcc : ownership.getStudyToUser().keySet()) {
                // add curator to allowed study users.
                ownership.getStudyToUser().get(studyAcc).add(login.getLoggedInUserName());
                // now iterate through the users assigned to the study and add them to the database
                for (String user : ownership.getStudyToUser().get(studyAcc)) {
                    User u = umControl.getUserByUsername(user);
                    umControl.addUserToStudy(studyAcc, u);
                }
            }

            transaction.commit();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    appContainer.setGlassPanelContents(createResultPanel(Globals.LOAD_SUCCESS, Globals.BACK_MAIN, Globals.BACK_MAIN_OVER, Globals.EXIT, Globals.EXIT_OVER, "<p>" + report + "</p>", getValidatorReport()));
                    progressIndicator.stop();
                    appContainer.validate();
                }
            });
        } catch (final Exception e) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    e.printStackTrace();
                    appContainer.setGlassPanelContents(createResultPanel(Globals.LOAD_SUCCESS_UM_FAILED, Globals.BACK_MAIN, Globals.BACK_MAIN_OVER, Globals.EXIT, Globals.EXIT_OVER, "<p>" + report + "</p>"));
                    progressIndicator.stop();
                    appContainer.validate();
                }
            });
        }
    }

    protected void createUserManagement(final String report) {

        try {

            UserManagementControl umControl = new UserManagementControl();

            String[] users = umControl.getUsernames();

            studyPermissionManagement = new StudyAccessibilityModificationUI(appContainer, getLoadedStudies(), users);
            studyPermissionManagement.createGUI(false);

            progressIndicator.stop();
            appContainer.setGlassPanelContents(studyPermissionManagement);
            appContainer.validate();

            studyPermissionManagement.addPropertyChangeListener("doPrivacySettings", new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent propertyChangeEvent) {

                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            final Thread[] threads = new Thread[1];
                            threads[0] = new Thread(new Runnable() {
                                public void run() {
                                    appContainer.setGlassPanelContents(createProgressScreen("setting privacy info..."));
                                    appContainer.validate();
                                    progressIndicator.start();

                                    performUsermanagementAssociations(report);
                                }
                            });
                            threads[0].start();
                        }
                    });

                }
            });
        } catch (final Exception e) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    appContainer.setGlassPanelContents(createResultPanel(Globals.PRIVACY_MOD_FAILED, Globals.BACK_MAIN,
                            Globals.BACK_MAIN_OVER, Globals.EXIT, Globals.EXIT_OVER, e.getMessage()));
                    progressIndicator.stop();
                    appContainer.validate();
                }
            });
        }

    }

    protected void showLoaderMenu() {
        appContainer.setGlassPanelContents(loaderMenu);
        appContainer.validate();
    }


    private String[] getLoadedStudies() {
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
}
