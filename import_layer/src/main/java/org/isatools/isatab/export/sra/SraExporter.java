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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.xmlbeans.XmlOptions;
import org.isatools.isatab.export.sra.templateutil.SRAUtils;
import org.isatools.isatab.mapping.AssayTypeEntries;
import org.isatools.tablib.exceptions.TabIOException;
import org.isatools.tablib.exceptions.TabMissingValueException;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.model.Contact;
import uk.ac.ebi.bioinvindex.model.Investigation;
import uk.ac.ebi.bioinvindex.model.Publication;
import uk.ac.ebi.bioinvindex.model.Study;
import uk.ac.ebi.bioinvindex.model.processing.Assay;
import uk.ac.ebi.bioinvindex.model.term.ContactRole;
import uk.ac.ebi.bioinvindex.model.term.Design;
import uk.ac.ebi.bioinvindex.model.term.PublicationStatus;
import uk.ac.ebi.bioinvindex.utils.datasourceload.DataLocationManager;

import uk.ac.ebi.embl.era.sra.xml.AttributeType;
import uk.ac.ebi.embl.era.sra.xml.EXPERIMENTSETDocument;
import uk.ac.ebi.embl.era.sra.xml.ExperimentSetType;
import uk.ac.ebi.embl.era.sra.xml.LinkType;
import uk.ac.ebi.embl.era.sra.xml.LinkType.ENTREZLINK;
import uk.ac.ebi.embl.era.sra.xml.LinkType.URLLINK;
import uk.ac.ebi.embl.era.sra.xml.RUNSETDocument;
import uk.ac.ebi.embl.era.sra.xml.RunSetType;
import uk.ac.ebi.embl.era.sra.xml.SAMPLESETDocument;
import uk.ac.ebi.embl.era.sra.xml.STUDYDocument;
import uk.ac.ebi.embl.era.sra.xml.SUBMISSIONDocument;
import uk.ac.ebi.embl.era.sra.xml.SampleSetType;
import uk.ac.ebi.embl.era.sra.xml.StudyType;
import uk.ac.ebi.embl.era.sra.xml.StudyType.DESCRIPTOR.STUDYTYPE;
import uk.ac.ebi.embl.era.sra.xml.StudyType.DESCRIPTOR.STUDYTYPE.ExistingStudyType;
import uk.ac.ebi.embl.era.sra.xml.StudyType.STUDYATTRIBUTES;
import uk.ac.ebi.embl.era.sra.xml.StudyType.STUDYLINKS;
import uk.ac.ebi.embl.era.sra.xml.SubmissionType;
import uk.ac.ebi.embl.era.sra.xml.SubmissionType.ACTIONS;
import uk.ac.ebi.embl.era.sra.xml.SubmissionType.ACTIONS.ACTION;
import uk.ac.ebi.embl.era.sra.xml.SubmissionType.ACTIONS.ACTION.ADD;
import uk.ac.ebi.embl.era.sra.xml.SubmissionType.ACTIONS.ACTION.MODIFY;
import uk.ac.ebi.embl.era.sra.xml.SubmissionType.ACTIONS.ACTION.VALIDATE;
import uk.ac.ebi.embl.era.sra.xml.SubmissionType.ACTIONS.ACTION.HOLD;
import uk.ac.ebi.embl.era.sra.xml.SubmissionType.ACTIONS.ACTION.CANCEL;
import uk.ac.ebi.embl.era.sra.xml.SubmissionType.CONTACTS;
import uk.ac.ebi.embl.era.sra.xml.SubmissionType.CONTACTS.CONTACT;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;


/**
 * The SRA exporter. Makes SRA submissions (http://www.ebi.ac.uk/ena/). One
 * SRA submission is made for every study that has at least one assay file of type "sra" (to be specified in the
 * isatab_dispatch_mappings.csv file).
 *
 * @author brandizi
 * @author eamonnmag
 * @author agbeltran
 * @author proccaserra
 *         <p/>
 *         <b>date</b>: Jul 20, 2009
 */
public class SraExporter extends SraExportPipelineComponent {
    /**
     * @param store      the object store that contains the BII model to be converted into SRA submissions
     * @param sourcePath the directory of the ISA-Tab submission
     * @param exportPath the directory where the SRA files are saved. We create the "sra/" directory under this path.
     *                   We further create one submission per ISATAB study, naming it with the study accession (e.g.: export/sra/BII-S-4/).
     *                   In each of these directories you'll have a set of SRA files corresponding to one submission and to the BII study.
     */
    public SraExporter(BIIObjectStore store, String sourcePath, String exportPath) {
        super(store, sourcePath, exportPath);
        log.info("Successfully created SraExporter");
    }


    /**
     * The procedure to call for triggering the export
     */
    public void export() {
        log.info("SraExporter.export()");
        for (final Study study : store.valuesOfType(Study.class)) {
            // Go ahead only if there is some SRA assay
            boolean haveSra = false;
            for (Assay assay : study.getAssays()) {
                if ("sra".equals(AssayTypeEntries.getDispatchTargetIdFromLabels(assay))) {
                    haveSra = true;
                    break;
                }
            }
            if (!haveSra) {
                nonRepeatedMessages.add("No SRA assay found, skipping processing");
                continue;
            }


            final Investigation investigation = study.getUniqueInvestigation();
            if (investigation != null) {
                ndc.pushObject(investigation);
            }
            ndc.pushObject(study);

            String studyAcc = study.getAcc();

            log.trace("SraExporter, Working on study " + studyAcc);

            // Prepare the submission

            SubmissionType xsubmission = SubmissionType.Factory.newInstance();

            centerName = StringUtils.trimToNull(study.getSingleAnnotationValue("comment:SRA Center Name"));
            if (centerName == null) {
                throw new TabMissingValueException(MessageFormat.format(
                        "The study ''{0}'' has no 'SRA Center Name', cannot export to SRA format",
                        studyAcc
                ));
            }
            xsubmission.setCenterName(centerName);

            String submissionId = "";
            xsubmission.setAccession(submissionId);
            xsubmission.setAlias(studyAcc);


            //brokerName = StringUtils.trimToNull(study.getSingleAnnotationValue("comment:SRA Broker Name"));
            if (brokerName == null) {
                brokerName = "ISAcreator";
//                log.warn(MessageFormat.format(
//                        "The study ''{0}'' has no 'SRA Broker Name'",
//                        study.getAcc()
//                ));
            } else {
                xsubmission.setBrokerName(brokerName);
            }

            String labName = null; //StringUtils.trimToNull(study.getSingleAnnotationValue("comment:SRA Lab Name"));
            if (labName == null) {
//                labName=centerName;
//                xsubmission.setLabName(labName);

//                log.warn(MessageFormat.format(
//                        "The study ''{0}'' has no 'SRA Lab Name'",
//                        study.getAcc()
//                ));
            } else {
                xsubmission.setLabName(centerName);
            }


            Date subDate = study.getSubmissionDate();
            if (subDate != null) {
                Calendar xsubDate = Calendar.getInstance();
                xsubDate.setTime(subDate);
                xsubmission.setSubmissionDate(xsubDate);
            }

            buildExportedSubmissionContacts(xsubmission, study);
            buildStudyActions(xsubmission, study);


            //FILES xsubFiles = FILES.Factory.newInstance();
            RunSetType runSet = RunSetType.Factory.newInstance();
            ExperimentSetType expSet = ExperimentSetType.Factory.newInstance();
            SampleSetType sampleSet = SampleSetType.Factory.newInstance();

            //final int minFiles = xsubFiles.sizeOfFILEArray();

            STUDYDocument xstudyDoc = null;

            boolean isAssayOk = true;

            for (Assay assay : study.getAssays()) {
                // todo investigate

                if ("sra".equals(AssayTypeEntries.getDispatchTargetIdFromLabels(assay))) {
                    // Prepare the study
                    xstudyDoc = buildExportedStudy(study);

                    ndc.pushObject(assay);
                    log.trace("SraExporter, Working on assay " + assay.getAcc());


                    // Adds up the information built with the assay
                    if (!buildExportedAssay(assay, /*xsubFiles,*/ runSet, expSet, sampleSet)) {
                        isAssayOk = false;
                        // Skip all the assay file if only a single assay is wrong, a partial export is too dangerous
                        break;
                    }
                    ndc.popObject(); // assay
                }
            } // for Assay


            // If there is at least one run, we have to write the files in the export path, we need one
            // submission per study
            //
            if (isAssayOk && xstudyDoc != null) {

                String xSubmissionPath = exportPath + "/" + DataLocationManager.accession2FileName(studyAcc);
                try {
                    File xsubmissionDir = new File(xSubmissionPath);

                    if (!xsubmissionDir.exists()) {
                        FileUtils.forceMkdir(xsubmissionDir);
                    }

                    log.debug("SRA exporter: writing SRA XML files for study " + studyAcc);
                    SUBMISSIONDocument xsubmissionDoc = SUBMISSIONDocument.Factory.newInstance();
                    xsubmissionDoc.setSUBMISSION(xsubmission);

                    FileUtils.writeStringToFile(new File(xSubmissionPath + "/submission_initial.xml"), xsubmissionDoc.toString());
                    FileUtils.writeStringToFile(new File(xSubmissionPath + "/study_initial.xml"), xstudyDoc.toString());

                    SAMPLESETDocument sampleSetDoc = SAMPLESETDocument.Factory.newInstance();
                    sampleSetDoc.setSAMPLESET(sampleSet);
                    FileUtils.writeStringToFile(new File(xSubmissionPath + "/sample_set_initial.xml"), sampleSetDoc.toString());


                    EXPERIMENTSETDocument expSetDoc = EXPERIMENTSETDocument.Factory.newInstance();
                    EXPERIMENTSETDocument.Factory.newInstance();
                    expSetDoc.setEXPERIMENTSET(expSet);

                    // A modification is made on the XML to be output to remove any tags required for injection of elements into
                    // the DOM during conversion. These tags are usually found as <INJECTED_TAG>. The SRAUtils.removeInjectedTags method
                    // finds and replaces these tags with empty spaces.
                    FileUtils.writeStringToFile(new File(xSubmissionPath + "/experiment_set_initial.xml"), SRAUtils.removeInjectedTags(expSetDoc.toString()));

                    RUNSETDocument runSetDoc = RUNSETDocument.Factory.newInstance();
                    runSetDoc.setRUNSET(runSet);
                    FileUtils.writeStringToFile(new File(xSubmissionPath + "/run_set_initial.xml"), runSetDoc.toString());


                } catch (IOException ex) {
                    throw new TabIOException(MessageFormat.format("Error during SRA export of study {0}: {1}", studyAcc, ex.getMessage()), ex);
                } finally {
                    //we need to add the schema namespace to the output files in order to allow validation
                    SRAXMLSchemaInjector.addNameSpaceToFile(new File(xSubmissionPath + "/submission_initial.xml"), "SRA.submission.xsd", "<SUBMISSION center_name");
                    SRAXMLSchemaInjector.addNameSpaceToFile(new File(xSubmissionPath + "/study_initial.xml"), "SRA.study.xsd", "<STUDY alias");
                    SRAXMLSchemaInjector.addNameSpaceToFile(new File(xSubmissionPath + "/sample_set_initial.xml"), "SRA.sample.xsd", "<SAMPLE_SET>");
                    SRAXMLSchemaInjector.addNameSpaceToFile(new File(xSubmissionPath + "/experiment_set_initial.xml"), "SRA.experiment.xsd", "<EXPERIMENT_SET>");
                    SRAXMLSchemaInjector.addNameSpaceToFile(new File(xSubmissionPath + "/run_set_initial.xml"), "SRA.run.xsd", "<RUN_SET>");


                    //}
                    SRAXMLSchemaInjector.delete(new File(xSubmissionPath + "/submission_initial.xml"));
                    SRAXMLSchemaInjector.delete(new File(xSubmissionPath + "/study_initial.xml"));
                    SRAXMLSchemaInjector.delete(new File(xSubmissionPath + "/sample_set_initial.xml"));
                    SRAXMLSchemaInjector.delete(new File(xSubmissionPath + "/experiment_set_initial.xml"));
                    SRAXMLSchemaInjector.delete(new File(xSubmissionPath + "/run_set_initial.xml"));

                }
            } // is assay OK

            ndc.popObject(); // study
            if (investigation != null) {
                ndc.popObject();
            } // investigation
        } // for ( study )

        // Finally, output some messages...
        if (nonRepeatedMessages.size() > 0) {
            log.warn("SRA export completed with the following warnings:");
            for (String msg : nonRepeatedMessages) {
                log.warn(msg);
            }
        }
    }

    /**
     * Builds the SRA study from the ISATAB study, assuming this has some ENA assays.
     *
     * @return the SRA STUDY element that is to be used to build the corresponding XML study file.
     */
    private STUDYDocument buildExportedStudy(Study study) {

        final String studyAcc = study.getAcc();
        final Investigation investigation = study.getUniqueInvestigation();

        XmlOptions xmlOptions = new XmlOptions();
        xmlOptions.setSaveNamespacesFirst();

        STUDYDocument xstudyDoc = STUDYDocument.Factory.newInstance(xmlOptions);
        StudyType xstudy = StudyType.Factory.newInstance();
        xstudy.setAlias(studyAcc);

        StudyType.DESCRIPTOR xdescriptor = StudyType.DESCRIPTOR.Factory.newInstance();
        String description = StringUtils.trimToNull(study.getDescription());

        xdescriptor.setCENTERNAME(centerName);
        xstudy.setCenterName(centerName);
        if (!brokerName.equals(""))
            xstudy.setBrokerName(brokerName);


        String centerPrjName = StringUtils.trimToNull(study.getSingleAnnotationValue("comment:SRA Center Project Name"));
        if (centerPrjName == null) {
            centerPrjName = centerName;

//            throw new TabMissingValueException(MessageFormat.format(
//                    "The study ''{0}'' has no 'SRA Center Project Name', cannot export to SRA format",
//                    study.getAcc()
//            ));
        }
        xdescriptor.setCENTERPROJECTNAME(centerPrjName);


        String title = StringUtils.trimToNull(study.getTitle());
        if (title != null) {
            xdescriptor.setSTUDYTITLE(title);
        }

        String studyAbstract = StringUtils.trimToNull(study.getDescription());
        if (studyAbstract != null) {
            xdescriptor.setSTUDYABSTRACT(studyAbstract);
        }

        STUDYATTRIBUTES xattrs = STUDYATTRIBUTES.Factory.newInstance();
        STUDYLINKS xlinks = STUDYLINKS.Factory.newInstance();

        Date subDate = study.getSubmissionDate();
        if (subDate != null) {
            AttributeType xdate = buildStudyAttribute("Submission Date", DateFormatUtils.format(subDate, "dd/MM/yyyy"), null);
            xattrs.addNewSTUDYATTRIBUTE();
            xattrs.setSTUDYATTRIBUTEArray(xattrs.sizeOfSTUDYATTRIBUTEArray() - 1, xdate);
        }

        Date relDate = study.getReleaseDate();
        if (relDate != null) {
            AttributeType xdate = buildStudyAttribute("Release Date", DateFormatUtils.format(relDate, "dd/MM/yyyy"), null);
            xattrs.addNewSTUDYATTRIBUTE();
            xattrs.setSTUDYATTRIBUTEArray(xattrs.sizeOfSTUDYATTRIBUTEArray() - 1, xdate);
        }

        {
            AttributeType xacc = buildStudyAttribute("BII Study Accession", studyAcc, null);
            xattrs.addNewSTUDYATTRIBUTE();
            xattrs.setSTUDYATTRIBUTEArray(xattrs.sizeOfSTUDYATTRIBUTEArray() - 1, xacc);
        }

        if (investigation != null) {
            String invAcc = investigation.getAcc();
            if (investigation.getAcc() != null) {
                AttributeType xacc = buildStudyAttribute("BII Investigation Accession", invAcc, null);
                xattrs.addNewSTUDYATTRIBUTE();
                xattrs.setSTUDYATTRIBUTEArray(xattrs.sizeOfSTUDYATTRIBUTEArray() - 1, xacc);
            }
        }


        String studyDescr = StringUtils.trimToNull(study.getDescription());
        if (studyDescr != null) {
            xdescriptor.setSTUDYDESCRIPTION(studyDescr);
        }

        Collection<Design> designs = study.getDesigns();
        if (designs == null || designs.isEmpty()) {
            throw new TabMissingValueException(
                    MessageFormat.format("Study ''{0}'' has no study design, cannot be exported to SRA format", studyAcc)
            );
        }
        Design design = designs.iterator().next();

        // Try to see if the ISATAB study type is listed in the SRA schema, use 'other' otherwise.

        Collection<Assay> assays = study.getAssays();
        if (assays == null || assays.isEmpty()) {
            throw new TabMissingValueException(
                    MessageFormat.format("Study ''{0}'' has no study design, cannot be exported to SRA format", studyAcc)
            );
        }
        String measurement = assays.iterator().next().getMeasurement().getName();
        STUDYTYPE xstudyType = STUDYTYPE.Factory.newInstance();

        if (measurement.equalsIgnoreCase("transcription profiling")) {
            ExistingStudyType.Enum xdesign = ExistingStudyType.TRANSCRIPTOME_ANALYSIS;
            xstudyType.setExistingStudyType(xdesign);
        } else if (measurement.equalsIgnoreCase("environmental gene survey")) {
            ExistingStudyType.Enum xdesign = ExistingStudyType.METAGENOMICS;
            xstudyType.setExistingStudyType(xdesign);
        } else if (measurement.equalsIgnoreCase("DNA-protein binding site identification")) {
            ExistingStudyType.Enum xdesign = ExistingStudyType.GENE_REGULATION_STUDY;
            xstudyType.setExistingStudyType(xdesign);
        } else if (measurement.equalsIgnoreCase("transcription factor binding site identification")) {
            ExistingStudyType.Enum xdesign = ExistingStudyType.GENE_REGULATION_STUDY;
            xstudyType.setExistingStudyType(xdesign);
        } else if (measurement.equalsIgnoreCase("DNA methylation profiling")) {
            ExistingStudyType.Enum xdesign = ExistingStudyType.EPIGENETICS;
            xstudyType.setExistingStudyType(xdesign);
        } else if (measurement.equalsIgnoreCase("genome sequencing")) {
            ExistingStudyType.Enum xdesign = ExistingStudyType.WHOLE_GENOME_SEQUENCING;
            xstudyType.setExistingStudyType(xdesign);
        } else if (measurement.equalsIgnoreCase("chromosome rearrangement")) {
            ExistingStudyType.Enum xdesign = ExistingStudyType.POPULATION_GENOMICS;
            xstudyType.setExistingStudyType(xdesign);
        } else {
            xstudyType.setExistingStudyType(ExistingStudyType.OTHER);
        }

        xdescriptor.setSTUDYTYPE(xstudyType);
        xstudy.setDESCRIPTOR(xdescriptor);

        for (Contact contact : study.getContacts()) {
            if (!contact.getFullName().isEmpty()) {
                buildExportedContact(contact, xattrs, false);
            }
        }
        if (investigation != null) {
            for (Contact contact : investigation.getContacts()) {
                buildExportedContact(contact, xattrs, true);
            }
        }

        for (Publication publication : study.getPublications()) {
            buildExportedPublication(publication, xattrs, xlinks, false);
        }
        if (investigation != null) {
            for (Publication publication : investigation.getPublications()) {
                buildExportedPublication(publication, xattrs, xlinks, true);
            }
        }

        //TODO: BII link, protocols

        if (xattrs.sizeOfSTUDYATTRIBUTEArray() != 0) {
            xstudy.setSTUDYATTRIBUTES(xattrs);
        }
        if (xlinks.sizeOfSTUDYLINKArray() != 0) {
            xstudy.setSTUDYLINKS(xlinks);
        }

        if (description != null) {
            xdescriptor.setSTUDYDESCRIPTION(description);
            xstudy.setDESCRIPTOR(xdescriptor);
        }

        xstudyDoc.setSTUDY(xstudy);
        return xstudyDoc;
    }


    /**
     * Export an ISATAB Investigation/study contact as a SRA study attribute.
     *
     * @param contact         the ISATAB contact
     * @param xattrs          the study attributes, to which new entries about this contact are added.
     * @param isInvestigation use an appropriate tag depending on the fact this contact is for the study or
     *                        its investigation.
     */
    private void buildExportedContact(Contact contact, STUDYATTRIBUTES xattrs, boolean isInvestigation) {
        final String prefixLabel = isInvestigation ? "Investigation " : "Study ";

        String contactValueStr = "";

        String name = contact.getFullName();
        if (name != null) {
            contactValueStr += "Name: " + name + "\n";
        }

        String email = contact.getEmail();
        if (email != null) {
            contactValueStr += "e-mail: " + email + "\n";
        }

        String affiliation = contact.getAffiliation();
        if (affiliation != null) {
            contactValueStr += "Affiliation: " + affiliation + "\n";
        }

        String addr = contact.getAddress();
        if (addr != null) {
            contactValueStr += "Address: " + addr + "\n";
        }


        // TODO: phone, fax, url
        for (ContactRole role : contact.getRoles()) {
            contactValueStr += "Role: " + role.getName() + "\n";
        }

        if (contactValueStr.length() == 0) {
            return;
        }

        xattrs.addNewSTUDYATTRIBUTE();
        xattrs.setSTUDYATTRIBUTEArray(xattrs.sizeOfSTUDYATTRIBUTEArray() - 1,
                buildStudyAttribute(prefixLabel + "Contact", contactValueStr, null)
        );
    }


    /**
     * Export an ISATAB Investigation/Study publication as a SRA study attribute.
     *
     * @param xattrs          the study attributes, to which new entries about this publication are added.
     * @param isInvestigation use an appropriate tag depending on the fact this contact is for the study or
     *                        its investigation.
     */
    private void buildExportedPublication(Publication pub, STUDYATTRIBUTES xattrs, STUDYLINKS xlinks, boolean isInvestigation) {
        final String prefixLabel = isInvestigation ? "Investigation " : "Study ";
        String pubStr = "";

        String title = pub.getTitle();
        if (title != null) {
            pubStr += "Title: " + title + "\n";
        }

        String authors = pub.getAuthorList();
        if (authors != null) {
            pubStr += "Authors: " + authors + "\n";
        }

        PublicationStatus status = pub.getStatus();
        if (status != null) {
            pubStr += "Status: " + status.getName() + "\n";
        }

        String pmid = pub.getPmid();
        if (pmid != null) {
            pubStr += "PUBMED ID: " + pmid + "\n";
            ENTREZLINK pmedLink = ENTREZLINK.Factory.newInstance();
            pmedLink.setDB("pubmed");

            try {
                pmedLink.setID(new BigInteger(pmid));
                LinkType xlink = LinkType.Factory.newInstance();
                xlink.setENTREZLINK(pmedLink);
                xlinks.addNewSTUDYLINK();
                xlinks.setSTUDYLINKArray(xlinks.sizeOfSTUDYLINKArray() - 1, xlink);
            } catch (NumberFormatException ex) {
                log.warn("The PUBMED ID '" + pmid + "' for '" + title + "' is not valid, not exporting this publication");
            }
        }

        String doi = pub.getDoi();
        if (doi != null) {
            pubStr += "DOI: " + doi + "\n";
            URLLINK doiLink = URLLINK.Factory.newInstance();
            doiLink.setLABEL(prefixLabel + "Publication DOI");
            doiLink.setURL("http://dx.doi.org/" + doi);
            LinkType xlink = LinkType.Factory.newInstance();
            xlink.setURLLINK(doiLink);
            xlinks.addNewSTUDYLINK();
            xlinks.setSTUDYLINKArray(xlinks.sizeOfSTUDYLINKArray() - 1, xlink);
        }

        if (pubStr.length() == 0) {
            return;
        }

        xattrs.addNewSTUDYATTRIBUTE();
        xattrs.setSTUDYATTRIBUTEArray(xattrs.sizeOfSTUDYATTRIBUTEArray() - 1,
                buildStudyAttribute(prefixLabel + "Publication", pubStr, null)
        );

    }


    /**
     * Builds an SRA {@link } from a tag/value/unit triple.
     */
    private AttributeType buildStudyAttribute(String tag, String value, String unit) {
        if (tag == null || value == null) {
            return null;
        }

        AttributeType xattr = AttributeType.Factory.newInstance();
        xattr.setTAG(tag);
        xattr.setVALUE(value);
        if (unit != null) {
            xattr.setUNITS(unit);
        }
        return xattr;
    }


    /**
     * Builds the CONTACT elements in the SRA SUBMISSION node. This is taken from those SRA contacts that have an appropriate
     * "SRA Inform On Status" or "SRA Inform On Error" role.
     * <p/>
     * These contacts are mandatory, so an exception is raised if missing.
     *
     * @param xsub  appends the result to this SRA submission.
     * @param study the input study where to take the contacts from.
     */
    private void buildExportedSubmissionContacts(final SubmissionType xsub, final Study study) {
        CONTACTS xcontacts = CONTACTS.Factory.newInstance();
        for (Contact contact : study.getContacts()) {

            String email = contact.getEmail();
            if (email == null || email.length() == 0) {
                continue;
            }

            CONTACT xcontact = CONTACT.Factory.newInstance();
            xcontact.setName(contact.getFullName());

            boolean isSraContact = false;
            for (ContactRole role : contact.getRoles()) {
                String roleStr = role.getName();
                if ("SRA Inform On Status".equalsIgnoreCase(roleStr)) {
                    xcontact.setInformOnStatus(email);
                    isSraContact = true;
                } else if ("SRA Inform On Error".equalsIgnoreCase(roleStr)) {
                    xcontact.setInformOnError(email);
                    isSraContact = true;
                }
            }

            if (!isSraContact) {
                continue;
            }

            xcontacts.addNewCONTACT();
            xcontacts.setCONTACTArray(xcontacts.sizeOfCONTACTArray() - 1, xcontact);
        }

        if (xcontacts.sizeOfCONTACTArray() == 0) {
            throw new TabMissingValueException(MessageFormat.format("The study ''{0}'' has either no SRA contact or no email specified for the contact. Please ensure you have one contact with a 'Role' as 'SRA Inform On Status', otherwise we cannot export to SRA.", study.getAcc()));
        }
        xsub.setCONTACTS(xcontacts);
    }

    /**
     * Builds the ACTION elements for the submission of the parameter study. These are taken from certain ISA-Tab comments
     * in the ISA-Tab study. At least one action must be defined in SRA, so the procedure raises an exception in case this
     * does not happen.
     *
     * @param xsub  appends the result to this SRA submission.
     * @param study the input study where to take the contacts from.
     */
    private void buildStudyActions(final SubmissionType xsub, final Study study) {

        ACTIONS xactions = ACTIONS.Factory.newInstance();

        String action = StringUtils.trimToNull(study.getSingleAnnotationValue("comment:SRA Submission Action"));

        if ("ADD".equalsIgnoreCase(action)) {
            final String[] sources = new String[]{"study", "sample_set", "experiment_set", "run_set"};//, "analysis" could be added when needed
            final ADD.Schema.Enum[] schemas = new ADD.Schema.Enum[]{
                    ADD.Schema.STUDY, ADD.Schema.SAMPLE, ADD.Schema.EXPERIMENT, ADD.Schema.RUN
            };

            for (int i = 0; i < schemas.length; i++) {
                ADD xaddAction = ADD.Factory.newInstance();
                //source and schema are mandatory
                xaddAction.setSchema(schemas[i]);
                xaddAction.setSource(sources[i] + ".xml");
                ACTION xaction = xactions.addNewACTION();
                xaction.setADD(xaddAction);
            }

            if (xactions.sizeOfACTIONArray() == 0) {
                throw new TabMissingValueException(MessageFormat.format(
                        "The study ''{0}'' has no SRA Submission Action, cannot export to SRA", study.getAcc()));
            }

            xsub.setACTIONS(xactions);
        }

        if ("MODIFY".equalsIgnoreCase(action)) {
            final String[] sources = new String[]{"study", "sample_set", "experiment_set", "run_set"};//, "analysis" could be added when needed
            final MODIFY.Schema.Enum[] schemas = new MODIFY.Schema.Enum[]{
                    MODIFY.Schema.STUDY, MODIFY.Schema.SAMPLE, MODIFY.Schema.EXPERIMENT, MODIFY.Schema.RUN
            };

            for (int i = 0; i < schemas.length; i++) {
                MODIFY xmodifyAction = MODIFY.Factory.newInstance();
                //source and schema are mandatory
                xmodifyAction.setSchema(schemas[i]);
                xmodifyAction.setSource(sources[i] + ".xml");
                ACTION xaction = xactions.addNewACTION();
                xaction.setMODIFY(xmodifyAction);
            }

            if (xactions.sizeOfACTIONArray() == 0) {
                throw new TabMissingValueException(MessageFormat.format(
                        "The study ''{0}'' has no SRA Submission Action, cannot export to SRA", study.getAcc()));
            }

            xsub.setACTIONS(xactions);
        }

        if ("VALIDATE".equalsIgnoreCase(action)) {
            final String[] sources = new String[]{"study", "sample_set", "experiment_set", "run_set"};//, "analysis" could be added when needed
            final VALIDATE.Schema.Enum[] schemas = new VALIDATE.Schema.Enum[]{
                    VALIDATE.Schema.STUDY, VALIDATE.Schema.SAMPLE, VALIDATE.Schema.EXPERIMENT, VALIDATE.Schema.RUN
            };

            for (int i = 0; i < schemas.length; i++) {
                VALIDATE xvalidateAction = VALIDATE.Factory.newInstance();
                //source and schema are mandatory
                xvalidateAction.setSchema(schemas[i]);
                xvalidateAction.setSource(sources[i] + ".xml");
                ACTION xaction = xactions.addNewACTION();
                xaction.setVALIDATE(xvalidateAction);
            }

            if (xactions.sizeOfACTIONArray() == 0) {
                throw new TabMissingValueException(MessageFormat.format(
                        "The study ''{0}'' has no SRA Submission Action, cannot export to SRA", study.getAcc()));
            }

            xsub.setACTIONS(xactions);
        }

        if ("SUPPRESS".equalsIgnoreCase(action)) {
            ACTION.SUPPRESS xsuppressAction = ACTION.SUPPRESS.Factory.newInstance();
            xsuppressAction.setTarget(study.getAcc());
            ACTION xaction = xactions.addNewACTION();
            xaction.setSUPPRESS(xsuppressAction);

            if (xactions.sizeOfACTIONArray() == 0) {
                throw new TabMissingValueException(MessageFormat.format(
                        "The study ''{0}'' has no SRA Submission Action, cannot export to SRA", study.getAcc()));
            }

            xsub.setACTIONS(xactions);
        }

    }
}