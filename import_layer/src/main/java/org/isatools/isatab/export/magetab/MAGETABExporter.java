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

package org.isatools.isatab.export.magetab;

import org.apache.commons.io.FileUtils;
import org.isatools.isatab.mapping.AssayGroup;
import org.isatools.isatab.mapping.AssayTypeEntries;
import org.isatools.tablib.export.FormatElementExporter;
import org.isatools.tablib.export.FormatSetExporter;
import org.isatools.tablib.mapping.FormatSetTabMapper;
import org.isatools.tablib.schema.FormatSet;
import org.isatools.tablib.schema.FormatSetInstance;
import org.isatools.tablib.schema.SchemaBuilder;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.utils.datasourceload.DataLocationManager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Set;

/**
 * The MAGE-TAB exporter, creates a single instance of the format.
 * <p/>
 * date: Apr 7, 2008
 *
 * @author brandizi
 */
public class MAGETABExporter extends FormatSetExporter {
    public static final String MAGETAB_SCHEMA_PATH = "/magetab_1.0.format.xml";
    private static FormatSet magetabSchema;
    private final SDRFExporter sdrfExporter;


    /**
     * A single assay group (of microarray type) is associated to a single MAGETAB experiment, where our study corresponds to the
     * AE study.
     *
     * @param assayGroup must be of microarray type, error is raised otherwise.
     */
    public MAGETABExporter(BIIObjectStore store, AssayGroup assayGroup) {
        super(store);
        exporters = new ArrayList<FormatElementExporter<?>>();
        exporters.add(new IDFExporter(store, assayGroup.getStudy()));
        sdrfExporter = new SDRFExporter(store, assayGroup);
        exporters.add(sdrfExporter);
    }

    @Override
    protected FormatSetInstance createFormatSetInstance() {
        return new FormatSetInstance(getMAGETABSchema());
    }


    /**
     * Uses the file in {@link #MAGETAB_SCHEMA_PATH}.
     */
    public static FormatSet getMAGETABSchema() {
        if (magetabSchema != null) {
            return magetabSchema;
        }

        InputStream input = new BufferedInputStream(FormatSetTabMapper.class.getResourceAsStream(MAGETAB_SCHEMA_PATH));
        return magetabSchema = SchemaBuilder.loadFormatSetFromXML(input);
    }

    /**
     * Does the exporting job, taking all the assays in store of type microarray and creating a MAGE-TAB submission for
     * each of them.
     */
    public static void dispatch(BIIObjectStore store, String sourcePath, String outPath) throws IOException {
        if (outPath == null) {
            outPath = "";
        }
        if (outPath.length() > 0) {
            outPath += "/";
        }
        outPath += "magetab";

        for (AssayGroup assayGroup : store.valuesOfType(AssayGroup.class)) {
            if (!"magetab".equalsIgnoreCase(AssayTypeEntries.getDispatchTargetIdFromLabels(assayGroup))) {
                continue;
            }

            String assayPath = outPath + "/" + DataLocationManager.filePath2Id(assayGroup.getFilePath());
            File assayPathDir = new File(assayPath);
            if (!assayPathDir.exists()) {
                FileUtils.forceMkdir(assayPathDir);
            }

            log.info("Exporting MAGETAB assay: " + assayGroup.getFilePath());
            MAGETABExporter exporter = new MAGETABExporter(store, assayGroup);

            FormatSetInstance mageInstance = exporter.export();

            mageInstance.dump(assayPath);

            // All the external files
            exporter.dispatchFiles(sourcePath, assayPath);
        }
    }


    /**
     * Those files which refer to external files, needed to build a correct MAGETAB submission file set. It is initialized
     * upon class loading. CAN ONLY BE CALLED AFTER {@link #export()}.
     */
    public Set<String> getExternalFileFieldNames() {
        return sdrfExporter.getExternalFileFieldHeaders();
    }

    public void dispatchFiles(String sourcePath, String outPath) throws IOException {
        sdrfExporter.dispatchFiles(sourcePath, outPath);
    }

}
