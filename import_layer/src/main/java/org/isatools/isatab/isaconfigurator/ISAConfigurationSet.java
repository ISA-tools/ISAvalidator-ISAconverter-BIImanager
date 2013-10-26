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

package org.isatools.isatab.isaconfigurator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.isatools.isatab.commandline.AbstractImportLayerShellCommand;
import org.isatools.isatab.configurator.schema.*;
import org.isatools.isatab.mapping.AssayGroup;
import org.isatools.tablib.exceptions.TabIOException;
import org.isatools.tablib.exceptions.TabInternalErrorException;
import org.isatools.tablib.exceptions.TabMissingResourceException;
import uk.ac.ebi.bioinvindex.utils.i18n;

import java.io.*;
import java.util.*;

/**
 * Manages a set of files that describe ISAConfigurator's configuration.
 *
 * @author brandizi
 * see XSD file that defines the latter.
 */
public class ISAConfigurationSet {
    public final static String
            NS_CFG = "http://www.ebi.ac.uk/bii/isatab_configuration#";
    // "http://www.w3.org/2005/02/xpath-functions/#";

    private static String configPath = AbstractImportLayerShellCommand.getConfigPath() + "/isa_configurator";

    private Map<String, IsaTabConfigFileType> _isaConfigFiles;
    private Map<String, IsaTabConfigurationType> _isaConfigs;

    protected static final Logger log = Logger.getLogger(ISAConfigurationSet.class);

    /**
     * Default is  {@link AbstractImportLayerShellCommand#getConfigPath()}
     */
    public static void setConfigPath(String configPath) {
        ISAConfigurationSet.configPath = configPath;
    }

    /**
     * Reads a cofig file and adds up it to the config set
     */
    private void addConfigFile(String path, InputStream input) {
        try {
            IsatabConfigFileDocument configFileDoc = IsatabConfigFileDocument.Factory.parse(input);
            IsaTabConfigFileType configFile = configFileDoc.getIsatabConfigFile();

            for (int i = 0; i < configFile.sizeOfIsatabConfigurationArray(); i++) {
                IsaTabConfigurationType cfg = configFile.getIsatabConfigurationArray(i);
                for (XmlObject xfieldObj : getAllConfigurationFields(cfg)) {
                    if (xfieldObj instanceof FieldType) {
                        FieldType xfield = (FieldType) xfieldObj;
                        String xheader = xfield.getHeader();
                        if (xheader.contains("[")) {
                            int iFirstBracket = xheader.indexOf('[');
                            if (iFirstBracket > 0) {
                                String newHeader = xheader.substring(0, iFirstBracket) + " [" + xheader.substring(iFirstBracket + 1);
                                xfield.setHeader(newHeader);
                            }
                        }
                    }
                }
            }

            _isaConfigFiles.put(path, configFile);
        } catch (XmlException e) {
            throw new TabInternalErrorException(i18n.msg("isaconfig_loading_error", path, e.getMessage()), e);
        } catch (IOException e) {
            throw new TabIOException(i18n.msg("isaconfig_loading_error", path, e.getMessage()), e);
        }
    }

    /**
     * Reads a cofig file and adds up it to the config set
     */
    private void addConfigFile(File file) {
        try {
            addConfigFile(file.getAbsolutePath(), new BufferedInputStream(new FileInputStream(file)));
        } catch (FileNotFoundException e) {
            throw new TabMissingResourceException(i18n.msg("isaconfig_missing_file", file.getAbsolutePath()));
        }
    }

    /**
     * Reads a cofig file and adds up it to the config set
     */
    private void addConfigFile(String path) {
        addConfigFile(new File(path));
    }

    /**
     * Add all the config files that it can find in the path, basically scan all the XML files (recursively)
     */
    @SuppressWarnings("unchecked")
    private void addFromConfigDir(String path) {
        File cfgDir = new File(path);
        if (cfgDir == null || !cfgDir.isDirectory() || !cfgDir.canRead()) {
            log.warn(
                    "Cannot read ISAConfiguration information from the configuration directory: '" + path + "', "
                            + "no ISAConfigurator validation will be performed."
            );
            return;
        }
        for (File file : (Collection<File>) FileUtils.listFiles(new File(path), new String[]{"xml"}, true)) {
            addConfigFile(file);
        }
    }

    /**
     * Gets a map of name=>Config
     */
    private Map<String, IsaTabConfigFileType> getFileConfigs() {
        if (_isaConfigFiles != null) {
            return _isaConfigFiles;
        }
        _isaConfigFiles = new HashMap<String, IsaTabConfigFileType>();
        addFromConfigDir(configPath);
        return _isaConfigFiles;
    }

    /**
     * All the configurations, every configuration is keyed by measurement type and technology (it uses "//" as separator
     * in the string).
     */
    private Map<String, IsaTabConfigurationType> getIsaTabConfigs() {
        if (_isaConfigs != null) {
            return _isaConfigs;
        }

        Map<String, IsaTabConfigFileType> isaConfigFiles = getFileConfigs();
        _isaConfigs = new HashMap<String, IsaTabConfigurationType>();

        for (IsaTabConfigFileType cfile : isaConfigFiles.values()) {
            for (int i = 0; i < cfile.sizeOfIsatabConfigurationArray(); i++) {
                IsaTabConfigurationType cfg = cfile.getIsatabConfigurationArray(i);
                String ep = StringUtils.trimToEmpty(cfg.getMeasurement().getTermLabel()).toLowerCase(),
                        tech = StringUtils.trimToEmpty(cfg.getTechnology().getTermLabel()).toLowerCase();
                _isaConfigs.put(ep + "//" + tech, cfg);
            }
        }
        return _isaConfigs;
    }

    /**
     * Gets a specific configuration for a given measurement and technology
     */
    public IsaTabConfigurationType getConfig(String measurementType, String technologyType) {
        measurementType = StringUtils.trimToEmpty(measurementType).toLowerCase();
        technologyType = StringUtils.trimToEmpty(technologyType).toLowerCase();

        IsaTabConfigurationType config = getIsaTabConfigs().get(measurementType + "//" + technologyType);

        if (config == null) {
            config = getIsaTabConfigs().get("*//*");
        }

        return config;
    }

    /**
     * A wrapper of {@link #getConfig(String, String)}.
     */
    public IsaTabConfigurationType getConfig(final AssayGroup ag) {
        return getConfig(ag.getMeasurement().getName(), ag.getTechnologyName());
    }

    /**
     * All the configurations available, from all the loaded files
     */
    public Collection<IsaTabConfigurationType> getAllConfigs() {
        return getIsaTabConfigs().values();
    }

    /**
     * @return the ontolgy entry details that correspond to a certain label, by searching them in the block
     *         &lt;isatab-configuration&gt; / &lt;measurement&gt;
     */
    public OntologyEntryType getMeasurementFromLabel(String measurement) {
        if (measurement == null) {
            return null;
        }
        measurement = measurement.trim().toLowerCase();
        for (IsaTabConfigurationType cfg : getAllConfigs()) {
            OntologyEntryType cmeasure = cfg.getMeasurement();
            if (measurement.equalsIgnoreCase(cmeasure.getTermLabel())) {
                return cmeasure;
            }
        }
        return null;
    }

    /**
     * @return the ontolgy entry details that correspond to a certain label, by searching them in the block
     *         &lt;isatab-configuration&gt; / &lt;technology&gt;
     */
    public OntologyEntryType getTechnologyFromLabel(String tech) {
        if (tech == null) {
            return null;
        }
        tech = tech.trim().toLowerCase();
        for (IsaTabConfigurationType cfg : getAllConfigs()) {
            OntologyEntryType ctech = cfg.getTechnology();
            if (tech.equalsIgnoreCase(ctech.getTermLabel())) {
                return ctech;
            }
        }
        return null;
    }

    /**
     * It's empty when it has no configuration loaded
     */
    public boolean isEmpty() {
        return getIsaTabConfigs().isEmpty();
    }


    /**
     * Returns all the child elements of cfg.
     * <p/>
     * TODO: Never used/tested!
     */
    public static XmlObject[] getAllConfigurationFields(IsaTabConfigurationType cfg) {
        return cfg.selectPath("./*");
    }

    /**
     * Tells the field (field element) that has the parameter header as attribute (case-insensitive search).
     */
    public static FieldType getConfigurationField(IsaTabConfigurationType cfg, String header) {
        String lheader = header.toLowerCase();
        XmlObject[] results = cfg.selectPath(
                "declare namespace cfg='" + NS_CFG + "';"
                        + "./cfg:field[lower-case(@header)='" + lheader + "']"
        );
        if (results.length == 0) {
            return null;
        }
        if (results.length > 1) {
            throw new TabInternalErrorException(i18n.msg("isaconfig_config_syntax_error_too_many_fields", header));
        }
        return (FieldType) results[0];
    }

    /**
     * Returns the unit field definition that is next to the parameter field. null in case there isn't
     * anything like that.
     */
    public static UnitFieldType getUnitField(FieldType field) {
        XmlObject[] results = field.selectPath("./following-sibling::*");
        // First element seems to be $this, then there is what we want
        if (results.length < 2) {
            return null;
        }
        XmlObject result = results[1];
        return result instanceof UnitFieldType ? (UnitFieldType) result : null;
    }

    /**
     * All the protocol fields between two field elements, i.e.: an input field and an output field.
     */
    public static List<ProtocolFieldType> getProtocolsBetween(FieldType fieldIn, FieldType fieldOut) {
        return getProtocolsBetween(fieldIn, fieldOut, null);
    }

    /**
     * Filters the result given by {@link #getProtocolsBetween(FieldType, FieldType)} and selects those protocol
     * matching the parameter type in the protocol-type attribute.
     */
    public static List<ProtocolFieldType> getProtocolsBetween(FieldType fieldIn, FieldType fieldOut, String type) {
        List<ProtocolFieldType> protos = new ArrayList<ProtocolFieldType>();

        for (XmlObject node : fieldIn.selectPath(
                "declare namespace cfg='" + NS_CFG + "';"
                        + "./following-sibling::*"
        )) {
            if (node == fieldOut) {
                break;
            }
            if (node instanceof ProtocolFieldType) {
                ProtocolFieldType proto = (ProtocolFieldType) node;
                if (type == null || type.equalsIgnoreCase(proto.getProtocolType())) {
                    protos.add(proto);
                }
            }
        }
        return protos;
    }

    /**
     * Gets all the ontology source abbreviations that are mentioned in a reccomended-ontologies element under the
     * parameter field.
     */
    public static Set<String> getOntologySourceAbbreviations(FieldType field) {
        Set<String> result = new HashSet<String>();

        RecommendedOntologiesType recommendedOntos = field.getRecommendedOntologies();
        if (recommendedOntos == null) {
            return result;
        }

        for (OntologyType onto : recommendedOntos.getOntologyArray()) {
            String abbr = StringUtils.trimToNull(onto.getAbbreviation());
            if (abbr != null) {
                result.add(abbr);
            }
        }
        return result;
    }

    /**
     * Gets all the ontology roots that are specified in the branch elements of recommended ontologies
     * elements for the field.
     *
     * @return a map of sourceSymbol =&gt; { termAccession }
     */
    public static Map<String, Set<String>> getBranchIds(FieldType field) {
        Map<String, Set<String>> result = new HashMap<String, Set<String>>();

        RecommendedOntologiesType recommendedOntos = field.getRecommendedOntologies();
        if (recommendedOntos == null) {
            return result;
        }

        for (OntologyType onto : recommendedOntos.getOntologyArray()) {
            String osrc = StringUtils.trimToNull(onto.getAbbreviation());
            if (osrc == null) {
                continue;
            }

            Set<String> branches = result.get(osrc);
            if (branches == null) {
                branches = new HashSet<String>();
                result.put(osrc, branches);
            }

            for (BranchType branchObj : onto.getBranchArray()) {
                String termId = StringUtils.trimToNull(branchObj.getId());
                if (termId == null) {
                    continue;
                }

                branches.add(termId);
            }
        }
        return result;
    }

    /**
     * Saves a configuration, after it has been programmatically changed (using XML-Beans).
     * It's usually used in Junit tests.
     */
    public void saveConfigurationSet() {
        try {
            for (String path : _isaConfigFiles.keySet()) {
                IsaTabConfigFileType cfg = _isaConfigFiles.get(path);

                IsatabConfigFileDocument cfgDoc = IsatabConfigFileDocument.Factory.newInstance();
                cfgDoc.setIsatabConfigFile(cfg);
                cfgDoc.save(new File(path));
            }
        } catch (IOException ex) {
            throw new TabIOException("Error while saving the ISATAB configuration: " + ex.getMessage(), ex);
        }
    }
}
