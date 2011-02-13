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

package org.isatools.isatab.export.sra;

import org.junit.Test;
import uk.ac.ebi.embl.era.sra.xml.AttributeType;
import uk.ac.ebi.embl.era.sra.xml.STUDYDocument;
import uk.ac.ebi.embl.era.sra.xml.StudyType;
import uk.ac.ebi.embl.era.sra.xml.StudyType.DESCRIPTOR;
import uk.ac.ebi.embl.era.sra.xml.StudyType.STUDYATTRIBUTES;

import static java.lang.System.out;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SraDocCreationTest {
	@Test
	public void testDocCreation() throws Exception {
		STUDYDocument doc = STUDYDocument.Factory.newInstance();
		{
			StudyType study = StudyType.Factory.newInstance();
			study.setAccession("TEST-S-0");
			{
				DESCRIPTOR descriptor = DESCRIPTOR.Factory.newInstance();
				descriptor.setSTUDYDESCRIPTION("A test study. Bla Bla Bla");
				study.setDESCRIPTOR(descriptor);

				AttributeType parsedAttr = AttributeType.Factory.parse(
						"<STUDY_ATTRIBUTE><TAG>Foo Property</TAG><VALUE>Foo Value</VALUE></STUDY_ATTRIBUTE>"
				);
				STUDYATTRIBUTES attrs = study.addNewSTUDYATTRIBUTES();
				attrs.addNewSTUDYATTRIBUTE();
				attrs.setSTUDYATTRIBUTEArray(0, parsedAttr);
			}
			doc.setSTUDY(study);
		}
		String xmls = doc.toString();
		out.println("\n===== STUDY XML:\n" + xmls + "\n\n");
		assertNotNull("The Study XML is null!", xmls);
		assertTrue("Cannot find the STUDY element in the XML!", xmls.contains("<STUDY"));
		assertTrue("Cannot find STUDY DESCRIPTOR element in the XML!", xmls.contains("<STUDY_DESCRIPTION"));
		assertTrue("Cannot find STUDY_ATTRIBUTE/TAG element in the XML!", xmls.contains("<STUDY_ATTRIBUTE"));
		assertTrue("Cannot find STUDY_ATTRIBUTE/TAG element in the XML!", xmls.contains("<TAG"));
		assertTrue("Cannot find STUDY_ATTRIBUTE/TAG element in the XML!", xmls.contains("Foo Value"));
	}
}
