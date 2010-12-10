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

package org.isatools.tablib.schema;

import org.apache.log4j.Logger;
import org.isatools.tablib.exceptions.TabInternalErrorException;
import uk.ac.ebi.utils.collections.ListUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * June 4, 2007
 *
 * @author brandizi
 */
public class Record implements TabInstanceEntity {
    private SectionInstance parent;
    private final List<Object> values = new ArrayList<Object>();


    protected static final Logger log = Logger.getLogger(Record.class);

    public Record() {
    }

    public Record(SectionInstance sectionInstance) {
        this.setParent(sectionInstance);
    }


    public SectionInstance getParent() {
        return parent;
    }

    public void setParent(SectionInstance sectionInstance) {
        if (sectionInstance == null) {
            throw new TabInternalErrorException("cannot link a null section instance to a record");
        }
        this.parent = sectionInstance;
    }


    public Section getSection() {
        if (parent == null) {
            return null;
        }
        return parent.getSection();
    }


    /**
     * Gets the value of a field, retrieving it by means of the index in the section
     * the field belongs to (like an array index).
     */
    public Object get(int index) {
        if (values == null) {
            log.trace(String.format("Record.getStr (%u, value), no values inside the Record", index));
            return null;
        }

        checkRealFields(true, index);

        return ListUtils.get(values, index);
    }

    /**
     * Like get, converts the value to string
     */
    public String getString(int index) {
        return (String) get(index);
    }

    /**
     * Sets the value of a field, using the field's index in the section
     * the field belongs to (like an array index).
     */
    public void set(int index, Object value) {
        checkRealFields(true, index);

        if (index < 0) {
            throw new TabInternalErrorException("Record.set(pos, value), pos " + index + " is invalid");
        }

        // Fill with blanks until your position
        while (values.size() - 1 < index) {
            values.add(null);
        }

        values.set(index, value);
    }


    /**
     * Check if all the values that corresponds to valid realFields are null,
     * i.e.: check the record is null.
     */
    public boolean isNull() {
        this.checkRealFields(false, 0);
        if (values == null) {
            return true;
        }
        for (Object v : values) {
            if (v != null) {
                return false;
            }
        }
        return true;
    }

    /**
     * The number of columns/fields
     */
    public int size() {
        return values.size();
    }


    public String toString() {
        String rval = "";
        if (values == null) {
            return rval;
        }

        String sep = "";
        Section section = getSection();
        if (section == null) {
            return rval;
        }
        List<Field> fields = parent.getFields();

        for (Field field : fields) {
            if (field == null) {
                continue;
            }
            String header = field.getAttr("header");
            if (header == null) {
                header = field.getAttr("id");
            }
            int index = field.getIndex();
            rval += sep + String.format("'%s'(%d): '%s'", header, index, this.get(index));
            sep = "\t";
        }

        return rval;
    }

    /**
     * Checks that the section instance associated to this record has real fields set, if checkIdx is true, only
     * checks for fields existence. Throws exceptions in case of inconsistency.
     */
    private void checkRealFields(boolean checkIdx, int index) {
        List<Field> fields = parent.getFields();

        if (parent == null) {
            throw new TabInternalErrorException("Cannot set/add fileds to null records");
        }

        Section section = parent.getSection();

        if (fields == null) {
            throw new TabInternalErrorException("Real fields for the section '" + section.getAttr("id") + "' still not set");
        }

        if (checkIdx && index < 0 && index >= fields.size()) {
            throw new TabInternalErrorException(String.format(
                    "Cannot handle position %u for the record in section '%s', index out of bounds",
                    index,
                    section.getAttr("id")
            ));
        }
    }


}
