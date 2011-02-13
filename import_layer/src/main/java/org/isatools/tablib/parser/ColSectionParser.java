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

import org.apache.log4j.Logger;
import org.isatools.tablib.schema.FormatInstance;
import org.isatools.tablib.schema.Section;
import org.isatools.tablib.schema.SectionInstance;

import java.util.List;
import java.util.Vector;

/**
 * Maps a section where the values are spanned over colums.
 *
 * @author brandizi
 */
public class ColSectionParser extends RowSectionParser {
    protected static final Logger log = Logger.getLogger(ColSectionParser.class);

    public ColSectionParser(Section section, FormatInstance formatInstance) {
        super(section, formatInstance);
    }


    /**
     * The many-in-cols and one-in-col parser is actually a wrapper of RowSectionParser. When called by a per-column
     * arranged section, it first transposes the whole matrix being received, then it calls RowSectionParser.
     */
    public SectionInstance parseCsvLines(List<String[]> csvLines, int fromRow, int toRow, int fromCol, int toCol) {
        Section section = sectionInstance.getSection();
        String sectName = section.getAttr("id");

        log.trace("___ Parsing the section: " + sectName + " (sending to RowSectionParser) ___");

        try {

            if (csvLines == null || csvLines.size() == 0) {
                log.trace("WARNING, Parsing the section: " + sectName + " The CSV lines are empty");
                return this.sectionInstance;
            }

            // Transpose all and then pass it to the row-oriented parser
            // All the matrix is first transposed, regardless of the passed window
            //
            List<String[]> trnsLines = new Vector<String[]>();
            int sz = csvLines.size();
            for (int i = 0; i < sz; i++) {
                String line[] = csvLines.get(i);
                if (line == null) {
                    log.debug("Cannot process the transposition of the (physical) line #" + i + ": null line");
                    continue;
                }
                for (int j = 0; j < line.length; j++) {
                    String vij = line[j];
                    if (vij == null || "".equals(vij)) {
                        continue;
                    }

                    // Fill the transposed matrix so that you have the j row
                    for (int ifill = trnsLines.size(); ifill <= j; ifill++) {
                        trnsLines.add(new String[sz]);
                    }

                    // Assign the transposed element
                    String trnsLine[] = trnsLines.get(j);
                    trnsLine[i] = line[j];
                }
            }

            // Again, all the matrix is passed, cause we select slices only when we have started a set of
            // calls from the transposed matrix (i.e. only on subsections)
            //
            log.trace("matrix transposed");

            // Go upstream now
            return super.parseCsvLines(trnsLines, 0, trnsLines.size() - 1, 0, -1);
        }
        finally {
            log.trace("_____ /end Parsing the section: " + sectName + " _____");
        }
    }
}
