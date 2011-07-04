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

import org.isatools.isatab.configurator.schema.FieldType;
import org.isatools.isatab.configurator.schema.IsaTabConfigurationType;
import org.isatools.isatab.configurator.schema.OntologyEntryType;
import org.isatools.isatab.configurator.schema.ProtocolFieldType;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static java.lang.System.out;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@SuppressWarnings("static-access")
public class ISAConfigurationSetTest {
    private static ISAConfigurationSet cfgSet;

    @BeforeClass
    public static void load() {
        String baseDir = System.getProperty("basedir");
        String cfgPath = baseDir + "/target/test-classes/test-data/isatab/batch_test/configs/isa_configurator";
        ISAConfigurationSet.setConfigPath(cfgPath);
        cfgSet = new ISAConfigurationSet();
    }

    @Test
    public void testLoading() {
        IsaTabConfigurationType cfg = cfgSet.getConfig("[sample]", "");
        assertNotNull("Ouch! I couldn't get the expected configuration", cfg);
        out.println("\n\n_____ The tested config:");
        out.println(cfg);

        assertEquals("URP! Wrong no. of <field> elements", 2, cfg.sizeOfFieldArray());
    }


    @Test
    public void testGetConfigurationField() {
        IsaTabConfigurationType cfg = cfgSet.getConfig("transcription profiling", "DNA microarray");
        assertNotNull("Ouch! I couldn't get the expected configuration", cfg);

        FieldType f = cfgSet.getConfigurationField(cfg, "Sample Name");
        assertNotNull("Ouch! Sample field not found in the configuration via getConfigurationField()", f);
        assertEquals("Uhm... Bad sample field retrived via getConfigurationField()", "Sample Name", f.getHeader());
    }

    @Test
    public void testProtocolsBetween() {
        IsaTabConfigurationType cfg = cfgSet.getConfig("[sample]", "");
        assertNotNull("Ouch! I couldn't get the expected configuration!", cfg);

        FieldType fin = cfgSet.getConfigurationField(cfg, "Source Name");
        assertNotNull("Ouch! I couldn't get the Source Field", fin);

        FieldType fout = cfgSet.getConfigurationField(cfg, "Sample Name");
        assertNotNull("Ouch! I couldn't get the Source Field!", fout);

        List<ProtocolFieldType> protos = cfgSet.getProtocolsBetween(fin, fout);
        assertEquals("Argh! Wrong no of returned by getProtocolsBetween()!", 1, protos.size());
    }

    @Test
    public void testGetMeasurment() {
        IsaTabConfigurationType cfg = cfgSet.getConfig("transcription profiling", "DNA microarray");
        assertNotNull("Ouch! I couldn't get the expected configuration!", cfg);

        OntologyEntryType mes = cfg.getMeasurement();
        assertNotNull("Ach! couldn't get the measurement!", mes);
        assertEquals("Ach! Bad measurement!", "transcription profiling", mes.getTermLabel());
    }

    @Test
    public void testGetTechnology() {
        IsaTabConfigurationType cfg = cfgSet.getConfig("transcription profiling", "DNA microarray");
        assertNotNull("Ouch! I couldn't get the expected configuration!", cfg);

        OntologyEntryType mes = cfg.getTechnology();
        assertEquals("Ach! Bad technlogy!", "DNA microarray", mes.getTermLabel());
    }
}
