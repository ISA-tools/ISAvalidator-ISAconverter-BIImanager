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
import org.apache.commons.lang.StringUtils;
import org.isatools.tablib.exceptions.TabInternalErrorException;
import org.isatools.tablib.exceptions.TabMissingValueException;
import org.isatools.tablib.mapping.MappingUtils;
import org.isatools.tablib.mapping.properties.PropertyMappingHelper;
import org.isatools.tablib.schema.Field;
import org.isatools.tablib.schema.SectionInstance;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.model.Identifiable;
import uk.ac.ebi.bioinvindex.model.term.FreeTextTerm;
import uk.ac.ebi.bioinvindex.model.term.OntologyTerm;
import uk.ac.ebi.bioinvindex.utils.i18n;
import uk.ac.ebi.utils.reflection.ReflectionUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Maps a property value which is possibly an ontology entry (i.e.: which has a term source reference in the TAB).
 * Considers the value itself, the ontology source reference (evaluate if it's empty) and the possible accession.
 * <p/>
 * This mapper expects that the type PVT has the constructor
 * PVT(String name, MappedProperty type).
 * <p/>
 * Jan 9, 2008
 *
 * @author brandizi
 */
public abstract class BIIPropValFreeTextMappingHelper
        <T extends Identifiable, PT extends FreeTextTerm, PVT extends FreeTextTerm>
        extends PropertyMappingHelper<T, PVT> {
    /**
     * The field range where this source is represented
     */
    private final PT type;
    protected final MappingUtils mappingUtils;
    private Field right1Field;


    public BIIPropValFreeTextMappingHelper(
            BIIObjectStore store, SectionInstance sectionInstance, int fieldIndex, PT type) {
        super(store, sectionInstance, BIIPropertyValueMappingHelper.TERM_SOURCE_HEADER, null, fieldIndex);
        this.type = type;
        this.mappingUtils = new MappingUtils(store);
    }


    /**
     * This default version:
     * <ul>
     * <li> produces a {@link FreeTextTerm} instance
     * <li> checks if there is something in Term Source Name, if yes an {@link OntologyTerm} is attached,
     * using {@link #type}, possibly attaching an accession
     * </ul>
     */
    @SuppressWarnings("unchecked")
    @Override
    public PVT mapProperty(int recordIndex) {
        SectionInstance sectionInstance = getSectionInstance();

        int ifield = this.getFieldIndex();
        if (ifield == -1) {
            log.trace(String.format("WARNING: Field %s not found in section %s", getFieldName(), sectionInstance.getSectionId()));
            return null;
        }

        String valueStr = StringUtils.trimToNull(sectionInstance.getString(recordIndex, ifield));

        // null values don't make sense and we can ignore them.
        if (valueStr == null) {
            return null;
        }

        // instantiate a new property value and setup its name
        //

        // Needs to extrapolate the classes from the generic and dynamically instantiate the value.
        PVT value = null;
        try {
            value = (PVT) ConstructorUtils.invokeConstructor(
                    getMappedBIIPropertyValueClass(),
                    new Object[]{valueStr, (PT) type},
                    new Class[]{String.class, getMappedBIIPropertyClass()}
            );
        } catch (Exception ex) {
            String msg = "Error in creating new property value,\nClass<PV> = "
                    + this.getMappedBIIPropertyValueClass().getName()
                    + "\nClass<PVT> = " + this.getMappedBIIPropertyClass()
                    + "\nClass(Property)" + this.getMappedPropertyClass() + "\n  error: " + ex.getMessage();
            throw new TabInternalErrorException(msg, ex);
        }


        // Handle source and (optional) accession
        //
        String accessionStr = "";
        String sourceStr = StringUtils.trimToEmpty((String) sectionInstance.getString(recordIndex, ifield + 1));

        Field right1Field = getRight1Field();
        boolean hasAccessionHeader = right1Field != null && BIIPropertyValueMappingHelper.ACCESSION_HEADER.equals(right1Field.getAttr("id"));
        if (hasAccessionHeader) {
            accessionStr = StringUtils.trimToEmpty((String) sectionInstance.getString(recordIndex, ifield + 2));
        }

        if (sourceStr.length() == 0) {
            // I don't have a source but I have the accession: that's too bad!
            if (hasAccessionHeader && accessionStr.length() != 0) {
                throw new TabMissingValueException(i18n.msg("accession_without_source", accessionStr));
            }

            // Otherwise I fall to consider it free text
            log.debug("WARNING: No ontology source specified for '" + valueStr + "', assuming it is free text");
        } else {
            // I have a source, let's check the accession and eventually set the term
            if (!hasAccessionHeader || accessionStr.length() == 0) {
                String typeName = type == null ? "[?]" : type.getValue();
                log.debug("WARNING: Creating ontology term '" + typeName + "' / '" + valueStr + "' without accession, assuming it's free text");
            }
            OntologyTerm term =
                    (OntologyTerm) mappingUtils.createOntologyEntry(accessionStr, valueStr, sourceStr, OntologyTerm.class);
            value.addOntologyTerm(term);
        }

        if (log.isTraceEnabled()) {
            Collection<OntologyTerm> terms = value.getOntologyTerms();
            Iterator<OntologyTerm> titr = terms.iterator();
            OntologyTerm term = titr != null && titr.hasNext() ? titr.next() : null;
            log.trace("Returning mapped ontology term '" + value + "', source: " + term);
        }

        return value;
    }


    /**
     * The field located 2 positions next to startFieldIndex
     */
    protected Field getRight1Field() {
        if (right1Field != null) {
            return right1Field;
        }
        right1Field = getSectionInstance().getField(getFieldIndex() + 2);

        if (right1Field == null || !BIIPropertyValueMappingHelper.ACCESSION_HEADER.equals(right1Field.getAttr("id"))) {
            String typeName = type == null ? "[?]" : type.getValue();
            log.debug("WARNING Ontology attribute '" + typeName + "' without accession header," +
                    " assuming free text terms.");
        }

        return right1Field;
    }


    @Override
    public List<Integer> getMatchedFieldIndexes() {
        int fieldIndex = getFieldIndex();

        Field right1Field = getRight1Field();
        boolean hasAccessionHeader =
                right1Field != null && BIIPropertyValueMappingHelper.ACCESSION_HEADER.equals(right1Field.getAttr("id"));

        return hasAccessionHeader
                ? Arrays.asList(fieldIndex, fieldIndex + 1, fieldIndex + 2)
                : Arrays.asList(fieldIndex, fieldIndex + 1);
    }


    /**
     * The class bound to PT
     */
    public Class<PT> getMappedBIIPropertyClass() {
        // TODO: caching
        return ReflectionUtils.getTypeArgument(BIIPropValFreeTextMappingHelper.class, this.getClass(), 1);
    }


    /**
     * The class bound to PVT
     */
    public Class<PVT> getMappedBIIPropertyValueClass() {
        // TODO: caching
        return ReflectionUtils.getTypeArgument(BIIPropValFreeTextMappingHelper.class, this.getClass(), 2);
    }

}