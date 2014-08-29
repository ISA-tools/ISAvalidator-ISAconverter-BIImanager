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

package org.isatools.isatab_v1;

import org.apache.commons.lang.StringUtils;
import org.isatools.isatab.ISATABPersister;
import org.isatools.isatab.isaconfigurator.ISAConfigurationSet;
import org.isatools.isatab.mapping.AssayTypeEntries;
import org.isatools.tablib.exceptions.TabDuplicatedValueException;
import org.isatools.tablib.exceptions.TabMissingValueException;
import org.isatools.tablib.exceptions.TabValidationException;
import org.isatools.tablib.mapping.FormatSetTabMapper;
import org.isatools.tablib.schema.*;
import org.isatools.tablib.utils.logging.TabNDC;
import uk.ac.ebi.bioinvindex.utils.i18n;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;


/**
 * The specific loader to be used with the ISATAB format.
 * <p/>
 * PLEASE NOTE: This is *not* the GUI Loader, mentioned in the end-user documentation. That loader is actually
 * a wrapper of {@link ISATABPersister}. This loader refers to the first in-memory loading of the ISATAB structure,
 * which produces an object model that reflects the strucure in the spreadsheet files (named the Tabular View).
 * date: Mar 12, 2008
 *
 * @author brandizi
 */
public class ISATABLoader extends org.isatools.isatab.ISATABLoader {

    /**
     * This file is in included in the submission tool .jar and describes the ISATAB format
     */
    public static final String ISATAB_SCHEMA_PATH = "/isatab_v1_200810.format.xml";
    private static FormatSet isatabSchema;

    public ISATABLoader(String basePath) {
        super(ISATABLoader.getISATABSchema(), basePath);
    }


    public static FormatSet getISATABSchema() {
        if (isatabSchema != null) {
            return isatabSchema;
        }

        TabNDC ndc = TabNDC.getInstance();
        ndc.pushFormat("isatab_schema", "ISATAB-schema", ISATAB_SCHEMA_PATH);
        InputStream input = new BufferedInputStream(FormatSetTabMapper.class.getResourceAsStream(ISATABLoader.ISATAB_SCHEMA_PATH));
        isatabSchema = SchemaBuilder.loadFormatSetFromXML(input);
        ndc.popTabDescriptor();
        return isatabSchema;
    }


    /**
     * A new version with updated file names.
     */
    @Override
    protected void loadAssays() throws IOException {
        TabNDC ndc = TabNDC.getInstance();

        FormatSetInstance formatSetInstance = getFormatSetInstance();
        List<SectionInstance> assayInstances = formatSetInstance.getSectionInstances("investigation", "assays");

        if (assayInstances.size() == 0) {
            log.warn(i18n.msg("missing_assays_from_investigation_file"));
            return;
        }

        for (SectionInstance assayInstance : assayInstances) {
            if (assayInstance.getFields().size() > 0) {
                int endPointIdx = assayInstance.getField("Study Assay Measurement Type").getIndex();
                int technologyFieldIdx = assayInstance.getField("Study Assay Technology Type").getIndex();
                int assayFileNameIdx = assayInstance.getField("Study Assay File Name").getIndex();

                for (Record record : assayInstance.getRecords()) {
                    String endPoint = StringUtils.trimToNull(record.getString(endPointIdx));
                    String technology = StringUtils.trimToEmpty(record.getString(technologyFieldIdx));
                    String assayFileName = StringUtils.trimToEmpty(record.getString(assayFileNameIdx));

                    if (StringUtils.trimToNull(endPoint) == null && assayFileName.isEmpty()) {
                        continue;
                    } else if (endPoint == null) {
                        throw new TabMissingValueException("No Measurement specified for the assay file: '" + assayFileName
                                + "' (column #" + (assayFileNameIdx + 1) + "), measurement type is a mandatory attribute for the assay file");
                    }

                    // We can only deal with one of the formats defined in the ISATAB specification, and defined
                    // in the ISATAB schema file (in /main/resources/).
                    String assayFormatId = AssayTypeEntries.getAssayFormatIdFromLabels(endPoint, technology);

                    if (assayFormatId == null) {
                        String msg = "Ignoring the unknown assay of type: '" + endPoint + "' / '" + technology
                                + "', assay file:'" + assayFileName + "' (column #" + (assayFileNameIdx + 1) + ")";
                        log.warn(msg);
                    } else {
                        ndc.pushFormat(assayFormatId, assayFormatId, assayFileName);
                        String alreadyLoadedLabel = alreadyLoadedFiles.get(assayFileName);
                        if (alreadyLoadedLabel != null) {
                            throw new TabDuplicatedValueException(
                                    "The file " + assayFileName + " has already been loaded as " + alreadyLoadedLabel
                            );
                        }
                        FormatInstance assayFileInstance;
                        try {
                            assayFileInstance = load(assayFileName, assayFormatId);
                        } catch (TabValidationException tve) {
                            assayFileInstance = load(assayFileName, "generic_assay");
                        }

                        formatSetInstance.addFormatInstance(assayFileInstance);
                        alreadyLoadedFiles.put(assayFileName, "as assay file for an assay");
                        ndc.popTabDescriptor();
                    }
                } // for record
            }
        } // for assayInstance
    } // loadAssays

}
