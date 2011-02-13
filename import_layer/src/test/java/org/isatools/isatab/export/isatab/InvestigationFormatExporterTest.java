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

package org.isatools.isatab.export.isatab;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.isatools.isatab_v1.ISATABLoader;
import org.isatools.isatab_v1.mapping.ISATABMapper;
import org.isatools.tablib.export.FormatSetExporter;
import org.isatools.tablib.schema.*;
import org.isatools.tablib.utils.BIIObjectStore;
import org.junit.Test;
import uk.ac.ebi.bioinvindex.model.Identifiable;
import uk.ac.ebi.bioinvindex.model.processing.Processing;
import uk.ac.ebi.bioinvindex.utils.DotGraphGenerator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import static java.lang.System.out;
import static org.junit.Assert.*;

public class InvestigationFormatExporterTest {
	@Test
	public void testBasics() throws Exception {
		out.println("\n\n__________ Investigation Test __________\n\n");

		String baseDir = System.getProperty("basedir");
		String filesPath = baseDir + "/target/test-classes/test-data/isatab/isatab_bii/JCastrillo-BII-I-1";
		ISATABLoader loader = new ISATABLoader(filesPath);
		FormatSetInstance isatabInstance = loader.load();

		BIIObjectStore store = new BIIObjectStore();
		ISATABMapper isatabMapper = new ISATABMapper(store, isatabInstance);

		isatabMapper.map();

		assertTrue("Oh no! No object retrieved from test data!", store.size() > 0);

		Collection<Identifiable> objects = new ArrayList<Identifiable>();
		objects.addAll(store.values(Processing.class));
		DotGraphGenerator dotter = new DotGraphGenerator(objects);
		String exportPath = baseDir + "/target/export/isatab/JCastrillo-BII-I-1";

		String dotFilePath =
				exportPath + "/" + FilenameUtils.getBaseName(loader.getInvestigationFileName()) + ".dot";
		dotter.createGraph(dotFilePath);
		out.println("Submission loaded, DOT file created in " + dotFilePath);

		FormatSetExporter exporter = new ISATABExporter(store);
		FormatSetInstance isatab = exporter.export();

		out.println("Exporting done, RESULTS:");
		for (FormatInstance formatInstance : isatab.getFormatInstances()) {
			out.println("Format: " + formatInstance.getFormat().getId() + ", file: " + formatInstance.getFileId());
			for (SectionInstance sectionInstance : formatInstance.getSectionInstances()) {
				out.println("Section: " + sectionInstance.getSectionId());

				out.printf("%15.15s  ", "#");
				for (Field field : sectionInstance.getFields()) {
					out.printf("%30.30s  ", field.dump());
				}
				out.println();
				int i = 1;
				for (Record record : sectionInstance.getRecords()) {
					out.printf("%15.15s  ", i);
					for (int j = 0; j < record.size(); j++) {
						out.printf("%30.30s  ", record.getString(j));
					}
					out.println();
					if (i++ > 4) {
						break;
					}
				}
				out.println();
			}
			out.println("\n\n");
		}

		SectionInstance investigationInstance = isatab.getSectionInstance("investigation", "investigation");
		assertNotNull("Ouch! No investigation exported!", investigationInstance);
		assertEquals("Urp! Bad no. of mapped investigations", 1, investigationInstance.getRecords().size());

		SectionInstance ontoSourceInstances = isatab.getSectionInstance("investigation", "ontoSources");
		assertNotNull("Ouch! No ontology sources exported!", ontoSourceInstances);
		assertEquals("Urp! Bad no. of mapped ontology sources!", 9, ontoSourceInstances.getRecords().size());

		boolean efoFound = false;
		int idescr = ontoSourceInstances.getField("Term Source Description").getIndex();
		for (Record record : ontoSourceInstances.getRecords()) {
			if ("ArrayExpress Experimental Factor Ontology".equals(record.getString(idescr))) {
				efoFound = true;
				break;
			}
		}
		assertTrue("Arg! EFO not found!", efoFound);

		// TODO: a test for the 148 (+ headers) rows in BII-S1.txt and 2 (+1) in BII-S2.txt 

		out.println("Now writing files to " + exportPath);
		FileUtils.forceMkdir(new File(exportPath));
		isatab.dump(exportPath);

		out.println("\n\n__________ /end: Investigation Test __________\n\n");
	}

}
