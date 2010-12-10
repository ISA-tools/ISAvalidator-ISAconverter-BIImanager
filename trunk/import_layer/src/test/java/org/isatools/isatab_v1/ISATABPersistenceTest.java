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
 * - Manon Delahaye (ISA team trainee: BII web services)
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
 * To request enhancements: Êhttp://sourceforge.net/tracker/?group_id=215183&atid=1032652
 *
 *
 * __________
 * License:
 * __________
 *
 * Reciprocal Public License 1.5 (RPL1.5)
 * [OSI Approved License]
 *
 * Reciprocal Public License (RPL)
 * Version 1.5, July 15, 2007
 * Copyright (C) 2001-2007
 * Technical Pursuit Inc.,
 * All Rights Reserved.
 *
 * http://www.opensource.org/licenses/rpl1.5.txt
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
import org.isatools.isatab.ISATABPersister;
import org.isatools.isatab_v1.mapping.ISATABMapper;
import org.isatools.tablib.schema.FormatSetInstance;
import org.isatools.tablib.utils.BIIObjectStore;
import org.junit.Test;
import uk.ac.ebi.bioinvindex.dao.ejb3.DaoFactory;
import uk.ac.ebi.bioinvindex.model.Study;
import uk.ac.ebi.bioinvindex.model.processing.Assay;
import uk.ac.ebi.bioinvindex.model.processing.Processing;
import uk.ac.ebi.bioinvindex.model.xref.ReferenceSource;
import uk.ac.ebi.bioinvindex.model.xref.Xref;
import uk.ac.ebi.bioinvindex.utils.DotGraphGenerator;
import uk.ac.ebi.bioinvindex.utils.datasourceload.DataLocationManager;
import uk.ac.ebi.bioinvindex.utils.test.TransactionalDBUnitEJB3DAOTest;

import java.io.File;
import java.sql.Timestamp;

import static java.lang.System.out;
import static org.junit.Assert.assertTrue;

public class ISATABPersistenceTest extends TransactionalDBUnitEJB3DAOTest {

	public ISATABPersistenceTest() throws Exception {
		super();
	}

	protected void prepareSettings() {
		//beforeTestOperations.clear ();
		beforeTestOperations.add(DatabaseOperation.CLEAN_INSERT);
		dataSetLocation = "test-data/isatab/db_datasets/test_unloading.xml";
		dataSetLocation = null;
	}


	@SuppressWarnings("static-access")
	@Test
	public void testPersistence() throws Exception {

		out.println("\n\n_______________________ ISATAB Persistence Test _______________________\n\n");

		String baseDir = System.getProperty("basedir");
		String filesPath = baseDir + "/target/test-classes/test-data/isatab/isatab_v1_200810/griffin_gauguier_200810";
		ISATABLoader loader = new ISATABLoader(filesPath);
		FormatSetInstance isatabInstance = loader.load();

		BIIObjectStore store = new BIIObjectStore();
		ISATABMapper isatabMapper = new ISATABMapper(store, isatabInstance);

		isatabMapper.map();

		assertTrue("Oh no! No mapped object! ", store.size() > 0);

		DotGraphGenerator dotter = new DotGraphGenerator(store.values(Processing.class));
		String dotPath = filesPath + "/graph.dot";
		// WILL NEVER WORK WITH THIS CAUSE IT ASSIGNS IDs!!!
//		dotter.createGraph ( dotPath );
//		out.println ( "Graph saved into " + dotPath );

		out.println("\n_____________ Persisting the objects:\n" + isatabMapper.report(store));

		// Test the repository too
		String repoPath = baseDir + "/target/bii_test_repo/meta_data";
//		File repoDir = new File ( repoPath );
//		if ( !repoDir.exists () )
//			FileUtils.forceMkdir ( repoDir );

		ISATABPersister persister = new ISATABPersister(store, DaoFactory.getInstance(entityManager));
		Timestamp ts = persister.persist(filesPath);
		transaction.commit();

		// TODO: close sesssion, retrieve objects from DB, check they correspond to the submission

		Study study2 = store.getType(Study.class, "S:GG200810:2");
		String study2FileName = "study_" + DataLocationManager.getObfuscatedStudyFileName(study2);

		String submissionRepoPath2 = repoPath + "/" + study2FileName;
		assertTrue("Oh no! Submission directory not created in the submission repo: " + submissionRepoPath2 + "!",
				new File(submissionRepoPath2).exists()
		);
		assertTrue("Oh no! Submission file investigation.csv didn't go to the submission repository " + submissionRepoPath2 + "!",
				new File(submissionRepoPath2 + "/investigation.csv").exists()
		);
		assertTrue("Oh no! Submission file s-Study-Griffin.txt didn't go to the submission repository " + submissionRepoPath2 + "!",
				new File(submissionRepoPath2 + "/s-Study-Griffin.txt").exists()
		);

		Study study1 = store.getType(Study.class, "S:GG200810:1");
		String study1FileName = "study_" + DataLocationManager.getObfuscatedStudyFileName(study1);

		String submissionRepoPath1 = repoPath + "/" + study1FileName;
		assertTrue("Oh no! Submission file a-S1.A3.txt didn't go to the submission repository " + submissionRepoPath1 + "!",
				new File(submissionRepoPath1 + "/a-S1.A3.txt").exists()
		);

		String
				medaRepoPath = baseDir + "/target/bii_test_repo/meda",
				nonMedaRepoPath = baseDir + "/target/bii_test_repo/generic";

		assertTrue("Oh no! MEDA file repo wasn't created: " + medaRepoPath + "!",
				new File(medaRepoPath).exists()
		);
		assertTrue("Oh no! non-MEDA file repo wasn't created: " + nonMedaRepoPath + "!",
				new File(nonMedaRepoPath).exists()
		);
		assertTrue("Oh no! non-MEDA file clinchem.txt didn't go to its repository " + nonMedaRepoPath,
				new File(nonMedaRepoPath + "/" + study2FileName + "/raw_data/clinchem.txt").exists()
		);

		boolean hasSomeAnn = false;
		for (Study study : store.valuesOfType(Study.class)) {
			for (Assay assay : study.getAssays()) {
				for (Xref xref : assay.getXrefs()) {
					ReferenceSource xsrc = xref.getSource();
					if (StringUtils.contains(xsrc.getDescription(), "Data Files Repository")) {
						hasSomeAnn = true;
						break;
					}
				}
			}
		}
		assertTrue("Ops! I didn't find any assay annotation about their linked data files!", hasSomeAnn);


		out.println("\n\n\n\n________________ Done, Submission TS: " + ts.getTime() + " (" + ts + " + " + ts.getNanos() + "ns)");
		out.println("  Results:\n" + store.toStringVerbose());
		out.println("\n\n___________________ /end: ISATAB Persistence Test ___________________\n\n");
	}


}
