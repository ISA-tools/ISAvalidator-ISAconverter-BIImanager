// TODO: remove, was replaced by ConverterShellCommand
///*
// * __________
// *  CREDITS
// * __________
// *
// * Team page: http://isatab.sf.net/
// *  - Marco Brandizi (software engineer: ISAvalidator, ISAconverter, BII data management utility, BII model)
// *  - Eamonn Maguire (software engineer: ISAcreator, ISAcreator configurator, ISAvalidator, ISAconverter,  BII data management utility, BII web)
// *  - Nataliya Sklyar (software engineer: BII web application, BII model,  BII data management utility)
// *  - Philippe Rocca-Serra (technical coordinator: user requirements and standards compliance for ISA software, ISA-tab format specification, BII model, ISAcreator wizard, ontology)
// *  - Susanna-Assunta Sansone (coordinator: ISA infrastructure design, standards compliance, ISA-tab format specification, BII model, funds raising)
// *
// * Contributors:
// *  - Manon Delahaye (ISA team trainee:  BII web services)
// *  - Richard Evans (ISA team trainee: rISAtab)
// *
// *  ______________________
// * Contacts and Feedback:
// * ______________________
// *
// * Project overview: http://isatab.sourceforge.net/
// *
// * To follow general discussion: isatab-devel@list.sourceforge.net
// * To contact the developers: isatools@googlegroups.com
// *
// * To report bugs: http://sourceforge.net/tracker/?group_id=215183&atid=1032649
// * To request enhancements:  http://sourceforge.net/tracker/?group_id=215183&atid=1032652
// *
// * __________
// * License
// * __________
// *
// * This work is licenced under the Creative Commons Attribution-Share Alike 2.0 UK: England & Wales License. To view a copy of this licence, visit http://creativecommons.org/licenses/by-sa/2.0/uk/ or send a letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California 94105, USA.
// *
// * __________
// * Sponsors
// * __________
// * This work has been funded mainly by the EU Carcinogenomics (http://www.carcinogenomics.eu) [PL 037712] and in partby the EU NuGO [NoE 503630](http://www.nugo.org/everyone) projects and in part by EMBL-EBI.
// */
//
//package org.isatools.isatab.commandline;
//
//import static java.lang.System.out;
//
//import org.apache.log4j.Logger;
//
//import uk.ac.ebi.bioinvindex.utils.i18n;
//import org.isatools.isatab.export.pride.DraftPrideExporter;
//import org.isatools.tablib.utils.BIIObjectStore;
//
///**
// * Performs the PRIDE-ML creation
// * 
// * date: Jun 6, 2008
// * @author brandizi
// *
// */
//public class PrideDispatchShellCommand extends AbstractImportLayerShellCommand
//{
//
//	public static void main ( String[] args )
//	{
//		try 
//		{
//			setup ( args );
//			exportPath = args.length > 1 ? args [ 1 ] : sourceDirPath + "/export";
//			setupLog4JPath ( exportPath + "/pride/isatools.log" );
//			// Need to initialize this here, otherwise above config will fail
//			log = Logger.getLogger ( PrideDispatchShellCommand.class );
//
//			BIIObjectStore store = loadIsaTab ();
//			log.info ( i18n.msg ( "mapping_done_now_exporting", store.size () ) );
//
//			DraftPrideExporter exporter = new DraftPrideExporter ( store, sourceDirPath, exportPath );
//			exporter.export ();
//			log.info ( i18n.msg ( "converter_export_done", "PRIDE", exportPath + "/pride" ) );
//		}
//		catch ( Exception ex ) {
//			log.fatal ( "ERROR: problem while running the PRIDE dispatcher: " + ex.getMessage (), ex );
//		}
//	}
//}
