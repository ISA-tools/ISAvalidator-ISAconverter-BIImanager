package org.isatools.gui.datamanager;

import org.isatools.gui.Globals;
import org.isatools.gui.errorprocessing.ErrorReport;
import org.isatools.isatab.gui_invokers.GUIISATABLoader;
import org.isatools.isatab.gui_invokers.GUIInvokerResult;
import org.isatools.tablib.utils.BIIObjectStore;
import org.isatools.tablib.utils.logging.TabLoggingEventWrapper;

import javax.swing.*;
import java.io.File;
import java.io.Serializable;
import java.util.List;

public class DBLoader implements Serializable {
    private final DataManagerToolUI dataManagerToolUI;

    public DBLoader(DataManagerToolUI dataManagerToolUI) {
        this.dataManagerToolUI = dataManagerToolUI;
    }

    protected void loadToDatabase(BIIObjectStore store, String isatabSubmissionPath, final String report) {
        try {
            // will dispatch files to parent file
            File isatabLoadingDirector = new File(dataManagerToolUI.getSelectISATABUI().getSelectedFile());

            final String dispatchDirectory = isatabLoadingDirector.getPath();

            File dispatchDirCreate = new File(dispatchDirectory);
            if (!dispatchDirCreate.exists()) {
                dispatchDirCreate.mkdir();
            }

            final GUIISATABLoader isatabLoader = new GUIISATABLoader();

            GUIInvokerResult result = isatabLoader.persist(store, isatabSubmissionPath);

            // in the event of loading failing, tell the user why it didn't load.
            if (result == GUIInvokerResult.ERROR) {
                dataManagerToolUI.getAppContainer().setGlassPanelContents(
                        dataManagerToolUI.createResultPanel(Globals.LOAD_FAILED, Globals.LOAD_ANOTHER,
                                Globals.LOAD_ANOTHER_OVER, Globals.EXIT, Globals.EXIT_OVER, null));
                dataManagerToolUI.getAppContainer().validate();
            } else {

                // show success screen
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {

                        dataManagerToolUI.getProgressIndicator().stop();
                        // should perhaps extract this to the create user management method...

                        final Thread[] threads = new Thread[1];
                        threads[0] = new Thread(new Runnable() {
                            public void run() {
                                dataManagerToolUI.getAppContainer().setGlassPanelContents(dataManagerToolUI.createProgressScreen("finding users..."));
                                dataManagerToolUI.getAppContainer().validate();
                                dataManagerToolUI.getProgressIndicator().start();

                                dataManagerToolUI.createUserManagement(report);
                            }
                        });
                        threads[0].start();
                    }
                });
            }
        } catch (final Exception e) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    dataManagerToolUI.getProgressIndicator().stop();
                    dataManagerToolUI.getAppContainer().setGlassPanelContents(
                            dataManagerToolUI.createResultPanel(Globals.LOAD_FAILED, Globals.LOAD_ANOTHER,
                                    Globals.LOAD_ANOTHER_OVER, Globals.EXIT, Globals.EXIT_OVER, e.getMessage()));
                    dataManagerToolUI.getAppContainer().validate();
                }
            });
        }
    }
}