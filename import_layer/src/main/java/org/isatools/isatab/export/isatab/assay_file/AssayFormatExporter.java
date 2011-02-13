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

package org.isatools.isatab.export.isatab.assay_file;

import org.isatools.isatab.export.isatab.pipeline.IsaTabTableBuilder;
import org.isatools.isatab.mapping.AssayGroup;
import org.isatools.isatab_v1.ISATABLoader;
import org.isatools.tablib.exceptions.TabInternalErrorException;
import org.isatools.tablib.export.FormatArrayExporter;
import org.isatools.tablib.schema.*;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.model.Study;
import uk.ac.ebi.bioinvindex.utils.i18n;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Exports the information related to a single assay file, by using {@link IsaTabTableBuilder}.
 *
 * @author brandizi
 *         <b>date</b>: Jan 4, 2010
 */
public class AssayFormatExporter extends FormatArrayExporter {
    final String fileFormatId, fileId;

    public AssayFormatExporter(
            BIIObjectStore store, String fileFormatId, String fileId
    ) {
        super(store);
        this.fileFormatId = fileFormatId;
        this.fileId = fileId;
    }


    @Override
    public FormatInstanceArray export() {
        FormatInstanceArray result = new FormatInstanceArray();
        Collection<Study> studies = getStore().valuesOfType(Study.class);
        if (studies.size() == 0) {
            return result;
        }

        for (Study study : studies) {
            // builds a new spreadsheet with items about the current assay type
            //
            IsaTabTableBuilder tableBuilder = new IsaTabTableBuilder(getStore(), study, fileId);
            List<List<String>> pipelineTable = tableBuilder.getTable();

            Format tabFormat = ISATABLoader.getISATABSchema().getFormat(fileFormatId);
            FormatInstance tabFormatInstance = new FormatInstance(tabFormat);
            Section section = tabFormat.getSections().get(0);
            SectionInstance sectionInstance = new SectionInstance(section);
            tabFormatInstance.addSectionInstance(sectionInstance);

            List<String> headers = null;
            List<Integer> gtb2fields = null;

            // Translate the table used by IsaTabTableBuilder into the final tabular view, as it is required by the
            // tablib
            //
            for (List<String> row : pipelineTable) {
                if (headers == null) {
                    // Create the fields in the result section
                    headers = row;
                    // how the indexes in the original pipeline table are mapped to the final section instance.
                    // These two tables may be different, e.g.: due to ignored fields.
                    gtb2fields = new LinkedList<Integer>();

                    int icol = -1;
                    for (String header : headers) {
                        icol++;
                        Field field = section.getFieldByHeader(header, false);
                        if (field == null) {
                            // TODO: not sure this needs to be an error, for the moment it's only a warning cause a few of
                            // these bad guys come from tests.
                            //
                            log.warn(MessageFormat.format(
                                    "Header ''{0}'' is wrong for the type {1}, ignoring it", header, fileFormatId
                            ));
                            continue;
                        }

                        // Let's add a new real field on the basis of the header. The new field created gets its id from the
                        // original one in the schema, so it will have the "canonical" form, independently on what we found
                        // on the input (e.g.: upper case).
                        //
                        field = field.parseHeader(header, sectionInstance.size(), false);
                        sectionInstance.addField(field);
                        gtb2fields.add(icol);
                    }
                    continue;
                } // headers creation

                Record tabRow = new Record(sectionInstance);
                int rcol = -1;
                for (int icol : gtb2fields) {
                    tabRow.set(++rcol, row.get(icol));
                }

                sectionInstance.addRecord(tabRow);
            }

            tabFormatInstance.setFileId(this.fileId);

            if (sectionInstance.getRecords().size() > 0) {
                // Link the assay table to its assay group, needed for the file dispatcher
                //
                BIIObjectStore store = getStore();
                AssayGroup ag = store.getType(AssayGroup.class, fileId);
                if (ag == null) {
                    throw new TabInternalErrorException(i18n.msg("assay_format_exporter_missing_assay_group", fileId));
                }
                ag.setAssaySectionInstance(sectionInstance);

                // Finally add up to the being built result
                result.addFormatInstance(tabFormatInstance);
            }
        } // studies for
        return result;

    } // export()

}
