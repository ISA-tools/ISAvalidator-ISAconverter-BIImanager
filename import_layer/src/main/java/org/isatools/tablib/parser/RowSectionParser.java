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

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.isatools.tablib.exceptions.TabInternalErrorException;
import org.isatools.tablib.exceptions.TabValidationException;
import org.isatools.tablib.schema.*;
import uk.ac.ebi.bioinvindex.utils.i18n;

import java.util.Arrays;
import java.util.List;

/**
 * Maps a section with values spanned over rows, in a traditional relational table fashion.
 *
 * @author brandizi
 */
public class RowSectionParser implements TabParser<SectionInstance> {

    protected SectionInstance sectionInstance;
    protected static final Logger log = Logger.getLogger(RowSectionParser.class);


    public RowSectionParser(Section section, FormatInstance formatInstance) {
        this.sectionInstance = new SectionInstance(section, formatInstance);
    }


    /**
     * Parses a CSV header line and creates the corresponding resulting schema in the section
     * (i.e.: a set of fields with positions)
     */
    protected List<Field> parseHeaders(List<String[]> csvLines) {
        Section section = sectionInstance.getSection();
        String sectionId = section.getId();

        String headers[] = csvLines.get(0);
        log.trace("Headers for section " + sectionId + ": " + Arrays.toString(headers));

        int nlines = csvLines.size();

        for (int j = 0; j < headers.length; j++) {
            String header = StringUtils.trimToNull(headers[j]);

            if (header == null) {
                // Empty header, let's remove all!
                //
                log.warn(i18n.msg("empty_column", j));

                for (int i = 0; i < nlines; i++) {
                    String[] line = csvLines.get(i);
                    if (j == line.length)
                    // This happens sometime: the header has one extra-tab and the lines are OK
                    // we need to recraft the header only in that case
                    {
                        continue;
                    }
                    csvLines.set(i, (String[]) ArrayUtils.remove(line, j));
                }
                headers = csvLines.get(0);

                j--; // Restart from this point, which is now the next column
                continue;
            }
            log.trace("Working on header " + j + ":" + header);

            Field field = section.getFieldByHeader(header, false);
            if (field == null) {
                throw new TabValidationException(i18n.msg("unexpected_field_in_section_error", header, sectionId));
            }

            // Let's add a new real field on the basis of the header. The new field created gets its id from the
            // original one in the schema, so it will have the "canonical" form, independently on what we found
            // on the input (e.g.: upper case).
            //
            field = field.parseHeader(header, j, false);
            sectionInstance.addField(field);
        }
        return sectionInstance.getFields();
    }


    public SectionInstance parseCsvLines(List<String[]> csvLines) {
        return parseCsvLines(csvLines, 0, csvLines.size() - 1, 0, -1);
    }

    public SectionInstance parseCsvLines(List<String[]> csvLines, int fromRow, int toRow, int fromCol, int toCol) {
        Section section = sectionInstance.getSection();
        String sectionId = section.getId();
        String fileId = sectionInstance.getParent().getFileId();

        log.trace(String.format("___ Parsing 'lines' of Section: %s ('%s', %d, %d, %d, %d) ___\n",
                sectionId, fileId, fromRow, toRow, fromCol, toCol));

        if (csvLines == null || csvLines.size() == 0) {
            log.trace("WARNING, The CSV lines are empty, section: " + sectionId);
            return sectionInstance;
        }

        if (toCol == -1) {
            toCol = csvLines.get(0).length - 1;
        }
        if (fromRow == 0) {
            fromRow = 1;
        }

        if (toRow - fromRow < 0 || toCol - fromCol < 0) {
            log.trace("WARNING, The specified window is empty, section " + sectionId);
            return sectionInstance;
        }

        List<Field> fields = parseHeaders(csvLines);
        toCol = Math.min(toCol, fields.size() - 1);

        // Let's go over the lines
        //
        for (int i = fromRow; i <= toRow; i++) {
            String[] line = csvLines.get(i);
            if (line == null) {
                log.debug("Line #" + i + " is null, skipping");
                continue;
            }
            // Let's go along the headers
            //
            Record record = new Record(sectionInstance);
            for (int icol = fromCol; icol <= toCol; icol++) {
                // Set up the current record's value
                //
                Field field = sectionInstance.getField(icol);
                if (field == null) {
                    throw new TabInternalErrorException(i18n.msg("generic_field_error", icol, section.getId(), i));
                }
                if (icol >= line.length) {
                    log.debug("The (logical) line #" + i + " is too short, ignoring all from col #" + icol);
                    break;
                }
                String value = line[icol];

                record.set(icol, value);
            }

            // Let's add the record, provided that it is not empty
            if (record.isNull()) {
                log.trace("Parsing records for section " + sectionId + ": empty record");
            } else {
                if (log.isTraceEnabled()) {
                    log.trace("adding the record:" + record + " to the section " + sectionId);
                }
                sectionInstance.addRecord(record);
            }

        } // loop on the lines

        log.trace("___ /end of parsing section " + sectionId + " ___");
        return sectionInstance;
    }

}
