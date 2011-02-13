/*
 * __________
 * CREDITS
 * __________
 *
 * Team page: http://isatab.sf.net/
 * - Marco Brandizi (software engineer: ISAvalidator, ISAconverter, BII data management utility, BII model)
 * - Eamonn Maguire (software engineer: ISAcreator, ISAcreator configurator, ISAvalidator, ISAconverter,  BII data management utility, BII web)
 * - Nataliya Sklyar (software engineer: BII web application, BII model,  BII data management utility)
 * - Philippe Rocca-Serra (technical coordinator: user requirements and standards compliance for ISA software, ISA-tab format specification, BII model, ISAcreator wizard, ontology)
 * - Susanna-Assunta Sansone (coordinator: ISA infrastructure design, standards compliance, ISA-tab format specification, BII model, funds raising)
 *
 * Contributors:
 * - Manon Delahaye (ISA team trainee:  BII web services)
 * - Richard Evans (ISA team trainee: rISAtab)
 *
 *
 * ______________________
 * Contacts and Feedback:
 * ______________________
 *
 * Project overview: http://isatab.sourceforge.net/
 *
 * To follow general discussion: isatab-devel@list.sourceforge.net
 * To contact the developers: isatools@googlegroups.com
 *
 * To report bugs: http://sourceforge.net/tracker/?group_id=215183&atid=1032649
 * To request enhancements:  http://sourceforge.net/tracker/?group_id=215183&atid=1032652
 *
 *
 * __________
 * License:
 * __________
 *
 * This work is licenced under the Creative Commons Attribution-Share Alike 2.0 UK: England & Wales License. To view a copy of this licence, visit http://creativecommons.org/licenses/by-sa/2.0/uk/ or send a letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California 94105, USA.
 *
 * __________
 * Sponsors
 * __________
 * This work has been funded mainly by the EU Carcinogenomics (http://www.carcinogenomics.eu) [PL 037712] and in part by the
 * EU NuGO [NoE 503630](http://www.nugo.org/everyone) projects and in part by EMBL-EBI.
 */

package org.isatools.isatab.export.isatab.pipeline.wrapper_nodes;

import org.isatools.tablib.export.graph_algorithm.Node;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.model.processing.MaterialNode;
import uk.ac.ebi.bioinvindex.model.processing.Processing;

import java.util.Collection;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Provides common methods needed to implement {@link Node} for BII's
 * {@link uk.ac.ebi.bioinvindex.model.processing.Node}s.
 * <p/>
 * <p/>
 * <dl><dt>date</dt><dd>May 10, 2010</dd></dl>
 *
 * @author brandizi
 * @param <NT>
 */
@SuppressWarnings("unchecked")
public abstract class PipelineWrapperNode<NT extends uk.ac.ebi.bioinvindex.model.processing.Node>
		extends GraphElementWrapperNode {

	protected NT node;

	/**
	 * Used by {@link WrapperNodesFactory}.
	 */
	protected PipelineWrapperNode(BIIObjectStore store, NT node, String assayFileId) {
		super(store, assayFileId);
		this.node = node;
	}

	/**
	 * Used by {@link #createIsolatedClone()}.
	 */
	protected PipelineWrapperNode(PipelineWrapperNode original) {
		super(original);
		this.node = (NT) original.node;
	}

	/**
	 * Returns the left processings of this node, unless the node is the starting node of a sample file or
	 * of an assay file. In the latter case, omits the processings not belonging to the current assay for which
	 * the export is being generated.
	 */
	public SortedSet<Node> getInputs() {
		if (inputs == null) {
			inputs = new TreeSet<Node>();
			WrapperNodesFactory nodeFactory = WrapperNodesFactory.getInstance();

			if (nodeType == NodeType.REGULAR || nodeType == NodeType.END) {
				for (Processing inproc : (Collection<Processing>) node.getDownstreamProcessings()) {
					inputs.add(nodeFactory.getNode(inproc));
				}
			}
		}
		return Collections.unmodifiableSortedSet(inputs);
	}

	/**
	 * Stops at last samples in the sample if assayFileId == null, otherwise starts from them (if they are
	 * used by the specific sample file).
	 */
	public SortedSet<Node> getOutputs() {
		if (outputs == null) {
			outputs = new TreeSet<Node>();
			WrapperNodesFactory nodeFactory = WrapperNodesFactory.getInstance();

			if (nodeType == NodeType.REGULAR || nodeType == NodeType.START) {
				for (Processing outproc : (Collection<Processing>) node.getUpstreamProcessings()) {
					// If we're building a sample, only take the processings about this sample
					//
					if (assayFileId != null && this.node instanceof MaterialNode) {
						uk.ac.ebi.bioinvindex.model.processing.Node outprocOut =
								((Collection<uk.ac.ebi.bioinvindex.model.processing.Node>) outproc.getOutputNodes()).iterator().next();

						if (!outprocOut.getAssayFileIds().contains(assayFileId)) {
							continue;
						}
					}
					outputs.add(nodeFactory.getNode(outproc));
				}// for
			}// if
		}
		return Collections.unmodifiableSortedSet(outputs);
	}

}
