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

package org.isatools.tablib.export.properties;

import org.isatools.tablib.schema.Field;
import org.isatools.tablib.schema.Record;
import org.isatools.tablib.schema.SectionInstance;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.model.Annotatable;
import uk.ac.ebi.bioinvindex.model.Annotation;
import uk.ac.ebi.bioinvindex.model.term.AnnotationType;
import uk.ac.ebi.utils.regex.RegEx;

import java.util.List;

public class AnnotationExportingHelper<ST extends Annotatable> extends PropertyExportingHelper<ST> {
	protected final RegEx annotationTypePattern;
	int fieldIndex = -1;

	public AnnotationExportingHelper(BIIObjectStore store, String annotationTypePattern) {
		this(store, new RegEx(annotationTypePattern));
	}


	public AnnotationExportingHelper(BIIObjectStore store, RegEx annotationTypePattern) {
		super(store, annotationTypePattern.getPattern());
		this.annotationTypePattern = annotationTypePattern;
	}


	protected List<Annotation> getPropertyValues(ST source) {
		return source.getAnnotationValuesByRe(annotationTypePattern);
	}

	protected String getFieldName(AnnotationType atype) {
		return atype.getValue();
	}

	@Override
	public Record export(ST source, Record record) {
		int fieldIdx = this.fieldIndex;
		for (Annotation annval : getPropertyValues(source)) {
			AnnotationType atype = annval.getType();
			String fieldName = getFieldName(atype);

			SectionInstance sectionInstance = record.getParent();
			Field afield = sectionInstance.getField(fieldName, fieldIndex - 1);
			if (afield == null) {
				afield = new Field(fieldName);
				afield.setSection(sectionInstance.getSection());
				sectionInstance.addField(afield);
				fieldIdx = afield.getIndex();
			}
			record.set(fieldIdx, annval.getText());
		}
		return record;
	}


	@Override
	public SectionInstance setFields(SectionInstance sectionInstance) {
		fieldIndex = 0;
		return sectionInstance;
	}


}
