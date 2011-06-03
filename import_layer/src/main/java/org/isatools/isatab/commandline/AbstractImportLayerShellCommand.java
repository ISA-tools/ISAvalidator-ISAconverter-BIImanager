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

package org.isatools.isatab.commandline;

import org.apache.commons.cli.*;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.isatools.isatab.ISATABValidator;
import org.isatools.isatab.gui_invokers.GUIInvokerResult;
import org.isatools.isatab.mapping.AssayTypeEntries;
import org.isatools.isatab_v1.ISATABLoader;
import org.isatools.tablib.exceptions.TabIOException;
import org.isatools.tablib.exceptions.TabMissingResourceException;
import org.isatools.tablib.schema.FormatSetInstance;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.utils.datasourceload.DataLocationManager;
import uk.ac.ebi.bioinvindex.utils.datasourceload.DataSourceLoader;
import uk.ac.ebi.bioinvindex.utils.datasourceload.InvalidConfigurationException;
import uk.ac.ebi.bioinvindex.utils.i18n;

import javax.persistence.EntityManager;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

import static java.lang.System.out;

/**
 * Common tasks run for submission tool commands
 * <p/>
 * date: Jun 6, 2008
 *
 * @author brandizi
 */
public class AbstractImportLayerShellCommand {
    /**
     * TODO: a class of constants
     */
    public static final String PROPERTY_NAME_CONFIG_PATH = "bioinvindex.config-path";

    /**
     * All setup by {@link #setup(String[])}
     */
    protected static String baseDir, sourcePath, exportPath, sourceDirPath;
    private static String _configPath = null;

    /**
     * Setup by {@link #loadIsaTab()}
     */
    protected static ISATABLoader isatabLoader;

    /**
     * We need to initialize this inside main(), cause of configuration order issues.
     */
    protected static Logger log;

    /**
     * Setup common items needed to perform submission tool related tasks, which includes:
     * <ul><li>configuration from submission.config file</li>
     * <li>{@link #baseDir}, {@link #sourcePath}, {@link #exportPath}</li></ul>
     */
    protected static void setup(String[] args) throws IOException {
        setupConfig();

        // Working dirs
        //
        String baseDir = System.getProperty("user.dir");
        if (args.length == 0) {
            sourceDirPath = sourcePath = baseDir;
        } else {
            sourcePath = args[0];
            File sourcePathF = new File(sourcePath);
            sourceDirPath = sourcePathF.isFile()
                    ? sourceDirPath = StringUtils.trimToEmpty(sourcePathF.getParent())
                    : sourcePath;
        }
    }


    @SuppressWarnings("static-access")
    protected static Options createCommonOptions() {
        Options result = new Options();
        result.addOption(
                OptionBuilder.withArgName("file-path")
                        .withDescription(
                                "the file where to save an additional log (requires that you define FileApp in log4j.properties)"
                        )
                        .hasArg()
                        .withLongOpt("log-file")
                        .create("l")
        );
        return result;
    }

    /**
     * Creates a cli command line corresponding to the possible options clopts and the actual arguments args.
     *
     * @param invokerClass the class that invokes this method, from which the "printUsage()" method, if
     *                     present, is called in case of syntax errors.
     * @return the parsing result if no paring error occurs, otherwise print a message, a usage message and
     *         exit with 1 (via {@link System#exit(int)}).
     */
    protected static CommandLine parseCommandLine(
            Options clopts, String[] args, Class<? extends AbstractImportLayerShellCommand> invokerClass
    )
            throws SecurityException, IllegalArgumentException,
            IllegalAccessException, InvocationTargetException {
        CommandLineParser clparser = new GnuParser();
        CommandLine cmdl = null;
        try {
            cmdl = clparser.parse(clopts, args);
            return cmdl;
        }
        catch (ParseException e) {
            out.println("Command syntax error, " + e.getMessage());
            out.println();

            try {
                Method usageMethod = invokerClass.getMethod("printUsage", Options.class);
                usageMethod.invoke(null, clopts);
            }
            catch (NoSuchMethodException ex) {
            }

            System.exit(1);
            return null;
        }
    }


    /**
     * Maps an IsaTab files set into the BII model, you must call setup() before this, the files are read from
     * {@link #sourcePath}, i.e.: the command line argument #0
     */
    protected static BIIObjectStore loadIsaTab() throws IOException {
        isatabLoader = new ISATABLoader(sourcePath);
        FormatSetInstance isatabInstance = isatabLoader.load();

        ISATABValidator validator = new ISATABValidator(isatabInstance);
        if (GUIInvokerResult.WARNING == validator.validate()) {
            log.warn("WARNING: ISATAB validation reports problems, see log messages");
        }

        return validator.getStore();
    }

    /**
     * Outputs the log events sent to the FileApp appender to the parameter file path.
     */
    public static void setupLog4JPath(String filePath) {
        if (filePath == null) {
            return;
        }

        Logger rootLog = Logger.getRootLogger();
        if (rootLog == null) {
            return;
        }

        Appender app = rootLog.getAppender("FileApp");
        if (app == null) {
            return;
        }

        if (!(app instanceof FileAppender)) {
            return;
        }
        FileAppender fapp = (FileAppender) app;

        fapp.setFile(filePath);
        fapp.activateOptions();

        // OverridablePropertyConfigurator.setProperty ( "log4j.appender.FileApp.File", filePath );
    }

    public static void setupLog4JPath(CommandLine cmdl, String defaultPath) {
        String logpath = cmdl.getOptionValue("l", defaultPath);
        setupLog4JPath(logpath);
    }

    /**
     * @return the config path, as provided by the property {@link #PROPERTY_NAME_CONFIG_PATH}. If the property
     *         is null, returns the value returned by the user.dir property + "/config/"
     */
    public static String getConfigPath() {
        if (_configPath != null) {
            return _configPath;
        }

        //Try to look first in the root folder
        _configPath =AbstractImportLayerShellCommand.class.getClassLoader().getResource("").getPath() + "config/";
        
        System.out.println("Initial config path is :" + _configPath);
        
        
        //If this path do not exists...
        if (!new File(_configPath).exists()){

        	//...then use system properties.
            _configPath = StringUtils.trimToEmpty(
                    System.getProperty(PROPERTY_NAME_CONFIG_PATH, System.getProperty("user.dir") + "/config/")
            );

        }
        
        
        if (_configPath.length() > 0 && !_configPath.endsWith("/")) {
            _configPath += "/";
        }
        
        System.out.println("Final config path is :" + _configPath);
        return _configPath;
    }


    /**
     * Finds the hibernate.properties file in the directory returned by {@link #getConfigPath()}. This file
     * <b>must</b> exists, becuase we need some DB configuration as a minimum for using tools such as the Loader.
     */
    public static Properties getHibernateProperties() {
        String cfgPath = getConfigPath(),
                hibCfgPath = cfgPath + "hibernate.properties";

        try {
            File hibCfgFile = new File(hibCfgPath);
            Properties props = new Properties();
            props.load(new FileInputStream(hibCfgFile));
            return props;
        }
        catch (FileNotFoundException e) {
            throw new TabMissingResourceException(i18n.msg("hibernate_file_not_found", cfgPath), e);
        }
        catch (IOException e) {
            throw new TabIOException(i18n.msg("hibernate_file_not_found", cfgPath, e.getMessage()), e);
        }
    }


    /**
     * Sets up the properies coming from {@link #getConfigPath()} and also configures Log4j (log4j.properties) in the
     * same directory.
     */
    public static void setupConfig() {
        try {
            // Local configuration
            //
            String configPath = getConfigPath();
            String configFilePath = configPath + "config.properties";
            File propFile = new File(configFilePath);
            if (propFile.exists()) {
                Properties p = new Properties();
                p.load(new FileInputStream(new File(configFilePath)));
                for (Object keyo : p.keySet()) {
                    String key = (String) keyo;
                    System.setProperty(key, p.getProperty(key));
                }
            }

            // Log4j setup
            //
            String log4jConfigPath = System.getProperty("log4j.configuration", null);
            if (log4jConfigPath == null) {
                System.setProperty("log4j.configuration", "file:///" + configPath + "log4j.properties");
            }

            // Use our own configurator for log4j
            // TODO: remove, no longer used
            // System.setProperty ( "log4j.configuratorClass", "org.isatools.isatab.commandline.OverridablePropertyConfigurator" );
        }
        catch (IOException ex) {
            throw new TabIOException(i18n.msg("app_cfg_loading_io", ex.getMessage()), ex);
        }
    }

    /**
     * Creates a data location manager for the ISA tools. It searches {@link DataSourceLoader#DEFAULT_FILE_NAME}
     * under {@link #getConfigPath()}, if that file is found, this method will use it, after having updated the
     * BII db. Otherwise it uses the last version found in the BII db.
     */
    public static DataLocationManager createDataLocationManager(EntityManager entityManager) {
        try {
            DataLocationManager dataLocationMgr;

            String mappingFilePath = AbstractImportLayerShellCommand.getConfigPath() +
                    DataSourceLoader.DEFAULT_FILE_NAME;

            File locationFile = new File(mappingFilePath);

            // TODO: we don't need to read it from the resources, to be removed
            InputStream instream = locationFile.canRead()
                    ? new FileInputStream(locationFile)
                    : AssayTypeEntries.class.getResourceAsStream("/" + DataSourceLoader.DEFAULT_FILE_NAME);

            if (instream != null) {
                DataSourceLoader dataSourceLoader = new DataSourceLoader();

                // This is necessary, cause Timestamp storage in MySQL has seconds precision at most, so
                // it happens that persisted studies have the same timestamp value of data sources, which
                // make several tests to fail
                //
                Thread.sleep(1000);
                dataSourceLoader.setEntityManager(entityManager);
                dataSourceLoader.loadAll(instream);
                Thread.sleep(1000);
            }

            dataLocationMgr = new DataLocationManager();
            dataLocationMgr.setEntityManager(entityManager);
            return dataLocationMgr;
        }
        catch (FileNotFoundException ex) {
            throw new TabIOException(i18n.msg("dispatch_cfg_load_io", ex.getMessage()), ex);
        }
        catch (InvalidConfigurationException ex) {
            throw new TabIOException(i18n.msg("dispatch_cfg_load_io", ex.getMessage()), ex);
        }
        catch (InterruptedException ex) {
            throw new TabIOException(i18n.msg("dispatch_cfg_load_io", ex.getMessage()), ex);
        }
    }

    public static void printUsageCommonNotes() {
        out.println();
        out.println("Related Files");
        out.println();
        out.println("  config/: a set of configuration files are stored here.");
        out.println("  config.sh: defines common configuration options for the Submission command line tools.");
        out.println();
    }

}
