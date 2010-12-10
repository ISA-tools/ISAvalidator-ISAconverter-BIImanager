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
import java.util.List;

/**
 * The file dispatcher used by the ISATAB exporter ({@link ISATABDBExporter}), for the export to a local directory.
 * This dispatcher works with the data files only (the meta data are already exported by the exporter itself).
 *
 * @author brandizi
 *         <b>date</b>: Mar 11, 2010
 */
public class ISATabExportDataFilesDispatcher {

    private BIIObjectStore store;
    private final DataLocationManager dataLocationMgr;
    private final String exportPath;

    protected static final Logger log = Logger.getLogger(ISATabExportDataFilesDispatcher.class);


    /**
     * @param store           the submission to be dispatched, null when invoking {@link #undispatch(List)}
     * @param sourcePath      the path of the submission directory, null when undispatching.
     * @param dataLocationMgr the location manager that allows to know where the data files are in the
     *                        BII repository.
     * @param exportPath      the path where all the data files go.
     */
    public ISATabExportDataFilesDispatcher(
            BIIObjectStore store, DataLocationManager dataLocationMgr, String exportPath) {
        this.store = store;
        this.dataLocationMgr = dataLocationMgr;
        this.exportPath = exportPath;
    }

    /**
     * Does the job of copying the files about a given submission to the export path passed to the constructor.
     */
    public void dispatch() {

        try {
            for (Study study : store.valuesOfType(Study.class)) {
                dispatchStudy(study);
            }
        }
        catch (IOException ex) {
            throw new TabIOException(i18n.msg("isatab_export_data_file_io", ex.getMessage()), ex);
        }
    }

    /**
     * Dispatch the files about the study and then calls dispatch operations over the assays
     * ({@link #dispatchAssayGroup(String, AssayGroup)}.
     */
    private void dispatchStudy(Study study) throws IOException {
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

            dispatchAssayGroup(ag);
        }
    }

    /**
     * Dispatch the files about a given assay group
     *
     * @param outPath where the files go, no dispatch done if this is null.
     */
    private void dispatchAssayGroup(final AssayGroup ag) throws IOException {
        if (ag == null) {
            return;
        }

        SectionInstance assaySectionInstance = ag.getAssaySectionInstance();
        if (assaySectionInstance == null) {
            log.warn("I cannot find any record in the DB for the file: " + ag.getFilePath() + ", hope it's fine");
            return;
        }

        log.debug("Dispatching the files referred by " + ag.getFilePath());

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

                String srcFilePath = dataLocationMgr.getDataLocation(
                        study, ag.getMeasurement(), ag.getTechnology(), targetType
                );
                if (srcFilePath == null) {
                    log.trace("source path not available for the file " + srcFileRelPath);
                    continue;
                }

                srcFilePath += "/" + srcFileRelPath;
                File srcFile = new File(srcFilePath);
                srcFilePath = srcFile.getCanonicalPath();
                File targetDir = new File(exportPath);
                String targetFilePath = exportPath + "/" + srcFileRelPath;
                File targetFile = new File(targetFilePath);
                targetFilePath = targetFile.getCanonicalPath();

                if (!srcFile.exists()) {
                    log.info("WARNING: Source file '" + srcFilePath + "' / '" + fieldHeader + "' not found");
                } else {
                    if (targetFile.exists() && targetFile.lastModified() == srcFile.lastModified()) {
                        log.debug("Not copying " + srcFilePath + "' to '" + targetFilePath
                                + "': they seem to be the same.");
                    } else {
                        log.trace("Copying data file '" + fieldHeader + "' / '" + srcFilePath + "' to data repository...");
                        FileUtils.copyFileToDirectory(srcFile, targetDir, true);
                        // Needed cause of a bug in the previous function
                        targetFile.setLastModified(srcFile.lastModified());
                        log.trace("...done");
                        log.info("Data file '" + fieldHeader + "' / '" + srcFilePath + "' copied to '" + targetFilePath + "'");
                    }
                } // if
            } // for ( field )
        } // for ( record )
    } // dispatchAssayGroup ()

}
