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
import org.apache.log4j.Logger;
import org.isatools.tablib.exceptions.TabInternalErrorException;
import org.isatools.tablib.exceptions.TabStructureError;
import org.isatools.tablib.mapping.pipeline.ProcessingsTabMapper;
import org.isatools.tablib.mapping.properties.PropertyMappingHelper;
import org.isatools.tablib.schema.Field;
import org.isatools.tablib.schema.SectionInstance;
import org.isatools.tablib.schema.constraints.FieldCardinalityConstraint;
import org.isatools.tablib.schema.constraints.FieldConstraint;
import org.isatools.tablib.utils.BIIObjectStore;
import org.isatools.tablib.utils.Utils;
import uk.ac.ebi.bioinvindex.model.Identifiable;
import uk.ac.ebi.bioinvindex.utils.i18n;

import java.util.*;

/**
 * Maps sections from a TAB format to Java objects. This class does a class-specific job, consisting in
 * instantiating an object from T and using the {@PropertyMappingHelper PropertyMappingHelper(s)}
 * for populating the object.
 */
public abstract class ClassTabMapper<T extends Identifiable> extends RecordOrientedTabMapper<T> {

    /**
     * Must be initialized with entries of type: [field name -&gt; helperClass].
     * Then the mapper goes through the mapped section's headers and when it matches a field name
     * the corresponding helperClass is initialized.
     */
    protected Map<String, MappingHelperConfig<?>> mappingHelpersConfig =
            new HashMap<String, MappingHelperConfig<?>>();

    /**
     * Contains the possible property mappers, in the form Mapping Field Index -&gt; Mapper
     */
    protected Map<Integer, PropertyMappingHelper<T, ?>> propertyMappers;

    protected static final Logger log = Logger.getLogger(ClassTabMapper.class);


    /**
     * Defines a configuration for initializing a mapping helperClass
     *
     * @param <PH> the helperClass to be instantiated when fieldName is matched
     */
    @SuppressWarnings("unchecked")
    protected class MappingHelperConfig
            <PH extends PropertyMappingHelper> {
        private Map<String, String> options = new HashMap<String, String>();
        private final Class<PH> helperClass;

        public MappingHelperConfig(Class<PH> helperClass, String[][] config) {
            this(helperClass);
            if (config == null) {
                return;
            }
            for (String[] configEntry : config) {
                this.options.put(configEntry[0], configEntry[1]);
            }
        }

        public MappingHelperConfig(Class<PH> helperClass, Map<String, String> options) {
            this(helperClass);
            if (options != null) {
                this.options = options;
            }
        }

        public MappingHelperConfig(Class<PH> helperClass) {
            this.helperClass = helperClass;
        }

        /**
         * Options to be passed to the mapper. It is always non-null.
         * "fieldName" is always initialized by default.
         * Common options are:
         * <ul>
         * <li>propertyName: the name of the mapped object property, used, for instance in {@code set<PropertyName>()}</li>
         * </ul>
         */
        public Map<String, String> getOptions() {
            return options;
        }

        /**
         * The mapper which maps the property stored by fieldName
         */
        public Class<PH> getHelperClass() {
            return helperClass;
        }

        /**
         * The options to be used to initialize the mapper
         */
        public String getOption(String key) {
            return this.options.get(key);
        }

    }
    // MappingHelperConfig


    public ClassTabMapper(BIIObjectStore store, SectionInstance sectionInstance) {
        super(store, sectionInstance);
    }


    /**
     * Does some validation before delegating the mapping to the parent.
     *
     * @see {@link #validateHeaders()} and {@link #validateCardinality()}.
     */
    @Override
    public BIIObjectStore map() {
        // Do some validation
        if (!validateHeaders()) {
            throw new TabStructureError(
                    "Errors in fields order/structure with the file \"" + getSectionInstance().getFileId() + "\"");
        }
        validateCardinality();
        return super.map();
    }


    /**
     * Maps the class managed by this class mapper. DO NOT save the object in the object store, this is left
     * to the invoker.
     *
     * @return the mapped object
     */
    public T map(int recordIndex) {
        try {
            // instantiate a new mappedClass
            T mappedObject = (T) newMappedObject();

            // Use the property helpers
            Map<Integer, PropertyMappingHelper<T, ?>> propertyMappers = getPropertyHelpers();
            if (propertyMappers != null) {
                for (PropertyMappingHelper<T, ?> pmap : propertyMappers.values()) {
                    pmap.map(mappedObject, recordIndex);
                }
            }

            return mappedObject;
        } catch (Exception ex) {
            throw new TabInternalErrorException(String.format(
                    "Problem while mapping the class '%s': %s", getMappedClass().getSimpleName(), ex.getMessage()),
                    ex
            );
        }

    }


    /**
     * The property helpers. Define the properties of the managed mapped class
     * and to setup mappers to manage properties mapping. The helpers are computed on the basis of
     * {@link mappingHelpersConfig}.
     *
     * @return the property helpers, in the form starting field index -&gt; helper. While the result may be an empty
     *         collection, null is avoided.
     */
    public Map<Integer, PropertyMappingHelper<T, ?>> getPropertyHelpers() {
        List<Field> fields = getSectionInstance().getFields();
        return this.getPropertyHelpers(0, fields.size() - 1);
    }


    /**
     * This version may be used by sub-classes to restrict the range where the properties should be looked up.
     * Typically you will override {@link #getPropertyHelpers()} so that it calls a specific range.
     */
    protected Map<Integer, PropertyMappingHelper<T, ?>> getPropertyHelpers(int startFieldIndex, int endFieldIndex) {
        if (propertyMappers != null) {
            return Collections.unmodifiableMap(this.propertyMappers);
        }

        if (mappingHelpersConfig.size() == 0) {
            log.trace("WARNING, No helper defined for the the mapper " + this.getClass().getSimpleName());
            propertyMappers = Collections.emptyMap();
            return propertyMappers;
        }

        propertyMappers = new HashMap<Integer, PropertyMappingHelper<T, ?>>();

        for (int fieldIndex = startFieldIndex; fieldIndex <= endFieldIndex; fieldIndex++) {
            PropertyMappingHelper<T, ?> helper = newPropertyHelper(fieldIndex);
            if (helper != null) {
                propertyMappers.put(fieldIndex, helper);
            }

        } // for

        return propertyMappers;
    }


    /**
     * Creates the mapping helper for mapping the field in fieldIndex.
     * Can be used by {@link #getPropertyHelpers(int, int)}.
     */
    @SuppressWarnings("unchecked")
    protected PropertyMappingHelper<T, ?> newPropertyHelper(int fieldIndex) {
        SectionInstance sectionInstance = getSectionInstance();
        List<Field> fields = sectionInstance.getFields();
        Field field = fields.get(fieldIndex);

        String fieldName = field.getAttr("id");
        MappingHelperConfig<?> helperConfig = mappingHelpersConfig.get(fieldName);
        if (helperConfig == null) {
            return null;
        }

        PropertyMappingHelper<T, ?> helper;
        helperConfig.getOptions().put("fieldName", fieldName);

        try {
            helper =
                    (PropertyMappingHelper<T, ?>) ConstructorUtils.invokeConstructor(
                            helperConfig.getHelperClass(),
                            new Object[]{
                                    getStore(), sectionInstance, helperConfig.getOptions(), fieldIndex
                            },
                            new Class<?>[]{BIIObjectStore.class, SectionInstance.class, Map.class, Integer.class}
                    );
        } catch (Exception ex) {
            throw new TabInternalErrorException(
                    "Error while creating the mapper for the property "
                            + fieldName + "/" + helperConfig.getOption("propertyName") + ": " + ex.getMessage(),
                    ex
            );
        }
        return helper;
    }


    @Override
    public List<Integer> getMatchedFieldIndexes() {
        List<Integer> result = new ArrayList<Integer>();
        for (PropertyMappingHelper<?, ?> propertyHelper : getPropertyHelpers().values()) {
            result.addAll(propertyHelper.getMatchedFieldIndexes());
        }
        return result;
    }

    /**
     * Check that all headers have been matched, otherwise it must mean there are headers not belonging to this
     * section.
     * <p/>
     * By default this is used straight before starting the mapping, in the {@link #map()} method. It may
     * be necessary to use the validation methods differently, for instance by another mapper that composes TAB mappers
     * to build the mapping for a whole section (i.e.: see {@link ProcessingsTabMapper}.
     */
    @Override
    public boolean validateHeaders() {
        boolean result = true;
        SectionInstance sectionInstance = getSectionInstance();
        int size = sectionInstance.getFields().size();

        List<Integer> matchedFieldIndexes = getMatchedFieldIndexes();

        for (int i = 0; i < size; i++) {
            if (!matchedFieldIndexes.contains(i) && !Utils.checkIfFieldIsComment(sectionInstance.getField(i))) {

                log.warn(i18n.msg("wrong_field_or_position", sectionInstance.getField(i).getId(), i));
                result = false;
            }
        }
        return result;
    }

    /**
     * Check matched headers cardinality and compares that with what was specified in the field definition (TAB definition XML).
     * <p/>
     * By default this is used straight before starting the mapping, in the {@link #map()} method. It may
     * be necessary to use the validation methods differently, for instance by another mapper that composes TAB mappers
     * to build the mapping for a whole section (i.e.: see {@link ProcessingsTabMapper}.
     */
    public boolean validateCardinality() {
        boolean result = true;
        SectionInstance sectionInstance = getSectionInstance();
        int size = sectionInstance.getFields().size();
        List<Integer> matchedFieldIndexes = getMatchedFieldIndexes();

        // First count all the cardinalities
        //
        Map<String, Integer> cardinalities = new HashMap<String, Integer>();
        for (int i = 0; i < size; i++) {
            // This is checked elsewhere, so don't consider it for the cardinality
            if (!matchedFieldIndexes.contains(i)) {
                continue;
            }

            Field field = sectionInstance.getField(i);
            String key = field.dump().toString();

            Integer cardinality = cardinalities.get(key);
            if (cardinality == null) {
                cardinalities.put(key, 1);
            } else {
                cardinalities.put(key, cardinality + 1);
            }
        }

        // Now compare the collected counts to the corresponding cardinality constraints
        //
        for (int i = 0; i < size; i++) {
            if (!matchedFieldIndexes.contains(i)) {
                continue;
            }

            Field field = sectionInstance.getField(i);
            String key = field.dump().toString();

            for (FieldConstraint constraint : field.getConstraints()) {
                if (!(constraint instanceof FieldCardinalityConstraint)) {
                    continue;
                }
                Integer cardinality = cardinalities.get(key);
                if (cardinality == null) {
                    continue;
                }
                if (!((FieldCardinalityConstraint) constraint).validateCardinality(cardinality, key, log)) {
                    result = false;
                }
                cardinalities.remove(key);
            }
        }

        return result;
    }

}
