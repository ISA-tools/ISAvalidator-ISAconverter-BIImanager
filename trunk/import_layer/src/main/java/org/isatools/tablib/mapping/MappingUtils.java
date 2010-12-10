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

package org.isatools.tablib.mapping;

import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.isatools.isatab.mapping.AssayGroup;
import org.isatools.isatab.mapping.StudyWrapper;
import org.isatools.tablib.exceptions.TabMissingValueException;
import org.isatools.tablib.schema.Field;
import org.isatools.tablib.schema.FormatInstance;
import org.isatools.tablib.schema.SectionInstance;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.model.Identifiable;
import uk.ac.ebi.bioinvindex.model.Study;
import uk.ac.ebi.bioinvindex.model.term.FreeTextTerm;
import uk.ac.ebi.bioinvindex.model.term.OntologyEntry;
import uk.ac.ebi.bioinvindex.model.term.OntologyTerm;
import uk.ac.ebi.bioinvindex.model.xref.ReferenceSource;

/**
 * Some utilities used for the mapping.
 * <p/>
 * TODO: Move to a more proper package
 * TODO: createTerm for the case both free-text and OE can be provided (e.g.: fators)
 * <p/>
 * Feb 25, 2008
 *
 * @author brandizi
 */
public class MappingUtils {
    protected BIIObjectStore store;
    protected static final Logger log = Logger.getLogger(AbstractTabMapper.class);


    public MappingUtils(BIIObjectStore store) {
        this.store = store;
    }

    /**
     * Returns an object on the basis of key values found in a TAB section.
     * Does not exceptions in case of consistency errors (key is null or
     * referred object is missing).
     *
     * @param sectionInstance the section to read from
     * @param recordIndex     the record index to be considered
     * @param fieldName       the field name referring the target object, this is
     *                        passed as id value to get()
     * @param objectType      the object type to lookup in the store
     * @return the referred object, or null if not found.
     */
    @SuppressWarnings("unchecked")
    public <T extends Identifiable> T getReferredObjectUnchecked(
            SectionInstance sectionInstance, int recordIndex, String fieldName, Class<T> objectType
    ) {
        String key = sectionInstance.getString(recordIndex, fieldName);
        if (key == null) {
            return null;
        }
        T rval = (T) store.get(objectType, key);

        log.trace(String.format("returning the referred object <%s: '%s'>", objectType, key));
        return rval;
    }


    /**
     * If the the referred field is null returns null, if the referred field
     * is null and the reference is missing from the object store, raises an exception
     */
    public <T extends Identifiable> T getReferredObject(
            SectionInstance sectionInstance, int recordIndex, String fieldName, Class<T> objectType
    ) {
        String key = sectionInstance.getString(recordIndex, fieldName);
        if (key == null) {
            return null;
        }

        T rval = getReferredObjectUnchecked(sectionInstance, recordIndex, fieldName, objectType);
        if (rval == null) {
            throw new TabMissingValueException(String.format(
                    "Referred object <%s: '%s'> not found", objectType, key
            ));
        }
        return rval;
    }


    /**
     * Creates an ontology entry, starting from fields where it is defined in the TAB.
     *
     * @param accessionFieldName The name of the field where to get the term accession
     * @param labelFieldName     The name of the filed where to get the term label
     * @param sourceFieldName    The name of the field where to get the term source reference.
     * @param oeClass            The OntologyEntry class to be instantiated.
     * @return the new term, with sources attached
     */
    public <T extends OntologyEntry> T createOntologyEntry(
            SectionInstance sectionInstance, int recordIndex,
            String accessionFieldName, String labelFieldName, String sourceFieldName, Class<T> oeClass
    ) {
        Field accessionField = sectionInstance.getField(accessionFieldName);
        String sourceStr = sectionInstance.getString(recordIndex, sourceFieldName);
        String labelStr = sectionInstance.getString(recordIndex, labelFieldName);
        String accessionStr = accessionField == null
                ? null : sectionInstance.getString(recordIndex, accessionField.getIndex());


        return createOntologyEntry(accessionStr, labelStr, sourceStr, oeClass);
    }

    /**
     * Creates a new ontology entry, from string parameters. Assumes the source for the term was
     * defined in the References spreadsheet.
     *
     * @param acc        The accession for the OE
     * @param labelField The name of the filed where to get the term label
     * @param sourceAcc  The name of term source, as it appears in References/Ontology Source References.
     *                   Does not set the source if this parameter is null.
     * @param oeClass    The OntologyEntry class to be instantiated.
     * @return the new term, with sources attached
     */
    @SuppressWarnings("unchecked")
    public <OET extends OntologyEntry> OET createOntologyEntry(
            String acc, String label, String sourceAcc, Class<OET> oeClass
    ) {
        acc = StringUtils.trimToNull(acc);
        label = StringUtils.trimToEmpty(label);
        sourceAcc = StringUtils.trimToNull(sourceAcc);

        if (acc == null && label == null && sourceAcc == null) {
            return null;
        }

        String logpref =
                String.format("createOntologyEntry( '%s:%s(%s), '%s' ): ", sourceAcc, acc, label, oeClass.getSimpleName());

        if (acc == null) {
            acc = "NULL-ACCESSION";
            log.debug("WARNING, No accession provided for the term '" + label + "'/'" + sourceAcc + "', using fake accession: " + acc);
        }

        ReferenceSource source = null;
        if (sourceAcc == null) {
            source = new ReferenceSource("Fictitious source for to-be curated terms");
            source.setAcc("BII:NULL-SOURCE");
            log.debug("WARNING, No source specified for the Ontology Term: '" + acc + "'/'" + label + "', adding fake source: " + source);
        } else {
            source = (ReferenceSource) store.get(ReferenceSource.class, sourceAcc);
            if (source == null) {
                throw new TabMissingValueException("Cannot find the ontology source '" + sourceAcc + "'");
            }
        }

        OET oe;
        try {
            oe = (OET) ConstructorUtils.invokeConstructor(
                    oeClass, new Object[]{acc, label, source},
                    new Class[]{String.class, String.class, ReferenceSource.class}
            );
        }
        catch (Exception e) {
            throw new TabMissingValueException("Cannot create a new OntologyEntry: " + e.getMessage(), e);
        }

        store.valueOfType(TabMappingContext.class).put("used.oeSource." + sourceAcc, oe);

        if (log.isTraceEnabled()) {
            log.trace(logpref + "Returning new Ontology Entry: " + oe);
        }
        return oe;
    }


    /**
     * Creates an ontology entry, starting from fields where it is defined in the TAB.
     *
     * @param accessionFieldName The name of the field where to get the term accession
     * @param termSource         An Ontology source to be assigned to this term
     * @param oeClass            The OntologyEntry class to be instantiated
     * @return the new term, with sources attached
     */
    @SuppressWarnings("unchecked")
    public <T extends OntologyEntry> T createOntologyEntry(
            SectionInstance sectionInstance, int recordIndex, String accessionFieldName, String labelFieldName,
            ReferenceSource termSource, Class<T> oeClass
    ) {
        String logpref = String.format(
                "createOntologyEntry( '%s', '%d', '%s':'%s'(%s), '%s' ): ",
                sectionInstance.getSection().getId(), recordIndex, termSource.getAcc(), accessionFieldName, labelFieldName, oeClass.getCanonicalName()
        );

        String labelStr = sectionInstance.getString(recordIndex, labelFieldName);
        String accessionStr = sectionInstance.getStringUnchecked(recordIndex, accessionFieldName);

        if (termSource == null) {
            termSource = new ReferenceSource("Fictious source for to-be curated terms");
            termSource.setAcc("BII:NULL-SOURCE");
            log.warn(
                    "WARNING, No source provided for the term '" + labelStr + "' / '" + accessionStr + "', using fake source: "
                            + termSource
            );
        }

        if (accessionStr == null) {
            accessionStr = "NULL-ACCESSION";
            log.debug(
                    "WARNING, No accession provided for the term '" + labelStr + "' / '" + termSource.getAcc()
                            + "', using fake accession: " + accessionStr
            );
        }


        T oe;
        try {
            oe = (T) ConstructorUtils.invokeConstructor(
                    oeClass, new Object[]{accessionStr, labelStr, termSource},
                    new Class[]{String.class, String.class, ReferenceSource.class}
            );
        }
        catch (Exception e) {
            throw new TabMissingValueException("Cannot create a new OntologyEntry: " + e.getMessage(), e);
        }

        store.valueOfType(TabMappingContext.class).put("used.oeSource." + termSource.getAcc(), termSource);

        if (log.isTraceEnabled()) {
            log.trace(logpref + "Returning new Ontology Entry: " + oe);
        }
        return oe;
    }


    /**
     * Creates a term, starting from fields where it is defined in the TAB.
     * See the alternative signature for details
     *
     * @param accessionFieldName  The name of the field where to get the term accession
     * @param labelFieldName      The name of the field where the free text bit is
     * @param termSourceFieldName The name of the field where to get the term source reference. If it is null,
     *                            does not set the source
     * @param termClass           The subclass of Term class to instance, which will represent the term as free text.
     * @param ontoTermClass       the subclass of OntologyTerm to be used to attach an ontology entry to the
     *                            free text term.
     */
    public <FT extends FreeTextTerm, OT extends OntologyTerm> FT createTerm(
            SectionInstance sectionInstance, int recordIndex,
            String accessionFieldName, String labelFieldName, String termSourceFieldName,
            Class<FT> termClass, Class<OT> ontoTermClass
    ) {
        Field accessionField = sectionInstance.getField(accessionFieldName);
        String accStr = accessionField == null ? null : sectionInstance.getString(recordIndex, accessionField.getIndex());
        String labelStr = sectionInstance.getString(recordIndex, labelFieldName);
        String sourceStr = sectionInstance.getString(recordIndex, termSourceFieldName);

        return createTerm(accStr, labelStr, sourceStr, termClass, ontoTermClass);
    }

    /**
     * Wrapper with freeText = oeLabel
     */
    public <FT extends FreeTextTerm, OT extends OntologyTerm> FT createTerm(
            String accession, String termLabel, String sourceAcc, Class<FT> termClass, Class<OT> ontoTermClass
    ) {
        return createTerm(termLabel, accession, termLabel, sourceAcc, termClass, ontoTermClass);
    }


    /**
     * Creates a new free text term, from string parameters. Assumes the source for the term was
     * defined in the References spreadsheet. Free text terms are subclasses of Term and may have ontology
     * entries attached. We actually add one OE in case sourceAcc is non empty.
     *
     * @param accession     The accession for the OE. Will use a fictious accession if it's null
     * @param freeText      The freetext part for the term
     * @param termLabel     The label for the OE term. If null no OE is attached to the returned free text
     * @param sourceAcc     The name of term source, as it appears in References/Ontology Source References.
     *                      If null, attaches a fictious source
     * @param termClass     The subclass of Term class to instance, which will represent the term as free text.
     * @param ontoTermClass the subclass of OntologyTerm to be used to attach an ontology entry to the
     *                      free text term.
     * @return the new term created, with ontology entries attached.
     */
    @SuppressWarnings("unchecked")
    public <FT extends FreeTextTerm, OT extends OntologyTerm> FT createTerm(
            String freeText, String accession, String termLabel, String sourceAcc, Class<FT> termClass, Class<OT> ontoTermClass
    ) {
        accession = StringUtils.trimToNull(accession);
        termLabel = StringUtils.trimToEmpty(termLabel);
        sourceAcc = StringUtils.trimToNull(sourceAcc);

        if (freeText == null && accession == null && termLabel == null && sourceAcc == null) {
            return null;
        }

        String logpref = String.format(
                "createTerm( '%s':'%s'(%s), '%s', '%s' ): ",
                sourceAcc, accession, termLabel, termClass.getCanonicalName(), ontoTermClass.getCanonicalName()
        );

        FT term;
        try {
            term = (FT) ConstructorUtils.invokeConstructor(
                    termClass, new String[]{freeText}
            );
        }
        catch (Exception ex) {
            throw new TabMissingValueException("Cannot create a new Term: " + ex.getMessage(), ex);
        }

        if (termLabel != null) {
            log.trace(logpref + "creating ontology entry to be attached to the term: '" + accession + "'");
            OntologyTerm oterm = (OntologyTerm) this.createOntologyEntry(accession, termLabel, sourceAcc, ontoTermClass);
            term.addOntologyTerm(oterm);
        }

        if (log.isTraceEnabled()) {
            log.trace(logpref + "Returning new Term class: " + term);
        }

        return term;
    }


    /**
     * Gets the assay group associated to this section. Lookup the assay group in the store, using
     * sectionInstance.{@link SectionInstance#getParent()}.{@link FormatInstance#getFileId()} as key.
     */
    public AssayGroup getAssayGroupFromSection(SectionInstance sectionInstance) {
        String fileId = StringUtils.trimToNull(sectionInstance.getFileId());
        if (fileId == null) {
            log.error(
                    "ERROR: MappingUtils.getAssayGroup(): no fileId found in instance of "
                            + sectionInstance.getSectionId() + "/" + sectionInstance.getParent().getFormat().getId()
            );
            return null;
        }

        return (AssayGroup) store.get(AssayGroup.class, fileId);
    }

    /**
     * The Study associated to the pipeline the sectionInstance belong to. This is computed checking {@link #getFileId()},
     * if the format we are dealing with is a study format, uses the file ID to get a {@link StudyWrapper} from
     * the store. Otherwise lookup an {@link AssayGroup}.
     */
    public Study getStudyFromSection(SectionInstance sectionInstance) {
        String formatId = sectionInstance.getParent().getFormat().getId();

        if ("study_samples".equals(formatId)) {
            String fileId = StringUtils.trimToNull(sectionInstance.getFileId());

            StudyWrapper studyWrapper = (StudyWrapper) store.get(StudyWrapper.class, fileId);
            if (studyWrapper != null) {
                return studyWrapper.getStudy();
            } else
            // TODO must be an exception indeed
            {
                log.error(
                        "ERROR: Cannot find a study to associate to a material/data processing step, instance: "
                                + sectionInstance.getSectionId() + "/" + formatId + ", file: " + fileId
                );
            }
        } else {
            AssayGroup assayGroup = getAssayGroupFromSection(sectionInstance);
            if (assayGroup != null) {
                return assayGroup.getStudy();
            } else
            // TODO must be an exception indeed
            {
                log.error(
                        "ERROR: Cannot find a study to associate to a matarial/data processing step,file: "
                                + sectionInstance.getSectionId() + "/" + formatId + ", file: " + sectionInstance.getFileId()
                );
            }
        }
        return null;
    }
}
