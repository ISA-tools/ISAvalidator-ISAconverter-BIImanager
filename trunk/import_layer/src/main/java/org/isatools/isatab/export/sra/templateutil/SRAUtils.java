/*
 * CREDITS
 *  __________
 *
 *  Team page: http://isatab.sf.net/
 *    - Marco Brandizi (software engineer: ISAvalidator, ISAconverter, BII data management utility, BII model)
 *    - Eamonn Maguire (software engineer: ISAcreator, ISAcreator configurator, ISAvalidator, ISAconverter,  BII data management utility, BII web)
 *    - Nataliya Sklyar (software engineer: BII web application, BII model,  BII data management utility)
 *    - Philippe Rocca-Serra (technical coordinator: user requirements and standards compliance for ISA software, ISA-tab format specification, BII model, ISAcreator wizard, ontology)
 *    - Susanna-Assunta Sansone (coordinator: ISA infrastructure design, standards compliance, ISA-tab format specification, BII model, funds raising)
 *
 *  Contributors:
 *    - Manon Delahaye (ISA team trainee:  BII web services)
 *    - Richard Evans (ISA team trainee: rISAtab)
 *
 *  ______________________
 *  Contacts and Feedback:
 *  ______________________
 *
 *  Project overview: http://isatab.sourceforge.net/
 *
 *  To follow general discussion: isatab-devel@list.sourceforge.net
 *  To contact the developers: isatools@googlegroups.com
 *
 *  To report bugs: http://sourceforge.net/tracker/?group_id=215183&atid=1032649
 *  To request enhancements:  http://sourceforge.net/tracker/?group_id=215183&atid=1032652
 *
 *  __________
 *  License
 *  __________
 *
 *  This work is licenced under the Creative Commons Attribution-Share Alike 2.0 UK: England & Wales License. To view a copy of this licence, visit http://creativecommons.org/licenses/by-sa/2.0/uk/ or send a letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California 94105, USA.
 *
 *  __________
 *  Sponsors
 *  __________
 *  This work has been funded mainly by the EU Carcinogenomics (http://www.carcinogenomics.eu) [PL 037712] and in partby the EU NuGO [NoE 503630](http://www.nugo.org/everyone) projects and in part by EMBL-EBI.
 */

package org.isatools.isatab.export.sra.templateutil;

/**
 * SRAUtils provides methods for post conversion modification of SRA-XML files.
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 * @date Jun 9, 2010
 */


public class SRAUtils {

	public static final String INJECTED_TAG = "INJECTED_TAG";

	/**
	 * Removes the <INJECTED_TAG> elements from a String. These tags are injected by the SRATemplateLoader
	 * in order to support insertion of XML with no root element.
	 *
	 * @param toModify - String to remove the <INJECTED_TAG> & </INJECTED_TAG> elements from.
	 * @return String with the tags removed
	 */
	public static String removeInjectedTags(String toModify) {
		if (toModify != null) {
			toModify = toModify.replaceAll("<" + INJECTED_TAG + ">|</" + INJECTED_TAG + ">", "");
			return toModify;
		}
		return "";
	}

	/**
	 * Finds the SRATemplate of interest given an @see SRASection, a platform and a library layout
	 *
	 * @param section	   @see SRASection - PROCESSING or SPOT_DESCRIPTOR
	 * @param platform	  - e.g. 454, Illumina or SOLiD
	 * @param libraryLayout - e.g. paired, single or single barcode
	 * @param isBarcode	 - does the experiment use multiplexing? if so, a barcode is required!
	 * @return SRATemplate e.g. for SRASection.PROCESSING, 454 and paired end, this would return SRATemplate.PROCESSING_454_PAIRED
	 */
	public static SRATemplate getTemplate(SRASection section, String platform, String libraryLayout, boolean isBarcode) {


		libraryLayout = libraryLayout.toLowerCase();
		platform = platform.toLowerCase();

		// determine type of type
		if (section == SRASection.PROCESSING) {
			if (platform.contains(SRAPlatforms.LS454.toString())) {

				return libraryLayout.contains(SRALibraries.PAIRED.toString()) ?
						SRATemplate.PROCESSING_454_PAIRED :
						SRATemplate.PROCESSING_454_SINGLE;

			} else if (platform.contains(SRAPlatforms.ILLUMINA.toString())) {

				return libraryLayout.contains(SRALibraries.PAIRED.toString()) ?
						SRATemplate.PROCESSING_ILLUMINA_PAIRED :
						SRATemplate.PROCESSING_ILLUMINA_SINGLE;

			} else if (platform.contains(SRAPlatforms.SOLID.toString())) {

				return libraryLayout.contains(SRALibraries.PAIRED.toString()) ?
						SRATemplate.PROCESSING_SOLID_PAIRED :
						SRATemplate.PROCESSING_SOLID_SINGLE;
			}
		} else {
			if (platform.contains(SRAPlatforms.LS454.toString())) {

				return libraryLayout.contains(SRALibraries.PAIRED.toString()) ?
						SRATemplate.SPOT_DESCRIPTION_454_PAIRED :
						isBarcode ?
								SRATemplate.SPOT_DESCRIPTION_454_SINGLE_BARCODE :
								SRATemplate.SPOT_DESCRIPTION_454_SINGLE;

			} else if (platform.contains(SRAPlatforms.ILLUMINA.toString()) || platform.contains(SRAPlatforms.SOLEXA.toString())) {

				return libraryLayout.contains(SRALibraries.PAIRED.toString()) ?
						SRATemplate.SPOT_DESCRIPTION_ILLUMINA_PAIRED :
						SRATemplate.SPOT_DESCRIPTION_ILLUMINA_SINGLE;

			} else if (platform.contains(SRAPlatforms.SOLID.toString())) {

				return libraryLayout.contains(SRALibraries.PAIRED.toString()) ?
						SRATemplate.SPOT_DESCRIPTION_SOLID_PAIRED :
						SRATemplate.SPOT_DESCRIPTION_SOLID_SINGLE;
			}
		}

		return null;
	}

}
