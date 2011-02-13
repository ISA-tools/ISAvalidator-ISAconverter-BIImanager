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

package org.isatools.isatab.mapping.attributes.pipeline;


import org.isatools.tablib.exceptions.TabMissingValueException;
import org.isatools.tablib.schema.SectionInstance;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.model.Protocol;
import uk.ac.ebi.bioinvindex.model.processing.ProtocolApplication;
import uk.ac.ebi.bioinvindex.model.term.Parameter;
import uk.ac.ebi.bioinvindex.model.term.ParameterValue;
import uk.ac.ebi.bioinvindex.utils.i18n;

import java.util.Map;


/**
 * Maps the field Parameter[X] in a SDRF-like section.
 * <p/>
 * Jan 15, 2008
 *
 * @author brandizi
 */
public class ParameterValueMappingHelper
        extends BIIPropertyValueMappingHelper<ProtocolApplication, Parameter, ParameterValue> {
    /**
     * If true, when a Parameter Value refers to a parameter type that wasn't defined in the Investigation file,
     * a warning is issued. You may not want it, e.g.: when the "Performer" field is automatically mapped to a
     * parameter value.
     * <p/>
     * This option must be propagated in the delegates (
     * {@link FreeTextParameterValueMappingHelper} and {@link QtyParamMappingHelper}), as well as in the method
     * {@link ParameterValueMappingHelper#fixProtocolParameter(ProtocolApplication, ParameterValue, boolean)}.
     */
    protected boolean warnAutoCreation = true;


    public ParameterValueMappingHelper(BIIObjectStore store, SectionInstance sectionInstance,
                                       Map<String, String> options, int fieldIndex) {
        this(store, sectionInstance, fieldIndex);
    }

    public ParameterValueMappingHelper(
            BIIObjectStore store, SectionInstance sectionInstance, int fieldIndex) {
        super(store, sectionInstance, "Parameter Value", null, fieldIndex);
    }


    @Override
    protected FreeTextParameterValueMappingHelper newOntologyTermDelegate() {
        FreeTextParameterValueMappingHelper otDelegate = new FreeTextParameterValueMappingHelper(
                getStore(), getSectionInstance(), getFieldIndex(), getType()
        );
        otDelegate.warnAutoCreation = warnAutoCreation;
        return otDelegate;
    }

    @Override
    protected QtyParamMappingHelper newQuantityDelegate() {
        QtyParamMappingHelper qtyDelegate = new QtyParamMappingHelper(
                getStore(), getSectionInstance(), getFieldIndex(), getType()
        );
        qtyDelegate.warnAutoCreation = warnAutoCreation;
        return qtyDelegate;
    }


    /**
     * Setup the mapped parameter value for the mapped protocol application
     */
    @Override
    public void setProperty(ProtocolApplication mappedObject, ParameterValue paramValue) {
        if (paramValue == null) {
            return;
        }

        fixProtocolParameter(mappedObject, paramValue, warnAutoCreation);

        mappedObject.addParameterValue(paramValue);
        log.trace("Parameter " + paramValue.getType().getValue() + ": '" + paramValue + "' mapped to protocol " + mappedObject.hashCode());
    }


    /**
     * Fix the property's type that paramValue belongs to, by finding it in the protocol and replacing the type initially
     * attached to paramValue. Used inside setProperty().
     *
     * @param protoApp         the protocol application paramValue belongs to
     * @param paramValue       the value to check and of which property type to adjust.
     * @param warnAutoCreation the same meaning of {@link ParameterValueMappingHelper#warnAutoCreation}
     */
    protected static void fixProtocolParameter(ProtocolApplication protoApp, ParameterValue paramValue, boolean warnAutoCreation) {
        Parameter pvType = paramValue.getType();
        String pvTypeName = pvType.getValue();
        Protocol proto = protoApp.getProtocol();
        if (proto == null) {
            throw new TabMissingValueException(i18n.msg("missing_protocol_ref", protoApp.toString()));
        }

        Parameter protoParam = proto.findParameterByName(pvTypeName);
        if (protoParam == null) {
            if (warnAutoCreation) {
                log.warn(
                        "Parameter referred in protocol application '" + pvTypeName + "' not found in the protocol '"
                                + proto.getName() + "', creating it automatically"
                );
            }
            proto.addParameter(pvType);
        } else {
            // TODO: until we fix hashCode/equals I have to do that.
            proto.removeParameter(protoParam);
            protoParam.setOrder(pvType.getOrder());
            protoParam.addPropertyValue(paramValue);
            proto.addParameter(protoParam);
        }
    }

}