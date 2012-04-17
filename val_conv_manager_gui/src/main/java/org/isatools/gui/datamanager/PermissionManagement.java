package org.isatools.gui.datamanager;

import org.isatools.gui.Globals;
import org.isatools.gui.UIUtils;
import org.isatools.gui.datamanager.studyaccess.StudyAccessibilityModificationUI;
import org.isatools.gui.datamanager.studyaccess.StudyOwnershipUI;
import org.isatools.gui.datamanager.studyaccess.StudyPrivacyUI;
import org.isatools.isatab.manager.UserManagementControl;
import uk.ac.ebi.bioinvindex.model.VisibilityStatus;
import uk.ac.ebi.bioinvindex.model.security.User;

import javax.persistence.EntityTransaction;
import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PermissionManagement implements Serializable {
    private final DataManagerToolUI dataManagerToolUI;

    public PermissionManagement(DataManagerToolUI dataManagerToolUI) {
        this.dataManagerToolUI = dataManagerToolUI;
    }

    void createSecurityChangeInterface() {
        final Thread[] threads = new Thread[1];
        threads[0] = new Thread(new Runnable() {
            public void run() {
                try {

                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            dataManagerToolUI.getAppContainer().setGlassPanelContents(dataManagerToolUI.createProgressScreen("finding studies & users"));
                            dataManagerToolUI.getAppContainer().validate();
                            dataManagerToolUI.getProgressIndicator().start();

                        }
                    });

                    final UserManagementControl umControl = new UserManagementControl();

                    List<User> users = umControl.getUsers();
                    Set<String> studies = umControl.getAllStudies();
                    Map<String, VisibilityStatus> studyToVisability = umControl.getAllStudyVisibilityStatus();

                    dataManagerToolUI.setStudyPermissionManagement(new StudyAccessibilityModificationUI(dataManagerToolUI.getAppContainer(),
                            studies.toArray(new String[studies.size()]),
                            umControl.extractUserNamesFromUserList(users)));

                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            dataManagerToolUI.getStudyPermissionManagement().createGUI(true);
                        }
                    });

                    // setting study to users here.
                    dataManagerToolUI.getStudyPermissionManagement().getStudyOwnership()
                            .setStudyToUser(new HashMap<String, Set<String>>(umControl.getStudiesAndAssociatedUsers()));
                    dataManagerToolUI.getStudyPermissionManagement().getStudyPrivacy().setStudyPrivacySelections(studyToVisability);

                    dataManagerToolUI.getStudyPermissionManagement().addPropertyChangeListener("doPrivacySettings", new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent propertyChangeEvent) {

                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    final Thread[] threads = new Thread[1];
                                    threads[0] = new Thread(new Runnable() {
                                        public void run() {
                                            dataManagerToolUI.getAppContainer().setGlassPanelContents(dataManagerToolUI.createProgressScreen("applying settings..."));
                                            dataManagerToolUI.getAppContainer().validate();
                                            dataManagerToolUI.getProgressIndicator().start();
                                            // perform associations and then show a screen telling the users what has been done :D
                                            try {
                                                modifyPrivacyAndAccessibility(umControl.getStudiesAndAssociatedUsers());
                                            } catch (final Exception e) {
                                                SwingUtilities.invokeLater(new Runnable() {
                                                    public void run() {
                                                        dataManagerToolUI.getAppContainer().setGlassPanelContents(dataManagerToolUI.createResultPanel(Globals.PRIVACY_MOD_FAILED, Globals.BACK_MAIN,
                                                                Globals.BACK_MAIN_OVER, Globals.EXIT, Globals.EXIT_OVER, e.getMessage()));
                                                        dataManagerToolUI.getProgressIndicator().stop();
                                                        dataManagerToolUI.getAppContainer().validate();
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

                    dataManagerToolUI.getStudyPermissionManagement().addPropertyChangeListener("doShowMenu", new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent propertyChangeEvent) {

                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    dataManagerToolUI.getAppContainer().setGlassPanelContents(dataManagerToolUI.getLoaderMenu());
                                    dataManagerToolUI.getAppContainer().validate();
                                }
                            });
                        }
                    });

                    dataManagerToolUI.getProgressIndicator().stop();
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            dataManagerToolUI.getAppContainer().setGlassPanelContents(dataManagerToolUI.getStudyPermissionManagement());
                            dataManagerToolUI.getAppContainer().validate();
                        }
                    });
                } catch (final Exception e) {
                    e.printStackTrace();
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            dataManagerToolUI.getAppContainer().setGlassPanelContents(dataManagerToolUI.createResultPanel(Globals.PRIVACY_MOD_FAILED, Globals.BACK_MAIN,
                                    Globals.BACK_MAIN_OVER, Globals.EXIT, Globals.EXIT_OVER, e.getMessage()));
                            dataManagerToolUI.getProgressIndicator().stop();
                            dataManagerToolUI.getAppContainer().validate();
                        }
                    });
                }
            }
        }

        );
        threads[0].start();
    }

    void modifyPrivacyAndAccessibility(Map<String, Set<String>> originalOwnership) {
        EntityTransaction transaction = null;
        UserManagementControl umControl = null;

        try {
            umControl = new UserManagementControl();
            transaction = umControl.getEntityManager().getTransaction();
            transaction.begin();

            // do visibility changes
            Map<String, VisibilityStatus> studyToVisability =
                    dataManagerToolUI.getStudyPermissionManagement().getStudyPrivacy().getStudyPrivacySelections();

            for (String studyAcc : studyToVisability.keySet()) {
                umControl.changeStudyVisability(studyAcc, studyToVisability.get(studyAcc));
            }

            // do ownership modifications
            Map<String, Set<String>> addedUsers = dataManagerToolUI.getAddedItems(originalOwnership,
                    dataManagerToolUI.getStudyPermissionManagement().getStudyOwnership().getStudyToUser());


            Map<String, Set<String>> removedUsers = dataManagerToolUI.getRemovedItems(originalOwnership,
                    dataManagerToolUI.getStudyPermissionManagement().getStudyOwnership().getStudyToUser());

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
                    JPanel resultPanel = dataManagerToolUI.createResultPanel(Globals.PRIVACY_MOD_SUCCESS, Globals.BACK_MAIN,
                            Globals.BACK_MAIN_OVER, Globals.EXIT, Globals.EXIT_OVER, message);
                    dataManagerToolUI.getAppContainer().setGlassPanelContents(resultPanel);
                    dataManagerToolUI.getProgressIndicator().stop();
                    dataManagerToolUI.getAppContainer().validate();
                }
            });

        } catch (final Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }

            if (umControl != null) {
                umControl.getEntityManager().clear();
            }
            e.printStackTrace();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    dataManagerToolUI.getAppContainer().setGlassPanelContents(dataManagerToolUI.createResultPanel(Globals.PRIVACY_MOD_FAILED, Globals.BACK_MAIN,
                            Globals.BACK_MAIN_OVER, Globals.EXIT, Globals.EXIT_OVER, e.getMessage()));
                    dataManagerToolUI.getProgressIndicator().stop();
                    dataManagerToolUI.getAppContainer().validate();
                }
            });
        }
    }

    protected void performUsermanagementAssociations(final String report) {
        UserManagementControl umControl = new UserManagementControl();

        try {
            StudyPrivacyUI privacy = dataManagerToolUI.getStudyPermissionManagement().getStudyPrivacy();
            StudyOwnershipUI ownership = dataManagerToolUI.getStudyPermissionManagement().getStudyOwnership();

            EntityTransaction transaction = umControl.getEntityManager().getTransaction();
            transaction.begin();

            for (String studyAcc : privacy.getStudyPrivacySelections().keySet()) {
                umControl.changeStudyVisability(studyAcc, privacy.getStudyPrivacySelections().get(studyAcc));
            }

            for (String studyAcc : ownership.getStudyToUser().keySet()) {
                // add curator to allowed study users.
                ownership.getStudyToUser().get(studyAcc).add(dataManagerToolUI.getLogin().getLoggedInUserName());
                // now iterate through the users assigned to the study and add them to the database
                for (String user : ownership.getStudyToUser().get(studyAcc)) {
                    User u = umControl.getUserByUsername(user);
                    umControl.addUserToStudy(studyAcc, u);
                }
            }

            transaction.commit();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    dataManagerToolUI.getAppContainer().setGlassPanelContents(dataManagerToolUI.createResultPanel(Globals.LOAD_SUCCESS, Globals.BACK_MAIN, Globals.BACK_MAIN_OVER, Globals.EXIT, Globals.EXIT_OVER, "<p>" + report + "</p>", dataManagerToolUI.getValidatorReport()));
                    dataManagerToolUI.getProgressIndicator().stop();
                    dataManagerToolUI.getAppContainer().validate();
                }
            });
        } catch (final Exception e) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    e.printStackTrace();
                    dataManagerToolUI.getAppContainer().setGlassPanelContents(dataManagerToolUI.createResultPanel(Globals.LOAD_SUCCESS_UM_FAILED, Globals.BACK_MAIN, Globals.BACK_MAIN_OVER, Globals.EXIT, Globals.EXIT_OVER, "<p>" + report + "</p>"));
                    dataManagerToolUI.getProgressIndicator().stop();
                    dataManagerToolUI.getAppContainer().validate();
                }
            });
        }
    }

    protected void createUserManagement(final String report) {

        try {

            UserManagementControl umControl = new UserManagementControl();

            String[] users = umControl.getUsernames();

            dataManagerToolUI.setStudyPermissionManagement(new StudyAccessibilityModificationUI(dataManagerToolUI.getAppContainer(), dataManagerToolUI.getLoadedStudies(), users));
            dataManagerToolUI.getStudyPermissionManagement().createGUI(false);

            dataManagerToolUI.getProgressIndicator().stop();
            dataManagerToolUI.getAppContainer().setGlassPanelContents(dataManagerToolUI.getStudyPermissionManagement());
            dataManagerToolUI.getAppContainer().validate();

            dataManagerToolUI.getStudyPermissionManagement().addPropertyChangeListener("doPrivacySettings", new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent propertyChangeEvent) {

                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            final Thread[] threads = new Thread[1];
                            threads[0] = new Thread(new Runnable() {
                                public void run() {
                                    dataManagerToolUI.getAppContainer().setGlassPanelContents(dataManagerToolUI.createProgressScreen("setting privacy info..."));
                                    dataManagerToolUI.getAppContainer().validate();
                                    dataManagerToolUI.getProgressIndicator().start();

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
                    dataManagerToolUI.getAppContainer().setGlassPanelContents(dataManagerToolUI.createResultPanel(Globals.PRIVACY_MOD_FAILED, Globals.BACK_MAIN,
                            Globals.BACK_MAIN_OVER, Globals.EXIT, Globals.EXIT_OVER, e.getMessage()));
                    dataManagerToolUI.getProgressIndicator().stop();
                    dataManagerToolUI.getAppContainer().validate();
                }
            });
        }

    }
}