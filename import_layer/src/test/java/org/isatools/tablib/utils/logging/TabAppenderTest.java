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

package org.isatools.tablib.utils.logging;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;
import uk.ac.ebi.bioinvindex.model.Study;

import java.util.List;

import static java.lang.System.out;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TabAppenderTest {
	@Test
	@SuppressWarnings("static-access")
	public void testTabAppender() {
		out.println("\n" + StringUtils.center(" Testing TabAppender ", 120, "-"));

		Logger log = Logger.getLogger(TabAppenderTest.class);
		// This is necessary in order to override external config.
		log.setLevel(Level.ALL);

		ListTabAppender app = new ListTabAppender();
		final String APP_NAME = "testTabAppender";
		app.setName(APP_NAME);
		app.setLayout(new PatternLayout("%d {%x} [%-5p] (%C{1},%L): %m%n"));
		app.setThreshold(Level.ALL);

		Logger rootLog = log.getRootLogger();
		rootLog.setLevel(Level.ALL);
		rootLog.addAppender(app);

		TabNDC ndc = TabNDC.getInstance();
		int ct = 0;
		ndc.pushFormat("fooFormat", "A Foo Test Format", "some/path");
		log.debug("A debug message");
		ct++;
		log.info("An info message");
		ct++;
		Study study = new Study("Test Study");
		study.setAcc("TEST:S:01");
		ndc.pushObject(study);
		log.warn("A warn message ");
		ct++;
		log.warn("Another warn message");
		ct++;
		ndc.popObject();
		log.trace("Debug message, one step back in the NDC");
		ct++;
		ndc.popTabDescriptor();

		ListTabAppender app1 = (ListTabAppender) log.getRootLogger().getAppender(APP_NAME);
		assertTrue("log4j is not returing the correct appender!", app1 == app);

		List<TabLoggingEventWrapper> tlog = app1.getOutput();
		for (TabLoggingEventWrapper tevent : tlog) {
			out.println("TAB CONTEXT: " + tevent.getTabDescriptors());
			out.println("OBJECTS: " + TabNDC.getObjectDescriptions(tevent.getObjects()));
			out.println(tevent.getFormattedMessage());
			out.println(StringUtils.repeat("-", 120) + "\n");
		}

		assertEquals("Wrong no of recorded messages!", ct, tlog.size());

		out.println(StringUtils.center(" /end:Testing TabAppender ", 120, "-") + "\n\n");
	}
}
