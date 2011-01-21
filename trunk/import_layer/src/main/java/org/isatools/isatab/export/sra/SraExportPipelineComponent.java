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

package org.isatools.isatab.export.sra;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.isatools.isatab.export.sra.templateutil.*;
import org.isatools.tablib.exceptions.TabInvalidValueException;
import org.isatools.tablib.exceptions.TabMissingValueException;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.model.*;
import uk.ac.ebi.bioinvindex.model.processing.Assay;
import uk.ac.ebi.bioinvindex.model.processing.ProtocolApplication;
import uk.ac.ebi.bioinvindex.model.term.OntologyTerm;
import uk.ac.ebi.bioinvindex.model.term.ProtocolComponent;
import uk.ac.ebi.bioinvindex.utils.i18n;
import uk.ac.ebi.bioinvindex.utils.processing.ProcessingUtils;

import uk.ac.ebi.embl.era.sra.xml.*;
import uk.ac.ebi.embl.era.sra.xml.ExperimentType.DESIGN;
import uk.ac.ebi.embl.era.sra.xml.ExperimentType.DESIGN.LIBRARYDESCRIPTOR;
import uk.ac.ebi.embl.era.sra.xml.ExperimentType.DESIGN.LIBRARYDESCRIPTOR.LIBRARYLAYOUT;
import uk.ac.ebi.embl.era.sra.xml.ExperimentType.DESIGN.LIBRARYDESCRIPTOR.LIBRARYLAYOUT.PAIRED;
import uk.ac.ebi.embl.era.sra.xml.ExperimentType.DESIGN.LIBRARYDESCRIPTOR.LIBRARYSELECTION;
import uk.ac.ebi.embl.era.sra.xml.ExperimentType.DESIGN.LIBRARYDESCRIPTOR.LIBRARYSOURCE;
import uk.ac.ebi.embl.era.sra.xml.ExperimentType.DESIGN.LIBRARYDESCRIPTOR.LIBRARYSTRATEGY;
import uk.ac.ebi.embl.era.sra.xml.ExperimentType.DESIGN.SAMPLEDESCRIPTOR;
import uk.ac.ebi.embl.era.sra.xml.ExperimentType.DESIGN;
import uk.ac.ebi.embl.era.sra.xml.ExperimentType.PROCESSING;
import uk.ac.ebi.embl.era.sra.xml.ExperimentType.STUDYREF;

import uk.ac.ebi.embl.era.sra.xml.RunType.DATABLOCK;
import uk.ac.ebi.embl.era.sra.xml.RunType.DATABLOCK.FILES;
import uk.ac.ebi.embl.era.sra.xml.RunType.DATABLOCK.FILES.FILE;
import uk.ac.ebi.embl.era.sra.xml.RunType.EXPERIMENTREF;

import java.awt.*;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * SRA-exporter, functions related to the ISATAB experimental pipeline. See {@link SraExportComponent} for further
 * information on how the SRA exporter classes are arranged.
 *
 * @author brandizi
 *         <b>date</b>: Jul 20, 2009
 */
abstract class SraExportPipelineComponent extends SraExportSampleComponent {

    protected static SRATemplateLoader sraTemplateLoader = new SRATemplateLoader();

    protected SraExportPipelineComponent(BIIObjectStore store, String sourcePath, String exportPath) {
        super(store, sourcePath, exportPath);
    }


    /**
     * Builds the SRA elements that are related to this ISATAB assay. It adds runs and an experiment to the respective
     * set.
     *
     * @return true if it could successfully build the exported items.
     */
    protected boolean buildExportedAssay(
            Assay assay, SubmissionType.FILES xsubFiles, RunSetType xrunSet, ExperimentSetType xexperimentSet, SampleSetType xsampleSet
    ) {
        String assayAcc = assay.getAcc();

        // Now create an experiment for the input material and link it to the run

        // get Material associated to the assay and get its identifier
        Material material = assay.getMaterial();
        String materialAcc = material.getAcc();

        //create a new SRA Experiment and assign ISA Material name as SRA Experiment Title
        ExperimentType xexp = ExperimentType.Factory.newInstance();
        xexp.setAlias(materialAcc);
        xexp.setTITLE("experiment made with the sample " + material.getName());

        xexp.setCenterName(centerName);
        xexp.setBrokerName(brokerName);

        PlatformType xplatform = buildExportedPlatform(assay);
        if (xplatform == null) {
            return false;
        }
        xexp.setPLATFORM(xplatform);

        xexp.setPROCESSING(buildExportedProcessing(assay));


        STUDYREF xstudyRef = STUDYREF.Factory.newInstance();
        xstudyRef.setRefname(assay.getStudy().getAcc());
        xexp.setSTUDYREF(xstudyRef);
        EXPERIMENTREF xexpRef = EXPERIMENTREF.Factory.newInstance();
        xexpRef.setRefname(materialAcc);

        DESIGN xdesign = DESIGN.Factory.newInstance();
        xdesign.setDESIGNDESCRIPTION("See study and sample descriptions for details");

        SAMPLEDESCRIPTOR xsampleRef = buildExportedAssaySample(assay, xsampleSet);
        if (xsampleRef == null) {
            return false;
        }

        xdesign.setSAMPLEDESCRIPTOR(xsampleRef);

        LIBRARYDESCRIPTOR xlib = buildExportedLibraryDescriptor(assay);
        if (xlib == null) {
            return false;
        }
        xdesign.setLIBRARYDESCRIPTOR(xlib);

        SpotDescriptorType xspotd = buildExportedSpotDescriptor(assay);
        if (xspotd == null) {
            return false;
        }

        xdesign.setSPOTDESCRIPTOR(xspotd);

        xexp.setDESIGN(xdesign);


        // For each file, builds one run, with one data block and one file
        // TODO: We should introduce something like "Run Name", so that multiple files associated to a single run can be
        // specified
        //
        for (AssayResult ar : ProcessingUtils.findAssayResultsFromAssay(assay)) {
            Data data = ar.getData();
            String url = StringUtils.trimToNull(data.getUrl());
            Study study = ar.getStudy();

            if (url == null) {
                String msg = MessageFormat.format(
                        "The assay file of type {0} / {1} for study {2} has a data file node without file name, ignoring",
                        assay.getMeasurement().getName(),
                        assay.getTechnologyName(),
                        assay.getStudy().getAcc()
                );
                nonRepeatedMessages.add(msg + ". Data node is " + data.getName());
                log.trace(msg);
                return false;
            }

            FILE.Filetype.Enum xfileType = null;
            String fileType = StringUtils.trimToNull(data.getSingleAnnotationValue("comment:SRA File Type"));
            if (fileType == null) {
                // Let's try to get it from the file extension
                //
                fileType = StringUtils.trimToNull(FilenameUtils.getExtension(url));
                if (fileType != null) {
                    xfileType = FILE.Filetype.Enum.forString(fileType.toLowerCase());
                }

                if (xfileType == null) {
                    String msg = MessageFormat.format(
                            "The assay file of type {0} / {1} for study {2} has a data file node without the annotation " +
                                    "'SRA file type' and I cannot compute the file type from the file name, ignoring the assay",
                            assay.getMeasurement().getName(),
                            assay.getTechnologyName(),
                            assay.getStudy().getAcc()
                    );
                    nonRepeatedMessages.add(msg);
                    log.trace(msg + ". Data node is " + data.getName());
                    return false;
                }
            }

            if (xfileType == null) {
                // fileType is certainly non null at this point, cause it was explicitly provided and so we
                // have to process it
                //
                xfileType = FILE.Filetype.Enum.forString(fileType.toLowerCase());

                if (xfileType == null) {
                    String msg = MessageFormat.format(
                            "The assay file of type {0} / {1} for study {2} has a bad 'SRA File Type' annotation: '" + fileType + "'" +
                                    ", ignoring the assy",
                            assay.getMeasurement().getName(),
                            assay.getTechnologyName(),
                            assay.getStudy().getAcc()
                    );
                    nonRepeatedMessages.add(msg);
                    log.trace(msg + ". Data node is " + data.getName());
                    return false;
                }
            }


            RunType xrun = RunType.Factory.newInstance();
            xrun.setAlias(assayAcc);
            xrun.setCenterName(centerName);
            xrun.setBrokerName(brokerName);

            DATABLOCK dataBlock = DATABLOCK.Factory.newInstance();
            FILES xfiles = FILES.Factory.newInstance();
            FILE xfile = FILE.Factory.newInstance();
            xfile.setFiletype(xfileType);
            xfile.setFilename(url);
            xfiles.addNewFILE();
            xfiles.setFILEArray(0, xfile);
            dataBlock.setFILES(xfiles);
            xrun.addNewDATABLOCK();
            xrun.setDATABLOCKArray(xrun.sizeOfDATABLOCKArray() - 1, dataBlock);

            addExportedSubmissionFile(xsubFiles, url);
            // TODO: remove, it's deprecated now xrun.setTotalDataBlocks ( BigInteger.ONE );
            xrun.setEXPERIMENTREF(xexpRef);

            xrunSet.addNewRUN();
            xrunSet.setRUNArray(xrunSet.sizeOfRUNArray() - 1, xrun);
        }

        xexperimentSet.addNewEXPERIMENT();
        xexperimentSet.setEXPERIMENTArray(xexperimentSet.sizeOfEXPERIMENTArray() - 1, xexp);
        return true;
    }


    /**
     * Builds the SRA {@link LIBRARYDESCRIPTOR}, this is taken from the ISATAB "library construction" protocol that has
     * been used for this assay.
     * <p/>
     * Some of these parameters are mandatory in SRA, and/or constrained to certain values, so the method raises an
     * exception in case they're not defined.
     */
    protected LIBRARYDESCRIPTOR buildExportedLibraryDescriptor(Assay assay) {
        ProtocolApplication papp = getProtocol(assay, "library construction");
        if (papp == null) {
            return null;
        }

        LIBRARYDESCRIPTOR xlib = LIBRARYDESCRIPTOR.Factory.newInstance();
        xlib.setLIBRARYNAME(getParameterValue(assay, papp, "Library Name", true));
        // TODO check it is one of the Enum types
        xlib.setLIBRARYSTRATEGY(LIBRARYSTRATEGY.Enum.forString(
                getParameterValue(assay, papp, "Library Strategy", true
                )));
        xlib.setLIBRARYSOURCE(LIBRARYSOURCE.Enum.forString(
                getParameterValue(assay, papp, "Library Source", true
                )));
        xlib.setLIBRARYSELECTION(LIBRARYSELECTION.Enum.forString(
                getParameterValue(assay, papp, "Library Selection", true
                )));

        String pdescription = StringUtils.trimToNull(papp.getProtocol().getDescription());
        if (pdescription != null) {
            xlib.setLIBRARYCONSTRUCTIONPROTOCOL(pdescription);
        }

        String libLayout = getParameterValue(assay, papp, "Library Layout", true);

        LIBRARYLAYOUT xlibLayout = LIBRARYLAYOUT.Factory.newInstance();
        if ("single".equalsIgnoreCase(libLayout)) {
            xlibLayout.addNewSINGLE();
            xlib.setLIBRARYLAYOUT(xlibLayout);
        } else if ("paired".equalsIgnoreCase(libLayout)) {
            // todo check with philippe about these parameters
            xlibLayout.addNewPAIRED();
            xlib.setLIBRARYLAYOUT(xlibLayout);
        } else {
            throw new TabInvalidValueException(i18n.msg(
                    "sra_invalid_param",
                    assay.getMeasurement().getName(),
                    assay.getTechnologyName(),
                    assay.getStudy().getAcc(),
                    "Library Layout/type",
                    libLayout
            ));
        }
        xlib.setLIBRARYLAYOUT(xlibLayout);

        LIBRARYDESCRIPTOR.TARGETEDLOCI xtargetedloci = LIBRARYDESCRIPTOR.TARGETEDLOCI.Factory.newInstance();

        LIBRARYDESCRIPTOR.TARGETEDLOCI.LOCUS xlocus = LIBRARYDESCRIPTOR.TARGETEDLOCI.LOCUS.Factory.newInstance();

        String locus = getParameterValue(assay, papp, "target_gene", true);

        if (locus != null) {
            if (locus.toLowerCase().contains("16s")) {
                xlocus.setLocusName(LIBRARYDESCRIPTOR.TARGETEDLOCI.LOCUS.LocusName.X_16_S_R_RNA);
            } else {
                xlocus.setLocusName(LIBRARYDESCRIPTOR.TARGETEDLOCI.LOCUS.LocusName.OTHER);
            }
        }

        LIBRARYDESCRIPTOR.TARGETEDLOCI.LOCUS[] xlocusArray = new LIBRARYDESCRIPTOR.TARGETEDLOCI.LOCUS[]{xlocus};
        xtargetedloci.setLOCUSArray(xlocusArray);

        xlib.setTARGETEDLOCI(xtargetedloci);

        String pooling = getParameterValue(assay, papp, "mid", true);
        if (pooling != null) {
            LIBRARYDESCRIPTOR.POOLINGSTRATEGY xpoolingstrategy = LIBRARYDESCRIPTOR.POOLINGSTRATEGY.Factory.newInstance();
            xlib.setPOOLINGSTRATEGY(LIBRARYDESCRIPTOR.POOLINGSTRATEGY.MULTIPLEXED_LIBRARIES);

        }


        return xlib;
    }

    private SRATemplate getSRATemplateToInject(SRASection sraSection, Assay assay, ProtocolApplication pApp, boolean isBarcoded) {
        String sequencingPlatform = getParameterValue(assay, pApp, "platform", true);
        String sequencingLibrary = getParameterValue(assay, pApp, "library layout", true);

        return SRAUtils.getTemplate(sraSection, sequencingPlatform, sequencingLibrary, isBarcoded);
    }

    private SRATemplate getSRATemplateToInject(SRASection sraSection, Assay assay, ProtocolApplication pApp) {
        return getSRATemplateToInject(sraSection, assay, pApp, false);
    }

    /**
     * Builds the SRA {@link SpotDescriptorType}, this is taken from the ISATAB "sequencing" protocol that has
     * been used for this assay.
     * <p/>
     * Some of these parameters are mandatory in SRA, and/or constrained to certain values, so the method raises an
     * exception in case they're not defined.
     */
    protected SpotDescriptorType buildExportedSpotDescriptor(Assay assay) {
        ProtocolApplication pApp = getProtocol(assay, "sequencing");
        if (pApp == null) {
            return null;
        }

        String barcode = getParameterValue(assay, pApp, "barcode", false);

        System.out.println("barcode: " + barcode);

        String adapterSpec = getParameterValue(assay, pApp, "Adapter Spec", false);
        String numOfSpotReads = getParameterValue(assay, pApp, "Number of reads per spot", false);

        boolean usesBarcode = (barcode != null);

        SRATemplate sraTemplateToInject = getSRATemplateToInject(SRASection.SPOT_DESCRIPTOR, assay, pApp, usesBarcode);

        SpotDescriptorType xspotd = SpotDescriptorType.Factory.newInstance();

        Map<SRAAttributes, String> userDefinedAttributes = new HashMap<SRAAttributes, String>();

        if (!StringUtils.isEmpty(adapterSpec)) {
            userDefinedAttributes.put(SRAAttributes.ADAPTER_SPEC, adapterSpec);
        }

        if (!StringUtils.isEmpty(numOfSpotReads)) {
            userDefinedAttributes.put(SRAAttributes.NUMBER_OF_READS_PER_SPOT, numOfSpotReads);
        }

        if (!StringUtils.isEmpty(barcode)) {
            userDefinedAttributes.put(SRAAttributes.READ_GROUP_TAG, barcode);
        }

        try {
            String sraTemplate = sraTemplateLoader.getSRAProcessingTemplate(sraTemplateToInject, userDefinedAttributes);

            System.out.println(sraTemplate);

            XmlOptions xmlOptions = new XmlOptions();
            xmlOptions.setDocumentType(SpotDescriptorType.Factory.newInstance().schemaType());

            XmlObject parsedAttr =
                    XmlObject.Factory.parse(sraTemplate, xmlOptions);

            xspotd.set(parsedAttr);

            return xspotd;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (XmlException e) {
            e.printStackTrace();
        }

        return xspotd;
    }


    /**
     * Builds the SRA {@link PROCESSING}, this is taken from the ISATAB "sequencing" protocol that has
     * been used for this assay.
     * <p/>
     * Some of these parameters are mandatory in SRA, and/or constrained to certain values, so the method raises an
     * exception in case they're not defined.
     */
    protected PROCESSING buildExportedProcessing(final Assay assay) {
        ProtocolApplication pApp = getProtocol(assay, "sequencing");

        SRATemplate sraTemplateToInject = getSRATemplateToInject(SRASection.PROCESSING, assay, pApp);

        String seqSpaceStr = getParameterValue(assay, pApp, "Sequence space", false);

        String baseCaller = getParameterValue(assay, pApp, "Base caller", false);

        String qualityScorer = getParameterValue(assay, pApp, "Quality scorer", false);

        String numberOfLevels = getParameterValue(assay, pApp, "Number of levels", false);

        String multiplier = getParameterValue(assay, pApp, "Multiplier", false);

        Map<SRAAttributes, String> userDefinedAttributes = new HashMap<SRAAttributes, String>();

        if (!StringUtils.isEmpty(seqSpaceStr)) {
            userDefinedAttributes.put(SRAAttributes.SEQUENCE_SPACE, seqSpaceStr);
        }

        if (!StringUtils.isEmpty(baseCaller)) {
            userDefinedAttributes.put(SRAAttributes.BASE_CALLER, baseCaller);
        }

        if (!StringUtils.isEmpty(qualityScorer)) {
            userDefinedAttributes.put(SRAAttributes.QUALITY_SCORER, qualityScorer);
        }

        if (!StringUtils.isEmpty(numberOfLevels)) {
            userDefinedAttributes.put(SRAAttributes.NUMBER_OF_LEVELS, numberOfLevels);
        }

        if (!StringUtils.isEmpty(multiplier)) {
            userDefinedAttributes.put(SRAAttributes.MULTIPLIER, multiplier);
        }


        // TODO: modify to pull out the technology and library using Parameter Value[platform] & ParameterValue[library layout] respectively
        // TODO: PRS-> according to new configuration for sequencing, Parameter Value[library layout] is moved to the library construction protocol
        // TODO: PRS-> replace Parameter Value[platform] with Parameter Value[sequencing instrument] and checks on values.
        // TODO: PRS-> add support for Immunoprecipitation techniques, requires detection of Protocol
        // TODO: PRS-> add support for 'Targeted Loci' in SRA experiment and find in ISA-TAB file if assay is environmental gene survey


        try {
            String sraTemplate = sraTemplateLoader.getSRAProcessingTemplate(sraTemplateToInject, userDefinedAttributes);

            XmlOptions xmlOptions = new XmlOptions();
            xmlOptions.setDocumentType(PROCESSING.Factory.newInstance().schemaType());

            PROCESSING processing = PROCESSING.Factory.newInstance();

            XmlObject processingObject = XmlObject.Factory.parse(sraTemplate, xmlOptions);

            processing.set(processingObject);
            return processing;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (XmlException e) {
            e.printStackTrace();
        }

        return PROCESSING.Factory.newInstance();

    }


    /**
     * Builds the SRA {@link PlatformType}, this is partly taken from the ISATAB "sequencing" protocol and partly from the
     * "platform" field in the ISATAB assay section (investigation file).
     * <p/>
     * Some of these parameters are mandatory in SRA, and/or constrained to certain values, so the method raises an
     * exception in case they're not defined.
     * TODO: this could be replaced by relying on the ISA Parameter Value[sequencing instrument] available from latest configuration
     */
    protected PlatformType buildExportedPlatform(final Assay assay) {
        ProtocolApplication pApp = getProtocol(assay, "sequencing");
        if (pApp == null) {
            return null;
        }
        Protocol proto = pApp.getProtocol();

        // Get the instrument information associated to that sequencing protocol
        // TODO: PRS: rely on a ISA Parameter Value[sequencing instrument] instead to obtain the information
        // TODO: PRS: check against the declared ISA assay platform


        String sequencinginst = getParameterValue(assay, pApp, "sequencing instrument", true);

        PlatformType.LS454.INSTRUMENTMODEL.Enum.forString(sequencinginst);

        if (("454 GS".equalsIgnoreCase(sequencinginst) ||
                "454 GS 20".equalsIgnoreCase(sequencinginst) ||
                "454 GS FLX".equalsIgnoreCase(sequencinginst) ||
                "454 GS FLX Titanium".equalsIgnoreCase(sequencinginst) ||
                "454 GS Junior".equalsIgnoreCase(sequencinginst) ||
                "GS20".equalsIgnoreCase(sequencinginst) ||
                "GS FLX".equalsIgnoreCase(sequencinginst)
        )) {

            // todo finish
        }


        String xinstrument = null;

        for (ProtocolComponent pcomp : proto.getComponents()) {
            for (OntologyTerm ctype : pcomp.getOntologyTerms()) {
                String pctypeStr = ctype.getName().toLowerCase();
                if (pctypeStr.contains("instrument") || pctypeStr.contains("sequencer")) {
                    xinstrument = pcomp.getValue();
                    break;
                }
            }
        }

        if (xinstrument == null) {
            String msg = MessageFormat.format(
                    "The assay file of type {0} / {1} for study {2} has no Instrument declared in the ISA Sequencing Protocol",
                    assay.getMeasurement().getName(),
                    assay.getTechnologyName(),
                    assay.getStudy().getAcc()
            );
            throw new TabMissingValueException(msg);
        }


        PlatformType xplatform = PlatformType.Factory.newInstance();
        String platform = StringUtils.upperCase(assay.getAssayPlatform());


        if (platform.toLowerCase().contains("454")) {


            //if ("LS454".equalsIgnoreCase(platform)) {

            PlatformType.LS454 ls454 = PlatformType.LS454.Factory.newInstance();
            ls454.setINSTRUMENTMODEL(PlatformType.LS454.INSTRUMENTMODEL.Enum.forString(xinstrument));
            ls454.setKEYSEQUENCE(getParameterValue(assay, pApp, "Key Sequence", false));
            ls454.setFLOWSEQUENCE(getParameterValue(assay, pApp, "Flow Sequence", false));

            String flowCountStr = getParameterValue(assay, pApp, "Flow Count", false);
            ls454.setFLOWCOUNT(new BigInteger(checkNumericParameter(flowCountStr)));
            xplatform.setLS454(ls454);

        } else if (platform.toLowerCase().contains("illumina")) {
            PlatformType.ILLUMINA illumina = PlatformType.ILLUMINA.Factory.newInstance();
            illumina.setINSTRUMENTMODEL(PlatformType.ILLUMINA.INSTRUMENTMODEL.Enum.forString(xinstrument));
            illumina.setCYCLESEQUENCE(getParameterValue(assay, pApp, "Cycle Sequence", true));
            illumina.setCYCLECOUNT(new BigInteger(checkNumericParameter(getParameterValue(assay, pApp, "Cycle Count", true))));
            xplatform.setILLUMINA(illumina);

        } else if (platform.toLowerCase().contains("helicos")) {
            //("HELICOS".equalsIgnoreCase(platform)) {
            PlatformType.HELICOS helicos = PlatformType.HELICOS.Factory.newInstance();
            helicos.setINSTRUMENTMODEL(PlatformType.HELICOS.INSTRUMENTMODEL.Enum.forString(xinstrument));
            helicos.setFLOWSEQUENCE(getParameterValue(assay, pApp, "Flow Sequence", true));
            helicos.setFLOWCOUNT(new BigInteger(checkNumericParameter(getParameterValue(assay, pApp, "Flow Count", true))));
            xplatform.setHELICOS(helicos);

        } else if (platform.toLowerCase().contains("solid")) {
            // ("ABI SOLID".equalsIgnoreCase(platform) || "ABI_SOLID".equalsIgnoreCase(platform)) {
            PlatformType.ABISOLID abisolid = PlatformType.ABISOLID.Factory.newInstance();
            abisolid.setINSTRUMENTMODEL(PlatformType.ABISOLID.INSTRUMENTMODEL.Enum.forString(xinstrument));

            {
                String colorMatrix = getParameterValue(assay, pApp, "Color Matrix", false);
                // single dibase colours are semicolon-separated
                if (colorMatrix != null) {

                    PlatformType.ABISOLID.COLORMATRIX xcolorMatrix = PlatformType.ABISOLID.COLORMATRIX.Factory.newInstance();

                    String dibases[] = colorMatrix.split("\\;");
                    if (dibases != null && dibases.length > 0) {

                        PlatformType.ABISOLID.COLORMATRIX.COLOR xcolors[] = new PlatformType.ABISOLID.COLORMATRIX.COLOR[dibases.length];
                        int i = 0;
                        for (String dibase : dibases) {
                            PlatformType.ABISOLID.COLORMATRIX.COLOR xcolor = PlatformType.ABISOLID.COLORMATRIX.COLOR.Factory.newInstance();
                            xcolor.setDibase(dibase);
                            xcolors[i++] = xcolor;
                        }
                        xcolorMatrix.setCOLORArray(xcolors);
                        abisolid.setCOLORMATRIX(xcolorMatrix);
                    }
                }
            }

            {
                String colorMatrixCode = getParameterValue(assay, pApp, "Color Matrix Code", false);
                if (colorMatrixCode != null) {
                    abisolid.setCOLORMATRIXCODE(colorMatrixCode);
                }
            }

            // TODO: remove, deprecated abisolid.setCYCLECOUNT ( new BigInteger ( getParameterValue ( assay, papp, "Cycle Count", true ) ) );

            abisolid.setSEQUENCELENGTH(new BigInteger(checkNumericParameter(getParameterValue(assay, pApp, "Cycle Count", false))));

            xplatform.setABISOLID(abisolid);
        } else {
            throw new TabInvalidValueException(MessageFormat.format(
                    "The SRA platform ''{0}'' for the assay ''{1}''/''{2}'' in the study ''{3}'' is invalid. Please supply the Platform information for the Assay in the Investigation file",
                    platform, assay.getMeasurement().getName(), assay.getTechnologyName(), assay.getStudy().getAcc()
            ));
        }

        return xplatform;
    }

    private String checkNumericParameter(String parameterValue) {

        return parameterValue == null || parameterValue.trim().equals("") ? "0" : parameterValue;

    }
}
