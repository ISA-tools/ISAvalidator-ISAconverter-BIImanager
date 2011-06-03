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

package org.isatools.isatab.manager;

//import com.sun.tools.javac.util.Log;
import org.apache.log4j.Logger;
//import org.apache.log4j.lf5.LogLevel;
import org.isatools.isatab.gui_invokers.GUIISATABLoader;
import org.isatools.isatab.gui_invokers.GUIISATABUnloader;
import org.isatools.isatab.gui_invokers.GUIISATABValidator;
import org.isatools.isatab.gui_invokers.GUIInvokerResult;
import org.isatools.isatab.isaconfigurator.ISAConfigurationSet;
//import org.isatools.tablib.utils.logging.TabLoggingEventWrapper;
import uk.ac.ebi.bioinvindex.model.Study;
import uk.ac.ebi.bioinvindex.model.VisibilityStatus;
import uk.ac.ebi.bioinvindex.model.security.User;

import javax.persistence.EntityTransaction;
//import javax.swing.*;
import java.io.File;
import java.util.*;


/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 13/04/2011
 *         Time: 11:32
 */
public class SimpleManager {

    private static Logger log = Logger.getLogger(SimpleManager.class.getName());
    
    private VisibilityStatus status = VisibilityStatus.PUBLIC;

    public SimpleManager() {
    }

    public VisibilityStatus getStatus(){
    	return status;
    }
    public void setStatus(VisibilityStatus status){
    	this.status = status;
    }
    public void loadISATab(String isatabFile, String userName, VisibilityStatus status) throws Exception {
    	this.status = status;
    	loadISAtab(isatabFile, userName);
    }
    
    public void loadISAtab(String isatabFile, String configurationDirectory, String userName) throws Exception {

        if (loadConfiguration(configurationDirectory)) {
            loadISAtab(isatabFile, userName);
        } else {
            log.info("No configuration directory found...");
        }

    }

    public void loadISAtab(String isatabFile, String userName) throws Exception {

        GUIISATABValidator isatabValidator = new GUIISATABValidator();

        GUIInvokerResult validationResult = isatabValidator.validate(isatabFile);
        if (validationResult == GUIInvokerResult.SUCCESS) {
            GUIISATABLoader loader = new GUIISATABLoader();
            log.info("Validation successful, now proceeding to load ISAtab into the BII...");
            GUIInvokerResult loadingResult = loader.persist(isatabValidator.getStore(), isatabFile);


            if (loadingResult == GUIInvokerResult.SUCCESS) {

                // using this call, we can get all objects of type Study from the BIIObjectStore.
                Collection<Study> studies = isatabValidator.getStore().valuesOfType(Study.class);

                Set<String> accessions = new HashSet<String>();

                for (Study study : studies) {
                    accessions.add(study.getAcc());
                }

                changeStudyPermissions(status, userName, accessions.toArray(new String[accessions.size()]));
                log.info("Loading completed and reindexing performed");
            }

        } else {
            log.error("Loading failed. See log for details.");
        }

    }

    /**
     * For given Studies, by their IDs, modifies their visibility
     *
     * @param status     - @see VisibilityStatus
     * @param studyOwner - user to be assigned as an 'owner' of this Study.
     * @param studyIDs   - e.g. "BII-S-1", "BII-S-2"
     */
    public void changeStudyPermissions(VisibilityStatus status, String studyOwner, String[] studyIDs) {
        UserManagementControl umControl = new UserManagementControl();
        log.info("Attempting to change study permissions");
        try {

            EntityTransaction transaction = umControl.getEntityManager().getTransaction();
            transaction.begin();

            umControl.getUsers();

            User owner = umControl.getUserByUsername(studyOwner);

            for (String studyAcc : studyIDs) {
                log.info("Setting permissions for " + studyAcc);
                umControl.changeStudyVisability(studyAcc, status);

                if (owner != null) {
                    umControl.addUserToStudy(studyAcc, owner);
                } else {
                    log.info("No user found, so a user has not been assigned to this study");
                }
            }

            transaction.commit();

        } catch (final Exception e) {
            log.info("A problem occurred when setting permissions");
        }
    }


    public void unLoadISAtab(Set<String> toUnload) {

        GUIISATABUnloader unloaderUtil = new GUIISATABUnloader();

        log.info(toUnload.size() > 1 ? "unloading studies..." : "unloading study");

        GUIInvokerResult result = unloaderUtil.unload(toUnload);

        if (result == GUIInvokerResult.SUCCESS) {
            // fire updates back to the listener(s)
            log.info("Unloading complete. Unloaded " + toUnload.size() + " studies");

        } else {
            log.info("Some problems were encountered when loading");
        }

    }

    private boolean loadConfiguration(String configuration) {
        if (new File(configuration).exists()) {
            ISAConfigurationSet.setConfigPath(configuration);
            return true;
        } else {
            return false;
        }
    }

    public static void main(String[] args) throws Exception {
        SimpleManager manager = new SimpleManager();

        if (args[0].equals("load")) {
            // args[0] should be the isatab directory. args[1] should be the configuration directory
            if (args.length == 3) {
                manager.loadISAtab(args[1], args[2]);
            } else {
                manager.loadISAtab(args[1], args[2], args[3]);
            }
            System.exit(0);
        }


        if (args[0].equals("unload")) {
            if (args.length > 1) {
                Set<String> toUnload = new HashSet<String>();
                toUnload.addAll(Arrays.asList(args).subList(1, args.length));
                manager.unLoadISAtab(toUnload);
                System.exit(0);
            } else {
                log.info("No studies to load.");
            }
        }
    }
}
