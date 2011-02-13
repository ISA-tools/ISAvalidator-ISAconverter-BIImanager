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

import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.List;

import static java.lang.System.out;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Basic testing of the Record class, used to store TAB contents
 * <p/>
 * May 30, 2007
 *
 * @author brandizi
 */
public class RecordTest {

	@Test
	public void testRecord() throws Exception {
		System.out.println("--- Testing Record class ---");
		InputStream input = new BufferedInputStream(this.getClass().getResourceAsStream("/test-data/tablib/sample_format_def.xml"));
		FormatSet schema = SchemaBuilder.loadFormatSetFromXML(input);
		assertNotNull("I have a schema", schema);

		System.out.println("--- Populating with records ---");
		Section section = (Section) schema.getDescendant("protocol", Section.class);
		List<Field> fields = section.getFields();
		int nflds = fields.size();

		FormatSetInstance formatSetInstance = new FormatSetInstance(schema);
		FormatInstance formatInstance = new FormatInstance(schema.getFormat("idf_sdrf"), formatSetInstance);
		formatSetInstance.addFormatInstance(formatInstance);
		SectionInstance sectionInstance = new SectionInstance(section, formatInstance);
		formatInstance.addSectionInstance(sectionInstance);


		for (int l = 0; l < 10; l++) {
			Record record = new Record(sectionInstance);
			for (int i = 0; i < nflds; i++) {
				// This will usually be set by the CSV parser
				if (l == 0) {
					Field field = fields.get(i);
					field.setIndex(i);
					sectionInstance.addField(field);
				}

				record.set(i, "Foo value " + i);
			}
			sectionInstance.addRecord(record);
			System.out.println("  Added Record #" + l);
		}


		System.out.println("--- Done, result: ---");
		List<Record> records = sectionInstance.getRecords();
		assertNotNull("I have some records inside the schema", records);

		for (Record record : records) {
			Section sect = record.getSection();
			assertEquals("rec.getSectionInstance().getSection() returns the right thing", sect, section);

			out.println("record Content: " + record);
		}
	}

}
