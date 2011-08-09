package org.isatools.isatab.gui_invokers;

import org.isatools.isatab.commandline.AbstractImportLayerShellCommand;
import org.isatools.isatab.commandline.PersistenceShellCommand;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.model.Study;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * GUIBIIReindex
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 * @date Jun 14, 2010
 */


public class GUIBIIReindex extends AbstractGUIInvokerWithStudySelection {

    public GUIBIIReindex() {
        super();
        AbstractImportLayerShellCommand.setupLog4JPath(System.getProperty("user.dir") + "/reindexer.log");
        initEntityManager();
    }


    public GUIInvokerResult reindexSelectedStudies(Set<String> studyIds) {
        try {
            BIIObjectStore store = new BIIObjectStore();


            if (loadStudiesFromDB() == GUIInvokerResult.SUCCESS) {

                Collection<Study> studies = getRetrievedStudies();
                if (studies == null || studies.size() == 0) {
                    vlog.info("No studies in the BII DB");
                } else {
                    for (Study s : studies) {
                        if (studyIds.contains(s.getAcc())) {
                            store.put(Study.class, s.getAcc(), s);
                        }
                    }
                }
                PersistenceShellCommand.reindexStudiesEfficient(store, entityManager, getHibernateProperties());
                return GUIInvokerResult.SUCCESS;
            } else {
                vlog.error("Failed to load studies from database.");
                return GUIInvokerResult.ERROR;
            }
        } catch (Exception e) {
            e.printStackTrace();
            vlog.error("Problem occurred when reindexing BII database: " + e.getMessage());
            return GUIInvokerResult.ERROR;
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }

        }
    }

    public GUIInvokerResult reindexDatabase() {

        try {
            BIIObjectStore store = new BIIObjectStore();

            if (loadStudiesFromDB() == GUIInvokerResult.SUCCESS) {

                Collection<Study> studies = getRetrievedStudies();
                if (studies == null || studies.size() == 0) {
                    vlog.info("No studies in the BII DB");
                } else {
                    for (Study s : studies) {
                        store.put(Study.class, s.getAcc(), s);
                    }
                }
                PersistenceShellCommand.reindexStudiesEfficient(store, entityManager, getHibernateProperties());
                return GUIInvokerResult.SUCCESS;
            } else {
                vlog.error("Failed to load studies from database.");
                return GUIInvokerResult.ERROR;
            }
        } catch (Exception e) {
            e.printStackTrace();
            vlog.error("Problem occurred when reindexing BII database: " + e.getMessage());
            return GUIInvokerResult.ERROR;
        } finally {
            entityManager.close();
        }
    }

}
