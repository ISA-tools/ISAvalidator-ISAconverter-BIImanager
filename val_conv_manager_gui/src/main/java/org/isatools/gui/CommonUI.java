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

package org.isatools.gui;

import org.apache.log4j.Level;
import org.isatools.effects.GenericPanel;
import org.isatools.effects.SmallLoader;
import org.isatools.effects.UIHelper;
import org.isatools.errorreporter.model.*;
import org.isatools.errorreporter.ui.ErrorReporterView;
import org.isatools.fileutils.ArchiveOrDirectoryFileFilter;
import org.isatools.fileutils.FileUnzipper;
import org.isatools.gui.converter.ConverterBackgroundPanel;
import org.isatools.gui.datamanager.DataManagerBackgroundPanel;
import org.isatools.gui.datamanager.studyaccess.LoginUI;
import org.isatools.gui.errorprocessing.ErrorReport;
import org.isatools.gui.validator.ValidatorBackgroundPanel;
import org.isatools.isatab.gui_invokers.GUIISATABValidator;
import org.isatools.isatab.gui_invokers.GUIInvokerResult;
import org.isatools.isatab.isaconfigurator.ISAConfigurationSet;
import org.isatools.tablib.utils.BIIObjectStore;
import org.isatools.tablib.utils.logging.TabLoggingEventWrapper;

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
 * Single entry point for displaying the different stages of the validation from selecting the ISATAB file, status screen
 * to tell use user that the ISATAB is being validated and screens to inform the user of success, or why the validation failed!
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 * @date Mar 31, 2009
 */

public abstract class CommonUI extends JLayeredPane {
    private GenericPanel generic;

    protected AppContainer appContainer;
    protected JFileChooser fileChooser;
    protected SmallLoader progressIndicator;
    protected ApplicationType useAs;
    private String[] options;

    protected ImageIcon buttonImage;
    protected ImageIcon buttonImageOver;

    protected LoadConfigurationPanel loadConfigurationUI;
    protected SelectISATABUI selectISATABUI;
    protected LoginUI login;

    private File configDir;

    // Loads and validate a submission, the first step for almost all the operations.
    protected GUIISATABValidator isatabValidator;

    public CommonUI(final AppContainer appContainer, ApplicationType useAs, String[] options) {
        this.appContainer = appContainer;
        this.useAs = useAs;
        this.options = options;
        setLayout(new OverlayLayout(this));
        setBackground(UIHelper.BG_COLOR);
        createGUI();
    }

    public void createGUI() {

        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new ArchiveOrDirectoryFileFilter());
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        if (useAs == ApplicationType.VALIDATOR) {
            generic = new ValidatorBackgroundPanel();
            buttonImage = new ImageIcon(getClass().getResource("/images/validator/validate_button.png"));
            buttonImageOver = new ImageIcon(getClass().getResource("/images/validator/validate_button_over.png"));
        } else if (useAs == ApplicationType.MANAGER) {
            generic = new DataManagerBackgroundPanel();
            buttonImage = new ImageIcon(getClass().getResource("/images/DataManager/load_button.png"));
            buttonImageOver = new ImageIcon(getClass().getResource("/images/DataManager/load_button_over.png"));
        } else {
            generic = new ConverterBackgroundPanel();
            buttonImage = new ImageIcon(getClass().getResource("/images/converter/convert_button.png"));
            buttonImageOver = new ImageIcon(getClass().getResource("/images/converter/convert_button_over.png"));
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                add(generic, JLayeredPane.DEFAULT_LAYER);
//                add(new GradientPanel(), JLayeredPane.DEFAULT_LAYER + 1);

                loadConfigurationUI = new LoadConfigurationPanel();
                loadConfigurationUI.createGUI();

                appContainer.setGlassPanelContents(loadConfigurationUI);

                loadConfigurationUI.addPropertyChangeListener("configSelected", new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                        Object eventObject = propertyChangeEvent.getNewValue();
                        if (eventObject instanceof File) {
                            configDir = (File) eventObject;
                            ISAConfigurationSet.setConfigPath(configDir.getAbsolutePath());
                        }

                        instantiateLoadIsaPanel();

                        if (useAs != ApplicationType.MANAGER) {
                            appContainer.setGlassPanelContents(selectISATABUI);
                        } else {
                            createLoginScreen();
                            appContainer.setGlassPanelContents(login);
                        }
                    }
                });

            }
        });
    }

    protected abstract void createLoginScreen();

    protected abstract void instantiateConversionUtilPanel(String fileLoc);

    protected abstract void loadToDatabase(BIIObjectStore store, String isatabSubmissionPath, final String report);

    protected abstract void showLoaderMenu();

    protected void validateFile(final String[] fileLoc) {

        isatabValidator = new GUIISATABValidator();
        File location = new File(fileLoc[0]);

        if (!new File(fileLoc[0]).isDirectory()) {
            // then need to unzip to a directory and change fileloc to point to this!
            try {
                fileLoc[0] = FileUnzipper.unzip(location);
            } catch (Exception e) {
                System.out.println("problem occurred \n" + e.getMessage());
            }
        }

        // validate the file.
        GUIInvokerResult result = isatabValidator.validate(fileLoc[0]);

        if (result == GUIInvokerResult.SUCCESS) {
            if (useAs == ApplicationType.VALIDATOR) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        appContainer.setGlassPanelContents(createResultPanel(Globals.VALID_ISATAB, Globals.VALIDATE_ANOTHER,
                                Globals.VALIDATE_ANOTHER_OVER, Globals.EXIT, Globals.EXIT_OVER, isatabValidator.report(true), getValidatorReport().getReport().isEmpty() ? null : getValidatorReport()));
                        progressIndicator.stop();
                    }
                });
            } else if (useAs == ApplicationType.CONVERTER) {
                instantiateConversionUtilPanel(fileLoc[0]);
                progressIndicator.stop();
            } else {
                final Thread[] threads = new Thread[1];
                threads[0] = new Thread(new Runnable() {
                    public void run() {
                        appContainer.setGlassPanelContents(createProgressScreen("loading into BII..."));
                        appContainer.validate();
                        progressIndicator.start();
                        loadToDatabase(isatabValidator.getStore(), fileLoc[0], isatabValidator.report(true));
                    }
                });
                threads[0].start();
            }
        } else {

            Map<String, List<ErrorMessage>> errorMessages = getErrorMessages(
                    isatabValidator.getLog());
            displayValidationErrorsAndWarnings(errorMessages);

            progressIndicator.stop();
        }
        appContainer.validate();
    }

    private void displayValidationErrorsAndWarnings(Map<String, List<ErrorMessage>> fileToErrors) {
        List<ISAFileErrorReport> errors = new ArrayList<ISAFileErrorReport>();
        for (String fileName : fileToErrors.keySet()) {
            errors.add(new ISAFileErrorReport(fileName,
                    FileType.INVESTIGATION, fileToErrors.get(fileName)));
        }

        if (fileToErrors.size() > 0) {
            ErrorReporterView view = new ErrorReporterView(errors);
            view.setPreferredSize(new Dimension(400, 450));
            view.createGUI();
            view.add(createReturnToMenuOrExitPanel(), BorderLayout.SOUTH);
            appContainer.setGlassPanelContents(view);
            appContainer.validate();
        }
    }

    private JPanel createReturnToMenuOrExitPanel() {
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setOpaque(false);

        final JLabel backButton = new JLabel(useAs == ApplicationType.VALIDATOR
                ? Globals.VALIDATE_ANOTHER : useAs == ApplicationType.CONVERTER
                ? Globals.CONVERT_ANOTHER : Globals.LOAD_ANOTHER, JLabel.LEFT);

        backButton.addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent event) {
                backButton.setIcon(useAs == ApplicationType.VALIDATOR
                        ? Globals.VALIDATE_ANOTHER : useAs == ApplicationType.CONVERTER
                        ? Globals.CONVERT_ANOTHER : Globals.LOAD_ANOTHER);
                if (backButton.getIcon() == Globals.BACK_MAIN) {
                    showLoaderMenu();
                } else {
                    appContainer.setGlassPanelContents(selectISATABUI);
                }

                appContainer.validate();
            }

            public void mouseEntered(MouseEvent event) {
                backButton.setIcon(useAs == ApplicationType.VALIDATOR
                        ? Globals.VALIDATE_ANOTHER_OVER : useAs == ApplicationType.CONVERTER
                        ? Globals.CONVERT_ANOTHER_OVER : Globals.LOAD_ANOTHER_OVER);
            }

            public void mouseExited(MouseEvent event) {
                backButton.setIcon(useAs == ApplicationType.VALIDATOR
                        ? Globals.VALIDATE_ANOTHER : useAs == ApplicationType.CONVERTER
                        ? Globals.CONVERT_ANOTHER : Globals.LOAD_ANOTHER);
            }
        });

        buttonPanel.add(backButton, BorderLayout.WEST);

        final JLabel exitButton = new JLabel(Globals.EXIT, JLabel.RIGHT);

        exitButton.addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent event) {
                appContainer.setVisible(false);
                appContainer.dispose();
                System.exit(0);
            }

            public void mouseEntered(MouseEvent event) {
                exitButton.setIcon(Globals.EXIT_OVER);
            }

            public void mouseExited(MouseEvent event) {
                exitButton.setIcon(Globals.EXIT);
            }
        });

        buttonPanel.add(exitButton, BorderLayout.EAST);

        return buttonPanel;
    }

    public ErrorReport getValidatorReport() {
        List<TabLoggingEventWrapper> logResult = isatabValidator.getLog();

        return new ErrorReport(logResult, getAllowedLogLevels());
    }

    public Set<Level> getAllowedLogLevels() {
        Set<Level> displayedLogLevels = new HashSet<Level>();
        displayedLogLevels.add(Level.WARN);
        displayedLogLevels.add(Level.ERROR);

        return displayedLogLevels;
    }


    protected void instantiateLoadIsaPanel() {
        selectISATABUI = new SelectISATABUI(buttonImage, buttonImageOver, fileChooser, useAs);
        selectISATABUI.addPropertyChangeListener("doValidation", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                final Thread[] threads = new Thread[1];
                threads[0] = new Thread(new Runnable() {
                    public void run() {
                        appContainer.setGlassPanelContents(createProgressScreen("validating isatab..."));
                        appContainer.validate();
                        progressIndicator.start();

                        validateFile(new String[]{selectISATABUI.getSelectedFile()});
                    }
                });

                threads[0].start();
            }
        });
        selectISATABUI.addPropertyChangeListener("toMenu", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                showLoaderMenu();
            }
        });
    }

    /**
     * Create loading panel showing little ticker and text informing user that validation is occuring!
     *
     * @param text -  message to show when validating or loading...
     * @return - JPanel
     */
    public JPanel createProgressScreen(String text) {

        JPanel loadingPanel = new JPanel();
        loadingPanel.setLayout(new BoxLayout(loadingPanel, BoxLayout.PAGE_AXIS));
        loadingPanel.setOpaque(false);
        loadingPanel.setSize(new Dimension(300, 100));

        progressIndicator = new SmallLoader(10, 5.0f, 150, 70, text);

        loadingPanel.add(progressIndicator);

        return loadingPanel;

    }

    public JPanel createResultPanel(ImageIcon mainImage, final ImageIcon anotherImage, final ImageIcon anotherImageOver, final ImageIcon exitImage, final ImageIcon exitImageOver, final String message) {
        return createResultPanel(mainImage, anotherImage, anotherImageOver, exitImage, exitImageOver, message, null);
    }

    public JPanel createResultPanel(ImageIcon mainImage, final ImageIcon anotherImage, final ImageIcon anotherImageOver, final ImageIcon exitImage, final ImageIcon exitImageOver, final String message, ErrorReport report) {
        final TaskResultPanel resultPanel = new TaskResultPanel(mainImage, anotherImage, anotherImageOver, exitImage, exitImageOver, message, report);

        resultPanel.addPropertyChangeListener("back", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                if (anotherImage == Globals.BACK_MAIN) {
                    showLoaderMenu();
                } else {
                    appContainer.setGlassPanelContents(selectISATABUI);
                }

                appContainer.validate();
            }
        });

        resultPanel.addPropertyChangeListener("exit", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                appContainer.setVisible(false);
                appContainer.dispose();
                System.exit(0);
            }
        });

        resultPanel.createGUI();
        return resultPanel;
    }

    protected Map<String, List<ErrorMessage>> getErrorMessages(List<TabLoggingEventWrapper> logEvents) {
        Map<String, List<ErrorMessage>> fileToErrors = new HashMap<String, List<ErrorMessage>>();

        for (TabLoggingEventWrapper event : logEvents) {
            String fileName = ErrorUtils.extractFileInformation(event.getLogEvent());

            if (fileName != null) {
                if (event.getLogEvent().getLevel().toInt() >= Level.WARN_INT) {
                    if (!fileToErrors.containsKey(fileName)) {
                        fileToErrors.put(fileName, new ArrayList<ErrorMessage>());
                    }
                    fileToErrors.get(fileName).add(new ErrorMessage(event.getLogEvent().getLevel() == Level.WARN ? ErrorLevel.WARNING : ErrorLevel.ERROR, event.getLogEvent().getMessage().toString()));
                }
            }
        }
        return fileToErrors;
    }

}
