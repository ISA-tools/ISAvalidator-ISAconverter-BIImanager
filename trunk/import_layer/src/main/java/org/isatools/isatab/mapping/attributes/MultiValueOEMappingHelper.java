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
import org.isatools.tablib.exceptions.TabInvalidValueException;
import org.isatools.tablib.mapping.MappingUtils;
import org.isatools.tablib.mapping.properties.PropertyMappingHelper;
import org.isatools.tablib.schema.SectionInstance;
import org.isatools.tablib.utils.BIIObjectStore;
import org.isatools.tablib.utils.Utils;
import uk.ac.ebi.bioinvindex.model.Identifiable;
import uk.ac.ebi.bioinvindex.model.term.OntologyEntry;
import uk.ac.ebi.utils.reflection.ReflectionUtils;

import java.util.*;

/**
 * Maps a multi-value set of fields that stores an {@link OntologyEntry Ontology Entry}
 * <p/>
 * <dl><dt>date:</dt><dd>Nov 13, 2008</dd></dl>
 *
 * @author brandizi
 * @param <T>
 * @param <PT>
 */
public abstract class MultiValueOEMappingHelper<T extends Identifiable, PT extends OntologyEntry>
        extends PropertyMappingHelper<T, Collection<PT>> {
    private final String accessionFieldName, sourceFieldName;
    private final MappingUtils mappingUtils;

    public MultiValueOEMappingHelper(
            BIIObjectStore store, SectionInstance sectionInstance, Map<String, String> options, int fieldIndex
    ) {
        super(store, sectionInstance, options, fieldIndex);
        mappingUtils = new MappingUtils(store);

        if (options == null) {
            accessionFieldName = sourceFieldName = null;
        } else {
            accessionFieldName = options.get("accessionFieldName");
            sourceFieldName = options.get("sourceFieldName");
        }
    }

    /**
     * @param fieldName          e.g.: "Contact Roles Type", contains labels for Ontology Entries.
     * @param accessionFieldName e.g.: "Contact Roles Type Accession Number"
     * @param sourceFieldName    e.g.: "Contact Roles Type Source REF"
     * @param propertyName       the property name to be mapped, e.g.: "roles". Usually has reference-purpouse only.
     */
    public MultiValueOEMappingHelper(
            BIIObjectStore store, SectionInstance sectionInstance,
            String fieldName, String accessionFieldName, String sourceFieldName,
            String propertyName, int fieldIndex
    ) {
        super(store, sectionInstance, fieldName, propertyName, fieldIndex);
        mappingUtils = new MappingUtils(store);

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


        if (!(
                accSs.length == 0 && srcSs.length == 0
                        || propSs.length == accSs.length && accSs.length == srcSs.length
        )) {
            throw new TabInvalidValueException(String.format(
                    "Bad specification for multi-valued property: <%s> = '%s', <%s> = '%s', <%s> = '%s'",
                    getFieldName(), propS,
                    accessionFieldName, accS,
                    sourceFieldName, srcS
            ));
        }

        Collection<PT> result = new ArrayList<PT>();
        Class<PT> oeClass = ReflectionUtils.getTypeArgument(MultiValueOEMappingHelper.class, this.getClass(), 1);
        for (int i = 0; i < propSs.length; i++) {
            String propSi = StringUtils.trimToNull(propSs[i]);
            String accSi = null, srcSi = null;
            accSi = accSs.length == 0 ? null : StringUtils.trimToNull(accSs[i]);
            srcSi = srcSs.length == 0 ? null : StringUtils.trimToNull(srcSs[i]);

            PT term = mappingUtils.createOntologyEntry(accSi, propSi, srcSi, oeClass);
            result.add(term);
        }

        return result;
    }

    @Override
    public List<Integer> getMatchedFieldIndexes() {
        int fieldIndex = getFieldIndex();
        return this.accessionFieldName == null
                ? Arrays.asList(fieldIndex)
                : Arrays.asList(fieldIndex, fieldIndex + 1, fieldIndex + 2);
    }

}
