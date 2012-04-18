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

package org.isatools.gui.datamanager.studyaccess;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.isatools.gui.AppContainer;
import org.isatools.gui.datamanager.StudyOperationCommon;
import org.isatools.isatab.gui_invokers.GUIInvokerResult;
import org.isatools.tablib.utils.logging.TabLoggingEventWrapper;
import org.jdesktop.fuse.InjectedResource;
import org.jdesktop.fuse.ResourceInjector;
import uk.ac.ebi.bioinvindex.model.Study;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

// Make this abstract for use as an unloading interface or an export interface for ISATab.
public class StudyAccessionGUI extends StudyOperationCommon {

    protected static final Logger log = Logger.getLogger(StudyAccessionGUI.class);

    @InjectedResource
    private ImageIcon unloadButton, unloadButtonOver,
            unloadStudyHeader, noStudiesPresentInfo,
            connectionProblemInfo;

    private StudySelector studySelector;
    private Set<String> availableStudies;
    private Collection<Study> studies;

    // need to look something like the converter gui in terms of providing selection alternatives, only difference being
    // that the list will contain study accessions populated through retrieval from the database!
    public StudyAccessionGUI(AppContainer appCont) {
        super(appCont);

        ResourceInjector.get("datamanager-package.style").inject(this);

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                doLoading();
            }
        });
    }

    private void doLoading() {
        Thread loadingThread = new Thread(new Runnable() {
            public void run() {
                container.setGlassPanelContents(createProgressScreen("finding studies..."));
                container.validate();
                sl.start();

                studies = loadStudiesFromDatabase();
                if (studies == null) {
                    // show error message
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            container.setGlassPanelContents(createStudyAccessionLoadErrorPanel());
                            container.validate();
                            sl.stop();
                        }
                    });
                } else if (studies.size() == 0) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            container.setGlassPanelContents(createEmptyDbPanel());
                            container.validate();
                            sl.stop();
                        }
                    });
                } else {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            // remove all components from the screen.
                            removeAll();
                            container.setGlassPanelContents(createStudySelectionPanel());
                            container.validate();
                            sl.stop();
                        }
                    });

                }
            }
        });

        loadingThread.start();
    }

    private JPanel createEmptyDbPanel() {
        JPanel errorPanel = new JPanel();
        errorPanel.setLayout(new BoxLayout(errorPanel, BoxLayout.PAGE_AXIS));
        errorPanel.setOpaque(false);

        errorPanel.add(new JLabel(noStudiesPresentInfo), JLabel.CENTER);

        // add button to go back to main menu!

        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setOpaque(false);

        buttonPanel.add(createBackToMainMenuButton(), BorderLayout.WEST);

        errorPanel.add(Box.createVerticalStrut(20));
        errorPanel.add(buttonPanel);

        return errorPanel;
    }

    private JPanel createStudyAccessionLoadErrorPanel() {
        JPanel errorPanel = new JPanel();
        errorPanel.setLayout(new BoxLayout(errorPanel, BoxLayout.PAGE_AXIS));
        errorPanel.setOpaque(false);
        errorPanel.add(new JLabel(connectionProblemInfo), JLabel.CENTER);
        return errorPanel;
    }


    private JPanel createStudySelectionPanel() {

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));
        container.setOpaque(false);

        JPanel studyAccessionSelector = new JPanel();
        studyAccessionSelector.setLayout(new BorderLayout());
        studyAccessionSelector.setOpaque(false);

        JLabel information = new JLabel(unloadStudyHeader);
        information.setHorizontalAlignment(SwingConstants.LEFT);
        information.setVerticalAlignment(SwingConstants.TOP);

        studyAccessionSelector.add(information, BorderLayout.NORTH);

        retrieveAndProcessStudyInformation();

        studySelector = new StudySelector(availableStudies);
        studyAccessionSelector.add(studySelector);

        container.add(studyAccessionSelector);

        // and need a convert button!
        JPanel buttonCont = new JPanel(new BorderLayout());
        buttonCont.setOpaque(false);

        final JLabel unload = new JLabel(this.unloadButton, JLabel.RIGHT);

        unload.addMouseListener(new MouseAdapter() {

            public void mouseEntered(MouseEvent mouseEvent) {
                unload.setIcon(unloadButtonOver);
            }

            public void mouseExited(MouseEvent mouseEvent) {
                unload.setIcon(StudyAccessionGUI.this.unloadButton);
            }

            public void mousePressed(MouseEvent mouseEvent) {
                Set<String> studiesToUnload = studySelector.getSelectedStudies();
                log.info("going to unload: ");
                for (String acc : studiesToUnload) {
                    log.info("study with acc " + acc);
                }
                doUnloading(studiesToUnload);
            }

        });
        buttonCont.add(unload, BorderLayout.EAST);
        buttonCont.add(createBackToMainMenuButton(), BorderLayout.WEST);
        container.add(buttonCont);
        return container;
    }

    private void doUnloading(final Set<String> toUnload) {


        Thread unloadingThread = new Thread(new Runnable() {
            public void run() {
                String message = toUnload.size() > 1 ? "unloading studies..." : "unloading study";
                container.setGlassPanelContents(createProgressScreen(message));
                container.validate();
                sl.start();

                GUIInvokerResult result = unloaderUtil.unload(toUnload);

                if (result == GUIInvokerResult.SUCCESS) {
                    // fire updates back to the listener(s)
                    sl.stop();
                    firePropertyChange("unloadSuccessful", "", getReport(toUnload));
                } else {
                    String topMostError = "";
                    for (TabLoggingEventWrapper tlew : unloaderUtil.getLog()) {
                        LoggingEvent le = tlew.getLogEvent();
                        if (le.getMessage() != null) {
                            topMostError = le.getMessage().toString();
                        }
                    }
                    sl.stop();
                    firePropertyChange("errorOccurred", "", topMostError);
                }

            }
        });

        unloadingThread.start();
    }

    private void retrieveAndProcessStudyInformation() {
        studies = loadStudiesFromDatabase();
        log.info("got studies, there are  : " + studies.size() + " of them!");
        if (studies != null) {

            processStudies(studies);
            log.info("available studies size is : " + availableStudies.size() + " and content is:");
            for (String studyAcc : availableStudies) {
                log.info("\t" + studyAcc);
            }
        }

    }

    private void processStudies(Collection<Study> studies) {
        availableStudies = new HashSet<String>();
        for (Study study : studies) {
            availableStudies.add(study.getAcc());
        }
    }

    private String getReport(Set<String> studiesToBeUnloaded) {
        StringBuilder report = new StringBuilder();

        report.append("<html>the following studies were unloaded from the database successfully:");
        report.append("<p>");
        for (String s : studiesToBeUnloaded) {
            report.append(s).append(" ");
        }
        report.append("</p>");
        report.append("</html>");

        return report.toString();
    }

}
