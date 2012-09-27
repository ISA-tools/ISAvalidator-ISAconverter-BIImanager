/*
 * __________
 * CREDITS
 * __________
 *
 * Team page: http://isatab.sf.net/
 * - Marco Brandizi (software engineer: ISAvalidator, ISAconverter, BII data management utility, BII model)
 * - Eamonn Maguire (software engineer: ISAcreator, ISAcreator configurator, ISAvalidator, ISAconverter,  BII data management utility, BII web)
 * - Nataliya Sklyar (software engineer: BII web application, BII model,  BII data management utility)
 * - Philippe Rocca-Serra (technical coordinator: user requirements and standards compliance for ISA software, ISA-tab format specification, BII model, ISAcreator wizard, ontology)
 * - Susanna-Assunta Sansone (coordinator: ISA infrastructure design, standards compliance, ISA-tab format specification, BII model, funds raising)
 *
 * Contributors:
 * - Manon Delahaye (ISA team trainee:  BII web services)
 * - Richard Evans (ISA team trainee: rISAtab)
 *
 *
 * ______________________
 * Contacts and Feedback:
 * ______________________
 *
 * Project overview: http://isatab.sourceforge.net/
 *
 * To follow general discussion: isatab-devel@list.sourceforge.net
 * To contact the developers: isatools@googlegroups.com
 *
 * To report bugs: http://sourceforge.net/tracker/?group_id=215183&atid=1032649
 * To request enhancements:  http://sourceforge.net/tracker/?group_id=215183&atid=1032652
 *
 *
 * __________
 * License:
 * __________
 *
 * This work is licenced under the Creative Commons Attribution-Share Alike 2.0 UK: England & Wales License. To view a copy of this licence, visit http://creativecommons.org/licenses/by-sa/2.0/uk/ or send a letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California 94105, USA.
 *
 * __________
 * Sponsors
 * __________
 * This work has been funded mainly by the EU Carcinogenomics (http://www.carcinogenomics.eu) [PL 037712] and in part by the
 * EU NuGO [NoE 503630](http://www.nugo.org/everyone) projects and in part by EMBL-EBI.
 */

package org.isatools.isatab_v1;

import org.apache.commons.lang.StringUtils;
import org.dbunit.operation.DatabaseOperation;
import org.isatools.isatab.gui_invokers.GUIISATABLoader;
import org.isatools.isatab.gui_invokers.GUIISATABUnloader;
import org.isatools.isatab.gui_invokers.GUIISATABValidator;
import org.isatools.isatab.gui_invokers.GUIInvokerResult;
import org.isatools.tablib.utils.BIIObjectStore;
import org.junit.Ignore;
import org.junit.Test;
import uk.ac.ebi.bioinvindex.model.Identifiable;
import uk.ac.ebi.bioinvindex.model.Study;
import uk.ac.ebi.bioinvindex.persistence.Persister;
import uk.ac.ebi.bioinvindex.utils.datasourceload.DataLocationManager;
import uk.ac.ebi.bioinvindex.utils.test.TransactionalDBUnitEJB3DAOTest;

import javax.persistence.Query;
import java.io.File;

import static java.lang.System.out;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RealCasesTest extends TransactionalDBUnitEJB3DAOTest {
	private String baseDir;
	private String testsPath;


	public RealCasesTest() throws Exception {
		super();
	}

	protected void prepareSettings() {
		beforeTestOperations.add(DatabaseOperation.CLEAN_INSERT);
		dataSetLocation = null;

		baseDir = System.getProperty("basedir");
		testsPath = baseDir + "/target/test-classes/test-data/isatab/isatab_real_cases/";
	}


	private void runSingleTest(String isatabPath) throws Exception {
		String label = "'" + isatabPath + "'";
		if (Persister.isLightPersistence()) {
			label += " (Light Mode)";
		}
		out.println("\n\n" + StringUtils.center(" Real Case Test on " + label, 180, "=-") + "\n");

		// initEntityManager ( true );

		GUIISATABValidator validator = new GUIISATABValidator();
		GUIInvokerResult result = validator.validate(isatabPath);
		BIIObjectStore store = validator.getStore();

		assertFalse("Validation returns an error!", GUIInvokerResult.ERROR.equals(result));
		assertTrue("No study loaded, Store is empty!", store.valuesOfType(Study.class).size() > 0);
		out.println("Loading of " + store.size() + " objects. Result is " + result + ". Now persisting.");

		GUIISATABLoader loader = new GUIISATABLoader();
		result = loader.persist(store, isatabPath);
		out.println("\n_____ Persistence done. Result is " + result + ". Now unloading.");
		assertFalse("Peristence returns an error!", GUIInvokerResult.ERROR.equals(result));

		String repoPath = baseDir + "/target/bii_test_repo/meta_data";
		Study study = store.valueOfType(Study.class);
		String studyFileName = "study_" + DataLocationManager.getObfuscatedStudyFileName(study);

		String submissionRepoPath = repoPath + "/" + studyFileName;
		assertTrue("Oh no! Submission directory not created in the submission repo: " + submissionRepoPath + "!",
				new File(submissionRepoPath).exists()
		);

		session.flush();

		GUIISATABUnloader unloader = new GUIISATABUnloader();
		result = unloader.unload(store.valuesOfType(Study.class));
		out.println("\n_____ Unloading done. Result is " + result + ".");
		assertFalse("Unloading returns an error!", GUIInvokerResult.ERROR.equals(result));

		session.flush();

		Query q = entityManager.createQuery("SELECT e FROM " + Identifiable.class.getName() + " e WHERE e.submissionTs = :ts");
		q.setParameter("ts", study.getSubmissionTs());
		boolean isAllUnloaded = true;
		for (Object o : q.getResultList()) {
			out.println("**** Oh No! I have found an entity that should be unloaded! " + o);
			isAllUnloaded = false;
		}
		assertTrue("Sigh! I still have some objects that were not unloaded", isAllUnloaded);

		assertFalse("Oh no! Submission directory not deleted from the submission repo: " + submissionRepoPath + "!",
				new File(submissionRepoPath).exists()
		);

		out.println("\n" + StringUtils.center(" /end: Real Case Test on " + label, 180, "=-") + "\n");
	}

	@Test
	public void testHarwardCD133() throws Exception {
		runSingleTest(testsPath + "harvard-CD133");
	}

	@Test
	public void testBII_I_1_v201001() throws Exception {
		runSingleTest(testsPath + "BII-I-1_v201001");
	}

	@Test
	public void testHarwardCD133_Light() throws Exception {
		System.setProperty(Persister.LIGHT_PERSISTENCE_PROPERTY, "true");
		runSingleTest(testsPath + "harvard-CD133");
	}

	@Ignore
	public void testBII_I_1_v201001_Light() throws Exception {
		System.setProperty(Persister.LIGHT_PERSISTENCE_PROPERTY, "true");
		runSingleTest(testsPath + "BII-I-1_v201001");
	}

}
