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

import org.isatools.tablib.export.TabExportUtils;
import org.isatools.tablib.schema.Field;
import org.isatools.tablib.schema.Record;
import org.isatools.tablib.schema.SectionInstance;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.model.Identifiable;
import uk.ac.ebi.bioinvindex.model.term.FreeTextTerm;

public class FreetextTermCollectionExportingHelper<ST extends Identifiable, FT extends FreeTextTerm>
		extends CollectionExportingHelper<ST, FT> {
	final String termLabel, oeLabel, accFieldName, sourceFieldName;
	private int fieldIndex = -1;


	public FreetextTermCollectionExportingHelper(BIIObjectStore store, String propertyName,
												 String termLabel, String oeLabel, String accFieldName, String sourceFieldName) {
		super(store, propertyName);
		this.termLabel = termLabel;
		this.oeLabel = oeLabel;
		this.accFieldName = accFieldName;
		this.sourceFieldName = sourceFieldName;
	}

	public FreetextTermCollectionExportingHelper(BIIObjectStore store, String propertyName,
												 String termLabel, String accFieldName, String sourceFieldName) {
		this(store, propertyName, termLabel, null, accFieldName, sourceFieldName);
	}


	@Override
	public Record export(ST source, Record record) {
		if (source == null) {
			int idx = fieldIndex;
			record.set(idx++, ""); // label
			if (oeLabel != null) {
				record.set(idx++, "");
			} // oeLabel
			record.set(idx++, ""); // acc
			record.set(idx++, ""); // src
			return record;
		}

		BIIObjectStore store = getStore();
		String labels = "", oelabels = "", accs = "", srcs = "";

		String sep = "";
		for (FT term : getPropertyValues(source)) {
			TabExportUtils.TermString tstr = TabExportUtils.storeSource(store, term);

			labels += sep + tstr.label;
			oelabels += sep + tstr.oelabel;
			accs += sep + tstr.acc;
			srcs += sep + tstr.src;

			sep = ";";
		}

		// Void if it's only a separator chain
		if (labels.matches("\\;+")) {
			labels = "";
		}
		if (oelabels.matches("\\;+")) {
			oelabels = "";
		}
		if (accs.matches("\\;+")) {
			accs = srcs = "";
		}

		int idx = fieldIndex;
		record.set(idx++, labels); // label
		if (oeLabel != null) {
			record.set(idx++, oelabels);
		} // oeLabel
		record.set(idx++, accs); // acc
		record.set(idx++, srcs); // src

		return record;
	}

	@Override
	public SectionInstance setFields(SectionInstance sectionInstance) {
		this.fieldIndex = -1;
		for (String fieldName : new String[]{termLabel, oeLabel, accFieldName, sourceFieldName}) {
			if (fieldName == null) {
				continue;
			}

			Field f = new Field(fieldName);
			f.setSection(sectionInstance.getSection());
			sectionInstance.addField(f);

			if (fieldIndex == -1) {
				fieldIndex = f.getIndex();
			}
		}
		return sectionInstance;
	}

}
