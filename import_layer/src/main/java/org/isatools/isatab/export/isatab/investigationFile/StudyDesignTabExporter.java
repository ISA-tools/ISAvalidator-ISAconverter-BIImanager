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
 * - Manon Delahaye (ISA team trainee:  BII web services)
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
 * To request enhancements:  http://sourceforge.net/tracker/?group_id=215183&atid=1032652
 *
 *
 * __________
 * License:
 * __________
 *
 * This work is licenced under the Creative Commons Attribution-Share Alike 2.0 UK: England & Wales License. To view a copy of this licence, visit http://creativecommons.org/licenses/by-sa/2.0/uk/ or send a letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California 94105, USA.
 *
 * __________
 * Sponsors
 * __________
 * This work has been funded mainly by the EU Carcinogenomics (http://www.carcinogenomics.eu) [PL 037712] and in part by the
 * EU NuGO [NoE 503630](http://www.nugo.org/everyone) projects and in part by EMBL-EBI.
 */

package org.isatools.isatab.export.isatab.investigationFile;

import org.isatools.isatab.export.properties.FreeTextSimpleExportingUtility;
import org.isatools.isatab_v1.ISATABLoader;
import org.isatools.tablib.export.StringPropertyComparator;
import org.isatools.tablib.schema.Record;
import org.isatools.tablib.schema.Section;
import org.isatools.tablib.schema.SectionInstance;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.model.term.Design;

import java.util.Collection;

public class StudyDesignTabExporter extends StudyComponentTabExporter<Design> {
	private final FreeTextSimpleExportingUtility termXUtil;


	public StudyDesignTabExporter(BIIObjectStore store) {
		super(store);
		this.comparator = new StringPropertyComparator<Design>("value");
		this.termXUtil = new FreeTextSimpleExportingUtility(store,
				"Study Design Type",
				"Study Design Type Term Accession Number",
				"Study Design Type Term Source REF"
		);
	}

	/**
	 * Exports it as a free-text term
	 */
	@Override
	public Record export(Design source, Record record) {
		return termXUtil.export(source, record);
	}


	@Override
	protected Collection<Design> getSources() {
		return getCurrentSource().getDesigns();
	}

	@Override
	protected Section getSection() {
		return ISATABLoader.getISATABSchema().getDescendant("studyDesigns", Section.class);
	}


	/**
	 * Prepares the fields about the factor types
	 */
	@Override
	public SectionInstance createSectionInstance() {
		SectionInstance sectionInstance = new SectionInstance(getSection());
		return termXUtil.setFields(sectionInstance);
	}

}
