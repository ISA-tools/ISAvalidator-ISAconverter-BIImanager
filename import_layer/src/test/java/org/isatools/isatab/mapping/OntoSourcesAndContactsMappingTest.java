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

import org.isatools.tablib.mapping.FormatTabMapper;
import org.isatools.tablib.parser.TabLoader;
import org.isatools.tablib.schema.*;
import org.isatools.tablib.utils.BIIObjectStore;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import static java.lang.System.out;
import static org.junit.Assert.*;

public class OntoSourcesAndContactsMappingTest {
	private FormatSet schema;
	private FormatInstance formatInstance;
	private BIIObjectStore store = new BIIObjectStore();

	private class TestFormatTabMapper extends FormatTabMapper {
		public TestFormatTabMapper(BIIObjectStore store, FormatInstance formatSetInstance) {
			super(store, formatSetInstance);

			sectionMappersConfig.put("ontoSources", OntologySourceTabMapper.class);
			sectionMappersConfig.put("contacts", OldContactTabMapper.class);
		}

	}


	public OntoSourcesAndContactsMappingTest() throws Exception {
		super();

		InputStream input = new BufferedInputStream(this.getClass().getResourceAsStream("/test-data/isatab/isatab_format_1_refs.xml"));
		schema = SchemaBuilder.loadFormatSetFromXML(input);
		assertNotNull("I didn't get a schema", schema);

		TabLoader loader = new TabLoader(schema);
		formatInstance = loader.parse(
				null,
				new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(
						"/test-data/isatab/isatab_format_1_refs.csv"))),
				"references"
		);
	}


	@Test
	public void testBasicMapping() {
		out.println("\n\n\n ________ Testing contacts, basics _________");

		// Cont the records, to check you have the same number of objects
		SectionInstance contacts = formatInstance.getSectionInstance("contacts");
		List<Record> records = contacts.getRecords();
		int size = records == null ? 0 : records.size();

		// Cont the records, to check you have the same number of objects
		SectionInstance ontoSources = formatInstance.getSectionInstance("ontoSources");
		records = ontoSources.getRecords();
		size += records == null ? 0 : records.size();

		FormatTabMapper mapper = new TestFormatTabMapper(store, formatInstance);
		mapper.map();

		assertTrue("Oh no! No returned object (empty result)! ", store.size() > 0);

		out.println("__ RESULTS: __  ");
		out.println(store.toStringVerbose());

		int szStore = store.size();
		assertEquals(String.format("Uh?! I've the wrong number of items in the store"), size + 1, szStore);
		out.println("The final store has " + szStore + " items, as expected");


		out.println("________ /end: Testing contacts, basics _________\n\n\n");
	}

}
