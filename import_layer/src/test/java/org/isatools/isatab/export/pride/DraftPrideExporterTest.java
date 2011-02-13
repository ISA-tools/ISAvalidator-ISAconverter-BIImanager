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

package org.isatools.isatab.export.pride;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.isatools.isatab.ISATABLoader;
import org.isatools.isatab.mapping.ISATABMapper;
import org.isatools.tablib.schema.FormatSetInstance;
import org.isatools.tablib.utils.BIIObjectStore;
import org.junit.Test;
import uk.ac.ebi.bioinvindex.model.Identifiable;
import uk.ac.ebi.bioinvindex.model.processing.Processing;
import uk.ac.ebi.bioinvindex.utils.DotGraphGenerator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import static java.lang.System.out;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DraftPrideExporterTest {

	@SuppressWarnings("unchecked")
	@Test
	public void testPipelines() throws Exception {
		out.println("\n\n________________ DraftPrideExporter Test ________________\n\n");

		String baseDir = System.getProperty("basedir");
		String filesPath = baseDir + "/target/test-classes/test-data/isatab/isatab/example";
		ISATABLoader loader = new ISATABLoader(filesPath);
		FormatSetInstance isatabInstance = loader.load();

		BIIObjectStore store = new BIIObjectStore();
		ISATABMapper isatabMapper = new ISATABMapper(store, isatabInstance);

		isatabMapper.map();
		assertTrue("Oh no! No object retrieved from test data!", store.size() > 0);

		Collection<Identifiable> objects = new ArrayList<Identifiable>();
		objects.addAll(store.values(Processing.class));
		DotGraphGenerator dotter = new DotGraphGenerator(objects);
		String dotFileName = baseDir + "/target/pride_export.dot";
		dotter.createGraph(dotFileName);
		out.println("\n\nExperimental Graph written in " + dotFileName);

		String exportPath = baseDir + "/target/export";
		File exportDir = new File(exportPath);
		DraftPrideExporter exporter = new DraftPrideExporter(store, filesPath, exportPath);
		exporter.export();

		assertTrue("Ouch! Export directory doesn't exist!", exportDir.exists());
		Collection<File> files = FileUtils.listFiles(
				exportDir, new WildcardFileFilter("animal2_sample2_run1"), FileFilterUtils.trueFileFilter()
		);
		assertNotNull("Ouch! Expected PRIDE file not found!", files != null && files.size() > 0);


		out.println("\n\n________________ /end:DraftPrideExporter Test ________________\n\n");
	}

}
