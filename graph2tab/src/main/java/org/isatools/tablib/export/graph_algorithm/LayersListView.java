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

import java.util.*;


/**
 * A table view over the layer contents, that allow to get the i,j values of the resulting SDRF-like table.
 * This wraps the intermediate {@link TableContents} view and provide the final exportable table, by hiding the
 * per-layer organization in the layer contents view.
 */
class LayersListView extends AbstractList<List<String>> {
    private final TableContents layerContents;

    private static final String UNSUPPORTED_MSG = "The table view created by the ISATAB export cannot be modified";


    /**
     * Maps from a column of the final table view to a layer and the column within the layer.
     */
    private final Map<Integer, int[]> col2LayerColMap = new HashMap<Integer, int[]>();

    /**
     * The cache for the final result, prepared by the constructor
     */
    private final List<String> rows[];

    /**
     * Builds the table view. The result is sorted according to what it's returned by the {@link Node} interface.
     */
    @SuppressWarnings("unchecked")
    public LayersListView(TableContents layerContents) {
        this.layerContents = layerContents;

        int icol = 0;
        List<String> headers = new LinkedList<String>();
        for (int layer : new TreeSet<Integer>(layerContents.getLayers())) {
            List<String> layerHeaders = layerContents.getLayerHeaders(layer);
            headers.addAll(layerHeaders);

            int sz = layerHeaders.size();
            // We may have empty layers when they're about Processing(s) without any protocol applications
            if (sz == 0) {
                continue;
            }

            for (int iLayerCol = 0; iLayerCol < sz; iLayerCol++) {
                col2LayerColMap.put(icol++, new int[]{layer, iLayerCol});
            }
        }
        rows = new List[layerContents.colsMaxSize() + 1];

        // Certain headers are postfixed with the field they refer to (e.g.: term source REF)
        // We must un-marshal these values.
        //
        icol = 0;
        for (String header : headers) {
            if (header != null) {
                int itrunc = header.lastIndexOf('|');
                if (itrunc >= 0) {
                    headers.set(icol, header.substring(0, itrunc));
                }
            }
            icol++;
        }
        rows[0] = headers;

    }


    /**
     * Returns the headers if irow == 0, otherwise returns the {@link SingleLayersRowView} corresponding to
     * irow.
     */
    @Override
    public List<String> get(int irow) {
        if (irow == 0) {
            return rows[0];
        }

        SingleLayersRowView row = (SingleLayersRowView) rows[irow];
        if (row == null) {
            row = new SingleLayersRowView(layerContents, col2LayerColMap, irow - 1);
            rows[irow] = row;
        }
        return row;
    }

    /**
     * Max number of rows found in {@link LayerContent#columns}. Usually all the contents have the same size.
     */
    @Override
    public int size() {
        return rows.length;
    }


    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public void add(int index, List<String> element) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public boolean add(List<String> o) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public boolean addAll(int index, Collection<? extends List<String>> c) {
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
    public List<String> remove(int index) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public List<String> set(int index, List<String> element) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

}