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
 under the License.                                    5

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
import org.isatools.tablib.exceptions.TabIOException;
import org.isatools.tablib.exceptions.TabInternalErrorException;
import org.isatools.tablib.exceptions.TabInvalidValueException;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.model.AssayResult;
import uk.ac.ebi.bioinvindex.model.Data;
import uk.ac.ebi.bioinvindex.model.Material;
import uk.ac.ebi.bioinvindex.model.Protocol;
import uk.ac.ebi.bioinvindex.model.processing.Assay;
import uk.ac.ebi.bioinvindex.model.processing.ProtocolApplication;
import uk.ac.ebi.bioinvindex.model.xref.Xref;
import uk.ac.ebi.bioinvindex.utils.i18n;
import uk.ac.ebi.bioinvindex.utils.processing.ProcessingUtils;
import uk.ac.ebi.embl.era.sra.xml.*;
import uk.ac.ebi.embl.era.sra.xml.ExperimentType.STUDYREF;
import uk.ac.ebi.embl.era.sra.xml.RunType.DATABLOCK;
import uk.ac.ebi.embl.era.sra.xml.RunType.DATABLOCK.FILES;
import uk.ac.ebi.embl.era.sra.xml.RunType.DATABLOCK.FILES.FILE;
import uk.ac.ebi.embl.era.sra.xml.RunType.DATABLOCK.FILES.FILE.ChecksumMethod;
import uk.ac.ebi.embl.era.sra.xml.RunType.EXPERIMENTREF;
import uk.ac.ebi.utils.io.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

//import uk.ac.ebi.embl.era.sra.xml.ExperimentType.PROCESSING;

//testing to see whether it pulls md5

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

    private boolean containsAnnotation(Assay assay, String annotation) {
        Collection<Xref> crossReferences = assay.getXrefs();

        for (Xref xref : crossReferences) {
            log.info("Found XREF for " + xref.getAcc());
            if (xref.getSource().getAcc().contains(annotation)) {

                return true;
            }
        }
        return false;
    }


    /**
     * Builds the SRA elements that are related to this ISATAB assay. It adds runs and an experiment to the respective
     * set.
     *
     * @return true if it could successfully build the exported items.
     */
    protected boolean buildExportedAssay(Assay assay, /*SubmissionType.FILES xsubFiles,*/ RunSetType xrunSet, ExperimentSetType xexperimentSet, SampleSetType xsampleSet) {


        String assayAcc = assay.getAcc();

        boolean doExport = true;
        if (containsAnnotation(assay, "EXPORT")) {

            log.info("HAS EXPORT COMMENT IN ASSAY");

            String export = assay.getSingleAnnotationValue("comment:Export");

            log.info("export is " + export);
            doExport = !export.equalsIgnoreCase("no");

        } else {
            log.info("NO EXPORT COMMENT FOUND");
        }

        log.info("Perform export? " + doExport);

        if (doExport) {

            // Now create an experiment for the input material and link it to the run

            // get Material associated to the assay and get its identifier
            Material material = assay.getMaterial();
            String materialAcc = material.getAcc();

            //create a new SRA Experiment and assign ISA Material name as SRA Experiment Title
            ExperimentType xexp = ExperimentType.Factory.newInstance();
            xexp.setAlias(materialAcc);
            xexp.setTITLE("Sequencing library derived from sample " + material.getName());

            xexp.setCenterName(centerName);
            xexp.setBrokerName(brokerName);

            PlatformType xplatform = buildExportedPlatform(assay);
            if (xplatform == null) {
                return false;
            }
            xexp.setPLATFORM(xplatform);

            Map<SequencingProperties, String> sequencingProperties = getSequencingInstrumentAndLayout(assay);

            //xexp.setPROCESSING(buildExportedProcessing(assay, sequencingProperties));


            STUDYREF xstudyRef = STUDYREF.Factory.newInstance();
            xstudyRef.setRefname(assay.getStudy().getAcc());
            xexp.setSTUDYREF(xstudyRef);
            EXPERIMENTREF xexpRef = EXPERIMENTREF.Factory.newInstance();
            xexpRef.setRefname(materialAcc);

              STUDYREF.Factory.newInstance();




           // DESIGN xdesign = DESIGN.Factory.newInstance();
            LibraryType xdesign =  LibraryType.Factory.newInstance();
            xdesign.setDESIGNDESCRIPTION("See study and sample descriptions for details");


            SampleDescriptorType xsampleRef = buildExportedAssaySample(assay, xsampleSet);
            if (xsampleRef == null) {
                return false;
            }

            xdesign.setSAMPLEDESCRIPTOR(xsampleRef);


            LibraryDescriptorType xlib = buildExportedLibraryDescriptor(assay);
            if (xlib == null) {
                return false;
            }
            xdesign.setLIBRARYDESCRIPTOR(xlib);

            SpotDescriptorType xspotd = buildExportedSpotDescriptor(assay, sequencingProperties);
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
                //we remove the compression extension so that getExtension methods returns the true extension
                System.out.println("Data is: " + data.getUrl());

                String fileType = data.getSingleAnnotationValue("comment:SRA File Type") == null
                        ? data.getUrl()
                        : data.getSingleAnnotationValue("comment:SRA File Type");

                fileType = fileType.replace(".gz", "");

                System.out.println("Data after replacement is: " + fileType);


                // Let's try to get it from the file extension
                fileType = StringUtils.trimToNull(FilenameUtils.getExtension(fileType));
                System.out.println("File type is : " + fileType);

                if (fileType != null) {
                    xfileType = FILE.Filetype.Enum.forString(fileType.toLowerCase());
                    if (xfileType == null) {
                        String msg = MessageFormat.format(
                                "The assay file of type {0} / {1} for study {2} has a bad 'SRA File Type' annotation: '" + fileType + "'" +
                                        ", ignoring the assay",
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

                xfile.setChecksumMethod(ChecksumMethod.MD_5);

                String md5;

                if (!fileToMD5.containsKey(url)) {

                    try {
                        md5 = IOUtils.getMD5(new File(this.sourcePath + "/" + url));
                        fileToMD5.put(url, md5);
                    } catch (NoSuchAlgorithmException e) {
                        throw new TabInternalErrorException(
                                "Problem while trying to compute the MD5 for '" + url + "': " + e.getMessage(), e
                        );
                    } catch (IOException e) {
                        throw new TabIOException(
                                "I/O problem while trying to compute the MD5 for '" + url + "': " + e.getMessage(), e
                        );

                    }
                }

                xfile.setChecksum(fileToMD5.get(url));

                System.out.println("MD5checksum: " + fileToMD5.get(url));

                xfiles.addNewFILE();
                xfiles.setFILEArray(0, xfile);
                dataBlock.setFILES(xfiles);
                xrun.addNewDATABLOCK();
                xrun.setDATABLOCK(dataBlock);

                //addExportedSubmissionFile(xsubFiles, url);
                // TODO: remove, it's deprecated now xrun.setTotalDataBlocks ( BigInteger.ONE );
                xrun.setEXPERIMENTREF(xexpRef);
                xrunSet.addNewRUN();
                xrunSet.setRUNArray(xrunSet.sizeOfRUNArray() - 1, xrun);
            }

            xexperimentSet.addNewEXPERIMENT();
            xexperimentSet.setEXPERIMENTArray(xexperimentSet.sizeOfEXPERIMENTArray() - 1, xexp);
        }

        return true;
    }


    /**
     * Builds the SRA {@link LibraryDescriptorType}, this is taken from the ISATAB "library construction" protocol that has
     * been used for this assay.
     * <p/>
     * Some of these parameters are mandatory in SRA, and/or constrained to certain values, so the method raises an
     * exception in case they're not defined.
     */
    protected LibraryDescriptorType buildExportedLibraryDescriptor(Assay assay) {

        ProtocolApplication papp = getProtocol(assay, "library construction");
        if (papp == null) {
            return null;
        }

        String measurement = assay.getMeasurement().getName();
        String technology = assay.getTechnologyName();

        System.out.println("Measurement:" + measurement);
        System.out.println("Technology:" + technology);

        LibraryDescriptorType xlib = LibraryDescriptorType.Factory.newInstance();

        //NOTE: from Schema1.3, setting the library Name is required!
        //Here library name is available only if protocol parameter 'library name' is supplied

//        if (targetTaxon != null) { xlib.setLIBRARYNAME(assay.getAcc() + "_" + targetTaxon);      }
//        else {
//            //IF no 'parameter [library name] is found', then set the library name to be that on the assay
            xlib.setLIBRARYNAME(assay.getAcc() + "");
       // }

        StringBuffer protocol = new StringBuffer();
        String pDescription = StringUtils.trimToNull(papp.getProtocol().getDescription());
        if (pDescription != null) {
            protocol.append("\n protocol_description: " + pDescription);
        }


        //HERE we handle SRA way of coding transcription profiling using sequencing. We can automatically set LIBRARY SOURCE to TRANSCRIPTOMIC
        //same for Strategy and selection however we check against the user input via the ISA file
        if (measurement.equalsIgnoreCase("transcription profiling") && technology.equalsIgnoreCase("nucleotide sequencing")) {



            xlib.setLIBRARYSOURCE(LibraryDescriptorType.LIBRARYSOURCE.TRANSCRIPTOMIC);
            xlib.setLIBRARYSTRATEGY(LibraryDescriptorType.LIBRARYSTRATEGY.RNA_SEQ);
            xlib.setLIBRARYSELECTION(LibraryDescriptorType.LIBRARYSELECTION.RT_PCR);

            ProtocolApplication pappIp = getProtocol(assay, "library construction");
            if (pappIp == null) {
                return null;
            }


        }

        if (measurement.equalsIgnoreCase("DNA methylation profiling") && technology.equalsIgnoreCase("nucleotide sequencing")) {

            xlib.setLIBRARYSOURCE(LibraryDescriptorType.LIBRARYSOURCE.GENOMIC);

            String selection = getParameterValue(assay, papp, "library selection", true);
            String strategy = getParameterValue(assay, papp, "library strategy", true);

            //String strategy = getParameterValue(assay, papp, "library strategy", true);

            //now checking that the input obtained from parsing ISA is compatible with SRA CV

            if (("MRE-Seq".equalsIgnoreCase(strategy)) ||
                    ("MeDIP-Seq".equalsIgnoreCase(strategy)) ||
                    ("MBD-Seq".equalsIgnoreCase(strategy)) ||
                    ("Bisulfite-Seq".equalsIgnoreCase(strategy)) ||
                    ("OTHER".equalsIgnoreCase(strategy))
                    ) {

                xlib.setLIBRARYSTRATEGY(LibraryDescriptorType.LIBRARYSTRATEGY.Enum.forString(strategy));
            } else {
                xlib.setLIBRARYSTRATEGY(LibraryDescriptorType.LIBRARYSTRATEGY.OTHER);
                System.out.println("ERROR:value supplied is not compatible with SRA1.2 schema" + strategy);
            }


            //String selection = getParameterValue(assay, papp, "library selection", true);

            if (("MF".equalsIgnoreCase(selection)) ||
                    ("PCR".equalsIgnoreCase(selection)) ||
                    ("HMPR".equalsIgnoreCase(selection)) ||
                    ("MSLL".equalsIgnoreCase(selection)) ||
                    ("5-methylcytidine antibody".equalsIgnoreCase(selection)) ||
                    ("MBD2 protein methyl-CpG binding domain".equals(selection)) ||
                    ("other".equalsIgnoreCase(selection))
                    ) {
                xlib.setLIBRARYSELECTION(LibraryDescriptorType.LIBRARYSELECTION.Enum.forString(selection));
            } else {
                xlib.setLIBRARYSELECTION(LibraryDescriptorType.LIBRARYSELECTION.OTHER);
                System.out.println("ERROR:value supplied is not compatible with SRA1.2 schema" + selection);
            }

        }


        //Here, we deal with chromatin remodeling use case, user input via ISA is about library strategy, library selection, library layout
        if (measurement.equalsIgnoreCase("histone modification profiling") && technology.equalsIgnoreCase("nucleotide sequencing")) {

            xlib.setLIBRARYSOURCE(LibraryDescriptorType.LIBRARYSOURCE.GENOMIC);
            xlib.setLIBRARYSTRATEGY(LibraryDescriptorType.LIBRARYSTRATEGY.CH_IP_SEQ);
            xlib.setLIBRARYSELECTION(LibraryDescriptorType.LIBRARYSELECTION.CH_IP);

            ProtocolApplication pappIp = getProtocol(assay, "library construction");
            if (pappIp == null) {
                return null;
            }

            //dealing with Chromatin immunoprecipitation requirements in ISA_TAB and dumping those in SRA Library Construction Protocol section
            String crosslink = getParameterValue(assay, pappIp, "cross linking", true);
            if (crosslink != null) {
                protocol.append("\n cross-linking: ").append(crosslink);
            }

            String fragmentation = getParameterValue(assay, pappIp, "DNA fragmentation", true);
            if (fragmentation != null) {
                protocol.append("\n DNA fragmentation: ").append(fragmentation);
            }

            String fragsize = getParameterValue(assay, pappIp, "DNA fragment size", true);
            if (fragsize != null) {
                protocol.append("\n DNA fragment size: ").append(fragsize);
            }

            String ipAntibody = getParameterValue(assay, pappIp, "immunoprecipitation antibody", true);
            if (ipAntibody != null) {

                String[] interestingbits = ipAntibody.split(";");
                if (interestingbits[0] != null) {
                    protocol.append("\n antibody name: ").append(interestingbits[0]);
                }
                if (interestingbits[1] != null) {
                    protocol.append("\n antibody manufacturer: ").append(interestingbits[1]);
                }
                if (interestingbits[2] != null) {
                    protocol.append("\n antibody reference number: ").append(interestingbits[2]);
                }
                if (interestingbits[3] != null) {
                    protocol.append("\n immunoprecipitation antibody: ").append(interestingbits[3]);
                }

            }
        }


        //HERE, we handle the MIMARKS annotation for library construction in environmental gene survey and map those to SRA objects
        //reliance on ISA parameters tagged to INSDC codes 'target taxon,target_gene,target_subfragment, mid,

        if (measurement.equalsIgnoreCase("environmental gene survey") && technology.equalsIgnoreCase("nucleotide sequencing")) {

            //deducing the values for source.strategy.selection from ISA assay

            xlib.setLIBRARYSOURCE(LibraryDescriptorType.LIBRARYSOURCE.METAGENOMIC);
            xlib.setLIBRARYSTRATEGY(LibraryDescriptorType.LIBRARYSTRATEGY.AMPLICON);
            xlib.setLIBRARYSELECTION(LibraryDescriptorType.LIBRARYSELECTION.PCR);

            //dealing with MIMARKS requirements in ISA_TAB and dumping those in SRA Library Construction Protocol section
            String pBibRef = getParameterValue(assay, papp, "nucl_acid_amp", false);
            if (pBibRef != null) {
                protocol.append("\n nucl_acid_amp: ").append(pBibRef);
            }

            String pUrl = getParameterValue(assay, papp, "url", false);
            if (pUrl != null) {
                protocol.append("\n url: ").append(pUrl);

            }

            String targetTaxon = "";
            targetTaxon = getParameterValue(assay, papp, "target_taxon", false);
            if (targetTaxon != null) {
                protocol.append("\n target_taxon: ").append(targetTaxon);
                xlib.setLIBRARYNAME(assay.getAcc() + "_" + targetTaxon);
            }

            String targetGene = getParameterValue(assay, papp, "target_gene", true);
            if (targetGene != null) {
                protocol.append("\n target_gene: ").append(targetGene);
            }

            String targetSubfrag = getParameterValue(assay, papp, "target_subfragment", true);
            if (targetSubfrag != null) {
                protocol.append("\n target_subfragment: ").append(targetSubfrag);
            }

            String pcrPrimers = getParameterValue(assay, papp, "pcr_primers", true);
            if (pcrPrimers != null) {
                protocol.append("\n pcr_primers: ").append(pcrPrimers.replaceAll("=", ":"));
            }




            String pcrConditions = getParameterValue(assay, papp, "pcr_cond", true);
            if (pcrConditions != null) {
                protocol.append("\n pcr_cond: ").append(pcrConditions.replaceAll("=", ":"));
            }

            //Here we rely on SRA targeted loci and specify relevent information
            //NOTE: as SRA schema only support 16S R RNA as a possible value, all actual targets supplied by users in ISA
            //are dumped in the protocol and preceded by INSDC code as defined under MIMARKS (MIENS).

            LibraryDescriptorType.TARGETEDLOCI xtargetedloci = LibraryDescriptorType.TARGETEDLOCI.Factory.newInstance();

            LibraryDescriptorType.TARGETEDLOCI.LOCUS xlocus = LibraryDescriptorType.TARGETEDLOCI.LOCUS.Factory.newInstance();

            String locus = getParameterValue(assay, papp, "target_gene", true);

            //This is a new requirement starting from SRA.1.3 schema that a database Xref be provided, we are now checking this in.
            String pcrPrimersXref = getParameterValue(assay, papp, "pcr_primers_identifier", false);

            XRefType locusXref = XRefType.Factory.newInstance();

            if (pcrPrimersXref != null) {

                locusXref.setID("pcrPrimersXref");
                locusXref.setDB("PubMed");
            }
            
            if (locus != null) {
                if (locus.toLowerCase().contains("16s")) {
                    xlocus.setLocusName(LibraryDescriptorType.TARGETEDLOCI.LOCUS.LocusName.X_16_S_R_RNA);
                   // xlocus.setPROBESET(locusXref);
                }
                else if (locus.toLowerCase().contains("18s")) {
                    xlocus.setLocusName(LibraryDescriptorType.TARGETEDLOCI.LOCUS.LocusName.X_18_S_R_RNA);
                   // xlocus.setPROBESET(locusXref);
                }

                else if (locus.toLowerCase().contains("cox")) {
                    xlocus.setLocusName(LibraryDescriptorType.TARGETEDLOCI.LOCUS.LocusName.COX_1);
                  //  xlocus.setPROBESET(locusXref);
                }
                else if (locus.toLowerCase().contains("its")) {
                    xlocus.setLocusName(LibraryDescriptorType.TARGETEDLOCI.LOCUS.LocusName.ITS_1_5_8_S_ITS_2);
                 //  xlocus.setPROBESET(locusXref);
                }
                else if (locus.toLowerCase().contains("matk")) {
                    xlocus.setLocusName(LibraryDescriptorType.TARGETEDLOCI.LOCUS.LocusName.MAT_K);
                  //  xlocus.setPROBESET(locusXref);
                }
                else if (locus.toLowerCase().contains("rbcl")) {
                    xlocus.setLocusName(LibraryDescriptorType.TARGETEDLOCI.LOCUS.LocusName.RBCL);
                   // xlocus.setPROBESET(locusXref);
                }
                else {
                    xlocus.setLocusName(LibraryDescriptorType.TARGETEDLOCI.LOCUS.LocusName.OTHER);
                   // xlocus.setDescription(locus);
                }
            }

            LibraryDescriptorType.TARGETEDLOCI.LOCUS[] xlocusArray = new LibraryDescriptorType.TARGETEDLOCI.LOCUS[]{xlocus};
            xtargetedloci.setLOCUSArray(xlocusArray);

            xlib.setTARGETEDLOCI(xtargetedloci);

        }



        //here we support reporting of multiplex identifiers (mid aka barcodes)
        String[] barcodes = getBarcodesForAssays(assay);

        if (barcodes.length > 0 && !StringUtils.isEmpty(barcodes[0])) {
            for (int barcodeIndex = 0; barcodeIndex < barcodes.length; barcodeIndex++) {
                System.out.println("barcode found:"+ barcodes[barcodeIndex]);
                protocol.append("\n mid: ").append(barcodes[barcodeIndex]);
            }
        }

        xlib.setLIBRARYCONSTRUCTIONPROTOCOL(protocol.toString());


        // Library Layout: This is a core requirements to ensure that ISA provides enough information to support SRA export
        // The Library layout parameter is common to all ISA assay configurations which are relying on sequencing
        String libLayout = getParameterValue(assay, papp, "library layout", true);

        LibraryDescriptorType.LIBRARYLAYOUT xlibLayout = LibraryDescriptorType.LIBRARYLAYOUT.Factory.newInstance();
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



        String pooling = getParameterValue(assay, papp, "mid", false);
        if (pooling != null) {
            LibraryDescriptorType.POOLINGSTRATEGY xpoolingstrategy = LibraryDescriptorType.POOLINGSTRATEGY.Factory.newInstance();
            //xlib.setPOOLINGSTRATEGY(LibraryDescriptorType.POOLINGSTRATEGY.MULTIPLEXED_LIBRARIES);
            xlib.setPOOLINGSTRATEGY(xpoolingstrategy.getStringValue());

        }
              return xlib;
    }


    private Map<SequencingProperties, String> getSequencingInstrumentAndLayout(Assay assay) {

        ProtocolApplication sequencingPApp = getProtocol(assay, "nucleic acid sequencing");

        String sequencingPlatform = getParameterValue(assay, sequencingPApp, SequencingProperties.SEQUENCING_PLATFORM.toString(), true);

        ProtocolApplication libConstructionPApp = getProtocol(assay, "library construction");

        String sequencingLibrary = getParameterValue(assay, libConstructionPApp, SequencingProperties.LIBRARY_LAYOUT.toString(), true);

        Map<SequencingProperties, String> properties = new HashMap<SequencingProperties, String>();

        properties.put(SequencingProperties.LIBRARY_LAYOUT, sequencingLibrary);
        properties.put(SequencingProperties.SEQUENCING_PLATFORM, sequencingPlatform);

        return properties;
    }


    private SRATemplate getSRATemplateToInject(SRASection sraSection, Map<SequencingProperties, String> sequencingProperties, boolean isBarcoded) {

        return SRAUtils.getTemplate(sraSection, sequencingProperties.get(SequencingProperties.SEQUENCING_PLATFORM),
                sequencingProperties.get(SequencingProperties.LIBRARY_LAYOUT), isBarcoded);
    }

    private SRATemplate getSRATemplateToInject(SRASection sraSection, Map<SequencingProperties, String> sequencingProperties) {
        return getSRATemplateToInject(sraSection, sequencingProperties, false);
    }

    /**
     * Builds the SRA {@link SpotDescriptorType}, this is taken from the ISATAB "sequencing" protocol that has
     * been used for this assay.
     * <p/>
     * Some of these parameters are mandatory in SRA, and/or constrained to certain values, so the method raises an
     * exception in case they're not defined.
     */
    protected SpotDescriptorType buildExportedSpotDescriptor(Assay assay, Map<SequencingProperties, String> sequencingProperties) {

        String[] barcodes = getBarcodesForAssays(assay);

        ProtocolApplication pApp = getProtocol(assay, "nucleic acid sequencing");
        if (pApp == null) {
            return null;
        }

        String adapterSpec = getParameterValue(assay, pApp, "Adapter Spec", false);
        String numOfSpotReads = getParameterValue(assay, pApp, "Number of reads per spot", false);

        boolean usesBarcode = (barcodes.length > 0);

        SRATemplate sraTemplateToInject = getSRATemplateToInject(SRASection.SPOT_DESCRIPTOR, sequencingProperties, usesBarcode);

        SpotDescriptorType xspotd = SpotDescriptorType.Factory.newInstance();

        Map<SRAAttributes, String> userDefinedAttributes = new HashMap<SRAAttributes, String>();

        if (!StringUtils.isEmpty(adapterSpec)) {
            userDefinedAttributes.put(SRAAttributes.ADAPTER_SPEC, adapterSpec);
        }

        if (!StringUtils.isEmpty(numOfSpotReads)) {
            userDefinedAttributes.put(SRAAttributes.NUMBER_OF_READS_PER_SPOT, numOfSpotReads);
        }

        if (barcodes.length > 0 && !StringUtils.isEmpty(barcodes[0])) {
            System.out.println("BARCODE IN SPOT: " + barcodes[0] );
            userDefinedAttributes.put(SRAAttributes.READ_GROUP_TAG, barcodes[0]);
        }

        try {
            String sraTemplate = sraTemplateLoader.getSRAProcessingTemplate(sraTemplateToInject, userDefinedAttributes);

            XmlOptions xmlOptions = new XmlOptions();
            xmlOptions.setDocumentType(SpotDescriptorType.Factory.newInstance().schemaType());

            XmlObject parsedAttr = XmlObject.Factory.parse(sraTemplate, xmlOptions);

            xspotd.set(parsedAttr);

            // now output rest of the barcodes (if there is more than one :) )

            if (barcodes.length > 1) {
                // output the barcode

                SpotDescriptorType.SPOTDECODESPEC.READSPEC readSpec = getReadSpecWithBaseCalls(xspotd.getSPOTDECODESPEC());

                // create new array of base calls and place the already added base call item into the array

                SpotDescriptorType.SPOTDECODESPEC.READSPEC.EXPECTEDBASECALLTABLE.BASECALL[] baseCallArray = new SpotDescriptorType.SPOTDECODESPEC.READSPEC.EXPECTEDBASECALLTABLE.BASECALL[barcodes.length];

                SpotDescriptorType.SPOTDECODESPEC.READSPEC.EXPECTEDBASECALLTABLE table = readSpec.getEXPECTEDBASECALLTABLE();


                for (int barcodeIndex = 0; barcodeIndex < barcodes.length; barcodeIndex++) {

                    SpotDescriptorType.SPOTDECODESPEC.READSPEC.EXPECTEDBASECALLTABLE.BASECALL baseCall = SpotDescriptorType.SPOTDECODESPEC.READSPEC.EXPECTEDBASECALLTABLE.BASECALL.Factory.newInstance();
                    baseCall.setReadGroupTag(barcodes[barcodeIndex]);
                    baseCall.setStringValue(barcodes[barcodeIndex]);

                    baseCallArray[barcodeIndex] = baseCall;
                }

                System.out.println("BASE CALL TABLE: " + readSpec.getEXPECTEDBASECALLTABLE());
                System.out.println("MY BASE CALL ARRAY: \n" + baseCallArray[0].toString());
                readSpec.getEXPECTEDBASECALLTABLE().setBASECALLArray(baseCallArray);
            }


            return xspotd;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (XmlException e) {
            e.printStackTrace();
        }

        return xspotd;
    }


    private SpotDescriptorType.SPOTDECODESPEC.READSPEC getReadSpecWithBaseCalls(SpotDescriptorType.SPOTDECODESPEC spotDecodeSpec) {
        if (spotDecodeSpec.getREADSPECArray().length > 0) {
            for (SpotDescriptorType.SPOTDECODESPEC.READSPEC readSpec : spotDecodeSpec.getREADSPECArray()) {
                if (readSpec.getEXPECTEDBASECALLTABLE() != null) {
                    return readSpec;
                }
            }
        }

        return null;
    }

    private String[] getBarcodesForAssays(Assay assay) {

        ProtocolApplication libConstructionPApp = getProtocol(assay, "library construction");
        if (libConstructionPApp == null) {
            return null;
        }

        String[] barcodes = getParameterValues(assay, libConstructionPApp, "mid", false);

        return (barcodes == null) ? new String[0] : barcodes;
    }


    /**
     * Builds the SRA {@link PROCESSING}, this is taken from the ISATAB "sequencing" protocol that has
     * been used for this assay.
     * <p/>
     * Some of these parameters are mandatory in SRA, and/or constrained to certain values, so the method raises an
     * exception in case they're not defined.
     */
//    protected PROCESSING buildExportedProcessing(final Assay assay, Map<SequencingProperties, String> sequencingProperties) {
//        ProtocolApplication pApp = getProtocol(assay, "nucleic acid sequencing");
//
//        SRATemplate sraTemplateToInject = getSRATemplateToInject(SRASection.PROCESSING, sequencingProperties);
//
//        String seqSpaceStr = "";
//
//        seqSpaceStr = getParameterValue(assay, pApp, "Sequence space", false);
//
//
//        String baseCaller = "";
//        baseCaller = getParameterValue(assay, pApp, "Base caller", false);
//
//        String qualityScorer = "";
//
//        qualityScorer = getParameterValue(assay, pApp, "Quality scorer", false);
//
//        String numberOfLevels = "";
//        numberOfLevels = getParameterValue(assay, pApp, "Number of levels", false);
//
//        String multiplier = "";
//        multiplier = getParameterValue(assay, pApp, "Multiplier", false);
//
//        Map<SRAAttributes, String> userDefinedAttributes = new HashMap<SRAAttributes, String>();
//
//        if (!StringUtils.isEmpty(seqSpaceStr)) {
//            userDefinedAttributes.put(SRAAttributes.SEQUENCE_SPACE, seqSpaceStr);
//        }
//
//        if (!StringUtils.isEmpty(baseCaller)) {
//            userDefinedAttributes.put(SRAAttributes.BASE_CALLER, baseCaller);
//        }
//
//        if (!StringUtils.isEmpty(qualityScorer)) {
//            userDefinedAttributes.put(SRAAttributes.QUALITY_SCORER, qualityScorer);
//        }
//
//        if (!StringUtils.isEmpty(numberOfLevels)) {
//            userDefinedAttributes.put(SRAAttributes.NUMBER_OF_LEVELS, numberOfLevels);
//        }
//
//        if (!StringUtils.isEmpty(multiplier)) {
//            userDefinedAttributes.put(SRAAttributes.MULTIPLIER, multiplier);
//        }
//
//
//        // TODO: modify to pull out the technology and library using Parameter Value[platform] & ParameterValue[library layout] respectively
//        // TODO: PRS-> according to new configuration for sequencing, Parameter Value[library layout] is moved to the library construction protocol
//        // TODO: PRS-> replace Parameter Value[platform] with Parameter Value[sequencing instrument] and checks on values.
//        // TODO: PRS-> add support for Immunoprecipitation techniques, requires detection of Protocol
//        // TODO: PRS-> add support for 'Targeted Loci' in SRA experiment and find in ISA-TAB file if assay is environmental gene survey
//
//
//        try {
//            String sraTemplate = sraTemplateLoader.getSRAProcessingTemplate(sraTemplateToInject, userDefinedAttributes);
//
//            XmlOptions xmlOptions = new XmlOptions();
//            xmlOptions.setDocumentType(PROCESSING.Factory.newInstance().schemaType());
//
//            PROCESSING processing = PROCESSING.Factory.newInstance();
//
//            XmlObject processingObject = XmlObject.Factory.parse(sraTemplate, xmlOptions);
//
//            processing.set(processingObject);
//            return processing;
//
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (XmlException e) {
//            e.printStackTrace();
//        }
//
//        return PROCESSING.Factory.newInstance();
//
//    }


    /**
     * Builds the SRA {@link PlatformType}, this is partly taken from the ISATAB "sequencing" protocol and partly from the
     * "platform" field in the ISATAB assay section (investigation file).
     * <p/>
     * Some of these parameters are mandatory in SRA, and/or constrained to certain values, so the method raises an
     * exception in case they're not defined.
     * TODO: this could be replaced by relying on the ISA Parameter Value[sequencing instrument] available from latest configuration
     */
    protected PlatformType buildExportedPlatform(final Assay assay) {
        ProtocolApplication pApp = getProtocol(assay, "nucleic acid sequencing");
        if (pApp == null) {
            return null;
        }
        Protocol proto = pApp.getProtocol();

        // Get the instrument information associated to that sequencing protocol
        // TODO: PRS: rely on a ISA Parameter Value[sequencing instrument] instead to obtain the information
        // TODO: PRS: check against the declared ISA assay platform


        String sequencinginst = getParameterValue(assay, pApp, "sequencing instrument", true);

        PlatformType xplatform = PlatformType.Factory.newInstance();


        if (sequencinginst.contains("454")) {

            PlatformType.LS454 ls454 = PlatformType.LS454.Factory.newInstance();

            //if we can detect which instrument it was that is consistent with SRA schema
            if (sequencinginst.equalsIgnoreCase("454 GS") ||
                    sequencinginst.equalsIgnoreCase("454 GS FLX") ||
                    sequencinginst.equalsIgnoreCase("454 GS 20") ||
                    sequencinginst.equalsIgnoreCase("454 GS FLX Titanium") ||
                    sequencinginst.equalsIgnoreCase("454 GS Junior")) {

                ls454.setINSTRUMENTMODEL(PlatformType.LS454.INSTRUMENTMODEL.Enum.forString(sequencinginst));
            }
            //otherwise, we fall back on the 'unspecified' value to avoid falling over
            else {
                ls454.setINSTRUMENTMODEL(PlatformType.LS454.INSTRUMENTMODEL.Enum.forString("unspecified"));
            }

            //ls454.setFLOWSEQUENCE("TACG");
            //ls454.setFLOWCOUNT(BigInteger.valueOf(800));
            xplatform.setLS454(ls454);
        } else if (sequencinginst.toLowerCase().contains("illumina")) {

            PlatformType.ILLUMINA illumina = PlatformType.ILLUMINA.Factory.newInstance();

            //if we can detect which instrument it was that is consistent with SRA schema
            if (sequencinginst.equalsIgnoreCase("Illumina Genome Analyzer") ||
                    sequencinginst.equalsIgnoreCase("Illumina Genome Analyzer II") ||
                    sequencinginst.equalsIgnoreCase("Illumina Genome Analyzer IIx") ||
                    sequencinginst.equalsIgnoreCase("Illumina HiScanSQ") ||
                    sequencinginst.equalsIgnoreCase("Illumina HiSeq 2500") ||
                    sequencinginst.equalsIgnoreCase("Illumina HiSeq 2000") ||
                    sequencinginst.equalsIgnoreCase("Illumina HiSeq 1000") ||
                    sequencinginst.equalsIgnoreCase("Illumina MiSeq")) {

                illumina.setINSTRUMENTMODEL(PlatformType.ILLUMINA.INSTRUMENTMODEL.Enum.forString(sequencinginst));

            }
            //otherwise, we fall back on the 'unspecified' value to avoid falling over
            else {
                illumina.setINSTRUMENTMODEL(PlatformType.ILLUMINA.INSTRUMENTMODEL.Enum.forString("unspecified"));
            }

            // illumina.setCYCLESEQUENCE(getParameterValue(assay, pApp, "Cycle Sequence", true));
            // illumina.setCYCLECOUNT(new BigInteger(checkNumericParameter(getParameterValue(assay, pApp, "Cycle Count", true))));
            xplatform.setILLUMINA(illumina);

        } else if (sequencinginst.toLowerCase().contains("helicos")) {

            PlatformType.HELICOS helicos = PlatformType.HELICOS.Factory.newInstance();

            if (sequencinginst.equalsIgnoreCase("Helicos HeliScope")) {

                helicos.setINSTRUMENTMODEL(PlatformType.HELICOS.INSTRUMENTMODEL.Enum.forString(sequencinginst));
            } else {
                helicos.setINSTRUMENTMODEL(PlatformType.HELICOS.INSTRUMENTMODEL.Enum.forString("unspecified"));
            }

            //helicos.setFLOWSEQUENCE(getParameterValue(assay, pApp, "Flow Sequence", true));
            //helicos.setFLOWCOUNT(new BigInteger(checkNumericParameter(getParameterValue(assay, pApp, "Flow Count", true))));
            xplatform.setHELICOS(helicos);


        } else if (sequencinginst.toLowerCase().contains("ion torrent")) {

            PlatformType.IONTORRENT iontorrent = PlatformType.IONTORRENT.Factory.newInstance();

            if (sequencinginst.equalsIgnoreCase("Ion Torrent PGM") || sequencinginst.equalsIgnoreCase("Ion Torrent Proton") ) {

                iontorrent.setINSTRUMENTMODEL(PlatformType.IONTORRENT.INSTRUMENTMODEL.Enum.forString(sequencinginst));
            } else {
                iontorrent.setINSTRUMENTMODEL(PlatformType.IONTORRENT.INSTRUMENTMODEL.Enum.forString("unspecified"));
            }

            xplatform.setIONTORRENT(iontorrent);

        }

        else if (sequencinginst.toLowerCase().contains("solid")) {

            PlatformType.ABISOLID abisolid = PlatformType.ABISOLID.Factory.newInstance();

            if (    sequencinginst.equalsIgnoreCase("AB SOLiD System") ||
                    sequencinginst.equalsIgnoreCase("AB SOLiD System 2.0") ||
                    sequencinginst.equalsIgnoreCase("AB SOLiD System 3.0") ||
                    sequencinginst.equalsIgnoreCase("AB SOLiD 3 System Plus") ||
                    sequencinginst.equalsIgnoreCase("AB SOLiD 4 System") ||
                    sequencinginst.equalsIgnoreCase("AB SOLiD 4hq System") ||
                    sequencinginst.equalsIgnoreCase("AB SOLiD PI System") ||
                    sequencinginst.equalsIgnoreCase("AB SOLiD 5500") ||
                    sequencinginst.equalsIgnoreCase("AB SOLiD 5500xl") ||
                    sequencinginst.equalsIgnoreCase("AB 5500 Genetic Analyzer") ||
                    sequencinginst.equalsIgnoreCase("AB 5500xl Genetic Analyzer")
                    ) {

                abisolid.setINSTRUMENTMODEL(PlatformType.ABISOLID.INSTRUMENTMODEL.Enum.forString(sequencinginst));
            } else {
                abisolid.setINSTRUMENTMODEL(PlatformType.ABISOLID.INSTRUMENTMODEL.Enum.forString("unspecified"));
            }

            //{
                //String colorMatrix = getParameterValue(assay, pApp, "Color Matrix", false);
                // single dibase colours are semicolon-separated
                //if (colorMatrix != null) {
                    //PlatformType.ABISOLID.COLORMATRIX xcolorMatrix = PlatformType.ABISOLID.COLORMATRIX.Factory.newInstance();
                    //String dibases[] = colorMatrix.split("\\;");
                    //if (dibases != null && dibases.length > 0) {
                        //PlatformType.ABISOLID.COLORMATRIX.COLOR xcolors[] = new PlatformType.ABISOLID.COLORMATRIX.COLOR[dibases.length];
                        //int i = 0;
                        //for (String dibase : dibases) {
                            //PlatformType.ABISOLID.COLORMATRIX.COLOR xcolor = PlatformType.ABISOLID.COLORMATRIX.COLOR.Factory.newInstance();
                            //xcolor.setDibase(dibase);
                            //xcolors[i++] = xcolor;
                        //}
                        //xcolorMatrix.setCOLORArray(xcolors);
                        //abisolid.setCOLORMATRIX(xcolorMatrix);
                    //}
                //}
            //}

            //{
            //    String colorMatrixCode = getParameterValue(assay, pApp, "Color Matrix Code", false);
            //    if (colorMatrixCode != null) {
            //        abisolid.setCOLORMATRIXCODE(colorMatrixCode);
            //    }
            //}

            // TODO: remove, deprecated abisolid.setCYCLECOUNT ( new BigInteger ( getParameterValue ( assay, papp, "Cycle Count", true ) ) );

            //abisolid.setSEQUENCELENGTH(new BigInteger(checkNumericParameter(getParameterValue(assay, pApp, "Cycle Count", false))));

            xplatform.setABISOLID(abisolid);
        }


        //PlatformType.LS454.INSTRUMENTMODEL.Enum.forString(sequencinginst);

//        if (   ("454 GS".equalsIgnoreCase(sequencinginst) ||
//                "454 GS 20".equalsIgnoreCase(sequencinginst) ||
//                "454 GS FLX".equalsIgnoreCase(sequencinginst) ||
//                "454 GS FLX Titanium".equalsIgnoreCase(sequencinginst) ||
//                "454 GS Junior".equalsIgnoreCase(sequencinginst))) {
//
//            PlatformType.LS454 ls454 = PlatformType.LS454.Factory.newInstance();
//            ls454.setINSTRUMENTMODEL(PlatformType.LS454.INSTRUMENTMODEL.Enum.forString(sequencinginst));
//            // todo finish
//        }


//        String xinstrument = null;
//
//        for (ProtocolComponent pcomp : proto.getComponents()) {
//            for (OntologyTerm ctype : pcomp.getOntologyTerms()) {
//                String pctypeStr = ctype.getName().toLowerCase();
//                if (pctypeStr.contains("instrument") || pctypeStr.contains("sequencer")) {
//                    xinstrument = pcomp.getValue();
//                    break;
//                }
//            }
//        }
//
//        if (xinstrument == null) {
//            String msg = MessageFormat.format(
//                    "The assay file of type {0} / {1} for study {2} has no Instrument declared in the ISA Sequencing Protocol",
//                    assay.getMeasurement().getName(),
//                    assay.getTechnologyName(),
//                    assay.getStudy().getAcc()
//            );
//            throw new TabMissingValueException(msg);
//        }


        //if (platform!=null) {


//        if (platform.toLowerCase().contains("454")) {
//
//
//            //if ("LS454".equalsIgnoreCase(platform)) {
//
//            PlatformType.LS454 ls454 = PlatformType.LS454.Factory.newInstance();
//            ls454.setINSTRUMENTMODEL(PlatformType.LS454.INSTRUMENTMODEL.Enum.forString(xinstrument));
//
//            //String keyseqStr = "TACG";
//
//
//            //ls454.setKEYSEQUENCE(keyseqStr);
//
//            //String flowSeqstr = "TACG";
//            ls454.setFLOWSEQUENCE("TACG");
//            //int flowCount = 800;
//            ls454.setFLOWCOUNT(BigInteger.valueOf(800));
//            xplatform.setLS454(ls454);
//
//        } else if (platform.toLowerCase().contains("illumina")) {
//            PlatformType.ILLUMINA illumina = PlatformType.ILLUMINA.Factory.newInstance();
//            illumina.setINSTRUMENTMODEL(PlatformType.ILLUMINA.INSTRUMENTMODEL.Enum.forString(xinstrument));
//            illumina.setCYCLESEQUENCE(getParameterValue(assay, pApp, "Cycle Sequence", true));
//            illumina.setCYCLECOUNT(new BigInteger(checkNumericParameter(getParameterValue(assay, pApp, "Cycle Count", true))));
//            xplatform.setILLUMINA(illumina);
//
//        } else if (platform.toLowerCase().contains("helicos")) {
//            //("HELICOS".equalsIgnoreCase(platform)) {
//            PlatformType.HELICOS helicos = PlatformType.HELICOS.Factory.newInstance();
//            helicos.setINSTRUMENTMODEL(PlatformType.HELICOS.INSTRUMENTMODEL.Enum.forString(xinstrument));
//            helicos.setFLOWSEQUENCE(getParameterValue(assay, pApp, "Flow Sequence", true));
//            helicos.setFLOWCOUNT(new BigInteger(checkNumericParameter(getParameterValue(assay, pApp, "Flow Count", true))));
//            xplatform.setHELICOS(helicos);
//
//        } else if (platform.toLowerCase().contains("solid")) {
//            // ("ABI SOLID".equalsIgnoreCase(platform) || "ABI_SOLID".equalsIgnoreCase(platform)) {
//            PlatformType.ABISOLID abisolid = PlatformType.ABISOLID.Factory.newInstance();
//            abisolid.setINSTRUMENTMODEL(PlatformType.ABISOLID.INSTRUMENTMODEL.Enum.forString(xinstrument));
//
//            {
//                String colorMatrix = getParameterValue(assay, pApp, "Color Matrix", false);
//                // single dibase colours are semicolon-separated
//                if (colorMatrix != null) {
//
//                    PlatformType.ABISOLID.COLORMATRIX xcolorMatrix = PlatformType.ABISOLID.COLORMATRIX.Factory.newInstance();
//
//                    String dibases[] = colorMatrix.split("\\;");
//                    if (dibases != null && dibases.length > 0) {
//
//                        PlatformType.ABISOLID.COLORMATRIX.COLOR xcolors[] = new PlatformType.ABISOLID.COLORMATRIX.COLOR[dibases.length];
//                        int i = 0;
//                        for (String dibase : dibases) {
//                            PlatformType.ABISOLID.COLORMATRIX.COLOR xcolor = PlatformType.ABISOLID.COLORMATRIX.COLOR.Factory.newInstance();
//                            xcolor.setDibase(dibase);
//                            xcolors[i++] = xcolor;
//                        }
//                        xcolorMatrix.setCOLORArray(xcolors);
//                        abisolid.setCOLORMATRIX(xcolorMatrix);
//                    }
//                }
//            }
//
//            {
//                String colorMatrixCode = getParameterValue(assay, pApp, "Color Matrix Code", false);
//                if (colorMatrixCode != null) {
//                    abisolid.setCOLORMATRIXCODE(colorMatrixCode);
//                }
//            }
//
//            // TODO: remove, deprecated abisolid.setCYCLECOUNT ( new BigInteger ( getParameterValue ( assay, papp, "Cycle Count", true ) ) );
//
//            abisolid.setSEQUENCELENGTH(new BigInteger(checkNumericParameter(getParameterValue(assay, pApp, "Cycle Count", false))));
//
//            xplatform.setABISOLID(abisolid);
        else {
            throw new TabInvalidValueException(MessageFormat.format(
                    "The SRA platform ''{0}'' for the assay ''{1}''/''{2}'' in the study ''{3}'' is invalid. Please supply the Platform information for the Assay in the Investigation file",
                    sequencinginst, assay.getMeasurement().getName(), assay.getTechnologyName(), assay.getStudy().getAcc()
            ));
        }

        return xplatform;
    }

    private String checkNumericParameter(String parameterValue) {

        return parameterValue == null || parameterValue.trim().equals("") ? "0" : parameterValue;

    }
}
