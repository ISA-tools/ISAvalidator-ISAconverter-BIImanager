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
 * SRATemplate
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 * @date Jun 4, 2010
 */
public enum SRATemplate {
	PROCESSING_454_PAIRED("processing_454_paired.xml"),
	PROCESSING_454_SINGLE("processing_454_single.xml"),
	PROCESSING_ILLUMINA_SINGLE("processing_illumina_single.xml"),
	PROCESSING_ILLUMINA_PAIRED("processing_illumina_paired.xml"),
	PROCESSING_SOLID_PAIRED("processing_solid_paired.xml"),
	PROCESSING_SOLID_SINGLE("processing_solid_single.xml"),
	SPOT_DESCRIPTION_454_PAIRED("spotdesc_454_paired.xml"),
	SPOT_DESCRIPTION_454_SINGLE("spotdesc_454_single.xml"),
	SPOT_DESCRIPTION_454_SINGLE_BARCODE("spotdesc_454_single_barcode.xml"),
	SPOT_DESCRIPTION_ILLUMINA_SINGLE("spotdesc_illumina_single.xml"),
	SPOT_DESCRIPTION_ILLUMINA_PAIRED("spotdesc_illumina_paired.xml"),
	SPOT_DESCRIPTION_SOLID_SINGLE("spotdesc_solid_single.xml"),
	SPOT_DESCRIPTION_SOLID_PAIRED("spotdesc_solid_paired.xml");

	private String fileName;

	SRATemplate(String fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}

	@Override
	public String toString() {
		return fileName;
	}
}
