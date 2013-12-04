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
 * To request enhancements: �http://sourceforge.net/tracker/?group_id=215183&atid=1032652
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

package org.isatools.isatab_v1.validator;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.isatools.isatab.gui_invokers.GUIISATABValidator;
import org.isatools.isatab.gui_invokers.GUIInvokerResult;
import org.isatools.isatab.isaconfigurator.ISAConfigurationSet;
import org.isatools.tablib.utils.logging.TabLoggingEventWrapper;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static java.lang.System.out;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ValidatorTest {

	@Test
	public void testValidatorWithCastrillo() throws Exception {
		out.println("\n\n" + StringUtils.center("Testing the validator with Castrillo submission", 120, "-") + "\n");

		String baseDir = System.getProperty("basedir");
		String subDir = baseDir + "/target/test-classes/test-data/isatab/isatab_bii/JCastrillo-BII-I-1";

		GUIISATABValidator validator = new GUIISATABValidator();
		GUIInvokerResult result = validator.validate(subDir);


		out.println("Results:");
		out.println(result == GUIInvokerResult.ERROR ? "Validation failed!" : validator.report());

		out.println("Some Log Results:");
		List<TabLoggingEventWrapper> log = validator.getLog();

		boolean unusedSourceMsgFound = false, unusedFactorMsgFound = false, unusedProtoParamFound = false;
		int ih = log.size(), il = ih - 10;
		for (int i = ih - 1; i >= 0; i--) {
			TabLoggingEventWrapper event = log.get(i);
			String msg = StringUtils.trimToEmpty(event.getFormattedMessage());
			if (i > il) {
				out.println("LOG EVENT:" + msg);
			}

			if (msg.contains(
					"WARNING: the ontology source '_FOO1_' is declared in the investigation file but never used"
			)) {
				unusedSourceMsgFound = true;
			} else if (msg.contains(
					"the factor type '_foo unused factor_' is declared in the investigation file"
			)) {
				unusedFactorMsgFound = true;
			} else if (msg.contains(
					"the parameter 'sample volume' for the protocol 'metabolite extraction' is declared in the investigation file"
			)) {
				unusedProtoParamFound = true;
			}
		}
		assertEquals("Validation failed!", GUIInvokerResult.SUCCESS, result);

		assertTrue("Unused ontology source not found by the validator! :-(", unusedSourceMsgFound);
		assertTrue("Unused factor not found by the validator! :-(", unusedFactorMsgFound);
		assertTrue("Unused protocol parameter not found by the validator! :-(", unusedProtoParamFound);

		out.println("\n" + StringUtils.center("/end:Testing the validator with Castrillo submission", 120, "-") + "\n\n");
	}

	@Test
	public void testValidatorDupeFiles() throws Exception {
		out.println("\n\n" + StringUtils.center("Testing validator for the dupe files case", 120, "-") + "\n");

		String baseDir = System.getProperty("basedir");
		String subDir = baseDir + "/target/test-classes/test-data/isatab/isatab_bii/JCastrillo-BII-I-1_duped_files";

		GUIISATABValidator validator = new GUIISATABValidator();
		GUIInvokerResult result = validator.validate(subDir);

		assertEquals("Validation should have failed!", GUIInvokerResult.ERROR, result);


		out.println("\n" + StringUtils.center("/end:Testing validator for the dupe files case", 120, "-") + "\n\n");
	}

	// TODO: assertions
	// TODO: cardinality checking
	@Test
	public void testValidatorFieldOrder() throws Exception {
		out.println("\n\n" + StringUtils.center("Testing validator for the wrong field order case", 120, "-") + "\n");

		String baseDir = System.getProperty("basedir");
		String subDir = baseDir + "/target/test-classes/test-data/isatab/isatab_bii/JCastrillo-BII-I-1_wrong_field_order";

		GUIISATABValidator validator = new GUIISATABValidator();

		GUIInvokerResult result = validator.validate(subDir);
		assertEquals("Validation should have failed!", GUIInvokerResult.ERROR, result);

		out.println("\n" + StringUtils.center("/end:Testing validator for the wrong field order case", 120, "-") + "\n\n");
	}

    @Test
    public void testValidatorForUnreferencedSamples() {
        out.println("\n\n" + StringUtils.center("Testing validator for the unreference sample name case", 120, "-") + "\n");

        String baseDir = System.getProperty("basedir");
        String subDir = baseDir + "/target/test-classes/test-data/isatab/isatab_bii/MTBLS35";

        GUIISATABValidator validator = new GUIISATABValidator();

        GUIInvokerResult result = validator.validate(subDir);
        assertEquals("Validation should have failed!", GUIInvokerResult.ERROR, result);

        out.println("\n" + StringUtils.center("/end:Testing validator for the wrong field order case", 120, "-") + "\n\n");
    }

    @Test
    public void testValidatorForMissingFactorValues() {
        out.println("\n\n" + StringUtils.center("Testing validator for the unreference sample name case", 120, "-") + "\n");

        String baseDir = System.getProperty("basedir");
        String subDir = baseDir + "/target/test-classes/test-data/isatab/isatab_bii/JCastrillo-BII-I-1-Missing-Factors";

        GUIISATABValidator validator = new GUIISATABValidator();

        GUIInvokerResult result = validator.validate(subDir);
        assertEquals("Validation should have failed!", GUIInvokerResult.SUCCESS, result);

        System.out.println(validator.report());

        out.println("\n" + StringUtils.center("/end:Testing validator for the wrong field order case", 120, "-") + "\n\n");
    }

    @Test
    public void testValidatorForOntologyHeaders() {
        out.println("\n\n" + StringUtils.center("Testing validator when using ontology value to define headers", 120, "-") + "\n");

        ISAConfigurationSet.setConfigPath("/Users/eamonnmaguire/git/isarepo/ISAcreator/Configurations/isaconfig-default_v2013-02-13/");
        String baseDir = System.getProperty("basedir");
        String subDir = "/Users/eamonnmaguire/Dropbox/ISATAB-datasets/SCIDATA-sample2-ISA-tab_metadata";

        GUIISATABValidator validator = new GUIISATABValidator();

        GUIInvokerResult result = validator.validate(subDir);
        assertEquals("Validation should have failed!", GUIInvokerResult.SUCCESS, result);

        System.out.println(validator.report());

        out.println("\n" + StringUtils.center("/end:Testing validator for the wrong field order case", 120, "-") + "\n\n");
    }

}
