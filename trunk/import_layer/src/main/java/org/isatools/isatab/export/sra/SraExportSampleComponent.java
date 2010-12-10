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

import org.apache.commons.lang.StringUtils;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.model.Material;
import uk.ac.ebi.bioinvindex.model.processing.Assay;
import uk.ac.ebi.bioinvindex.model.processing.MaterialNode;
import uk.ac.ebi.bioinvindex.model.term.CharacteristicValue;
import uk.ac.ebi.bioinvindex.model.term.MaterialRole;
import uk.ac.ebi.bioinvindex.model.term.OntologyTerm;
import uk.ac.ebi.bioinvindex.model.xref.ReferenceSource;
import uk.ac.ebi.bioinvindex.utils.processing.ProcessingUtils;
import uk.ac.ebi.embl.era.sra.xml.AttributeType;
import uk.ac.ebi.embl.era.sra.xml.ExperimentType.DESIGN.SAMPLEDESCRIPTOR;
import uk.ac.ebi.embl.era.sra.xml.ExperimentType.DESIGN.SAMPLEDESCRIPTOR.POOL;
import uk.ac.ebi.embl.era.sra.xml.ExperimentType.DESIGN.SAMPLEDESCRIPTOR.POOL.MEMBER;
import uk.ac.ebi.embl.era.sra.xml.SAMPLESETDocument.SAMPLESET;
import uk.ac.ebi.embl.era.sra.xml.SampleType;
import uk.ac.ebi.embl.era.sra.xml.SampleType.SAMPLEATTRIBUTES;
import uk.ac.ebi.embl.era.sra.xml.SampleType.SAMPLENAME;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * SRA-exporter, functions related to the building of SRA samples. See {@link SraExportComponent} for further
 * information on how the SRA exporter classes are arranged.
 *
 * @author brandizi
 *         <b>date</b>: Jul 20, 2009, upgraded to SRA 1.0 on June 2010
 */
abstract class SraExportSampleComponent extends SraPipelineExportUtils {

    protected SraExportSampleComponent(BIIObjectStore store, String sourcePath, String exportPath) {
        super(store, sourcePath, exportPath);
    }

// TODO: remove
//	/**
//	 * Builds the SRA sample that is directly associated to the ISATAB assay. This means that the sample must be linked to
//	 * an SRA experiment.
//	 * 
//	 * Exactly one ISATAB sample must be directly associated to the assay. If that is not the case, this method will show a
//	 * warning and return null. SRA export for the assay that falls under that case should be canceled.
//	 * 
//	 * This method will call {@link #buildExportedSample(MaterialNode, SAMPLESET)} to pull up both the material the assay
//	 * directly derives from and all the samples in the downstream pipeline that leads to the assay.
//	 * 
//	 *  
//	 * @param assay the ISATAB assay.
//	 * @param sampleSet the SRA SAMPLESET element, to which the resulting sample will be added.
//	 * 
//	 * @return the descriptor about the newly created sample. This structure contains the sample reference too, which can be
//	 * used in a SRA experiment.
//	 * 
//	 */
//	protected SAMPLEDESCRIPTOR buildExportedAssaySample ( Assay assay, SAMPLESET sampleSet ) 
//	{
//		Material material = assay.getMaterial ();
//		MaterialNode materialNode = material.getMaterialNode ();
//		
//		Collection<MaterialNode> sampleNodes = ProcessingUtils.findBackwardMaterialNodes ( materialNode, "", true );
//		if ( sampleNodes.size () != 1 ) 
//		{
//			String msg = MessageFormat.format ( 
//				"The assay file of type {0} / {1} for study {2} has at least one assay with {3} input samples." + 
//				"This assays are ignored. You need {4}",
//				assay.getMeasurement ().getName (), 
//				assay.getTechnologyName (),
//				assay.getStudy ().getAcc (),
//				sampleNodes.size () == 0 ? "no" : "too many", 
//				sampleNodes.size () == 0 ? "to create at least one sample for the assay" : "to pool the samples"
//			);
//			nonRepeatedMessages.add ( msg );
//			log.trace ( msg + ". Assay is: " + assay.getAcc () );
//			return null;
//		}
//		
//		MaterialNode sampleNode = sampleNodes.iterator ().next ();
//		SampleType xsample = buildExportedSample ( sampleNode, sampleSet );
//		
//		// Finally create a descriptor from the sample and pass it back
//		SAMPLEDESCRIPTOR xsampleRef = SAMPLEDESCRIPTOR.Factory.newInstance ();
//		xsampleRef.setRefname ( xsample.getAlias () );
//		return xsampleRef;
//	}

    /**
     * <p>Builds the sample descriptor for this assay. It populates the sample set at the same time.
     * The method also checks that we have an SRA-compatible structure in the ISATAB file. That is:</p>
     * <p/>
     * <p>
     * source--(0-1)--&gt;sample--(0-n)--&gt;sample--(0-1)--other-biomaterial--&gt;other-biomat--(0-1)--&gt;...assay
     * </p>
     * <p/>
     * <p>which means: we must have a chain from the assay back to its first sample, then we can have at most one
     * level of pooling (no pooled samples composed of other pooled samples). Whatever you have after the sample is
     * merged with the latter and a unique SRA sample is built this way. This solution is one of the most compatible with
     * SRA.</p>
     */
    protected SAMPLEDESCRIPTOR buildExportedAssaySample(Assay assay, SAMPLESET sampleSet) {
        Material material = assay.getMaterial();
        MaterialNode materialNode = material.getMaterialNode();

        MaterialNode baseMatNode = null;

        // Check left (backward) materials until you meet a sample. We cannot have pooling until then.
        //
        while (true) {
            Collection<MaterialNode> leftMaterialNodes = ProcessingUtils.findBackwardMaterialNodes(materialNode, "", true);
            if (leftMaterialNodes.size() != 1) {
                String msg = MessageFormat.format(
                        "The assay file of type {0} / {1} for study {2} has an experimental graph that is not compatible with the " +
                                "SRA format (more than one pooling after the sample headers). Cases like this will be ignored. " +
                                "Please review the ISATAB/SRA encoding guidelines.",
                        assay.getMeasurement().getName(),
                        assay.getTechnologyName(),
                        assay.getStudy().getAcc()
                );
                nonRepeatedMessages.add(msg);
                log.trace(msg + ". Assay is: " + assay.getAcc());
                return null;
            }

            MaterialNode leftMaterialNode = leftMaterialNodes.iterator().next();
            Material leftMat = leftMaterialNode.getMaterial();
            if (baseMatNode == null) {
                baseMatNode = leftMaterialNode;
            }

            MaterialRole leftMatTypeObj = leftMat.getType();
            String leftMatType = leftMatTypeObj.getAcc();
            if (StringUtils.contains(leftMatType, "sample")) {
                // OK, we have the sample, check the pooling
                Collection<SampleType> xpoolMembers = buildExportedPool(leftMaterialNode, assay);
                // If the pooling is invalid, cancel all for this assay
                if (xpoolMembers == null) {
                    return null;
                }

                SampleType xsample = buildSingleExportedSample(baseMatNode);
                if (xsample == null) {
                    return null;
                }

                // Cool, it has a valid structure too, let's build the SRA representation, by merging all
                // the properties and cascaded properties
                //
                SAMPLEDESCRIPTOR xSampledescriptor = SAMPLEDESCRIPTOR.Factory.newInstance();
                xSampledescriptor.setRefname(xsample.getAlias());

                sampleSet.addNewSAMPLE();
                sampleSet.setSAMPLEArray(sampleSet.getSAMPLEArray().length - 1, xsample);

                // If the pool is not empty, add it to the sample descriptor
                // TODO: READ_LABEL and multiplexed experiments not supported yet.
                //
                if (xpoolMembers.size() > 0) {
                    POOL xpool = xSampledescriptor.addNewPOOL();
                    for (SampleType xpoolMember : xpoolMembers) {
                        MEMBER xmember = xpool.addNewMEMBER();
                        xmember.setRefname(xpoolMember.getAlias());
                    }
                }

                // All done for this assay, let's return
                return xSampledescriptor;
            } // if sample found

            materialNode = leftMaterialNode;

        } // while
    }

// TODO: Remove
//	/**
//	 * Builds an SRA sample, from one of the samples in the ISATAB pipeline. This method works recursively, at each call it
//	 * builds the current sample by calling {@link #buildSingleExportedSample(MaterialNode)} over itself and all the samples
//	 * the current sample derives from. This way, references are obtained that are placed into the MEMBERS section of the 
//	 * created sample.  
//	 * 
//	 * @param sampleNode the ISATAB node about a biomaterial that need to be exported. All the ISATAB material types are considered SRA samples
//	 * (there is a tag "Material Role" that have values like bio-source,sample, extract, etc.)
//
//	 * 
//	 * @param sampleSet the SRA sample set, to which the newly created sample will be added.
//	 * 
//	 * @return the SRA {@link SampleType}, i.e.: the SRA representation of the ISATAB sample. 
//	 * 
//	 */
//	private SampleType buildExportedSample ( MaterialNode sampleNode, SAMPLESET sampleSet )
//	{
//		SampleType xsample = buildSingleExportedSample ( sampleNode );
//		
//		// Add backward samples as members
//		Collection<MaterialNode> backwardSampleNodes = ProcessingUtils.findBackwardMaterialNodes ( sampleNode, "", true );
//    MEMBERS xpooledSampleRefs = MEMBERS.Factory.newInstance ();
//		
//		for ( MaterialNode pooledSampleNode: backwardSampleNodes ) 
//		{
//			// Do the same for the pooled sample
//			SampleType xpooledSample = buildExportedSample ( pooledSampleNode, sampleSet );
//			
//			// Link to the xsample as member
//			MEMBER xpooledSampleRef = MEMBER.Factory.newInstance ();
//			
//			// TODO: Does it work?! Read the real barcode (from material comment)
//			xpooledSampleRef.setBarcodeSeqence ( "NA" );
//			xpooledSampleRef.setSampleRef ( xpooledSample.getAlias () );
//			xpooledSampleRefs.addNewMEMBER ();
//			xpooledSampleRefs.setMEMBERArray ( xpooledSampleRefs.sizeOfMEMBERArray () - 1, xpooledSampleRef );
//		}
//		
//		if ( xpooledSampleRefs.sizeOfMEMBERArray () > 0 )
//			xsample.setMEMBERS ( xpooledSampleRefs );
//
//		sampleSet.addNewSAMPLE ();
//		sampleSet.setSAMPLEArray ( sampleSet.sizeOfSAMPLEArray () - 1, xsample );
//		
//		return xsample;	
//	}


    /**
     * Builds a single SRA sample, working only on the sample description and attributes. This is used by
     * {@link #buildExportedSample(MaterialNode, SAMPLESET)}.
     *
     * @param sampleNode the ISATAB node about the input material. All the ISATAB material types are considered SRA samples
     *                   (there is a tag "Material Role" that have values like bio-source,sample, extract, etc.)
     * @return an instance of the SRA SAMPLE element.
     */
    private SampleType buildSingleExportedSample(MaterialNode sampleNode) {
        Material sample = sampleNode.getMaterial();

        SampleType xsample = SampleType.Factory.newInstance();
        SAMPLENAME xsampleName = SAMPLENAME.Factory.newInstance();

        xsample.setCenterName(centerName);
        xsample.setBrokerName(brokerName);

        String xsampleDescription = StringUtils.trimToEmpty(sample.getSingleAnnotationValue("description"));

        SAMPLEATTRIBUTES sampleAttrs = SAMPLEATTRIBUTES.Factory.newInstance();

        MaterialRole sampleType = sample.getType();

        if (StringUtils.containsIgnoreCase(sampleType.getAcc(), "sample")) {
            xsample.setAlias(sample.getAcc());

            String sampleName = StringUtils.trimToNull(sample.getName());
            if (sampleName != null) {
                // TODO: Common name is a GeneBank identifier and not supported for the moment
                // xsampleName.setCOMMONNAME ( sampleName );
                xsample.setTITLE(sampleName);
            }
        }

// It seems there are only samples in SRA, no other roles    
//    sampleAttrs.addNewSAMPLEATTRIBUTE ();
//    sampleAttrs.setSAMPLEATTRIBUTEArray ( sampleAttrs.sizeOfSAMPLEATTRIBUTEArray () - 1, 
//    	buildSampleAttribute ( "Material Experimental Role", sampleType.getName (), null )
//    );


        // Adds up the sample characteristics
        for (CharacteristicValue cvalue : sample.getCharacteristicValues()) {
            AttributeType xattr = characteristicValue2SampleAttr(cvalue);
            if (xattr != null) {
                sampleAttrs.addNewSAMPLEATTRIBUTE();
                sampleAttrs.setSAMPLEATTRIBUTEArray(sampleAttrs.sizeOfSAMPLEATTRIBUTEArray() - 1, xattr);
            }

            // Set the NCBI term if found. TODO: factorize
            for (OntologyTerm oe : cvalue.getOntologyTerms()) {
                ReferenceSource src = oe.getSource();
                if (src == null) {
                    break;
                }
                if (!StringUtils.containsIgnoreCase(src.getDescription(), "NCBI Taxonomy")) {
                    break;
                }
                String taxon = oe.getAcc();
                if (taxon == null) {
                    break;
                }
                try {
                    xsampleName.setTAXONID(Integer.parseInt(taxon));
                }
                catch (NumberFormatException ex) {
                    nonRepeatedMessages.add("Invalid NCBI ID '" + taxon + "', ignoring it");
                }
                break; // Only the first one
            }

        }

        // Lookup the sample this sample derives from and and collect all their characteristics. We think it makes sense to
        // do that cause we're not sure the sample members (in a pool) will be taken into account
        // by SRA software recipients. Moreover, we merge together biomaterials that are not samples (extracts,
        // labeled extracts).
        //
        Collection<MaterialNode> backwardNodes = ProcessingUtils.findBackwardMaterialNodes(sampleNode, "", false);
        for (MaterialNode backwardNode : backwardNodes) {
            Material backwardMaterial = backwardNode.getMaterial();

            sampleType = backwardMaterial.getType();
            if (StringUtils.trimToNull(xsample.getAlias()) == null && StringUtils.containsIgnoreCase(sampleType.getAcc(), "sample")) {
                xsample.setAlias(backwardMaterial.getAcc());

                String sampleName = StringUtils.trimToNull(backwardMaterial.getName());
                if (sampleName != null) {
                    // TODO: Common name is a GeneBank identifier and not supported for the moment
                    // xsampleName.setCOMMONNAME ( sampleName );
//					sampleAttrs.addNewSAMPLEATTRIBUTE ();
//					sampleAttrs.setSAMPLEATTRIBUTEArray ( sampleAttrs.sizeOfSAMPLEATTRIBUTEArray () - 1, 
//						buildSampleAttribute ( "Material Name", sampleName, null )
//					);
                    xsample.setTITLE(sampleName);
                }
            }


            for (CharacteristicValue cvalue : backwardMaterial.getCharacteristicValues()) {
                AttributeType xattr = characteristicValue2SampleAttr(cvalue);
                if (xattr != null) {
                    sampleAttrs.addNewSAMPLEATTRIBUTE();
                    sampleAttrs.setSAMPLEATTRIBUTEArray(sampleAttrs.sizeOfSAMPLEATTRIBUTEArray() - 1, xattr);
                }

                // Set the NCBI term if found. TODO: factorize
                for (OntologyTerm oe : cvalue.getOntologyTerms()) {
                    ReferenceSource src = oe.getSource();
                    if (src == null) {
                        break;
                    }
                    if (!StringUtils.containsIgnoreCase(src.getDescription(), "NCBI Taxonomy")) {
                        break;
                    }
                    String taxon = oe.getAcc();
                    if (taxon == null) {
                        break;
                    }
                    try {
                        xsampleName.setTAXONID(Integer.parseInt(taxon));
                    }
                    catch (NumberFormatException ex) {
                        nonRepeatedMessages.add("Invalid NCBI ID '" + taxon + "', ignoring it");
                    }
                    break; // Only the first one
                }

            } // for ( cvalue )
        }

        // TODO: should we check that xsample.getAlias () is not null?
        //
        xsample.setSAMPLENAME(xsampleName);

        if (sampleAttrs.sizeOfSAMPLEATTRIBUTEArray() > 0) {
            xsample.setSAMPLEATTRIBUTES(sampleAttrs);
        }

        if (xsampleDescription.length() != 0) {
            xsample.setDESCRIPTION(xsampleDescription);
        }

        return xsample;
    }

    /**
     * Attempts to build an SRA pool, starting from the BII owner's node. It assumes that the owner is a
     * sample and tries to see if left nodes are sample too. If not, or if there is more than one level of
     * pooling (not permitted in SRA), files an error message and returns null.
     * <p/>
     * assay is used just to report user messages.
     */
    private Set<SampleType> buildExportedPool(MaterialNode poolOwnerNode, Assay assay) {
        Set<SampleType> result = new HashSet<SampleType>();

        String firstLeftType = null;

        for (MaterialNode leftNode : ProcessingUtils.findBackwardMaterialNodes(poolOwnerNode, "", true)) {
            Material leftMat = leftNode.getMaterial();
            MaterialRole leftTypeTerm = leftMat.getType();
            String leftType = leftTypeTerm.getAcc();
            if (StringUtils.containsIgnoreCase(leftType, "sample")) {
                leftType = "sample";
            } else if (StringUtils.containsIgnoreCase(leftType, "source")) {
                leftType = "source";
            } else {
                String msg = MessageFormat.format(
                        "The assay file of type {0} / {1} for study {2} has biomaterials of type {3}"
                                + " before some sample, this is not valid for the SRA format. Please review the ISATAB/SRA formatting"
                                + " guidelines",
                        assay.getMeasurement().getName(),
                        assay.getTechnologyName(),
                        assay.getStudy().getAcc(),
                        leftTypeTerm.getName()
                );
                nonRepeatedMessages.add(msg);
                log.trace(msg + ". Assay is: " + assay.getAcc());
                return null;
            }
            if (firstLeftType == null) {
                firstLeftType = leftType;
            }

            if ("sample".equals(firstLeftType)) {
                if (!"sample".equals(leftType)) {
                    // Should never happen, cause we don't allow gaps, but anyway...
                    String msg = MessageFormat.format(
                            "The assay file of type {0} / {1} for study {2} has an experimental graph structure that is not "
                                    + "compatible with the SRA format (irregular pooling structure). "
                                    + "Please review the ISATAB/SRA formatting guidelines",
                            assay.getMeasurement().getName(),
                            assay.getTechnologyName(),
                            assay.getStudy().getAcc()
                    );
                    nonRepeatedMessages.add(msg);
                    log.trace(msg + ". Assay is: " + assay.getAcc());
                    return null;
                }

                Collection<MaterialNode> leftLeftNodes = ProcessingUtils.findBackwardMaterialNodes(leftNode, "", true);
                if (leftLeftNodes.size() == 0) {
                    // OK, doesn't have sources, strange, but let's say the left node is a pool member
                    result.add(buildSingleExportedSample(leftNode));
                    continue;
                }

                MaterialNode leftLeftNode = leftLeftNodes.iterator().next();
                Material leftLeftMaterial = leftLeftNode.getMaterial();
                MaterialRole leftLeftRole = leftLeftMaterial.getType();
                if (!StringUtils.containsIgnoreCase(leftLeftRole.getAcc(), "source")) {
                    String msg = MessageFormat.format(
                            "The assay file of type {0} / {1} for study {2} has nested pooled samples, this is not supported by "
                                    + "the SRA format. Please review the ISATAB/SRA formatting guidelines",
                            assay.getMeasurement().getName(),
                            assay.getTechnologyName(),
                            assay.getStudy().getAcc()
                    );
                    nonRepeatedMessages.add(msg);
                    log.trace(msg + ". Assay is: " + assay.getAcc());
                    return null;
                }

                if (leftLeftNode.getDownstreamProcessings().size() != 0) {
                    String msg = MessageFormat.format(
                            "The assay file of type {0} / {1} for study {2} has an experimental graph structure that is not "
                                    + "compatible with the SRA format (sources with derived nodes). Please review the ISATAB/SRA "
                                    + "formatting guidelines",
                            assay.getMeasurement().getName(),
                            assay.getTechnologyName(),
                            assay.getStudy().getAcc()
                    );
                    nonRepeatedMessages.add(msg);
                    log.trace(msg + ". Assay is: " + assay.getAcc());
                    return null;
                }

                // OK, the structure is fine, let's add a new sample
                result.add(buildSingleExportedSample(leftNode));
            } else {
                // It's a source: check we have all sources on the left and that they are the first nodes.
                //
                if (!"source".equals(leftType)) {
                    // Should never happen, cause we don't allow gaps, but anyway...
                    String msg = MessageFormat.format(
                            "The assay file of type {0} / {1} for study {2} has an experimental graph structure that is not "
                                    + "compatible with the SRA format (irregular pooling structure). "
                                    + "Please review the ISATAB/SRA formatting guidelines",
                            assay.getMeasurement().getName(),
                            assay.getTechnologyName(),
                            assay.getStudy().getAcc()
                    );
                    nonRepeatedMessages.add(msg);
                    log.trace(msg + ". Assay is: " + assay.getAcc());
                    return null;
                }

                if (leftNode.getDownstreamProcessings().size() != 0) {
                    String msg = MessageFormat.format(
                            "The assay file of type {0} / {1} for study {2} has an experimental graph structure that is not "
                                    + "compatible with the SRA format (sources with derived nodes). "
                                    + "Please review the ISATAB/SRA formatting guidelines",
                            assay.getMeasurement().getName(),
                            assay.getTechnologyName(),
                            assay.getStudy().getAcc()
                    );
                    nonRepeatedMessages.add(msg);
                    log.trace(msg + ". Assay is: " + assay.getAcc());
                    return null;
                }
            } // if on type cases
        } // for leftNode

        return result;
    }
}
