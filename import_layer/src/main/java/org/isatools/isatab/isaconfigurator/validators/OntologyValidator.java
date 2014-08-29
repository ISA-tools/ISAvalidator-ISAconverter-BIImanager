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
import org.isatools.isatab.configurator.schema.FieldType;
import org.isatools.isatab.configurator.schema.IsaTabConfigurationType;
import org.isatools.isatab.gui_invokers.GUIInvokerResult;
import org.isatools.isatab.isaconfigurator.ISAConfigurationSet;
import org.isatools.isatab.mapping.attributes.pipeline.BIIPropertyValueMappingHelper;
import org.isatools.tablib.schema.Field;
import org.isatools.tablib.schema.Record;
import org.isatools.tablib.schema.SectionInstance;
import org.isatools.tablib.utils.BIIObjectStore;

import java.util.List;
import java.util.Set;

/**
 * Validates aspects related to ontologies, such as the fact a field must come from a certain ontology or a given
 * ontology term. It uses {@link OntologyValidationManager} and connects to OLS and BioPortal.
 *
 * @author brandizi
 *         <b>date</b>: Nov 5, 2009
 */
public class OntologyValidator extends AbstractValidatorComponent {

    private OntologyValidationManager ovalidator = new OntologyValidationManager();

    public OntologyValidator(BIIObjectStore store, ISAConfigurationSet isaConfigSet, Set<String> messages) {
        super(store, isaConfigSet, messages);
    }

    @Override
    public GUIInvokerResult validate(SectionInstance table, IsaTabConfigurationType cfg) {
        boolean result = true;

        // TODO: unit
        List<Field> fields = table.getFields();
        int nfields = fields.size();
        for (Field field : fields) {
            String header = field.getAttr("header");
            FieldType cfield = isaConfigSet.getConfigurationField(cfg, header);

            if (cfield == null) {
                continue;
            }
            if (cfield.getRecommendedOntologies() == null) {
                continue;
            }

            int idx = field.getIndex(), ridx = idx + 1, rridx = idx + 2;
            Field rfield = ridx < nfields ? table.getField(ridx) : null,
                    rrfield = rridx < nfields ? table.getField(rridx) : null;
            String rheader = rfield == null ? null : rfield.getAttr("header"),
                    rrheader = rrfield == null ? null : rrfield.getAttr("header");

            if (
                    !(BIIPropertyValueMappingHelper.TERM_SOURCE_HEADER.equalsIgnoreCase(rheader)
                            && BIIPropertyValueMappingHelper.ACCESSION_HEADER.equalsIgnoreCase(rrheader))
                    ) {
                log.warn("The Field '" + header + "' should have values from ontologies and has no ontology headers instead");
                result = false;
                continue;
            }

            for (Record record : table.getRecords()) {
                result &= validateSingleField(record, idx, cfg, cfield);
            }
        }

        return result ? GUIInvokerResult.SUCCESS : GUIInvokerResult.WARNING;
    }

    private boolean validateSingleField(Record record, int col, IsaTabConfigurationType cfg, FieldType cfgField) {
        String value = StringUtils.trimToNull(record.getString(col)),
                source = StringUtils.trimToNull(record.getString(col + 1)),
                acc = StringUtils.trimToNull(record.getString(col + 2));

        boolean result = true;

        if (value == null || acc == null || source == null) {
            messages.add(
                    "Incomplete values for ontology headers, for the field '" + cfgField.getHeader() + "' in the file '"
                            + record.getParent().getFileId() + "'. Check that all the label/accession/source are provided."
            );
            return false;
        }

        if (!ovalidator.validateConfiguredOntologySource(source, cfgField)) {
            messages.add(
                    "Source '" + source + "' doesn't match the required source(s) for the field '" + cfgField.getHeader()
                            + "' in the file '" + record.getParent().getFileId() + "'"
            );
            result = false;
        }

        if (!result) {
            return false;
        }

        // Evaluate this only if the source is valid and the term exists, otherwise it doesn't make much sense
        //

        if (!ovalidator.validateBranchMatching(source, acc, cfgField)) {
            messages.add(
                    "Term '" + source + ":" + acc + "/" + value + "' is not among the valid terms for the field '"
                            + cfgField.getHeader() + "' in the file '" + record.getParent().getFileId() + "'"
            );
            return false;
        }

        return true;
    }
}
