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

package org.isatools.tablib.utils;

import au.com.bytecode.opencsv.CSVReader;
import org.isatools.tablib.exceptions.TabIOException;
import org.isatools.tablib.exceptions.TabInternalErrorException;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An iterator over CSV files/readers. It is able to map the lines of a CSV files into Java objects. Every time next()
 * is invoked, an object is created on the basis of the current line in the CSV reader. In the simplest case the mapping is
 * one object per line. In more complex cases {@link #readNextObject()} may scroll over more than 1 row in order to create
 * a new mapped object.
 * <p/>
 * <h3>Notes for extending this class</h3>
 * Since Reader cannot be rewinded, we have to always read in advance the next object (and the next line). When next()
 * is called, this last read object is returned and, just before returning, another object is read and stored
 * for the next call to next(). Likewise, {@link #readNextLine()} store the next line read and leave it available to
 * other methods, via {@link #lastReadLine()}.
 * <p/>
 * <p/>TODO: in case of large files we might need to close the reader and other memory-freeing stuff in finalize().
 * <p/>
 * <dl><dt>Date:</dt><dd>Jun 1, 2008</dd></dl>
 *
 * @author brandizi
 * @param <OT> The object type which is mapped by the CSV stream.
 */
public abstract class CsvIterator<OT> implements Iterator<OT> {
    private CSVReader csvReader;
    private String[] lastReadLine;
    protected OT lastReadObject = null;

    protected CsvIterator() {
    }

    /**
     * The constructor should 1) call {@link #initCsvReader(CSVReader)} as one of the first actions
     * 2) call {@link #initFirstObject()} before exiting.
     */
    public CsvIterator(CSVReader csvReader) throws IOException {
        initCsvReader(csvReader);
        initFirstObject();
    }

    /**
     * always reads the first object available in the reader, setting {@link #lastReadObject}, and always position the
     * reader at the line about the next object (the next line in the simplest case). Usually called by the constructor.
     */
    protected void initFirstObject() throws IOException {
        readNextLine();
        lastReadObject = readNextObject();
    }


    public boolean hasNext() {
        return lastReadObject != null;
    }

    public OT next() {
        if (!this.hasNext()) {
            throw new NoSuchElementException("CSV Iterator: no new element available");
        }

        OT result = lastReadObject;
        try {
            lastReadObject = readNextObject();
        }
        catch (IOException ex) {
            throw new TabIOException("CSV Iterator, problems while reading underlining stream: " + ex.getMessage(), ex);
        }
        return result;
    }

    /**
     * This method should assume the next line in the reader is available, under {@link #lastReadLine()}, and it should
     * convert the read line into the corresponding mapped object. It should advance the reader if multiple lines are used
     * to describe one object. It should advance the reader even when the object is mapped by one line only.
     * By working this way, it should be that, when this methood is to to return, the reader should be automatically
     * positioned at the next available line for the next object.
     */
    protected abstract OT readNextObject() throws IOException;

    /**
     * This is usually called inside {@link #readNextObject()}, in case an object is mapped over more than 1 line
     */
    protected String[] readNextLine() throws IOException {
        return lastReadLine = csvReader.readNext();
    }

    /**
     * The last read line, which remains the same until {@link #readNextLine()} is invoked again
     */
    protected String[] lastReadLine() {
        return lastReadLine;
    }

    /**
     * Not supported, throws {@link UnsupportedOperationException}
     */
    public void remove() {
        throw new TabInternalErrorException("Internal Error, CSV Iterator: remove() not supported");
    }

    /**
     * Setup the CSVReader this iterator is based on
     */
    protected void initCsvReader(CSVReader csvReader) {
        this.csvReader = csvReader;
    }

}
