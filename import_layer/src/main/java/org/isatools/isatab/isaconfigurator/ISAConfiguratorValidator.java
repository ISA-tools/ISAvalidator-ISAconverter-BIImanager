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

package org.isatools.isatab.isaconfigurator;


import org.apache.log4j.Logger;
import org.isatools.isatab.ISATABValidator;
import org.isatools.isatab.configurator.schema.IsaTabConfigurationType;
import org.isatools.isatab.gui_invokers.GUIInvokerResult;
import org.isatools.isatab.isaconfigurator.validators.*;
import org.isatools.isatab.mapping.AssayGroup;
import org.isatools.isatab_v1.mapping.ISATABReducedMapper;
import org.isatools.tablib.exceptions.TabValidationException;
import org.isatools.tablib.schema.SectionInstance;
import org.isatools.tablib.utils.BIIObjectStore;
import org.isatools.tablib.utils.logging.TabNDC;
import uk.ac.ebi.bioinvindex.utils.i18n;

import java.util.HashSet;
import java.util.Set;

/**
 * The ISAConfigurator Validator.
 * <p/>
 * This is the component to be used for validate an ISATAB submission by using the rules that
 * come from a configuration defined by means of the ISAConfigurator tool. Usually you don't need
 * to invoke this component directly, cause it is already used by the {@link ISATABValidator}.
 *
 * @author brandizi
 *         <b>date</b>: Oct 9, 2009
 */
public class ISAConfiguratorValidator {
    private final BIIObjectStore store;
    private final ISAConfigurationSet isaConfigSet;

    private Set<String> messages = new HashSet<String>();

    protected static final Logger log = Logger.getLogger(ISAConfiguratorValidator.class);
    protected static final TabNDC ndc = TabNDC.getInstance();

    /**
     * If you only want to validate the tabular view, without the full validation, you just need to use the
     * {@link ISATABReducedMapper}.
     */
    public ISAConfiguratorValidator(BIIObjectStore store) {
        isaConfigSet = new ISAConfigurationSet();
        this.store = store;
    }

    /**
     * Do all here.
     */
    public GUIInvokerResult validate() {
        GUIInvokerResult result;
        messages.clear();

        if (isaConfigSet.isEmpty()) {
            return GUIInvokerResult.SUCCESS;
        }

        result = validateAllTables();

        for (String msg : messages) {
            log.warn(msg);
        }

        return result;
    }


    /**
     * Goes through all the ISATAB tables (sample files and assay files) and uses
     * the validators in the validators package.
     */
    private GUIInvokerResult validateAllTables() {
        GUIInvokerResult result = GUIInvokerResult.SUCCESS;

        AbstractValidatorComponent[] validators = new AbstractValidatorComponent[]{
                new RequiredFieldsValidator(store, isaConfigSet, messages),
                new FieldValuesValidator(store, isaConfigSet, messages),
                new OntologyValidator(store, isaConfigSet, messages),
                new UnitFieldsValidator(store, isaConfigSet, messages),
                new ProtocolFieldsValidator(store, isaConfigSet, messages),
                new FactorValuePresenceValidator(store, isaConfigSet, messages)
        };

        Set<SectionInstance> processedSampleSections = new HashSet<SectionInstance>();
        for (AssayGroup ag : store.valuesOfType(AssayGroup.class)) {
            ndc.pushObject(ag.getStudy());

            // The Sample file
            SectionInstance sampleSection = ag.getSampleSectionInstance();
            if (!processedSampleSections.contains(sampleSection)) {
                IsaTabConfigurationType cfg = isaConfigSet.getConfig("[sample]", "");
                if (cfg == null) {
                    messages.add(
                            "No ISA Configuration defined for the sample file, the ISA Configurator validation is not done for the file '"
                                    + sampleSection.getFileId() + "'"
                    );
                } else {
                    ndc.pushFormat(sampleSection.getParent());
                    for (AbstractValidatorComponent validator : validators) {
                        GUIInvokerResult valResult = validator.validate(sampleSection, cfg);
                        if (GUIInvokerResult.ERROR == valResult) {
                            throw new TabValidationException(i18n.msg("isacfg_validation_failed"));
                        } else if (valResult == GUIInvokerResult.WARNING) {
                            result = GUIInvokerResult.WARNING;
                        }
                    }

                    SampleNameValidator sampleNameLinkValidator = new SampleNameValidator();
                    GUIInvokerResult sampleNameLinkResult = sampleNameLinkValidator.validate(sampleSection, ag);
                    if (sampleNameLinkResult == GUIInvokerResult.ERROR) {
                        throw new TabValidationException(i18n.msg("sample_link_check_failed"));
                    }

                    ndc.popTabDescriptor();
                }

                processedSampleSections.add(sampleSection);
            }

            // The Assay file
            SectionInstance assaySection = ag.getAssaySectionInstance();
            if (assaySection != null) {
                IsaTabConfigurationType cfg = isaConfigSet.getConfig(ag);
                if (cfg == null) {
                    messages.add(
                            "No ISA Configuration defined for the type "
                                    + ag.getMeasurement().getName() + " / " + ag.getTechnologyName()
                                    + ", the ISA Configurator validation is not done for the file '" + assaySection.getFileId() + "'"
                    );
                } else {
                    ndc.pushFormat(assaySection.getParent());
                    for (AbstractValidatorComponent validator : validators) {
                        GUIInvokerResult valResult = validator.validate(assaySection, cfg);

                        if (GUIInvokerResult.ERROR == valResult) {
                            // TRY with the generic assay, if it exists.
                            IsaTabConfigurationType genericConfig = isaConfigSet.getConfig("*", "*");
                            if (genericConfig != null && genericConfig != cfg) {
                                log.info("**** Trying validation with the generic assay type.");
                                valResult = validator.validate(assaySection, genericConfig);
                                if (valResult == GUIInvokerResult.ERROR) {
                                    throw new TabValidationException(i18n.msg("isacfg_validation_failed"));
                                }
                                log.info("**** Validation successful with the generic assay type.");
                            } else {
                                log.info("**** No generic assay type available to validate against.");
                                throw new TabValidationException(i18n.msg("isacfg_validation_failed"));
                            }
                        } else if (valResult == GUIInvokerResult.WARNING) {
                            result = GUIInvokerResult.WARNING;
                        }
                    }
                    ndc.popTabDescriptor();
                }
            }
            ndc.popObject(); // study
        }
        return result;
    }

}
