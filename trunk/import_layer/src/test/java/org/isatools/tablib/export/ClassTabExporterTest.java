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

package org.isatools.tablib.export;

import org.isatools.tablib.mapping.testModels.News;
import org.isatools.tablib.mapping.testModels.NewsTabExporter;
import org.isatools.tablib.schema.Record;
import org.isatools.tablib.schema.SectionInstance;
import org.isatools.tablib.utils.BIIObjectStore;
import org.junit.Test;

import java.util.List;

import static java.lang.System.out;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ClassTabExporterTest {

	@Test
	public void testClassExporter() {
		out.println("\n\n __________________________ ClassTabExporter Test ____________________________ ");

		BIIObjectStore store = new BIIObjectStore();
		store.put(News.class, "01", new News("2008-01-01", "Happy New Year", "Bla Bla Bla"));
		store.put(News.class, "02", new News("2007-12-25", "Merry Xmas", "Test Test Test"));
		store.put(News.class, "03", new News("2006-03-23", "Test News", "This is the 10^9th test I write..."));

		NewsTabExporter exporter = new NewsTabExporter(store);
		SectionInstance sectionInstance = exporter.export();
		assertNotNull("URP! AbstractTabExporter returns null!", sectionInstance);
		List<Record> records = sectionInstance.getRecords();
		assertNotNull("Gosh! AbstractTabExporter returns null list of records", records);
		assertEquals("Ouch! Wrong # of mapped results returned!", 3, records.size());

		assertEquals("URP! Wrong retrieved value (#1)!", "Merry Xmas", sectionInstance.getString(1, "News Title"));
		assertEquals("URP! Wrong retrieved value (#2)!", "Bla Bla Bla", sectionInstance.getString(0, "News Abstract"));

		out.println("\n\nExporing completed, results:\n");
		for (int i = 0; i < records.size(); i++) {
			out.println(i + ". " + records.get(i));
		}

		out.println("\n\n __________________________ /end: ClassTabExporter Test ____________________________ \n\n\n");
	}

}
