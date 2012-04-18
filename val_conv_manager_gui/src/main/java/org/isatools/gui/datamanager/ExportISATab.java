package org.isatools.gui.datamanager;

import org.isatools.gui.Globals;
import org.isatools.gui.datamanager.exportisa.ExportISAGUI;
import org.isatools.gui.datamanager.exportisa.SuccessOrFailureGUI;
import org.isatools.gui.errorprocessing.ErrorReport;
import org.isatools.isatab.gui_invokers.GUIISATABExporter;
import org.isatools.isatab.gui_invokers.GUIInvokerResult;
import org.jdesktop.fuse.InjectedResource;
import org.jdesktop.fuse.ResourceInjector;
import uk.ac.ebi.bioinvindex.model.Study;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.Collection;

public class ExportISATab implements Serializable {

    @InjectedResource
    private ImageIcon exportFailureImage;

    private final DataManagerToolUI dataManagerToolUI;
    private ExportISAGUI exportISAGUI;

    public ExportISATab(DataManagerToolUI dataManagerToolUI) {
        ResourceInjector.get("datamanager-package.style").inject(this);
        this.dataManagerToolUI = dataManagerToolUI;
    }

    void createExportISATabInterface() {
        final Thread[] threads = new Thread[1];
        threads[0] = new Thread(new Runnable() {
            public void run() {
                try {

                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            dataManagerToolUI.getAppContainer().setGlassPanelContents(dataManagerToolUI.createProgressScreen("finding studies..."));
                            dataManagerToolUI.getAppContainer().validate();
                            dataManagerToolUI.getProgressIndicator().start();
                        }
                    });

                    exportISAGUI = new ExportISAGUI(dataManagerToolUI.getAppContainer());

                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            exportISAGUI.createGUI();
                        }
                    });

                    exportISAGUI.addPropertyChangeListener("toMenu", new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                            dataManagerToolUI.getAppContainer().setGlassPanelContents(dataManagerToolUI.getLoaderMenu());
                            dataManagerToolUI.getAppContainer().validate();
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
                                            dataManagerToolUI.getAppContainer().setGlassPanelContents(dataManagerToolUI.createProgressScreen("exporting ISATab..."));
                                            dataManagerToolUI.getAppContainer().validate();
                                            dataManagerToolUI.getProgressIndicator().start();
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

                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            dataManagerToolUI.getProgressIndicator().stop();
                            dataManagerToolUI.getAppContainer().setGlassPanelContents(exportISAGUI);
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
        });
        threads[0].start();
    }

    void exportSelectedStudiesToISATab(Collection<Study> studies, boolean exportDataFiles) {
        exportSelectedStudiesToISATab(studies, null, exportDataFiles);
    }

    void exportSelectedStudiesToISATab(Collection<Study> studies, String exportFileLocation, boolean exportDataFiles) {
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
                dataManagerToolUI.createResultPanel(exportFailureImage, Globals.BACK_MAIN, Globals.BACK_MAIN_OVER, Globals.EXIT,
                        Globals.EXIT_OVER, null);
            } else {
                SuccessOrFailureGUI successGUI = new SuccessOrFailureGUI(dataManagerToolUI.getAppContainer(), SuccessOrFailureGUI.EXPORT_SUCCESS,
                        exportISAGUI.generateExportSuccessMessage());

                successGUI.addPropertyChangeListener("toMenu", new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                        dataManagerToolUI.getAppContainer().setGlassPanelContents(dataManagerToolUI.getLoaderMenu());
                        dataManagerToolUI.getAppContainer().validate();
                    }
                });

                dataManagerToolUI.getAppContainer().setGlassPanelContents(successGUI);
                dataManagerToolUI.getProgressIndicator().stop();
                dataManagerToolUI.getAppContainer().validate();

            }

        } catch (Exception e) {
            SuccessOrFailureGUI failureGUI = new SuccessOrFailureGUI(dataManagerToolUI.getAppContainer(), SuccessOrFailureGUI.EXPORT_FAILURE,
                    exportISAGUI.generateExportErrorMessage(e));

            failureGUI.addPropertyChangeListener("toMenu", new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                    dataManagerToolUI.getAppContainer().setGlassPanelContents(dataManagerToolUI.getLoaderMenu());
                    dataManagerToolUI.getAppContainer().validate();
                }
            });

            dataManagerToolUI.getAppContainer().setGlassPanelContents(failureGUI);
            dataManagerToolUI.getProgressIndicator().stop();
            dataManagerToolUI.getAppContainer().validate();
        }
    }
}