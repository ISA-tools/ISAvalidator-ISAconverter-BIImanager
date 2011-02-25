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

package org.isatools.tablib.export.graph_algorithm;

import java.io.File;
import java.io.PrintStream;
import java.util.*;

/**
 * <p>The graph-to-table conversion algorithm.</p>
 * <p/>
 * <p>This applies the method of node splitting: when an experimental graph node is not normalized, ie: has not at most
 * one input and at most one output, it is duplicated into multiple nodes, so that it is possible to redistribute the
 * excess of nodes. The final result is that the initial graph to be converted is transformed into a set of chains, each
 * corresponding to one row of the exported spreadsheet.</p>
 * <p/>
 * <p><b>PLEASE NOTE</b>: we assume that the graph is already layered, ie: every path from a source (a node without inputs)
 * to a destination (a node without outputs) has the same number of nodes of the others and has the same type of nodes
 * at any given layer. The latter means eg.: that the sources are all biosources and at distance 1 from them there
 * are always bio-samples. All the nodes involved must be reachable from the parameter passed to the constructor.
 * <p/>
 * <p><b>DEBUGGING NOTE</b>: you can see how the initial graph is transformed by setting the system property
 * graph2tab.debug_mode=true. This can be done this way in maven:
 * <pre>
 *   mvn -DargLine='-Dgraph2tab.debug_mode=true' -Dtest=[YourTest] test
 * </pre
 * </p>
 * <p/>
 * <dl><dt>date</dt><dd>May 10, 2010</dd></dl>
 *
 * @author brandizi
 */
class ChainsBuilder {
    private Set<Node> nodes;
    private SortedSet<Node> startNodes = null;
    private boolean isInitialized = false;

    private LayersBuilder layersBuilder;
    
    /**
     * Used for debugging code, number file names about dumped DOT graphs
     */
    private static int dotFileNoCounter = 0;


    /**
     * Initializes the graph to be exported with any set of nodes that allow to reach the sources and, from them, the final
     * destinations. The resulting graph must be layered (see the introduction above).
     * 
     * @param layersBuilder it's used in case layering is required, the method 
     * {@link LayersBuilder#addSplittedNode(Node, Node)} will be invoked every time a node is split, so that its
     * layering information is updated. null means we don't require layering at all (cause the graph is even).
     */
    public ChainsBuilder(Set<Node> nodes, LayersBuilder layersBuilder ) {
        this.nodes = nodes;
        this.layersBuilder = layersBuilder;
    }

    /**
     * Just finds and stores the sources (all the nodes having no input).
     */
    private void initStartNodes() {
        if (startNodes != null) {
            return;
        }

        startNodes = new TreeSet<Node>();
        for (Node n : nodes) {
            initStartNodes(n);
        }
    }

    /**
     * Just finds and stores the sources (all the nodes having no input). This is the recursive party.
     */
    private void initStartNodes(Node node) {
        SortedSet<Node> ins = node.getInputs();
        if (ins.isEmpty()) {
            startNodes.add(node);
            return;
        }
        for (Node in : ins) {
            initStartNodes(in);
        }
    }

    /**
     * Loops over {@link #getStartNodes()} and applies {@link #normalize(Node, boolean)} to all the graph.
     */
    private void buildPaths() {
        initStartNodes();

        for (Node n : new LinkedList<Node>(startNodes)) {
            normalize(n, true);
        }
    }

    /**
     * Duplicates the parameter node. The new node will have nodeIn as input (if not null) and nodeOut as
     * output (if not null). Furthermore, nodeIn (nodeOut) is removed from the original node, if this doesn't
     * leave it with zero inputs (outputs)
     */
    private Node split(Node node, Node nodeIn, Node nodeOut) {
        Node clone = node.createIsolatedClone();

        if (nodeIn != null) {
            clone.addInput(nodeIn);
            if (node.getInputs().size() > 1) {
                node.removeInput(nodeIn);
            }
        }
        if (nodeOut != null) {
            clone.addOutput(nodeOut);
            if (node.getOutputs().size() > 1) {
                node.removeOutput(nodeOut);
            }
        }

        if (startNodes.contains(node)) {
            startNodes.add(clone);
        }
        
        if ( layersBuilder != null )
        	layersBuilder.addSplittedNode ( node, clone );
        
        return clone;
    }

    /**
     * The core of the splitting-based algorithm. This function is called for every node in the graph (initial calls pass
     * the source nodes and then there is a recursion inside here). For every node that has too many inputs and/or outputs
     * the graph is transformed via a call to {@link #split(Node, Node, Node)}. This encompasses several cases differing
     * in the number of inputs/outputs that the node has, see the source code for details.
     *
     * @param isTowardRight: the recursion is initially from left to right, in certain cases, we need to re-check nodes
     *                       on the left of the current one, because the latter has been split and, after the split, there are more than one
     *                       edge going back to the left. by passing false to this parameter, the method knows it is going back and not
     *                       traversing the graph for the first time. See the source code for details.
     */
    private void normalize(Node node, boolean isTowardRight) {
        if ("true".equals(System.getProperty("graph2tab.debug_mode"))) {
            try {
                PrintStream outstr = new PrintStream(new File("/tmp/g2t_chains_builder_" + ++dotFileNoCounter + ".dot"));
                outDot(outstr);
                outstr.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        List<Node> ins = new LinkedList<Node>(node.getInputs());
        List<Node> outs = new LinkedList<Node>(node.getOutputs());

        int nins = ins.size(), nouts = outs.size();

        if (nins == 0 || startNodes.contains(node)) {
            // Starting nodes, do the splitting if more than 1 out
            //
            Iterator<Node> outItr = outs.iterator();

            // Let's recycle this node for the first splitting
            outItr.next();

            while (outItr.hasNext()) {
                split(node, null, outItr.next());
            }

            if (isTowardRight) {
                for (Node out : outs) {
                    normalize(out, true);
                }
            }
        } else if (nouts == 0) {
            // End nodes, do the splitting if more than 1 input.
            //
            Iterator<Node> inItr = ins.iterator();

            // Let's recycle this node for the first splitting
            inItr.next();


            while (inItr.hasNext()) {
                split(node, inItr.next(), null);
            }

            // Propagate back
            if (isTowardRight) {
                for (Node in : ins) {
                    normalize(in, false);
                }
            }
        } else if (nins == 1 && nouts == 1) {
            // Already normalized node, let's gain a bit of performance by stopping if we're coming back from the right
            //
            if (isTowardRight) {
                normalize(outs.get(0), true);
            }
        } else if (nins >= nouts) {
            // Excess of inputs, we have to spread inputs over outputs via splittings
            //
            Iterator<Node> inItr = ins.iterator(),
                    outItr = outs.iterator();

            // Recycle this node for the first splitting
            inItr.next();
            outItr.next();
            while (inItr.hasNext()) {
                if (!outItr.hasNext()) {
                    outItr = outs.iterator();
                }
                split(node, inItr.next(), outItr.next());
            }

            // and then we have to continue rightward
            //
            if (isTowardRight) {
                for (Node out : outs) {
                    normalize(out, true);
                }
            }
        } else // nins < nouts
        {
            // Excess of outputs, we need to spread outputs over inputs
            //
            Iterator<Node> inItr = ins.iterator(),
                    outItr = outs.iterator();
            // Recycle this node for the first splitting
            inItr.next();
            outItr.next();
            while (outItr.hasNext()) {
                if (!inItr.hasNext()) {
                    inItr = ins.iterator();
                }
                split(node, inItr.next(), outItr.next());
            }

            // and then we have to continue both leftward and rightward
            // The right spread is only to be issued when we come here from the left, otherwise we're just going back
            // to left and some other caller is already taking care of the right direction
            //
            if (isTowardRight) {
                for (Node out : outs) {
                    normalize(out, true);
                }
            }
            for (Node in : ins) {
                normalize(in, false);
            }
        }
    }

    /**
     * The computed starting nodes, ie: those that haven't any inputs. This returns a set of chains, each one representing
     * a row in the exported table.
     */
    public Set<Node> getStartNodes() {
        if (!isInitialized) {
            buildPaths();
        }
        return Collections.unmodifiableSet(startNodes);
    }

    /**
     * A facility useful for debugging. Outputs a syntax that can be used by GraphViz to show the graph being
     * built.
     */
    public void outDot(PrintStream out) {
        Map<Node, Integer> ids = new HashMap<Node, Integer>();
        Set<Node> visited = new HashSet<Node>();

        out.println("strict digraph ExperimentalPipeline {");
        out.println("  graph [rankdir=LR];");

        for (Node node : startNodes) {
            outDot(out, ids, visited, node);
        }

        out.println("}");
    }

    /**
     * @see #outDot(PrintStream).
     */
    private void outDot(PrintStream out, Map<Node, Integer> ids, Set<Node> visited, Node node) {
        if (visited.contains(node)) {
            return;
        }
        visited.add(node);

        String nodelbl = node.toString();
        Integer nodeid = ids.get(node);
        if (nodeid == null) {
            nodeid = ids.size();
            ids.put(node, nodeid);
            out.println("  " + nodeid + "[label = \"" + nodelbl + "\"];");
        }

        for (Node nout : node.getOutputs()) {
            Integer outid = ids.get(nout);
            if (outid == null) {
                outid = ids.size();
                ids.put(nout, outid);
                String outlbl = nout.toString();
                out.println("  " + outid + "[label = \"" + outlbl + "\"];");
            }

            out.println("  " + nodeid + " -> " + outid + ";");
            outDot(out, ids, visited, nout);
        }
    }

}
