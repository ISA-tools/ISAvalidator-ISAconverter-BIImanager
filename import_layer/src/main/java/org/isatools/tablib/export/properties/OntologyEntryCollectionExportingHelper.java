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

import org.apache.commons.lang.StringUtils;
import org.isatools.tablib.export.TabExportUtils;
import org.isatools.tablib.export.TabExportUtils.OEString;
import org.isatools.tablib.schema.Field;
import org.isatools.tablib.schema.Record;
import org.isatools.tablib.schema.SectionInstance;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.model.Identifiable;
import uk.ac.ebi.bioinvindex.model.term.OntologyEntry;

public class OntologyEntryCollectionExportingHelper<ST extends Identifiable, OE extends OntologyEntry>
		extends CollectionExportingHelper<ST, OE> {
	final String labelFieldName, accFieldName, sourceFieldName;
	private int fieldIndex = -1;


	public OntologyEntryCollectionExportingHelper(BIIObjectStore store, String propertyName,
												  String labelFieldName, String accFieldName, String sourceFieldName) {
		super(store, propertyName);
		this.labelFieldName = labelFieldName;
		this.accFieldName = accFieldName;
		this.sourceFieldName = sourceFieldName;
	}


	@Override
	public Record export(ST source, Record record) {
		if (source == null) {
			record.set(fieldIndex, "");
			record.set(fieldIndex + 1, "");
			record.set(fieldIndex + 2, "");
			return record;
		}

		BIIObjectStore store = getStore();
		String labels = "", accs = "", srcs = "";

		String sep = "";
		for (OE oe : getPropertyValues(source)) {
			OEString oestr = TabExportUtils.storeSource(store, oe);

			labels += sep + StringUtils.trimToEmpty(oestr.label);
			accs += sep + StringUtils.trimToEmpty(oestr.acc);
			srcs += sep + StringUtils.trimToEmpty(oestr.src);
			sep = ";";
		}

		if (accs.matches("\\;+"))
		// Void if it's only a separator chain
		{
			accs = srcs = "";
		}

		record.set(fieldIndex, labels);
		record.set(fieldIndex + 1, accs);
		record.set(fieldIndex + 2, srcs);

		return record;
	}

	@Override
	public SectionInstance setFields(SectionInstance sectionInstance) {
		Field f = new Field(this.labelFieldName);
		f.setSection(sectionInstance.getSection());
		sectionInstance.addField(f);
		this.fieldIndex = f.getIndex();

		f = new Field(this.accFieldName);
		f.setSection(sectionInstance.getSection());
		sectionInstance.addField(f);

		f = new Field(this.sourceFieldName);
		f.setSection(sectionInstance.getSection());
		sectionInstance.addField(f);

		return sectionInstance;
	}

}
