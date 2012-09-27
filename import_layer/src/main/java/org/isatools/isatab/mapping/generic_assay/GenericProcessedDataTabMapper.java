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

package org.isatools.isatab.mapping.generic_assay;

import org.apache.commons.lang.StringUtils;
import org.isatools.isatab.mapping.attributes.AnnotationMappingHelper;
import org.isatools.tablib.mapping.pipeline.DataNodeTabMapper;
import org.isatools.tablib.schema.SectionInstance;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.model.Data;
import uk.ac.ebi.bioinvindex.model.term.DataType;
import uk.ac.ebi.bioinvindex.model.xref.ReferenceSource;

/**
 * Maps generic raw data nodes.
 * <p/>
 * TODO: The raw data file is actually an attribute of the assay, so we should be able to associate a data node too to
 * the assay, then make the run become a data-node, not material node.
 * <p/>
 * TODO: For generic assay case, I am assuming that raw data is always specified and that no factors occur downstream
 * raw data. We must fix this.
 * <p/>
 * Feb 2008
 *
 * @author brandizi
 */
public class GenericProcessedDataTabMapper extends DataNodeTabMapper {

    @SuppressWarnings("unchecked")
    public GenericProcessedDataTabMapper(BIIObjectStore store, SectionInstance sectionInstance, int fieldIndex, int endField) {
        super(store, sectionInstance, fieldIndex, endField);

        // First column is "Derived Data File"
    }


    @Override
    public Data map(int recordIndex) {
        Data data = super.map(recordIndex);
        SectionInstance sectionInstance = getSectionInstance();
        String dataFileName = StringUtils.trimToEmpty(sectionInstance.getString(recordIndex, getFieldIndex()));
        data.setUrl(dataFileName);

        // TODO Remove
        // createAssayResult ( data );

        return data;
    }


    /**
     * Provides a new data object
     */
    @Override
    public Data newMappedObject() throws InstantiationException, IllegalAccessException {
        // TODO: we need proper constants for the roles
        // TODO: fix to use real ReferenceSource
        //
        return new Data(
                "", new DataType("bii:generic_assay_derived_data", "Generic Derived Data", new ReferenceSource("bii:data_types", "bii:data_types"))
        );
    }


    /**
     * Links assay name and data file name
     */
    @Override
    protected String getName(int recordIndex, Data mappedObject) {
        SectionInstance sectionInstance = getSectionInstance();
        String dataFileName = StringUtils.trimToEmpty(sectionInstance.getString(recordIndex, getFieldIndex()));
        String assayName = StringUtils.trimToEmpty(sectionInstance.getString(recordIndex, "Assay Name"));
        return dataFileName + " (" + assayName + ")";
    }


}