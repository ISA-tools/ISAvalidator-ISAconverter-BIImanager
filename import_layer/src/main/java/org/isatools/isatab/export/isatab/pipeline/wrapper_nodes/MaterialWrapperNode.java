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

package org.isatools.isatab.export.isatab.pipeline.wrapper_nodes;

import org.apache.commons.lang.StringUtils;
import org.isatools.tablib.export.graph2tab.DefaultTabValueGroup;
import org.isatools.tablib.export.graph2tab.Node;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.model.Material;
import uk.ac.ebi.bioinvindex.model.processing.MaterialNode;
import uk.ac.ebi.bioinvindex.model.term.Characteristic;
import uk.ac.ebi.bioinvindex.model.term.CharacteristicValue;
import uk.ac.ebi.bioinvindex.model.term.FactorValue;
import uk.ac.ebi.bioinvindex.model.term.MaterialRole;
import uk.ac.ebi.utils.string.StringSearchUtils;

import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * The specific implementation of {@link Node} for {@link MaterialNode}.
 *
 * @author brandizi
 *         <b>date</b>: Mar 12, 2010
 */
public class MaterialWrapperNode extends PipelineWrapperNode<MaterialNode> {

    /**
     * Used by {@link WrapperNodesFactory}.
     */
    MaterialWrapperNode(BIIObjectStore store, MaterialNode materialNode, String assayFileId) {
        super(store, materialNode, assayFileId);

        String sampleFileId = materialNode.getSampleFileId();
        Set<String> assayFileIds = materialNode.getAssayFileIds();

        if (sampleFileId != null && !assayFileIds.isEmpty())
        // This is a boundary sample node, ie the last sample in a sample file and the first in an
        // assay file, tell the wrapper it is a start/end node, depending on which spreadsheet we are
        // building
        {
            nodeType = assayFileId == null ? NodeType.END : NodeType.START;
        }

        init();
    }

    /**
     * Initializes the values returned by {@link GraphElementWrapperNode#getTabValues()}.
     */
    private void init() {
        Material mmaterial = node.getMaterial();
        MaterialRole mtype = mmaterial.getType();
        String mtypeAcc = mtype.getAcc();
        String mh;
        if (StringSearchUtils.containsOneOf(mtypeAcc, "source")) {
            mh = "Source Name";
        } else if (StringSearchUtils.containsOneOf(mtypeAcc, "sample")) {
            mh = "Sample Name";
        } else if (StringSearchUtils.containsOneOf(mtypeAcc, "labelled_extract")) {
            mh = "Labeled Extract Name";
        } else if (StringSearchUtils.containsOneOf(mtypeAcc, "extract")) {
            mh = "Extract Name";
        } else if (StringSearchUtils.containsOneOf(mtypeAcc, "gel_electrophoresis_assay")) {
            mh = "Gel Electrophoresis Assay Name";
        } else if (StringSearchUtils.containsOneOf(mtypeAcc, "generic_assay")) {
            mh = "Assay Name";
        } else if (StringSearchUtils.containsOneOf(mtypeAcc, "hybridization")) {
            mh = "Hybridization Assay Name";
        } else if (StringSearchUtils.containsOneOf(mtypeAcc, "ms_spec")) {
            mh = "MS Assay Name";
        } else if (StringSearchUtils.containsOneOf(mtypeAcc, "nmr_spec")) {
            mh = "NMR Assay Name";
        } else
        // TODO: warning
        {
            mh = "Sample Name";
        }

        String value = StringUtils.trimToNull(mmaterial.getName());
        tabValues.add(new DefaultTabValueGroup(mh, value));

        // Annotations that translate into fields
        initAnnotations(mmaterial,
                "description", "Description",
                "arrayDesignREF", "Array Design REF",
                "provider", "Provider"
        );

        // Comments are attached next to the node name
        initComments(mmaterial);

        // Work out the characteristics
        SortedSet<CharacteristicValue> sortedCvs = new TreeSet<CharacteristicValue>(
                new Comparator<CharacteristicValue>() {
                    public int compare(CharacteristicValue cv1, CharacteristicValue cv2) 
                    {
                    	int deltaOrder = cv1.getType().getOrder() - cv2.getType().getOrder();
                    	if ( deltaOrder != 0 ) return deltaOrder;
                    	// Same order because the two come from different files, so let's use the type
                    	return cv1.getType ().getValue ().compareTo ( cv2.getType ().getValue () );
                    }
                }
        );
        for (CharacteristicValue cv : mmaterial.getCharacteristicValues()) {
            sortedCvs.add(cv);
        }
        // And their values
        for (CharacteristicValue cv : sortedCvs) {
            initCharacteristicValue(cv);
        }


        // Same for factor-values, but skip them if the sample goes to the assay file, cause
        // in such a case they're reported in the sample file only, to avoid duplicates in the
        // assay file.
        //
        if (assayFileId == null) {
            SortedSet<FactorValue> sortedFvs = new TreeSet<FactorValue>(
                    new Comparator<FactorValue>() {
                        public int compare(FactorValue fv1, FactorValue fv2) {
                            return fv1.getType().getOrder() - fv2.getType().getOrder();
                        }
                    }
            );
            for (FactorValue fv : mmaterial.getFactorValues()) {
                sortedFvs.add(fv);
            }
            for (FactorValue fv : sortedFvs) {
                initFactorValue(fv);
            }
        }
    }

    protected void initCharacteristicValue(CharacteristicValue cv) {
        Characteristic type = cv.getType();
        initProperty(
                "Characteristics [" + type.getValue() + "]",
                cv.getValue(),
                cv.getSingleOntologyTerm(),
                cv.getUnit()
        );
    }

}
