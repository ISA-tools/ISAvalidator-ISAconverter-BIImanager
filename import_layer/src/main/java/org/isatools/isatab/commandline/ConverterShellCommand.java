package org.isatools.isatab.commandline;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.isatools.isatab.export.magetab.MAGETABExporter;
import org.isatools.isatab.export.pride.DraftPrideExporter;
import org.isatools.isatab.export.sra.SraExporter;
import org.isatools.isatab.gui_invokers.GUIInvokerResult;
import org.isatools.isatab.isaconfigurator.ISAConfigurationSet;
import org.isatools.isatab.isaconfigurator.ISAConfiguratorValidator;
import org.isatools.isatab_v1.ISATABLoader;
import org.isatools.isatab_v1.mapping.ISATABReducedMapper;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.utils.i18n;

import java.io.PrintWriter;
import java.util.Arrays;

import static java.lang.System.out;

/**
 * Performs the conversion in one of the available target formats.
 * <p/>
 * <dl><dt>date</dt><dd>May 11, 2010</dd></dl>
 *
 * @author brandizi
 */
public class ConverterShellCommand extends AbstractImportLayerShellCommand {
	private final static String[] VALID_TARGETS = {"magetab", "prideml", "sra", "all"};

	@SuppressWarnings("static-access")
	public static void main(String[] args) {
		String validStr = Arrays.toString(VALID_TARGETS);

		try {
			Options clopts = createCommonOptions();
			clopts.addOption(OptionBuilder.withArgName("type")
					.withDescription("The type of desired conversion output. Valid values are:" + validStr)
					.hasArg()
					.withLongOpt("target-format")
					.create("t")
			);
			CommandLine cmdl = AbstractImportLayerShellCommand.parseCommandLine(
					clopts, args, ConverterShellCommand.class
			);


			args = cmdl.getArgs();
			if (args == null || args.length == 0) {
				printUsage(clopts);
				System.exit(1);
			}

			String targetType = StringUtils.lowerCase(cmdl.getOptionValue("t"));
			if (targetType == null || !ArrayUtils.contains(VALID_TARGETS, targetType)) {
				printUsage(clopts);
				System.exit(1);
			}

			setup(args);

			setupLog4JPath(cmdl, null);
			// Need to initialize this here, otherwise above config will fail
			log = Logger.getLogger(ConverterShellCommand.class);

			exportPath = args.length > 1 ? args[1] : sourceDirPath;

			BIIObjectStore store = loadIsaTab();
			log.info(i18n.msg("mapping_done_now_exporting", store.size()));

			boolean wantAll = "all".equals(targetType);
			if (wantAll || "magetab".equals(targetType)) {
				MAGETABExporter.dispatch(store, sourceDirPath, exportPath);
				log.info(i18n.msg("converter_export_done", "MAGETAB", exportPath + "/magetab"));
			}
			if (wantAll || "prideml".equals(targetType)) {
				DraftPrideExporter exporter = new DraftPrideExporter(store, sourceDirPath, exportPath);
				exporter.export();
				log.info(i18n.msg("converter_export_done", "PRIDE", exportPath + "/pride"));
			}
			if (wantAll || "sra".equals(targetType)) {
				log.info("Validating ISA-Tab before export first");
				String configPath = args[2];
				log.info("Using config: " + configPath);
				ISAConfigurationSet.setConfigPath(configPath);
				log.info("Loading ISA-Tab from: " + sourceDirPath);
				ISATABLoader loader = new ISATABLoader(sourceDirPath);
				ISATABReducedMapper mapper = new ISATABReducedMapper(new BIIObjectStore(), loader.load());
				ISAConfiguratorValidator validator = new ISAConfiguratorValidator(mapper.map());
				log.info("Running validator");
				if (validator.validate() != GUIInvokerResult.SUCCESS) {
					log.warn("Validation failed");
				} else {
					log.info("Using SraExporter");
					SraExporter exporter = new SraExporter(store, sourceDirPath, exportPath);
					boolean flag = exporter.export();
					log.info(i18n.msg("converter_export_done", "SRA", exportPath + "/sra"));
					// SRA output correctly produced
					if (flag) {
						System.exit(0);
					}
					// no study has been converted to SRA
					else {
						System.exit(1);
					}
				}
			}
		}
		catch (Exception ex) {
			String msg = "ERROR: problem while running the ISATAB converter: " + ex.getMessage();
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
				"convert [options] -t <type> <source-path> [<dest-path>]",
				"\nConverts an ISATAB submission into a different format.\n\nOptions:",
				opts,
				2, 4,
				"\n",
				false
		);
		printUsageCommonNotes();
	}

}
