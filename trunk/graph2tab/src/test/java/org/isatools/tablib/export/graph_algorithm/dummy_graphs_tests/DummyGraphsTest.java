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

import org.isatools.tablib.export.graph_algorithm.Node;
import org.isatools.tablib.export.graph_algorithm.TableBuilder;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.lang.System.out;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * <dl><dt>date</dt><dd>Jun 1, 2010</dd></dl>
 *
 * @author brandizi
 */
public class DummyGraphsTest {
	private void assertContains(List<List<String>> paths, String V1, String V2) {
		assertContains(paths, V1, V2, true);
	}

	private void assertDoesntContain(List<List<String>> paths, String V1, String V2) {
		assertContains(paths, V1, V2, false);
	}

	/**
	 * Verifies that a matrix returned by {@link TableBuilder#getTable()} (doesn't) contains the edge V1-&gt;V2, ie
	 * two subsequent cells in the same column, having the parameter values.
	 * This is used in the tests below.
	 */
	private void assertContains(List<List<String>> paths, String V1, String V2, boolean wanted) {
		int lastColIdx = paths.get(0).size() - 1;
		for (int i = 0; i < paths.size(); i++) {
			List<String> path = paths.get(i);
			assertEquals("Error in table sizes!: " + i + ": " + path, lastColIdx, path.size() - 1);
			for (int j = 0; j < lastColIdx;) {
				if (V1.equals(path.get(j++)) && V2.equals(path.get(j))) {
					if (wanted) {
						return;
					}
					fail("Table Error! should not exist: " + V1 + " -> " + V2);
				}
			}
		}
		if (wanted) {
			fail("Table Error! should exist: " + V1 + " -> " + V2);
		}
	}

	/**
	 * <p> Tests This example graph:</p>
	 * <p/>
	 * <img src = "exp_graph1.png">
	 */
	@Test
	public void testG1() {
		out.println("_______ PATHS TEST 1 __________ ");

		Set<Node> nodes = new HashSet<Node>();

		DummyGraph g = new DummyGraph();

		g.addChain(0, "ACDEF");
		g.addChain(0, "BC");
		nodes.add(g.addChain(2, "DGH"));
		g.addChain(3, "GI");

		TableBuilder tb = new TableBuilder(nodes);
		out.println(tb.report());

		List<List<String>> paths = tb.getTable();

		assertEquals("N. rows Error!", 4, paths.size());
		assertEquals("N. cols Error!", 5, paths.get(0).size());

		assertContains(paths, "C", "D");
		assertContains(paths, "G", "H");
		assertContains(paths, "B", "C");
		assertContains(paths, "D", "E");

		assertDoesntContain(paths, "A", "B");
		assertDoesntContain(paths, "E", "H");
		assertDoesntContain(paths, "E", "I");
		assertDoesntContain(paths, "G", "F");

	}


	/**
	 * <p> Tests This example graph:</p>
	 * <p/>
	 * <img src = "exp_graph2.png">
	 */
	@Test
	public void testG2() {
		out.println("_______ PATHS TEST 2 __________ ");

		Set<Node> nodes = new HashSet<Node>();

		DummyGraph g = new DummyGraph();

		g.addChain(0, "FEDCA");
		g.addChain(0, "HGDCB");
		g.addChain(0, "IGD");

		nodes.add(g.getNode(2, "D"));

		TableBuilder tb = new TableBuilder(nodes);
		out.println(tb.report());

		List<List<String>> paths = tb.getTable();

		assertEquals("N. rows Error!", 4, paths.size());
		assertEquals("N. cols Error!", 5, paths.get(2).size());

		assertContains(paths, "H", "G");
		assertContains(paths, "I", "G");
		assertContains(paths, "C", "A");
		assertContains(paths, "C", "B");
		assertContains(paths, "D", "C");
		assertContains(paths, "F", "E");

		assertDoesntContain(paths, "I", "E");
		assertDoesntContain(paths, "F", "G");

	}

	/* TODO: G3, G4, G5 (see attached images)
		 * I've already tested these cases in an old version of this library and they work. I've to
		 * migrate the code here.
		 *
		 */

}
