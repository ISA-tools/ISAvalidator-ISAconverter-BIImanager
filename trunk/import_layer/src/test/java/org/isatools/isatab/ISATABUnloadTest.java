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

package org.isatools.isatab;

import org.dbunit.operation.DatabaseOperation;
import org.isatools.isatab.mapping.ISATABMapper;
import org.isatools.tablib.schema.FormatSetInstance;
import org.isatools.tablib.utils.BIIObjectStore;
import org.junit.Test;
import uk.ac.ebi.bioinvindex.dao.ejb3.DaoFactory;
import uk.ac.ebi.bioinvindex.model.Identifiable;
import uk.ac.ebi.bioinvindex.model.Study;
import uk.ac.ebi.bioinvindex.utils.test.TransactionalDBUnitEJB3DAOTest;

import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.Collection;

import static java.lang.System.out;
import static org.junit.Assert.assertTrue;

public class ISATABUnloadTest extends TransactionalDBUnitEJB3DAOTest {

	public ISATABUnloadTest() throws Exception {
		super();
	}

	protected void prepareSettings() {
		beforeTestOperations.add(DatabaseOperation.CLEAN_INSERT);
		dataSetLocation = "test-data/isatab/db_datasets/test_unloading.xml";
	}

	@Test
	public void testUnloading() throws Exception {
		out.println("\n\n_______________________ ISATAB Unloading Test _______________________\n\n");

		String baseDir = System.getProperty("basedir");
		String filesPath = baseDir + "/target/test-classes/test-data/isatab/isatab/example";
		ISATABLoader loader = new ISATABLoader(filesPath);
		FormatSetInstance isatabInstance = loader.load();

		BIIObjectStore store = new BIIObjectStore();
		ISATABMapper isatabMapper = new ISATABMapper(store, isatabInstance);
		isatabMapper.map();
		assertTrue("Oh no! No mapped object! ", store.size() > 0);


		out.println("\n_____________ Persisting " + store.size() + " objects:\n");
		ISATABPersister persister = new ISATABPersister(store, DaoFactory.getInstance(entityManager));
		Timestamp ts = persister.persist(filesPath);
		transaction.commit();

		out.println("\n\n\n\n________________ Done, Submission TS: " + ts.getTime() + " (" + ts + " + " + ts.getNanos() + "ns)");

		Collection<Study> studies = store.valuesOfType(Study.class);
		Study study = studies.iterator().next();
//		for ( AssayResult ar: study.getAssayResults () ) {
//			out.println ( "____ Assay Result: " + ar );
//			for ( PropertyValue<?> prop: ar.getCascadedPropertyValues () )
//				out.println ( "\t" + prop );
//		}

		session.flush();


		ISATABUnloader unloader = new ISATABUnloader(DaoFactory.getInstance(entityManager), ts);
		unloader.unload();
		out.println("Unloading executed, messages: " + unloader.getMessages());

		Query q = entityManager.createQuery("SELECT e FROM " + Identifiable.class.getName() + " e WHERE e.submissionTs = :ts");
		q.setParameter("ts", ts);
		boolean isAllUnloaded = true;
		for (Object o : q.getResultList()) {
			out.println("**** Oh No! I have found an entity that should be unloaded! " + o);
			isAllUnloaded = false;
		}
		assertTrue("Sigh! I still have some objects that were not unloaded", isAllUnloaded);

		out.println("\n\n___________________ /end: ISATAB Unloading Test ___________________\n\n");
	}

}
