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

package org.isatools.tablib.utils;

import au.com.bytecode.opencsv.CSVReader;
import org.apache.commons.lang.StringUtils;
import org.isatools.tablib.mapping.testModels.News;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static java.lang.System.out;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CsvIteratorTest {
	private class TestCsvIterator extends CsvIterator<News> {

		public TestCsvIterator(CSVReader csvReader) throws IOException {
			super(csvReader);
		}

		@Override
		protected News readNextObject() throws IOException {
			String line[] = lastReadLine();
			if (line == null) {
				return lastReadObject = null;
			}
			readNextLine();

			String date = line.length > 0 ? line[0] : "";
			String title = line.length > 1 ? line[1] : "";
			String abs = line.length > 2 ? line[2] : "";
			return new News(date, title, abs);
		}
	}

	private class TestHeadedCsvIterator extends HeadedCsvIterator<News> {

		public TestHeadedCsvIterator(CSVReader csvReader) throws IOException {
			super(csvReader);
		}

		@Override
		protected News readNextObject() throws IOException {
			String line[] = lastReadLine();
			if (line == null) {
				return lastReadObject = null;
			}
			readNextLine();

			String date = StringUtils.trimToEmpty(getValue(line, "date"));
			String title = StringUtils.trimToEmpty(getValue(line, "title"));
			String abs = StringUtils.trimToEmpty(getValue(line, "abs"));
			return new News(date, title, abs);
		}
	}


	@Test
	public void testBasics() throws IOException {
		out.println("\n\n\n  _________________ CsvIteratorTest, basics ______________________________ \n");

		InputStreamReader reader = new InputStreamReader(new BufferedInputStream(this.getClass().getResourceAsStream(
				"/test-data/tablib/utils/sample.csv"
		)));
		CSVReader csvReader = new CSVReader(reader, '\t', '"', 0);
		CsvIterator<News> iterator = new TestCsvIterator(csvReader);

		int count = 0;
		while (iterator.hasNext()) {
			News news = iterator.next();
			out.println(++count + ":" + news);
			if (count == 29) {
				assertEquals("Wrong date in randomly tested news", "04/05/06 00:00", news.getDate());
				assertEquals("Wrong title in randomly tested news", "Fabio Ceccherini", news.getTitle());
				assertEquals("Wrong abstract in randomly tested news", "Province of Siena", news.getAbs());
			}

		}

		assertEquals("Arg! Wrong no. of read items", 71, count);

		out.println("\n\n  _________________ /end: CsvIteratorTest, basics ______________________________ \n\n");

	}


	@Test
	public void test2Readers() throws IOException {
		out.println("\n\n\n  _________________ CsvIteratorTest, test2Readers ______________________________ \n");

		InputStream stream1 = this.getClass().getResourceAsStream("/test-data/tablib/utils/sample.csv");
		InputStream stream2 = this.getClass().getResourceAsStream("/test-data/tablib/utils/sample.csv");

		InputStreamReader reader1 = new InputStreamReader(new BufferedInputStream(stream1));
		InputStreamReader reader2 = new InputStreamReader(new BufferedInputStream(stream2));
		CSVReader csvReader1 = new CSVReader(reader1, '\t', '"', 0);
		CSVReader csvReader2 = new CSVReader(reader2, '\t', '"', 0);
		CsvIterator<News> iterator1 = new TestCsvIterator(csvReader1);
		CsvIterator<News> iterator2 = new TestCsvIterator(csvReader2);

		int count = 0;
		while (iterator1.hasNext()) {
			assertTrue("Oh no! expecting more elements from iterator 2", iterator2.hasNext());

			News news1 = iterator1.next();
			News news2 = iterator2.next();

			out.println(++count + " NEWS1: " + news1);
			out.println(count + " NEWS2: " + news2);
			out.println();

			assertTrue("Urp! I should get the same news items from parallel iterators", news1.equals(news2));
		}
		assertEquals("Arg! Wrong no. of read items", 71, count);

		out.println("\n\n  _________________ /end: CsvIteratorTest, test2Readers ______________________________ \n\n");
	}


	@Test
	public void testHeaded() throws IOException {
		out.println("\n\n\n  _________________ CsvIteratorTest, headers ______________________________ \n");

		InputStreamReader reader = new InputStreamReader(new BufferedInputStream(this.getClass().getResourceAsStream(
				"/test-data/tablib/utils/sample_w_header.csv"
		)));
		CSVReader csvReader = new CSVReader(reader, '\t', '"', 0);
		CsvIterator<News> iterator = new TestHeadedCsvIterator(csvReader);

		int count = 0;
		while (iterator.hasNext()) {
			News news = iterator.next();
			out.println(++count + ":" + news);
			if (count == 29) {
				assertEquals("Wrong date in randomly tested news", "04/05/06 00:00", news.getDate());
				assertEquals("Wrong title in randomly tested news", "Fabio Ceccherini", news.getTitle());
				assertEquals("Wrong abstract in randomly tested news", "Province of Siena", news.getAbs());
			}

		}

		assertEquals("Arg! Wrong no. of read items", 71, count);
		out.println("\n\n  _________________ /end: CsvIteratorTest, headers ______________________________ \n\n");

	}

}
