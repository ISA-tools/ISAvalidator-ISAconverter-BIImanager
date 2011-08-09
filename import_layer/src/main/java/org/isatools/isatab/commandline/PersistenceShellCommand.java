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

package org.isatools.isatab.commandline;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.isatools.isatab.ISATABPersister;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.dao.StudyDAO;
import uk.ac.ebi.bioinvindex.dao.ejb3.DaoFactory;
import uk.ac.ebi.bioinvindex.model.Study;
import uk.ac.ebi.bioinvindex.utils.i18n;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Properties;

import static java.lang.System.out;

/**
 * Performs the persistence into the BII database.
 * <p/>
 * <dl><dt>date:</dt><dd>Jun 6, 2008</dd></dl>
 *
 * @author brandizi
 */
public class PersistenceShellCommand extends AbstractImportLayerShellCommand {

    public static void main(String[] args) {
        EntityTransaction transaction = null;
        try {

            Options clopts = createCommonOptions();
            CommandLine cmdl = AbstractImportLayerShellCommand.parseCommandLine(
                    clopts, args, PersistenceShellCommand.class
            );

            args = cmdl.getArgs();
            if (args == null || args.length == 0) {
                printUsage(clopts);
                System.exit(1);
            }

            setup(args);
            setupLog4JPath(cmdl, null);

            // Need to initialize this here, otherwise above config will fail
            log = Logger.getLogger(PersistenceShellCommand.class);

            Properties hibProps = AbstractImportLayerShellCommand.getHibernateProperties();
            hibProps.setProperty("hibernate.search.indexing_strategy", "manual");
            hibProps.setProperty("hibernate.hbm2ddl.auto", "update");
            hibProps.setProperty("hbm2ddl.drop", "false");

            EntityManagerFactory entityManagerFactory =
                    Persistence.createEntityManagerFactory("BIIEntityManager", hibProps);
            EntityManager entityManager = entityManagerFactory.createEntityManager();

            BIIObjectStore store = loadIsaTab();
            log.info(i18n.msg("mapping_done_now_persisting", store.size()));

            ISATABPersister persister = new ISATABPersister(store, DaoFactory.getInstance(entityManager));
            transaction = entityManager.getTransaction();
            transaction.begin();
            Timestamp ts = persister.persist(sourceDirPath);
            transaction.commit();
            entityManager.close();

            reindexStudies(store, hibProps);

            log.info("\n\n" + i18n.msg("mapping_done_data_saved_in_db"));
            log.info("\n\n" + i18n.msg("submission_done_ts_reported",
                    "" + ts.getTime(),
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").format(ts) + "." + ts.getNanos()) + "\n"
            );
            System.exit(0);
        } catch (Exception ex) {
            String msg = "ERROR: problem while running the ISATAB loader: " + ex.getMessage();
            if (log == null) {
                out.println(msg + "\n");
                ex.printStackTrace();
            } else {
                log.fatal(msg, ex);
            }
            System.exit(1);
        }
    }

    /**
     * TODO: this is a patch used until we are able to make the auto-indexing upon load feature working
     * It is called after persistence.
     *
     * @param store               must contain the studies that have to be reindexed (with proper accession).
     * @param entityManager1 - use a supplied entity manager.
     * @param hibernateProperties - The hibernate properties indicating DB properties and Index location/Strategy,
     *                            and so forth.
     */
    public static void reindexStudiesEfficient(BIIObjectStore store, EntityManager entityManager1, Properties hibernateProperties) {
        // Need to initialize this here, otherwise above config will fail
        log = Logger.getLogger(PersistenceShellCommand.class);

        hibernateProperties.setProperty("hibernate.search.indexing_strategy", "event");
        hibernateProperties.setProperty("hibernate.hbm2ddl.auto", "update");
        hibernateProperties.setProperty("hbm2ddl.drop", "false");

        StudyDAO studyDAO = DaoFactory.getInstance(entityManager1).getStudyDAO();
        FullTextEntityManager fullTxtEm = Search.getFullTextEntityManager(entityManager1);
        EntityTransaction tnx = entityManager1.getTransaction();

        tnx.begin();
        for (Study study : store.valuesOfType(Study.class)) {
            Study dbStudy = studyDAO.getByAcc(study.getAcc());
            if (dbStudy != null) {
                out.println("Indexing Study #" + dbStudy.getAcc());
                fullTxtEm.index(dbStudy);
                log.info("Indexing of Study # " + dbStudy.getAcc() + " is complete!");
            }

        }
        log.info("Commiting & closing Entity Manager.");
        tnx.commit();
    }

    /**
     * TODO: this is a patch used until we are able to make the auto-indexing upon load feature working
     * It is called after persistence.
     *
     * @param store               must contain the studies that have to be reindexed (with proper accession).
     * @param hibernateProperties - The hibernate properties indicating DB properties and Index location/Strategy,
     *                            and so forth.
     */
    public static void reindexStudies(BIIObjectStore store, Properties hibernateProperties) {
        // Need to initialize this here, otherwise above config will fail
        log = Logger.getLogger(PersistenceShellCommand.class);

        hibernateProperties.setProperty("hibernate.search.indexing_strategy", "event");
        hibernateProperties.setProperty("hibernate.hbm2ddl.auto", "update");
        hibernateProperties.setProperty("hbm2ddl.drop", "false");

        EntityManagerFactory emf1 = Persistence.createEntityManagerFactory("BIIEntityManager", hibernateProperties);
        EntityManager entityManager1 = emf1.createEntityManager();

        StudyDAO studyDAO = DaoFactory.getInstance(entityManager1).getStudyDAO();
        FullTextEntityManager fullTxtEm = Search.getFullTextEntityManager(entityManager1);
        EntityTransaction tnx = entityManager1.getTransaction();

        tnx.begin();
        for (Study study : store.valuesOfType(Study.class)) {
            Study dbStudy = studyDAO.getByAcc(study.getAcc());
            if (dbStudy != null) {
                out.println("Indexing Study #" + dbStudy.getAcc());
                fullTxtEm.index(dbStudy);
                log.info("Indexing of Study # " + dbStudy.getAcc() + " is complete!");
            }

        }
        log.info("Commiting & closing Entity Manager.");
        tnx.commit();
        entityManager1.close();
    }

    public static void printUsage(Options opts) {
        out.println();

        HelpFormatter helpFormatter = new HelpFormatter();
        PrintWriter pw = new PrintWriter(out, true);
        helpFormatter.printHelp(pw, 80,
                "load [options] <source-path>",
                "\nImports from an ISA-TAB archive into the BII database.\n\nOptions:",
                opts,
                2, 4,
                "\n",
                false
        );
        printUsageCommonNotes();
    }

}
