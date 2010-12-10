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
 * - Manon Delahaye (ISA team trainee: BII web services)
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
 * To request enhancements: Êhttp://sourceforge.net/tracker/?group_id=215183&atid=1032652
 *
 *
 * __________
 * License:
 * __________
 *
 * Reciprocal Public License 1.5 (RPL1.5)
 * [OSI Approved License]
 *
 * Reciprocal Public License (RPL)
 * Version 1.5, July 15, 2007
 * Copyright (C) 2001-2007
 * Technical Pursuit Inc.,
 * All Rights Reserved.
 *
 * http://www.opensource.org/licenses/rpl1.5.txt
 *
 * __________
 * Sponsors
 * __________
 * This work has been funded mainly by the EU Carcinogenomics (http://www.carcinogenomics.eu) [PL 037712] and in part by the
 * EU NuGO [NoE 503630](http://www.nugo.org/everyone) projects and in part by EMBL-EBI.
 */

package org.isatools.tablib.schema;

import junit.framework.TestCase;

import java.util.List;

/**
 * Basic tests on classes used to represent a TAB format (schema)
 *
 * @author brandizi
 */
public class SchemaNodeTest extends TestCase {

	/* Static init */

	{
		if (rootNode == null) {
			System.out.println("--- SchemaNodeTest, Initializing ---");
			rootNode = new SchemaNode("n0");
			addNodes(rootNode, 1, 3, 3);
		}
	}


	public SchemaNodeTest(String testName) {
		super(testName);
	}

	protected void addNodes(SchemaNode root, int level, int maxLevel, int howMany) {
		if (level > maxLevel) {
			return;
		}
		int lev1 = level + 1;
		String rid = root.getAttr("id");
		for (int i = 0; i < howMany; i++) {
			SchemaNode newNode = new SchemaNode(rid + "." + i);
			root.addChild(newNode);
			System.out.println("-- Added: " + newNode.getAttr("id") + " to " + root.getAttr("id"));
			addNodes(newNode, lev1, maxLevel, howMany);
		}
	}

	protected static SchemaNode rootNode;
	protected static final int LEVS = 3, NPLEVS = 3;


	protected void setUp() throws Exception {
	}

	protected void tearDown() throws Exception {
	}

	public void testCreate() {
		assertEquals("Tree was created", rootNode.getAttr("id"), "n0");
	}


	/**
	 * Tests getDescendants()
	 */
	public void testDescendants() {
		List<SchemaNode> descs = rootNode.getDescendants();

		System.out.println("\n\n--- Dumping all appended nodes ---");
		for (SchemaNode n : descs) {
			System.out.println(n.getAttr("id"));
		}

		assertNotNull("We have descendants", descs);
		double n = (NPLEVS - Math.pow(NPLEVS, LEVS + 1)) / (1 - NPLEVS);
		assertEquals("Nodes created are: " + n, n, 1.0 * descs.size());
	}

	/**
	 * Tests getChildren()
	 */
	public void testChildren() {
		List<SchemaNode> children = rootNode.getChildren();

		System.out.println("\n\n--- Dumping all children ---");
		for (SchemaNode n : children) {
			System.out.println(n.getAttr("id"));
		}
		assertNotNull("We have children", children);
		assertEquals("Nodes created are: " + NPLEVS, NPLEVS, children.size());
	}


	/**
	 * Tests getDescendand( id ), with node searching
	 */
	public void testFind() {
		System.out.println("\n\n--- testFind ---");
		String id = "n0.1.2";
		SchemaNode node = rootNode.getDescendant(id);

		System.out.println("\n\n--- Found node: " + (node == null ? null : node.getAttr("id")));

		assertNotNull("We have something", node);
		assertEquals("with #" + id, node.getAttr("id"), id);
	}


}
