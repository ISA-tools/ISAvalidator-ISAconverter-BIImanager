// TODO: Remove

///**
//
// The ISAconverter, ISAvalidator & BII Management Tool are components of the ISA software suite (http://www.isa-tools.org)
//
// Exhibit A
// The ISAconverter, ISAvalidator & BII Management Tool are licensed under the Mozilla Public License (MPL) version
// 1.1/GPL version 2.0/LGPL version 2.1
//
// "The contents of this file are subject to the Mozilla Public License
// Version 1.1 (the "License"). You may not use this file except in compliance with the License.
// You may obtain copies of the Licenses at http://www.mozilla.org/MPL/MPL-1.1.html.
//
// Software distributed under the License is distributed on an "AS IS"
// basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
// License for the specific language governing rights and limitations
// under the License.
//
// The Original Code is the ISAconverter, ISAvalidator & BII Management Tool.
//
// The Initial Developer of the Original Code is the ISA Team (Eamonn Maguire, eamonnmag@gmail.com;
// Philippe Rocca-Serra, proccaserra@gmail.com; Susanna-Assunta Sansone, sa.sanson@gmail.com;
// http://www.isa-tools.org). All portions of the code written by the ISA Team are Copyright (c)
// 2007-2011 ISA Team. All Rights Reserved.
//
// Contributor(s):
// Rocca-Serra P, Brandizi M, Maguire E, Sklyar N, Taylor C, Begley K, Field D,
// Harris S, Hide W, Hofmann O, Neumann S, Sterk P, Tong W, Sansone SA. ISA software suite:
// supporting standards-compliant experimental annotation and enabling curation at the community level.
// Bioinformatics 2010;26(18):2354-6.
//
// Alternatively, the contents of this file may be used under the terms of either the GNU General
// Public License Version 2 or later (the "GPL") - http://www.gnu.org/licenses/gpl-2.0.html, or
// the GNU Lesser General Public License Version 2.1 or later (the "LGPL") -
// http://www.gnu.org/licenses/lgpl-2.1.html, in which case the provisions of the GPL
// or the LGPL are applicable instead of those above. If you wish to allow use of your version
// of this file only under the terms of either the GPL or the LGPL, and not to allow others to
// use your version of this file under the terms of the MPL, indicate your decision by deleting
// the provisions above and replace them with the notice and other provisions required by the
// GPL or the LGPL. If you do not delete the provisions above, a recipient may use your version
// of this file under the terms of any one of the MPL, the GPL or the LGPL.
//
// Sponsors:
// The ISA Team and the ISA software suite have been funded by the EU Carcinogenomics project
// (http://www.carcinogenomics.eu), the UK BBSRC (http://www.bbsrc.ac.uk), the UK NERC-NEBC
// (http://nebc.nerc.ac.uk) and in part by the EU NuGO consortium (http://www.nugo.org/everyone).
//
// */
//
//package org.isatools.isatab.export.isatab.pipeline;
//
//import uk.ac.ebi.bioinvindex.model.processing.GraphElement;
//
//import java.io.PrintWriter;
//import java.io.StringWriter;
//import java.util.*;
//
///**
// * Allows to manage a layered graph representation of an experimental pipeline. The nodes in the same layer
// * are those who will be reported in the same group of columns in a tabular representation (e.g.: ISATAB or MAGETAB).
// * <p/>
// * For more details, see Section 3.3.2 of
// * <a href = "http://annotare.googlecode.com/files/MAGE-TABv1.1.pdf">this document</a>
// * <p/>
// * TODO: For the moment is not used, cause we import more regular structures and the layer of every
// * node is simply its distance from its source.
// *
// * @author brandizi
// *         <b>date</b>: Dec 14, 2009
// */
//public class GraphLayers {
//    private Map<Integer, Set<GraphElement>> layer2GraphElMap = new HashMap<Integer, Set<GraphElement>>();
//    private Map<GraphElement, Integer> graphEl2LayerMap = new HashMap<GraphElement, Integer>();
//    private int requiredRows = 0;
//
//
//    /**
//     * Set the layer for a graph element. Returns the old layer or -1 if none.
//     */
//    public int setLayer(GraphElement ge, int layer) {
//        Integer oldLayer = getLayer(ge);
//        if (oldLayer == layer) {
//            return layer;
//        }
//
//        graphEl2LayerMap.remove(ge);
//        removeGraphEl2LayerEntry(oldLayer, ge);
//
//        if (layer == -1) {
//            return oldLayer;
//        }
//
//        addGraphEl2LayerEntry(layer, ge);
//        graphEl2LayerMap.put(ge, layer);
//        return oldLayer;
//    }
//
//    /**
//     * Tells if a graph element is assigned to a given layer.
//     */
//    public boolean containsGraphEl(int layer, GraphElement ge) {
//        Set<GraphElement> layerElements = layer2GraphElMap.get(layer);
//        if (layerElements == null) {
//            return false;
//        }
//        return layerElements.contains(ge);
//    }
//
//    /**
//     * The layer the graph element is assigned to, or null if none yet.
//     */
//    public int getLayer(GraphElement ge) {
//        Integer layer = graphEl2LayerMap.get(ge);
//        return layer == null ? -1 : layer;
//    }
//
//    /**
//     * All the defined layers
//     */
//    public Set<Integer> getAllLayers() {
//        return layer2GraphElMap.keySet();
//    }
//
//    /**
//     * All the defined layers, sorted in ascending order.
//     * <p/>
//     * TODO: for the moment, the result is regenerated at each call, not cached.
//     */
//    public SortedSet<Integer> getAllLayersSorted() {
//        return new TreeSet<Integer>(getAllLayers());
//    }
//
//    /**
//     * All the graph elements assigned to this layer.
//     */
//    public Set<GraphElement> getGraphEls(int layer) {
//        Set<GraphElement> layerElements = layer2GraphElMap.get(layer);
//        if (layerElements == null) {
//            return Collections.emptySet();
//        }
//        return Collections.unmodifiableSet(layerElements);
//    }
//
//    /**
//     * Internal management of the addition of a graph element into a layer.
//     * Returns true if something has changed.
//     */
//    private boolean addGraphEl2LayerEntry(int layer, GraphElement ge) {
//        if (ge == null) {
//            return false;
//        }
//
//        Set<GraphElement> layerElements = layer2GraphElMap.get(layer);
//        int lsize = -1;
//
//        if (layerElements == null) {
//            layerElements = new HashSet<GraphElement>();
//            layer2GraphElMap.put(layer, layerElements);
//            lsize = 1; // will be this size after addition
//        }
//        ;
//
//        if (layerElements.add(ge)) {
//            if (lsize == -1) {
//                lsize = layerElements.size();
//            } // was not 1
//            if (requiredRows < lsize) {
//                requiredRows = lsize;
//            }
//            return true;
//        }
//
//        return false;
//    }
//
//    /**
//     * Removes a graph element from a layer.
//     */
//    private boolean removeGraphEl2LayerEntry(int layer, GraphElement ge) {
//        Set<GraphElement> layerElements = layer2GraphElMap.get(layer);
//        if (layerElements == null) {
//            return false;
//        }
//        if (layerElements.remove(ge)) {
//            int size = layerElements.size();
//            if (requiredRows < size) {
//                requiredRows = size;
//            }
//            return true;
//        }
//        return false;
//    }
//
//    /**
//     * Inserts an empty layer in layer, All objects in layer1 >= layer are shifted one layer up.
//     */
//    public void insertLayer(int layer) {
//        if (layer < 0) {
//            return;
//        }
//
//        Map<Integer, Set<GraphElement>> layer2GraphElMapNew = new HashMap<Integer, Set<GraphElement>>();
//
//        for (int layer1 : layer2GraphElMap.keySet()) {
//            if (layer1 < layer) {
//                layer2GraphElMapNew.put(layer1, layer2GraphElMap.get(layer1));
//            } else {
//                Set<GraphElement> ges1 = layer2GraphElMap.get(layer1);
//                int layer1Inc = layer1 + 1;
//                layer2GraphElMapNew.put(layer1Inc, ges1);
//                for (GraphElement ge1 : ges1) {
//                    graphEl2LayerMap.put(ge1, layer1Inc);
//                }
//            }
//        }
//        layer2GraphElMap = layer2GraphElMapNew;
//    }
//
//
//    /**
//     * The number of rows that are needed to generate a tabular view of this graph. This is
//     * the max ( {@link #getGraphEls(int) getGraphEls ( layer )}.size() ) for each layer. The value
//     * is cached.
//     */
//    public int getRequiredRows() {
//        return requiredRows;
//    }
//
//    /**
//     * Provides a string representation of the contnets of this layered graph, useful for debugging purposes.
//     */
//    public String report() {
//        StringWriter sout = new StringWriter();
//        PrintWriter out = new PrintWriter(sout);
//
//        SortedSet<Integer> layers = getAllLayersSorted();
//        for (int layer : layers) {
//            out.println("\n==== Layer #" + layer + ":");
//            for (GraphElement ge : getGraphEls(layer)) {
//                out.print(ge.getClass().getSimpleName() + ": " + ge.getAcc() + "\t\t");
//            }
//            out.println();
//        }
//        return sout.toString();
//    }
//
//}
