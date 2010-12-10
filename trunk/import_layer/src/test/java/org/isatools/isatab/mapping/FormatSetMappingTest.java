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

import org.isatools.tablib.mapping.FormatSetTabMapper;
import org.isatools.tablib.mapping.testModels.isatab.SimpleISATABTabMapper;
import org.isatools.tablib.parser.TabLoader;
import org.isatools.tablib.schema.FormatSet;
import org.isatools.tablib.schema.FormatSetInstance;
import org.isatools.tablib.schema.SchemaBuilder;
import org.isatools.tablib.utils.BIIObjectStore;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.out;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Maps an example of a whole format set, with the ISATAB references and investigation sections.
 * Feb 27, 2008
 *
 * @author brandizi
 */
public class FormatSetMappingTest {
	private FormatSet schema;
	private FormatSetInstance formatSetInstance;
	private BIIObjectStore store = new BIIObjectStore();


	public FormatSetMappingTest() throws Exception {

		InputStream input = new BufferedInputStream(this.getClass().getResourceAsStream("/test-data/isatab/multi_format.xml"));
		schema = SchemaBuilder.loadFormatSetFromXML(input);
		assertNotNull("I didn't get a schema", schema);

		TabLoader loader = new TabLoader(schema);

		Map<String, String> loadingMap = new HashMap<String, String>();
		loadingMap.put("/test-data/isatab/isatab_2_refs.csv", "references");
		loadingMap.put("/test-data/isatab/investigation_complex.csv", "investigation");
		loader.loadResources(loadingMap);

		formatSetInstance = loader.getFormatSetInstance();
	}


	@Test
	public void testBasicMapping() {
		out.println("\n\n\n ________ Testing complete format set _________");

		FormatSetTabMapper mapper = new SimpleISATABTabMapper(store, formatSetInstance);

		store = (BIIObjectStore) mapper.map();

		assertNotNull("Oh no! No returned object! ", store);
		assertTrue("Oh no! No returned object (empty result)! ", store.size() > 0);

		out.println("__ RESULTS: __  ");
		out.println(store.toStringVerbose());

		out.println("________ /end: Testing complete format set _________\n\n\n");
	}

}