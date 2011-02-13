/**

 The ISAconverter, ISAvalidator & BII Management Tool are components of the ISA software suite (http://www.isa-tools.org)

 Exhibit A
 The ISAconverter, ISAvalidator & BII Management Tool are licensed under the Mozilla Public License (MPL) version
 1.1/GPL version 2.0/LGPL version 2.1

 "The contents of this file are subject to the Mozilla Public License
 Version 1.1 (the "License"). You may not use this file except in compliance with the License.
 You may obtain copies of the Licenses at http://www.mozilla.org/MPL/MPL-1.1.html.

 Software distributed under the License is distributed on an "AS IS"
 basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 License for the specific language governing rights and limitations
 under the License.

 The Original Code is the ISAconverter, ISAvalidator & BII Management Tool.

 The Initial Developer of the Original Code is the ISA Team (Eamonn Maguire, eamonnmag@gmail.com;
 Philippe Rocca-Serra, proccaserra@gmail.com; Susanna-Assunta Sansone, sa.sanson@gmail.com;
 http://www.isa-tools.org). All portions of the code written by the ISA Team are Copyright (c)
 2007-2011 ISA Team. All Rights Reserved.

 Contributor(s):
 Rocca-Serra P, Brandizi M, Maguire E, Sklyar N, Taylor C, Begley K, Field D,
 Harris S, Hide W, Hofmann O, Neumann S, Sterk P, Tong W, Sansone SA. ISA software suite:
 supporting standards-compliant experimental annotation and enabling curation at the community level.
 Bioinformatics 2010;26(18):2354-6.

 Alternatively, the contents of this file may be used under the terms of either the GNU General
 Public License Version 2 or later (the "GPL") - http://www.gnu.org/licenses/gpl-2.0.html, or
 the GNU Lesser General Public License Version 2.1 or later (the "LGPL") -
 http://www.gnu.org/licenses/lgpl-2.1.html, in which case the provisions of the GPL
 or the LGPL are applicable instead of those above. If you wish to allow use of your version
 of this file only under the terms of either the GPL or the LGPL, and not to allow others to
 use your version of this file under the terms of the MPL, indicate your decision by deleting
 the provisions above and replace them with the notice and other provisions required by the
 GPL or the LGPL. If you do not delete the provisions above, a recipient may use your version
 of this file under the terms of any one of the MPL, the GPL or the LGPL.

 Sponsors:
 The ISA Team and the ISA software suite have been funded by the EU Carcinogenomics project
 (http://www.carcinogenomics.eu), the UK BBSRC (http://www.bbsrc.ac.uk), the UK NERC-NEBC
 (http://nebc.nerc.ac.uk) and in part by the EU NuGO consortium (http://www.nugo.org/everyone).

 */

package org.isatools.isatab.export.isatab.filesdispatching;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.isatools.isatab.export.isatab.ISATABDBExporter;
import org.isatools.isatab.mapping.AssayGroup;
import org.isatools.tablib.exceptions.TabIOException;
import org.isatools.tablib.export.FormatExporter;
import org.isatools.tablib.schema.Record;
import org.isatools.tablib.schema.SectionInstance;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.model.Study;
import uk.ac.ebi.bioinvindex.model.term.AnnotationTypes;
import uk.ac.ebi.bioinvindex.utils.datasourceload.DataLocationManager;
import uk.ac.ebi.bioinvindex.utils.i18n;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Used by {@link ISATABDBExporter} for verifying dangling data files. This component scans all the files about a
 * submission that are in the BII file repository and takes care of those that are no longer referenced by the exported
 * meta-data files, created from the BII db. Then it is able to do several things with them, like logging warnings or
 * backup the orphan files.
 *
 * @author brandizi
 *         <b>date</b>: Mar 11, 2010
 */
public class ISATabExportRepoFilesChecker {

    private BIIObjectStore store;
    private final DataLocationManager dataLocationMgr;
    private Map<File, String> referencedFiles = new HashMap<File, String>();

    protected static final Logger log = Logger.getLogger(ISATabExportRepoFilesChecker.class);

    private enum Mode {
        /**
         * Collects file references from the ISATAB spreadsheets
         */
        COLLECT,

        /**
         * Like {@link #COLLECT}, but without methods/actions that are not needed during the export to a local directory
         */
        COLLECT_LOCAL_EXPORT,

        /**
         * Backups the files that are no longer referenced by the spreadsheets
         */
        BACKUP,

        /**
         * Only warns about no longer referred files, omitting the backup
         */
        WARN
    }

    ;


    /**
     * @param store           the submission to be dispatched, null when invoking {@link #undispatch(List)}
     * @param sourcePath      the path of the submission directory, null when undispatching.
     * @param dataLocationMgr the location manager that allows to know where the data files are in the
     *                        BII repository.
     */
    public ISATabExportRepoFilesChecker(
            BIIObjectStore store, DataLocationManager dataLocationMgr) {
        this.store = store;
        this.dataLocationMgr = dataLocationMgr;
    }

    /**
     * Used for local export, check all the orphan files, as explained above, and issues warning for them.
     */
    public void checkMissingFilesForLocalExport() {
        referencedFiles.clear();
        checkMissingFiles(Mode.COLLECT_LOCAL_EXPORT);
        checkMissingFiles(Mode.WARN);
    }

    /**
     * Used for the export to the repository. It checks the orphan files and issues a warning, then, if the parameter
     * is not true, copies the files to a backup location. This is the path where the file is + "/original/".
     */
    public void checkMissingFilesForRepositoryExport(boolean skipOldDataFilesBackup) {
        referencedFiles.clear();
        checkMissingFiles(Mode.COLLECT);
        checkMissingFiles(skipOldDataFilesBackup ? Mode.WARN : Mode.BACKUP);
    }

    /**
     * Check all the missing files by invoking {@link #checkStudy(Study, Mode)} for all studies in the
     * store passed to the constructor.
     */
    private void checkMissingFiles(Mode mode) {
        try {
            for (Study study : store.valuesOfType(Study.class)) {
                checkStudy(study, mode);
            }
        }
        catch (IOException ex) {
            throw new TabIOException(i18n.msg("isatab_export_data_file_io", ex.getMessage()), ex);
        }
    }

    /**
     * Check the files about a single study.
     *
     * @param mode the kind of behavior wanted, see {@link Mode}
     */
    private void checkStudy(Study study, Mode mode) throws IOException {
        for (AssayGroup ag : store.valuesOfType(AssayGroup.class)) {
            if (ag.getStudy() != study) {
                continue;
            }

            // It's better to show at least the study dirs, so that user doesn't think there are errors.
            // So, create it in case not yet here
            //
            for (AnnotationTypes targetType : AnnotationTypes.DATA_PATH_ANNOTATIONS) {
                String dispatchPath = StringUtils.trimToNull(dataLocationMgr.getDataLocation(
                        study, ag.getMeasurement(), ag.getTechnology(), targetType)
                );

                if (dispatchPath == null)
                // TODO: externalization
                // TODO: single message
                {
                    log.debug(MessageFormat.format(
                            "WARNING: no file dispatching destination defined for the type: \"{0}\"/\"{1}\"/\"{2}\", files of this type will be ignored",
                            ag.getMeasurement().getName(), ag.getTechnologyName(), targetType
                    ));
                }
            }

            if (mode == Mode.COLLECT || mode == Mode.COLLECT_LOCAL_EXPORT) {
                collectReferredFiles(ag, mode);
            } else
            // WARN || BACKUP
            {
                processOrphanFiles(ag, mode);
            }
        }
    }


    /**
     * Track all the files references by a given assay group, saving them into {@link #referencedFiles}. This will be
     * used by {@link #processOrphanFiles(AssayGroup, Mode)}.
     */
    private void collectReferredFiles(final AssayGroup ag, Mode mode) throws IOException {
        if (ag == null) {
            return;
        }

        SectionInstance assaySectionInstance = ag.getAssaySectionInstance();
        if (assaySectionInstance == null) {
            log.warn("I cannot find any record in the DB for the file: " + ag.getFilePath() + ", hope it's fine");
            return;
        }

        log.debug("Checking the repository files referred by " + ag.getFilePath());

        Study study = ag.getStudy();

        for (Record record : assaySectionInstance.getRecords()) {
            int recSize = record.size();
            for (int fieldIndex = 0; fieldIndex < recSize; fieldIndex++) {
                // TODO: change this so that we get: field name, file-type, file-path
                String[] filePathPair = FormatExporter.getExternalFileValue(record, fieldIndex);

                if (filePathPair == null) {
                    continue;
                }
                String fieldHeader = filePathPair[0],
                        srcFileRelPath = filePathPair[1],
                        srcFileType = filePathPair[2];

                if (srcFileRelPath == null) {
                    continue;
                }

                AnnotationTypes targetType;
                if ("raw".equals(srcFileType)) {
                    targetType = AnnotationTypes.RAW_DATA_FILE_PATH;
                } else if ("processed".equals(srcFileType)) {
                    targetType = AnnotationTypes.PROCESSED_DATA_FILE_PATH;
                } else {
                    targetType = AnnotationTypes.GENERIC_DATA_FILE_PATH;
                }

                String dataLocationPath = dataLocationMgr.getDataLocation(
                        study, ag.getMeasurement(), ag.getTechnology(), targetType
                );
                if (dataLocationPath == null) {
                    log.trace("source path not available for the file " + dataLocationPath);
                    continue;
                }
                String srcFilePath = dataLocationPath + "/" + srcFileRelPath;
                File srcFile = new File(srcFilePath);
                srcFilePath = srcFile.getCanonicalPath();

                if (!srcFile.exists()) {
                    if (mode == Mode.COLLECT) {
                        log.info("Source file '" + srcFilePath + "' / '" + fieldHeader + "' not found");
                    }
                } else {
                    if (mode == Mode.COLLECT || mode == Mode.COLLECT_LOCAL_EXPORT) {
                        this.referencedFiles.put(srcFile, srcFileRelPath);
                    }
                }

            } // for ( field )
        } // for ( record )
    } // checkAssayGroup ()


    /**
     * Process all files that results non referenced, ie they're not in {@link #referencedFiles}, after that this was
     * computed by {@link #collectReferredFiles(AssayGroup, Mode)}. For these files issues a warning and also
     * copies them on a backup location, if mode == {@link Mode#BACKUP}.
     */
    private void processOrphanFiles(final AssayGroup ag, Mode mode) throws IOException {
        if (ag == null) {
            return;
        }

        SectionInstance assaySectionInstance = ag.getAssaySectionInstance();
        if (assaySectionInstance == null) {
            log.warn("I cannot find any record in the DB for the file: " + ag.getFilePath() + ", hope it's fine");
            return;
        }

        Study study = ag.getStudy();

        // TODO: generic
        for (AnnotationTypes targetType : AnnotationTypes.DATA_PATH_ANNOTATIONS) {
            String dataLocationPath = StringUtils.trimToNull(dataLocationMgr.getDataLocation(
                    study, ag.getMeasurement(), ag.getTechnology(), targetType
            ));
            if (dataLocationPath == null) {
                continue;
            }
            File dataLocationDir = new File(dataLocationPath);
            dataLocationPath = dataLocationDir.getCanonicalPath();
            if (!dataLocationDir.isDirectory() && !dataLocationDir.canRead() && !dataLocationDir.canWrite()) {
                log.warn("Directory '" + dataLocationPath + "' is not accessible, skipping it");
                continue;
            }

            log.debug("Checking unreferenced files into '" + dataLocationPath + "'");

            Collection<File> existingFiles = FileUtils.listFiles(dataLocationDir, null, true);
            for (File existingFile : existingFiles) {
                if (this.referencedFiles.containsKey(existingFile)) {
                    continue;
                }
                String existingPath = existingFile.getCanonicalPath();

                log.warn("WARNING: The file '" + existingPath + "' is no longer referred by the exported submission");
                if (mode != Mode.BACKUP) {
                    continue;
                }

                String backupPath = dataLocationPath + "/original";
                log.warn("Moving it to the backup location: '" + backupPath + "'");
                String fileRelPath = existingPath.substring(dataLocationPath.length() + 1);
                String backupFilePath = backupPath + "/" + fileRelPath;
                File backupFile = new File(backupFilePath);

                if (backupFile.exists() && backupFile.lastModified() == existingFile.lastModified()) {
                    log.debug("Not copying " + existingPath + "' to '" + backupFilePath
                            + "': they seem to be the same.");
                    continue;
                }
                log.trace("Copying data file '" + existingPath + "' / '" + backupFilePath + "' to data repository...");
                FileUtils.moveFile(existingFile, backupFile);
                // Needed cause of a bug in the previous function
                backupFile.setLastModified(existingFile.lastModified());
                log.trace("...done");

                // Do it only once
                this.referencedFiles.put(existingFile, "");
            }
        }

    }
}
