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
 * - Manon Delahaye (ISA team trainee:  BII web services)
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
 * To request enhancements:  http://sourceforge.net/tracker/?group_id=215183&atid=1032652
 *
 *
 * __________
 * License:
 * __________
 *
 * This work is licenced under the Creative Commons Attribution-Share Alike 2.0 UK: England & Wales License. To view a copy of this licence, visit http://creativecommons.org/licenses/by-sa/2.0/uk/ or send a letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California 94105, USA.
 *
 * __________
 * Sponsors
 * __________
 * This work has been funded mainly by the EU Carcinogenomics (http://www.carcinogenomics.eu) [PL 037712] and in part by the
 * EU NuGO [NoE 503630](http://www.nugo.org/everyone) projects and in part by EMBL-EBI.
 */

package org.isatools.isatab.commandline;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;
import org.isatools.isatab.export.isatab.ISATABDBExporter;
import uk.ac.ebi.bioinvindex.dao.ejb3.DaoFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.PrintWriter;
import java.util.Properties;

import static java.lang.System.out;


/**
 * A <i>provisional</i> command line that exports studies from the BII db onto a local directory.
 * TODO: it's used for internal tests, to be completed with all the available options.
 *
 * @author brandizi
 *         <b>date</b>: Mar 11, 2010
 */
public class ISATABExportShellCommand extends AbstractImportLayerShellCommand {
	@SuppressWarnings("static-access")
	public static void main(String[] args) {
		try {
			Options clopts = createCommonOptions();
			clopts.addOption(OptionBuilder.withArgName("directory-path")
					.withDescription("Exports to local path (instead of the BII repository), useful for checking")
					.hasArg()
					.withLongOpt("to-dir")
					.create("d")
			);
			clopts.addOption("s", "skip-data-files", false,
					"Avoids to export (-to-dir option) or backup (BII repo export) the data files"
			);
			CommandLine cmdl = AbstractImportLayerShellCommand.parseCommandLine(
					clopts, args, ISATABExportShellCommand.class
			);

			args = cmdl.getArgs();
			if (args == null || args.length == 0) {
				printUsage(clopts);
				System.exit(1);
			}

			setup(args);

			setupLog4JPath(cmdl, null);
			// Need to initialize this here, otherwise above config will fail
			log = Logger.getLogger(ISATABExportShellCommand.class);

			String exportPath = cmdl.getOptionValue("d");
			boolean skipDataFiles = cmdl.hasOption("s");

			Properties hibProps = AbstractImportLayerShellCommand.getHibernateProperties();
			hibProps.setProperty("hibernate.search.indexing_strategy", "manual");
			// We assume this, since we are unloading something that is supposed to already exist (including during tests).
			hibProps.setProperty("hibernate.hbm2ddl.auto", "update");

			EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("BIIEntityManager", hibProps);
			EntityManager entityManager = entityManagerFactory.createEntityManager();
			DaoFactory daoFactory = DaoFactory.getInstance(entityManager);

			ISATABDBExporter dbexp = new ISATABDBExporter(daoFactory, args);

			if (exportPath != null) {
				dbexp.exportToPath(exportPath, skipDataFiles);
			} else {
				dbexp.exportToRepository(skipDataFiles);
			}

			System.exit(0);
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
				"isaexport [options] <study-acc> <study-acc> ...",
				"\nExports ISATAB studies from the BII database back to the file repository, or to a local path.\n\nOptions:",
				opts,
				2, 4,
				"\n",
				false
		);
		printUsageCommonNotes();
	}

}
