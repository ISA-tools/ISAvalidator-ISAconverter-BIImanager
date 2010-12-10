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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.isatools.tablib.exceptions.TabInternalErrorException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * An instance of a Section in a TAB format.
 * <p/>
 * Feb 21, 2008
 *
 * @author brandizi
 */
public class SectionInstance implements FormatInstanceComponent {
    private final Section section;
    private FormatInstance parent;
    private final List<Field> fields = new ArrayList<Field>();
    private final List<Record> records = new ArrayList<Record>();
    private int startingLine = -1;

    protected static final Logger log = Logger.getLogger(SectionInstance.class);

    public SectionInstance(Section section) {
        if (section == null) {
            throw new TabInternalErrorException("Cannot have a section instance of a null section");
        }
        this.section = section;
    }

    public SectionInstance(Section section, FormatInstance parent) {
        this(section);
        setParent(parent);
    }

    public Section getSection() {
        return section;
    }

    public FormatInstance getParent() {
        return parent;
    }

    public void setParent(FormatInstance parent) {
        if (parent == null) {
            throw new TabInternalErrorException("Cannot link a section instance to a null parent, section: " + section.getId());
        }
        this.parent = parent;
    }


    public List<Record> getRecords() {
        return Collections.unmodifiableList(records);
    }

    public void addRecord(Record record) {
        if (record == null) {
            throw new TabInternalErrorException("Cannot add a null record to the section " + section.getId());
        }

        SectionInstance sectionInstance = record.getParent();

        if (sectionInstance != null && sectionInstance != this) {
            throw new TabInternalErrorException(
                    "Attempt to add a record to a section other than the one stored by the instance: "
                            + "section: " + section.getAttr("id") + " record's section: " + record.getSection().getAttr("id")
            );
        }
        records.add(record);
        if (sectionInstance == null) {
            record.setParent(sectionInstance);
        }
    }


    public Record getRecord(int index) {
        return getRecords().get(index);
    }

    /**
     * a wrapper of {@link Record#get(int)}
     */
    public Object getValue(int recordIndex, int fieldIndex) {
        return getRecord(recordIndex).get(fieldIndex);
    }

    /**
     * a wrapper of {@link Record#getString(int)}
     */
    public String getString(int recordIndex, int fieldIndex) {
        return getRecord(recordIndex).getString(fieldIndex);
    }

    /**
     * A wrapper which uses {@link #getRecord(int)}, {@link #getField(String, int)} and {@link Record#get(int)}
     */
    public Object getValue(int recordIndex, String fieldName, int searchFrom) {
        Field field = getField(fieldName, searchFrom);
        if (field == null) {
            throw new TabInternalErrorException("Field '" + fieldName + "' not found after " + searchFrom + " position");
        }
        Record record = getRecord(recordIndex);
        return record.get(field.getIndex());
    }

    /**
     * A wrapper with searchFrom = -1
     */
    public Object getValue(int recordIndex, String fieldName) {
        return getValue(recordIndex, fieldName, -1);
    }


    /**
     * A wrapper which uses {@link #getRecord(int)}, {@link #getField(String, int)} and {@link Record#getString(int)}
     */
    public String getString(int recordIndex, String fieldName, int searchFrom) {
        Field field = getField(fieldName, searchFrom);
        if (field == null) {
            throw new TabInternalErrorException(
                    "Field '" + fieldName + "' not found after " + searchFrom + " position, in section " + getSection().getId()
            );
        }
        Record record = getRecord(recordIndex);
        return record.getString(field.getIndex());
    }

    /**
     * A wrapper with searchFrom = -1
     */
    public String getString(int recordIndex, String fieldName) {
        return getString(recordIndex, fieldName, -1);
    }

    /**
     * A wrapper which uses {@link #getRecord(int)}, {@link #getField(String, int)} and {@link Record#getString(int)} and
     * does not complain if the field doesn't exist. WARNING: do not use this by default, you'll it will be *very* difficult
     * to trace this kind of error otherwise.
     */
    public String getStringUnchecked(int recordIndex, String fieldName, int searchFrom) {
        Field field = getField(fieldName, searchFrom);
        if (field == null) {
            log.trace(
                    "WARNING: Field '" + fieldName + "' not found after " + searchFrom + " position, in section " + getSection().getId()
                            + ", getStringUnchecked() is returing null"
            );
            return null;
        }
        Record record = getRecord(recordIndex);
        return record.getString(field.getIndex());
    }

    /**
     * A wrapper with searchFrom = -1. WARNING: do not use this by default, you'll it will be *very* difficult
     * to trace this kind of error otherwise.
     */
    public String getStringUnchecked(int recordIndex, String fieldName) {
        return getStringUnchecked(recordIndex, fieldName, -1);
    }


    public List<Field> getFields() {
        return Collections.unmodifiableList(fields);
    }

    public Field getField(int index) {
        if (fields == null) {
            return null;
        }
        if (index < 0 || index >= fields.size()) {
            return null;
        }

        return this.fields.get(index);
    }

    /**
     * Gets the next field located after the specified index
     */
    public Field getField(String id, int searchFrom) {
        if (id == null) {
            throw new TabInternalErrorException(
                    "Section.getField( id, pos ): id is null, Section: '" + this.section.getAttr("id") + "'"
            );
        }

        for (int i = searchFrom + 1; i < fields.size(); i++) {
            Field field = fields.get(i);
            if (id.equals(field.getAttr("id"))) {
                return field;
            }
        }
        return null;
    }

    /**
     * A wrapper of {@link #getField(String, int) getField ( id, -1 )}
     */
    public Field getField(String id) {
        return getField(id, -1);
    }

    /**
     * Gets a field by header, searching from a certain position, either in case-sensitive or
     * case-insensitive fashion.
     */
    public Field getFieldByHeader(String header, int searchFrom, boolean isCaseSensitive) {
        if (header == null) {
            throw new TabInternalErrorException(
                    "Section.getFieldByHeader():  header is null, Section: '" + this.section.getAttr("id") + "'"
            );
        }
        if (fields == null) {
            return null;
        }

        for (int i = searchFrom + 1; i < fields.size(); i++) {
            Field field = fields.get(i);
            if (isCaseSensitive && header.equals(field.getAttr("header"))
                    || header.equalsIgnoreCase(field.getAttr("header"))) {
                return field;
            }
        }
        return null;
    }

    /**
     * case-sensitive version
     */
    public Field getFieldByHeader(String header, int searchFrom) {
        return getFieldByHeader(header, searchFrom, true);
    }

    /**
     * Searches from the beginning in case-sensitive fashion
     */
    public Field getFieldByHeader(String header) {
        return getFieldByHeader(header, -1, true);
    }

    /**
     * Searches from the beginning
     */
    public Field getFieldByHeader(String header, boolean isCaseSensitive) {
        return getFieldByHeader(header, -1, isCaseSensitive);
    }

    /**
     * Sets up a Field, using its index as index inside the Section
     * <p/>
     * TODO: Check it matches the fields
     */
    public void setField(Field field) {
        if (field == null) {
            throw new TabInternalErrorException(String.format(
                    "null field passed to Section.setRealField(), section: '%s'",
                    this.section.getAttr("id")
            ));
        }

        int index = field.getIndex();
        if (index == -1) {
            throw new TabInternalErrorException(String.format(
                    "Illegal field passed to Section.setRealField(), section: '%s', field: '%s'",
                    this.section.getAttr("id"),
                    field.getAttr("id")
            ));
        }

        while (fields.size() - 1 < index) {
            fields.add(null);
        }

        fields.set(index, field);
    }


    /**
     * Sets up the field properly before adding the field to the instance
     */
    public void addField(Field field) {
        if (field == null) {
            throw new TabInternalErrorException("Cannot add a null field to the section: " + section.getId());
        }

        if (field.getSection() != this.section) {
            throw new TabInternalErrorException(
                    "Attempt to add a format instance to a parent other than the one stored by the instance: "
                            + "section: " + field.getAttr("id")
            );
        }

        if (fields == null) {
            field.setIndex(0);
        } else {
            field.setIndex(fields.size());
        }

        setField(field);
    }


    /**
     * A wrapper of {@link Section#getId()}
     */
    public String getSectionId() {
        return getSection().getId();
    }


    /**
     * A wrapper of {@link #getParent()}.{@link FormatInstance#getFileId() getFileId()}
     */
    public String getFileId() {
        return getParent().getFileId();
    }


    /**
     * The line index in the original file where this section instance starts (where the header is)
     */
    public int getStartingLine() {
        return startingLine;
    }

    public void setStartingLine(int startingLine) {
        this.startingLine = startingLine;
    }


    /**
     * The no. of records
     */
    public int size() {
        return records.size();
    }


    private StringBuilder dumpSectionHeader() {
        StringBuilder result = new StringBuilder();
        Section section = getSection();
        if ("true".equals(section.getAttr("is-exported-header-omitted"))) {
            return result;
        }
        String header = StringUtils.trimToNull(getSection().getAttr("header"));
        if (header != null) {
            result.append('"').append(header).append("\"\n");
        }
        return result;
    }


    private StringBuilder dumpHeadersOverRow() {
        StringBuilder result = dumpSectionHeader();

        String sep = "";
        for (Field field : getFields()) {
            result.append(sep).append("\"").append(field.dump()).append("\"");
            sep = "\t";
        }
        result.append("\n");
        return result;
    }

    /**
     * Dumps "many-in-rows" sections
     */
    private StringBuilder dumpOverRows() {
        StringBuilder result = dumpHeadersOverRow();
        for (Record record : getRecords()) {
            if (record.isNull()) {
                continue;
            }
            int recordSize = record.size();
            String sep = "";
            for (int i = 0; i < recordSize; i++) {
                result.append(sep).append('"').append("").append(
                        StringUtils.trimToEmpty(record.getString(i))
                ).append('"');
                sep = "\t";
            }
            result.append("\n");
        }
        return result;
    }


    /**
     * Dumps "many-in-cols" and "one-in-col" sections
     */
    private StringBuilder dumpOverColumns() {

        StringBuilder result = dumpSectionHeader();

        for (Field field : getFields()) {
            result.append('"').append(field.dump()).append('"');
            for (Record record : getRecords()) {
                if (record.isNull()) {
                    continue;
                }
                result.append("\t\"").append(
                        StringUtils.trimToEmpty(record.getString(field.getIndex()))
                ).append('"');
            }
            result.append("\n");
        }
        return result;
    }

    /**
     * Dumps the section in TSV format
     */
    public StringBuilder dump() {
        String type = getSection().getAttr("type");
        StringBuilder result = "one-in-col".equals(type) || "many-in-cols".equals(type)
                ? dumpOverColumns()
                : dumpOverRows();
        result.append("\n");
        return result;
    }

}
