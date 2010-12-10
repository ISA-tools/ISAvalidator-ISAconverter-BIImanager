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


import org.isatools.tablib.exceptions.TabInternalErrorException;
import org.isatools.tablib.parser.ColSectionParser;
import org.isatools.tablib.parser.RowSectionParser;
import org.isatools.tablib.parser.TabParser;

import java.util.List;


/**
 * May 25, 2007
 *
 * @author brandizi
 */
public class Section extends SchemaNode {

    private String header = null;

    public Section() {
    }

    public Section(String id) {
        super(id);
    }

    /**
     * @return getFormat() if the section isn't nested in another section, walk
     *         the tree downstream and return the format the root section belongs to.
     */
    public Format getFormat() {
        return (Format) getParent();
    }


    public void setFormat(Format parent) {
        if (this.getParent() instanceof Section) {
            log.trace(
                    "WARNING, setFormat() is replacing my section's " +
                            "parent with a new parent type, section #" + this.getAttr("id")
            );
        }
        this.setParent(parent);
    }

    public void setParentSection(Section parent) {
        if (this.getParent() instanceof Format) {
            log.trace(
                    "WARNING: setFormat() is replacing my section's " +
                            "parent with a new parent type, section #" + this.getAttr("id")
            );
        }
        this.setParent(parent);
    }


    @SuppressWarnings("unchecked")
    public List<Field> getFields() {
        return (List) getChildren();
    }


    @SuppressWarnings("unchecked")
    public void setFields(List<Field> fields) {
        setChildren((List) fields);
    }

    public void addField(Field field) {
        this.addChild(field);
    }

    /**
     * Lookup a children by id. I always expect that all the nodes at a given level has distinct IDs.
     */
    public Field getField(String id, boolean isCaseSensitive) {
        SchemaNode child = getChild(id);
        if (child == null) {
            return null;
        }
        if (!(child instanceof Field)) {
            throw new TabInternalErrorException("Child #" + id + " is not a Field");
        }
        return (Field) child;
    }

    /**
     * case-sensitive version
     */
    public Field getField(String id) {
        return getField(id, true);
    }

    /**
     * Search a field whose name corresponds to this header, where header may be in the form
     * Name[x](y)
     */
    public Field getFieldByHeader(String header, boolean isCaseSensitive) {
        String bits[] = Field.parseHeaderRawResult(header, isCaseSensitive);

        if (bits == null || bits.length < 2) {
            throw new TabInternalErrorException(
                    "Field.getFieldByHeader(): bad syntax for the header: '" + header + "'"
            );
        }

        String newid = bits[1].trim();

        return getField(newid, isCaseSensitive);
    }

    /**
     * case-sensitive version
     */
    public Field getFieldByHeader(String header) {
        return getFieldByHeader(header, true);
    }

    public String getXmlElementName() {
        return "section";
    }


    /**
     * The label if present, the ID otherwise
     */
    public String getHeader() {
        if (header != null) {
            return header;
        }
        header = getAttr("header");
        if (header == null) {
            header = getId();
        }
        return header;
    }


    /**
     * This is used by TabLoader to load Record(s) from an array of TSV lines.
     * By default returns RowSectionParser or ColSectionParser, depending on the
     * section type.
     */
    public TabParser<SectionInstance> getParser(FormatInstance formatInstance) {
        TabParser<SectionInstance> parser;

        String type = this.getAttr("type");

        if ("many-in-rows".equals(type)) {
            parser = new RowSectionParser(this, formatInstance);
        } else if ("many-in-cols".equals(type) || "one-in-col".equals(type)) {
            parser = new ColSectionParser(this, formatInstance);
        } else {
            throw new TabInternalErrorException("Wrong section type: " + type);
        }

        return parser;
    }

}
