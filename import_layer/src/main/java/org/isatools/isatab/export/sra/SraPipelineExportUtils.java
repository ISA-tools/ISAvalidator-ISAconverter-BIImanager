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

package org.isatools.isatab.export.sra;


import org.apache.commons.lang.StringUtils;
import org.isatools.tablib.exceptions.TabInvalidValueException;
import org.isatools.tablib.exceptions.TabMissingValueException;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.model.AssayResult;
import uk.ac.ebi.bioinvindex.model.Data;
import uk.ac.ebi.bioinvindex.model.Protocol;
import uk.ac.ebi.bioinvindex.model.processing.Assay;
import uk.ac.ebi.bioinvindex.model.processing.DataNode;
import uk.ac.ebi.bioinvindex.model.processing.ProtocolApplication;
import uk.ac.ebi.bioinvindex.model.term.*;
import uk.ac.ebi.bioinvindex.utils.i18n;
import uk.ac.ebi.bioinvindex.utils.processing.ProcessingUtils;
import uk.ac.ebi.embl.era.sra.xml.AttributeType;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * SRA-exporter, functions related to the experimental pipeline. See {@link SraExportComponent} for further information on how the SRA exporter
 * classes are arranged.
 *
 * @author brandizi
 *         <b>date</b>: Jul 20, 2009
 */
abstract class SraPipelineExportUtils extends SraExportUtils {

    protected SraPipelineExportUtils(BIIObjectStore store, String sourcePath, String exportPath) {
        super(store, sourcePath, exportPath);
    }


    /**
     * Get a particular protocol type that has been used to produce the parameter assay.
     * <p/>
     * TODO: move to Model?
     *
     * @param typeStr: a string that is contained in the protocol type (free text part). Matching is case-insensitive.
     */
    protected ProtocolApplication getProtocol(final Assay assay, final String typeStr) {
        Collection<AssayResult> ars = ProcessingUtils.findAssayResultsFromAssay(assay);
        if (ars.size() == 0) {
            return null;
        }

        // TODO: gets the first assay and the first protocol only. We should add a check/message on that

        AssayResult ar = ars.iterator().next();
        Data data = ar.getData();
        DataNode dataNode = data.getProcessingNode();

        // Take the sequencing protocol
        Collection<ProtocolApplication> protoApps = ProcessingUtils.findBackwardProtocolApplications(
                dataNode, typeStr, null, false
        );


        if (protoApps.size() == 0) {
            String msg = MessageFormat.format(
                    "The assay file of type ''{0}''/''{1}'' for study ''{2}'' has no ''{3}'' protocol, ignoring the assay",
                    assay.getMeasurement().getName(),
                    assay.getTechnologyName(),
                    assay.getStudy().getAcc(),
                    typeStr
            );
            nonRepeatedMessages.add(msg);
            return null;
        }

        // Get the exact application where there is the protocol
        for (ProtocolApplication papp : protoApps) {
            Protocol proto = papp.getProtocol();
            if (proto == null) {
                continue;
            }
            ProtocolType ptype = proto.getType();
            if (ptype == null) {
                continue;
            }

            return papp;
        }
        return null;
    }

    /**
     * Calls {@link #getParameterValues(Assay, ProtocolApplication, String, boolean)} and returns the first result from
     * it, so it's undefined in case there is more than one value.
     */
    protected String getParameterValue(
            final Assay assay, final ProtocolApplication papp, final String paramType, final boolean isMandatory) {
        String pvalues[] = getParameterValues(assay, papp, paramType, isMandatory);
        return pvalues == null || pvalues.length == 0 ? null : pvalues[0];
    }

    /**
     * Gets the parameter values of a particular parameter type that have been used for a particular protocol application.
     * <p/>
     * TODO: to be moved in the Model?
     *
     * @param assay       used for error reporting
     * @param papp        the protocol application you're interested in
     * @param paramType   part of the parameter type (the free text part of it), matching is case-insensitive
     * @param isMandatory id true, an exception is raised when the value corresponding to this type is missing. Otherwise it
     *                    just reports a warning.
     * @return all the values found fo this parameter.
     */
    protected String[] getParameterValues(final Assay assay, final ProtocolApplication papp, final String paramType, final boolean isMandatory) {
        Collection<ParameterValue> pvalues = papp.getParameterValuesByType(paramType);
        if (pvalues == null || pvalues.size() == 0) {
            if (isMandatory) {
                throw new TabMissingValueException(
                        i18n.msg(
                                "sra_missing_param", assay.getMeasurement().getName(), assay.getTechnologyName(),
                                assay.getStudy().getAcc(), paramType
                        )
                );
            } else {
                return null;
            }
        }
        String[] result = new String[pvalues.size()];
        int i = 0;
        for (ParameterValue pvalue : pvalues) {
            result[i++] = pvalue.getValue();
        }
        return result;
    }

    protected String getParameterValue(
            final Assay assay, final ProtocolApplication papp, final String[] paramTypeArray, final boolean isMandatory) {
        String[] pvalues = null;
        for(int i=0; i<paramTypeArray.length; i++){
            pvalues = getParameterValues(assay, papp, paramTypeArray[i], false);
            if (pvalues!=null && pvalues.length!=0) return pvalues[0];
        }
        if (isMandatory){
            throw new TabMissingValueException(
                    i18n.msg(
                            "sra_missing_param", assay.getMeasurement().getName(), assay.getTechnologyName(),
                            assay.getStudy().getAcc(), paramTypeArray[0]
                    )
            );
        }

        return null;
    }

    /**
     * Works like {@link #getParameterValue (Assay, ProtocolApplication, String, boolean)} but assumes the resulting
     * parameter value is actually composed of multiple values, separated by the semicolon (";") character.
     */
    protected Map<String, String> getMultiValueParameter(final Assay assay, final String paramType, final String paramValue) {
        Map<String, String> result = new HashMap<String, String>();
        if (paramValue == null || paramValue.length() == 0) {
            return result;
        }
        String chunks[] = paramValue.split("\\;");
        if (chunks == null || chunks.length == 0) {
            throw new TabInvalidValueException(i18n.msg(
                    "sra_invalid_param",
                    assay.getMeasurement().getName(),
                    assay.getTechnologyName(),
                    assay.getStudy().getAcc(),
                    paramType,
                    paramValue
            ));
        }
        for (String chunk : chunks) {
            String entry[] = null;
            if (chunk != null && chunk.length() != 0) {
                entry = chunk.split("\\=");
            }
            if (entry == null || entry.length != 2) {
                throw new TabInvalidValueException(i18n.msg(
                        "sra_invalid_param",
                        assay.getMeasurement().getName(),
                        assay.getTechnologyName(),
                        assay.getStudy().getAcc(),
                        paramType,
                        paramValue
                ));
            }
            String key = StringUtils.trimToNull(entry[0]), val = StringUtils.trimToNull(entry[1]);
            if (key == null || val == null) {
                throw new TabInvalidValueException(i18n.msg(
                        "sra_invalid_param",
                        assay.getMeasurement().getName(),
                        assay.getTechnologyName(),
                        assay.getStudy().getAcc(),
                        paramType,
                        paramValue
                ));
            }
            result.put(key, val);
        }
        return result;
    }


    /**
     * Transforms a BII Characteristic value into a SRA/SAMPLEATTRIBUTE annotation, i.e. something like
     * TAG:value/unit
     */
    protected AttributeType characteristicValue2SampleAttr(CharacteristicValue cvalue) {
        String cvalueStr = cvalue.getValue();
        if (cvalueStr == null) {
            return null;
        }
        // TODO ontology term
        Characteristic ctype = cvalue.getType();
        String ctypeStr = StringUtils.trimToNull(ctype.getValue());
        UnitValue uval = cvalue.getUnit();
        String uvalStr = uval == null ? null : StringUtils.trimToNull(uval.getValue());
        AttributeType xattr = buildSampleAttribute(ctypeStr, cvalueStr, uvalStr);
        return xattr;
    }

    /**
     * A facility to build a SAMPLE_ATTRIBUTE from a tag/value/unit triple. Returns null if either tag or value are null.
     */

    // TODO: add a lookup to rely on insdc codes instead of migs/miens labels for SRA export
    // use
    protected AttributeType buildSampleAttribute(String tag, String value, String unit) {
        if (tag == null || value == null) {
            return null;
        }

        AttributeType xattr = AttributeType.Factory.newInstance();
        xattr.setTAG(tag);
        xattr.setVALUE(value);
        if (unit != null) {
            xattr.setUNITS(unit);
        }
        return xattr;
    }

}
