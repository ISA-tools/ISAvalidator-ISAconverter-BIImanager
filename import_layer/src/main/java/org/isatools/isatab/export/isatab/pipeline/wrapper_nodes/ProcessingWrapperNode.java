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

import org.isatools.tablib.export.graph_algorithm.DefaultTabValueGroup;
import org.isatools.tablib.export.graph_algorithm.Node;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.model.Protocol;
import uk.ac.ebi.bioinvindex.model.processing.Processing;
import uk.ac.ebi.bioinvindex.model.processing.ProtocolApplication;
import uk.ac.ebi.bioinvindex.model.term.Parameter;
import uk.ac.ebi.bioinvindex.model.term.ParameterValue;

import java.util.*;

/**
 * Produces a tabular fragment for a processing step and its protocol applications.
 *
 * @author brandizi
 *         <b>date</b>: Mar 12, 2010
 */
@SuppressWarnings("unchecked")
public class ProcessingWrapperNode extends GraphElementWrapperNode {
    private Processing processing;

    /**
     * Used by {@link WrapperNodesFactory}.
     */
    ProcessingWrapperNode(BIIObjectStore store, Processing processing, String assayFileId) {
        super(store, assayFileId);
        this.processing = processing;
        init();
    }

    /**
     * Used by {@link #createIsolatedClone()}.
     */
    protected ProcessingWrapperNode(ProcessingWrapperNode original) {
        super(original);
        this.processing = original.processing;
    }

    /**
     * Initializes the values returned by {@link GraphElementWrapperNode#getTabValues()}.
     */
    private void init() {
        SortedMap<Integer, ProtocolApplication> mpapps = new TreeMap<Integer, ProtocolApplication>();
        for (ProtocolApplication papp : (Collection<ProtocolApplication>) processing.getProtocolApplications()) {
            mpapps.put(papp.getOrder(), papp);
        }

        for (int order : mpapps.keySet()) {
            initProtocolApp(mpapps.get(order));
        }
    }

    /**
     * Adds a protocol ref and its parameters to the headers/values of this application
     */
    private void initProtocolApp(ProtocolApplication papp) {
        Protocol proto = papp.getProtocol();
        String pname = proto.getName();

        tabValues.add(new DefaultTabValueGroup("Protocol REF", pname));

        // Comments are attached next to the node name
        initComments(papp);

        for (ParameterValue pv : papp.getParameterValues()) {
            if ("Performer".equals(pv.getType().getValue()))
            // This has its own header
            {
                tabValues.add(new DefaultTabValueGroup("Performer", pv.getValue()));
            } else {
                initParameterValue(pv);
            }
        }
    }

    /**
     * Adds a parameter value to the headers/values of this application
     */
    protected void initParameterValue(ParameterValue pv) {
        Parameter type = pv.getType();
        initProperty(
                "Parameter Value [" + type.getValue() + "]",
                pv.getValue(),
                pv.getSingleOntologyTerm(),
                pv.getUnit()
        );
    }

    public Node createIsolatedClone() {
        return new ProcessingWrapperNode(this);
    }

    /**
     * If it's not a {@link NodeType#CLONE} node returns all the inputs for this processing.
     */
    public SortedSet<Node> getInputs() {
        if (inputs == null) {
            inputs = new TreeSet<Node>();
            WrapperNodesFactory nodeFactory = WrapperNodesFactory.getInstance();

            if (nodeType == NodeType.REGULAR || nodeType == NodeType.END) {
                for (uk.ac.ebi.bioinvindex.model.processing.Node input :
                        (Collection<uk.ac.ebi.bioinvindex.model.processing.Node>) processing.getInputNodes()
                        ) {
                    inputs.add(nodeFactory.getNode(input));
                }
            }
        }
        return Collections.unmodifiableSortedSet(inputs);
    }

    /**
     * If it's not a {@link NodeType#CLONE} node returns all the outputs for this processing.
     */
    public SortedSet<Node> getOutputs() {
        if (outputs == null) {
            outputs = new TreeSet<Node>();
            WrapperNodesFactory nodeFactory = WrapperNodesFactory.getInstance();

            if (nodeType == NodeType.REGULAR || nodeType == NodeType.START) {
                for (uk.ac.ebi.bioinvindex.model.processing.Node output :
                        (Collection<uk.ac.ebi.bioinvindex.model.processing.Node>) processing.getOutputNodes()
                        ) {
                    outputs.add(nodeFactory.getNode(output));
                }
            }
        }

        return Collections.unmodifiableSortedSet(outputs);
    }


}
