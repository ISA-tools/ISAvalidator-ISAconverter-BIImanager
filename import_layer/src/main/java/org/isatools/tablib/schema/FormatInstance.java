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

import java.util.*;

/**
 * An instance of a TAB format
 * <p/>
 * <p><b>date</b>: Mar 3, 2008</p>
 *
 * @author brandizi
 */
public class FormatInstance implements FormatInstanceEntity {
    private final Format format;
    private FormatSetInstance parent = null;
    private List<SectionInstance> sectionInstances = new ArrayList<SectionInstance>();
    private String fileId;

    private StringBuilder head = new StringBuilder(), tail = new StringBuilder();

    public FormatInstance(Format format) {
        if (format == null) {
            throw new TabInternalErrorException("Cannot have a format set instance of a null format");
        }
        this.format = format;
    }

    public FormatInstance(Format format, FormatSetInstance parent) {
        this(format);
        this.setParent(parent);
    }

    public Format getFormat() {
        return format;
    }

    public FormatSetInstance getParent() {
        return parent;
    }

    public void setParent(FormatSetInstance parent) {
        if (parent == null) {
            throw new TabInternalErrorException("Cannot link a FormatInstance to a null parent, format: " + format.getId());
        }
        this.parent = parent;
    }

    public List<SectionInstance> getSectionInstances() {
        return Collections.unmodifiableList(sectionInstances);
    }

    /**
     * Sorts the section instances according to the priority attribute defined in the format's XML
     * TODO: for the moment does not work with the sections belonging to a section block, cause the final
     * format instance doesn't see the latter.
     */
    public Collection<SectionInstance> getSortedSectionInstances() {
        SortedMap<Integer, SectionInstance> result = new TreeMap<Integer, SectionInstance>();
        int i = 0;
        for (SectionInstance sectionInstance : getSectionInstances()) {
            Section section = sectionInstance.getSection();
            String priorityStr = section.getAttr("export-priority");
            int priority = priorityStr == null ? i : -new Integer(priorityStr);
            result.put(priority, sectionInstance);
            i++;
        }
        return result.values();
    }


    public void addSectionInstance(SectionInstance instance) {
        if (instance == null) {
            throw new TabInternalErrorException("Cannot add a null section instance to the format instance " + format.getId());
        }

        FormatInstance parent = instance.getParent();
        if (parent != null && parent != this) {
            throw new TabInternalErrorException(
                    "Attempt to add a section instance to a format instance other than the one stored by the section: "
                            + "section: " + instance.getSection().getAttr("id")
            );
        }
        sectionInstances.add(instance);
        if (parent == null) {
            instance.setParent(this);
        }
    }


    /**
     * Gets the first section instance having the parameter id
     */
    public SectionInstance getSectionInstance(String sectionId) {
        for (SectionInstance instance : getSectionInstances()) {
            if (sectionId.equals(instance.getSection().getId())) {
                return instance;
            }
        }
        return null;
    }

    /**
     * Gets the instances of a particular section
     */
    public List<SectionInstance> getSectionInstances(String sectionId) {
        List<SectionInstance> result = new ArrayList<SectionInstance>();
        for (SectionInstance instance : getSectionInstances()) {
            if (sectionId.equals(instance.getSection().getId())) {
                result.add(instance);
            }
        }
        return result;
    }


    /**
     * In case it is needed, you may set an ID for the file this instance comes from. It should be
     * without path and without extension. This is used for assay files in the ISATAB format
     */
    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileId() {
        return this.fileId;
    }

    /**
     * This is printed on the top of the spreadsheet, before dumping all the sections
     */
    public StringBuilder getHead() {
        return head;
    }

    /**
     * This is printed on the top of the spreadsheet, before dumping all the sections
     */
    public void setHead(StringBuilder head) {
        this.head = head;
    }

    /**
     * Allows to add a single line (splitted into columns)
     */
    private void addLine(StringBuilder sb, String... columns) {
        char sep = '\0';
        for (String icol : columns) {
            if (sep != '\0') {
                sb.append(sep);
                sep = '\t';
            }
            sb.append(icol);
        }
        if (sep != '\0') {
            sb.append("\n");
        }

    }


    /**
     * Allows to add a single line (splitted into columns) to {@link #getHead()}
     */
    public void addHeadLine(String... columns) {
        addLine(head, columns);
    }

    /**
     * Adds a single-cell line with '#' followed by the parameter comment
     */
    private void addComment(StringBuilder sb, String comment) {
        sb.append("#").append(comment).append("\n");
    }

    /**
     * Adds ({@link #getHead()}) a single-cell line with '#' followed by the parameter comment
     */
    public void addHeadComment(String comment) {
        addComment(head, comment);
    }

    /**
     * This is printed appended to the spreadsheet, after the dump of all the sections
     */
    public StringBuilder getTail() {
        return tail;
    }

    /**
     * This is printed appended to the spreadsheet, after the dump of all the sections
     */
    public void setTail(StringBuilder tail) {
        this.tail = tail;
    }

    /**
     * Allows to add a single line (splitted into columns) to {@link #getTail()}
     */
    public void addTailLine(String... columns) {
        addLine(tail, columns);
    }

    /**
     * Adds (to {@link #getTail()}) a single-cell line with '#' followed by the parameter comment
     */
    public void addTailComment(String comment) {
        addComment(tail, comment);
    }


    /**
     * Dumps the format instance in TSV format
     */
    public StringBuilder dump() {
        StringBuilder result = new StringBuilder();
        result.append(head);

        for (SectionInstance sectionInstance : getSortedSectionInstances()) {
            result.append(sectionInstance.dump());
        }

        result.append(tail);
        return result;
    }
}
