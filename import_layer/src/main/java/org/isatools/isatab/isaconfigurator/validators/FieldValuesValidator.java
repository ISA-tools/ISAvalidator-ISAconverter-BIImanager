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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.isatools.isatab.configurator.schema.FieldType;
import org.isatools.isatab.configurator.schema.IsaTabConfigurationType;
import org.isatools.isatab.gui_invokers.GUIInvokerResult;
import org.isatools.isatab.isaconfigurator.ISAConfigurationSet;
import org.isatools.tablib.mapping.properties.DatePropertyMappingHelper;
import org.isatools.tablib.schema.Record;
import org.isatools.tablib.schema.SectionInstance;
import org.isatools.tablib.utils.BIIObjectStore;

import java.text.ParseException;
import java.util.Set;

/**
 * Validates the single values in a spreadsheet, for things such as the requirement of being from a given data type.
 * <p/>
 * TODO: numerical range
 *
 * @author brandizi
 *         <b>date</b>: Oct 21, 2009
 */
@SuppressWarnings("static-access")
public class FieldValuesValidator extends AbstractValidatorComponent {
    public FieldValuesValidator(BIIObjectStore store, ISAConfigurationSet isaConfigSet, Set<String> messages) {
        super(store, isaConfigSet, messages);
    }

    @Override
    public GUIInvokerResult validate(SectionInstance table, IsaTabConfigurationType cfg) {
        boolean result = true;
        for (Record record : table.getRecords()) {
            int ncols = record.size();
            for (int icol = 0; icol < ncols; icol++) {
                FieldType cfield = isaConfigSet.getConfigurationField(
                        cfg, table.getField(icol).getAttr("header")
                );
                if (cfield == null) {
                    continue;
                }
                result &= validateSingleField(record, icol, cfg, cfield);
            }
        }
        return result ? GUIInvokerResult.SUCCESS : GUIInvokerResult.WARNING;
    }


    /**
     * Does the job for a single field.
     */
    private boolean validateSingleField(Record record, int icol, IsaTabConfigurationType cfg, FieldType cfgField) {
        String dataType = StringUtils.trimToNull(cfgField.getDataType());
        if (dataType == null || "string".equalsIgnoreCase(dataType)) {
            return true;
        }

        String value = StringUtils.trimToNull(record.getString(icol));
        if (value == null) {
            if (cfgField.getIsRequired()) {
                messages.add(
                        "Missing value for the required field '" + cfgField.getHeader() + "' in the file '"
                                + record.getParent().getFileId() + "'"
                );
            }
            return true;
        }

        boolean isValidValue = true;

        if ("boolean".equalsIgnoreCase(dataType)) {
            isValidValue = "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
        } else if ("date".equalsIgnoreCase(dataType)) {
            try {
                DateUtils.parseDate(value, DatePropertyMappingHelper.VALID_FORMATS);
            }
            catch (ParseException e) {
                isValidValue = false;
            }
        } else if ("integer".equalsIgnoreCase(dataType)) {
            try {
                Integer.parseInt(value);
            }
            catch (NumberFormatException e) {
                isValidValue = false;
            }
        } else if ("double".equalsIgnoreCase(dataType)) {
            try {
                Double.parseDouble(value);
            }
            catch (NumberFormatException e) {
                isValidValue = false;
            }
        } else if ("ontology-term".equalsIgnoreCase(dataType) || "Ontology term".equalsIgnoreCase(dataType)) {
            return true;
        } else {
            // TODO: list, OEs
            messages.add("Unknown data type '" + dataType + "' for field '" + cfgField.getHeader() + "' in the file '"
                    + record.getParent().getFileId() + "'");
            // TODO: false?
            return false;
        }

        if (!isValidValue) {
            String header = cfgField.getHeader();
            log.debug(
                    "Invalid value '" + value + "' for type '" + dataType + "' of the field '" + header + "'"
            );
            messages.add("Invalid values found in the field '" + header + "' in the file '"
                    + record.getParent().getFileId()
            );
        }

        return isValidValue;
    }

}
