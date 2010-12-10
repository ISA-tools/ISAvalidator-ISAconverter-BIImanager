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
 * This work is licenced under the Creative Commons Attribution-Share Alike 2.0 UK: England & Wales License. 
 * To view a copy of this licence, visit http://creativecommons.org/licenses/by-sa/2.0/uk/ or send a letter to
 * Creative Commons, 171 Second Street, Suite 300, San Francisco, California 94105, USA.
 *
 * __________
 * Sponsors
 * __________
 * This work has been funded mainly by the EU Carcinogenomics (http://www.carcinogenomics.eu) [PL 037712] and in part by the
 * EU NuGO [NoE 503630](http://www.nugo.org/everyone) projects and in part by EMBL-EBI.
 */

package org.isatools.tablib.export.graph_algorithm.simple_biomodel_tests.node_wrappers;

import org.isatools.tablib.export.graph_algorithm.DefaultAbstractNode;
import org.isatools.tablib.export.graph_algorithm.DefaultTabValueGroup;
import org.isatools.tablib.export.graph_algorithm.Node;
import org.isatools.tablib.export.graph_algorithm.TabValueGroup;
import org.isatools.tablib.export.graph_algorithm.simple_biomodel_tests.model.Annotation;
import org.isatools.tablib.export.graph_algorithm.simple_biomodel_tests.model.ExperimentNode;
import org.isatools.tablib.export.graph_algorithm.simple_biomodel_tests.model.OntoTerm;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * The basic wrapper of the exported object model. Basically we implement {@link #getInputs()}
 * and {@link #getOutputs()}, plus the way the node provides its table cells, ie {@link #getTabValues()}.
 * <p/>
 * <dl><dt>date</dt><dd>Jun 1, 2010</dd></dl>
 *
 * @author brandizi
 */
public abstract class ExpNodeWrapper extends DefaultAbstractNode {
	private ExperimentNode base;

	/**
	 * This should be used only by your custom factory, {@link NodeFactory} in this example.
	 */
	ExpNodeWrapper(ExperimentNode base) {
		this.base = base;
	}

	/**
	 * This is used by the implementation of {@link Node#createIsolatedClone()}. Essentially, copies
	 * the wrapped node and makes empty input/output sets.
	 */
	protected ExpNodeWrapper(ExpNodeWrapper original) {
		this.base = original.base;
		this.inputs = new TreeSet<Node>();
		this.outputs = new TreeSet<Node>();
	}

	/**
	 * In this case we're able to write a generic method that is customised by the descendants.
	 * The methods shows how to add an ontology term to a free text value. This is done by keeping
	 * all into the same {@link TabValueGroup}.
	 *
	 * @param nameHeader	  eg: "BioMaterial Name", "Protocol REF"
	 * @param annHeaderPrefix eg: "Characteristic", "Parameter Value", here headers will be built
	 *                        with the schema annHeaderPrefix [Êtype ], eg: Characteristic [ÊOrganism ]
	 */
	protected List<TabValueGroup> getTabValues(String nameHeader, String annHeaderPrefix) {
		List<TabValueGroup> result = new ArrayList<TabValueGroup>();
		result.add(new DefaultTabValueGroup(nameHeader, base.getName()));

		for (Annotation annotation : base.getAnnotations()) {
			DefaultTabValueGroup tbg = new DefaultTabValueGroup(
					annHeaderPrefix + " [ " + annotation.getType() + " ]", annotation.getValue()
			);
			OntoTerm ot = annotation.getOntoTerm();
			if (ot != null) {
				tbg.add("Term Accession Number", ot.getAcc());
				tbg.add("Term Source REF", ot.getSource());
			}
			result.add(tbg);
		}
		return result;
	}

	/**
	 * If it's not a clone produced by {@link Node#createIsolatedClone()}, it uses {@link NodeFactory} to build wrappers
	 * for the input nodes of the base and to return them.
	 * <p/>
	 * This is the typical way this method is implemented by.
	 */
	@Override
	public SortedSet<Node> getInputs() {
		if (inputs != null) {
			return super.getInputs();
		}
		inputs = new TreeSet<Node>();
		NodeFactory nodeFact = NodeFactory.getInstance();
		for (ExperimentNode in : base.getInputs()) {
			inputs.add(nodeFact.getNode(in));
		}
		return super.getInputs();
	}

	/**
	 * If it's not a clone produced by {@link Node#createIsolatedClone()}, it uses {@link NodeFactory} to build wrappers
	 * for the output nodes of the base and to return them.
	 * <p/>
	 * This is the typical way this method is implemented by.
	 */
	@Override
	public SortedSet<Node> getOutputs() {
		if (outputs != null) {
			return super.getOutputs();
		}
		outputs = new TreeSet<Node>();
		NodeFactory nodeFact = NodeFactory.getInstance();
		for (ExperimentNode out : base.getOutputs()) {
			outputs.add(nodeFact.getNode(out));
		}
		return super.getOutputs();
	}

}
