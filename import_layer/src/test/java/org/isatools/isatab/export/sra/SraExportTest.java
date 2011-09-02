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

package org.isatools.isatab.export.sra;

import org.apache.commons.lang.StringUtils;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.isatools.isatab_v1.ISATABLoader;
import org.isatools.isatab_v1.mapping.ISATABMapper;
import org.isatools.tablib.schema.FormatSetInstance;
import org.isatools.tablib.utils.BIIObjectStore;
import org.junit.Test;
import uk.ac.ebi.embl.era.sra.xml.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.out;
import static junit.framework.Assert.assertTrue;


public class SraExportTest {


    @Test
    public void testBasicExport() throws XmlException {
        Map<String, String> testData = getTestData();
        for (String directory : testData.keySet()) {

            out.println("\n\n" + StringUtils.center("Testing the SRA exporter with " + directory, 120, "-") + "\n");

            System.setProperty(
                    "bioinvindex.converters.sra.backlink",
                    "(This study is linked to the BII project, see http://www.ebi.ac.uk/bioinvindex/study.seam?studyId=${study-acc})");

            String baseDir = System.getProperty("basedir");

            System.out.println("basedir = " + baseDir);

            String filesPath = baseDir + "/target/test-classes/test-data/isatab/isatab_bii/" + directory;

            System.out.println("filesPath = " + filesPath);

            ISATABLoader loader = new ISATABLoader(filesPath);
            FormatSetInstance isatabInstance = null;
            try {
                isatabInstance = loader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }

            BIIObjectStore store = new BIIObjectStore();
            ISATABMapper mapper = new ISATABMapper(store, isatabInstance);
            mapper.map();

            String studyExportPath = baseDir + "/target/export/";

            SraExporter sraExporter = new SraExporter(store, filesPath, studyExportPath);
            sraExporter.export();

            System.out.println("exportPath = " + studyExportPath);

            System.out.println(studyExportPath + "/sra/" + testData.get(directory) + "/study.xml");

            assertTrue("Ouch! No SRA export directory created for " + directory + ": " + studyExportPath, new File(studyExportPath).exists());
            assertTrue("Ouch! No SRA study.xml created", new File(studyExportPath + "/sra/" + testData.get(directory) + "/study.xml").exists());

            // Validate the generated XML files
            try {
                XmlOptions xopts = new XmlOptions();
                xopts.setValidateOnSet();
                SubmissionType xsub = SubmissionType.Factory.parse(new File(studyExportPath + "/sra/" + testData.get(directory) + "/submission.xml"), xopts);
                StudyType xstudy = StudyType.Factory.parse(new File(studyExportPath + "/sra/" + testData.get(directory) + "/study.xml"), xopts);
                SAMPLESETDocument xsamples = SAMPLESETDocument.Factory.parse(new File(studyExportPath + "/sra/" + testData.get(directory) + "/sample_set.xml"), xopts);
                EXPERIMENTSETDocument xexps = EXPERIMENTSETDocument.Factory.parse(new File(studyExportPath + "/sra/" + testData.get(directory) + "/experiment_set.xml"), xopts);
                RUNSETDocument xruns = RUNSETDocument.Factory.parse(new File(studyExportPath + "/sra/" + testData.get(directory) + "/run_set.xml"), xopts);
            } catch (XmlException ex) {
                throw new XmlException("Argh! Validation of resulting SRA/XML failed!: " + ex.getMessage(), ex);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private Map<String, String> getTestData() {
        Map<String, String> testData = new HashMap<String, String>();

        testData.put("BII-S-4", "BII-S-4");

        return testData;

    }
}
