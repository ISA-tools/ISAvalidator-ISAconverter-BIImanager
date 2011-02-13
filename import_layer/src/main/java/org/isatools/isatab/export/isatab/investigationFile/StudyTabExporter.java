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
import org.isatools.tablib.export.properties.DateExportingHelper;
import org.isatools.tablib.export.properties.PropertyExportingHelper;
import org.isatools.tablib.export.properties.StringExportingHelper;
import org.isatools.tablib.mapping.pipeline.ProcessingEntityTabMapper;
import org.isatools.tablib.schema.Field;
import org.isatools.tablib.schema.Record;
import org.isatools.tablib.schema.Section;
import org.isatools.tablib.schema.SectionInstance;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.model.Study;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public class StudyTabExporter extends StudyComponentTabExporter<Study> {
	public StudyTabExporter(BIIObjectStore store) {
		super(store);
		propertyExportingHelpers = new LinkedList<PropertyExportingHelper<Study>>();
		propertyExportingHelpers.add(new StringExportingHelper<Study>(store, "acc", "Study Identifier"));
		propertyExportingHelpers.add(new StringExportingHelper<Study>(store, "title", "Study Title"));
		propertyExportingHelpers.add(new DateExportingHelper<Study>(store, "submissionDate", "Study Submission Date"));
		propertyExportingHelpers.add(new DateExportingHelper<Study>(store, "releaseDate", "Study Public Release Date"));
		propertyExportingHelpers.add(new StringExportingHelper<Study>(store, "description", "Study Description"));
	}


	@Override
	public SectionInstance createSectionInstance() {
		// TODO Auto-generated method stub
		SectionInstance result = super.createSectionInstance();

		Field sampleFileField = new Field("Study File Name");
		sampleFileField.setSection(result.getSection());
		result.addField(sampleFileField);
		return result;
	}


	@Override
	public Record export(Study source, Record record) {
		super.export(source, record);
		record.set(
				record.getParent().getField("Study File Name").getIndex(),
				source.getSingleAnnotationValue(ProcessingEntityTabMapper.SAMPLE_FILE_ANNOTATION_TAG)
		);
		return record;
	}


	@Override
	protected Collection<Study> getSources() {
		return Collections.singletonList(getCurrentSource());
	}

	@Override
	protected Section getSection() {
		return ISATABLoader.getISATABSchema().getDescendant("study", Section.class);
	}

}
