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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.isatools.isatab_v1.mapping.ISATABMapper;
import org.isatools.tablib.schema.FormatSetInstance;
import org.isatools.tablib.utils.BIIObjectStore;
import org.junit.Test;
import uk.ac.ebi.bioinvindex.model.AssayResult;
import uk.ac.ebi.bioinvindex.model.Identifiable;
import uk.ac.ebi.bioinvindex.model.Study;
import uk.ac.ebi.bioinvindex.model.processing.Processing;
import uk.ac.ebi.bioinvindex.model.term.FactorValue;
import uk.ac.ebi.bioinvindex.utils.DotGraphGenerator;

import java.util.ArrayList;
import java.util.Collection;

import static java.lang.System.out;
import static org.junit.Assert.*;


public class GenericAssayMappingTest {
	/**
	 * Tests correct mapping of generic assay
	 */
	@Test
	@SuppressWarnings("static-access")
	public void testLoadingAndMapping() throws Exception {
		out.println("\n\n__________ Generic Case Mapping Test __________\n\n");

		String baseDir = System.getProperty("basedir");
		String filesPath = baseDir + "/target/test-classes/test-data/isatab/generic_assay";

		ISATABLoader loader = new ISATABLoader(filesPath);
		FormatSetInstance isatabInstance = loader.load();

		out.println("\n\n_____ Loaded, now mapping");
		BIIObjectStore store = new BIIObjectStore();
		ISATABMapper isatabMapper = new ISATABMapper(store, isatabInstance);

		isatabMapper.map();
		assertTrue("Oh no! No mapped object! ", store.size() > 0);

		Study study = store.getType(Study.class, "bii:study:3");
		assertNotNull("Oh no! Study bii:study:3 not found in the results!", study);
		Collection<AssayResult> ars = new ArrayList<AssayResult>(study.getAssayResults());
		final Collection<FactorValue> fvs = new ArrayList<FactorValue>();
		CollectionUtils.filter(ars, new Predicate() {
			public boolean evaluate(Object object) {
				AssayResult ar = (AssayResult) object;
				if ("bii:generic_assay_derived_data".equals(ar.getData().getType().getAcc())) {
					fvs.addAll(ar.getFactorValues());
					return true;
				}
				return false;
			}
		});

		assertEquals("Ouch! Wrong no of assay results from the generic assay file!", 12, ars.size());
		assertEquals("Ouch! Wrong no of factor values from the generic assay file!", 24, fvs.size());
		assertEquals("Ouch! Wrong no of unique factor values from the generic assay file!", 3,
				AssayResult.filterRepeatedPropertyValues(fvs).size()
		);


		out.println("\n\n__ RESULTS: __  ");
		out.println(store.toStringVerbose());

		Collection<Identifiable> objects = new ArrayList<Identifiable>();
		objects.addAll(store.values(Processing.class));
		DotGraphGenerator dotter = new DotGraphGenerator(objects);
		String dotFileName = baseDir + "/target/generic_assay.dot";
		dotter.createGraph(dotFileName);
		out.println("\n\nExperimental Graph written in " + dotFileName);

		out.println("\n\n__________ /end: Generic Case Mapping Test __________\n\n\n\n");
	}

}
