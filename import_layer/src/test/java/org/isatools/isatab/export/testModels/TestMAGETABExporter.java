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

package org.isatools.isatab.export.testModels;
//package org.isatools.isatab.export.testModels;
//
//import java.io.BufferedInputStream;
//import java.io.InputStream;
//import java.util.ArrayList;
//
//import uk.ac.ebi.bioinvindex.model.impl.Study;
//import org.isatools.isatab.export.magetab.IDFExporter;
//import org.isatools.isatab.export.magetab.SDRFExporter;
//import org.isatools.tablib.export.FormatElementExporter;
//import org.isatools.tablib.export.FormatSetExporter;
//import org.isatools.tablib.mapping.BIIObjectStore;
//import org.isatools.tablib.mapping.FormatSetTabMapper;
//import org.isatools.tablib.schema.FormatSet;
//import org.isatools.tablib.schema.FormatSetInstance;
//import org.isatools.tablib.schema.SchemaBuilder;
//
///**
// * The MAGE-TAB exporter, test version.
// * 
// * date: Apr 7, 2008
// * @author brandizi
// *
// */
//public class TestMAGETABExporter extends FormatSetExporter
//{
//	public static final String MAGETAB_SCHEMA_PATH = "/magetab_1.0.format.xml";
//	private static FormatSet magetabSchema;
//
//	public TestMAGETABExporter ( BIIObjectStore store, Study study ) 
//	{
//		super ( store );
//		exporters = new ArrayList<FormatElementExporter<?>> ();
//		exporters.add ( new IDFExporter ( store, study )  );
//		exporters.add ( new MeDaFormatTabExporter ( store, study )  );
//	}
//
//	@Override
//	protected FormatSetInstance createFormatSetInstance () {
//		return new FormatSetInstance ( getMAGETABSchema () );
//	}
//	
//	/**
//	 * Uses the file in {@link #MAGETAB_SCHEMA_PATH}.
//	 * 
//	 */
//	public static FormatSet getMAGETABSchema () 
//	{
//		if ( magetabSchema != null ) return magetabSchema;
//		
//		InputStream input = new BufferedInputStream ( FormatSetTabMapper.class.getResourceAsStream ( MAGETAB_SCHEMA_PATH )); 
//		return magetabSchema = SchemaBuilder.loadFormatSetFromXML ( input );
//	}
//}
