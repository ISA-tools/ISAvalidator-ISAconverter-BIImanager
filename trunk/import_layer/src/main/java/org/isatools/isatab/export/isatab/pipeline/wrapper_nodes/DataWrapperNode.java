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
import org.isatools.tablib.export.graph_algorithm.DefaultTabValueGroup;
import org.isatools.tablib.export.graph_algorithm.Node;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.model.Data;
import uk.ac.ebi.bioinvindex.model.processing.DataNode;
import uk.ac.ebi.bioinvindex.model.term.DataType;
import uk.ac.ebi.bioinvindex.model.term.FactorValue;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * The specific implementation of {@link Node} for {@link DataNode}.
 *
 * @author brandizi
 *         <b>date</b>: Mar 12, 2010
 */
public class DataWrapperNode extends PipelineWrapperNode<DataNode> {

    /**
     * Used by {@link #createIsolatedClone()}.
     */
    protected DataWrapperNode(DataWrapperNode original) {
        super(original);
    }

    /**
     * Used by {@link WrapperNodesFactory}.
     */
    DataWrapperNode(BIIObjectStore store, DataNode dataNode, String assayFileId) {
        super(store, dataNode, assayFileId);
        init();
    }

    /**
     * Initializes the values returned by {@link GraphElementWrapperNode#getTabValues()}.
     */
    private void init() {
        Data data = node.getData();

        DataType dtype = data.getType();
        String dtypeAcc = dtype.getAcc();
        String dh = null, dhurl = null, dhmurl = null;

        if (containsOne(dtypeAcc, "generic_assay_raw_data")) {
            dhurl = "Raw Data File";
        } else if (containsOne(dtypeAcc, "gel_electrophoresis_raw_data")) {
            dh = "Scan Name";
            dhurl = "Raw Data File";
        } else if (containsOne(dtypeAcc, "microarray_raw_data")) {
            dh = "Scan Name";
            dhurl = "Array Data File";
            dhmurl = "Array Data Matrix File";
        } else if (containsOne(dtypeAcc, "ms_spec_raw_data")) {
            dhurl = "Raw Spectral Data File";
        } else if (containsOne(dtypeAcc, "nmr_spec_raw_data")) {
            dhurl = "Free Induction Decay Data File";
        } else if (containsOne(dtypeAcc, "generic_assay_normalized_data")) {
            dh = "Normalization Name";
            dhurl = "Derived Data File";
        } else if (containsOne(dtypeAcc, "ms_spec_normalized_data", "nmr_spec_normalized_data")) {
            dh = "Normalization Name";
            dhurl = "Derived Spectral Data File";
        } else if (containsOne(dtypeAcc, "gel_normalized_data")) {
            dh = "Normalization Name";
            dhurl = "Derived Data File";
        } else if (containsOne(dtypeAcc, "microarray_normalized_data")) {
            dh = "Normalization Name";
            dhurl = "Derived Array Data File";
            dhmurl = "Derived Array Data Matrix File";
        } else if (containsOne(dtypeAcc, "microarray_derived_data")) {
            dh = "Data Transformation Name";
            dhurl = "Derived Array Data File";
            dhmurl = "Derived Array Data Matrix File";
        } else if (containsOne(dtypeAcc, "ms_spec_derived_data", "nmr_spec_derived_data")) {
            dh = "Data Transformation Name";
            dhurl = "Derived Spectral Data File";
        } else if (containsOne(dtypeAcc, "gel_derived_data", "generic_assay_derived_data")) {
            dh = "Data Transformation Name";
            dhurl = "Derived Data File";
        } else {
            log.debug(
                    "The data of type '" + dtypeAcc + "' doesn't belong to ISATAB and will be ignored by the ISATAB exporter"
            );
            // This is used on a few tests
            dh = "Data Name";
        }

        if (dh != null) {
            String v = StringUtils.trimToNull(data.getName());
            tabValues.add(new DefaultTabValueGroup(dh, v));
        }

        if (dhurl != null) {
            String v = StringUtils.trimToNull(data.getUrl());
            if (v != null) {
                tabValues.add(new DefaultTabValueGroup(dhurl, v));
            }
        }
        if (dhmurl != null) {
            String v = StringUtils.trimToNull(data.getDataMatrixUrl());
            if (v != null) {
                tabValues.add(new DefaultTabValueGroup(dhmurl, v));
            }
        }

        // Factor-values
        SortedSet<FactorValue> sortedFvs = new TreeSet<FactorValue>(
                new Comparator<FactorValue>() {
                    public int compare(FactorValue fv1, FactorValue fv2) {
                        return fv1.getType().getOrder() - fv2.getType().getOrder();
                    }
                }
        );
        for (FactorValue fv : data.getFactorValues()) {
            sortedFvs.add(fv);
        }
        for (FactorValue fv : sortedFvs) {
            initFactorValue(fv);
        }

        // Comments are attached next to the node name
        initComments(data);

        // Annotations that translate into fields
        initAnnotations(data,
                "imageFile", "Image File",
                "spotPickingFile", "Spot Picking File",
                "proteinsFile", "Protein Assignment File",
                "peptidesFile", "Peptide Assignment File",
                "ptmsFile", "Post Translational Modification Assignment File",
                "metaboliteFile", "Metabolite Assignment File"
        );

    }

    public Node createIsolatedClone() {
        return new DataWrapperNode(this);
    }

}
