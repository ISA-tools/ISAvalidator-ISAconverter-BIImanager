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

package org.isatools.isatab.export.isatab;


import org.isatools.isatab.ISATABLoader;
import org.isatools.isatab.export.isatab.assay_file.AllAssaysExporter;
import org.isatools.isatab.export.isatab.investigationFile.InvestigationFormatExporter;
import org.isatools.isatab.export.isatab.sample_file.SampleFormatExporter;
import org.isatools.isatab.mapping.AssayGroup;
import org.isatools.tablib.export.FormatElementExporter;
import org.isatools.tablib.export.FormatSetExporter;
import org.isatools.tablib.mapping.pipeline.ProcessingEntityTabMapper;
import org.isatools.tablib.schema.FormatSetInstance;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.model.Study;
import uk.ac.ebi.bioinvindex.model.processing.Assay;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Exports a single ISATAB submission. This class works only on the generation of the ISATAB meta-data files, complete
 * exports are done by using this class in {@link ISATABDBExporter}.
 *
 * @author brandizi
 *         <b>date</b>: Mar 12, 2010
 */
public class ISATABExporter extends FormatSetExporter {
	/**
	 * It will export objects inside this store.
	 * <b>WARNING</b>:
	 */
	public ISATABExporter(BIIObjectStore store) {
		super(store);
		initAssayGroups();
		exporters = new ArrayList<FormatElementExporter<?>>();
		exporters.add(new SampleFormatExporter(store));
		exporters.add(new AllAssaysExporter(store));
		exporters.add(new InvestigationFormatExporter(store));
	}

	@Override
	protected FormatSetInstance createFormatSetInstance() {
		return new FormatSetInstance(ISATABLoader.getISATABSchema());
	}

	/**
	 * Scans the assays and recreates {@link AssayGroup}s into the the store, ready for the subsequent export.
	 * This is necessary for exports from the BII db.
	 */
	private void initAssayGroups() {
		BIIObjectStore store = getStore();
		for (Study study : store.valuesOfType(Study.class)) {
			Set<String> doneFiles = new HashSet<String>();

			for (Assay assay : study.getAssays()) {
				String assayFileId = assay.getSingleAnnotationValue(
						ProcessingEntityTabMapper.ASSAY_FILE_ANNOTATION_TAG
				);
				if (doneFiles.contains(assayFileId)) {
					continue;
				}
				doneFiles.add(assayFileId);

				AssayGroup ag = new AssayGroup(study);
				ag.setMeasurement(assay.getMeasurement());
				ag.setTechnology(assay.getTechnology());
				ag.setPlatform(assay.getAssayPlatform());
				ag.setFilePath(assayFileId);
				ag.setAcc(assayFileId);

				// This is needed by the file dispatcher
				store.put(AssayGroup.class, assayFileId, ag);
			}
		}
	}
}
