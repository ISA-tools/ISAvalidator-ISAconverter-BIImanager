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
import org.apache.log4j.Logger;
import org.isatools.isatab.ISATABPersister;
import org.isatools.isatab_nano.NANOFormatWrapper;
import org.isatools.tablib.exceptions.TabInternalErrorException;
import org.isatools.tablib.exceptions.TabStructureError;
import org.isatools.tablib.schema.*;
import org.isatools.tablib.utils.logging.TabNDC;
import uk.ac.ebi.bioinvindex.utils.i18n;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * Read a TAB file and loads an instance of it.
 * <p/>
 * PLEASE NOTE: This is *not* the GUI Loader, mentioned in the end user documentation. That loader is actually
 * a wrapper of {@link ISATABPersister}. This loader refers to the first in-memory loading of the ISATAB structure.
 * TODO: review the names.
 *
 * @author brandizi
 */
public class TabLoader {
    private FormatSetInstance formatSetInstance;
    protected String basePath = "";

    private static enum ParserState {
        WAITING_SECTION_BEGIN, READING_SECTION_LINES
    }

    protected static final Logger log = Logger.getLogger(TabLoader.class);


    /**
     * @param schema the schema  to be used to parse the CSV files
     */
    public TabLoader(FormatSet schema) {
        this.formatSetInstance = new FormatSetInstance(schema);
    }

    /**
     * @param schema   the schema  to be used to parse the CSV files
     * @param basePath The directory where to read the submission files from
     */
    public TabLoader(FormatSet schema, String basePath) {
        this(schema);
        this.basePath = basePath;
    }

    /**
     * The result of the parsing
     */
    public FormatSetInstance getFormatSetInstance() {
        return formatSetInstance;
    }

    /**
     * Uses the base path
     */
    public FormatInstance load(String fileId, String formatId) throws IOException {
        return load(basePath, fileId, formatId);
    }

    /**
     * Loads a file which is an instance of formatId, assigns fileId to the result ({@link FormatInstance#getFileId()})
     */
    public FormatInstance load(String prefix, String fileId, String formatId) throws IOException {
        String filePath = prefix + "/" + fileId;
        TabNDC ndc = TabNDC.getInstance();
        ndc.pushFormat(formatId, formatId, fileId);
        log.info("TabLoader, loading format: " + formatId + " from " + filePath);
        BufferedReader reader = new BufferedReader(new FileReader(new File(filePath)));
        FormatInstance result = parse(fileId, reader, formatId);
        ndc.popTabDescriptor();
        return result;
    }


    /**
     * Uses the base path
     */
    public FormatSetInstance loadResourcesFromFileIds(Map<String, String> formatResources) throws IOException {
        return loadResourcesFromFileIds(basePath, formatResources);
    }


    /**
     * Loads format instances from file IDs. These are prefixed with the path parameter and postfixed with
     * proper file extension. The method is intended to be used for the assays in the ISATAB format. This version
     * is mainly used for testing purposes.
     */
    public FormatSetInstance loadResourcesFromFileIds(String pathPrefix, Map<String, String> formatResources) throws IOException {
        for (String fileId : formatResources.keySet()) {
            String formatId = formatResources.get(fileId);
            String path = pathPrefix + fileId;

            log.info("Loading format " + formatId + " from " + path);

            BufferedReader input = new BufferedReader(new InputStreamReader(
                    this.getClass().getResourceAsStream(path)
            ));
            FormatInstance formatInstance = parse(fileId, input, formatId);
            formatSetInstance.addFormatInstance(formatInstance);
        }

        return formatSetInstance;
    }


    /**
     * Loads format instances from a map of resource-path => format ID. This is a facility used in tests.
     */
    public FormatSetInstance loadResources(Map<String, String> formatResources) throws IOException {
        for (String path : formatResources.keySet()) {
            String formatId = formatResources.get(path);

            log.info("Loading format " + formatId + " from " + path);

            BufferedReader input = new BufferedReader(new InputStreamReader(
                    this.getClass().getResourceAsStream(path)
            ));
            FormatInstance formatInstance = parse(path, input, formatId);
            formatSetInstance.addFormatInstance(formatInstance);
        }

        return formatSetInstance;
    }


    /**
     * Loads a tabular file from stream, parses it and spawns the
     * in-memory format object model. Instantiate a new format and returns it.
     * <p/>
     * fileId is used to set it in the resulting format instance.
     */
    public FormatInstance parse(String fileId, Reader reader, String formatId) throws IOException {
        log.trace("Parsing the format: " + formatId);
        FormatSet schema = formatSetInstance.getFormatSet();
        Format format = schema.getFormat(formatId);
        return parse(fileId, reader, format);
    }


    /**
     * Loads a tabular file from a stream, parses it and spawns the
     * in-memory format object model. Instantiate a new {@link FormatInstance} and returns it.
     * <p/>
     * TODO: move the code in a specific parser.
     *
     * @param fileId   used to store it in the resulting format instance.
     * @param reader   the TSV stream to read lines from
     * @param format the name of format expected in the reader
     */
    public FormatInstance parse(String fileId, Reader reader, Format format)
            throws IOException {

        // ____________ Init ______________
        if (format == null) {
            throw new TabInternalErrorException("parse(): null format");
        }

        log.trace("Parsing the format: " + format.getId());

        // The CSV reader
        TabCsvReader csvrdr = new TabCsvReader(reader, format);
        List<String[]> csvLines = new ArrayList<String[]>();

        // The current parser status
        ParserState status = ParserState.WAITING_SECTION_BEGIN;

        // Use the format
        FormatInstance formatInstance = new FormatInstance(format, formatSetInstance);
        formatInstance.setFileId(fileId);

        // Step through the sections while scanning the lines
        Section curSection = null;
        int sectionStartingLine = -1;

        for (String[] csvLine; (csvLine = csvrdr.readNext()) != null; ) {
            log.trace("Parsing line: " + ArrayUtils.toString(csvLine));

            if (TabCsvReader.isCommentLine(csvLine))
            // Comment, go ahead
            {
                continue;
            }

            switch (status) {
                case WAITING_SECTION_BEGIN:
                    if (TabCsvReader.isBlankLine(csvLine))
                    // Go ahead, we allow for multiple blank lines
                    {
                        continue;
                    }

                    if (TabCsvReader.isHeaderLine(csvLine)) {
                        String csvHeader = csvLine[0];
                        Section section = format.getSectionByHeader(csvHeader, false);

                        if (section != null) {
                            // Convert the header into the preferred format (e.g.: upper case)
                            csvLine[0] = section.getAttr("header");

                            // Start considering the next section
                            status = ParserState.READING_SECTION_LINES;
                            curSection = section;
                            sectionStartingLine = csvrdr.getLastReadLineIndex();
                            log.trace(String.format("Start parsing the section: '%s' ('%s') at line %d",
                                    section.getId(), csvHeader, sectionStartingLine)
                            );
                        } else {
                            throw new TabStructureError(i18n.msg("wrong_section_header", csvHeader, csvrdr.getLastReadLineIndex()));
                        }
                    } else {
                        throw new TabStructureError(i18n.msg("missing_section_header", csvrdr.getLastReadLineIndex()));
                    }
                    break;

                case READING_SECTION_LINES:
                    if (TabCsvReader.isBlankLine(csvLine)) {
                        // Pass the accumulated lines to the section parser
                        log.trace("Invoking the parser");
                        SectionInstance sectionInstance = curSection.getParser(formatInstance).parseCsvLines(csvLines);
                        sectionInstance.setStartingLine(sectionStartingLine);
                        formatInstance.addSectionInstance(sectionInstance);

                        // Clear parsed lines and move to the next section
                        csvLines.clear();
                        curSection = null;
                        status = ParserState.WAITING_SECTION_BEGIN;
                    } else {
                        // Accumulate the lines for the section

                        NANOFormatWrapper.processFileHeader(csvLine);
                        csvLines.add(csvLine);

                        log.trace("Added the line:\n" + Arrays.toString(csvLine) + "\nlen: " + csvLine.length);
                    }

                    // no other case will occur
            }    // switch
        } // loop on the lines


        // Check final status and possible inconsistencies
        //
        if (status == ParserState.READING_SECTION_LINES) {
            // Pass the last accumulated lines to the section parser
            log.trace("Invoking the parser (at the EOF)");
            SectionInstance sectionInstance = curSection.getParser(formatInstance).parseCsvLines(csvLines);
            sectionInstance.setStartingLine(sectionStartingLine);
            formatInstance.addSectionInstance(sectionInstance);
        }

        log.trace("Done parsing format: " + format.getId());
        return formatInstance;
    }

}
