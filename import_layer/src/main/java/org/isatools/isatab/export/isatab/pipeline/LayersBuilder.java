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
//import org.apache.log4j.Logger;
//import org.isatools.isatab.export.isatab.pipeline.wrapper_nodes.GraphElementWrapperNode;
//import org.isatools.isatab.export.isatab.pipeline.wrapper_nodes.WrapperNodesFactory;
//import org.isatools.tablib.exceptions.TabUnsupportedException;
//import org.isatools.tablib.mapping.pipeline.ProcessingEntityTabMapper;
//import org.isatools.tablib.utils.BIIObjectStore;
//import uk.ac.ebi.bioinvindex.model.Material;
//import uk.ac.ebi.bioinvindex.model.Study;
//import uk.ac.ebi.bioinvindex.model.processing.*;
//import uk.ac.ebi.bioinvindex.utils.i18n;
//import uk.ac.ebi.bioinvindex.utils.processing.ProcessingUtils;
//
//import java.util.*;
//
///**
// * Builds the layered graph corresponding to a given piece of experimental pipeline.
// * <p/>
// * TODO: For the moment is not used, cause we import more regular structures and the layer of every
// * node is simply its distance from its source.
// *
// * @author brandizi
// *         <b>date</b>: Dec 14, 2009
// */
//@SuppressWarnings("unchecked")
//public class LayersBuilder {
//    private final Study study;
//    private final String assayFileId;
//    private final boolean wantSample;
//
//    private GraphLayers gls = new GraphLayers();
//    private final BIIObjectStore store;
//
//    private Map<GraphElement, GraphElementWrapperNode> tabFrags =
//            new HashMap<GraphElement, GraphElementWrapperNode>();
//
//    private boolean isInitialized = false;
//
//    protected static final Logger log = Logger.getLogger(LayersBuilder.class);
//
//    /**
//     * Works with assays having a particular pair of measurement/technology and belonging to a paricular study.
//     *
//     * @param store       needed to populate it with ontology sources that are met over the pipeline.
//     * @param wantSample  Tells what to actually build for a given study: the sample file or the assay file pointed by
//     *                    assayFileId.
//     * @param assayFileId Only the assays coming from this file will be exported
//     */
//    public LayersBuilder(BIIObjectStore store, Study study, boolean wantSample, String assayFileId) {
//        this.study = study;
//        this.wantSample = wantSample;
//        this.assayFileId = assayFileId;
//        this.store = store;
//    }
//
//    /**
//     * Defaults to {@link #SAMPLE_VIRTUAL_MEASUREMENT}, null
//     */
//    public LayersBuilder(BIIObjectStore store, Study study) {
//        this(store, study, true, null);
//    }
//
//    /**
//     * Calls everything and builds the final result.
//     */
//    private void build() {
//        isInitialized = true;
//        WrapperNodesFactory.createInstance(store, null);
//        computeAllLayers();
//        computeTypedLayers();
//    }
//
//
//    /**
//     * Assigns all the layers for all the pertinent graph elements.
//     */
//    private void computeAllLayers() {
//
//        for (Assay assay : study.getAssays()) {
//            if (wantSample) {
//                // In case we want the sample file, we have to find back the right-most samples that come from the sample
//                // file.
//                //
//                Material am = assay.getMaterial();
//                if (am == null) {
//                    continue;
//                }
//                Node mn = am.getMaterialNode();
//                if (mn == null) {
//                    continue;
//                }
//                for (MaterialNode sampleNode : ProcessingUtils.findSampleFileLastNodes(mn, false)) {
//                    computeLayer(sampleNode);
//                }
//                continue;
//            }
//
//            if (!assayFileId.equals(assay.getSingleAnnotationValue(
//                    ProcessingEntityTabMapper.ASSAY_FILE_ANNOTATION_TAG))
//                    ) {
//                continue;
//            }
//
//            Material am = assay.getMaterial();
//            if (am == null) {
//                continue;
//            }
//            Node mn = am.getMaterialNode();
//            if (mn == null) {
//                continue;
//            }
//            for (Node endNode : ProcessingUtils.findEndNodes(mn)) {
//                computeLayer(endNode);
//            }
//        }
//    }
//
//    /**
//     * Compute the layer for this node.
//     */
//    private int computeLayer(Node node) {
//        int maxl = gls.getLayer(node);
//        if (maxl != -1) {
//            return maxl;
//        }
//        maxl = 0;
//
//        if (node instanceof MaterialNode && !wantSample) {
//            // We are building the layers for an assay file, hence let's stop at the first sample found that goes
//            // back to the left and which comes from a sample file.
//            //
//            Material material = ((MaterialNode) node).getMaterial();
//            if (material.getSingleAnnotationValue("sampleFileId") != null) {
//                gls.setLayer(node, 0);
//                return 0;
//            }
//        }
//
//        for (Processing downProc : (Collection<Processing>) node.getDownstreamProcessings()) {
//            maxl = Math.max(maxl, computeLayer(downProc) + 1);
//        }
//        gls.setLayer(node, maxl);
//        return maxl;
//    }
//
//    /**
//     * Compute the layer for this processing
//     */
//    private int computeLayer(Processing proc) {
//        int maxl = gls.getLayer(proc);
//        if (maxl != -1) {
//            return maxl;
//        }
//        maxl = 0;
//
//        for (Node input : (Collection<Node>) proc.getInputNodes()) {
//            maxl = Math.max(maxl, computeLayer(input) + 1);
//        }
//        gls.setLayer(proc, maxl);
//
//        return maxl;
//    }
//
//    /**
//     * It is supposed to rearrange the initially assigned layers by considering the type of each graph element. The type of a GE is
//     * given by its first header, as it is returned by {@link GraphElementWrapperNode}. Such header is e.g.: "Source Name",
//     * "Sample Name", "Data Transformation Name".
//     * <p/>
//     * <p>TODO: <b>HOWEVER WE DON'T SUPPORT THESE CASES AT THE MOMENT</b>. They're generated when a node is omitted for some paths and
//     * not for others (e.g: sample1,extract1,data1 and sample2,,data2). The validator doesn't accept such a situation and
//     * rearranging the layers in such a case is not simple. For instance, consider the case:</p>
//     * <p/>
//     * <table border = "1" cellspacing="0">
//     * <tr><td>src1</td><td>sample1</td><td>sample1</td><td>xtract1</td><td>labelled_xtract1</td></tr>
//     * <tr><td>src2</td><td>labelled_xtract2</td><td></td><td></td><td></td></tr>
//     * </table>
//     * <p/>
//     * One has to understand that the node to be moved is labelled_xtract2 and that it shifts on the right by 3
//     * columns. Not exactly simple...
//     */
//    private void computeTypedLayers() {
//        Set<GraphElement> toBeShiftedGes = new HashSet<GraphElement>();
//
//        // Check all the layers, mark the elements that are not compatible
//        // with their current layer
//        //
//        for (int layer : gls.getAllLayers()) {
//            String layerHdr = null;
//            for (GraphElement ge : gls.getGraphEls(layer)) {
//                if (toBeShiftedGes.contains(ge)) {
//                    continue;
//                }
//                List<String> headers = getGraphTabFragment(ge).getTabValues().get(0).getHeaders();
//                if (headers.isEmpty()) {
//                    continue;
//                }
//
//                String hdr = headers.get(0);
//                if (layerHdr == null) {
//                    layerHdr = hdr;
//                } else if (!layerHdr.equals(hdr)) {
//                    log.error(
//                            "Layers Builder, failing on layers with gaps, mismatching headers: '" + layerHdr + "'," + hdr
//                                    + "', node: " + ge + " (" + ge.getClass() + ")"
//                    );
//                    throw new TabUnsupportedException(i18n.msg("pipeline_with_gaps", study.getAcc()));
//                }
//            }
//        }
//    }
//
//    /**
//     * Provides the {@link GraphElementWrapperNode} corresponding to this graph element. The returned object contains a tabular
//     * representation of the parameter (see its description for details).
//     */
//    public GraphElementWrapperNode getGraphTabFragment(GraphElement ge) {
//        if (!isInitialized) {
//            build();
//        }
//
//        GraphElementWrapperNode hm = tabFrags.get(ge);
//        if (hm == null) {
//            hm = WrapperNodesFactory.getInstance().getNode(ge);
//
//            if (ge instanceof MaterialNode) {
//                MaterialNode mn = (MaterialNode) ge;
//                tabFrags.put(mn, hm);
//            } else if (ge instanceof Processing) {
//                Processing proc = (Processing) ge;
//                tabFrags.put(proc, hm);
//            } else if (ge instanceof DataNode) {
//                DataNode dn = (DataNode) ge;
//                tabFrags.put(dn, hm);
//            }
//        }
//        return hm;
//    }
//
//    /**
//     * The {@link GraphLayers} object used to represent the layered graph built by this class.
//     */
//    public GraphLayers getGraphLayers() {
//        if (!isInitialized) {
//            build();
//        }
//        return gls;
//    }
//
//    /**
//     * The study this class is working on.
//     */
//    public Study getStudy() {
//        return study;
//    }
//
//    public String getAssayFileId() {
//        return assayFileId;
//    }
//
//    /**
//     * Tells what to actually build for a given study: the sample file or the assay file pointed by
//     * assayFileId.
//     */
//    public boolean wantSample() {
//        return wantSample;
//    }
//
//}
