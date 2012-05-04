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

import org.apache.commons.beanutils.ConstructorUtils;
import org.isatools.tablib.exceptions.TabInternalErrorException;
import org.isatools.tablib.mapping.properties.PropertyMappingHelper;
import org.isatools.tablib.schema.Field;
import org.isatools.tablib.schema.SectionInstance;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.model.Identifiable;
import uk.ac.ebi.bioinvindex.model.term.OntologyTerm;
import uk.ac.ebi.bioinvindex.model.term.Property;
import uk.ac.ebi.bioinvindex.model.term.PropertyValue;
import uk.ac.ebi.bioinvindex.model.xref.ReferenceSource;
import uk.ac.ebi.bioinvindex.utils.i18n;
import uk.ac.ebi.utils.reflection.ReflectionUtils;

import java.util.List;
import java.util.Map;

/**
 * Maps a property value in a SDRF-like section, such as a Characteristics or a Parameter.
 * A property of this type may have additional fields:<ul>
 * <li>term source (+ accession), managed by a specific mapper)
 * <li>unit (and unit fields, managed by another specific mapper)
 * </ul>
 * Jan 24, 2008
 *
 * @param <PT>  The property type the value is about (an instance
 *              of this is created and attached to the value).
 * @param <PVT> The propertyValue mapped by this mapper.
 * @author brandizi
 */
public abstract class BIIPropertyValueMappingHelper
        <T extends Identifiable, PT extends Property<PVT>, PVT extends PropertyValue<PT>>
        extends PropertyMappingHelper<T, PVT> {
    // TODO: class of constants
    public static final String TERM_SOURCE_HEADER = "Term Source REF";
    public static final String UNIT_HEADER = "Unit";
    public static final String ACCESSION_HEADER = "Term Accession Number";

    /**
     * the TAB header where the description of this field starts, initialized by the constructor
     */
    private PT type;
    private Field rightField, right1Field;
    private PropertyMappingHelper<T, PVT> delegateHelper;


    public BIIPropertyValueMappingHelper(BIIObjectStore store, SectionInstance sectionInstance,
                                         Map<String, String> options, int fieldIndex) {
        super(store, sectionInstance, options, fieldIndex);
    }

    public BIIPropertyValueMappingHelper(BIIObjectStore store, SectionInstance sectionInstance,
                                         String fieldName, String propertyName, int fieldIndex) {
        super(store, sectionInstance, fieldName, propertyName, fieldIndex);
    }


    /**
     * Decides which helper actually does the job, on the basis of the existence of a term source field,
     * or a unit field, or nothing
     */
    private PropertyMappingHelper<T, PVT> getDelegateHelper() {
        Field rightField = getRightField();
        String rightHeader = rightField == null ? null : rightField.getAttr("id");
        if (TERM_SOURCE_HEADER.equals(rightHeader)) {
            delegateHelper = newOntologyTermDelegate();
        } else if (UNIT_HEADER.equals(rightHeader)) {
            delegateHelper = newQuantityDelegate();
        } else {
            delegateHelper = new PlainBIIPropValDelegate<T, PT, PVT>(this);
        }

        return delegateHelper;

    }


    /**
     * Delegates to {@link #getDelegateHelper()}
     */
    @Override
    public T map(T mappedObject, int recordIndex) {
        return getDelegateHelper().map(mappedObject, recordIndex);
    }


    /**
     * Delegates to {@link #getDelegateHelper()}
     */
    @Override
    public PVT mapProperty(int recordIndex) {
        return getDelegateHelper().mapProperty(recordIndex);
    }


    /**
     * Gets the type of the attribute mapped by this mapper. Computes it
     * from the header identified by {@link #getFieldIndex()}.
     */
    @SuppressWarnings("unchecked")
    public PT getType() {

        if (type != null) {
            return type;
        }

        int fieldIndex = getFieldIndex();

        SectionInstance sectionInstance = getSectionInstance();
        Field attributeField = sectionInstance.getField(fieldIndex);

        if (attributeField == null) {
            throw new TabInternalErrorException(i18n.msg("absent_field", fieldIndex));
        }

        String typeStr = attributeField.getAttr("type");
        if (typeStr == null || typeStr.length() == 0) {
            throw new TabInternalErrorException(i18n.msg("absent_field_attribute_type", fieldIndex, attributeField));
        }

        Class<PT> typeClass = ReflectionUtils.getTypeArgument(BIIPropertyValueMappingHelper.class, this.getClass(), 1);
        PT type;
        try {
            type = (PT) ConstructorUtils.invokeConstructor(typeClass, fieldIndex);
        } catch (Exception ex) {
            throw new TabInternalErrorException(i18n.msg("pb_mapping_attribute", typeStr, ex.getMessage()), ex);
        }
        type.setValue(typeStr);

        // we may have an accession in the header, which defines the type as an ontology term. It can have accessions as
        // URLs or as the standard OBI:23122 type. Here we get those and attach the ontology to the term so that we can 
        // use it later. 
        String accessionStr = attributeField.getAttr("accession");
        if (accessionStr != null && accessionStr.length() > 0) {
            boolean accessionIsUrl = attributeField.isAccessionURL();
            String source = accessionIsUrl ? "" : accessionStr.contains(":") ? accessionStr.substring(0, accessionStr.indexOf(":")) : "";
            System.out.println("Adding ontology term to " + typeStr + ", it is " + accessionStr + " whose source is " + source);
            type.addOntologyTerm(new OntologyTerm(accessionStr, typeStr, new ReferenceSource(source)));
        }
        return type;
    }


    /**
     * Creates the specific helper needed to manage the terms with Term Source REF
     */
    protected abstract BIIPropValFreeTextMappingHelper<T, PT, PVT> newOntologyTermDelegate();


    /**
     * Creates the specific helper needed to manage terms with units
     */
    protected abstract QtyBIIPropValMappingHelper<T, PT, PVT> newQuantityDelegate();


    /**
     * The field located next to startFieldIndex
     */
    protected Field getRightField() {
        if (rightField != null) {
            return rightField;
        }
        return rightField = getSectionInstance().getField(getFieldIndex() + 1);
    }


    /**
     * The field located 2 positions next to startFieldIndex
     */
    protected Field getRight1Field() {
        if (right1Field != null) {
            return right1Field;
        }
        return right1Field = getSectionInstance().getField(getFieldIndex() + 2);
    }

    /**
     * Delegates it to the {@link #getDelegateHelper()}
     */
    @Override
    public List<Integer> getMatchedFieldIndexes() {
        return getDelegateHelper().getMatchedFieldIndexes();
    }

    ;

}