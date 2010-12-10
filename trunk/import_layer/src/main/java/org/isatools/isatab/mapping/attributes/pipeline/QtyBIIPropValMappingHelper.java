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
import org.isatools.tablib.mapping.MappingUtils;
import org.isatools.tablib.mapping.properties.PropertyMappingHelper;
import org.isatools.tablib.schema.Field;
import org.isatools.tablib.schema.SectionInstance;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.model.Identifiable;
import uk.ac.ebi.bioinvindex.model.term.Property;
import uk.ac.ebi.bioinvindex.model.term.PropertyValue;
import uk.ac.ebi.bioinvindex.model.term.Unit;
import uk.ac.ebi.bioinvindex.model.term.UnitValue;
import uk.ac.ebi.utils.reflection.ReflectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Maps a property value which is a quantity and has possibly a unit.
 * <p><b>PLEASE NOTE</b>: expects a constructor with {@link #PT} for {@link #PVT}.</p>
 * <p/>
 * This class is abstract because of
 * <a href = "http://www.artima.com/weblogs/viewpostP.jsp?thread=208860">this</a>.
 * <p/>
 * <p><b>date</b>: Jan 9, 2008</p>
 *
 * @author brandizi
 */
public abstract class QtyBIIPropValMappingHelper
        <T extends Identifiable, PT extends Property<PVT>, PVT extends PropertyValue<PT>>
        extends PropertyMappingHelper<T, PVT> {
    /**
     * The field range where this source is represented
     */
    private PT type;

    protected final MappingUtils mappingUtils;

    private Field right2Field;
    private PropValUnitMappingHelper<PVT> unitOntologyMapper;


    public QtyBIIPropValMappingHelper(
            BIIObjectStore store, SectionInstance sectionInstance, int fieldIndex, PT type) {
        super(store, sectionInstance, "Unit", null, fieldIndex);
        this.type = type;
        mappingUtils = new MappingUtils(store);
    }


    @SuppressWarnings("unchecked")
    public PVT mapProperty(int recordIndex) {

        SectionInstance sectionInstance = getSectionInstance();
        int startFieldIndex = getFieldIndex();

        // instantiate a new property value and setup its name
        //
        String valueStr = StringUtils.trimToNull(sectionInstance.getString(recordIndex, startFieldIndex));
        if (valueStr == null) {
            return null;
        }

        PVT value;
        try {
            value = (PVT) ConstructorUtils.invokeConstructor(getMappedPropertyValueClass(), type);
        } catch (Exception ex) {
            throw new TabInternalErrorException("Error in creating new property value, message: " + ex.getMessage(), ex);
        }
        value.setValue(valueStr);

        // Get the unit from the Unit column
        //   if there is an ontology attribute mapper, then there is the source header.
        PropValUnitMappingHelper<PVT> unitOntologyMapper = getUnitOntologyMapper();
        if (unitOntologyMapper != null)
        // The unit has a term source header, let's map it with the ontology term mapper
        {
            unitOntologyMapper.map(value, recordIndex);
        } else {
            // Otherwise it is plain text only
            //
            String unitValueStr = StringUtils.trimToEmpty(sectionInstance.getString(recordIndex, startFieldIndex + 1));
            // TODO: Remap the unit type (in the persister?!)
            UnitValue unitValue = new UnitValue(unitValueStr, null);
            value.setUnit(unitValue);
        }

        if (valueStr.length() == 0) {
            UnitValue unitValue = value.getUnit();
            String uvalStr;
            if (unitValue != null) {
                uvalStr = StringUtils.trimToNull(unitValue.getValue());
                Unit type = unitValue.getType();
                if (uvalStr == null && type != null) {
                    uvalStr = StringUtils.trimToNull(type.getValue());
                }
                if (uvalStr != null) {
                    log.trace("WARNING: I have an attribute with the unit '" + uvalStr
                            + "' but without a value, accepting it, hope it's fine...");
                }
            } else {
                return null;
            }
        }

        return value;
    }


    public Class<PVT> getMappedPropertyValueClass() {
        // TODO: caching
        return ReflectionUtils.getTypeArgument(QtyBIIPropValMappingHelper.class, this.getClass(), 2);
    }


    /**
     * Returns the proper mapper in case we have the header for unit ontology source
     */
    protected PropValUnitMappingHelper<PVT> getUnitOntologyMapper() {
        if (unitOntologyMapper != null) {
            return unitOntologyMapper;
        }
        Field right2Field = getRight2Field();
        String right2Header = right2Field == null ? null : right2Field.getAttr("id");
        if (BIIPropertyValueMappingHelper.TERM_SOURCE_HEADER.equals(right2Header)) {
            unitOntologyMapper = new PropValUnitMappingHelper<PVT>(
                    getStore(), getSectionInstance(), getFieldIndex() + 1
            );
        }
        return unitOntologyMapper;

    }


    /**
     * The field located 2 positions next to startFieldIndex.
     */
    protected Field getRight2Field() {
        if (right2Field != null) {
            return right2Field;
        }
        return right2Field = getSectionInstance().getField(getFieldIndex() + 2);
    }


    @Override
    public List<Integer> getMatchedFieldIndexes() {
        PropValUnitMappingHelper<PVT> unitOntologyMapper = getUnitOntologyMapper();
        int fieldIndex = getFieldIndex();
        if (unitOntologyMapper == null) {
            return Arrays.asList(fieldIndex, fieldIndex + 1);
        }

        List<Integer> result = new ArrayList<Integer>();
        result.addAll(Arrays.asList(fieldIndex, fieldIndex + 1));
        result.addAll(unitOntologyMapper.getMatchedFieldIndexes());
        return result;
    }

}