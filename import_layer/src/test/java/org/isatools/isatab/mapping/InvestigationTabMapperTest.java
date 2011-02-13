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

import org.isatools.tablib.parser.TabLoader;
import org.isatools.tablib.schema.*;
import org.isatools.tablib.utils.BIIObjectStore;
import org.junit.Test;
import uk.ac.ebi.bioinvindex.model.Investigation;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import static java.lang.System.out;
import static org.junit.Assert.*;

public class InvestigationTabMapperTest {
	@Test
	public void testInvestigationMapper() throws Exception {
		// Load the test case, as usually
		//
		out.println("\n\n_____ Testing InvestigationTabMapper _______");

		InputStream input = new BufferedInputStream(this.getClass().getResourceAsStream("/test-data/isatab/investigation.xml"));
		FormatSet schema = SchemaBuilder.loadFormatSetFromXML(input);
		assertNotNull("I didn't get a schema", schema);

		// out.println( schema.toString ( true ) );

		TabLoader loader = new TabLoader(schema);
		FormatInstance formatInstance = loader.parse(
				null,
				new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/test-data/isatab/investigation.csv"))),
				"investigation"
		);

		SectionInstance sectionInstance = formatInstance.getSectionInstance("investigation");
		List<Record> recs = sectionInstance.getRecords();

		assertNotNull("I couldn't get records!", recs);
		assertTrue("I couldn't get records (empty result)!", recs.size() > 0);

		// Invoke the mapper
		//
		InvestigationTabMapper mapper = new InvestigationTabMapper(new BIIObjectStore(), sectionInstance);
		Investigation inv = mapper.map(0);
		assertNotNull("Couldn't get any mapped object!", inv);

		out.println("--- Results: ---");
		out.println(inv);

		out.println("_____ /end: Testing InvestigationTabMapper _______\n\n\n");
	}


	@Test
	public void testInvestigationMapperComplex() throws Exception {
		// Load the test case, as usually
		//
		out.println("\n\n_____ Testing InvestigationTabMapper, Complex _______");

		InputStream input = new BufferedInputStream(this.getClass().getResourceAsStream("/test-data/isatab/investigation.xml"));
		FormatSet schema = SchemaBuilder.loadFormatSetFromXML(input);
		assertNotNull("I didn't get a schema", schema);

		// out.println( schema.toString ( true ) );

		TabLoader loader = new TabLoader(schema);
		FormatInstance formatInstance = loader.parse(
				null,
				new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/test-data/isatab/investigation_complex.csv"))),
				"investigation"
		);

		SectionInstance sectionInstance = formatInstance.getSectionInstance("investigation");
		List<Record> recs = sectionInstance.getRecords();
		assertNotNull("I couldn't get records!", recs);
		assertTrue("I couldn't get records (empty result)!", recs.size() > 0);

		// Create some testing object, do not import them from files, in order to keep the test isolated
		BIIObjectStore store = new BIIObjectStore();

// TODO: Investigation contact disappeared from ISATAB 0.2, we need to clarify this		
//		Contact contact = new Contact ( "Jens", "", "Zeller", "zeller@somewhere.net" );
//		store.put ( Contact.class, contact.getEmail (), contact );
//		Publication pub = new Publication (
//			"How to stop looking after your navel and enoying life", "Guru Brandizi, Mister Bean"
//		);
//		pub.setAcc ( "266374" );
//		store.put ( Publication.class, pub.getAcc (), pub );


		// Invoke the mapper
		//
		InvestigationTabMapper mapper = new InvestigationTabMapper(store, sectionInstance);
		Investigation inv = mapper.map(0);
		assertNotNull("Couldn't get any mapped object!", inv);

		assertEquals("Ouch! Investigation mapping failed",
				"A Test Investigation, made for testing purposes", inv.getTitle()
		);

// TODO: Investigation contact disappeared from ISATAB 0.2, we need to clarify this		
//		Collection<Contact> contacts = inv.getContacts ();
//		assertNotNull ( "Ouch! The mapped object has no contacts (null result)!", contacts );
//		assertTrue ( "Ouch! The mapped object has no contacts (empty result)!", contacts.size () > 0 );
//		Contact contact1 = contacts.iterator ().next ();
//		assertEquals ( "BURP! The mapped investigation's contact is different than the one originally stored", contact, contact1 );
//
//		Collection<Publication> pubs = inv.getPublications ();
//		assertNotNull ( "Ouch! The mapped object has no publications (null result)!", pubs );
//		assertTrue ( "Ouch! The mapped object has no contacts (empty result)!", pubs.size () > 0 );
//		Publication pub1 = pubs.iterator ().next ();
//		assertEquals ( "BURP! The mapped investigation's publication is different than the one originally stored", pub, pub1 );


		out.println("--- Results: ---");
		out.println(inv);

		out.println("_____ /end: Testing InvestigationTabMapper, Complex _______\n\n\n");
	}


}