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

package org.isatools.tablib.mapping.pipeline;

import org.apache.commons.lang.StringUtils;
import org.isatools.isatab.mapping.assay_common.attributes.AssayResultFVMappingHelper;
import org.isatools.tablib.mapping.MappingUtils;
import org.isatools.tablib.schema.SectionInstance;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.model.Data;
import uk.ac.ebi.bioinvindex.model.Study;
import uk.ac.ebi.bioinvindex.utils.datasourceload.DataLocationManager;

/**
 * All mappers that produce data objects extends this.
 * <p/>
 * <dl><dt>date:</dt><dd>Dec 2, 2008</dd></dl>
 *
 * @author brandizi
 */
public abstract class DataNodeTabMapper extends ProcessingNodeTabMapper<Data> {
    private final MappingUtils mappingUtils;
    private Boolean isLastMapper = null;

    protected DataNodeTabMapper(BIIObjectStore store, SectionInstance sectionInstance, int fieldIndex, int endField) {
        super(store, sectionInstance, fieldIndex, endField);
        mappingUtils = new MappingUtils(store);
    }


    /**
     * Setup the accession for the data object.
     *
     * @see org.isatools.tablib.mapping.ClassTabMapper#map(int)
     */
    @Override
    public Data map(int recordIndex) {
        Data data = super.map(recordIndex);
        data.setName(getName(recordIndex, data));

        SectionInstance sectionInstance = getSectionInstance();
        Study study = mappingUtils.getStudyFromSection(sectionInstance);

        String dataAcc = "";
        if (study == null) {
            dataAcc += "bii:";
        } else {
            String studyAcc = StringUtils.trimToNull(study.getAcc());
            if (studyAcc != null) {
                dataAcc += studyAcc + ":";
            }
        }

        // Setup data accession, which is always scoped within the assay file
        String typeAcc = data.getType().getAcc().substring(4);
        dataAcc += typeAcc + ":" + DataLocationManager.filePath2Id(sectionInstance.getFileId()) + ".";
        String dataName = data.getName().trim();
        dataAcc += dataName.length() == 0 ? "null." + recordIndex + "." + getFieldIndex() : dataName;

        data.setAcc(dataAcc);

        if (log.isTraceEnabled()) {
            log.trace("New mapped Data object (might be incomplete): " + data);
        }

        // Forces the possibility to set it as last only before mapping starts.
        if (isLastMapper == null) {
            isLastMapper = false;
        }

        if (isLastMapper)
        // So, if you're the last one, create an assayResult and attach it to this data object
        {
            createAssayResult(data);
        }

        return data;
    }

    /**
     * Setup this mapper as the last mapper.
     */
    public void setAsLastMapper() {
        if (isLastMapper != null && !isLastMapper) {
            throw new InternalError(
                    "Internal error, a data node mapper can be set as the last mapper in the pipeline only before the mapping starts"
            );
        }

        isLastMapper = true;

        // As last node, you have the responsibility to harvest the factor values
        this.mappingHelpersConfig.put("Factor Value",
                new MappingHelperConfig<AssayResultFVMappingHelper>(
                        AssayResultFVMappingHelper.class, new String[][]{{"lookAllHeaders", "true"}})
        );
    }
}
