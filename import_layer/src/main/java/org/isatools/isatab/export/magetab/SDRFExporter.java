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

import org.isatools.isatab.mapping.AssayGroup;
import org.isatools.isatab.mapping.AssayTypeEntries;
import org.isatools.tablib.export.FormatExporter;
import org.isatools.tablib.schema.*;
import org.isatools.tablib.utils.BIIObjectStore;

import java.util.ArrayList;
import java.util.List;

public class SDRFExporter extends FormatExporter {
    private final AssayGroup assayGroup;


    /**
     * A single assay group (of microarray type) is associated to a single MAGETAB experiment, where our study corresponds to the
     * AE study.
     */
    public SDRFExporter(BIIObjectStore store, AssayGroup assayGroup) {
        super(store);
        this.assayGroup = assayGroup;
    }


    /**
     * Merge together the sample spreadsheet and the assay spreadsheet which are associated to the current assay group.
     */
    public FormatInstance export() {
        clearExternalFilePaths();

        Format sdrf = MAGETABExporter.getMAGETABSchema().getFormat("sdrf");
        Section sdrfSection = sdrf.getSection("sdrf_section");

        // Go through every assay group
        //

        if (!"magetab".equalsIgnoreCase(AssayTypeEntries.getDispatchTargetIdFromLabels(assayGroup))) {
            throw new RuntimeException(
                    "MAGE exporter: cannot export the non microarray Assay " + assayGroup.getFilePath()
            );
        }

        FormatInstance sdrfInstance = createFormatInstance();
        SectionInstance sdrfSectionInstance = new SectionInstance(sdrfSection);

        // Merge the sample file and the assay file
        //
        SectionInstance sampleSectionInstance = assayGroup.getSampleSectionInstance();
        SectionInstance assaySectionInstance = assayGroup.getAssaySectionInstance();

        List<Field> fields = new ArrayList<Field>(sampleSectionInstance.getFields());
        int nsampleFields = fields.size();
        fields.addAll(assaySectionInstance.getFields());

        List<Record> assayRecords = assaySectionInstance.getRecords();
        int nassayRecs = assayRecords.size();
        for (int irec = 0; irec < nassayRecs; irec++) {
            String sampleName = assaySectionInstance.getString(irec, "Sample Name");
            Record sampleRec = getRecordBySample(sampleSectionInstance, sampleName);
            Record assayRecord = assayRecords.get(irec);
            Record sdrfRecord = new Record(sdrfSectionInstance);

            // Go through all the fields (sample file + assay file)
            int isdrfField = 0, fieldsSize = fields.size();
            for (int ifield = 0; ifield < fieldsSize; ifield++) {
                final Field field = fields.get(ifield);
                final String fieldId = field.getId();

                // Are we in the sample file or in the assay file?
                final String value = ifield < nsampleFields
                        ? sampleRec.getString(ifield)
                        : assayRecord.getString(ifield - nsampleFields);

                // Do not dupe the field in the assay which refers to the sample name. We need to check the value too, because
                // multiple samples may appear.
                if (ifield >= nsampleFields && value.equals(sampleName) && "Sample Name".equals(fieldId)) {
                    continue;
                }

                if (irec == 0) {
                    // Add the field to the exported section
                    Field sdrfField = field.clone();
                    sdrfField.setSection(sdrfSection);
                    String sdrfFieldName = sdrfField.getAttr("id");

                    // Some name mapping between ISATAB an MAGETAB is needed
                    String newSdrfFieldName = null;
                    if ("Hybridization Assay Name".equals(sdrfFieldName)) {
                        newSdrfFieldName = "Hybridization Name";
                    } else if ("Unit".equals(sdrfFieldName)) {
                        // MAGETAB mandates a type
                        sdrfField.setAttr("type", "UnknownUnitType");
                    }

                    if (newSdrfFieldName != null) {
                        sdrfField.setAttr("id", newSdrfFieldName);
                    }

                    sdrfSectionInstance.addField(sdrfField);
                }

                // and add the record value from either the sample file or the assay file
                sdrfRecord.set(isdrfField, value);

                addExternalFilePath(sdrfRecord, isdrfField);
                isdrfField++;
            }

            sdrfSectionInstance.addRecord(sdrfRecord);
        }

        sdrfInstance.addSectionInstance(sdrfSectionInstance);

        return sdrfInstance;
    }


    /**
     * Finds the first record where there is a sample name equal to the parameter
     */
    private Record getRecordBySample(SectionInstance sectionInstance, String sampleName) {
        List<Record> records = sectionInstance.getRecords();
        int nrecs = records.size();
        for (int irec = 0; irec < nrecs; irec++) {
            if (sampleName.equals(sectionInstance.getString(irec, "Sample Name"))) {
                return records.get(irec);
            }
        }
        return null;
    }


    @Override
    protected FormatInstance createFormatInstance() {
        Format sdrf = MAGETABExporter.getMAGETABSchema().getFormat("sdrf");

        FormatInstance sdrfInstance = new FormatInstance(sdrf);
        sdrfInstance.setFileId("sdrf.txt");
        return sdrfInstance;
    }


}
