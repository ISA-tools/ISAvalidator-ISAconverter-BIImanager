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

package org.isatools.isatab.isaconfigurator.validators;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.isatools.isatab.configurator.schema.FieldType;
import org.isatools.isatab.configurator.schema.IsaTabConfigurationType;
import org.isatools.isatab.configurator.schema.ProtocolFieldType;
import org.isatools.isatab.gui_invokers.GUIInvokerResult;
import org.isatools.isatab.isaconfigurator.ISAConfigurationSet;
import org.isatools.tablib.exceptions.TabMissingValueException;
import org.isatools.tablib.mapping.MappingUtils;
import org.isatools.tablib.schema.Field;
import org.isatools.tablib.schema.Record;
import org.isatools.tablib.schema.SectionInstance;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.model.Protocol;
import uk.ac.ebi.bioinvindex.model.Study;
import uk.ac.ebi.bioinvindex.model.term.ProtocolType;
import uk.ac.ebi.bioinvindex.utils.i18n;

import java.util.*;

/**
 * Validates the requirement that a given protocol type must appear between certain field types.
 *
 * @author brandizi
 *         <b>date</b>: Nov 5, 2009
 */
@SuppressWarnings("static-access")
public class ProtocolFieldsValidator extends AbstractValidatorComponent {
    public ProtocolFieldsValidator(BIIObjectStore store, ISAConfigurationSet isaConfigSet, Set<String> messages) {
        super(store, isaConfigSet, messages);
    }

    @Override
    public GUIInvokerResult validate(SectionInstance table, IsaTabConfigurationType cfg) {
        boolean result = true;

        List<Field> fields = table.getFields();
        int nfields = fields.size();
        int icol = 0;

        while (icol < nfields) {
            Field leftField;
            String leftHeader, ucLeftHeader;

            // First the node field
            do {
                leftField = fields.get(icol);
                leftHeader = leftField.getAttr("header");
                ucLeftHeader = leftHeader.toLowerCase();
            } while (
                    icol++ < nfields &&
                            !ucLeftHeader.endsWith(" name")
                            && !ucLeftHeader.endsWith(" data file")
                            && !ucLeftHeader.endsWith(" data matrix file")
                    );

            // All the protocols until the next node field
            List<Field> protos = new ArrayList<Field>();
            Field rightField = null;
            String lcRightHeader = null;
            boolean outFound = false;
            for (; icol < nfields; icol++) {
                rightField = fields.get(icol);
                lcRightHeader = rightField.getAttr("header").toLowerCase();
                if ("protocol ref".equals(lcRightHeader)) {
                    protos.add(rightField);
                } else if (
                        lcRightHeader.endsWith(" name")
                                && !lcRightHeader.endsWith(" data file")
                                && !lcRightHeader.endsWith(" data matrix file")) {
                    outFound = true;
                    break;
                }
            }

            // Consistent structure?
            int nprotos = protos.size();
            if (nprotos > 0 && !outFound) {
                // last col is the protocol, no good!
                log.warn("Protocol REF column without output in file '" + table.getParent().getFileId() + "'");
                return GUIInvokerResult.WARNING;
            }

            if (!outFound) {
                return result ? GUIInvokerResult.SUCCESS : GUIInvokerResult.WARNING;
            }

            // Do we have some configured protocol application between these two?
            FieldType cleftField = isaConfigSet.getConfigurationField(cfg, leftHeader),
                    crightField = isaConfigSet.getConfigurationField(cfg, lcRightHeader);
            if (cleftField == null || crightField == null) {
                continue;
            }

            List<ProtocolFieldType> cprotos = isaConfigSet.getProtocolsBetween(cleftField, crightField);

            if (cprotos == null || cprotos.size() == 0) {
                continue;
            }

            // If yes, check all the applications
            for (Record record : table.getRecords()) {
                result &= validateProtocolFields(record, leftField, rightField, protos, cprotos);
            }

        } // Loop on fields

        return result ? GUIInvokerResult.SUCCESS : GUIInvokerResult.WARNING;
    }


    private boolean validateProtocolFields(
            Record record, Field inField, Field outField, List<Field> protoAppFields, List<ProtocolFieldType> cfgProtos
    ) {
        boolean result = true;

        MappingUtils mappingUtils = new MappingUtils(store);
        Study study = mappingUtils.getStudyFromSection(record.getParent());
        String studyAcc = study.getAcc();

        Map<ProtocolFieldType, Integer> matchedCfProtos = new HashMap<ProtocolFieldType, Integer>();

        for (Field papp : protoAppFields) {
            String pappVal = StringUtils.trimToNull(record.getString(papp.getIndex()));
            if (pappVal != null) {
                // check this proto app is in the config
                //

                boolean isMatched = false;

                Protocol proto = store.getType(Protocol.class, studyAcc + "\\" + pappVal);
                if (proto == null) {
                    throw new TabMissingValueException(i18n.msg("ref_protocol_missing", pappVal, study.getAcc()));
                }

                ProtocolType ptype = proto.getType();
                String ptypeStr = ptype == null ? null : StringUtils.trimToNull(ptype.getName());
                if (ptypeStr != null) {
                    for (ProtocolFieldType cfproto : cfgProtos) {
                        String cfPtype = StringUtils.trimToNull(cfproto.getProtocolType());
                        if (cfPtype == null) {
                            continue;
                        }

                        if (StringUtils.equalsIgnoreCase(cfPtype, ptypeStr)) {
                            isMatched = true;
                            matchedCfProtos.put(cfproto, MapUtils.getInteger(matchedCfProtos, cfproto, 0) + 1);
                            // TODO: Do specific checkings
                        }
                    }
                }

                if (!isMatched && ptypeStr != null) {
                    messages.add(
                            "The used protocol type '" + ptypeStr + "' is not defined in the ISA-configuration as a protocol between '"
                                    + inField.getAttr("header") + "' and '" + outField.getAttr("header") + "', in the file '"
                                    + record.getParent().getFileId() + "'"
                    );
                    result = false;
                }
            }

            // Now check about the cfg protocol that are possibly not matched or matched too many times
            for (ProtocolFieldType cfproto : cfgProtos) {
                String cfPtype = StringUtils.trimToNull(cfproto.getProtocolType());
                if (cfPtype == null) {
                    continue;
                }

                int nmatches = MapUtils.getInteger(matchedCfProtos, cfgProtos, 0);
                if (nmatches == 0) {
                    if (cfproto.getIsRequired()) {
                        messages.add(
                                "The protocol type '" + cfPtype + "' is required between '"
                                        + inField.getAttr("header") + "' and '" + outField.getAttr("header") + "', in the file '"
                                        + record.getParent().getFileId() + "'"
                        );
                        result = false;
                    }
                } else if (nmatches > 1) {
                    messages.add(
                            "Strangely, the protocol '" + pappVal + "' is used more than once between '"
                                    + inField.getAttr("header") + "' and '" + outField.getAttr("header") + "', in the file '"
                                    + record.getParent().getFileId() + "'"
                    );
                    result = false;
                }
            }
        }

        return result;
    }

}
