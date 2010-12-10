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

package org.isatools.isatab.export.isatab.pipeline;

import org.isatools.isatab.export.isatab.pipeline.wrapper_nodes.WrapperNodesFactory;
import org.isatools.tablib.export.graph_algorithm.Node;
import org.isatools.tablib.export.graph_algorithm.TableBuilder;
import org.isatools.tablib.mapping.pipeline.ProcessingEntityTabMapper;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.model.Material;
import uk.ac.ebi.bioinvindex.model.Study;
import uk.ac.ebi.bioinvindex.model.processing.Assay;
import uk.ac.ebi.bioinvindex.model.processing.MaterialNode;
import uk.ac.ebi.bioinvindex.utils.processing.ProcessingUtils;

import java.util.HashSet;
import java.util.List;

/**
 * Builds a table view of an experimental pipeline, taken from an instance of the BII model.
 * Uses the approach described in  section 3.3.2 of
 * <a href = "http://annotare.googlecode.com/files/MAGE-TABv1.1.pdf">this document</a>.
 *
 * @author brandizi
 *         <b>date</b>: Dec 16, 2009
 */
public class IsaTabTableBuilder extends TableBuilder {

    private final BIIObjectStore store;
    private final Study study;
    private final String assayFileId;


    /**
     * Works with a single study and on all the assays belonging to a particular assay type.
     * if isSorted is true sort the table.
     * <p/>
     * If wantSample exports the sample file for this study, otherwise exports those assays coming from
     * assayFileId.
     * <p/>
     * store is needed to populate it with met ontology sources.
     */
    public IsaTabTableBuilder(BIIObjectStore store, Study study, String assayFileId) {
        super();
        this.store = store;
        this.study = study;
        this.assayFileId = assayFileId;
    }


    /**
     * A default with wantedSample = true;
     */
    public IsaTabTableBuilder(BIIObjectStore store, Study study) {
        this(store, study, null);
    }


    @SuppressWarnings("unchecked")
    @Override
    public List<List<String>> getTable() {
        if (table != null) {
            return table;
        }

        WrapperNodesFactory nodeFactory = WrapperNodesFactory.createInstance(store, assayFileId);
        nodes = new HashSet<Node>();

        if (assayFileId == null) {
            for (Assay assay : study.getAssays()) {
                Material am = assay.getMaterial();
                if (am == null) {
                    continue;
                }
                uk.ac.ebi.bioinvindex.model.processing.Node mn = am.getMaterialNode();
                if (mn == null) {
                    continue;
                }

                for (uk.ac.ebi.bioinvindex.model.processing.Node startNode : ProcessingUtils.findStartingNodes(mn)) {
                    MaterialNode mnode = (MaterialNode) startNode;
                    // Sample nodes not having the sample file annotation are likely used in the assay file only and we must
                    // ignore them here
                    if (mnode.getMaterial().getSingleAnnotationValue(ProcessingEntityTabMapper.SAMPLE_FILE_ANNOTATION_TAG) == null) {
                        continue;
                    }
                    nodes.add(nodeFactory.getNode(mnode));
                }
            }
        } else {
            for (Assay assay : study.getAssays()) {
                Material am = assay.getMaterial();
                if (am == null) {
                    continue;
                }
                MaterialNode mnode = am.getMaterialNode();
                if (mnode == null) {
                    continue;
                }
                if (!mnode.getAssayFileIds().contains(assayFileId)) {
                    continue;
                }

                nodes.add(nodeFactory.getNode(mnode));
            }
        }

        return super.getTable();
    }


//	public boolean wantSample () {
//		return assayFileId == null;
//	}

    public String getSampleFileId() {
        return
                study.getSingleAnnotationValue(ProcessingEntityTabMapper.SAMPLE_FILE_ANNOTATION_TAG);
    }

    public String getAssayFileId() {
        return assayFileId;
    }

} // class
