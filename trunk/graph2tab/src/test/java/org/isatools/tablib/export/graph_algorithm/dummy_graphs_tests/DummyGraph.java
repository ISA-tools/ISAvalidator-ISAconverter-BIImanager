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

import java.util.HashMap;
import java.util.Map;

/**
 * This is used by the tests, mainly to have a quick way to generate graphs from their topology
 * specification (see {@link #addChain(int, String)}).
 * <p/>
 * <dl><dt>date</dt><dd>Jun 1, 2010</dd></dl>
 *
 * @author brandizi
 */
public class DummyGraph {
	private Map<String, DummyNode> nodeMap = new HashMap<String, DummyNode>();

	public DummyNode getNode(int column, String value) {
		String key = "" + column + value;
		DummyNode node = nodeMap.get("" + column + value);
		if (node != null) {
			return node;
		}
		node = new DummyNode(column, value);
		nodeMap.put(key, node);
		return node;
	}

	/**
	 * Creates a chain of nodes that start at the layer startLayer. Each node has a single-character label,
	 * so that the chain is represented by the chLabels string, eg: "ABC" creates the chain
	 * A-&gt;B-&gt;C chain.
	 *
	 * @return the first node in the created chain ( DummyNode ( startNode, "A" ) in the example above).
	 */
	public DummyNode addChain(int startLayer, String chLabels) {
		DummyNode result = null;
		char[] chars = chLabels.toCharArray();
		for (int i = 0; i < chars.length - 1;) {
			String in = "" + chars[i], out = "" + chars[++i];
			DummyNode inn = getNode(startLayer, in), outn = getNode(++startLayer, out);
			inn.addOutput(outn);
			if (i == 1) {
				result = inn;
			}
		}
		return result;
	}

}
