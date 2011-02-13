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

package org.isatools.isatab.export.isatab;

import org.isatools.isatab.gui_invokers.GUIISATABExporter;
import org.isatools.isatab.gui_invokers.GUIISATABLoader;
import org.isatools.isatab.gui_invokers.GUIISATABValidator;
import org.isatools.isatab.gui_invokers.GUIInvokerResult;
import org.isatools.tablib.mapping.TabMappingContext;
import org.isatools.tablib.schema.Field;
import org.isatools.tablib.schema.Record;
import org.isatools.tablib.schema.SectionInstance;
import org.isatools.tablib.utils.BIIObjectStore;
import org.junit.Test;
import uk.ac.ebi.bioinvindex.dao.ejb3.DaoFactory;
import uk.ac.ebi.bioinvindex.model.Identifiable;
import uk.ac.ebi.bioinvindex.model.Study;
import uk.ac.ebi.bioinvindex.model.processing.Processing;
import uk.ac.ebi.bioinvindex.utils.DotGraphGenerator;
import uk.ac.ebi.bioinvindex.utils.datasourceload.DataLocationManager;
import uk.ac.ebi.bioinvindex.utils.test.TransactionalDBUnitEJB3DAOTest;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import static java.lang.System.out;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ISATABDBExporterTest extends TransactionalDBUnitEJB3DAOTest {
	private final static String BASEDIR = System.getProperty("basedir");
	private final static String TESTSPATH = BASEDIR + "/target/test-classes/test-data/isatab/isatab_real_cases/";

	/**
	 * Used in {@link ISATABDBExporterTest#testExportToRepo()} to simulate the fact that a data file is no longer
	 * referenced by the new ISATAB.
	 */
	private static class MockISATABDBExporter extends ISATABDBExporter {
		public MockISATABDBExporter(DaoFactory daoFactory, String... studyAccs) {
			super(daoFactory, studyAccs);
		}

		protected ExportedItem exportSubmission(ExportedItem expItem) {
			if (expItem.isatab != null) {
				return expItem;
			}
			expItem = super.exportSubmission(expItem);
			for (SectionInstance sectionInstance :
					expItem.isatab.getSectionInstances("generic_assay", "generic_assay_pipeline")
					) {
				Field field = sectionInstance.getField("Raw Data File");
				if (field == null) {
					continue;
				}
				int idx = field.getIndex();
				for (Record record : sectionInstance.getRecords()) {
					if ("LM-Mel-14.TIFF".equals(record.get(idx))) {
						record.set(idx, "LM-Mel-14_new_value.txt");
					}
				}
			}
			return expItem;
		}
	}


	public ISATABDBExporterTest() throws Exception {
		super();
		cleanAll();
	}


	protected void prepareSettings() {
		// beforeTestOperations.add ( DatabaseOperation.CLEAN_INSERT );
		beforeTestOperations = null;
		dataSetLocation = null;
	}


	private void load(String isatabPath) throws Exception {
		out.println("Loading test submission: " + isatabPath);

		// initEntityManager ( true );

		GUIISATABValidator validator = new GUIISATABValidator();
		GUIInvokerResult result = validator.validate(isatabPath);
		BIIObjectStore store = validator.getStore();

		assertFalse("Validation returns an error!", GUIInvokerResult.ERROR.equals(result));
		assertTrue("No study loaded, Store is empty!", store.valuesOfType(Study.class).size() > 0);
		out.println("Loading of " + store.size() + " objects. Result is " + result + ". Now persisting.");


		GUIISATABLoader loader = new GUIISATABLoader();
		result = loader.persist(store, isatabPath);
		out.println("\n_____ Persistence done. Result is " + result + ".");
		assertFalse("Peristence returns an error!", GUIInvokerResult.ERROR.equals(result));

		Collection<Identifiable> objects = new ArrayList<Identifiable>();
		for (Class<? extends Identifiable> ic : store.types()) {
			for (Identifiable o : store.valuesOfType(ic)) {
				if (!(o instanceof TabMappingContext)) {
					o.setId(null);
				}
			}
		}

		objects.addAll(store.values(Processing.class));
		DotGraphGenerator dotter = new DotGraphGenerator(objects);
		String dotFilePath = isatabPath + "/graph.dot";
		dotter.createGraph(dotFilePath);
		out.println("DOT file created in " + dotFilePath + ".");


		String repoPath = BASEDIR + "/target/bii_test_repo/meta_data";
		Study study = store.valueOfType(Study.class);
		String studyFileName = "study_" + DataLocationManager.getObfuscatedStudyFileName(study);

		String submissionRepoPath = repoPath + "/" + studyFileName;
		assertTrue("Oh no! Submission directory not created in the submission repo: " + submissionRepoPath + "!",
				new File(submissionRepoPath).exists()
		);

		session.flush();
		out.println(" /end of loading\n");
	}

	@Test
	public void testExportToPath() throws Exception {
		load(TESTSPATH + "harvard-CD133");
		load(TESTSPATH + "BII-I-1_v201001");

		ISATABDBExporter dbexp = new ISATABDBExporter(daoFactory, "BII-S-1", "BII-S-2", "HSPH-1");
		// ISATABDBExporter dbexp = new ISATABDBExporter ( daoFactory, "BII-S-1", "BII-S-2" );
		// ISATABDBExporter dbexp = new ISATABDBExporter ( daoFactory, "HSPH-1" );

		final String EXPPATH = BASEDIR + "/target/isatab_export";
		dbexp.exportToPath(EXPPATH);
		assertTrue("Argh! BII-I-1 not exported!", new File(EXPPATH + "/BII-I-1").exists());
		assertTrue("Argh! HSPH-1 not exported!", new File(EXPPATH + "/HSPH-1").exists());
	}

	@Test
	public void testExportToRepo() throws Exception {
		load(TESTSPATH + "harvard-CD133");
		ISATABDBExporter dbexp = new MockISATABDBExporter(daoFactory, "HSPH-1");

		dbexp.exportToRepository();
		// TODO: check they were moved to the repo
	}

	@Test
	public void testGUIInvoker() throws Exception {
		// Loads the test submission
		load(TESTSPATH + "harvard-CD133");

		// From now on: how to use the GUI invoker
		GUIISATABExporter guiExp = new GUIISATABExporter();
		guiExp.loadStudiesFromDB();

		// You can Add/remove values to this collection, on the basis of user selection
		// I show and test this below.
		Collection<Study> studies = guiExp.getRetrievedStudies();
		for (Study study : new ArrayList<Study>(studies)) {
			if (!"HSPH-1".equals(study.getAcc())) {
				studies.remove(study);
			}
		}
		// See also the other methods in this class for options and local path export 
		GUIInvokerResult result = guiExp.isatabExportToRepository(studies);
		assertFalse("Urp! The GUI returns ERROR!", GUIInvokerResult.ERROR.equals(result));
	}
}
