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

package org.isatools.isatab.export.pride;

import org.junit.Test;
import uk.ac.ebi.pride.model.interfaces.core.Identification;
import uk.ac.ebi.pride.model.interfaces.core.Modification;
import uk.ac.ebi.pride.model.interfaces.core.Peptide;
import uk.ac.ebi.pride.model.interfaces.mzdata.CvParam;
import uk.ac.ebi.pride.model.interfaces.mzdata.UserParam;

import java.io.File;
import java.util.Collection;

import static java.lang.System.out;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class ProteinFilesTest {
	@Test
	public void testIdentificationsBasic() {
		out.println("\n\n\n____________________ Test of Pride Protein Files importer __________________________\n");

		String baseDir = System.getProperty("basedir");
		String filesPath = baseDir + "/target/test-classes/test-data/isatab/pride/";


		IdentificationCsvCollection idcoll = new IdentificationCsvCollection(
				new File(filesPath + "proteins.csv"),
				new File(filesPath + "peptides.csv"),
				new File(filesPath + "ptms.csv")
		);


		out.println("  Over-file collections created, results:");
		int count = 0, peptCount = 0, modCount = 0;
		boolean foundTestPept = false, isFirstMod = true;
		for (Identification ident : idcoll) {
			out.println(++count + ": " + ident);

			out.println("  Peptides:");
			Collection<Peptide> pepts = ident.getPeptides();
			peptCount += pepts.size();
			for (Peptide pept : pepts) {
				out.printf("  spectrum = %s, sequence = %s (%d:%d), cvparams = %s, user-params = %s\n",
						pept.getSpectrumRef(),
						pept.getSequence(),
						pept.getStart(),
						pept.getEnd(),
						pept.getPeptideAdditionalCvParams(),
						pept.getPeptideAdditionalUserParams()
				);

				if ("Q12349".equals(ident.getAccessionNumber()) && "WCFRTRGTRVCWVGEWECF".equals(pept.getSequence())) {
					foundTestPept = true;
					assertEquals("Auch! Wrong no. of parameters for test peptide", 2, pept.getPeptideAdditionalCvParams().size());
				}

				out.println("   PTMS:");
				Collection<Modification> mods = pept.getModifications();
				modCount += mods.size();
				for (Modification mod : mods) {
					out.printf("    acc = %s, db = %s (ver. %s), cvparams = %s, uparams = %s\n",
							mod.getAccession(), mod.getModDatabase(), mod.getModDatabaseVersion(),
							mod.getModificationAdditionalCvParams(), mod.getModificationAdditionalUserParams()
					);

					if (isFirstMod) {
						assertEquals("Auch! wrong no. of parameters on test PTM!", 2, mod.getModificationAdditionalCvParams().size());
						assertEquals("Auch! wrong no. of mono deltas on test PTM!", 2, mod.getMonoisotopicMassDeltas().size());
						assertEquals("Auch! wrong no. of average deltas on test PTM!", 2, mod.getAverageMassDeltas().size());
						isFirstMod = false;
					}
				}
			}
			out.println();

			if ("Q12362".equals(ident.getAccessionNumber())) {
				Collection<CvParam> cvparams = ident.getIdentificationAdditionalCvParams();
				assertEquals("Ops! Wrong number of saved CV params for identification #" + ident.getAccessionNumber(), 2, cvparams.size());
				Collection<UserParam> uparams = ident.getIdentificationAdditionalUserParams();
				assertEquals("Ops! Wrong number of saved user params for identification #" + ident.getAccessionNumber(), 1, uparams.size());
			}
		}

		assertEquals("Ouch! Wrong number of identifications read", 28, idcoll.size());
		assertEquals("Ouch! Wrong number of peptides read", 191, peptCount);
		assertEquals("Ouch! Wrong number of PTMs read", 108, modCount);
		assertTrue("Ops! An expected peptide not found!", foundTestPept);

		out.println("\n ____________________ /end: Test of Pride Protein Files importer __________________________\n\n");

	}
}
