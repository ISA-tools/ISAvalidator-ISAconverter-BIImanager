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
import org.isatools.tablib.schema.FormatInstance;
import org.isatools.tablib.schema.FormatSet;
import org.isatools.tablib.schema.SchemaBuilder;
import org.isatools.tablib.utils.BIIObjectStore;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import static java.lang.System.out;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ReferenceAndProtocolsMappingTest {
	private FormatSet schema;
	private FormatInstance formatInstance;
	private BIIObjectStore store = new BIIObjectStore();


	private class RefTabMapper extends FormatTabMapper {
		public RefTabMapper(BIIObjectStore store, FormatInstance formatInstance) {
			super(store, formatInstance);

			sectionMappersConfig.put("ontoSources", OntologySourceTabMapper.class);
			sectionMappersConfig.put("contacts", OldContactTabMapper.class);
			sectionMappersConfig.put("publications", OldPublicationTabMapper.class);
			sectionMappersConfig.put("protocols", OldProtocolTabMapper.class);
		}

	}


	public ReferenceAndProtocolsMappingTest() throws Exception {
		super();

		InputStream input = new BufferedInputStream(this.getClass().getResourceAsStream("/test-data/isatab/isatab_3_refs.xml"));
		schema = SchemaBuilder.loadFormatSetFromXML(input);
		assertNotNull("I didn't get a schema", schema);

		TabLoader loader = new TabLoader(schema);
		formatInstance = loader.parse(
				null,
				new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(
						"/test-data/isatab/isatab_3_refs.csv"))), "references"
		);
	}


	@Test
	public void testBasicMapping() {
		out.println("\n\n\n ________ Testing Reference Section, with protocols _________");

		FormatTabMapper mapper = new RefTabMapper(store, formatInstance);
		store = (BIIObjectStore) mapper.map();
		assertNotNull("Oh no! No returned object! ", store);
		assertTrue("Oh no! No returned object (empty result)! ", store.size() > 0);

		out.println("__ RESULTS: __  ");
		out.println(store.toStringVerbose());

		out.println("________ /end: Testing Reference Section, , with protocols _________\n\n\n");
	}

}