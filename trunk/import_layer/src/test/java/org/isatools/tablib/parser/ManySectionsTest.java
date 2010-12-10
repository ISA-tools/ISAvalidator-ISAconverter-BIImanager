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

package org.isatools.tablib.parser;

import org.isatools.tablib.schema.*;
import org.junit.Test;

import java.io.*;
import java.util.List;

import static java.lang.System.out;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


/**
 * June 14, 2007
 *
 * @author brandizi
 */
public class ManySectionsTest {
	private TabLoader loader;
	private FormatSet schema;


	/**
	 * Tests a complete simple TAB/TSV, going through several sections
	 */
	@Test
	public void testManySections() throws IOException {
		out.println("--- Testing Many sections in the TAB ---");
		InputStream input = new BufferedInputStream(this.getClass().getResourceAsStream("/test-data/tablib/sample_format_def.xml"));
		schema = SchemaBuilder.loadFormatSetFromXML(input);
		assertNotNull("I didn't get a schema", schema);

		// out.println( schema.toString ( true ) );

		loader = new TabLoader(schema);
		FormatInstance formatInstance = loader.parse(
				null,
				new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/test-data/tablib/sample_format_instance.csv"))),
				"idf_sdrf"
		);


		out.println("--- Testing Many sections in the TAB, Results: ---\n\n");

		List<SectionInstance> sectionInstances = formatInstance.getSectionInstances();
		for (SectionInstance sectionInstance : sectionInstances) {
			out.println("Section: " + sectionInstance.getSectionId());
			for (Record record : sectionInstance.getRecords()) {
				out.println(record + "\n");
			}
			out.println("\n");
		}

		out.println("--- Testing Many sections in the TAB, end ---\n\n");
	}

	/**
	 * Tests repeated sections
	 */
	@Test
	public void testRepeatedSections() throws IOException {
		out.println("--- Testing Repeated sections in the TAB ---");
		InputStream input = new BufferedInputStream(this.getClass().getResourceAsStream("/test-data/tablib/sample_format_def.xml"));
		schema = SchemaBuilder.loadFormatSetFromXML(input);
		assertNotNull("I didn't get a schema", schema);

		// out.println( schema.toString ( true ) );

		loader = new TabLoader(schema);
		FormatInstance formatInstance = loader.parse(
				null,
				new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/test-data/tablib/sample_format_instance_repeated_sections.csv"))),
				"idf_sdrf"
		);


		out.println("--- Testing Repeated sections in the TAB, Results: ---\n\n");

		List<SectionInstance> sectionInstances = formatInstance.getSectionInstances("experiment");
		assertNotNull("Arg! No experiment instance", sectionInstances);
		assertEquals("Ouch! Bad no. of experiment instances", 2, sectionInstances.size());

		for (SectionInstance sectionInstance : sectionInstances) {
			out.println("Section: " + sectionInstance.getSectionId());
			for (Record record : sectionInstance.getRecords()) {
				out.println(record + "\n");
			}
			out.println("\n");
		}

		out.println("--- /end: Testing Repeated sections in the TAB ---\n\n");
	}

}
