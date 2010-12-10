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

package org.isatools.tablib.export.graph_algorithm;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Map;


/**
 * Provide a list view for a single row in a layered graph. That is: every columns is mapped in its layer and
 * the proper column within the layer, while the row is the one passed to the constructor.
 */
class SingleLayersRowView extends AbstractList<String> implements Comparable<SingleLayersRowView> {
    private final LayerContents layerContents;
    private final Map<Integer, int[]> col2LayerColMap;
    private final int size, irow;

    private static final String UNSUPPORTED_MSG = "The table view created by the ISATAB export cannot be modified";


    public SingleLayersRowView(LayerContents layerContents, Map<Integer, int[]> col2LayerColMap, int irow) {
        this.layerContents = layerContents;
        this.col2LayerColMap = col2LayerColMap;
        this.size = col2LayerColMap.size();
        this.irow = irow;
    }

    @Override
    public String get(int icol) {
        int layerIdx[] = col2LayerColMap.get(icol);
        int layer = layerIdx[0], layerCol = layerIdx[1];
        return layerContents.get(layer, irow, layerCol);
    }

    @Override
    public int size() {
        return size;
    }

    /**
     * Rows are compared considering the first column in each layer and excluding the "Protocol REF" columns.
     * This corresponds to considering headers like "Source Name", "Sample Name" etc.
     * <p/>
     * <p>The comparison is lexicographical, i.e.: compares columns in order until it finds different values, considers
     * row1 &lt; row2 if row2 == row1 + some non-empty tail, considers row1 == row2 if all row(i) == row(j).</p>
     */
    public int compareTo(SingleLayersRowView row2) {
        if (row2 == null) {
            return 1;
        }
        if (this == row2) {
            return 0;
        }

        int prevLayer = -1;
        for (int icol = 0; icol < size && icol < row2.size; icol++) {
            int layerIdx[] = col2LayerColMap.get(icol);
            int layer = layerIdx[0], layerCol = layerIdx[1];
            String header = layerContents.getHeader(layer, layerCol);
            if (layer == prevLayer || "Protocol REF".equalsIgnoreCase(header)) {
                prevLayer = layer;
                continue;
            }

            String nodev1 = layerContents.get(layer, irow, layerCol);
            String nodev2 = layerContents.get(layer, row2.irow, layerCol);

            if (nodev1 == null) {
                return nodev2 == null ? 0 : -1;
            }
            if (nodev2 == null) {
                return 1;
            }

            int comp = nodev1.compareToIgnoreCase(nodev2);
            if (comp != 0) {
                return comp;
            }
            prevLayer = layer;
        }
        return this.size - row2.size;
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public void add(int index, String element) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public boolean add(String o) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public boolean addAll(int index, Collection<? extends String> c) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public void clear() {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public String remove(int index) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public String set(int index, String element) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

}