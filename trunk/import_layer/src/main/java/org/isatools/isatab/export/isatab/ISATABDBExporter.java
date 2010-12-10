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

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.isatools.isatab.commandline.AbstractImportLayerShellCommand;
import org.isatools.isatab.export.isatab.filesdispatching.ISATabExportDataFilesDispatcher;
import org.isatools.isatab.export.isatab.filesdispatching.ISATabExportRepoFilesChecker;
import org.isatools.tablib.exceptions.TabIOException;
import org.isatools.tablib.exceptions.TabInternalErrorException;
import org.isatools.tablib.exceptions.TabUnsupportedException;
import org.isatools.tablib.schema.FormatSetInstance;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.dao.StudyDAO;
import uk.ac.ebi.bioinvindex.dao.ejb3.DaoFactory;
import uk.ac.ebi.bioinvindex.model.Investigation;
import uk.ac.ebi.bioinvindex.model.Study;
import uk.ac.ebi.bioinvindex.utils.datasourceload.DataLocationManager;
import uk.ac.ebi.bioinvindex.utils.i18n;

import javax.persistence.EntityManager;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Retrieves studies to be exported from the BII database and uses {@link ISATABExporter} to export them
 * to either local path or back to the BII repository (i.e.: updates the repo with information from the db).
 * <p/>
 * This class also manages the export of multiple studies that possibly belong to different investigations.
 * In such a case multiple ISATAB instances are spawned as export result.
 *
 * @author brandizi
 *         <b>date</b>: Feb 18, 2010
 */
public class ISATABDBExporter {

	private final StudyDAO studyDAO;
	private Collection<Study> studies;
	private Collection<String> studyAccs;
	/**
	 * @see {@link ExportedItem}
	 */
	private Collection<ExportedItem> exportedItems;
	private final EntityManager entityManager;
	private DataLocationManager dataLocationMgr;
	private boolean isInitialized = false;

	protected static final Logger log = Logger.getLogger(ISATABDBExporter.class);

	/**
	 * Used internally. Studies are grouped according to the investigation they belong to and  one submission
	 * per group is exported. If a study doesn't belong to any investigation, it produces a submission with that only
	 * study in it.
	 * <p/>
	 * each submission is stored in an instance of ExportedItem, together with related information
	 */
	protected static class ExportedItem {
		public final Investigation investigation;
		public final Set<Study> studies;

		/**
		 * Produced by the {@link ISATABExporter} using the store passed to it
		 */
		public FormatSetInstance isatab;

		/**
		 * {@link ISATABExporter} exports what it finds in this store
		 */
		public BIIObjectStore store;

		public ExportedItem(Investigation investigation, Set<Study> studies) {
			this.investigation = investigation;
			this.studies = studies;
		}

		/**
		 * Uses the investigation or the study, depending on what we have
		 */
		public String getLabel() {
			if (investigation != null)
			// TODO: check it's not null
			{
				return "Investigation " + investigation.getAcc();
			}
			return "Study " + studies.iterator().next().getAcc();
		}

		/**
		 * The path for a local export, where the result has the study or the investigation accession
		 */
		public String getExportPath(String base) {
			// Use the investigation, or the first study if no investigation (there should be one study only in that case).
			if (investigation != null) {
				// TODO: check it's not null
				return base + "/" + DataLocationManager.accession2FileName(investigation.getAcc());
			}
			// TODO: check it's not empty
			return base + "/" + DataLocationManager.accession2FileName(studies.iterator().next().getAcc());
		}

		/**
		 * (mkdir) Creates the export path and returns a proper error message in case of
		 * problems (using {@link #getLabel()})
		 */
		public void createExportPath(String base) {
			File exportDir = new File(getExportPath(base));
			if (!exportDir.exists()) {
				try {
					FileUtils.forceMkdir(exportDir);
				}
				catch (IOException e) {
					throw new TabIOException(i18n.msg("isatab_export_path_creation_io", base, e.getMessage()), e);
				}
			}
		}
	}

	/**
	 * You can initialize the class by either a list of accessions or a list of {@link Study} objects.
	 */
	public ISATABDBExporter(DaoFactory daoFactory, String... studyAccs) {
		this(daoFactory, Arrays.asList(studyAccs));
	}

	/**
	 * You can initialize the class by either a list of accessions or a list of {@link Study} objects.
	 */
	public ISATABDBExporter(DaoFactory daoFactory, Study... studies) {
		this(daoFactory, Arrays.asList(studies));
	}

	/**
	 * You can initialize the class by either a list of accessions or a list of {@link Study} objects.
	 * Here you can pass either a collection of studies or of strings.
	 */
	@SuppressWarnings("unchecked")
	public ISATABDBExporter(DaoFactory daoFactory, Collection studies) {
		// this.daoFactory = daoFactory;
		studyDAO = daoFactory.getStudyDAO();
		entityManager = daoFactory.getEntityManager();

		if (studies.isEmpty()) {
			studies = studyAccs = null;
			return;
		}
		Object first = studies.iterator().next();
		if (first instanceof String) {
			this.studyAccs = studies;
			this.studies = null;
		} else if (first instanceof Study) {
			this.studyAccs = null;
			this.studies = studies;
		} else {
			throw new TabInternalErrorException(
					"Wrong parameter of type" + studies.getClass().getSimpleName() + ", TABDB Exporter must be initialized with"
							+ "either a list of accessions or a list of Study objects");
		}
	}

	/**
	 * Prepares the {@link #exportedItems} collection, according to the grouping described in {@link ExportedItem}.
	 */
	private void init() {
		if (isInitialized) {
			return;
		}
		dataLocationMgr = AbstractImportLayerShellCommand.createDataLocationManager(entityManager);

		// Prepare the study collection
		if (studies == null) {
			if (studyAccs == null) {
				return;
			}
			studies = new LinkedList<Study>();
			for (String acc : studyAccs) {
				Study study = studyDAO.getByAcc(acc);
				if (study == null) {
					throw new TabInternalErrorException("ISATAB Exporter, cannot find study #" + acc);
				}
				studies.add(study);
			}
		}

		// Builds the export items, where studies are grouped by investigation or kept alone if they've no investigation 
		// 
		exportedItems = new LinkedList<ExportedItem>();
		Map<String, ExportedItem> inv2ExpItem = new HashMap<String, ExportedItem>();

		for (Study study : studies) {
			if (study == null)
			// TODO: log
			{
				continue;
			}

			Collection<Investigation> investigations = study.getInvestigations();
			int sz = investigations.size();
			if (sz > 1) {
				throw new TabUnsupportedException(
						"The study " + study.getAcc() + " is linked to more than one investigation, we don't support this case yet" +
								" in the ISATAB exporter"
				);
			}

			if (sz == 1) {
				// There is the investigation
				Investigation thisInv = investigations.iterator().next();
				String iacc = thisInv.getAcc();
				// TODO: check the ACC
				ExportedItem expItem = inv2ExpItem.get(iacc);
				if (expItem == null) {
					// Investigation is new
					Set<Study> studies = new HashSet<Study>();
					studies.add(study);
					expItem = new ExportedItem(thisInv, studies);
					exportedItems.add(expItem);
					inv2ExpItem.put(iacc, expItem);
				} else
				// Investigation already met
				{
					expItem.studies.add(study);
				}
			} else {
				// Study is alone, no investigation involved => single study export
				Set<Study> studies = new HashSet<Study>();
				studies.add(study);
				exportedItems.add(new ExportedItem(null, studies));
			}
		}
		isInitialized = true;

	} // init ()


	/**
	 * A wrapper of {@link #exportToRepository(boolean) exportToRepository ( false) }
	 */
	public void exportToRepository() {
		exportToRepository(false);
	}


	/**
	 * Exports the studies passed to the constructor back to the BII file repository. This produces
	 * replacement of old ISATAB meta-data files (which are stored in a backup location by the loader).
	 * <p/>
	 * It also produces a the backup of data files no longer referenced by the new meta-data spreadsheet,
	 * as it is described in {@link ISATabExportRepoFilesChecker}. the latter can be skipped passing true
	 * to skipOldDataFilesBackup.
	 */
	public void exportToRepository(boolean skipOldDataFilesBackup) {
		init();
		for (ExportedItem expItem : exportedItems) {
			exportToRepository(expItem, skipOldDataFilesBackup);
		}
	}

	/**
	 * Does the job for the single submission. Called by {@link #exportToRepository(boolean)}
	 */
	private void exportToRepository(ExportedItem expItem, boolean skipOldDataFilesBackup) {
		exportSubmission(expItem);

		for (Study study : expItem.studies) {
			String metaRepoPath = dataLocationMgr.buildISATabMetaDataLocation(study);
			try {
				expItem.isatab.dump(metaRepoPath);
				ISATabExportRepoFilesChecker repoChecker = new ISATabExportRepoFilesChecker(expItem.store, dataLocationMgr);
				repoChecker.checkMissingFilesForRepositoryExport(skipOldDataFilesBackup);
			}
			catch (IOException e) {
				throw new TabIOException(
						i18n.msg("isatab_export_path_creation_io", expItem.getLabel(), metaRepoPath, e.getMessage())
						, e
				);
			}
		}
	}

	/**
	 * A wrapper of {@link #exportToPath(String, boolean) exportToPath ( false )}
	 */
	public void exportToPath(String exportPath) {
		exportToPath(exportPath, false);
	}


	/**
	 * Exports the studies passed to the constructor to a specified path. It will create per-study and
	 * per-investigation directories under exportPath (using accessions as names). It will copy the ISATAB
	 * meta-data into those directories and the data-files (may be skipped with skipDataFilesDump = true ).
	 * Additionally, it reports those data files that are still in the BII file repository and no longer referenced
	 * by the new meta-data files, as it is described in {@link ISATabExportRepoFilesChecker}.
	 */
	public void exportToPath(String exportPath, boolean skipDataFilesDump) {
		init();
		for (ExportedItem expItem : exportedItems) {
			exportToPath(expItem, exportPath, skipDataFilesDump);
		}
	}

	/**
	 * Does the job for a single submission. Called by {@link #exportToPath(ExportedItem, String, boolean)}.
	 */
	private void exportToPath(ExportedItem expItem, String exportPathBase, boolean skipDataFilesDump) {
		exportSubmission(expItem);
		String exportPath = expItem.getExportPath(exportPathBase);
		try {
			expItem.createExportPath(exportPathBase);
			expItem.isatab.dump(exportPath);

			ISATabExportRepoFilesChecker repoChecker = new ISATabExportRepoFilesChecker(expItem.store, dataLocationMgr);
			repoChecker.checkMissingFilesForLocalExport();

			if (skipDataFilesDump) {
				return;
			}

			ISATabExportDataFilesDispatcher filesDispatcher = new ISATabExportDataFilesDispatcher(
					expItem.store, dataLocationMgr, exportPath
			);
			filesDispatcher.dispatch();
		}
		catch (IOException e) {
			throw new TabIOException(
					i18n.msg("isatab_export_path_creation_io", expItem.getLabel(), exportPath, e.getMessage())
					, e
			);
		}
	}

	/**
	 * Populates expItem.isatab with the submission about the studies and (possibly) the investigation
	 * in this item.
	 */
	protected ExportedItem exportSubmission(ExportedItem expItem) {
		if (expItem.isatab != null) {
			return expItem;
		}
		log.debug("exporting " + expItem.getLabel());

		BIIObjectStore store = new BIIObjectStore();
		Investigation investigation = expItem.investigation;
		Set<Study> studies = expItem.studies;

		for (Study study : studies) {
			store.put(Study.class, study.getAcc(), study);
		}
		if (investigation != null) {
			store.put(Investigation.class, investigation.getAcc(), investigation);
		}

		ISATABExporter isatabExporter = new ISATABExporter(store);
		FormatSetInstance isatab = isatabExporter.export();
		expItem.isatab = isatab;
		expItem.store = store;
		return expItem;
	}
}
