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

package org.isatools.isatab.export.pride;

import au.com.bytecode.opencsv.CSVReader;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import uk.ac.ebi.bioinvindex.utils.i18n;
import uk.ac.ebi.pride.model.implementation.core.GelFreeIdentificationImpl;
import uk.ac.ebi.pride.model.implementation.core.GelImpl;
import uk.ac.ebi.pride.model.implementation.core.TwoDimensionalIdentificationImpl;
import uk.ac.ebi.pride.model.interfaces.core.Gel;
import uk.ac.ebi.pride.model.interfaces.core.Identification;
import uk.ac.ebi.pride.model.interfaces.core.Peptide;
import uk.ac.ebi.pride.model.interfaces.mzdata.CvParam;
import uk.ac.ebi.pride.model.interfaces.mzdata.UserParam;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * The iterator for the identification file. You shouldn't use this class directly, it's used by {@link IdentificationCsvCollection}.
 * <p/>
 * <dl><dt>Date:</dt><dd>Jun 1, 2008</dd></dl>
 *
 * @author brandizi
 */
public class IdentificationCsvIterator extends PrideCsvIterator<Identification> {
    private final String
            ID_TYPE_GELFREE = "Gel Free",
            ID_TYPE_2D = "2D-GE Based";

    private final File peptideCsvFile, modificationCsvFile;

    /**
     * @param csvReader           the reader where identifications are read from
     * @param peptideCsvFile      the file to be used to read the peptides associated to the identifications
     * @param modificationCsvFile the file which is passed to {@link PeptideCsvCollection} and which stores the PTMs associated
     *                            to the peptides.
     */
    public IdentificationCsvIterator(CSVReader csvReader, File peptideCsvFile, File modificationCsvFile) throws IOException {
        initCsvReader(csvReader);
        this.peptideCsvFile = peptideCsvFile;
        this.modificationCsvFile = modificationCsvFile;
        initFirstObject();
    }

    /**
     * Reads the next identification in the CSV and append a {@link PeptideCsvCollection} to it.
     *
     * @see org.isatools.tablib.utils.CsvIterator#readNextObject()
     */
    @Override
    protected Identification readNextObject() throws IOException {
        String line[] = lastReadLine();
        if (line == null) {
            return lastReadObject = null;
        }

        // Get the parameters, which can be spanned over multiple lines
        //
        Collection<CvParam> cvparams = new ArrayList<CvParam>();
        Collection<UserParam> uparams = new ArrayList<UserParam>();

        // Prepares the next line
        readNextLine();

        if (parseParams(line, cvparams, uparams)) {
            for (
                    String[] pline = lastReadLine();
                    pline != null
                            && StringUtils.trimToNull(getValue(pline, "Protein Unique ID")) == null
                            && parseParams(pline, cvparams, uparams);
                    pline = readNextLine()
                    ) {
                ;
            }
        }

        // Get the peptides from the over-peptide-spreadsheet iterator
        Collection<Peptide> peptides = new PeptideCsvCollection(peptideCsvFile, getValue(line, "Protein Unique ID"), modificationCsvFile);
        // DEBUG Collection<Peptide> peptides = new ArrayList<Peptide> ();

        String type = getValue(line, "Type");

        Identification ident = null;


        if (ID_TYPE_GELFREE.equals(type)) {
            ident = new GelFreeIdentificationImpl(
                    getValue(line, "Protein Accession"),
                    getValue(line, "Accession Version"),
                    getValue(line, "Splice Isoform"),
                    getValue(line, "Search Database"),
                    peptides, // peptides
                    cvparams,
                    uparams,
                    getValue(line, "Search Engine"),
                    getValue(line, "Search DB Version"),
                    NumberUtils.createDouble(StringUtils.trimToNull(getValue(line, "Sequence Coverage"))),
                    NumberUtils.createDouble(StringUtils.trimToNull(getValue(line, "Score"))),
                    NumberUtils.createDouble(StringUtils.trimToNull(getValue(line, "Threshold"))),
                    NumberUtils.createLong(StringUtils.trimToNull(getValue(line, "Spectrum Reference")))
            );
        } else if (ID_TYPE_2D.equals(type)) {
            Gel gel = new GelImpl(getValue(line, "Gel Link"), null, null);
            ident = new TwoDimensionalIdentificationImpl(
                    getValue(line, "Protein Accession"),
                    getValue(line, "Accession Version"),
                    getValue(line, "Splice Isoform"),
                    getValue(line, "Search Database"),
                    getValue(line, "Search DB Version"),
                    peptides,
                    cvparams,
                    uparams,
                    NumberUtils.createDouble(StringUtils.trimToNull(getValue(line, "pI"))),
                    NumberUtils.createDouble(StringUtils.trimToNull(getValue(line, "Mol. Wt."))),
                    NumberUtils.createDouble(StringUtils.trimToNull(getValue(line, "Sequence Coverage"))),
                    gel,
                    NumberUtils.createDouble(StringUtils.trimToNull(getValue(line, "X-Coord"))),
                    NumberUtils.createDouble(StringUtils.trimToNull(getValue(line, "Y-Coord"))),
                    NumberUtils.createLong(StringUtils.trimToNull(getValue(line, "Spectrum Reference"))),
                    getValue(line, "Search Engine"),
                    NumberUtils.createDouble(StringUtils.trimToNull(getValue(line, "Score"))),
                    NumberUtils.createDouble(StringUtils.trimToNull(getValue(line, "Threshold")))
            );
        } else {
            throw new RuntimeException(i18n.msg("unknown_protein", type));
        }
        //"Error in the Identification file, unknown protein type: " + type );

        return ident;
    }
}
