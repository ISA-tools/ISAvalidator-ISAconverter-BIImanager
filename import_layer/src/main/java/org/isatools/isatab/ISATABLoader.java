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

package org.isatools.isatab;

import org.apache.commons.io.FileUtils;
import org.isatools.tablib.exceptions.TabDuplicatedValueException;
import org.isatools.tablib.exceptions.TabInvalidValueException;
import org.isatools.tablib.exceptions.TabMissingResourceException;
import org.isatools.tablib.mapping.FormatSetTabMapper;
import org.isatools.tablib.parser.TabLoader;
import org.isatools.tablib.schema.*;
import uk.ac.ebi.bioinvindex.utils.i18n;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.apache.commons.io.filefilter.FileFilterUtils.*;

/**
 * The specific loader to be used with the ISATAB format.
 * PLEASE NOTE: This is *not* the GUI Loader, mentioned in the end-user documentation. That loader is actually
 * a wrapper of {@link ISATABPersister}. This loader refers to the first in-memory loading of the ISATAB structure.
 * date: Mar 12, 2008
 *
 * @author brandizi
 */
public class ISATABLoader extends TabLoader {
    /**
     * This file is in included in the submission tool .jar and describes the ISATAB format
     */
    public static final String ISATAB_SCHEMA_PATH = "/isatab_0.2.format.xml";
    private static FormatSet isatabSchema;

    private String investigationFileName = "investigation.csv";

    /**
     * Allows to trace already loaded files and do checkings about
     * Entries are: filename, label about what it was met first time for, e.g.:
     * "investigation.csv, Investigation File".
     */
    protected Map<String, String> alreadyLoadedFiles = new HashMap<String, String>();

    /**
     * Initializes the loader with the schema defined in {@link #ISATAB_SCHEMA_PATH}
     */
    public ISATABLoader(String basePath) {
        this(ISATABLoader.getISATABSchema(), basePath);
    }

    @SuppressWarnings("unchecked")
    protected ISATABLoader(FormatSet schema, String basePath) {
        super(schema, basePath);

        // Let's see if it is a directory or a file, try to get the default name otherwise
        //
        File sourcePathF = new File(basePath);
        if (sourcePathF.isFile()) {
            this.basePath = sourcePathF.getParent();
            investigationFileName = sourcePathF.getName();
        } else {
            Collection<File> ifiles = FileUtils.listFiles(
                    sourcePathF, andFileFilter(prefixFileFilter("i_"), suffixFileFilter(".txt")), null
            );
            int nfiles = ifiles.size();
            if (nfiles < 1) {
                // Also try investigation.csv, it is not mentioned by the specs but used in many tests
                investigationFileName = "investigation.csv";
                File ifile = new File(basePath + "/" + investigationFileName);
                if (!ifile.isFile() || !ifile.canRead()) {
                    throw new TabMissingResourceException(i18n.msg("missing_investigation_file", basePath));
                }
                String msg = "  Warning: no investigation file name i_xxx.txt found in '" + basePath + "'\n"
                        + "  using '" + investigationFileName + "' instead";
                log.warn(msg);
            } else if (nfiles > 1) {
                // More than 1? Use the most recent.
                List<File> lifiles = new ArrayList<File>(ifiles);
                Collections.sort(lifiles, new Comparator<File>() {
                    public int compare(File f1, File f2) {
                        if (f1 == null) {
                            return f2 == null ? 0 : 1;
                        }
                        if (f2 == null) {
                            return f1 == null ? 0 : -1;
                        }
                        return -1 * new Long(f1.lastModified()).compareTo(f2.lastModified());
                    }
                });
                investigationFileName = lifiles.get(0).getName();
                String msg =
                        "\n  Warning: More than one i_xxx.txt investigation file found in '" + basePath + "'\n"
                                + "  using the most recent: '" + investigationFileName + "'";
                log.warn(msg);
            } else {
                investigationFileName = ifiles.iterator().next().getName();
                String msg = "Working with '" + investigationFileName + "' investigation file from '" + basePath + "'";
                log.info(msg);
            }
        }
    }


    /**
     * Loads the investigation file.
     */
    private void loadInvestigation() throws IOException {
        FormatInstance investigationInstance = load(investigationFileName, "investigation");
        getFormatSetInstance().addFormatInstance(investigationInstance);
        alreadyLoadedFiles.put(investigationFileName, "as Investigation file");
    }

    /**
     * Loads the studies in the Study blocks of the Investigation files. Moreover, check the reference about
     * the study file made in each "Studies" section and loads the corresponding study_sample files.
     */
    private void loadStudies() throws IOException {
        FormatSetInstance formatSetInstance = getFormatSetInstance();
        List<SectionInstance> studyInstances = formatSetInstance.getSectionInstances("investigation", "study");

        if (studyInstances.size() == 0) {
            log.warn("missing_sample_file_from_investigation_file");
            return;
        }

        for (SectionInstance studyInstance : studyInstances) {
            String studyFileId = studyInstance.getString(0, "Study File Name");
            String alreadyLoadedLabel = alreadyLoadedFiles.get(studyFileId);
            if (alreadyLoadedLabel != null) {
                throw new TabDuplicatedValueException(i18n.msg(
                        "study_file_already_exists", studyFileId, alreadyLoadedLabel
                ));
            }
            FormatInstance studyFileInstance = load(studyFileId, "study_samples");
            formatSetInstance.addFormatInstance(studyFileInstance);
            alreadyLoadedFiles.put(studyFileId, "as sample file for a study");
        }
    }

    /**
     * Check all the references assays in the Study Assays of the Study block and loads the corresponding
     * assay files.
     */
    protected void loadAssays() throws IOException {
        FormatSetInstance formatSetInstance = getFormatSetInstance();
        List<SectionInstance> assayInstances = formatSetInstance.getSectionInstances("investigation", "assays");
        for (SectionInstance assayInstance : assayInstances) {
            int endPointIdx = assayInstance.getField("Measurements/Endpoints Name").getIndex();
            int technologyFieldIdx = assayInstance.getField("Technology Type").getIndex();
            int assayFileNameIdx = assayInstance.getField("Assay File Name").getIndex();
            for (Record record : assayInstance.getRecords()) {
                String endPoint = record.getString(endPointIdx);
                String technology = record.getString(technologyFieldIdx);
                String assayFileName = record.getString(assayFileNameIdx);

//			  DNA microarray
//			  Gel Electrophoresis
//			  Mass Spectrometry
//			  NMR Spectroscopy
//			  High throughput sequencing

                String assayFormatId = null;
                if ("DNA Microarray".equalsIgnoreCase(technology)) {
                    assayFormatId = "transcriptomics_assay";
                } else if ("Mass Spectrometry".equalsIgnoreCase(technology)) {
                    assayFormatId = "ms_spec_assay";
                } else {
                    throw new TabInvalidValueException(i18n.msg("unknown_assay_type", endPoint, technology));
                }

                FormatInstance assayFileInstance = load(assayFileName, assayFormatId);
                formatSetInstance.addFormatInstance(assayFileInstance);
            }
        }
    }


    /**
     * Loads the whole set of ISATAB spreadsheets.
     *
     * @return the same format set instance returned by {@link TabLoader#getFormatSetInstance()}
     */
    public FormatSetInstance load() throws IOException {
        // Load the Investigation file
        loadInvestigation();

        // Load the studies in Investigation.studyFileName
        loadStudies();

        // For all the assay files named in the assays, load the file
        loadAssays();

        return getFormatSetInstance();
    }


    public static FormatSet getISATABSchema() {
        if (isatabSchema != null) {
            return isatabSchema;
        }

        InputStream input = new BufferedInputStream(FormatSetTabMapper.class.getResourceAsStream(ISATAB_SCHEMA_PATH));
        return isatabSchema = SchemaBuilder.loadFormatSetFromXML(input);
    }


    /**
     * Returns the investigation file name detected in the class constructor
     */
    public String getInvestigationFileName() {
        return investigationFileName;
    }

}
