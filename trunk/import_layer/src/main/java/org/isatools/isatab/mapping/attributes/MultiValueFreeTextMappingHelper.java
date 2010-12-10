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

package org.isatools.isatab.mapping.attributes;

import org.apache.commons.lang.StringUtils;
import org.isatools.tablib.exceptions.TabInternalErrorException;
import org.isatools.tablib.exceptions.TabInvalidValueException;
import org.isatools.tablib.mapping.MappingUtils;
import org.isatools.tablib.mapping.properties.PropertyMappingHelper;
import org.isatools.tablib.schema.SectionInstance;
import org.isatools.tablib.utils.BIIObjectStore;
import org.isatools.tablib.utils.Utils;
import uk.ac.ebi.bioinvindex.model.Identifiable;
import uk.ac.ebi.bioinvindex.model.term.FreeTextTerm;
import uk.ac.ebi.bioinvindex.model.term.OntologyTerm;
import uk.ac.ebi.utils.reflection.ReflectionUtils;

import java.util.*;

/**
 * Maps a multi-valued free-text term, where values are in single cells, with a semicolon separator.
 * This version assumes that both the fields for the free text value and the ontology entry exist.
 * <p/>
 * <p><b>date</b>: Sep 16, 2008</p>
 *
 * @author brandizi
 * @param <T> the type which of property is mapped.
 * @param <PT> the property being mapped.
 */
public abstract class MultiValueFreeTextMappingHelper<T extends Identifiable, PT extends FreeTextTerm>
        extends PropertyMappingHelper<T, Collection<PT>> {
    private final String ontologyTermFieldName, accessionFieldName, sourceFieldName;
    private final MappingUtils mappingUtils;

    public MultiValueFreeTextMappingHelper(
            BIIObjectStore store, SectionInstance sectionInstance, Map<String, String> options, int fieldIndex
    ) {
        super(store, sectionInstance, options, fieldIndex);
        mappingUtils = new MappingUtils(store);

        if (options == null) {
            ontologyTermFieldName = accessionFieldName = sourceFieldName = null;
        } else {
            ontologyTermFieldName = options.get("ontologyTermFieldName");
            accessionFieldName = options.get("accessionFieldName");
            sourceFieldName = options.get("sourceFieldName");
        }
    }

    /**
     * @param fieldName             e.g.: "Component Name"
     * @param ontologyTermFieldName e.g.: "Component Type"
     * @param accessionFieldName    e.g.: "Component Type Accession Number"
     * @param sourceFieldName       e.g.: "Component Type Source REF"
     * @param propertyName          the property name to be mapped (needed only as a reference), e.g.: "components"
     */
    public MultiValueFreeTextMappingHelper(
            BIIObjectStore store, SectionInstance sectionInstance,
            String fieldName, String ontologyTermFieldName, String accessionFieldName, String sourceFieldName,
            String propertyName, int fieldIndex
    ) {
        super(store, sectionInstance, fieldName, propertyName, fieldIndex);
        mappingUtils = new MappingUtils(store);

        this.ontologyTermFieldName = ontologyTermFieldName;
        this.accessionFieldName = accessionFieldName;
        this.sourceFieldName = sourceFieldName;
    }


    @Override
    public Collection<PT> mapProperty(int recordIndex) {
        SectionInstance sectionInstance = getSectionInstance();

        // Check on null is needed, split() returns the whole empty string otherwise
        //
        String propS = sectionInstance.getString(recordIndex, getFieldName());
        String propSs[] = Utils.splitMultiValue(propS, ";");

        String termNameS = sectionInstance.getString(recordIndex, ontologyTermFieldName);
        String termNameSs[] = Utils.splitMultiValue(termNameS, ";");

        String accS = sectionInstance.getStringUnchecked(recordIndex, accessionFieldName);
        String accSs[] = Utils.splitMultiValue(accS, ";");

        String srcS = sectionInstance.getStringUnchecked(recordIndex, sourceFieldName);
        String srcSs[] = Utils.splitMultiValue(srcS, ";");

        if (srcSs.length == 1 && accSs.length > 1) {
            // When only one source is specified for multiple terms, assume it is the same for all the terms
            srcS = srcSs[0].trim();
            srcSs = new String[accSs.length];
            for (int i = 0; i < accSs.length; i++) {
                srcSs[i] = srcS;
            }
        }

        int n = -1;

        if (propSs.length > 0
                && ((termNameSs.length == propSs.length &&
                (accSs.length == termNameSs.length && srcSs.length == termNameSs.length
                        || accSs.length == 0 && srcSs.length == 0))
                || termNameSs.length == 0 && accSs.length == 0 && srcSs.length == 0
        )
                )
        // There is the type + term label part, we can have same number of accessions/sources, or nothing else
        {
            n = propSs.length;
        } else if (
                termNameSs.length == accSs.length && srcSs.length == accSs.length
                        || accSs.length == 0 && srcSs.length == 0
                )
        // No type, we either have the OEs or not
        {
            n = termNameSs.length;
        } else {
            throw new TabInvalidValueException(String.format(
                    "Bad specification for multi-valued property: <%s> = '%s', <%s> = '%s', <%s> = '%s', <%s> = '%s'",
                    getFieldName(), propS,
                    ontologyTermFieldName, termNameS,
                    accessionFieldName, accS,
                    sourceFieldName, srcS
            ));
        }

        Collection<PT> result = new ArrayList<PT>();
        Class<PT> ptClass = ReflectionUtils.getTypeArgument(MultiValueFreeTextMappingHelper.class, this.getClass(), 1);
        for (int i = 0; i < n; i++) {
            String propSi = i < propSs.length ? StringUtils.trimToNull(propSs[i]) : null;
            String termNameSi = i < termNameSs.length ? StringUtils.trimToNull(termNameSs[i]) : null;
            if (propSi == null) {
                propSi = termNameSi;
            }
            if (propSi == null) {
                throw new TabInternalErrorException("Null value for '" + getFieldName() + "'");
            }

            String accSi = null, srcSi = null;
            if (accSs.length != 0) {
                accSi = StringUtils.trimToNull(accSs[i]);
                srcSi = StringUtils.trimToNull(srcSs[i]);
            }

            PT term = mappingUtils.createTerm(propSi, accSi, termNameSi, srcSi, ptClass, OntologyTerm.class);
            if (term != null) {
                result.add(term);
            }
        }

        return result;
    }

    @Override
    public List<Integer> getMatchedFieldIndexes() {
        int fieldIndex = getFieldIndex();
        return this.ontologyTermFieldName == null
                ? Arrays.asList(fieldIndex)
                : Arrays.asList(fieldIndex, fieldIndex + 1, fieldIndex + 2, fieldIndex + 3);
    }
}
