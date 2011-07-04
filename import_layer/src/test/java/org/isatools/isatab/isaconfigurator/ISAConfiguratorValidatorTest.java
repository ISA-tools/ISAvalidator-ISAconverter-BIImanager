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

package org.isatools.isatab.isaconfigurator;

import org.isatools.isatab.gui_invokers.GUIInvokerResult;
import org.isatools.isatab_v1.ISATABLoader;
import org.isatools.isatab_v1.mapping.ISATABReducedMapper;
import org.isatools.tablib.exceptions.TabValidationException;
import org.isatools.tablib.utils.BIIObjectStore;
import org.junit.Test;

import static java.lang.System.out;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ISAConfiguratorValidatorTest {

    @Test
    public void testLoading() throws Exception {
        String baseDir = System.getProperty("basedir");
        String cfgPath = baseDir + "/target/test-classes/test-data/isatab/batch_test/configs/isa_configurator";
        String isapath = baseDir + "/target/test-classes/test-data/isatab/isatab_bii/JCastrillo-BII-I-1";
        ISAConfigurationSet.setConfigPath(cfgPath);

        ISATABLoader loader = new ISATABLoader(isapath);
        ISATABReducedMapper mapper = new ISATABReducedMapper(new BIIObjectStore(), loader.load());
        ISAConfiguratorValidator validator = new ISAConfiguratorValidator(mapper.map());

        assertEquals("Gosh! The validator should return WARNING", GUIInvokerResult.WARNING, validator.validate());
    }

    @Test
    public void testLoadingNewAssayType() throws Exception {
        String baseDir = System.getProperty("basedir");
        String cfgPath = baseDir + "/target/test-classes/test-data/isatab/isa_configurator/isa_config_test";
        String isapath = baseDir + "/target/test-classes/test-data/isatab/isa_configurator/new_assay_submission";
        ISAConfigurationSet.setConfigPath(cfgPath);

        ISATABLoader loader = new ISATABLoader(isapath);
        ISATABReducedMapper mapper = new ISATABReducedMapper(new BIIObjectStore(), loader.load());
        ISAConfiguratorValidator validator = new ISAConfiguratorValidator(mapper.map());

        assertEquals("Gosh! The validator should return WARNING", GUIInvokerResult.WARNING, validator.validate());
    }

    @Test
    public void testLoadingRequiredFields() throws Exception {
        String baseDir = System.getProperty("basedir");
        String cfgPath = baseDir + "/target/test-classes/test-data/isatab/isa_configurator/isa_config_required_fields_test";
        String isapath = baseDir + "/target/test-classes/test-data/isatab/isa_configurator/new_assay_submission";
        ISAConfigurationSet.setConfigPath(cfgPath);

        ISATABLoader loader = new ISATABLoader(isapath);
        ISATABReducedMapper mapper = new ISATABReducedMapper(new BIIObjectStore(), loader.load());
        ISAConfiguratorValidator validator = new ISAConfiguratorValidator(mapper.map());

        try {
            validator.validate();
        } catch (TabValidationException ex) {
            out.println("We got the expected exception: \"" + ex.getMessage() + "\", congratulations!");
            return;
        }
        fail("Oh no! Error exception not generated!");
    }


}
