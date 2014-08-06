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
import uk.ac.ebi.embl.era.sra.xml.*;
import uk.ac.ebi.embl.era.sra.xml.SampleDescriptorType.POOL;
import uk.ac.ebi.embl.era.sra.xml.SampleType.SAMPLEATTRIBUTES;
import uk.ac.ebi.embl.era.sra.xml.SampleType.SAMPLENAME;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SRA-exporter, functions related to the building of SRA samples. See {@link SraExportComponent} for further
 * information on how the SRA exporter classes are arranged.
 *
 * @author brandizi
 *         <b>date</b>: Jul 20, 2009, upgraded to SRA 1.0 on June 2010
 */
abstract class SraExportSampleComponent extends SraPipelineExportUtils {

    private Set<String> addedSources = new HashSet<String>();

    protected SraExportSampleComponent(BIIObjectStore store, String sourcePath, String exportPath) {
        super(store, sourcePath, exportPath);
    }

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
    protected SampleDescriptorType buildExportedAssaySample(Assay assay, SampleSetType sampleSet) {

        Material material = assay.getMaterial();
        MaterialNode materialNode = material.getMaterialNode();

        MaterialNode baseMatNode = null;

        // Check left (backward) materials until you meet a sample. We cannot have pooling until then.
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
                SampleDescriptorType xSampledescriptor = SampleDescriptorType.Factory.newInstance();
                xSampledescriptor.setRefname(xsample.getAlias());


                if (xsample.getAlias() != null && !addedSources.contains(xsample.getAlias())) {
                    sampleSet.addNewSAMPLE();
                    sampleSet.setSAMPLEArray(sampleSet.getSAMPLEArray().length - 1, xsample);
                    addedSources.add(xsample.getAlias());
                }

                // If the pool is not empty, add it to the sample descriptor
                // TODO: READ_LABEL and multiplexed experiments not supported yet.
                if (xpoolMembers.size() > 0) {
                    POOL xpool = xSampledescriptor.addNewPOOL();
                    for (SampleType xpoolMember : xpoolMembers) {

                        PoolMemberType xmember = xpool.addNewMEMBER();
                        xmember.setRefname(xpoolMember.getAlias());
                    }
                }

                // All done for this assay, let's return
                return xSampledescriptor;
            } // if sample found

            materialNode = leftMaterialNode;

        } // while
    }

    /**
     * Builds a single SRA sample, working only on the sample description and attributes. This is used by
     * {@link #buildSingleExportedSample(MaterialNode)}.
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

        populateSampleAttributes(sample, sampleAttrs, xsampleName);

        // Lookup the sample this sample derives from and and collect all their characteristics. We think it makes sense to
        // do that cause we're not sure the sample members (in a pool) will be taken into account
        // by SRA software recipients. Moreover, we merge together biomaterials that are not samples (extracts,
        // labeled extracts).
        Collection<MaterialNode> backwardNodes = ProcessingUtils.findBackwardMaterialNodes(sampleNode, "", false);
        String sourceName = getSourceName(backwardNodes);

        for (MaterialNode backwardMaterialNode : backwardNodes) {
            populateSampleAttributes(backwardMaterialNode.getMaterial(), sampleAttrs, xsampleName);
        }

        xsample.setAlias(sourceName);
        xsample.setTITLE(sourceName);
        xsample.setSAMPLENAME(xsampleName);

        if (sampleAttrs.sizeOfSAMPLEATTRIBUTEArray() > 0) {
            xsample.setSAMPLEATTRIBUTES(sampleAttrs);
        }

        if (xsampleDescription.length() != 0) {
            xsample.setDESCRIPTION(xsampleDescription);
        }
        return xsample;
    }


    private String getSourceName(Collection<MaterialNode> backwardNodes) {
        for (MaterialNode backwardNode : backwardNodes) {
            Material backwardMaterial = backwardNode.getMaterial();
            if (StringUtils.containsIgnoreCase(backwardMaterial.getType().getAcc(), "source")) {
                return backwardMaterial.getAcc();
            }
        }
        return null;
    }

    private void populateSampleAttributes(Material material, SAMPLEATTRIBUTES sampleAttrs, SAMPLENAME xsampleName) {
        for (CharacteristicValue cvalue : material.getCharacteristicValues()) {


            AttributeType xattr = characteristicValue2SampleAttr(cvalue);
            System.out.println("cvalue = " + cvalue.getType().getValue());
            boolean isOrganismTag = checkCharacteristicValue(cvalue, "(?i)tax|(?i)organism");

            System.out.println("Is an organism tag? " + isOrganismTag);

            if (xattr != null) {
                if (!isOrganismTag) {
                    sampleAttrs.addNewSAMPLEATTRIBUTE();
                    sampleAttrs.setSAMPLEATTRIBUTEArray(sampleAttrs.sizeOfSAMPLEATTRIBUTEArray() - 1, xattr);
                }
            }

            if (isOrganismTag) {
                for (OntologyTerm oe : cvalue.getOntologyTerms()) {

                    ReferenceSource src = oe.getSource();

                    if (src == null) {
                        break;
                    }

                   // wrong logic here. Was OR, should have been AND. In the case where both statements are true,
                   // the overriding conclusion is false due to the negation operation.
                    if ((!StringUtils.containsIgnoreCase(src.getDescription(), "ncbi")) && (!StringUtils.containsIgnoreCase(src.getDescription(), "taxonomy"))) {
                        break;
                    }

                    String taxon = oe.getAcc();

                    //dealing with accesssions as URLs
                    int index_underscore = taxon.indexOf('_');
                    if (index_underscore!=-1)
                        taxon = taxon.substring(index_underscore+1);
                    System.out.println("taxon = " + taxon);

                    if (taxon == null) {
                        break;
                    }
                    try {

                       // log.info("cvalue.getType().getValue()) = " + cvalue.getType().getValue());
                       // System.out.println("cvalue.getType().getValue()) = " + cvalue.getType().getValue());

                        xsampleName.setTAXONID(Integer.parseInt(taxon));
                        xsampleName.setSCIENTIFICNAME(oe.getName());
                       // System.out.println("xattr: " + xattr + oe.getName());


                    } catch (NumberFormatException ex) {
                        nonRepeatedMessages.add("Invalid NCBI ID '" + taxon + "', ignoring it");
                    }
                    break; // Only the first one
                }
            }
        } // for ( cvalue )
    }

    private boolean checkCharacteristicValue(CharacteristicValue cValue, String regex) {

        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(cValue.getType().getValue());
        return m.find();
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
                    SampleType sample = buildSingleExportedSample(leftNode);
                    if (sample != null) {
                        result.add(sample);
                    }
                    continue;
                }

                MaterialNode leftLeftNode = leftLeftNodes.iterator().next();
                Material leftLeftMaterial = leftLeftNode.getMaterial();
                MaterialRole leftLeftRole = leftLeftMaterial.getType();

                if (!StringUtils.containsIgnoreCase(leftLeftRole.getAcc(), "source")) {
                    String msg = MessageFormat.format(
                            "The assay file of type {0} / {1} for study {2} has nested pooled samples, this is not supported by "
                                    + "the SRA format. Please review the ISATAB/SRA formatting guidelines.",
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
                                    + "formatting guidelines.",
                            assay.getMeasurement().getName(),
                            assay.getTechnologyName(),
                            assay.getStudy().getAcc()
                    );
                    nonRepeatedMessages.add(msg);
                    log.trace(msg + ". Assay is: " + assay.getAcc());
                    return null;
                }

                // OK, the structure is fine, let's add a new sample
                SampleType sample = buildSingleExportedSample(leftNode);
                if (sample != null) {
                    result.add(sample);
                }
            } else {
                // It's a source: check we have all sources on the left and that they are the first nodes.
                //
                if (!"source".equals(leftType)) {
                    // Should never happen, cause we don't allow gaps, but anyway...
                    String msg = MessageFormat.format(
                            "The assay file of type {0} / {1} for study {2} has an experimental graph structure that is not "
                                    + "compatible with the SRA format (irregular pooling structure). "
                                    + "Please review the ISATAB/SRA formatting guidelines.",
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
                                    + "Please review the ISATAB/SRA formatting guidelines.",
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
