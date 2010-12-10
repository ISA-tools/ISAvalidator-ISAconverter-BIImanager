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

package org.isatools.isatab.export.isatab.assay_file;

import org.isatools.isatab.mapping.AssayTypeEntries;
import org.isatools.tablib.export.FormatArrayExporter;
import org.isatools.tablib.mapping.pipeline.ProcessingEntityTabMapper;
import org.isatools.tablib.schema.FormatInstanceArray;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.model.Study;
import uk.ac.ebi.bioinvindex.model.processing.Assay;
import uk.ac.ebi.bioinvindex.model.term.AssayTechnology;
import uk.ac.ebi.bioinvindex.model.term.Measurement;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Exports all pairs of Measurement/Technology that are found in a database for a given study.
 * For all distinct pair creates a new Assay file (using {@link AssayFormatExporter}) and Assay block in the
 * investigation file.
 *
 * @author brandizi
 *         <b>date</b>: Mar 11, 2010
 */
public class AllAssaysExporter extends FormatArrayExporter {

	public AllAssaysExporter(BIIObjectStore store) {
		super(store);
	}

	@Override
	public FormatInstanceArray export() {
		BIIObjectStore store = getStore();
		FormatInstanceArray result = new FormatInstanceArray();
		Collection<Study> studies = store.valuesOfType(Study.class);
		if (studies.size() == 0) {
			return result;
		}

		// Scan all the studies and assays and invoke a new assay file export for each new Assay type. 
		//
		Set<String> doneFiles = new HashSet<String>();
		for (Study study : studies) {
			for (Assay assay : study.getAssays()) {
				String assayFileId = assay.getSingleAnnotationValue(
						ProcessingEntityTabMapper.ASSAY_FILE_ANNOTATION_TAG
				);
				if (doneFiles.contains(assayFileId)) {
					continue;
				}
				doneFiles.add(assayFileId);

				Measurement measurement = assay.getMeasurement();
				AssayTechnology tech = assay.getTechnology();

				// We can only deal with one of the formats defined in the ISATAB specification, and defined
				// in the ISATAB schema file (in /main/resources/).
				// 
				String assayFormatId = AssayTypeEntries.getAssayFormatIdFromLabels(assay);

				if (assayFormatId == null) {
					String msg = "Ignoring the unknown assay of type: '" + measurement.getName() + "' / '" + tech.getName() + "'";
					log.warn(msg);
					continue;
				}

				log.debug("Exporting file: " + assayFileId + ", " + measurement.getName() + ", " + tech.getName());

				// TODO: pass down the file ID
				AssayFormatExporter assayGroupExporter = new AssayFormatExporter(
						store, assayFormatId, assayFileId
				);

				FormatInstanceArray expAssays = assayGroupExporter.export();
				result.addFormatInstanceArray(expAssays);
			}
		}
		return result;
	}
}
