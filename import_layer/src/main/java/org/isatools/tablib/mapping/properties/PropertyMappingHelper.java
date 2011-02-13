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

package org.isatools.tablib.mapping.properties;

import org.apache.log4j.Logger;
import org.isatools.tablib.mapping.ClassTabMapper;
import org.isatools.tablib.schema.SectionInstance;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.model.Identifiable;
import uk.ac.ebi.utils.reflection.ReflectionUtils;

import java.util.*;


/**
 * Maps a property from a record's field in a TAB to a class property
 * An helper used in object mappers.
 * <p/>
 * TODO: setProperty should go in setter classes (implements MappingSetter), so that
 * setters would be reusable and duplication could be reduced (StringSetter,
 * CharacteristicValueSetter, ParamValueSetter).
 *
 * @param <T> the object the mapped property belongs to
 * @param <PT> the property mapped by this mapping helper
 */
public abstract class PropertyMappingHelper<T extends Identifiable, PT> {
    private final String propertyName;
    private final String fieldName;
    private final SectionInstance sectionInstance;
    private final BIIObjectStore store;
    private int fieldIndex = -1;
    private final Map<String, String> options;

    protected static final Logger log = Logger.getLogger(PropertyMappingHelper.class);


    /**
     * <b>PLEASE NOTE</b>: a constructor in this form <i>must</i> always be defined if you want initialize
     * a mapper via {@link ClassTabMapper#exportingHelpersConfig}. Moreover, you <b>must</b> call this constructor if
     * you want your options to be later available in #get
     *
     * @param formatSet       the TAB instance I read values from
     * @param sectionInstance the TAB section I read from
     * @param options         <ul>
     *                        <li>["fieldName"] the TAB field I read from.</li>
     *                        <li>["propertyName"] the name of mapped property. For instance it could be used
     *                        in a setter named {@code set<PropertyName>( name )}.</li>
     *                        <li>More options can be accepted and their interpretation depend on the specific helper.</li>
     *                        </ul>
     * @param fieldIndex      the field this mapper gets information from
     */
    public PropertyMappingHelper(
            BIIObjectStore store, SectionInstance sectionInstance, Map<String, String> options,
            int fieldIndex
    ) {
        this.store = store;
        this.sectionInstance = sectionInstance;

        if (options == null) {
            this.fieldName = this.propertyName = null;
            this.options = new HashMap<String, String>();
        } else {
            this.fieldName = options.get("fieldName");
            this.propertyName = options.get("propertyName");
            this.options = options;
        }

        this.fieldIndex = fieldIndex;
    }


    /**
     * Allow for an internal alternative construction
     */
    public PropertyMappingHelper(
            BIIObjectStore store, SectionInstance sectionInstance,
            String fieldName, String propertyName, int fieldIndex
    ) {
        this.store = store;
        this.sectionInstance = sectionInstance;
        this.fieldName = fieldName;
        this.propertyName = propertyName;
        this.fieldIndex = fieldIndex;
        this.options = new HashMap<String, String>();
        options.put("fieldName", fieldName);
        options.put("propertyName", propertyName);
    }


    /**
     * Sets the property this property mapper is concerned with to an existing
     * mapped object. The defatult version just calls {@link #mapProperty(int)} and pass
     * the returned property to {@link #setProperty(Identifiable, Object)}.
     */
    public T map(T mappedObject, int recordIndex) {

        // Map the value
        PT propertyValue = this.mapProperty(recordIndex);

        // Invoke the setter
        this.setProperty(mappedObject, propertyValue);

        // Done
        return mappedObject;
    }


    /**
     * Assigns the property propertyValue to the object mappedObject. The subclass has to specialize the
     * way this is actually done.
     */
    public abstract void setProperty(T mappedObject, PT propertyValue);

    /**
     * Returns a new property value from a string value, which comes from a TAB field. The subclass
     * has to define how such mapping occurs.
     */
    public abstract PT mapProperty(int recordIndex);


    /**
     * The property this helper populates.
     */
    public String getPropertyName() {
        return propertyName;
    }


    /**
     * The name of the field in the TAB this the property value is taken from
     */
    public String getFieldName() {
        return fieldName;
    }


    public SectionInstance getSectionInstance() {
        return sectionInstance;
    }


    /**
     * The store I use to refer objects
     */
    public BIIObjectStore getStore() {
        return store;
    }

    /**
     * This helper gets the value to be mapped from the field located at this position
     */
    public int getFieldIndex() {
        return fieldIndex;
    }


    /**
     * Returns a read-only version of options
     */
    public Map<String, String> getOptions() {
        return Collections.unmodifiableMap(options);
    }


    /**
     * The class bound to PT
     */
    public Class<PT> getMappedPropertyClass() {
        // TODO: caching
        return ReflectionUtils.getTypeArgument(PropertyMappingHelper.class, this.getClass(), 1);
    }

    /**
     * The default implementation returns {@link #getFieldIndex()}
     */
    public List<Integer> getMatchedFieldIndexes() {
        return Arrays.asList(getFieldIndex());
    }

}
