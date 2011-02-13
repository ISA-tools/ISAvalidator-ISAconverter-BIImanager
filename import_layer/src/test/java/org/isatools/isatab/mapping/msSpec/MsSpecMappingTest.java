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

package org.isatools.isatab.mapping.msSpec;
//package org.isatools.isatab.mapping.msSpec;
//
//import static java.lang.System.out;
//import static org.junit.Assert.assertNotNull;
//
//import java.io.BufferedInputStream;
//import java.io.InputStream;
//import java.util.HashMap;
//import java.util.Map;
//
//import org.junit.Test;
//
//import org.isatools.tablib.mapping.BIIObjectStore;
//import org.isatools.tablib.mapping.FormatSetTabMapper;
//import org.isatools.tablib.mapping.testModels.isatab.ISATABWithMsSpecMapper;
//import org.isatools.tablib.parser.TabLoader;
//import org.isatools.tablib.schema.FormatSet;
//import org.isatools.tablib.schema.FormatSetInstance;
//import org.isatools.tablib.schema.SchemaBuilder;
//
//public class MsSpecMappingTest
//{
//	
//	@Test
//	public void testBasicMapping () throws Exception
//	{
//		// Load the test case, as usually
//		//
//		out.println ( "\n\n____ Testing TranscriptomicsMapper ____" );
//
//		InputStream input = new BufferedInputStream ( this.getClass ().getResourceAsStream ( 
//			"/test-data/isatab/assays/study_assays.xml" 
//		));
//		FormatSet schema = SchemaBuilder.loadFormatSetFromXML ( input );
//		assertNotNull ( "I didn't get a schema", schema );
//
//		TabLoader loader = new TabLoader ( schema );
//		
//		Map<String, String> loadingMap = new HashMap<String, String> ();
//			loadingMap.put ( "/test-data/isatab/assays/proteomics_refs.csv", "references" );
//		  loadingMap.put ( "/test-data/isatab/assays/assay_proteomics.csv", "ms_spec_assay" );
//		loader.loadResources ( loadingMap );
//
//		BIIObjectStore store = new BIIObjectStore();
//		FormatSetInstance formatSetInstance = loader.getFormatSetInstance ();
//		FormatSetTabMapper mapper = new ISATABWithMsSpecMapper ( store, formatSetInstance );
//		mapper.map ();
//		
//		out.println ( "\n___ RESULTS: ___" );
//		out.println ( store.toStringVerbose () );
//
//		out.println ( "\n\n____ /end: Testing TranscriptomicsMapper ____" );
//	}		
//	
//}