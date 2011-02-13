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

import org.isatools.tablib.exceptions.TabInvalidValueException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static java.lang.System.out;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Several tests on Field class, used to represent single columns or rows in a TAB
 * <p/>
 * May 30, 2007
 *
 * @author brandizi
 */
public class FieldTest {
	private Field f;

	@Before
	public void setUp() {
		out.println("_______ Testing Field.setHeader()______ ");
	}

	@Test
	public void testBasicHeader() {
		f = new Field("FactorValue");
		f = f.parseHeader("FactorValue", 0);
		out.println(f.getAttr("type") + ", " + f.getAttr("type1"));
		assertEquals("Test 1, Type wrongly recognized", f.getAttr("type"), null);
		assertEquals("Test 1, Type 1 wrongly recognized", f.getAttr("type1"), null);
	}

	@Test
	public void testComplexHeader() {
		f = new Field("FactorValue");
		f = f.parseHeader("FactorValue [Grow Condition]", 1);
		out.println(String.format("Type: '%s', Type 1: '%s'", f.getAttr("type"), f.getAttr("type1")));
		assertEquals("Test 2, Type wrongly recognized", f.getAttr("type"), "Grow Condition");
		assertEquals("Test 2, Type 1 wrongly recognized", f.getAttr("type1"), null);
	}

	@Test
	public void testVeryComplexHeader() {

		f = new Field("FactorValue");
		f = f.parseHeader("FactorValue [Grow Condition] (media)", 0);
		out.println(String.format("Type: '%s', Type 1: '%s'", f.getAttr("type"), f.getAttr("type1")));
		assertEquals("Test 1, Type wrongly recognized", f.getAttr("type"), "Grow Condition");
		assertEquals("Test 1, Type 1 wrongly recognized", f.getAttr("type1"), "media");
	}


	@Test
	public void testWrongHeader() {
		boolean checkPoint = false;
		try {

			f = new Field("FactorValue");
			f = f.parseHeader("FactorValue1 [Grow Condition] (media)", 0);
			out.println(String.format("Type: '%s', Type 1: '%s'", f.getAttr("type"), f.getAttr("type1")));

		}
		catch (TabInvalidValueException ex) {
			checkPoint = true;
		}
		assertTrue("No exception reported for wrong header", checkPoint);
	}

	@Test
	public void testBadSyntaxHeader() {
		boolean checkPoint = false;
		try {

			f = new Field("FactorValue");
			f = f.parseHeader("FactorValue1 Grow Condition] (media)", 0);
		}
		catch (TabInvalidValueException ex) {
			checkPoint = true;
		}
		assertTrue("No Exception reported for wrong header 1", checkPoint);
	}

	@Test
	public void testBadSyntaxHeader1() {
		boolean checkPoint = false;
		try {
			f = new Field("FactorValue )£== [Grow Condition]");
			f = f.parseHeader("FactorValue [Grow Condition] (media)", 0);
		}
		catch (TabInvalidValueException ex) {
			checkPoint = true;
		}
		assertTrue("No Exception reported for wrong header 2", checkPoint);

	}


	@Test
	public void testBadSyntaxHeader2() {
		boolean checkPoint = false;
		try {
			f = new Field("FactorValue [Grow Condition] (media) (another thing)");
			f = f.parseHeader("FactorValue [Grow Condition] (media)", 0);
		}
		catch (TabInvalidValueException ex) {
			checkPoint = true;
		}
		assertTrue("No Exception reported for wrong header 3", checkPoint);
	}


	@After
	public void tearDown() throws Exception {
		out.println("_______ End, Testing Field.setHeader()______\n\n");
	}

}
