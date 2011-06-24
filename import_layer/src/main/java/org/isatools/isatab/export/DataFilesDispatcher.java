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

package org.isatools.isatab.export;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.isatools.isatab.commandline.AbstractImportLayerShellCommand;
import org.isatools.isatab.mapping.AssayGroup;
import org.isatools.isatab.mapping.StudyWrapper;
import org.isatools.tablib.exceptions.TabIOException;
import org.isatools.tablib.export.FormatExporter;
import org.isatools.tablib.schema.Record;
import org.isatools.tablib.schema.SectionInstance;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.dao.StudyDAO;
import uk.ac.ebi.bioinvindex.dao.ejb3.DaoFactory;
import uk.ac.ebi.bioinvindex.model.Study;
import uk.ac.ebi.bioinvindex.model.processing.Assay;
import uk.ac.ebi.bioinvindex.model.term.AnnotationTypes;
import uk.ac.ebi.bioinvindex.model.term.AssayTechnology;
import uk.ac.ebi.bioinvindex.model.term.Measurement;
import uk.ac.ebi.bioinvindex.model.xref.ReferenceSource;
import uk.ac.ebi.bioinvindex.model.xref.Xref;
import uk.ac.ebi.bioinvindex.utils.datasourceload.DataLocationManager;
import uk.ac.ebi.bioinvindex.utils.datasourceload.DataSourceConfigFields;

import javax.persistence.EntityManager;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The ISATAB file dispatcher. Copies the ISATAB meta-data files and data files to the repositories, according to the
 * configuration retrieved by the {@link DataLocationManager}.
 * TODO: probably the {@link #undispatch(List)} method should be in an Undispatcher class.
 *
 * @author brandizi
 */
public class DataFilesDispatcher {

    private BIIObjectStore store;
    private final String sourcePath;
    private final DataLocationManager dataLocationMgr;

    protected static final Logger log = Logger.getLogger(DataFilesDispatcher.class);

    /**
     * Wrapper with store = null, sourcePath = null, used for undispatching
     *
     * @param entityManager @link #EntityManager
     */
    public DataFilesDispatcher(EntityManager entityManager) {
        this(null, null, entityManager);
    }

    /**
     * @param store         the submission to be dispatched, null when invoking {@link #undispatch(List)}
     * @param sourcePath    the path of the submission directory, null when undispatching.
     * @param entityManager we need this cause the configuration is taken from {@link DataLocationManager}
     */
    public DataFilesDispatcher(BIIObjectStore store, String sourcePath, EntityManager entityManager) {
        this.store = store;
        this.sourcePath = sourcePath;
        this.dataLocationMgr = AbstractImportLayerShellCommand.createDataLocationManager(entityManager);
    }


    /**
     * WARNING: IT IS NECESSARY that this is called BEFORE persistence, since it also calls {@link #addFilePathAnnotations(uk.ac.ebi.bioinvindex.model.Study)},
     * which needs to check if files were copied to the target repositories.
     */
    public void dispatch() {

        try {
            for (Study study : store.valuesOfType(Study.class)) {
                dispatchStudy(study);
                addFilePathAnnotations(study);
            }
        } catch (IOException ex) {
            throw new TabIOException(MessageFormat.format(
                    "ERROR while copying the submission files to file repositories: {0}", ex.getMessage()), ex
            );
        }
    }

    /**
     * Dispatch the files about the study and then calls dispatch operations over the assays
     * ({@link #dispatchAssayGroup(org.isatools.isatab.mapping.AssayGroup)}.
     *
     * @param study @link #Study to be dispatched
     * @throws java.io.IOException Can be thrown in the event of file system issues.
     */
    private void dispatchStudy(Study study) throws IOException {
        // Copy the investigation file to the submission repo
        // TODO: for the moment this works cause a submission can have one investigation only. In case
        // the many-to-many cardinality of study-investigation relation is exploited, we need to review this
        String investigationFileName = study.getSingleAnnotationValue("investigationFile");
        dispatchFileToSubmissionRepo(study, "Investigation File Name", investigationFileName);

        // Copy the sample file to the submission repo
        String sampleFilePath = StudyWrapper.findSampleFile(store, study);
        if (sampleFilePath == null) {
            log.warn("Sample file not found for " + study.getAcc());
        }
        dispatchFileToSubmissionRepo(study, "Study File Name", sampleFilePath);

        for (AssayGroup ag : store.valuesOfType(AssayGroup.class)) {
            if (ag.getStudy() != study) {
                continue;
            }


            // It's better to show at least the study dirs, so that user doesn't think there are errors.
            // So, create it in case not yet here
            //
            for (AnnotationTypes targetType : AnnotationTypes.DATA_PATH_ANNOTATIONS) {
                String dispatchPath = StringUtils.trimToNull(dataLocationMgr.getDataLocation(
                        study, ag.getMeasurement(), ag.getTechnology(), targetType
                ));

                if (dispatchPath != null) {
                    File outDir = new File(dispatchPath);
                    if (!outDir.exists()) {
                        FileUtils.forceMkdir(outDir);
                    }
                } else {
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
     * @param ag the @see AssayGroup to be dispatched.
     * @throws java.io.IOException - this method is resolving file paths, IO Exceptions are therefore a likelihood.
     */
    private void dispatchAssayGroup(final AssayGroup ag) throws IOException {
        SectionInstance assaySectionInstance = ag.getAssaySectionInstance();
        if (assaySectionInstance == null) {
            return;
        }

        Study study = ag.getStudy();

        // Copy the assay file to the submission repo
        String assayFileRelativePath = assaySectionInstance.getFileId();
        dispatchFileToSubmissionRepo(ag.getStudy(), "Assay File Name", assayFileRelativePath);

        for (Record record : assaySectionInstance.getRecords()) {
            int recSize = record.size();
            for (int fieldIndex = 0; fieldIndex < recSize; fieldIndex++) {

                String[] filePathTriple = FormatExporter.getExternalFileValue(record, fieldIndex);

                if (filePathTriple == null) {
                    continue;
                }

                String fieldHeader = filePathTriple[0];
                String sourceFilePath = filePathTriple[1];
                String srcFileType = filePathTriple[2];

                if (sourceFilePath == null) {
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

                String targetPath = StringUtils.trimToNull(
                        dataLocationMgr.getDataLocation(study, ag.getMeasurement(), ag.getTechnology(), targetType));


                if (targetPath == null) {
                    continue;
                }

                // Relative paths occur e.g. for an unzipped, self-contained ISAtab archive,
                // Absolute paths can be expected if the ISAtab files are describing data 
                // on a local filesystem

                File srcFile = new File(sourceFilePath);
                // if the path already exists, it is an absolute path
                if (!srcFile.exists()) {
                    // otherwise, it's most likely to be a relative path
                    srcFile = new File(sourcePath + "/" + sourceFilePath);
                }

                File targetDir = new File(targetPath);

                File targetFile = new File(targetPath + "/" + (new File(sourceFilePath)).getName());

                if (!srcFile.exists()) {
                    log.info("WARNING: Source file '" + sourceFilePath + "' / '" + fieldHeader + "' not found");
                } else {

                    if (targetFile.exists() && targetFile.lastModified() == srcFile.lastModified()) {
                        log.debug("Not copying “" + srcFile.getCanonicalPath() + "' to '" + targetFile.getCanonicalPath()
                                + "': they seem to be the same.");
                    } else {
                        log.trace("Copying data file '" + fieldHeader + "' / '" + sourceFilePath + "' to data repository...");

                        if (srcFile.isDirectory()) {
                            FileUtils.copyDirectory(srcFile, targetFile, true);
                        } else {
                            FileUtils.copyFileToDirectory(srcFile, targetDir, true);
                        }

                        // Needed cause of a bug in the copyFileToDirectory() function
                        targetFile.setLastModified(srcFile.lastModified());
                        log.trace("...done");
                        log.info(
                                "Data file '" + fieldHeader + "' / '" + srcFile.getCanonicalPath() + "' copied to '"
                                        + targetDir.getCanonicalPath() + "'"
                        );
                    }
                } // if
            } // for ( field )
        } // for ( record )
    } // dispatchAssayGroup ()


    /**
     * Dispatches a file to the submission repository, where meta-data files are supposed to go.
     *
     * @param study               - @link #Study to be dispatched
     * @param fieldName           - Field containing the data file locations?
     * @param srcFileRelativePath - Relative path of Source files
     * @throws java.io.IOException - Can be thrown as a result of file system problems.
     */
    private void dispatchFileToSubmissionRepo(Study study, String fieldName, String srcFileRelativePath)
            throws IOException {
        // Get the file from the location manager
        String targetPath = dataLocationMgr.buildISATabMetaDataLocation(study);

        if (targetPath == null) {
            log.debug(
                    "No META-data file location defined, skipping files copy, probably you need to edit data_locations.xml"
            );
            return;
        }

        srcFileRelativePath = StringUtils.trimToNull(srcFileRelativePath);
        if (srcFileRelativePath == null) {
            String msg = "WARNING: Empty value for file of type '" + fieldName + "', skipping copy";
            log.error(msg);
            return;
        }

        String srcFilePath = sourcePath + "/" + srcFileRelativePath;
        File srcFile = new File(srcFilePath);
        File targetDir = new File(targetPath);
        String targetFilePath = targetPath + "/" + srcFileRelativePath;
        File targetFile = new File(targetFilePath);

        if (!targetDir.exists()) {
            log.info("Creating the submission directory: '" + targetPath + "'");
            FileUtils.forceMkdir(targetDir);
        }

        log.trace("Copying " + srcFile.getPath() + " to '" + targetFilePath + "'...");
        FileUtils.copyFile(srcFile, targetFile, true);
        // needed, there's a bug in the previous function
        targetFile.setLastModified(srcFile.lastModified());

        log.trace("...done");

        String backupTargetPath = targetPath + "/original";
        String backupFilePath = backupTargetPath + "/" + srcFileRelativePath;
        File backupFile = new File(backupFilePath);
        log.trace("Copying " + srcFile.getPath() + " to '" + backupFilePath + "'...");
        FileUtils.copyFile(srcFile, backupFile, true);
        // needed, there's a bug in the previous function
        backupFile.setLastModified(srcFile.lastModified());
        log.trace("...done");

        log.info("'" + srcFile.getPath() + "' copied to '" + targetFilePath + "' (and to original/)");
    }


    /**
     * Remove those files that were copied into data file repositories, i.e.: directories built on the basis of
     * study accessions and their contents.
     *
     * @param studies - @link #List<Study> to be unloaded
     */
    public void undispatch(List<Study> studies) {
        final String placeOlder = DataSourceConfigFields.ACCESSION_PLACEHOLDER.getName();

        try {
            if (studies == null || studies.size() == 0) {
                return;
            }

            for (Study study : studies) {
                String studyAccession = StringUtils.trimToNull(study.getAcc());

                if (studyAccession == null) {
                    log.error("Data file unloader, Cannot work with null accession");
                    continue;
                }

                for (String location : dataLocationMgr.getAllDataLocations()) {
                    if (location == null) {
                        continue;
                    }

                    // We need to remove what follows the study accession, cause the whole directory about the study must be
                    // removed
                    //
                    int istrip = location.indexOf(placeOlder);
                    String studyLoc = StringUtils.substring(location, 0, istrip + placeOlder.length());
                    studyLoc = DataLocationManager.buildLocation(studyLoc, study);

                    File targetDir = new File(studyLoc);
                    if (targetDir.exists()) {
                        log.trace("Deleting '" + studyLoc + "'...");
                        FileUtils.deleteDirectory(targetDir);
                        log.trace("...done");
                        log.info("'" + studyLoc + "' deleted by the unloader");
                    } else {
                        log.trace("Skipping the deletion of non-existent:" + studyLoc);
                    }
                }
            }
        } catch (IOException ex) {
            throw new TabIOException(MessageFormat.format(
                    "ERROR while undeleting the submission files from file repositories: {0}", ex.getMessage(), ex
            ));
        }
    }


    /**
     * Adds Xreferences to assays that need to have a web link to their data files.
     * TODO: this is a quite dirty solution that comes from previous implementations. We add these annotations just
     * to mark the fact the assays has indeed some file that was actually copied to some repository. The web link
     * is no longer set here, but taken from the system configuration, i.e.: from DataLocationManager
     * @param study @link #Study to have files loaded for.
     */
    private void addFilePathAnnotations(Study study) {
        final String studyDirName = DataLocationManager.getObfuscatedStudyFileName(study);
        final String studyAcc = study.getAcc();
        final Set<String> messages = new HashSet<String>();

        for (Assay assay : study.getAssays()) {

            Measurement measurement = assay.getMeasurement();
            AssayTechnology technology = assay.getTechnology();


            String measurementTypeAccession = measurement.getAcc();
            String technologyAccession = technology.getAcc();
            String measurementType = measurement.getName();
            String technologyType = technology.getName();

            for (AnnotationTypes targetType : AnnotationTypes.DATA_PATH_ANNOTATIONS) {

                // todo need to make sure the correct data location reference is used for dispatch.
                String targetLocation = StringUtils.trimToNull(
                        dataLocationMgr.getDataLocation(study, measurement, technology, targetType));


                // if there is no target location specified, skip this iteration
                if (targetLocation == null || targetLocation.trim().equals("")) {
                    continue;
                }

                File[] dataFiles = new File(targetLocation).listFiles();

                if (dataFiles == null || dataFiles.length == 0) {
                    continue;
                }

                if (targetType == AnnotationTypes.DATA_PATH_ANNOTATIONS[0] // check it the first time only
                        && assay.getSingleXrefContaining(":RAW") != null
                        || assay.getSingleXrefContaining(":PROCESSED") != null
                        || assay.getSingleXrefContaining(":GENERIC") != null) {

                    messages.add(
                            "Some assays for the study #" + studyAcc + " have both file link annotation comments and a data location" +
                                    " defined. You should probably review the ISATAB or the configured data locations. Keeping the comments in" +
                                    " the assay file and skipping the data location definition for this file"
                    );
                    break;
                }


                String webAnnType = "", targetTypeTitle = "";

                if (targetType == AnnotationTypes.RAW_DATA_FILE_PATH) {
                    webAnnType = "WEB:RAW";
                    targetTypeTitle = "Raw Data Files Repository";
                } else if (targetType == AnnotationTypes.PROCESSED_DATA_FILE_PATH) {
                    webAnnType = "WEB:PROCESSED";
                    targetTypeTitle = "Processed Data Files Repository";
                } else {
                    webAnnType = "WEB:GENERIC";
                    targetTypeTitle = "Generic Data Files Repository";
                }

                webAnnType = measurementTypeAccession + ":" + technologyAccession + ":" + webAnnType;

                // TODO: these values are already inside the data location manager
                targetTypeTitle += " [" + measurementType + ", " + technologyType + "]";

                ReferenceSource source = new ReferenceSource(webAnnType);
                source.setDescription(targetTypeTitle);
                source.setAcc(webAnnType);
                // TODO: these values are already inside the data location manager
                source.setUrl("");
                Xref xref = new Xref(studyDirName, source);
                assay.addXref(xref);
            } // for targetType
        } // for assay

        for (String message : messages) {
            log.warn(message);
        }

    } // addFilePathAnnotations ()

}
