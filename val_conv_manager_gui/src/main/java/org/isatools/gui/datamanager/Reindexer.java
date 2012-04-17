package org.isatools.gui.datamanager;

import org.isatools.gui.datamanager.exportisa.SuccessOrFailureGUI;
import org.isatools.isatab.gui_invokers.GUIBIIReindex;
import org.isatools.isatab.gui_invokers.GUIInvokerResult;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;

public class Reindexer implements Serializable {
    private final DataManagerToolUI dataManagerToolUI;

    public Reindexer(DataManagerToolUI dataManagerToolUI) {
        this.dataManagerToolUI = dataManagerToolUI;
    }

    void performReindexing() {
        final Thread[] threads = new Thread[1];
        threads[0] = new Thread(new Runnable() {
            public void run() {
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            dataManagerToolUI.getAppContainer().setGlassPanelContents(dataManagerToolUI.createProgressScreen("reindexing..."));
                            dataManagerToolUI.getAppContainer().validate();
                            dataManagerToolUI.getProgressIndicator().start();
                        }
                    });

                    GUIBIIReindex reindexer = new GUIBIIReindex();

                    final SuccessOrFailureGUI successOrFailGUI;

                    if (reindexer.reindexDatabase() == GUIInvokerResult.SUCCESS) {
                        successOrFailGUI = new SuccessOrFailureGUI(dataManagerToolUI.getAppContainer(), SuccessOrFailureGUI.INDEX_SUCCESS,
                                "<html>Reindexing of the BII database has completed successfully!</html>");

                    } else {
                        successOrFailGUI = new SuccessOrFailureGUI(dataManagerToolUI.getAppContainer(), SuccessOrFailureGUI.INDEX_FAILURE, null);
                    }

                    successOrFailGUI.addPropertyChangeListener("toMenu", new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                            dataManagerToolUI.getAppContainer().setGlassPanelContents(dataManagerToolUI.getLoaderMenu());
                            dataManagerToolUI.getAppContainer().validate();
                        }
                    });

                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            dataManagerToolUI.getAppContainer().setGlassPanelContents(successOrFailGUI);
                            dataManagerToolUI.getProgressIndicator().stop();
                            dataManagerToolUI.getAppContainer().validate();
                        }
                    });


                } catch (final Exception e) {
                    e.printStackTrace();
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            SuccessOrFailureGUI successOrFailGUI = new SuccessOrFailureGUI(dataManagerToolUI.getAppContainer(), SuccessOrFailureGUI.INDEX_FAILURE,
                                    e.getMessage());
                            dataManagerToolUI.getAppContainer().setGlassPanelContents(successOrFailGUI);
                            dataManagerToolUI.getProgressIndicator().stop();
                            dataManagerToolUI.getAppContainer().validate();
                        }
                    });
                }
            }
        });

        threads[0].start();
    }
}