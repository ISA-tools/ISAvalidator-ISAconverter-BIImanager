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

package org.isatools.isatab.mapping;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.isatools.isatab.ISATABLoader;
import org.isatools.tablib.schema.FormatSetInstance;
import org.isatools.tablib.utils.BIIObjectStore;
import org.junit.Test;
import uk.ac.ebi.bioinvindex.model.AssayResult;
import uk.ac.ebi.bioinvindex.model.Identifiable;
import uk.ac.ebi.bioinvindex.model.Study;
import uk.ac.ebi.bioinvindex.model.processing.Assay;
import uk.ac.ebi.bioinvindex.model.processing.Processing;
import uk.ac.ebi.bioinvindex.model.term.PropertyValue;
import uk.ac.ebi.bioinvindex.utils.DotGraphGenerator;
import uk.ac.ebi.bioinvindex.utils.processing.ProcessingUtils;

import java.util.ArrayList;
import java.util.Collection;

import static java.lang.System.out;
import static org.junit.Assert.*;


public class ISATABIntegrationTest {
	/**
	 * Tests that a (almost) complete ISATAB is correctly loaded and mapped into BII. Additionally produces graphs of the
	 * mapped experimental pipelines. The latter may be visualize by means of GraphViz, and it's stored in
	 * target/
	 */
	@SuppressWarnings("static-access")
	@Test
	public void testLoadingAndMapping() throws Exception {
		out.println("\n\n__________ ISATAB Mapping Test __________\n\n");

		String baseDir = System.getProperty("basedir");
		String filesPath = baseDir + "/target/test-classes/test-data/isatab/isatab/example";
		ISATABLoader loader = new ISATABLoader(filesPath);
		FormatSetInstance isatabInstance = loader.load();

		BIIObjectStore store = new BIIObjectStore();
		ISATABMapper isatabMapper = new ISATABMapper(store, isatabInstance);

		isatabMapper.map();
		assertTrue("Oh no! No mapped object! ", store.size() > 0);


		Study study = store.getType(Study.class, "bii:study:1");
		assertNotNull("Oh no! Study lsid:bii:study:19239 not found in the results!", study);
		Collection<Assay> assays = study.getAssays();
		Assay assay = (Assay) CollectionUtils.find(assays, new Predicate() {
			public boolean evaluate(Object assay) {
				return StringUtils.trimToEmpty(((Assay) assay).getAcc()).startsWith(
						"bii:study:1:assay:Study1.animal6.liver.extract1.le1.hyb1");
			}
		});
		assertNotNull("Gulp! Assay ...le1.hyb1.11 not found!", assay);


		Collection<AssayResult> ars = study.getAssayResults();
		out.println("__ ASSAY RESULTS: __  ");
		for (AssayResult ar : ars) {
			out.println(ar);
		}
		assertEquals("Oh no! Wrong no. of AssayResult(s)", 24, ars.size());

		ars = ProcessingUtils.findAssayResultsFromAssay(assay);
		assertEquals("Urp! Wrong no. of AssayResult(s) for assay test case", 1, ars.size());
		AssayResult ar = ars.iterator().next();

		out.println("__ CASCADED VALUES ON THE TEST CASE: __  ");
		Collection<PropertyValue> arProps = ar.filterRepeatedPropertyValues(ar.getCascadedPropertyValues());
		for (PropertyValue<?> v : arProps) {
			out.println("    " + v);
		}
		assertEquals("Urp! Wrong number of cascaded properties returned by mapped assay!", 11, arProps.size());

		out.println("\n\n__ RESULTS: __  ");
		out.println(store.toStringVerbose());

		Collection<Identifiable> objects = new ArrayList<Identifiable>();
		objects.addAll(store.values(Processing.class));
		DotGraphGenerator dotter = new DotGraphGenerator(objects);
		String dotFileName = baseDir + "/target/isatab.dot";
		dotter.createGraph(dotFileName);
		out.println("\n\nExperimental Graph written in " + dotFileName);


		out.println("\n\n__________ /end: ISATAB Mapping Test __________\n\n\n\n");

	}


	/**
	 * Test a case with transcriptomics only. Again, mapping and graph produced.
	 */
	@Test
	public void testSimpleISATAB() throws Exception {
		out.println("\n\n__________ ISATAB Mapping Test (TX Only) __________\n\n");

		String baseDir = System.getProperty("basedir");
		String filesPath = baseDir + "/target/test-classes/test-data/isatab/isatab/example_tx";
		ISATABLoader loader = new ISATABLoader(filesPath);
		FormatSetInstance isatabInstance = loader.load();

		BIIObjectStore store = new BIIObjectStore();
		ISATABMapper isatabMapper = new ISATABMapper(store, isatabInstance);

		isatabMapper.map();

		assertTrue("Oh no! No mapped object! ", store.size() > 0);


		out.println("__ RESULTS: __  ");
		out.println(store.toStringVerbose());

		Collection<Identifiable> objects = new ArrayList<Identifiable>();
		objects.addAll(store.values(Processing.class));
		DotGraphGenerator dotter = new DotGraphGenerator(objects);
		String dotFileName = baseDir + "/target/isatab_tx.dot";
		dotter.createGraph(dotFileName);
		out.println("\n\nExperimental Graph written in " + dotFileName);

		out.println("\n\n__________ /end: ISATAB Mapping Test (TX Only) __________\n\n\n\n");
	}

}
