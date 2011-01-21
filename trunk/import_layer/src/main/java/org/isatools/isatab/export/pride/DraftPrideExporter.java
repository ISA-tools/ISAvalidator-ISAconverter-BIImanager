/*

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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.isatools.isatab.mapping.AssayTypeEntries;
import org.isatools.isatab.mapping.ISATABMapper;
import org.isatools.tablib.exceptions.TabIOException;
import org.isatools.tablib.exceptions.TabMissingValueException;
import org.isatools.tablib.utils.BIIObjectStore;
import org.isatools.tablib.utils.logging.TabNDC;
import uk.ac.ebi.bioinvindex.model.*;
import uk.ac.ebi.bioinvindex.model.processing.*;
import uk.ac.ebi.bioinvindex.model.term.*;
import uk.ac.ebi.bioinvindex.model.xref.ReferenceSource;
import uk.ac.ebi.bioinvindex.utils.datasourceload.DataLocationManager;
import uk.ac.ebi.bioinvindex.utils.i18n;
import uk.ac.ebi.bioinvindex.utils.processing.ExperimentalPipelineVisitor;
import uk.ac.ebi.bioinvindex.utils.processing.ProcessingVisitAction;
import uk.ac.ebi.pride.model.implementation.core.ExperimentImpl;
import uk.ac.ebi.pride.model.implementation.core.ProtocolStepImpl;
import uk.ac.ebi.pride.model.implementation.mzData.CvParamImpl;
import uk.ac.ebi.pride.model.implementation.mzData.UserParamImpl;
import uk.ac.ebi.pride.model.interfaces.core.Experiment;
import uk.ac.ebi.pride.model.interfaces.core.Identification;
import uk.ac.ebi.pride.model.interfaces.core.ProtocolStep;
import uk.ac.ebi.pride.model.interfaces.mzdata.CvParam;
import uk.ac.ebi.pride.model.interfaces.mzdata.MzData;
import uk.ac.ebi.pride.model.interfaces.mzdata.Param;
import uk.ac.ebi.pride.model.interfaces.mzdata.UserParam;
import uk.ac.ebi.pride.xml.MzDataXMLUnmarshaller;
import uk.ac.ebi.pride.xml.XMLMarshaller;

import java.io.*;
import java.util.*;

/**
 * The PRIDE exporter. This is a first version, TODO: needs to be refactored, modularised etc.
 * <p/>
 * <dl><dt>Date:</dt><dd>May 14, 2008</dd></dl>
 *
 * @author brandizi
 */
public class DraftPrideExporter implements ProcessingVisitAction {
    private final BIIObjectStore store;
    private final String exportPath;
    private final String importPath;


    // Steps in the current assay
    private List<ProtocolStep> prideProtoSteps;
    // General params about the current assay
    private Set<Param> prideExperimentParams;
    // The mzData for the current assay
    private String mzDataPath;

    // The PRIDE files for the current assay
    private String identificationsPath, peptidesPath, modificationsPath;

    private final ExperimentalPipelineVisitor graphVisitor = new ExperimentalPipelineVisitor(this);

    protected static final Logger log = Logger.getLogger(DraftPrideExporter.class);

    public BIIObjectStore getStore() {
        return store;
    }

    /**
     * @param store      where the BII objects to export to pride are stored. This is usually created from {@link ISATABMapper}
     *                   and is supposed to contain the model about a single investigation.
     * @param importPath the path from which the ISATAB is read. This is needed to know where mzData files and identifications
     *                   files are to be taken from
     * @param exportPath the path where to export the results. A pride/ directory will be created there, together with one
     *                   directory per assay.
     */
    public DraftPrideExporter(BIIObjectStore store, String importPath, String exportPath) {
        this.store = store;
        this.importPath = importPath;
        this.exportPath = exportPath;
    }

    /**
     * walk across the experimental pipeline, passed to
     * {@link ExperimentalPipelineVisitor#visit(ProcessingVisitAction, uk.ac.ebi.bioinvindex.model.impl.processing.Node)}.
     */
    public boolean visit(GraphElement graphElement) {
        if (graphElement instanceof Processing<?, ?>) {
            Processing<?, ?> processing = (Processing<?, ?>) graphElement;
            for (ProtocolApplication protoApp : processing.getProtocolApplications()) {
                // TODO: filter on the protocol type

                // Processes the protocol
                //
                Set<CvParam> cvparams = new HashSet<CvParam>();
                HashSet<UserParam> uparams = new HashSet<UserParam>();
                Protocol protocol = protoApp.getProtocol();
                if (protocol != null) {
                    ProtocolType type = protocol.getType();
                    if (type != null) {
                        ReferenceSource typeSrc = type.getSource();
                        if (typeSrc == null) {
                            throw new TabMissingValueException(i18n.msg("ontoterm_without_source", type));
                        }
                        uparams.add(new UserParamImpl("BioInvestigation Index Protocol Type", 0, type.getName()));
                        cvparams.add(new CvParamImpl(type.getAcc(), type.getSource().getAcc(), type.getName(), 0, ""));
                    }
                    uparams.add(new UserParamImpl("BioInvestigation Index Protocol Name", 2 + uparams.size(), protocol.getName()));
                    uparams.add(new UserParamImpl("BioInvestigation Index Protocol Description", 2 + uparams.size(), protocol.getDescription()));
                } else {
                    log.trace("PRIDE exporting, no protocol defined for the processing " + processing.getAcc());
                }

                // Adds up the parameters
                //
                for (ParameterValue param : protoApp.getParameterValues()) {
                    Parameter ptype = param.getType();
                    Collection<Param> prideParams = exportPropertyValue(param, ptype.getOrder() + 3);

                    if (ptype.getRole() == PropertyRole.FACTOR) {
                        prideExperimentParams.addAll(prideParams);
                    }

                    for (Param prideParam : prideParams) {
                        if (prideParam instanceof CvParam) {
                            cvparams.add((CvParam) prideParam);
                        } else if (prideParam instanceof UserParam) {
                            uparams.add((UserParam) prideParam);
                        }
                    }
                }

                ProtocolStep prideProtoStep = new ProtocolStepImpl(prideProtoSteps.size(), cvparams, uparams);
                prideProtoSteps.add(prideProtoStep);
            }
        } else if (graphElement instanceof MaterialNode) {
            Material material = ((MaterialNode) graphElement).getMaterial();

            // Get the factors
            for (CharacteristicValue characteristic : material.getCharacteristicValues()) {
                Characteristic ctype = characteristic.getType();

                if (ctype.getRole() == PropertyRole.FACTOR) {
                    prideExperimentParams.addAll(exportPropertyValue(characteristic, ctype.getOrder()));
                }
            }
        } else if (graphElement instanceof DataNode) {
            Data data = ((DataNode) graphElement).getData();
            String dataTypeAcc = data.getType().getAcc();

            if ("bii:ms_spec_raw_data".equals(dataTypeAcc)) {
                mzDataPath = data.getUrl();
            }

            // Get the proteins files from the MS/SPEC run material
            if ("bii:ms_spec_derived_data".equals(dataTypeAcc) || "bii:ms_spec_normalized_data".equals(dataTypeAcc)) {
                String identificationsPath = StringUtils.trimToNull(data.getSingleAnnotationValue("proteinsFile"));
                if (identificationsPath != null) {
                    this.identificationsPath = importPath + "/" + identificationsPath;
                }

                String peptidesPath = StringUtils.trimToNull(data.getSingleAnnotationValue("peptidesFile"));
                if (peptidesPath != null) {
                    this.peptidesPath = importPath + "/" + peptidesPath;
                }

                String modificationsPath = StringUtils.trimToNull(data.getSingleAnnotationValue("ptmsFile"));
                if (modificationsPath != null) {
                    this.modificationsPath = importPath + "/" + modificationsPath;
                }
            }

        }
        // if graphElement

        return true;
    }


    /**
     * Processes a BII property and computes the corresponding set of {@link Param} which represents the property in PRIDE-XML
     */
    private static <PT extends Property<?>> List<Param> exportPropertyValue(PropertyValue<PT> value, int initialOrder) {
        Collection<OntologyTerm> valoes = value.getOntologyTerms();
        OntologyTerm valoe = null;
        if (valoes != null && !valoes.isEmpty()) {
            valoe = valoes.iterator().next();
        }

        PT type = value.getType();
        Collection<OntologyTerm> typeoes = type.getOntologyTerms();
        OntologyTerm typeoe = null;
        if (typeoes != null && !typeoes.isEmpty()) {
            typeoe = typeoes.iterator().next();
        }

        List<Param> result = new ArrayList<Param>();

        String valueStr = value.getValue();
        UnitValue valueUnit = value.getUnit();
        if (valueUnit != null) {
            valueStr += " " + valueUnit.getValue();
        }

        // Export the type
        //
        if (typeoe == null) {
            // The type is free text, let's map the value with a UD entry
            result.add(new UserParamImpl(type.getValue(), initialOrder + type.getOrder(), valueStr));
        } else {
            // Otherwise, we can create a CV entry
            //
            String typeoeAcc = StringUtils.trimToNull(typeoe.getAcc()),
                    typeoeName = StringUtils.trimToEmpty(typeoe.getName()),
                    typeValue = StringUtils.trimToEmpty(type.getValue());
            if (typeoeAcc == null) {
                log.error("The term '" + typeoeName + "' has an empty accession, we are exporting it to PRIDE, but it won't probably work");
                typeoe.setAcc("");
            }
            if (typeoeName.equals(typeValue)) {
                result.add(new CvParamImpl(
                        typeoeAcc, typeoe.getSource().getAcc(), typeValue, initialOrder + type.getOrder(),
                        valueStr
                ));
            } else {
                // we have a free text + OE, we need two params
                result.add(new UserParamImpl(typeValue, initialOrder + type.getOrder(), valueStr));
                result.add(new CvParamImpl(
                        typeoe.getAcc(), typeoe.getSource().getAcc(), typeoe.getName(), initialOrder + type.getOrder(),
                        typeValue
                ));
            }
        }

        // Is the value an OE too? => Another annotation
        //
        if (valoe != null) {

            if (StringUtils.trimToNull(valoe.getAcc()) == null) {
                log.error("The term '" + valoe + "' has an empty accession, we are exporting it to PRIDE, but it won't probably work");
                valoe.setAcc("");
            }

            result.add(new CvParamImpl(
                    valoe.getAcc(), valoe.getSource().getAcc(), valoe.getName(), initialOrder + type.getOrder(), valueStr
            ));
        }

        // Is it a factor? We need a further annotation in case
        if (type.getRole() == PropertyRole.FACTOR) {
            result.add(new UserParamImpl("Factor Type", initialOrder + type.getOrder(), type.getValue()));
        }
        return result;
    }


    /**
     * Issues the assay processing, wich will go through the pipeline graph
     */
    private void processAssay(Assay assay) {
        prideProtoSteps = new ArrayList<ProtocolStep>();
        prideExperimentParams = new HashSet<Param>();
        identificationsPath = peptidesPath = modificationsPath = null;

        graphVisitor.reset();
        Material material = assay.getMaterial();
        MaterialNode mnode = material.getMaterialNode();
        graphVisitor.visitBackward(mnode);
        graphVisitor.visitForward(mnode, true);

        if (identificationsPath == null) {
            throw new TabMissingValueException(i18n.msg("missing_identification_file", material.getName()));
        }

        if (peptidesPath == null) {
            throw new TabMissingValueException(i18n.msg("missing_peptide_file", material.getName()));
        }

        if (modificationsPath == null) {
            throw new TabMissingValueException(i18n.msg("missing_ptm_file", material.getName()));
        }
    }


    /**
     * Gets the identifications about the current assay
     */
    private Collection<Identification> export2Identifications() {
        return new IdentificationCsvCollection(
                new File(identificationsPath),
                new File(peptidesPath),
                new File(modificationsPath)
        );
    }


    /**
     * Exports the study attached to the assay
     * <p/>
     * TODO: add CVterms for investigations (or in description)
     */
    private Experiment export2PrideExperiment(Assay assay) {

        Study study = assay.getStudy();
        // Not used at the moment, PRIDE team assigns it
        String prideAccession = StringUtils.trimToNull(assay.getAcc());


        String prideTitle = study.getTitle();

        // TODO: constant class
        String backLink = StringUtils.trimToNull(System.getProperty("bioinvindex.converters.prideml.backlink", null));
        if (backLink != null) {
            backLink = backLink.replaceAll("\\$\\{study-acc\\}", study.getAcc());
            prideTitle += backLink;
        }

        // The protocol steps
        processAssay(assay);


        // The factors
        List<UserParam> exuparams = new ArrayList<UserParam>();
        List<CvParam> excvparams = new ArrayList<CvParam>();
        for (Param param : prideExperimentParams) {
            if (param instanceof UserParamImpl) {
                exuparams.add((UserParam) param);
            } else {
                excvparams.add((CvParam) param);
            }
        }

        // The mzData file
        String mzdataPath = importPath + "/" + mzDataPath;
        MzData mzdata;
        try {
            Reader reader = new BufferedReader(new FileReader(new File(mzdataPath)));
            MzDataXMLUnmarshaller unmarshaller = new MzDataXMLUnmarshaller();
            mzdata = unmarshaller.unMarshall(reader);
        }
        catch (IOException ex) {
            throw new TabIOException(i18n.msg("missing_mzdata", mzdataPath, ex.getMessage()), ex);
        }


        Experiment prideExperiment = new ExperimentImpl(
                // TODO: shall we support resubmission? In this case we must provide the accession that
                // we get from PRIDE, after first dispatching. Wherever this is put in the ISATAB.
                // prideAccession,																						// accession
                null,
                prideTitle,                                                                                                // title
                null,                                                                                                            // refs
                prideTitle,                                                                                             // Short label
                prideProtoSteps,                                                                                     // proto steps
                export2Identifications(),                                                                // identifications
                "Protocol steps as specified by the ISATAB submission",        // protocol name
                mzdata,                                                                                                        // mzData
                excvparams,                                                                                                // additional CV params
                exuparams                                                                                                 // additional UDF params
        );

        return prideExperiment;
    }


    /**
     * Does the whole export job
     */
    public void export() {
        for (final Study study : store.valuesOfType(Study.class)) {
            TabNDC ndc = TabNDC.getInstance();
            final Investigation investigation = study.getUniqueInvestigation();
            if (investigation != null) {
                ndc.pushObject(investigation);
            }
            ndc.pushObject(study);

            log.trace("PRIDEExporter, Working on study " + study.getAcc());

            for (Assay assay : study.getAssays()) {
                if (!"prideml".equals(AssayTypeEntries.getDispatchTargetIdFromLabels(assay))) {
                    continue;
                }

                ndc.pushObject(assay);

                log.trace("PRIDEExporter, Working on assay " + assay.getAcc());

                // Exports the experiment associated to the assay
                Experiment prideExperiment = export2PrideExperiment(assay);
                Collection<Experiment> prideExperiments = new ArrayList<Experiment>();
                prideExperiments.add(prideExperiment);

                XMLMarshaller marshaller = new XMLMarshaller();
                String assayXPath = exportPath + "/pride";
                File assayXDir = new File(assayXPath);
                try {
                    if (!assayXDir.exists()) {
                        FileUtils.forceMkdir(assayXDir);
                    }

                    String prideMlRelPath = DataLocationManager.accession2FileName(assay.getAcc());
                    marshaller.marshallExperiments(prideExperiments,
                            new FileWriter(new File(assayXPath + "/" + prideMlRelPath + ".xml"))
                    );
                }
                catch (IOException ex) {
                    throw new TabIOException(
                            i18n.msg("cannot_export_pride_todir", assay.getAcc(), assayXPath, ex.getMessage()), ex
                    );
                } // try-catch

                ndc.popObject(); // assay
            } // for assay

            ndc.popObject(); // study
            if (investigation != null) {
                ndc.popObject();
            } // investigation
        } // for study

    } // export()

}
