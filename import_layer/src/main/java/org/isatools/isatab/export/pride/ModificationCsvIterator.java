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
import uk.ac.ebi.pride.model.implementation.core.AverageMassDeltaImpl;
import uk.ac.ebi.pride.model.implementation.core.ModificationImpl;
import uk.ac.ebi.pride.model.implementation.core.MonoMassDeltaImpl;
import uk.ac.ebi.pride.model.interfaces.core.MassDelta;
import uk.ac.ebi.pride.model.interfaces.core.Modification;
import uk.ac.ebi.pride.model.interfaces.mzdata.CvParam;
import uk.ac.ebi.pride.model.interfaces.mzdata.UserParam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * The iterator for the modifications file. You shouldn't need to directly use this, it is used inside {@link ModificationCsvCollection},
 * which in turn is invoked by {@link PeptideCsvIterator}, to populate the identifications with their peptides.
 * <p/>
 * <dl><dt>date:</dt><dd>Jun 1, 2008</dd></dl>
 *
 * @author brandizi
 */
public class ModificationCsvIterator extends PrideCsvIterator<Modification> {
    private final String parentId;

    /**
     * @param csvReader where the peptides for this iterator are read from
     * @param parentId  the "Peptide Unique ID" field to be used to select the proper modifications.
     */
    public ModificationCsvIterator(CSVReader csvReader, String parentId) throws IOException {
        initCsvReader(csvReader);
        this.parentId = parentId;
        initFirstObject();
    }

    /**
     * Reads the next modification in the CSV
     */
    @Override
    protected Modification readNextObject() throws IOException {
        String line[];
        for (
                line = lastReadLine();
                line != null && !parentId.equals(line[headers.get("Peptide Unique ID")]);
                line = readNextLine()
                ) {
            ;
        }

        if (line == null) {
            return lastReadObject = null;
        }

        // Get the parameters and the deltas, both of which can be spanned over multiple lines
        //
        Collection<CvParam> cvparams = new ArrayList<CvParam>();
        Collection<UserParam> uparams = new ArrayList<UserParam>();
        Collection<MassDelta> monoDeltas = new ArrayList<MassDelta>();
        Collection<MassDelta> averageDeltas = new ArrayList<MassDelta>();

        // Prepares the next line
        readNextLine();

        // no short-circuit ( i.e.: '||'), we do need to parse both types
        if (parseParams(line, cvparams, uparams) | parseDeltas(line, monoDeltas, averageDeltas)) {
            for (
                    String[] pline = lastReadLine();
                    pline != null
                            && StringUtils.trimToNull(getValue(pline, "Peptide Unique ID")) == null
                            && (parseParams(pline, cvparams, uparams) | parseDeltas(pline, monoDeltas, averageDeltas));
                    pline = readNextLine()
                    ) {
                ;
            }
        }


        return new ModificationImpl(
                getValue(line, "Accession"),
                NumberUtils.createInteger(StringUtils.trimToNull(getValue(line, "Location"))),
                getValue(line, "PTM Database"),
                StringUtils.trimToNull(getValue(line, "Database Version")),
                monoDeltas, // AvgMonoMassDeltas
                averageDeltas, // AvgMassDeltas
                cvparams,  // CVParams
                uparams   // UserParams
        );
    }


    /**
     * Parses the delta values available in {@link #lastReadLine()}, populating the result parameters. Doesn't
     * do anything else, advancing over multiple lines is up to the caller.
     *
     * @return true if some valid value was found, false otherwise.
     */
    private boolean parseDeltas(String[] line, Collection<MassDelta> monoDeltas, Collection<MassDelta> averageDeltas) {
        if (line == null) {
            return false;
        }

        boolean result = false;

        Double monoDeltaValue = NumberUtils.createDouble(StringUtils.trimToNull(getValue(line, "Mono Delta Value")));

        if (monoDeltaValue != null) {
            monoDeltas.add(new MonoMassDeltaImpl(monoDeltaValue));
            result = true;
        }

        Double avgDeltaValue = NumberUtils.createDouble(StringUtils.trimToNull(getValue(line, "Average Delta Value")));
        if (avgDeltaValue != null) {
            averageDeltas.add(new AverageMassDeltaImpl(avgDeltaValue));
            result = true;
        }

        return result;
    }

}
