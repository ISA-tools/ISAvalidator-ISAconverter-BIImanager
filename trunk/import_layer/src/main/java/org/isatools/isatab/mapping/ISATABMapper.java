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

package org.isatools.isatab.mapping;

import org.apache.commons.lang.StringUtils;
import org.isatools.isatab.mapping.microarray.MaFormatTabMapper;
import org.isatools.isatab.mapping.msSpec.MsFormatTabMapper;
import org.isatools.tablib.exceptions.TabInconsistentValuesException;
import org.isatools.tablib.exceptions.TabInternalErrorException;
import org.isatools.tablib.mapping.FormatSetTabMapper;
import org.isatools.tablib.mapping.TabMappingContext;
import org.isatools.tablib.mapping.pipeline.ProcessingsTabMapper;
import org.isatools.tablib.schema.FormatSetInstance;
import org.isatools.tablib.utils.BIIObjectStore;
import org.isatools.tablib.utils.Utils;
import org.isatools.tablib.utils.logging.TabNDC;
import uk.ac.ebi.bioinvindex.model.*;
import uk.ac.ebi.bioinvindex.model.processing.*;
import uk.ac.ebi.bioinvindex.model.term.*;
import uk.ac.ebi.bioinvindex.model.xref.ReferenceSource;
import uk.ac.ebi.bioinvindex.utils.i18n;
import uk.ac.ebi.bioinvindex.utils.processing.ExperimentalPipelineVisitor;
import uk.ac.ebi.bioinvindex.utils.processing.ProcessingUtils;
import uk.ac.ebi.bioinvindex.utils.processing.ProcessingVisitAction;
import uk.ac.ebi.utils.collections.SortedObjectStore;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

/**
 * The ISATAB object mapper. Does the mapping the usual way (via format mappers), then it does a numer of
 * adjustments and checkings, mainly on the experimental pipeline.
 * <p/>
 * TODO: It has become a huuuge class, must refactor/split.
 * <p/>
 * date: Mar 12, 2008
 *
 * @author brandizi
 */
public class ISATABMapper extends FormatSetTabMapper {
    private TabNDC ndc = TabNDC.getInstance();
    private ExperimentalPipelineVisitor graphVisitor = new ExperimentalPipelineVisitor();

    public ISATABMapper(BIIObjectStore store, FormatSetInstance formatSetInstance) {
        super(store, formatSetInstance);
        store.put(TabMappingContext.class, "context", new TabMappingContext());
        this.formatMappersConfig.put("investigation", InvestigationFormatTabMapper.class);
        this.formatMappersConfig.put("study_samples", StudyFormatTabMapper.class);
        this.formatMappersConfig.put("transcriptomics_assay", MaFormatTabMapper.class);
        this.formatMappersConfig.put("ms_spec_assay", MsFormatTabMapper.class);
    }


    @Override
    public BIIObjectStore map() {
        super.map();

        mergeNodeTargets();
        removeOrphanNodes();
        mergeProcessings();
        removeDeadPaths();
        setupCascadedProperties();
        copyFactorValuesFromSamples();
        setupAssayResult2AssayLinks();
        checkAssayResultsExist();
        checkChaining();
        checkUnusedDeclaredEntities();
        checkInconsistentProperties();

        return getStore();
    }

    /**
     * Merges equivalent nodes. Check every possible node pair to see if the nodes point to the same {@link Material}
     * or the same {@link Data}. Merges the nodes in case and changes the corresponding upstream/downstream processings.
     */
    @SuppressWarnings("unchecked")
    private void mergeNodeTargets() {
        Map<ComparedNodeWrapper, ComparedNodeWrapper> checkedNodes = new HashMap<ComparedNodeWrapper, ComparedNodeWrapper>();
        for (Node<?, ?> node : new LinkedList<Node>(getStore().valuesOfType(Node.class))) {
            if (node == null) {
                continue;
            }
            ComparedNodeWrapper nodeWrapper = new ComparedNodeWrapper(node);
            ComparedNodeWrapper nodeWrapper1 = checkedNodes.get(nodeWrapper);
            if (nodeWrapper.equals(nodeWrapper1)) {
                if (node instanceof MaterialNode) // we don't need to check the other node type, they're equal
                {
                    MaterialNode mnode = (MaterialNode) node, mnode1 = (MaterialNode) nodeWrapper1.getBase();
                    Material material = mnode.getMaterial(), material1 = mnode1.getMaterial();

                    String
                            assayFile1 = material1.getSingleAnnotationValue("assayFileId"),
                            sampleFile = material.getSingleAnnotationValue("sampleFileId");

                    if (mnode != mnode1 && sampleFile != null && assayFile1 != null) {
                        // sample2 is only a reference from an assay file to sample1, defined in the sample file
                        // In that case we want to keep the one that is defined in the sample file, so that we will
                        // get correct results during the recreation of the ISATAB submission from the database (i.e.: when
                        // performing ISATAB export).
                        //
                        mergeNodeTargetsPair(mnode, mnode1);
                        checkedNodes.put(nodeWrapper, nodeWrapper);
                        checkedNodes.remove(nodeWrapper1);
                    } else {
                        mergeNodeTargetsPair(mnode1, mnode);
                    }
                } else
                // as said before, equals() already verified we have valid types only
                {
                    mergeNodeTargetsPair((DataNode) nodeWrapper1.getBase(), (DataNode) node);
                }
            } else {
                checkedNodes.put(nodeWrapper, nodeWrapper);
            }
        }
    }


    /**
     * Goes through all the materials or data(s) and removes the nodes which does not belong to any process, they
     * was eliminated by previous steps and are no longer valid.
     */
    private void removeOrphanNodes() {
        for (Study study : getStore().valuesOfType(Study.class)) {
            Investigation investigation = study.getUniqueInvestigation();
            if (investigation != null) {
                ndc.pushObject(investigation);
            }
            ndc.pushObject(study);
            Set<Assay> assays = new HashSet<Assay>(study.getAssays());
            for (Assay assay : assays) {
                Material material = assay.getMaterial();
                ndc.pushObject(material);

                MaterialNode assayNode = material.getMaterialNode();
                if (assayNode.getDownstreamProcessings().size() == 0 && assayNode.getUpstreamProcessings().size() == 0) {
                    // Let's remove both the node and the assay
                    //
                    assayNode.setMaterial(null);
                    study.removeAssay(assay);
                }

                ndc.popObject(); // Material
            }


            Set<AssayResult> assayResults = new HashSet<AssayResult>(study.getAssayResults());
            for (AssayResult ar : assayResults) {
                Data data = ar.getData();
                ndc.pushObject(data);
                DataNode dataNode = data.getProcessingNode();
                if (dataNode.getDownstreamProcessings().size() == 0 && dataNode.getUpstreamProcessings().size() == 0) {
                    // Let's remove both the node and the assay
                    //
                    dataNode.setData(null);
                    study.removeAssayResult(ar);
                }
                ndc.popObject(); // Material
            }


            ndc.popObject(); // Study
            if (investigation != null) {
                ndc.popObject();
            }
        }
    }


    /**
     * Tries to merge a single node pair, on the basis of the {@link Material}(s) pointed by the nodes.
     */
    private boolean mergeNodeTargetsPair(MaterialNode node1, MaterialNode node2) {
        if (node1 == null || node2 == null) {
            throw new TabInternalErrorException(i18n.msg("cant_compare_null_nodes"));
        }

        if (node1 == node2) {
            return false;
        }

        Material material1 = node1.getMaterial();
        Material material2 = node2.getMaterial();

        if (material1 == null || material2 == null) {
            return false;
        }

        if (material1 == material2) {
            // Should never happen, cause node/material association is 1-1, but's let's do it to be sure
            //
            log.trace("WARNING: Merging same materials: [ " + material1.getName() + " ]");
            mergeNodePairs(node1, node2);
            return true;
        }

        // OK, they're physically different but equivalent, let's merge them
        //

        log.trace("Merging equivalent materials: [ " + material1.getName() + " ]");

        Collection<CharacteristicValue> cvals1 = material1.getCharacteristicValues();
        for (CharacteristicValue characteristicValue : material2.getCharacteristicValues()) {
            // TODO: do we allow multiple characteristics of the same type?
            if (!Utils.contains(cvals1, characteristicValue)) {
                material1.addCharacteristicValue(characteristicValue);
            }
        }

        // If we have samples, we must take into account the factor values too.
        Collection<FactorValue> fvs1 = material1.getFactorValues();
        for (final FactorValue fv2 : material2.getFactorValues()) {
            if (!Utils.contains(fvs1, fv2)) {
                material1.addFactorValue(fv2);
            }
        }

//		Set<String> doneAnns = new HashSet<String> ();
//		for ( Annotation annotation: material1.getAnnotations () ) 
//		{
//			String sann = StringUtils.trimToEmpty ( annotation.getType ().getValue () ) 
//			+ StringUtils.trimToEmpty ( annotation.getText () );
//			// Same type/value comes from merging and doesn't make sense
//			if ( sann.length () == 0 || doneAnns.contains ( sann ) ) continue;
//			doneAnns.add ( sann );
//		}
        for (Annotation annotation : material2.getAnnotations()) {
            // TODO: do we allow multiple annotations of the same type?
            String sann = StringUtils.trimToEmpty(annotation.getType().getValue())
                    + StringUtils.trimToEmpty(annotation.getText());
            // Same type/value comes from merging and doesn't make sense
//			if ( sann.length () == 0 || doneAnns.contains ( sann ) ) continue;
//			doneAnns.add ( sann );
            material1.addAnnotation(annotation);
        }

        // TODO: remove, both node2 and material2 will be eliminated
        // node2.setMaterial ( material1 );

        // Deletes the material, the associated assay, and all the nodes referring to it
        removeAssay(node2, material2);
        getStore().put(Material.class, material2.getName(), null);
        mergeNodePairs(node1, node2);

        return true;
    }


    /**
     * Tries to merge a single node pair, on the basis of the {@link Data}(s) pointed by the nodes.
     */
    private boolean mergeNodeTargetsPair(DataNode node1, DataNode node2) {
        if (node1 == null || node2 == null) {
            throw new TabInternalErrorException(i18n.msg("cant_compare_null_nodes"));
        }

        if (node1 == node2) {
            return false;
        }

        Data data1 = node1.getData();
        Data data2 = node2.getData();

        if (data1 == null || data2 == null) {
            return false;
        }

        if (data1 == data2) {
            // Should never happen, cause node/material association is 1-1, but's let's do it to be sure
            //
            log.trace("WARNING, Merging same data objects: [ " + data1.getName() + " ]");
            mergeNodePairs(node1, node2);
            return true;
        }

        // OK, they're physically different but equivalent, let's merge them
        //
        log.trace("Merging same data items: [ " + data1.getName() + " ]");

        String url2 = StringUtils.trimToEmpty(data2.getUrl());
        if (!StringUtils.trimToEmpty(data1.getUrl()).equals(url2)) {
            throw new TabInconsistentValuesException(i18n.msg("same_file_name_2_urls", data1.getName()));
        }

        String matrixUrl2 = StringUtils.trimToEmpty(data2.getDataMatrixUrl());
        if (!StringUtils.trimToEmpty(data1.getDataMatrixUrl()).equals(matrixUrl2)) {
            throw new TabInconsistentValuesException(i18n.msg("same_file_name_2_DM_urls", data1.getName()));
        }

//		Set<String> doneAnns = new HashSet<String> ();
//		for ( Annotation annotation: data1.getAnnotations () ) 
//		{
//			String sann = StringUtils.trimToEmpty ( annotation.getType ().getValue () ) 
//			+ StringUtils.trimToEmpty ( annotation.getText () );
//			// Same type/value comes from merging and doesn't make sense
//			if ( sann.length () == 0 || doneAnns.contains ( sann ) ) continue;
//			doneAnns.add ( sann );
//		}
        for (Annotation annotation : data2.getAnnotations()) {
            // TODO: do we allow multiple annotations of the same type?
            String sann = StringUtils.trimToEmpty(annotation.getType().getValue())
                    + StringUtils.trimToEmpty(annotation.getText());
//			// Same type/value comes from merging and doesn't make sense
//			if ( sann.length () == 0 || doneAnns.contains ( sann ) ) continue;
//			doneAnns.add ( sann );
            data1.addAnnotation(annotation);
        }

        // Remove the node, the data, the assayResult
        removeAssayResult(node2, data2);
        getStore().put(Data.class, data2.getName(), null);
        mergeNodePairs(node1, node2);
        return true;
    }


    /**
     * Called after mergeNodeTargetsPair, merges those nodes which share a {@link Material} or a {@link Data} instance
     */
    @SuppressWarnings("unchecked")
    private void mergeNodePairs(Node node1, Node node2) {
        log.trace("Merging Nodes: [ " + node1.getAcc() + ", " + node2.getAcc() + " ]");

        Collection<Processing<?, ?>> downs2 = new ArrayList<Processing<?, ?>>(node2.getDownstreamProcessings());
        for (Processing<?, ?> processing : downs2) {
            node2.removeDownstreamProcessing(processing);
            node1.addDownstreamProcessing(processing);
        }

        Collection<Processing<?, ?>> ups2 = new ArrayList<Processing<?, ?>>(node2.getUpstreamProcessings());
        for (Processing<?, ?> processing : ups2) {
            node2.removeUpstreamProcessing(processing);
            node1.addUpstreamProcessing(processing);
        }

        node2.setStudy(null); // just to be sure
        getStore().put(Node.class, node2.getAcc(), null);
    }


    /**
     * Removes an assay associated to the study in the node parameter and to the material parameter. The assay is removed
     * from the study and the assay group it points to, and this way becomes unreachable/garbaged.
     */
    private void removeAssay(MaterialNode node, Material material) {
        Study study = node.getStudy();

        if (study == null) {
            log.trace("Cannot get rid of a duplicated assay associated to a node without study, node:" + node.getAcc());
            return;
        }

        Collection<AssayGroup> assayGroups = getStore().valuesOfType(AssayGroup.class);

        Collection<Assay> assays = new ArrayList<Assay>(study.getAssays());
        for (Assay assay : assays) {
            if (!(assay.getStudy() == study && assay.getMaterial() == material)) {
                continue;
            }
            for (AssayGroup ag : assayGroups) {
                if (ag.getStudy() != study) {
                    continue;
                }
                ag.removeAssay(assay);
            }
            study.removeAssay(assay);
        }
    }


    /**
     * Removes an assay associated to the study in the node parameter and to the material parameter. The assay is removed
     * from the study it points to, and this way becomes unreachable/garbaged.
     */
    private void removeAssayResult(DataNode node, Data data) {
        Study study = node.getStudy();
        if (study == null) {
            // TODO: exception?
            log.trace("WARNING: Cannot get rid of a duplicated assay associated to a node without study, node:" + node.getAcc());
            return;
        }

        Collection<AssayResult> ars = new ArrayList<AssayResult>(study.getAssayResults());
        for (AssayResult ar : ars) {
            if (ar.getStudy() == study && ar.getData() == data) {
                study.removeAssayResult(ar);
            }
        }
    }


    /**
     * Setup the cascaded properties in the assay results, by getting them from the associated material and its downstream
     * experimental pipeline.
     */
    private void setupCascadedProperties() {
        for (Study study : getStore().valuesOfType(Study.class)) {
            for (AssayResult ar : study.getAssayResults()) {
                // Now it's a set, so dupes should be avoided automatically
                for (CharacteristicValue prop : ar.findPipelineCharacteristicValues()) {
                    ar.addCascadedPropertyValue(prop);
                }
            }
        }
    }


    /**
     * Copies the factor values attached to samples to the data objects associated to the assay results.
     * This is needed in order to cope with the splitting between study file and assay files.
     */
    private void copyFactorValuesFromSamples() {
        for (Study study : getStore().valuesOfType(Study.class)) {
            Set<AssayResult> assayResults = new HashSet<AssayResult>(study.getAssayResults());
            for (AssayResult assayResult : assayResults) {
                final Collection<FactorValue> fvs = new ArrayList<FactorValue>();

                graphVisitor.setActionAndReset(
                        new ProcessingVisitAction() {
                            private Set<MaterialNode> visitedNodes = new HashSet<MaterialNode>();

                            public boolean visit(GraphElement graphElement) {
                                if (!(graphElement instanceof MaterialNode)) {
                                    return true;
                                }
                                MaterialNode node = ((MaterialNode) graphElement);
                                if (visitedNodes.contains(node)) {
                                    return true;
                                }
                                visitedNodes.add(node);

                                Material material = node.getMaterial();
                                if (!(material instanceof Material)) {
                                    return true;
                                }

                                fvs.addAll(((Material) material).getFactorValues());
                                return true;
                            }
                        }
                );

                Data arData = assayResult.getData();
                DataNode dataNode = arData.getProcessingNode();
                graphVisitor.visitBackward(dataNode);
                for (FactorValue fv : fvs) {
                    assayResult.addCascadedPropertyValue(fv);
                }
            }
        }
    }


    /**
     * Setup the links between assay-results and the assays these derives from.
     */
    private void setupAssayResult2AssayLinks() {
        for (Study study : getStore().valuesOfType(Study.class)) {
            for (final Assay assay : study.getAssays()) {
                for (AssayResult ar : ProcessingUtils.findAssayResultsFromAssay(assay)) {
                    ar.addAssay(assay);
                }
            }
        }
    }


    /**
     * Checks that for each assay there is at least one assay result. Until better solution is found for the AR problem
     */
    private void checkAssayResultsExist() {
        for (Study study : getStore().valuesOfType(Study.class)) {
            Investigation investigation = study.getUniqueInvestigation();
            if (investigation != null) {
                ndc.pushObject(investigation);
            }
            ndc.pushObject(study);

            for (Assay assay : study.getAssays()) {
                ndc.pushObject(assay.getMaterial());
                if (ProcessingUtils.findAssayResultsFromAssay(assay).size() == 0) {
                    String msg = "WARNING, No assay result found for the assay \"" + assay +
                            "\", you might need certain node types, such as 'Normalization Name'. The assay won't have any property or" +
                            "factor value.";
                    log.warn(msg);
                }
                ndc.popObject(); // material
            }

            ndc.popObject(); // Study
            if (investigation != null) {
                ndc.popObject();
            }
        }
    }


    /**
     * Checks that declared entities (source, protocols, factors) are actually used. Logs warnings in case not.
     */
    private void checkUnusedDeclaredEntities() {
        BIIObjectStore store = getStore();

        // Unused Ontology Sources
        TabMappingContext context = store.valueOfType(TabMappingContext.class);
        for (ReferenceSource source : store.valuesOfType(ReferenceSource.class)) {
            if (!context.containsKey("used.oeSource." + source.getAcc())) {
                log.warn("WARNING: the ontology source '" + source.getAcc() + "' is declared in the investigation file but never used");
            }
        }

        // Unused Factor types
        Set<String> factorKeys = store.typeKeys(FactorTypeHelper.class);
        if (factorKeys != null) {
            for (String factorKey : factorKeys) {
                if (!context.containsKey("used.factorType." + factorKey)) {
                    FactorTypeHelper factor = store.getType(FactorTypeHelper.class, factorKey);
                    log.warn(
                            "WARNING: the factor type '" + factor.getValue()
                                    + "' is declared in the investigation file for the study '" + StudyWrapper.getLabel(factor.getStudy())
                                    + " but is never used"
                    );
                }
            }
        }

        // Unused Protocols and Protocol Parameters
        Set<String> protoKeys = store.typeKeys(Protocol.class);
        if (protoKeys != null) {
            for (String protoKey : protoKeys) {
                Protocol protocol = store.getType(Protocol.class, protoKey);
                String usedPref = "used.protocol." + protoKey;
                String studyAcc = protoKey.substring(0, protoKey.indexOf('\\'));
                String studyLabel = StudyWrapper.getLabel(store.getType(Study.class, studyAcc));


                if (!context.containsKey(usedPref)) {
                    log.warn("WARNING: the protocol '" + protocol.getName()
                            + "' is declared in the investigation file for the study '" + studyLabel + "' but is never used");
                    // Don't check the parameter, the whole protocol is not used at all...
                    continue;
                }

                for (Parameter param : protocol.getParameters()) {
                    String paramName = param.getValue();
                    if (!context.containsKey(usedPref + "\\" + paramName)) {
                        log.warn("WARNING: the parameter '" + paramName + "' for the protocol '" + protocol.getName()
                                + "' is declared in the investigation file for the study '" + studyLabel + "' but never is used");
                    }
                }
            }
        }

    }


    /**
     * Provides a human-readable report of the BII objects in the store.
     * TODO: this methods should be in the model, but together with {@link AssayGroup}.
     */
    public static String report(BIIObjectStore store) {

        StringWriter outb = new StringWriter();
        PrintWriter out = new PrintWriter(outb);
        Collection<AssayGroup> assayGroups = store.valuesOfType(AssayGroup.class);
        final ExperimentalPipelineVisitor graphVisitor = new ExperimentalPipelineVisitor();

        Investigation investigation = store.valueOfType(Investigation.class);

        out.println("\n======================== Investigation: "
                + (investigation == null
                ? "[none]"
                : investigation.getAcc() + " (" + investigation.getTitle() + ")"
        )
        );
        for (Study study : store.valuesOfType(Study.class)) {
            out.println("\n======== Study: " + study.getAcc() + " (" + study.getTitle() + ")");
            out.println("==== Sample file: " + StudyWrapper.findSampleFile(store, study));

            // First count the sources/samples
            //
            NodeCountVisitor visitor = new NodeCountVisitor();
            graphVisitor.setActionAndReset(visitor);
            for (Assay assay : study.getAssays()) {
                graphVisitor.visit(assay.getMaterial().getMaterialNode());
            }

            out.println("  A total of " + study.getAssays().size() + " asssay(s) found for this study and");
            out.println("  " + visitor.getSourceCt() + " source material(s) and");
            out.println("  " + visitor.getSampleCt() + " sample material(s).");

            // Then we have to go back to the graph for AG totals
            //
            for (AssayGroup ag : assayGroups) {
                if (ag.getStudy() != study) {
                    continue;
                }

                visitor = new NodeCountVisitor();
                graphVisitor.setActionAndReset(visitor);

                Collection<Assay> assays = ag.getAssays();
                for (Assay assay : assays) {
                    graphVisitor.visit(assay.getMaterial().getMaterialNode());
                }

                out.println("==== Assay File: " + ag.getFilePath()
                        + " type: " + ag.getMeasurement().getName()
                        + ", technology: " + ag.getTechnologyName()
                        + ", platform: " + ag.getPlatform());

                //out.println ( "  This assay file uses " + visitor.getSourceCt () + " source material(s) and" );
                //out.println ( "  " + visitor.getSampleCt () + " sample material(s)." );
                out.println("  " + assays.size() + " assay(s) found in the assay file, which has/have produced");
                out.println("  " + visitor.getRawDataCt() + " raw data items (s) and");
                out.println("  " + visitor.getDerivedDataCt() + " derived data item(s)");
            } // assay-group
        } // study
        return outb.toString();
    }

    /**
     * Creates a report ready for insertion between html tags...for display in GUI. It is similar
     * to {@link #report(BIIObjectStore)}, but kept separated so that we don't mess up it too much.
     */
    public static String htmlReport(BIIObjectStore store) {

        StringWriter outb = new StringWriter();
        PrintWriter out = new PrintWriter(outb);
        Collection<AssayGroup> assayGroups = store.valuesOfType(AssayGroup.class);
        final ExperimentalPipelineVisitor graphVisitor = new ExperimentalPipelineVisitor();

        Investigation investigation = store.valueOfType(Investigation.class);
        out.println("<div>");
        out.println("<p> <strong>Investigation:</strong> "
                + (investigation == null
                ? "[none]"
                : investigation.getAcc() + " (" + investigation.getTitle() + ")</p>"
        )
        );
        out.println("<ul>");
        for (Study study : store.valuesOfType(Study.class)) {
            out.println("<li><strong>Study: </strong>" + study.getAcc() + " (" + study.getTitle() + ")");
            out.println("<ul>");
            // First count the sources/samples
            //
            NodeCountVisitor visitor = new NodeCountVisitor();
            graphVisitor.setActionAndReset(visitor);
            for (Assay assay : study.getAssays()) {
                graphVisitor.visit(assay.getMaterial().getMaterialNode());
            }

            out.println("<li><strong>Sample file: </strong>" + StudyWrapper.findSampleFile(store, study) + " with " + study.getAssays().size() + " samples");
            out.println("<ul>");
            // Then we have to go back to the graph for AG totals
            //
            for (AssayGroup ag : assayGroups) {
                if (ag.getStudy() != study) {
                    continue;
                }

                visitor = new NodeCountVisitor();
                graphVisitor.setActionAndReset(visitor);

                Collection<Assay> assays = ag.getAssays();
                for (Assay assay : assays) {
                    graphVisitor.visit(assay.getMaterial().getMaterialNode());
                }

                out.println("<li>Assay File: " + ag.getFilePath() + " with " + assays.size() + " assays");
                out.println("<ul>");
                out.println("<li> <i>measurement </i> " + ag.getMeasurement().getName() + "</li>");
                out.println("<li> <i>technology </i> " + ag.getTechnologyName() + "</li>");
                out.println("<li> <i>platform </i> " + ag.getPlatform() + "</li>");

                out.println("</ul>");
                out.println("</li>");
            } // assay-group
            out.println("</ul>");
            out.println("</li>");
            out.println("</ul>");
            out.println("</li>");
        } // study
        out.println("</ul>");
        out.println("</div>");
        return outb.toString();
    }


    /**
     * Merges equivalent processings, ie: those having the same Protocol REF, same parameter values and
     * that came from adjacent rows.
     * <p/>
     * Processings with at least one proto application with no protocol specified are always different.
     */
    @SuppressWarnings("unchecked")
    private void mergeProcessings() {
        BIIObjectStore store = getStore();

        // colId, rowOrder => Proc
        SortedObjectStore<String, Integer, Processing> classifiedProcs =
                new SortedObjectStore<String, Integer, Processing>();

        for (Processing p : store.valuesOfType(Processing.class)) {
            String colId = p.getSingleAnnotationValue(ProcessingsTabMapper.PROCESSING_COL_ID_ANN_TAG);
            int rowOrder = Integer.parseInt(
                    p.getSingleAnnotationValue(ProcessingsTabMapper.PROCESSING_ROW_ORDER_ANN_TAG)
            );
            classifiedProcs.put(colId, rowOrder, p);
        }

        for (String colId : classifiedProcs.types()) {
            Processing prevProc = null;
            ComparedNodeWrapper wPrevProc = null;
            Boolean isPrevUniqueApp = null;

            for (int row : classifiedProcs.typeKeys(colId)) {
                Processing p = classifiedProcs.get(colId, row);
                ComparedNodeWrapper wp = new ComparedNodeWrapper(p);
                Boolean isUniqueApp = p.isUniqueApplication();

                if (wPrevProc == null
                        || !(isPrevUniqueApp != null && isPrevUniqueApp && isUniqueApp != null && isUniqueApp)
                        || !wPrevProc.equals(wp)) {
                    prevProc = p;
                    wPrevProc = wp;
                    isPrevUniqueApp = isUniqueApp;
                } else {
                    mergeProcessingPair(prevProc, p);
                }
            }
        }

        // And now the processings that share outputs
        // Retrieve them this way, so that you won't get ConcurrentModificationEx
        for (Processing p1 : new LinkedList<Processing>(store.valuesOfType(Processing.class))) {
            Boolean isUniqueApp = p1.isUniqueApplication();
            if (isUniqueApp != null && !isUniqueApp) {
                continue;
            }

            for (Node out : (Collection<Node>) p1.getOutputNodes()) {
                for (Processing p2 : new LinkedList<Processing>(out.getDownstreamProcessings())) {
                    Boolean p2UniqueApp = p2.isUniqueApplication();
                    if (p1 != p2 && (p2UniqueApp == null || p2UniqueApp)
                            && new ComparedNodeWrapper(p1).equals(new ComparedNodeWrapper(p2))) {
                        mergeProcessingPair(p1, p2);
                    }
                }
            }
        }
    }


    private boolean mergeProcessingPair(Processing p1, Processing p2) {
        if (p1 == p2) {
            return false;
        }

        // row order and repetitions
        //

        Annotation p1RowOrderAnn = p1.getSingleAnnotation(ProcessingsTabMapper.PROCESSING_ROW_ORDER_ANN_TAG),
                p2RowOrderAnn = p2.getSingleAnnotation(ProcessingsTabMapper.PROCESSING_ROW_ORDER_ANN_TAG);

        int p1RowOrder = Integer.parseInt(p1RowOrderAnn.getText()),
                p2RowOrder = Integer.parseInt(p2RowOrderAnn.getText());

        p1.removeAnnotation(p1RowOrderAnn);
        p1.addAnnotation(new Annotation(
                p1RowOrderAnn.getType(),
                Integer.toString(Math.min(p1RowOrder, p2RowOrder))
        ));

        for (Node input : new LinkedList<Node>(p2.getInputNodes())) {
            p2.removeInputNode(input);
            p1.addInputNode(input);
        }

        for (Node output : new LinkedList<Node>(p2.getOutputNodes())) {
            p2.removeOutputNode(output);
            p1.addOutputNode(output);
        }

        getStore().put(Processing.class, p2.getAcc(), null);
        return true;
    }


    /**
     * Removes those paths that don't touch any assay. These are not supported by the BII DB.
     */
    private void removeDeadPaths() {
        AssayFinder afinder = new AssayFinder();

        for (Node node : new LinkedList<Node>(getStore().valuesOfType(Node.class))) {
            if (node.getDownstreamProcessings().isEmpty()) {
                afinder.findPathAndAssayFrom(node, new LinkedList<GraphElement>());
            }
        }
    }

    private class AssayFinder {
        private Set<String> assayMaterialAccs = new HashSet<String>();

        AssayFinder() {
            for (Study study : getStore().valuesOfType(Study.class)) {
                for (Assay assay : study.getAssays()) {
                    assayMaterialAccs.add(assay.getMaterial().getAcc());
                }
            }
        }

        private void findPathAndAssayFrom(Node node, List<GraphElement> path) {
            List<GraphElement> newPath = new LinkedList<GraphElement>();
            newPath.addAll(path);
            newPath.add(node);

            if (node instanceof MaterialNode) {
                Material mater = ((MaterialNode) node).getMaterial();
                if (assayMaterialAccs.contains(mater.getAcc())) {
                    return;
                }
            }

            Collection<Processing> procs = new LinkedList<Processing>(node.getUpstreamProcessings());
            if (!procs.isEmpty()) {
                for (Processing proc : procs) {
                    findPathAndAssayFrom(proc, newPath);
                }
                return;
            }

            // This is a dead end: let's remove it and all the preceding nodes that only contribute to this
            //
            BIIObjectStore store = getStore();
            for (int i = newPath.size() - 1; i >= 0; i--) {
                GraphElement ge = newPath.get(i);
                if (ge instanceof Node) {
                    Node pnode = (Node) ge;

                    // If this node has at most one left process or one right proc, we can remove it.
                    //
                    Collection<Processing> inprocs = pnode.getDownstreamProcessings(),
                            outprocs = pnode.getUpstreamProcessings();
                    if (inprocs.size() > 1 || outprocs.size() > 1) {
                        continue;
                    }

                    for (Processing proc : new LinkedList<Processing>(outprocs)) {
                        pnode.removeUpstreamProcessing(proc);
                    }
                    for (Processing proc : new LinkedList<Processing>(inprocs)) {
                        pnode.removeDownstreamProcessing(proc);
                    }
                    store.put(Node.class, pnode.getAcc(), null);
                } else {
                    Processing proc = (Processing) ge;

                    // If this processing has at most one input or one output, it can be removed
                    //
                    Collection<Node> ins = proc.getInputNodes(),
                            outs = proc.getOutputNodes();
                    if (ins.size() > 1 || outs.size() > 1) {
                        continue;
                    }

                    for (Node pnode : new LinkedList<Node>(ins)) {
                        proc.removeInputNode(pnode);
                    }
                    for (Node pnode : new LinkedList<Node>(outs)) {
                        proc.removeOutputNode(pnode);
                    }
                    store.put(Processing.class, proc.getAcc(), null);
                }
            }
        }

        private void findPathAndAssayFrom(Processing proc, List<GraphElement> path) {
            List<GraphElement> newPath = new LinkedList<GraphElement>();
            newPath.addAll(path);
            newPath.add(proc);

            for (Node out : new LinkedList<Node>(proc.getOutputNodes())) {
                findPathAndAssayFrom(out, newPath);
            }
        }
    }


    /**
     * It's used by {@link ISATABMapper#report(BIIObjectStore)} for walking over all the nodes and counting the types in the
     * graph.
     * <p/>
     * TODO: this methods should be in the model, but together with {@link AssayGroup}.
     *
     * @author brandizi
     *         <b>date</b>: Apr 16, 2009
     */
    private static class NodeCountVisitor implements ProcessingVisitAction {
        private int sourceCt = 0, sampleCt = 0, rawDataCt = 0, derivedDataCt = 0;

        public boolean visit(GraphElement node) {
            if (node instanceof MaterialNode) {
                Material material = ((MaterialNode) node).getMaterial();
                String typeAcc = material.getType().getAcc();
                if ("bii:source".equals(typeAcc)) {
                    sourceCt++;
                } else if ("bii:sample".equals(typeAcc)) {
                    sampleCt++;
                }
            }
            if (node instanceof DataNode) {
                Data data = ((DataNode) node).getData();
                String typeAcc = data.getType().getAcc();
                if (typeAcc.endsWith("_raw_data")) {
                    rawDataCt++;
                } else if (typeAcc.endsWith("_derived_data") || typeAcc.endsWith("_normalized_data")) {
                    derivedDataCt++;
                }
            }
            return true;
        }

        public int getSourceCt() {
            return sourceCt;
        }

        public int getSampleCt() {
            return sampleCt;
        }

        public int getRawDataCt() {
            return rawDataCt;
        }

        public int getDerivedDataCt() {
            return derivedDataCt;
        }

    }

    /**
     * This implements the criteria that are used to determine, during mapping, if two nodes are to be considered
     * equivalent. The class simply wraps {@link Node} and implements {@link #equals(Object)} and {@link #hashCode()}.
     * <p/>
     * TODO: probably this should simply go inside {@link Node}, {@link MaterialNode} etc.
     *
     * @author brandizi
     *         <b>date</b>: Apr 24, 2009
     */
    private static class ComparedNodeWrapper {
        private GraphElement base;

        public ComparedNodeWrapper(GraphElement base) {
            if (base == null) {
                throw new TabInternalErrorException(i18n.msg("null_base_CompareNodeWrapper"));
            }
            this.base = base;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null) {
                return false;
            }
            if (!(o instanceof ComparedNodeWrapper)) {
                return false;
            }

            final ComparedNodeWrapper other = (ComparedNodeWrapper) o;
            if (this.base == other.base) {
                return true;
            } // should not happen, but anyway...

            final Study study = base.getStudy(), ostudy = other.base.getStudy();
            if (study != ostudy) {
                return false;
            }
            if (study == null) {
                return ostudy == null;
            }
            if (ostudy == null) {
                return false;
            }

            final String studyAcc = study.getAcc(), ostudyAcc = ostudy.getAcc();
            if (studyAcc != null ? !studyAcc.equals(ostudyAcc) : ostudyAcc != null) {
                return false;
            }

            if (base instanceof MaterialNode) {
                if (!(other.base instanceof MaterialNode)) {
                    return false;
                }
                final Material material = ((MaterialNode) base).getMaterial(),
                        omaterial = ((MaterialNode) other.base).getMaterial();
                if (material == omaterial) {
                    return true;
                }
                if (material == null) {
                    return omaterial == null;
                }
                if (omaterial == null) {
                    return false;
                }

                final String macc = material.getAcc(), omacc = omaterial.getAcc();
                if (macc != null ? !macc.equals(omacc) : omacc != null) {
                    return false;
                }

                final MaterialRole mtype = material.getType(), omtype = omaterial.getType();
                if (mtype == omtype) {
                    return true;
                }
                if (mtype == null) {
                    return omtype == null;
                }
                if (omtype == null) {
                    return false;
                }

                final String mtypeAcc = mtype.getAcc(), omtypeAcc = omtype.getAcc();
                if (mtypeAcc != null ? !mtypeAcc.equals(omtypeAcc) : omtypeAcc != null) {
                    return false;
                }
            } else if (base instanceof DataNode) {
                if (!(other.base instanceof DataNode)) {
                    return false;
                }
                final Data data = ((DataNode) base).getData(),
                        odata = ((DataNode) other.base).getData();
                if (data == odata) {
                    return true;
                }
                if (data == null) {
                    return odata == null;
                }
                if (odata == null) {
                    return false;
                }

                final String macc = data.getAcc(), omacc = odata.getAcc();
                if (macc != null ? !macc.equals(omacc) : omacc != null) {
                    return false;
                }

                final DataType dtype = data.getType(), odtype = odata.getType();
                if (dtype == odtype) {
                    return true;
                }
                if (dtype == null) {
                    return odtype == null;
                }
                if (odtype == null) {
                    return false;
                }

                final String mtypeAcc = dtype.getAcc(), omtypeAcc = odtype.getAcc();
                if (mtypeAcc != null ? !mtypeAcc.equals(omtypeAcc) : omtypeAcc != null) {
                    return false;
                }
            } else if (base instanceof Processing) {
                if (!(other.base instanceof Processing)) {
                    return false;
                }

                final Processing<?, ?> step = (Processing<?, ?>) base,
                        ostep = (Processing<?, ?>) other.base;

                Collection<ProtocolApplication> stepPapps = step.getProtocolApplications(),
                        ostepPapps = ostep.getProtocolApplications();

                // If either processing is missing the protocols, we must assume they're different, because there are
                // protocols applied but nothing is said on the fact they're the same applications.
                if (stepPapps == null || ostepPapps == null || stepPapps.isEmpty() || ostepPapps.isEmpty()
                        || !Arrays.deepEquals(stepPapps.toArray(), ostepPapps.toArray())) {
                    return false;
                }
            } else {
                throw new TabInternalErrorException(i18n.msg("unknown_node_type", base.getClass().getSimpleName()));
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = 0;
            final Study study = base.getStudy();
            if (study != null) {
                final String studyAcc = study.getAcc();
                if (studyAcc != null) {
                    result = studyAcc.hashCode();
                }
            }
            if (base instanceof MaterialNode) {
                final Material material = ((MaterialNode) base).getMaterial();
                if (material != null) {
                    final MaterialRole mtype = material.getType();
                    if (mtype != null) {
                        final String mtypeAcc = mtype.getAcc();
                        result = 31 * result + (mtypeAcc == null ? 0 : mtypeAcc.hashCode());
                    }
                    final String macc = material.getAcc();
                    result = 31 * result + (macc == null ? 0 : macc.hashCode());
                }
            } else if (base instanceof DataNode) {
                final Data data = ((DataNode) base).getData();
                if (data != null) {
                    final DataType dtype = data.getType();
                    if (dtype != null) {
                        final String dtypeAcc = dtype.getAcc();
                        result = 31 * result + (dtypeAcc == null ? 0 : dtypeAcc.hashCode());
                    }
                    final String dacc = data.getAcc();
                    result = 31 * result + (dacc == null ? 0 : dacc.hashCode());
                }
            } else if (base instanceof Processing) {
                Processing<?, ?> step = (Processing<?, ?>) base;

                result = 31 * result + Arrays.deepHashCode(step.getProtocolApplications().toArray());

                Collection<ProtocolApplication> stepPapps = step.getProtocolApplications();
                result = 31 * result + (stepPapps == null ? 0 : Arrays.deepHashCode(stepPapps.toArray()));

            } else {
                throw new TabInternalErrorException(
                        i18n.msg("unknown_node_type", base.getClass().getSimpleName())
                );
            }
            return result;
        }

        public GraphElement getBase() {
            return base;
        }
    }


    private void checkInconsistentProperties() {
        NodePropertyChecker visitor = new NodePropertyChecker();
        final ExperimentalPipelineVisitor graphVisitor = new ExperimentalPipelineVisitor(visitor);
        for (Study study : getStore().valuesOfType(Study.class)) {
            for (Assay assay : study.getAssays()) {
                graphVisitor.visit(assay.getMaterial().getMaterialNode());
            }
        }

        Set<String> messages = visitor.getMessages();

        for (String message : messages) {
            log.debug(message);
        }

        if (messages.size() != 0) {
            log.warn(i18n.msg("error_property_inconsistencies"));
        }
    }


    /**
     * This visitor is used in {@link ISATABMapper#checkInconsistentProperties()} to check that a node has not
     * two different values for a property (charcateristics or parameters or annotations) of the same type.
     *
     * @author brandizi
     *         <b>date</b>: May 9, 2009
     */
    private static class NodePropertyChecker implements ProcessingVisitAction {
        private final Set<String> messages = new TreeSet<String>();

        /**
         * We collect all the warning messages here, so that the potentially many repetitions that can occur can
         * be printed only once.
         */
        public Set<String> getMessages() {
            return messages;
        }

        public boolean visit(GraphElement node) {

            if (node instanceof MaterialNode) {
                Material material = ((MaterialNode) node).getMaterial();
                String materialLabel = "label " + material.getName();
                checkProperties(material.getCharacteristicValues(), materialLabel);
                checkAnnotations(material.getAnnotations(), materialLabel);

            } // if ( is material )

            else if (node instanceof DataNode) {
                Data data = ((DataNode) node).getData();
                checkAnnotations(data.getAnnotations(), "data " + data.getName());

            } // if ( is data )

            else if (node instanceof Processing) {
                Processing<?, ?> step = (Processing<?, ?>) node;
                Collection<ProtocolApplication> protoApps = step.getProtocolApplications();
                if (protoApps != null) {
                    for (ProtocolApplication protoApp : protoApps) {
                        Protocol proto = protoApp.getProtocol();
                        String plabel = "protocol " + proto.getName();
                        checkProperties(protoApp.getParameterValues(), plabel);
                        checkAnnotations(protoApp.getAnnotations(), "protocol " + plabel);
                    }
                }
            } // if ( is Processing )

            return true;
        }


        private <PV extends PropertyValue<?>> boolean checkProperties(Collection<PV> pvalues, String nodeLabel) {
            Map<String, PV> matchedValues = new HashMap<String, PV>();
            for (PV cvalue : pvalues) {
                Property<?> ptype = cvalue.getType();
                String ptypeStr = ptype.getValue();
                PV existval = matchedValues.get(ptypeStr);
                if (existval == null) {
                    matchedValues.put(ptypeStr, cvalue);
                    continue;
                }
                int oldOrder = ptype.getOrder();
                ptype.setOrder(oldOrder);
                if (existval.equals(cvalue)) {
                    ptype.setOrder(oldOrder);
                    continue;
                }

                // They're different
                //
                ptype.setOrder(oldOrder);
                String cvalStr = cvalue.getValue(), existvalStr = existval.getValue();
                UnitValue cvalu = cvalue.getUnit(), existvalu = existval.getUnit();
                String cavaluStr = cvalu == null ? "" : cvalu.getValue();
                String existvaluStr = existvalu == null ? "" : existvalu.getValue();

                String ptypeLabel = "[unknown type]";
                if (ptype instanceof Characteristic) {
                    ptypeLabel = "characteristic";
                } else if (ptype instanceof Factor) {
                    ptypeLabel = "factor";
                } else if (ptype instanceof Parameter) {
                    ptypeLabel = "parameter";
                }

                messages.add(i18n.msg("inconsistent_property_values",
                        ptypeLabel,
                        existvalStr, existvaluStr, ptypeStr,
                        cvalStr, cavaluStr,
                        nodeLabel
                ));
            }
            return false;
        }


        private boolean checkAnnotations(final Collection<Annotation> annotations, final String nodeLabel) {
            boolean result = true;
            Map<AnnotationType, Annotation> matchedAnnotations = new HashMap<AnnotationType, Annotation>();
            if (annotations == null) {
                return true;
            }
            for (Annotation ann : annotations) {
                AnnotationType type = ann.getType();
                Annotation existAnn = matchedAnnotations.get(type);
                if (existAnn == null) {
                    matchedAnnotations.put(type, ann);
                    continue;
                }
                if (existAnn.equals(ann)) {
                    continue;
                }
                messages.add(i18n.msg("inconsistent_annotations", existAnn.getText(), type.getValue(), ann.getText(), nodeLabel));
                result = false;
            }
            return result;
        }
    }


    /**
     * Checks some chaining rules:
     * if ( node is source ) => must be the first, must have upstream nodes
     * if ( other material ) => must have downstream nodes, must have materials or data
     * if ( raw data ) => must have downstream nodes, should have upstream derived data
     * if ( derived data ) => must have downstream nodes
     */
    private void checkChaining() {
        final ChainingChecker visitor = new ChainingChecker();
        final ExperimentalPipelineVisitor graphVisitor = new ExperimentalPipelineVisitor(visitor);
        for (Study study : getStore().valuesOfType(Study.class)) {
            for (Assay assay : study.getAssays()) {
                graphVisitor.visit(assay.getMaterial().getMaterialNode());
            }
        }

        Set<String> messages = visitor.getMessages();

        for (String message : messages) {
            log.debug(message);
        }

        if (messages.size() != 0) {
            log.warn(i18n.msg("error_pipeline_inconsistencies"));
        }
    }


    /**
     * Used by {@link ISATABMapper#checkChaining()}
     *
     * @author brandizi
     *         <b>date</b>: May 12, 2009
     */
    private static class ChainingChecker implements ProcessingVisitAction {
        private final Set<String> messages = new TreeSet<String>();

        /**
         * We collect all the warning messages here, so that the potentially many repetitions that can occur can
         * be printed only once.
         */
        public Set<String> getMessages() {
            return messages;
        }

        public boolean visit(GraphElement node) {
            if (node instanceof MaterialNode) {
                MaterialNode mnode = (MaterialNode) node;
                Material material = mnode.getMaterial();
                MaterialRole type = material.getType();
                String typeAcc = type.getAcc();
                String mname = material.getName(),
                        mtypeName = type.getName();

                if ("bii:source".equals(typeAcc)) {
                    if (mnode.getDownstreamProcessings().size() != 0) {
                        messages.add(i18n.msg("error_source_is_not_first", mname, mtypeName));
                    }
                } else {
                    if (mnode.getDownstreamProcessings().size() == 0) {
                        messages.add(i18n.msg("error_material_is_first", mname, mtypeName));
                    }
                }

                if (mnode.getUpstreamProcessings().size() == 0) {
                    messages.add(i18n.msg("error_material_is_last", mname, mtypeName));
                }
            }
            // if ( material )

            if (node instanceof DataNode) {
                DataNode dnode = (DataNode) node;
                Data data = dnode.getData();
                DataType type = data.getType();
                String typeAcc = type.getAcc();
                String dname = data.getName(),
                        dtypeName = type.getName();

                if (dnode.getDownstreamProcessings().size() == 0) {
                    messages.add(i18n.msg("error_data_is_first", dname, dtypeName));
                }

                if (typeAcc.endsWith("_raw_data")) {
                    if (dnode.getUpstreamProcessings().size() == 0) {
                        messages.add(i18n.msg("error_data_is_last", dname, dtypeName));
                    }
                }
            }
            return true;
        }
        // if ( data )

    }

}