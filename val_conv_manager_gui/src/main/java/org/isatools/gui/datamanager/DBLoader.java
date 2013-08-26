package org.isatools.gui.datamanager;
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
import org.isatools.gui.Globals;
import org.isatools.isatab.gui_invokers.GUIISATABLoader;
import org.isatools.isatab.gui_invokers.GUIInvokerResult;
import org.isatools.tablib.utils.BIIObjectStore;

import javax.swing.*;
import java.io.File;
import java.io.Serializable;

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