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

package org.isatools.isatab_v1.mapping;

import org.apache.commons.lang.StringUtils;
import org.isatools.isatab.mapping.AssayGroup;
import org.isatools.isatab.mapping.AssayTypeEntries;
import org.isatools.isatab.mapping.StudyComponentTabMapper;
import org.isatools.isatab.mapping.attributes.CommentMappingHelper;
import org.isatools.tablib.exceptions.TabInternalErrorException;
import org.isatools.tablib.exceptions.TabInvalidValueException;
import org.isatools.tablib.mapping.ClassTabMapper;
import org.isatools.tablib.mapping.MappingUtils;
import org.isatools.tablib.mapping.properties.StringPropertyMappingHelper;
import org.isatools.tablib.schema.SectionInstance;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.model.Study;
import uk.ac.ebi.bioinvindex.model.term.AssayTechnology;
import uk.ac.ebi.bioinvindex.model.term.Measurement;
import uk.ac.ebi.bioinvindex.utils.i18n;

import java.util.List;

/**
 * Maps contacts in the Investigation File
 * <p/>
 * Dec 2007
 *
 * @author brandizi
 */
public class AssayGroupTabMapper extends ClassTabMapper<AssayGroup> implements StudyComponentTabMapper {
    private MappingUtils mappingUtils;
    private Study mappedStudy;


    @SuppressWarnings("unchecked")
    public AssayGroupTabMapper(BIIObjectStore store, SectionInstance sectionInstance) {
        super(store, sectionInstance);

        mappingHelpersConfig.put("Study Assay File Name",
                new MappingHelperConfig<StringPropertyMappingHelper>(
                        StringPropertyMappingHelper.class, new String[][]{{"propertyName", "filePath"}}
                )
        );
        mappingHelpersConfig.put("Study Assay Technology Platform",
                new MappingHelperConfig<StringPropertyMappingHelper>(
                        StringPropertyMappingHelper.class, new String[][]{{"propertyName", "platform"}}
                )
        );
        this.mappingHelpersConfig.put("Comment", new MappingHelperConfig<CommentMappingHelper>(
                CommentMappingHelper.class
        ));

        mappingUtils = new MappingUtils(store);
    }

    public AssayGroup map(int recordIndex) {
        SectionInstance sectionInstance = getSectionInstance();

        // Basic properties
        AssayGroup rval = super.map(recordIndex);

        // Measurment Type
        Measurement measurement;
        {
            String epLabel = sectionInstance.getString(recordIndex, "Study Assay Measurement Type");
            String epAcc = sectionInstance.getString(recordIndex, "Study Assay Measurement Type Term Accession Number");
            if (epAcc == null) {
                // If we don't have an accession, try to get the term from predefined entries.
                measurement = AssayTypeEntries.getMeasurementTypeFromLabel(epLabel);

                if (measurement == null ) {
                     throw new TabInvalidValueException(i18n.msg("isatab_unknown_measurement_type", epLabel));
                }
            } else {
                measurement = mappingUtils.createOntologyEntry(
                        sectionInstance, recordIndex,
                        "Study Assay Measurement Type Term Accession Number",
                        "Study Assay Measurement Type",
                        "Study Assay Measurement Type Term Source REF",
                        Measurement.class
                );
                if (measurement == null) {
                    throw new TabInternalErrorException(i18n.msg("missing_measurement", rval.getFilePath()));
                }
            }
        }

        String measurementAcc = StringUtils.trimToNull(measurement.getAcc());
        if (measurementAcc == null) {
            String measurementName = StringUtils.trimToNull(measurement.getName());
            if (measurementName == null) {
                log.warn(
                        "The assay file " + rval.getFilePath()
                                + " has no measurement defined, this submission cannot be loaded in the BII database");
            } else {
                measurement.setAcc(measurementName);
                rval.setMeasurement(measurement);
            }
        } else {
            rval.setMeasurement(measurement);
        }

        // Technology Type
        AssayTechnology technology;
        {
            String techLabel = sectionInstance.getString(recordIndex, "Study Assay Technology Type");
            String techAcc = sectionInstance.getString(recordIndex, "Study Assay Technology Type Term Accession Number");
            if (techAcc == null) {
                // If we don't have an accession, try to get the term from predefined entries.
                technology = AssayTypeEntries.getAssayTechnologyFromLabel(techLabel);
                if (technology == null) {
                    throw new TabInvalidValueException(i18n.msg("isatab_unknown_assay_type", techLabel));
                }
            } else {
                technology = mappingUtils.createOntologyEntry(
                        sectionInstance, recordIndex,
                        "Study Assay Technology Type Term Accession Number",
                        "Study Assay Technology Type",
                        "Study Assay Technology Type Term Source REF",
                        AssayTechnology.class
                );

            }
        }

        if (technology != null) {
            String techAcc = StringUtils.trimToNull(technology.getAcc());
            if (techAcc == null) {
                String techName = StringUtils.trimToNull(technology.getName());
                if (techName != null) {
                    technology.setAcc(techName);
                    rval.setTechnology(technology);
                }
            } else {
                rval.setTechnology(technology);
            }
        }

        if (mappedStudy == null) {
            throw new TabInternalErrorException(
                    "Assay mapper: No study to attach this assay to, assay:" + rval.getFilePath()
            );
        } else {
            rval.setStudy(mappedStudy);
        }


        log.trace("New mapped assay group: " + rval);
        return rval;
    }


    @Override
    public String getStoreKey(AssayGroup mappedObject) {
        return mappedObject == null ? null : mappedObject.getFilePath();
    }


    @Override
    public AssayGroup newMappedObject() {
        return new AssayGroup();
    }


    @Override
    public int getPriority() {
        return 90;
    }

    public void setMappedInvestigation(Study mappedStudy) {
        this.mappedStudy = mappedStudy;
    }

    @Override
    public List<Integer> getMatchedFieldIndexes() {
        List<Integer> result = super.getMatchedFieldIndexes();
        addMatchedField("Study Assay Measurement Type", result);
        addMatchedField("Study Assay Measurement Type Term Accession Number", result);
        addMatchedField("Study Assay Measurement Type Term Source REF", result);
        addMatchedField("Study Assay Technology Type", result);
        addMatchedField("Study Assay Technology Type Term Accession Number", result);
        addMatchedField("Study Assay Technology Type Term Source REF", result);
        return result;
    }


}
