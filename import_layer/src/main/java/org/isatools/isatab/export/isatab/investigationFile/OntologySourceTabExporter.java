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

import org.isatools.isatab_v1.ISATABLoader;
import org.isatools.tablib.export.ClassTabExporter;
import org.isatools.tablib.export.StringPropertyComparator;
import org.isatools.tablib.export.properties.PropertyExportingHelper;
import org.isatools.tablib.export.properties.StringExportingHelper;
import org.isatools.tablib.schema.Section;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.model.xref.ReferenceSource;

import java.util.LinkedList;

/**
 * The Ontology Source exporter.
 * <p/>
 * date: Mar 31, 2008
 *
 * @author brandizi
 */
public class OntologySourceTabExporter extends ClassTabExporter<ReferenceSource> {

	public OntologySourceTabExporter(BIIObjectStore store) {
		super(store);
		propertyExportingHelpers = new LinkedList<PropertyExportingHelper<ReferenceSource>>();
		propertyExportingHelpers.add(new StringExportingHelper<ReferenceSource>(store, "acc", "Term Source Name"));
		propertyExportingHelpers.add(new StringExportingHelper<ReferenceSource>(store, "url", "Term Source File"));
		propertyExportingHelpers.add(new StringExportingHelper<ReferenceSource>(store, "version", "Term Source Version"));
		propertyExportingHelpers.add(new StringExportingHelper<ReferenceSource>(store, "description", "Term Source Description"));

		this.comparator = new StringPropertyComparator<ReferenceSource>("acc");
	}

	@Override
	protected Section getSection() {
		return ISATABLoader.getISATABSchema().getDescendant("ontoSources", Section.class);
	}

}
