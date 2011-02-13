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

package org.isatools.tablib.parser;

import au.com.bytecode.opencsv.CSVReader;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.isatools.tablib.schema.Format;
import org.isatools.tablib.schema.Section;

import java.io.IOException;
import java.io.Reader;

/**
 * An extension of {@link CSVReader} that does some pre-processing steps on TAB formats.
 * The main function played by this class is the following. For the lines:
 * <p/>
 * <pre>
 *   Design Name abc
 *   Design Type xyz
 * </pre>
 * <p/>
 * If these fields are part of a section defined with "is-header-omitted = true", the actual lines returned by {@link #readNext()} are:
 * <p/>
 * <pre>
 *   &lt;blank line&gt;
 *   Study Design
 *   Design Name abc
 *   Design Type xyz
 *   &lt;blank line&gt;
 * </pre>
 * <p/>
 * where the header line comes from the "header" attribute in the defining XML. Plus, it does a number of other tasks,
 * such as counting the lines read (from the underlining reader).
 * <p/>
 * <p><b>date</b>: Sep 8, 2008</p>
 *
 * @author brandizi
 */
public class TabCsvReader extends CSVReader {
    private static enum Status {
        /**
         * initial state, waiting for omitted-header section, goes to: here, next ADD_BLANK_LINE_TO_HEADER
         */
        NORMAL,
        /**
         * from normal, when a field of a section without header is met, goes to: here, next, ADD_BLANK_LINE_TO_HEADER
         */
        HEADER_OMITTED_SECTION_ENTERED,
        /**
         * from previous, we are inside the section, waiting for the end of section, goes to: here, next
         */
        INSIDE_HEADER_OMITTED_SECTION,
        /**
         * from previous, section is finished, we need to output a blank line, goes to: NORMAL
         */
        HEADER_OMITTED_SECTION_EXITED,
        /**
         * from NORMAL, we need to add a blank line before an header, we need to add the header itself and continue,
         * goes to: here, NORMAL
         */
        ADD_BLANK_LINE_TO_HEADER
    }

    private final Format format;
    private Status status = Status.NORMAL;
    private String[] lastRead = null;
    private String[] generatedHeader = null;

    private int lastReadLineIndex = -1;

    protected static final Logger log = Logger.getLogger(TabCsvReader.class);


    public TabCsvReader(Reader reader, Format format) {
        super(reader, '\t', '"', 0);
        this.format = format;
    }


    @Override
    public String[] readNext() throws IOException {
        String[] result = null;

        log.trace("TabCsvReader, current status: " + status);

        switch (status) {
            case NORMAL:
                // Keep reading until we meet the field of a section that has no header
                String[] next = super.readNext();
                lastReadLineIndex++;

                if (isCommentLine(next)) {
                    result = next;
                } else {
                    String header = next != null && next.length > 0 ? StringUtils.trimToNull(next[0]) : null;
                    Section section = header == null ? null : format.getSectionByField(header);
                    if (section != null) {
                        if ("true".equals(section.getAttr("is-header-omitted"))) {
                            // If we are in such a section, return \n and a generated header
                            result = new String[0];
                            generatedHeader = new String[]{section.getAttr("header")};
                            status = Status.HEADER_OMITTED_SECTION_ENTERED;
                        } else {
                            result = next;
                        }
                    } else if (TabCsvReader.isHeaderLine(next) && format.getSectionByHeader(header, false) != null) {
                        // If we have a section header, first return a blank line (which can be omitted), then return
                        // the header itself. You can continue with omitted-header case
                        //
                        result = new String[0];
                        status = Status.ADD_BLANK_LINE_TO_HEADER;
                    } else {
                        result = next;
                    }
                }
                // Actual data line will be returned after the generated header
                lastRead = next;
                break;

            case HEADER_OMITTED_SECTION_ENTERED:
                // We fall here after having entered a section without header,
                // if we still have to return the generated header, do it
                if (generatedHeader != null) {
                    result = generatedHeader;
                    generatedHeader = null;
                } else {
                    // otherwise return the line we kept and go ahead in normal status
                    result = lastRead;
                    status = Status.INSIDE_HEADER_OMITTED_SECTION;
                }
                break;

            case INSIDE_HEADER_OMITTED_SECTION:
                next = super.readNext();
                lastReadLineIndex++;
                // We stay inside the header-omitted section until we meet a blank line, or the header of the next section,
                // or a field which does not belong to the current header-omitted section
                if (!isCommentLine(next)
                        && (isBlankLine(next) || isHeaderLine(next) || format.getSectionByField(next[0]) != null)
                        )
                // we have another section, go to the closing state
                {
                    status = Status.HEADER_OMITTED_SECTION_EXITED;
                }

                // Returns the line inside the header-omitted section in any case
                result = next;
                lastRead = next;
                break;

            case HEADER_OMITTED_SECTION_EXITED:
                // If we are closing a header-omitted section, return a blank line and then go back to normal model
                result = new String[0];
                status = Status.NORMAL;
                break;

            case ADD_BLANK_LINE_TO_HEADER:
                result = lastRead;
                status = Status.NORMAL;

                // no other case possible
        }

        log.trace(
                "TabCsvReader, new status: " + status + ", last read: " + ArrayUtils.toString(lastRead)
                        + ", result: " + ArrayUtils.toString(result)
        );
        return result;
    }

    /**
     * @return The index (real index in the underlining reader) of the last read line.
     */
    public int getLastReadLineIndex() {
        return lastReadLineIndex;
    }


    /**
     * Helpers for CSV parsing
     */
    public static boolean isBlankLine(String[] line) {
        if (line == null) {
            return true;
        }
        for (String value : line) {
            if (StringUtils.trimToNull(value) != null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Helpers for CSV parsing
     */
    public static boolean isHeaderLine(String[] line) {
        if (line == null || line.length == 0) {
            return false;
        }
        if (StringUtils.trimToNull(line[0]) == null) {
            return false;
        }
        for (int icol = 1; icol < line.length; icol++) {
            if (StringUtils.trimToNull(line[icol]) != null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Helpers for CSV parsing
     */
    public static boolean isCommentLine(String[] line) {
        if (line == null || line.length == 0) {
            return false;
        }
        String line0 = line[0];
        return line0 != null && line0.startsWith("#");
    }
}
