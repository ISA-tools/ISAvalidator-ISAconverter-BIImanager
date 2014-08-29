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
 * To request enhancements: �http://sourceforge.net/tracker/?group_id=215183&atid=1032652
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

package org.isatools.isatab_v1.export;

import org.isatools.isatab.export.magetab.MAGETABExporter;
import org.isatools.isatab.mapping.AssayGroup;
import org.isatools.isatab_v1.ISATABLoader;
import org.isatools.isatab_v1.mapping.ISATABMapper;
import org.isatools.tablib.export.FormatSetExporter;
import org.isatools.tablib.schema.FormatInstance;
import org.isatools.tablib.schema.FormatSetInstance;
import org.isatools.tablib.schema.Record;
import org.isatools.tablib.schema.SectionInstance;
import org.isatools.tablib.utils.BIIObjectStore;
import org.junit.Test;

import java.io.File;

import static java.lang.System.out;
import static org.junit.Assert.assertTrue;

public class MAGETABExporterTest {

	@Test
	public void testMAGETABExporting() throws Exception {
		out.println("\n\n________________ MAGETAB Exporting Test ________________\n\n");

		System.setProperty(
				"bioinvindex.converters.magetab.backlink",
				"(This experiment is linked to the BII project, see http://www.ebi.ac.uk/bioinvindex/study.seam?studyId=${study-acc}) "
		);

		String baseDir = System.getProperty("basedir");
		String filesPath = baseDir + "/target/test-classes/test-data/isatab/isatab_v1_200810/griffin_gauguier_200810";
		ISATABLoader loader = new ISATABLoader(filesPath);
		FormatSetInstance isatabInstance = loader.load();

		BIIObjectStore store = new BIIObjectStore();
		ISATABMapper isatabMapper = new ISATABMapper(store, isatabInstance);

		isatabMapper.map();
		assertTrue("Oh no! No object retrieved from test data!", store.size() > 0);


		FormatSetExporter exporter = new MAGETABExporter(
				store, (AssayGroup) store.get(AssayGroup.class, "a-S1.A1.txt")
		);
		FormatSetInstance magetab = exporter.export();
		String exportPath = baseDir + "/target/export/magetab/manual_export";
		magetab.dump(exportPath);
		assertTrue("File sdrf.txt not created!",
				new File(exportPath + "/sdrf.txt").exists());

		out.println("MAGETAB instance exported to " + exportPath);

		out.println("RESULTS:");
		for (FormatInstance formatInstance : magetab.getFormatInstances()) {
			out.println("Format: " + formatInstance.getFormat().getId());
			for (SectionInstance sectionInstance : formatInstance.getSectionInstances()) {
				out.println("Section: " + sectionInstance.getSectionId());
				int i = 1;
				for (Record record : sectionInstance.getRecords()) {
					out.println(i++ + ".\t" + record);
				}
				out.println();
			}
			out.println("\n\n");
		}

		out.println("\n\n________________ /end: MAGETAB Exporting Test ________________\n\n");
	}


	@Test
	public void testDispatching() throws Exception {
		out.println("\n\n________________ MAGETAB Dispatching Test ________________\n\n");

		String baseDir = System.getProperty("basedir");
		String sourcePath = baseDir + "/target/test-classes/test-data/isatab/isatab_v1_200810/griffin_gauguier_200810";
		ISATABLoader loader = new ISATABLoader(sourcePath);
		FormatSetInstance isatabInstance = loader.load();

		BIIObjectStore store = new BIIObjectStore();
		ISATABMapper isatabMapper = new ISATABMapper(store, isatabInstance);

		isatabMapper.map();
		assertTrue("Oh no! No object retrieved from test data!", store.size() > 0);

		String exportPath = baseDir + "/target/export", testStudyExpPath = exportPath + "/magetab/a-S1_A1";

		MAGETABExporter.dispatch(store, sourcePath, exportPath);

		assertTrue("File " + testStudyExpPath + "/sdrf.txt not created!",
				new File(testStudyExpPath + "/sdrf.txt").exists());

		assertTrue("File " + testStudyExpPath + "/GC-RMA-data.txt not created!",
				new File(testStudyExpPath + "/GC-RMA-data.txt").exists());

		out.println("MAGETAB files created in " + exportPath);


		out.println("\n\n________________ /end: MAGETAB Dispatching Test ________________\n\n");
	}

}
