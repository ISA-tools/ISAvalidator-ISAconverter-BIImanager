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

package org.isatools.tablib.mapping.pipeline;

import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.commons.lang.StringUtils;
import org.isatools.isatab.mapping.AssayGroup;
import org.isatools.isatab.mapping.ISATABMapper;
import org.isatools.tablib.exceptions.TabInternalErrorException;
import org.isatools.tablib.exceptions.TabStructureError;
import org.isatools.tablib.mapping.MappingUtils;
import org.isatools.tablib.mapping.SectionTabMapper;
import org.isatools.tablib.schema.Field;
import org.isatools.tablib.schema.Record;
import org.isatools.tablib.schema.Section;
import org.isatools.tablib.schema.SectionInstance;
import org.isatools.tablib.schema.constraints.FieldCardinalityConstraint;
import org.isatools.tablib.schema.constraints.FieldConstraint;
import org.isatools.tablib.schema.constraints.FollowsConstraint;
import org.isatools.tablib.schema.constraints.PrecedesConstraint;
import org.isatools.tablib.utils.BIIObjectStore;
import org.isatools.tablib.utils.logging.TabNDC;
import uk.ac.ebi.bioinvindex.model.Annotation;
import uk.ac.ebi.bioinvindex.model.Data;
import uk.ac.ebi.bioinvindex.model.Material;
import uk.ac.ebi.bioinvindex.model.Study;
import uk.ac.ebi.bioinvindex.model.processing.*;
import uk.ac.ebi.bioinvindex.model.term.AnnotationType;
import uk.ac.ebi.bioinvindex.utils.datasourceload.DataLocationManager;
import uk.ac.ebi.bioinvindex.utils.i18n;

import java.util.*;


/**
 * The processings mapper. Analyzes all the experimental pipeline's nodes and protocol applications
 * and build a graph of {@link Processing} with them.
 * You may configure a sequence of {@link ProcessingNodeTabMapper}(s) and {@link AbstractProtocolApplicationTabMapper}(s)
 * this mapper has to expect from the pipeline, via the {@link #nodeMappersConfig} variable.
 * <p/>
 * Feb 6, 2008
 *
 * @author brandizi
 */
public abstract class ProcessingsTabMapper extends SectionTabMapper {
    private List<? extends ProcessingEntityTabMapper<?>> nodeMappers;
    protected final MappingUtils mappingUtils;

    /**
     * This annotation is added to {@link Processing} objects, so that it's possible to know those processings
     * coming from the same column. It's used by {@link ISATABMapper}, to merge equivalent processings.
     */
    public static final String PROCESSING_COL_ID_ANN_TAG = "processingColId";

    /**
     * This annotation is used to add the row order to {@link Processing} objects, which is needed
     * to merge equivalent processings.
     */
    public static final String PROCESSING_ROW_ORDER_ANN_TAG = "rowOrder";

    /**
     * The node mapper's configuration. For each node mapper, this processing mapper expect to find information
     * to feed the node mapper with, starting from the field named by the key in this Java map.

     * we do not do checkings on the order of node types (e.g.: a source precedes a sample), this is left
     * to the validator.
     */
    protected Map<String, Class<? extends ProcessingEntityTabMapper<?>>> nodeMappersConfig =
            new HashMap<String, Class<? extends ProcessingEntityTabMapper<?>>>();

    /**
     * You should provide a value for {@link #nodeMappersConfig}
     */
    public ProcessingsTabMapper(BIIObjectStore store, SectionInstance sectionInstance) {
        super(store, sectionInstance);
        mappingUtils = new MappingUtils(store);
    }


    /**
     * Do some decorations (such as logging or header's validation and then calls {@link #map(int)} over all rows.
     */
    @Override
    public BIIObjectStore map() {

        SectionInstance sectionInstance = getSectionInstance();
        String sectionName = sectionInstance.getSection().getId();
        String fileName = sectionInstance.getFileId();
        BIIObjectStore store = getStore();
        String logpref = String.format("Mapping records in the section '%s' / '%s': ", fileName, sectionName);

        TabNDC ndc = TabNDC.getInstance();
        ndc.pushSection(sectionInstance);

        try {
            // Assign this assay section to the assay group
            AssayGroup assayGroup = mappingUtils.getAssayGroupFromSection(sectionInstance);
            if (assayGroup != null) {
                assayGroup.setAssaySectionInstance(sectionInstance);
            }

            log.trace("\n\n" + logpref + "start");

            List<Record> records = sectionInstance.getRecords();
            int sz = records == null ? 0 : records.size();

            if (sz == 0) {
                log.trace(logpref + "WARNING, attempt to map the empty section " + sectionName);
                return null;
            }

            // Do some validation
            if (!validateHeaders()) {
                throw new TabStructureError("Errors in fields order/structure with the file \"" + fileName
                        + "\", see the log messages for details");
            }

            // Process all the rows
            for (int i = 0; i < sz; i++) {
                this.map(i);
            }
        }
        finally {
            log.trace(logpref + "returning with " + store.size() + " objects \n\n");
            ndc.popTabDescriptor();
        }
        return store;
    }

    /**
     * Produces a list of indexes which represent the splitting of headers into node headers and protocol reference
     * headers.
     *
     * @return a list l of field indexes, such that:
     *         all l[i] are indexes of {@link ProcessingNodeTabMapper}(s), if l[i+1] - l[i] > 1 all the mappers in
     *         between are {@link GenericProtocolApplicationTabMapper}(s).
     */
    @SuppressWarnings("unchecked")
    protected List<Integer> getFieldSplitting() {


        // Goes through the nodeMappers
        List<?> nodeMappers = getNodeMappers();
        List<Integer> fieldSplitting = new ArrayList<Integer>();

        int nodeMappersSize = nodeMappers.size();
        if (nodeMappersSize == 0) {
            throw new TabInternalErrorException("Error in pieline processing: no nodes to process");
        }

        int i = 0;
        while (i < nodeMappersSize) {
            // First one has to be the input
            if (i == 0) {
                ProcessingEntityTabMapper<?> inputMapper = ((List<ProcessingEntityTabMapper<?>>) nodeMappers).get(i);
                if (!(inputMapper instanceof ProcessingNodeTabMapper<?>)) {
                    String fieldName = inputMapper.getSectionInstance().getField(inputMapper.getFieldIndex()).getId();
                    log.trace(
                            "wrong mapper found where an Input mapper was expected. Field: " + fieldName
                                    + ", mapper found: " + inputMapper.getClass().getCanonicalName() + " (input/output mapper expected)"
                    );
                    throw new TabStructureError(
                            "Error in sample/assay file, bad column order with the field '" + fieldName + "'"
                                    + ". I expected an input node here (e.g.: material or data name), a Protocol REF was found instead"
                    );
                }
            }

            // the processing node, it is both input and output if 0 < i < nodeMappersSize - 1, it is an input only if
            // i == 0, it is an output only if i == nodeMappersSize - 1
            //
            fieldSplitting.add(i);

            // Then we should have protocols (or nothing)
            //
            ProcessingEntityTabMapper<?> protoMapper = null;
            for (i++; i < nodeMappersSize; i++) {
                protoMapper = ((List<ProcessingEntityTabMapper<?>>) nodeMappers).get(i);
                if (!(protoMapper instanceof AbstractProtocolApplicationTabMapper)) {
                    break;
                }
            }

            // and eventually the output. We do the checking twice (it is repeated at the loop begin) in order to
            // provide different error messages.
            //
            if (!(i == nodeMappersSize && protoMapper == null)
                    && !(i < nodeMappersSize && protoMapper instanceof ProcessingNodeTabMapper<?>)
                    ) {
                String fieldName = "[unknown]", mapperName = "[unknown]";
                if (protoMapper != null) {
                    fieldName = protoMapper.getSectionInstance().getField(protoMapper.getFieldIndex()).getId();
                    mapperName = protoMapper.getClass().getCanonicalName();
                }

                log.trace(
                        "in mapping the processing pipeline, last node is not an output, field: '" + fieldName + "'"
                                + ", mapper " + mapperName
                );

                throw new TabStructureError(
                        "Error in sample/assay file, bad column order with the field '" + fieldName + "'"
                                + ". Last column in the file must refer to an output node (e.g.: sample or data)"
                );
            }
        }

        if (log.isTraceEnabled()) {
            log.trace("ProcessingsMapper, Field Splitting: " + fieldSplitting);
        }

        return fieldSplitting;
    }


    /**
     * Goes through the processing splitting ({@link #getFieldSplitting()}) and builds the chain of processings
     * accordingly.
     */
    public BIIObjectStore map(int recordIndex) {
        BIIObjectStore store = getStore();
        SectionInstance sectionInstance = getSectionInstance();
        String fileId = sectionInstance.getFileId();

        List<Integer> fieldSplitting = getFieldSplitting();
        int splitSize = fieldSplitting.size(), splitSizeM1 = splitSize - 1;
        Node<?, ?> prevNode = null;

        final AnnotationType procColIdAnnType = new AnnotationType(PROCESSING_COL_ID_ANN_TAG);

        for (int i = 0; i < splitSizeM1; i++) {
            // Processing coordinates
            int inIndex = fieldSplitting.get(i), outIndex = fieldSplitting.get(i + 1);

            // Create it and save
            Processing<?, ?> processing = mapProcessing(prevNode, inIndex, outIndex, recordIndex);
            store.put(Processing.class, processing.getAcc(), processing);

            // Save the nodes, needed later for post-mapping rearrangements
            List<Node> nodes = new ArrayList<Node>(processing.getInputNodes());
            Collection<Node> outs = (Collection<Node>) processing.getOutputNodes();
            prevNode = outs.iterator().next();
            nodes.add(prevNode);
            for (Node<?, ?> node : nodes) {
                store.put(Node.class, node.getAcc(), node);
            }

            processing.addAnnotation(new Annotation(procColIdAnnType, fileId + ":" + i));
            log.trace("Returning new processing: " + processing.getAcc() + " (" + this.getClass().getSimpleName() + ")");
        }
        return store;
    }


    /**
     * Maps a processing after the identification of two nodes in the pipeline section.
     * It is assumed the pipeline section has an input node starting at inputIndexField,
     * an output node in outputIndexField, and protocol application references in between.
     *
     * @param prevNode         the node created at previous processing mapping in the same row, i.e.:
     *                         an output that will become the input of the new processing.
     * @param inputIndexField  the header where the description of the input starts from.
     * @param outputIndexField the header where the description of the output starts from.
     */
    protected Processing<?, ?> mapProcessing(
            Node<?, ?> prevNode, int inputIndexField, int outputIndexField, int recordIndex) {
        SectionInstance sectionInstance = getSectionInstance();
        List<?> nodeMappers = getNodeMappers();

        ProcessingNodeTabMapper<?> inputMapper = (ProcessingNodeTabMapper<?>) nodeMappers.get(inputIndexField);
        ProcessingNodeTabMapper<?> outputMapper = (ProcessingNodeTabMapper<?>) nodeMappers.get(outputIndexField);

        Class<?> inputType = inputMapper.getMappedClass();
        Class<?> outputType = outputMapper.getMappedClass();

        Processing<?, ?> processing;

        String fileId = StringUtils.trimToNull(sectionInstance.getFileId());
        Study study = mappingUtils.getStudyFromSection(sectionInstance);

        String processingKey = "";
        if (study == null) {
            processingKey = "bii:";
        } else {
            String studyAcc = StringUtils.trimToNull(study.getAcc());
            if (studyAcc != null) {
                processingKey += studyAcc + ":";
            }
        }
        processingKey += "proc:";
        if (fileId != null) {
            processingKey += DataLocationManager.filePath2Id(fileId) + ".";
        }
        processingKey += inputIndexField + "." + outputIndexField + "." + recordIndex;

        if (Material.class.isAssignableFrom(inputType) && Material.class.isAssignableFrom(outputType)) {
            // Needs a MaterialProcessing
            //
            processing = new MaterialProcessing(study);
            processing.setAcc(processingKey);

            MaterialNode inputNode;
            if (prevNode == null) {
                Material inputMaterial = (Material) inputMapper.map(recordIndex);
                inputNode = new MaterialNode(study);
                inputNode.setAcc(processingKey + ":in:0");
                inputNode.setMaterial(inputMaterial);
            } else {
                inputNode = (MaterialNode) prevNode;
            }

            Material outputMaterial = (Material) outputMapper.map(recordIndex);
            MaterialNode outputNode = new MaterialNode(study);
            outputNode.setAcc(processingKey + ":out:0");
            outputNode.setMaterial(outputMaterial);

            ((MaterialProcessing) processing).addInputNode(inputNode);
            ((MaterialProcessing) processing).addOutputNode(outputNode);
        } else if (Material.class.isAssignableFrom(inputType) && Data.class.isAssignableFrom(outputType)) {
            // It is data acquisition
            //
            processing = new DataAcquisition(study);
            processing.setAcc(processingKey);

            MaterialNode inputNode;

            if (prevNode == null) {
                Material inputMaterial = (Material) inputMapper.map(recordIndex);
                inputNode = new MaterialNode(study);
                inputNode.setAcc(processingKey + ":in:0");
                inputNode.setMaterial(inputMaterial);
            } else {
                inputNode = (MaterialNode) prevNode;
            }

            Data outputData = (Data) outputMapper.map(recordIndex);
            DataNode outputNode = new DataNode(study);
            outputNode.setAcc(processingKey + ":out:0");
            outputNode.setData(outputData);

            ((DataAcquisition) processing).addInputNode(inputNode);
            ((DataAcquisition) processing).addOutputNode(outputNode);
        } else if (Data.class.isAssignableFrom(inputType) && Data.class.isAssignableFrom(outputType)) {
            // It is a DataProcessing, such as normalization
            //
            processing = new DataProcessing(study);
            processing.setAcc(processingKey);

            DataNode inputNode;
            if (prevNode == null) {
                Data inputData = (Data) inputMapper.map(recordIndex);
                inputNode = new DataNode(study);
                inputNode.setAcc(processingKey + ":in:0");
                inputNode.setData(inputData);
            } else {
                inputNode = (DataNode) prevNode;
            }

            Data outputData = (Data) outputMapper.map(recordIndex);
            DataNode outputNode = new DataNode(study);
            outputNode.setAcc(processingKey + ":out:0");
            outputNode.setData(outputData);

            ((DataProcessing) processing).addInputNode(inputNode);
            ((DataProcessing) processing).addOutputNode(outputNode);
        } else {
            int inIdx = inputMapper.getFieldIndex(), outIdx = outputMapper.getFieldIndex();
            throw new TabStructureError(i18n.msg(
                    "mapping_wrong_order",
                    sectionInstance.getParent().getFileId(),
                    inputType.getSimpleName(),
                    sectionInstance.getField(inIdx).getId()
                            + "( #" + inIdx + " ), " + sectionInstance.getField(outIdx).getId()
                            + "( #" + outIdx + " )"
            ));
        }

        // OK, now let's go for the protocols between in/out
        for (int i = inputIndexField + 1; i < outputIndexField; i++) {
            GenericProtocolApplicationTabMapper protoAppMapper = (GenericProtocolApplicationTabMapper) nodeMappers.get(i);
            ProtocolApplication protoApp = protoAppMapper.map(recordIndex);
            // Protocol Applications should not have an accession in this stage, cause this prevents us from
            // detecting that two processings sharing the same inputs and outputs are actually the same.
            if (protoApp != null) {
                processing.addProtocolApplication(protoApp);
            }
        }

        // Add the row order, needed for merging equivalent protocol applications
        processing.addAnnotation(
                new Annotation(new AnnotationType(PROCESSING_ROW_ORDER_ANN_TAG),
                        Integer.toString(recordIndex))
        );

        return processing;
    }


    /**
     * Tells if this {@link #getSectionInstance()} is a section from the sample file or the assay file.
     * TODO: caching.
     */
    public boolean isSample() {
        String formatId = getSectionInstance().getSection().getFormat().getId();
        return "study_samples".equals(formatId);
    }


    /**
     * Returns the node mappers, as they are found in the TAB pipeline section, and accordingly to the
     * configuration defined by {@link #nodeMappersConfig}. That is: when a field has a name which matches a
     * key in {@link #nodeMappersConfig}, the corresponding mapper is instantiated and added to the result of
     * this method.
     * <p/>
     * <b>PLEASE NOTE</b>: the node mappers receive this mapper as parent.
     */
    @SuppressWarnings("unchecked")
    public List<? extends ProcessingEntityTabMapper<?>> getNodeMappers() {
        if (nodeMappers != null) {
            return nodeMappers;
        }

        nodeMappers = new ArrayList<ProcessingEntityTabMapper<?>>();

        SectionInstance sectionInstance = getSectionInstance();

        // I get to know the ending field only at the next iteration, not at the current one, so
        // I need the following tricky loop
        //
        List<Field> fields = sectionInstance.getFields();
        int fieldsSize = fields.size();
        Class<?> previousNodeMapperClass = null;
        int previousFieldIndex = -1;
        String previousFieldName = null;

        for (int iField = 0; iField <= fieldsSize; iField++) {
            Field field = null;
            Class<?> nodeMapperClass = null;
            int fieldIndex = -1;
            String fieldName = null;

            if (iField < fieldsSize) {
                field = fields.get(iField);
                fieldName = field.getAttr("id");
                fieldIndex = field.getIndex();
                nodeMapperClass = nodeMappersConfig.get(fieldName);
                if (nodeMapperClass == null) {
                    log.trace("No Node Mapper for Header " + fieldName + " during processing of " + sectionInstance.getSectionId());
                    continue;
                }
            } else {
                fieldIndex = fieldsSize - 1;
            }

            if (previousNodeMapperClass != null) {
                // Initializes the previous mapper, so that we know its boundaries.
                //
                ProcessingEntityTabMapper<?> nodeMapper;
                try {
                    nodeMapper = (ProcessingEntityTabMapper<?>) ConstructorUtils.invokeConstructor(previousNodeMapperClass,
                                    new Object[]{getStore(), sectionInstance, previousFieldIndex, fieldIndex},
                                    new Class<?>[]{BIIObjectStore.class, SectionInstance.class, Integer.class, Integer.class});
                }
                catch (Exception ex) {
                    throw new TabInternalErrorException(
                            "Error while creating the node mapper for " + previousFieldName + ": " + ex.getMessage(),
                            ex
                    );
                }
                ((List<ProcessingEntityTabMapper<?>>) nodeMappers).add(nodeMapper);
            }

            previousNodeMapperClass = nodeMapperClass;
            previousFieldIndex = fieldIndex;
            previousFieldName = fieldName;
        }

        // Mark the last mapper as such, so that it will know it is in charge for certain tasks, such as creating the
        // AssayResult and collecting the factor values.
        //
        int sz = nodeMappers.size();
        if (sz > 0) {
            ProcessingEntityTabMapper<?> lastMapper = nodeMappers.get(sz - 1);
            if (lastMapper instanceof DataNodeTabMapper) {
                ((DataNodeTabMapper) lastMapper).setAsLastMapper();
            }
        }

        // Now that we have the structure reflected by the headers, check the order rules defined in the TAB
        // definition file.
        //
        checkNodeOrderAndCardinality();

        return nodeMappers;
    }


    /**
     * Checks the order and cardinality of the fields about material/data mapping nodes. Uses the rules defined
     * in the TAB definition XML.
     */
    protected void checkNodeOrderAndCardinality() {
        SectionInstance sectionInstance = getSectionInstance();
        Section section = sectionInstance.getSection();

        // Cardinality
        for (String nodeFieldName : nodeMappersConfig.keySet()) {
            Field nodeField = section.getField(nodeFieldName);
            if (nodeField == null) {
                continue;
            }
            for (FieldConstraint constraint : nodeField.getConstraints()) {
                if (!(constraint instanceof FieldCardinalityConstraint)) {
                    continue;
                }
                FieldCardinalityConstraint cardCons = (FieldCardinalityConstraint) constraint;
                int nodeFieldCount = 0;
                for (ProcessingEntityTabMapper<?> mapper : nodeMappers) {
                    int fieldIndex = mapper.getFieldIndex();
                    Field field = sectionInstance.getField(fieldIndex);
                    if (nodeFieldName.equals(field.getId())) {
                        nodeFieldCount++;
                    }
                }
                cardCons.validateCardinality(nodeFieldCount, nodeField.getId(), log);
            }
        }

        // Order
        int nmappers = nodeMappers.size();
        for (int i = 0; i < nmappers; i++) {
            ProcessingEntityTabMapper<?> mapper = nodeMappers.get(i);
            int fieldIndex = mapper.getFieldIndex();
            Field field = sectionInstance.getField(fieldIndex);
            for (FieldConstraint constraint : field.getConstraints()) {

                if (constraint instanceof FollowsConstraint) {
                    FollowsConstraint pcons = (FollowsConstraint) constraint;
                    String consField = pcons.getFieldName();

                    if ("[nothing]".equals(consField)) {
                        // Means nothing should follows
                        if (i > 1) {
                            if (constraint.isMandatory()) {
                                throw new TabStructureError(i18n.msg(
                                        "nothing_follows_constraint_not_matched", "ERROR", field.getId(), "must"
                                ));
                            } else {
                                log.warn(i18n.msg(
                                        "nothing_follows_constraint_not_matched", "WARNING", field.getId(), "should"
                                ));
                            }
                        }
                        continue;
                    }


                    boolean isConsMatched = false;
                    for (int j = 0; j < i; j++) {
                        Field fieldj = sectionInstance.getField(nodeMappers.get(j).getFieldIndex());
                        if (consField.equals(fieldj.getId())) {
                            isConsMatched = true;
                            break;
                        }
                    }
                    if (!isConsMatched) {
                        if (pcons.isMandatory()) {
                            throw new TabStructureError(i18n.msg(
                                    "follows_constraint_not_matched", "ERROR", field.getId(), "must", consField
                            ));
                        } else {
                            log.warn(i18n.msg(
                                    "follows_constraint_not_matched", "WARNING", field.getId(), "should", consField));
                        }
                    }
                } // precedes

                else if (constraint instanceof PrecedesConstraint) {
                    PrecedesConstraint pcons = (PrecedesConstraint) constraint;
                    String consField = pcons.getFieldName();

                    if ("[nothing]".equals(consField)) {
                        // Means nothing should be after this node
                        if (i == nmappers - 1) {
                            if (constraint.isMandatory()) {
                                throw new TabStructureError(i18n.msg(
                                        "nothing_precedes_constraint_not_matched", "ERROR", field.getId(), "must"
                                ));
                            } else {
                                log.warn(i18n.msg(
                                        "nothing_precedes_constraint_not_matched", "WARNING", field.getId(), "should"
                                ));
                            }
                        }
                        continue;
                    }

                    boolean isConsMatched = false;
                    for (int j = i + 1; j < nmappers; j++) {
                        Field fieldj = sectionInstance.getField(nodeMappers.get(j).getFieldIndex());
                        if (consField.equals(fieldj.getId())) {
                            isConsMatched = true;
                            break;
                        }
                    }
                    if (!isConsMatched) {
                        if (pcons.isMandatory()) {
                            throw new TabStructureError(i18n.msg(
                                    "precedes_constraint_not_matched", "ERROR", field.getId(), "must", consField
                            ));
                        } else {
                            log.warn(i18n.msg(
                                    "precedes_constraint_not_matched", "WARNING", field.getId(), "should", consField
                            ));
                        }
                    }
                } // follows

            } // for constraint

            // Cardinality of fields belonging to single mappers.
            mapper.validateCardinality();

        } // for mappers
    }


    @Override
    public List<Integer> getMatchedFieldIndexes() {
        List<Integer> result = new ArrayList<Integer>();
        for (ProcessingEntityTabMapper<?> mapper : this.getNodeMappers()) {
            result.addAll(mapper.getMatchedFieldIndexes());
        }

        return result;
    }


    @Override
    public boolean validateHeaders() {
        boolean result = true;
        SectionInstance sectionInstance = getSectionInstance();
        List<Field> fields = sectionInstance.getFields();
        int ncols = fields.size();
        List<Integer> matchedCols = getMatchedFieldIndexes();

        // leave commented in case it's required later for debugging.
//        System.out.println("Matched columns");
//        for(int matchColumn : matchedCols) {
//            System.out.println("\t*** " + sectionInstance.getField(matchColumn).getId());
//        }

        for (int i = 0; i < ncols; i++) {
            if (!matchedCols.contains(i)) {
                log.warn(i18n.msg("wrong_field_or_position", sectionInstance.getField(i).getId(), i));
                result = false;
            }
        }
        return result;
    }

    public boolean checkSectionInstanceForFieldPresence(SectionInstance sectionInstance, String fieldName) {
        boolean fieldExists = sectionInstance.getFieldByHeader(fieldName) != null;
        log.info("Do we have a " + fieldName + "? " + fieldExists);
        return fieldExists;
    }

}
