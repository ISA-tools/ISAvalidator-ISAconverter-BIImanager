/**

 The ISAconverter, ISAvalidator & BII Management Tool are components of the ISA software suite (http://www.isa-tools.org)

 Exhibit A
 The ISAconverter, ISAvalidator & BII Management Tool are licensed under the Mozilla Public License (MPL) version
 1.1/GPL version 2.0/LGPL version 2.1

 "The contents of this file are subject to the Mozilla Public License
 Version 1.1 (the "License"). You may not use this file except in compliance with the License.
 You may obtain copies of the Licenses at http://www.mozilla.org/MPL/MPL-1.1.html.

 Software distributed under the License is distributed on an "AS IS"
 basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 License for the specific language governing rights and limitations
 under the License.

 The Original Code is the ISAconverter, ISAvalidator & BII Management Tool.

 The Initial Developer of the Original Code is the ISA Team (Eamonn Maguire, eamonnmag@gmail.com;
 Philippe Rocca-Serra, proccaserra@gmail.com; Susanna-Assunta Sansone, sa.sanson@gmail.com;
 http://www.isa-tools.org). All portions of the code written by the ISA Team are Copyright (c)
 2007-2011 ISA Team. All Rights Reserved.

 Contributor(s):
 Rocca-Serra P, Brandizi M, Maguire E, Sklyar N, Taylor C, Begley K, Field D,
 Harris S, Hide W, Hofmann O, Neumann S, Sterk P, Tong W, Sansone SA. ISA software suite:
 supporting standards-compliant experimental annotation and enabling curation at the community level.
 Bioinformatics 2010;26(18):2354-6.

 Alternatively, the contents of this file may be used under the terms of either the GNU General
 Public License Version 2 or later (the "GPL") - http://www.gnu.org/licenses/gpl-2.0.html, or
 the GNU Lesser General Public License Version 2.1 or later (the "LGPL") -
 http://www.gnu.org/licenses/lgpl-2.1.html, in which case the provisions of the GPL
 or the LGPL are applicable instead of those above. If you wish to allow use of your version
 of this file only under the terms of either the GPL or the LGPL, and not to allow others to
 use your version of this file under the terms of the MPL, indicate your decision by deleting
 the provisions above and replace them with the notice and other provisions required by the
 GPL or the LGPL. If you do not delete the provisions above, a recipient may use your version
 of this file under the terms of any one of the MPL, the GPL or the LGPL.

 Sponsors:
 The ISA Team and the ISA software suite have been funded by the EU Carcinogenomics project
 (http://www.carcinogenomics.eu), the UK BBSRC (http://www.bbsrc.ac.uk), the UK NERC-NEBC
 (http://nebc.nerc.ac.uk) and in part by the EU NuGO consortium (http://www.nugo.org/everyone).

 */

package org.isatools.isatab.mapping;

import au.com.bytecode.opencsv.CSVReader;
import org.apache.commons.lang.StringUtils;
import org.isatools.isatab.commandline.AbstractImportLayerShellCommand;
import org.isatools.isatab.configurator.schema.IsaTabAssayType;
import org.isatools.isatab.configurator.schema.IsaTabConfigurationType;
import org.isatools.isatab.configurator.schema.OntologyEntryType;
import org.isatools.isatab.isaconfigurator.ISAConfigurationSet;
import org.isatools.tablib.exceptions.TabIOException;
import uk.ac.ebi.bioinvindex.model.processing.Assay;
import uk.ac.ebi.bioinvindex.model.term.AssayTechnology;
import uk.ac.ebi.bioinvindex.model.term.Measurement;
import uk.ac.ebi.bioinvindex.model.xref.ReferenceSource;
import uk.ac.ebi.bioinvindex.utils.datasourceload.DataLocationManager;
import uk.ac.ebi.bioinvindex.utils.datasourceload.DataSourceConfigFields;
import uk.ac.ebi.bioinvindex.utils.i18n;

import java.io.*;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;

import static java.lang.System.out;


/**
 * <p>A small helper that reads the configuration file used to map the Measurement/Technology pairs to assay format in
 * the ISATAB format description. An assay format is essentially the description of the acceptable fields in the assay,
 * see the code documentation for details.</p>
 * <p>TODO: We will get rid of the file {@link #ASSAY_TYPE_ENTRIES_FILE_NAME}. All the information will come from
 * the {@link ISAConfigurationSet}.</p>
 * The list in the specs:
 * 4.3.1 Generic
 * 4.3.2 DNA microarray hybridization
 * Alias: DNA Microarray
 * 4.3.3 Gel electrophoresis
 * 4.3.4 Mass Spectrometry
 * 4.3.5 Nuclear Magnetic Resonance spectroscopy (NMR)
 * Alias: NMR Spectrometry
 * 4.3.6 High throughput sequencing
 * under dev, needs to be ignored
 *
 * @author brandizi
 */
public class AssayTypeEntries {
    /**
     * A file with this name is read from the path provided via {@link AbstractImportLayerShellCommand#getConfigPath()}.
     * If something is found there, it is used, otherwise, the file will be read via get
     * AssayTypeEntries.class.getResourceAsStream.
     */
    public static final String ASSAY_TYPE_ENTRIES_FILE_NAME = "isatab_dispatch_mappings.csv";
    private static int
            COL_MEASURMENT_LABEL = 0, COL_MEASURMENT_ACC = 1, COL_TECHNOLOGY_LABEL = 2, COL_TECHNOLOGY_ACC = 3,
            COL_ASSAY_FORMAT_ID = 5, COL_DISPATCH_ID = 6;

    private static String[][] entries;
    private static ReferenceSource obiSource;


    private static ISAConfigurationSet isaConfiguratorCfg = new ISAConfigurationSet();


    private static String[][] getEntries() {
        if (entries != null) {
            return entries;
        }

        try {
            String mappingFilePath = AbstractImportLayerShellCommand.getConfigPath() + ASSAY_TYPE_ENTRIES_FILE_NAME;
            File mappingFile = new File(mappingFilePath);

            InputStream instream = mappingFile.canRead()
                    ? new FileInputStream(mappingFile)
                    : AssayTypeEntries.class.getResourceAsStream("/" + ASSAY_TYPE_ENTRIES_FILE_NAME);

            CSVReader csvReader = new CSVReader(new InputStreamReader(new BufferedInputStream(instream)), '\t', '"');

            List<String[]> entriesL = new LinkedList<String[]>();
            boolean isFirst = true;
            for (String[] line = null; (line = csvReader.readNext()) != null; ) {
                // Skip the header
                if (isFirst) {
                    isFirst = false;
                    continue;
                }
                entriesL.add(line);
            }

            entries = entriesL.toArray(new String[][]{});
            return entries;
        } catch (IOException e) {
            throw new TabIOException(i18n.msg("assaytype_entries_no_load", e.getMessage()), e);
        }
    }


    public static String getAssayFormatIdFromLabels(String measurmentTypeLabel, String technologyTypeLabel) {
        measurmentTypeLabel = convertMeasurementTypeLabel(measurmentTypeLabel);
        technologyTypeLabel = convertTechnologyLabel(technologyTypeLabel);

        // First try with the stuff in the ISA Configuration's Configurator
        //
        IsaTabConfigurationType cfg = isaConfiguratorCfg.getConfig(measurmentTypeLabel, technologyTypeLabel);
        if (cfg != null) {
            IsaTabAssayType.Enum atype = cfg.getIsatabAssayType();
            if (atype != null) {
                return atype.toString();
            }
        }

        for (String[] entry : getEntries()) {
            if (StringUtils.trimToEmpty(entry[COL_MEASURMENT_LABEL]).equalsIgnoreCase(measurmentTypeLabel)
                    && StringUtils.trimToEmpty(entry[COL_TECHNOLOGY_LABEL]).equalsIgnoreCase(technologyTypeLabel)) {
                return StringUtils.trimToNull(entry[COL_ASSAY_FORMAT_ID]);
            }
        }
        return "generic_assay";
    }

    public static String getAssayFormatIdFromLabels(Assay assay) {
        return getAssayFormatIdFromLabels(assay.getMeasurement().getName(), assay.getTechnologyName());
    }

    public static String getAssayFormatIdFromLabels(AssayGroup assay) {
        return getAssayFormatIdFromLabels(assay.getMeasurement().getName(), assay.getTechnologyName());
    }


    /**
     * @param measurmentTypeLabel
     * @param technologyTypeLabel
     * @return
     */
    public static String getDispatchTargetIdFromLabels(String measurmentTypeLabel, String technologyTypeLabel) {
        measurmentTypeLabel = convertMeasurementTypeLabel(measurmentTypeLabel);
        technologyTypeLabel = convertTechnologyLabel(technologyTypeLabel);

        // First try with the stuff in the ISA Configuration's Configurator
        //
        IsaTabConfigurationType cfg = isaConfiguratorCfg.getConfig(measurmentTypeLabel, technologyTypeLabel);

        if (cfg != null) {
            String conversionTarget = StringUtils.trimToNull(cfg.getIsatabConversionTarget());
            if (conversionTarget != null) {
                return conversionTarget;
            }
        }


        // TODO: we want to eventually remove the CSV file
        //

        for (String[] entry : getEntries()) {
            if (StringUtils.trimToEmpty(entry[COL_MEASURMENT_LABEL]).equalsIgnoreCase(measurmentTypeLabel)
                    && StringUtils.trimToEmpty(entry[COL_TECHNOLOGY_LABEL]).equalsIgnoreCase(technologyTypeLabel)) {
                String dispatchTarget = StringUtils.trimToNull(entry[COL_DISPATCH_ID]);
                if (StringUtils.containsIgnoreCase(dispatchTarget, "New European Nucleotide Archive database")) {
                    dispatchTarget = "sra";
                } else if (StringUtils.containsIgnoreCase(dispatchTarget, "ArrayExpress")) {
                    dispatchTarget = "magetab";
                } else if (StringUtils.containsIgnoreCase(dispatchTarget, "Pride")) {
                    dispatchTarget = "prideml";
                } else if (StringUtils.containsIgnoreCase(dispatchTarget, "BII metabolomic")) {
                    dispatchTarget = "meda";
                } else {
                    dispatchTarget = "generic";
                }

                return dispatchTarget;
            }
        }
        return null;
    }

    public static String getDispatchTargetIdFromLabels(Assay assay) {
        return getDispatchTargetIdFromLabels(assay.getMeasurement().getName(), assay.getTechnologyName());
    }

    public static String getDispatchTargetIdFromLabels(AssayGroup assay) {
        return getDispatchTargetIdFromLabels(assay.getMeasurement().getName(), assay.getTechnologyName());
    }


    public static Measurement getMeasurementTypeFromLabel(String label) {
        label = convertMeasurementTypeLabel(label);

        // First try with what you have in the ISA Configurator Information
        //
        OntologyEntryType ctype = isaConfiguratorCfg.getMeasurementFromLabel(label);

        if (ctype != null) {
            label = ctype.getTermLabel();
            String acc = StringUtils.trimToNull(ctype.getTermAccession());
            if (acc == null) {
                acc = "NULL-ACCESSION";
            }

            ReferenceSource osrc;
            String sourceAcc = StringUtils.trimToNull(ctype.getSourceAbbreviation());

            if (sourceAcc == null || "obi".equalsIgnoreCase(sourceAcc)) {
                osrc = getOBISource();
            } else {
                osrc = new ReferenceSource(sourceAcc, sourceAcc);
                osrc.setDescription(ctype.getSourceTitle());
                osrc.setUrl(ctype.getSourceTitle());
            }
            return new Measurement(acc, label, osrc);
        }

        for (String[] entry : getEntries()) {
            String epLabel = StringUtils.trimToEmpty(entry[COL_MEASURMENT_LABEL]);
            if (!label.equalsIgnoreCase(epLabel)) {
                continue;
            }

            String acc = StringUtils.trimToEmpty(entry[COL_MEASURMENT_ACC]);
            if (acc.length() == 0
                    || StringUtils.containsIgnoreCase(acc, "submitted to OBI")
                    || StringUtils.containsIgnoreCase(acc, "old terms"))
            // There isn't much else I can do...
            {
                acc = "NULL-ACCESSION";
            }

            return new Measurement(acc, label, getOBISource());
        }
        return null;
    }

    public static AssayTechnology getAssayTechnologyFromLabel(String label) {
        label = convertTechnologyLabel(label);

        // First try with what you have in the ISA Configurator Information
        //
        OntologyEntryType ctype = isaConfiguratorCfg.getTechnologyFromLabel(label);

        if (ctype != null) {
            label = ctype.getTermLabel();
            String acc = StringUtils.trimToNull(ctype.getTermAccession());
            if (acc == null) {
                acc = "NULL-ACCESSION";
            }

            ReferenceSource osrc;
            String sourceAcc = StringUtils.trimToNull(ctype.getSourceAbbreviation());

            if (sourceAcc == null || "obi".equalsIgnoreCase(sourceAcc)) {
                osrc = getOBISource();
            } else {
                osrc = new ReferenceSource(sourceAcc, sourceAcc);
                osrc.setDescription(ctype.getSourceTitle());
                osrc.setUrl(ctype.getSourceTitle());
            }
            return new AssayTechnology(acc, label, osrc);
        }

        for (String[] entry : getEntries()) {
            String techLabel = StringUtils.trimToEmpty(entry[COL_TECHNOLOGY_LABEL]);
            if (!label.equalsIgnoreCase(techLabel)) {
                continue;
            }

            String acc = StringUtils.trimToEmpty(entry[COL_TECHNOLOGY_ACC]);
            if (acc.length() == 0
                    || StringUtils.containsIgnoreCase(acc, "submitted to OBI")
                    || StringUtils.containsIgnoreCase(acc, "old terms"))
            // There isn't much else I can do...
            {
                acc = "NULL-ACCESSION";
            }

            return new AssayTechnology(acc, label, getOBISource());
        }

        return null;
    }


    /**
     * Gets the OBI source, that we use here for all terms
     */
    public static ReferenceSource getOBISource() {
        if (obiSource != null) {
            return obiSource;
        }

        obiSource = new ReferenceSource("OBI");
        obiSource.setAcc("OBI");
        obiSource.setDescription("Ontology for Biological Investigation");
        obiSource.setUrl("http://obi-ontology.org");

        return obiSource;
    }


    /**
     * Converts the label into something that is compatible with the mappings defined into {@value #ASSAY_TYPE_ENTRIES_FILE_NAME},
     * for instance, "Metabolite Quantitation", defined in old test cases, is converted to "metabolite profiling".
     */
    private static String convertMeasurementTypeLabel(String label) {
        label = StringUtils.trimToEmpty(label);
        if ("Metabolite Quantitation".equalsIgnoreCase(label)) {
            return "metabolite profiling";
        }
        return label;
    }

    /**
     * Converts the label into something that is compatible with the mappings defined into {@value #ASSAY_TYPE_ENTRIES_FILE_NAME},
     * for instance, "Metabolite Quantitation", defined in old test cases, is converted to "metabolite profiling".
     */
    private static String convertTechnologyLabel(String label) {
        label = StringUtils.trimToEmpty(label);
        if ("high-throughput sequencing".equalsIgnoreCase(label)) {
            return "nucleotide sequencing";
        }

        return label;
    }


    /**
     * A quick converter that outputs destinations in a format compatible with {@link DataLocationManager}.
     */
    private static void printDataLocations() {
        int i = 0;

        for (String[] entry : getEntries()) {
            String ep = entry[COL_MEASURMENT_LABEL], tech = entry[COL_TECHNOLOGY_LABEL];
            String dispatchTargetId = getDispatchTargetIdFromLabels(ep, tech);

            if ("meda".equals(dispatchTargetId)) {
                dispatchTargetId = "meda";
            } else if ("magetab".equals(dispatchTargetId)) {
                dispatchTargetId = "microarray";
            } else if ("prideml".equals(dispatchTargetId)) {
                dispatchTargetId = "proteomics";
            }


            if ("microarray".equals(dispatchTargetId)) {
                out.print(MessageFormat.format(
                        "<datasource measurement_type=\"{0}\" technology_type=\"{1}\"\n" +
                                "  name=\"ArrayExpress\"\n" +
                                "  url=\"http://www.ebi.ac.uk/microarray-as/ae/\"\n" +
                                "  description=\"ArrayExpress\"\n" +
                                ">\n" +
                                "  <raw_data\n" +
                                "	  filesystem_path=\"\"\n" +
                                "	  web_url=\"ftp://ftp.ebi.ac.uk/pub/databases/microarray/data/experiment/'${type_goes_here}'/{2}/{2}.raw.zip\"\n" +
                                "  />\n" +
                                "  <processed_data\n" +
                                "	  filesystem_path=\"\"\n" +
                                "	  web_url=\"ftp://ftp.ebi.ac.uk/pub/databases/microarray/data/experiment/'${type_goes_here}'/{2}/{2}.processed.zip\"\n" +
                                "	/>\n" +
                                "  <db_entry web_url=\"http://www.ebi.ac.uk/microarray-as/ae/browse.html?keywords={2}\"/>\n" +
                                "</datasource>\n\n",
                        ep, tech, DataSourceConfigFields.ACCESSION_PLACEHOLDER.getName()
                ));
            } else if ("proteomics".equals(dispatchTargetId)) {
                out.print(MessageFormat.format(
                        "<datasource measurement_type=\"{0}\" technology_type=\"{1}\"\n" +
                                "  name=\"PRIDE\"\n" +
                                "  url=\"http://www.ebi.ac.uk/pride\"\n" +
                                "  description=\"PRIDE\"\n" +
                                ">\n" +
                                "  <processed_data\n" +
                                "	  filesystem_path=\"\"\n" +
                                "	  web_url=\"ftp://ftp.ebi.ac.uk/pub/databases/pride/PRIDE_Exp_Complete_Ac_{2}.xml.gz\"\n" +
                                "	/>\n" +
                                "  <db_entry web_url=\"http://www.ebi.ac.uk/pride/experimentLink.do?experimentAccessionNumber={2}\"/>\n" +
                                "</datasource>\n\n",
                        ep, tech, DataSourceConfigFields.ACCESSION_PLACEHOLDER.getName()
                ));
            } else if ("sra".equals(dispatchTargetId)) {
                out.print(MessageFormat.format(
                        "<datasource measurement_type=\"transcription profiling\" technology_type=\"nucleotide sequencing\" \n" +
                                "  name=\"ENA\"\n" +
                                ">\n" +
                                "  <raw_data web_url=\"ftp://ftp.era-xml.ebi.ac.uk/ERA000/'${study-acc}'\" />\n" +
                                "  <db_entry web_url=\"ftp://ftp.era-xml.ebi.ac.uk/ERA000/'${study-acc}'\"/>\n" +
                                "</datasource>\n\n",
                        ep, tech, DataSourceConfigFields.ACCESSION_PLACEHOLDER.getName()
                ));
            } else if ("sra".equals(dispatchTargetId)) {
                out.print(MessageFormat.format(
                        "<datasource measurement_type=\"{0}\" technology_type=\"{1}\"\n" +
                                "  name=\"EMBL-Bank\"\n" +
                                "  url=\"http://www.ebi.ac.uk/embl\"\n" +
                                "  description=\"EMBL Nucleotide Sequence Database\"\n" +
                                ">\n" +
                                "  <db_entry web_url=\"http://www.ebi.ac.uk/cgi-bin/dbfetch?db=EMBL&amp;id={2}\"/>\n" +
                                "</datasource>\n\n",
                        ep, tech, DataSourceConfigFields.ACCESSION_PLACEHOLDER.getName()
                ));
            } else {
                out.print(MessageFormat.format(
                        "<datasource measurement_type=\"{0}\" technology_type=\"{1}\">\n" +
                                "  <raw_data\n" +
                                "    filesystem_path=\"/ebi/ftp/pub/databases/bii/{2}_repo/study_{3}/raw_data\"\n" +
                                "    web_url=\"ftp://ftp.ebi.ac.uk/pub/databases/bii/{2}_repo/study_{3}/raw_data\"\n" +
                                "  />\n" +
                                "  <processed_data\n" +
                                "    filesystem_path=\"/ebi/ftp/pub/databases/bii/{2}_repo/study_{3}/processed_data\"\n" +
                                "    web_url=\"ftp://ftp.ebi.ac.uk/pub/databases/bii/{2}_repo/study_{3}/processed_data\"\n" +
                                "  />\n" +
                                "</datasource>\n\n",
                        ep, tech, dispatchTargetId, DataSourceConfigFields.ACCESSION_PLACEHOLDER.getName()
                ));
            }
        }
    }
}
