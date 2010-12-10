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

package org.isatools.isatab.gui_invokers;

import org.isatools.isatab.ISATABPersister;
import org.isatools.isatab.commandline.AbstractImportLayerShellCommand;
import org.isatools.isatab.commandline.PersistenceShellCommand;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.dao.ejb3.DaoFactory;
import uk.ac.ebi.bioinvindex.utils.i18n;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Properties;


/**
 * A Draft of the ISATAB validator.
 * <p/>
 * For the moment the validator simply tries to load the ISATAB and map it to the BII model. This interface is intended
 * to be used by another Java program that need to validate a submission (we wrote it for the ISATABCreator).
 *
 * @author brandizi
 *         <b>date</b>: Apr 5, 2009
 */
public class GUIISATABLoader extends AbstractGUIInvoker {
    // private final EntityManager entityManager;

    public GUIISATABLoader() {
        super();
    }


    /**
     * Persist the store, assuming it has already been loaded.
     */
    public GUIInvokerResult persist(BIIObjectStore store, String isatabSubmissionPath) {
        try {
            vlog.info("Persisting " + store.size() + " object(s)");

            Properties hibProps = AbstractImportLayerShellCommand.getHibernateProperties();
            hibProps.setProperty("hibernate.search.indexing_strategy", "manual");
            hibProps.setProperty("hbm2ddl.drop", "false");
            hibProps.setProperty("hibernate.hbm2ddl.auto", "update");
            EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("BIIEntityManager", hibProps);
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            ISATABPersister persister = new ISATABPersister(store, DaoFactory.getInstance(entityManager));

            EntityTransaction transaction = entityManager.getTransaction();
            transaction.begin();
            Timestamp ts = persister.persist(isatabSubmissionPath);
            transaction.commit();
            entityManager.close();

            PersistenceShellCommand.reindexStudies(store, hibProps);

            vlog.info("\n\n" + i18n.msg("mapping_done_data_saved_in_db"));
            vlog.debug("\n\n" + i18n.msg("submission_done_ts_reported",
                    "" + ts.getTime(),
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").format(ts) + "." + ts.getNanos()) + "\n"
            );

            return GUIInvokerResult.SUCCESS;
        }
        catch (Exception ex) {
            vlog.error(ex.getMessage(), ex);
            return GUIInvokerResult.ERROR;
        }
    }
}
