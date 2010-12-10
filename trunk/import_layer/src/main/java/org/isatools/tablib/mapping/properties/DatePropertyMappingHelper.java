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

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.isatools.isatab.commandline.permission_manager.PermissionManager;
import org.isatools.tablib.exceptions.TabInternalErrorException;
import org.isatools.tablib.schema.SectionInstance;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.model.Identifiable;
import uk.ac.ebi.bioinvindex.utils.i18n;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;

/**
 * Maps a date property, assuming a setter in the form set&lt;{@link #getPropertyName()}&gt;() exists.
 * <p/>
 * TODO: an option for valid formats. At the moment {@link #VALID_FORMATS} is used
 * <p/>
 * Jan 28, 2008
 *
 * @author brandizi
 */
public class DatePropertyMappingHelper<T extends Identifiable>
        extends PropertyMappingHelper<T, Date> {
    /**
     * TODO: Because of problems with Log4j, this array is duped in {@link PermissionManager#VALID_DATE_FORMATS}
     */
    public final static String[] VALID_FORMATS = new String[]{"yyyy-MM-dd", "dd-MM-yyyy", "dd/MM/yyyy"};

    public DatePropertyMappingHelper(
            BIIObjectStore store, SectionInstance sectionInstance,
            Map<String, String> options, int fieldIndex) {
        super(store, sectionInstance, options, fieldIndex);
    }


    public DatePropertyMappingHelper(
            BIIObjectStore store, SectionInstance sectionInstance,
            String fieldName, String propertyName, int fieldIndex) {
        super(store, sectionInstance, fieldName, propertyName, fieldIndex);
    }


    /**
     * Simply returns the value found at the field {@link #getFieldIndex()}, after having trimmed it.
     *
     * @see org.isatools.tablib.mapping.properties.PropertyMappingHelper#mapProperty(int)
     */
    @Override
    public Date mapProperty(int recordIndex) {
        SectionInstance sectionInstance = getSectionInstance();
        String tabValue = sectionInstance.getString(recordIndex, getFieldIndex());
        String dates = StringUtils.trimToNull(tabValue);
        if (dates == null) {
            return null;
        }
        Date result = null;
        try {
            result = DateUtils.parseDate(dates, VALID_FORMATS);
        } catch (ParseException e) {
            log.warn(i18n.msg("invalid_date_format", dates, sectionInstance.getField(getFieldIndex()).getId()));
        }
        return result;
    }


    /**
     * Maps a value in the TAB into the target property of this helper, fills the object with the
     * mapped value.
     * <p/>
     * This version assumes a standard setter exists, i.e.:
     * mappedObject.set&lt;{@link #getPropertyName()}&gt;.
     * It does not map anything if propertyValue is null.
     */
    public void setProperty(T mappedObject, Date propertyValue) {
        if (propertyValue == null) {
            return;
        }

        try {
            PropertyUtils.setProperty(mappedObject, getPropertyName(), propertyValue);
        }
        catch (Exception ex) {
            throw new TabInternalErrorException(
                    String.format("Problem while mapping property '%s' from class '%s': %s",
                            getPropertyName(), mappedObject.getClass().getSimpleName(), ex.getMessage()
                    ),
                    ex
            );
        }
        log.trace("Property " + getFieldName() + "/'" + propertyValue + "' mapped to object " + mappedObject);
    }

}
