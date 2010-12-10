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

package org.isatools.tablib.schema;

import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.List;

import static java.lang.System.out;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SchemaInstanceTest {
	@Test
	public void testBuild() {
		out.println("\n\n--- Testing Schema instances ---");
		InputStream input = new BufferedInputStream(this.getClass().getResourceAsStream("/test-data/tablib/sample_format_def.xml"));
		FormatSet schema = SchemaBuilder.loadFormatSetFromXML(input);
		assertNotNull("I have a schema", schema);

		FormatSetInstance formatSetInstance = new FormatSetInstance(schema);
		FormatInstance formatInstance = new FormatInstance(schema.getFormat("idf_sdrf"), formatSetInstance);
		formatSetInstance.addFormatInstance(formatInstance);

		Section section = (Section) schema.getDescendant("protocol", Section.class);
		SectionInstance sectionInstance = new SectionInstance(section, formatInstance);
		formatInstance.addSectionInstance(sectionInstance);

		section = (Section) schema.getDescendant("experiment", Section.class);
		sectionInstance = new SectionInstance(section, formatInstance);
		formatInstance.addSectionInstance(sectionInstance);

		sectionInstance = new SectionInstance(section, formatInstance);
		formatInstance.addSectionInstance(sectionInstance);

		out.println("Build done, testing some retrieval");


		formatInstance = formatSetInstance.getFormatInstance("idf_sdrf");
		assertNotNull("Ops! No formati instance idf_sdrf found!", formatInstance);
		assertEquals("Ouch! Bad format value for retrieved format", "idf_sdrf", formatInstance.getFormat().getId());
		out.println("--- /end: Testing Schema instances ---\n\n");

		sectionInstance = formatInstance.getSectionInstance("experiment");
		assertNotNull("Urp! No returned setion instance for experiment", sectionInstance);
		assertEquals("Ouch! Bad section value for retrieved section instance", "experiment", sectionInstance.getSection().getId());

		List<SectionInstance> sectionInstances = formatInstance.getSectionInstances("experiment");
		assertNotNull("Urp! No returned setion instance for experiment", sectionInstances);
		assertEquals("Ouch! Bad number of results for retrieval of experiment section", 2, sectionInstances.size());
	}
}
