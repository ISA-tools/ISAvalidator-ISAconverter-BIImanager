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

package org.isatools.tablib.export.graph_algorithm.dummy_graphs_tests;

import org.isatools.tablib.export.graph_algorithm.DefaultAbstractNode;
import org.isatools.tablib.export.graph_algorithm.DefaultTabValueGroup;
import org.isatools.tablib.export.graph_algorithm.Node;
import org.isatools.tablib.export.graph_algorithm.TabValueGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * A sample implementation of {@link Node}. This is is really simple and based on the straight extension of
 * {@link DefaultAbstractNode}. Typically you won't work like in this simple example, you'll will want to build
 * node wrappers and a node wrapper factory, see TODO.
 * <p/>
 * Note that the comparison/equivalence/hashing methods of {@link DefaultAbstractNode} are fine for this
 * implementation as well, because, they compares by node identity and sort by using the first value in
 * {@link #getTabValues()}.
 * <p/>
 * <dl><dt>date</dt><dd>May 31, 2010</dd></dl>
 *
 * @author brandizi
 */
public class DummyNode extends DefaultAbstractNode {
	private final int column;
	private final String value;

	/**
	 * This will result in a {@link TabValueGroup} with a single pair in it, where header = "Foo Header $column" and
	 * value is the value provided here.
	 */
	DummyNode(int column, String value) {
		this.column = column;
		this.value = value;
		this.inputs = new TreeSet<Node>();
		this.outputs = new TreeSet<Node>();
	}

	/**
	 * This is used during cloning
	 */
	private DummyNode(DummyNode original) {
		this(original.column, original.value);
	}

	/**
	 * Just verify that it's a {@link DummyNode}, used by addXXX().
	 */
	private void checkNodeType(Node node) {
		if (!(node instanceof DummyNode)) {
			throw new IllegalArgumentException("DummyNode(s) works only with other DummyNode(s)");
		}
	}

	/**
	 * Just checks that the input is of DummyNode
	 */
	public boolean addInput(Node input) {
		checkNodeType(input);
		return super.addInput(input);
	}

	/**
	 * Just checks that the output is of DummyNode
	 */
	public boolean addOutput(Node output) {
		checkNodeType(output);
		return super.addOutput(output);
	}

	/**
	 * See the constructor.
	 */
	public int getColumn() {
		return column;
	}

	/**
	 * See the constructor.
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Will use the constructor an will pass it itself. This is the typical approach to implement this.
	 */
	public Node createIsolatedClone() {
		return new DummyNode(this);
	}

	/**
	 * @return a single {@link TabValueGroup} with a single pair in it, where header = "Foo Header " + {@link #getColumn()}
	 *         and value is {@link #getValue()}.
	 */
	public List<TabValueGroup> getTabValues() {
		List<TabValueGroup> result = new ArrayList<TabValueGroup>();
		result.add(new DefaultTabValueGroup("Foo Header " + column, value));
		return result;
	}

}
