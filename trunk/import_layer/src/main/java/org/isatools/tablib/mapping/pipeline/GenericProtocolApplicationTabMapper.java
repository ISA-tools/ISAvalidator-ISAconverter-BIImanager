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
import org.isatools.tablib.exceptions.TabMissingValueException;
import org.isatools.tablib.mapping.MappingUtils;
import org.isatools.tablib.mapping.TabMappingContext;
import org.isatools.tablib.mapping.properties.PropertyMappingHelper;
import org.isatools.tablib.schema.Field;
import org.isatools.tablib.schema.SectionInstance;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.model.Protocol;
import uk.ac.ebi.bioinvindex.model.Study;
import uk.ac.ebi.bioinvindex.model.processing.ProtocolApplication;
import uk.ac.ebi.bioinvindex.model.term.Parameter;
import uk.ac.ebi.bioinvindex.model.term.ParameterValue;
import uk.ac.ebi.bioinvindex.utils.i18n;

/**
 * Maps protocol applications in the assay files
 * <p/>
 * Dec 19, 2007
 *
 * @author brandizi
 */
public class GenericProtocolApplicationTabMapper extends AbstractProtocolApplicationTabMapper<ProtocolApplication> {
    private final MappingUtils mappingUtils;

    public GenericProtocolApplicationTabMapper(BIIObjectStore store, SectionInstance sectionInstance, int fieldIndex, int endField) {
        super(store, sectionInstance, fieldIndex, endField);
        mappingUtils = new MappingUtils(store);
    }


    @SuppressWarnings("unchecked")
    public ProtocolApplication map(int recordIndex) {
        BIIObjectStore store = getStore();
        SectionInstance sectionInstance = getSectionInstance();
        int fieldIndex = getFieldIndex();
        Study study = mappingUtils.getStudyFromSection(sectionInstance);
        if (study == null) {
            throw new InternalError("No study found associated to the format/file: "
                    + sectionInstance.getParent().getFormat().getId() + "/" + sectionInstance.getParent().getFileId()
            );
        }
        String studyAcc = study.getAcc();

        // link the protocol
        //
        ProtocolApplication protocolApp = null;
        Field protoRefField = sectionInstance.getField(fieldIndex);
        String protoRefValue = null;
        Protocol protocol = null;
        if (protoRefField != null) {
            protoRefValue = StringUtils.trimToNull(sectionInstance.getString(recordIndex, fieldIndex));

            // It might happen that there is the protocol ref column but it has empty values.
            if (protoRefValue != null) {
                protocol = (Protocol) store.get(Protocol.class, studyAcc + "\\" + protoRefValue);
                if (protocol == null) {
                    throw new TabMissingValueException(i18n.msg("ref_protocol_missing", protoRefValue, study.getAcc()));
                }

                // Now add the Protocol Application
                protocolApp = new ProtocolApplication(protocol);
                protocolApp.setOrder(fieldIndex);
            } else {
                return null;
            }
        } else {
            if (recordIndex == 0) {
                log.warn("No protocol reference in the sample/assay file");
            }
            return null;
        }

        // Additional mappings provided by the property helpers
        for (PropertyMappingHelper pmap : getPropertyHelpers().values()) {
            pmap.map(protocolApp, recordIndex);
        }

        if (protoRefValue != null) {
            TabMappingContext context = store.valueOfType(TabMappingContext.class);
            String usedPref = "used.protocol." + studyAcc + "\\" + protoRefValue;
            context.put(usedPref, protocol);
            for (ParameterValue pval : protocolApp.getParameterValues()) {
                Parameter param = pval.getType();
                context.put(usedPref + "\\" + param.getValue(), param);
            }
        }

        // Add the file it comes from and its type (sample/assay)
        addSourceFileAnnotation(protocolApp);

        log.trace("New mapped Protocol Application: " + protocolApp.hashCode() + "/" + protocolApp);
        return protocolApp;
    }


    /**
     * Doesn't return anything, not used here
     */
    @Override
    public ProtocolApplication newMappedObject() {
        return null;
    }

}