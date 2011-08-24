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

package org.isatools.isatab.mapping;


import org.apache.commons.lang.StringUtils;
import org.isatools.isatab.mapping.attributes.CommentMappingHelper;
import org.isatools.tablib.exceptions.TabInternalErrorException;
import org.isatools.tablib.exceptions.TabMissingValueException;
import org.isatools.tablib.mapping.ClassTabMapper;
import org.isatools.tablib.mapping.MappingUtils;
import org.isatools.tablib.mapping.properties.StringPropertyMappingHelper;
import org.isatools.tablib.schema.SectionInstance;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.model.Investigation;
import uk.ac.ebi.bioinvindex.model.Study;
import uk.ac.ebi.bioinvindex.model.term.Design;
import uk.ac.ebi.bioinvindex.model.term.OntologyTerm;
import uk.ac.ebi.bioinvindex.utils.i18n;

import java.util.Collection;

/**
 * Maps the study sections in the Investigation file
 * <p/>
 * Dec 2007
 *
 * @author brandizi
 */
public class StudyTabMapper extends ClassTabMapper<Study> {
    private MappingUtils mappingUtils;
    private Study mappedStudy;

    @SuppressWarnings("unchecked")
    public StudyTabMapper(BIIObjectStore store, SectionInstance sectionInstance) {
        super(store, sectionInstance);

        mappingHelpersConfig.put("Study Identifier", new MappingHelperConfig<StringPropertyMappingHelper>(
                StringPropertyMappingHelper.class, new String[][]{{"propertyName", "acc"}}
        ));
        mappingHelpersConfig.put("Study Title", new MappingHelperConfig<StringPropertyMappingHelper>(
                StringPropertyMappingHelper.class, new String[][]{{"propertyName", "title"}}
        ));
        mappingHelpersConfig.put("Study Description", new MappingHelperConfig<StringPropertyMappingHelper>(
                StringPropertyMappingHelper.class, new String[][]{{"propertyName", "description"}}
        ));

        mappingHelpersConfig.put("Comment", new MappingHelperConfig<CommentMappingHelper>(
                CommentMappingHelper.class
        ));

        mappingUtils = new MappingUtils(store);
    }


    public Study map(int recordIndex) {

        SectionInstance sectionInstance = getSectionInstance();

        // Basic properties
        mappedStudy = super.map(recordIndex);

        if (StringUtils.trimToNull(mappedStudy.getAcc()) == null) {
            throw new TabMissingValueException(i18n.msg("missing_study_accession", mappedStudy.getTitle()));
        }

        // Design
        Design design = (Design) mappingUtils.createTerm(
                sectionInstance, recordIndex, "Study Design Type Term Accession Number", "Study Design Type", "Study Design Type Term Source REF",
                Design.class, OntologyTerm.class
        );


        mappedStudy.addDesign(design);

        // The investigation
        Collection<Investigation> investigations = (Collection) getStore().values(Investigation.class);
        if (investigations == null || investigations.size() == 0) {
            log.trace("study '" + mappedStudy.getAcc() + "' has no investigation");
        } else {
            if (investigations.size() > 1) {
                throw new TabInternalErrorException(i18n.msg("too_many_investigations", investigations.size()));
            }

            Investigation investigation = investigations.iterator().next();
            mappedStudy.addInvestigation(investigation);
        }

        log.trace("New mapped study: " + mappedStudy);
        return mappedStudy;
    }


    @Override
    public String getStoreKey(Study mappedObject) {
        return mappedObject == null ? null : mappedObject.getAcc();
    }

    /**
     * Keeps track of the mapped study. We should have only one study per study block
     */
    public Study getMappedStudy() {
        return mappedStudy;
    }


    @Override
    public int getPriority() {
        return 95;
    }

    /**
     * Always returns true, cause this mapper is old and incomplete and we're not interested in this kind of validation.
     */
    @Override
    public boolean validateHeaders() {
        return true;
    }

}