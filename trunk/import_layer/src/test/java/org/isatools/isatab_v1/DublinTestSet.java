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

import org.apache.commons.io.FileUtils;
import org.dbunit.operation.DatabaseOperation;
import org.isatools.isatab.ISATABPersister;
import org.isatools.isatab_v1.mapping.ISATABMapper;
import org.isatools.tablib.schema.FormatSetInstance;
import org.isatools.tablib.utils.BIIObjectStore;
import org.junit.Test;
import uk.ac.ebi.bioinvindex.dao.ejb3.DaoFactory;
import uk.ac.ebi.bioinvindex.model.Study;
import uk.ac.ebi.bioinvindex.utils.datasourceload.DataLocationManager;
import uk.ac.ebi.bioinvindex.utils.test.TransactionalDBUnitEJB3DAOTest;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;

import static java.lang.System.out;
import static org.junit.Assert.assertTrue;

public class DublinTestSet extends TransactionalDBUnitEJB3DAOTest {

	public DublinTestSet() throws Exception {
		super();
	}

	protected void prepareSettings() {
		//beforeTestOperations.clear ();
		beforeTestOperations.add(DatabaseOperation.CLEAN_INSERT);
		dataSetLocation = "test-data/isatab/db_datasets/empty.xml";
	}


	@SuppressWarnings("static-access")
	@Test
	public void testPersistence() throws Exception {

		out.println("\n\n_______________________ Dublin Test Set Persistence _______________________\n\n");

		persist("/target/test-classes/test-data/isatab/isatab_v1_200810/iconix_20081107red");
		persist("/target/test-classes/test-data/isatab/isatab_creator_200811/griffin-gauguier");
		persist("/target/test-classes/test-data/isatab/isatab_creator_200811/gg_proteomics");

		out.println("\n\n___________________ /end: Dublin Test Set Persistence ___________________\n\n");
	}


	@SuppressWarnings("static-access")
	private void persist(String path) throws IOException {
		out.println("\n\n_________ Loading and mapping: " + path + " _______\n\n");

		String baseDir = System.getProperty("basedir");
		String filesPath = baseDir + path;
		ISATABLoader loader = new ISATABLoader(filesPath);

		FormatSetInstance isatabInstance = loader.load();

		out.println("\n\n_____ Loaded, now mapping");
		BIIObjectStore store = new BIIObjectStore();
		ISATABMapper isatabMapper = new ISATABMapper(store, isatabInstance);

		isatabMapper.map();
		assertTrue("Oh no! No mapped object! ", store.size() > 0);

		out.println("\n_____________ Persisting");

		// Test the repository too
		String repoPath = baseDir + "/target/bii_test_repo/meta_data";
		File repoDir = new File(repoPath);
		if (!repoDir.exists()) {
			FileUtils.forceMkdir(repoDir);
		}

		if (!transaction.isActive()) {
			transaction.begin();
		}
		ISATABPersister persister = new ISATABPersister(store, DaoFactory.getInstance(entityManager));
		Timestamp ts = persister.persist(filesPath);
		transaction.commit();

		for (Study study : store.valuesOfType(Study.class)) {
			assertTrue("Oh no! Submission didn't go to the repository!",
					new File(repoPath + "/" + DataLocationManager.getObfuscatedStudyFileName(study)).exists()
			);
		}

		out.println("\n\n\n\n_______________ Done, Submission TS: " + ts.getTime() + " (" + ts + " + " + ts.getNanos() + "ns)");
	}

}
