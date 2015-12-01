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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.isatools.isatab.ISATABValidator;
import org.isatools.isatab.isaconfigurator.ISAConfigurationSet;
import org.isatools.isatab.mapping.ISATABMapper;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.model.Identifiable;
import uk.ac.ebi.bioinvindex.model.processing.Processing;
import uk.ac.ebi.bioinvindex.utils.DotGraphGenerator;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;

import static java.lang.System.out;

/**
 * Does preliminary validation tasks and other useful stuff.
 * <p/>
 * Among other things, creates the DOT graph which describes the experimental pipeline. This may be
 * opened with the GraphViz utility (see {@link DotGraphGenerator}).
 * <p/>
 * date: Jun 6, 2008
 *
 * @author brandizi
 */
public class ValidateShellCommand extends AbstractImportLayerShellCommand {

    @SuppressWarnings("static-access")
    public static void main(String[] args) {
        try {
            Options clopts = new Options();
            clopts.addOption(
                    OptionBuilder.withArgName("file-path")
                            .withDescription(
                                    "the file where to save an additional log (requires that you define FileApp in log4j.properties, defaults " +
                                            "to <export-path>/validator.log)."
                            )
                            .hasArg()
                            .withLongOpt("log-file")
                            .create("l")
            );
            clopts.addOption(OptionBuilder.withArgName("directory-path")
                    .withDescription("Is the directory where to save the result .dot file. Defaults to <source-path>")
                    .hasArg()
                    .withLongOpt("report-dir")
                    .create("d")
            );
            clopts.addOption(OptionBuilder.withArgName("configuration-path")
                            .withDescription("Is the directory where to load the configuration files from")
                            .hasArg()
                            .withLongOpt("report-dir")
                            .create("c")
            );
            CommandLine cmdl = AbstractImportLayerShellCommand.parseCommandLine(
                    clopts, args, ValidateShellCommand.class
            );

            args = cmdl.getArgs();
            if (args == null || args.length == 0) {
                printUsage(clopts);
                System.exit(1);
            }

            setup(args);

            exportPath = cmdl.getOptionValue("d");
            if (exportPath == null) {
                exportPath = sourceDirPath;
            }

            setupLog4JPath(cmdl, exportPath + "/validator.log");

            // Need to initialize this here, otherwise above config will fail
            log = Logger.getLogger(ValidateShellCommand.class);
            String configPath = cmdl.getOptionValue("c");
            log.info("Validating using config: " + configPath);
            ISAConfigurationSet.setConfigPath(configPath);
            BIIObjectStore store = loadIsaTab();

            log.info("ISATAB submission loaded, we have the following results:");
            log.info(ISATABMapper.report(store));

            // Create the .dot graph of the experimental pipeline
            //
            Collection<Identifiable> objects = new ArrayList<Identifiable>();
            objects.addAll(store.values(Processing.class));
            DotGraphGenerator dotter = new DotGraphGenerator(objects);
            String dotFileName =
                    exportPath + "/" + FilenameUtils.getBaseName(isatabLoader.getInvestigationFileName()) + ".dot";
            dotter.createGraph(dotFileName);

            log.info("\n\nExperimental Graph written in " + dotFileName);
        }
        catch (Exception ex) {
            String msg = "ERROR: problem while running the ISATAB exporter: " + ex.getMessage();
            if (log == null) {
                out.println(msg + "\n");
                ex.printStackTrace();
            } else {
                log.fatal(msg, ex);
            }
            System.exit(1);
        }
    }

    public static void printUsage(Options opts) {
        out.println();

        HelpFormatter helpFormatter = new HelpFormatter();
        PrintWriter pw = new PrintWriter(out, true);
        helpFormatter.printHelp(pw, 80,
                "validate [options] <source-path>",
                "\nDoes a preliminary validation on the ISATAB submission archive." +
                        "Additionally, creates the graph representing the experimental pipeline in an ISATAB file set." +
                        "The result is a .dot file, which can be shown with the GraphViz tool (http://www.graphviz.org/)." +
                        "\n\nOptions:"
                ,
                opts,
                2, 4,
                "\n",
                false
        );
        out.println();
    }
}
