package org.isatools.isatab.isaconfigurator;

import com.sun.tools.javac.util.Pair;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.isatools.isatab.gui_invokers.GUIISATABValidator;
import org.isatools.isatab.gui_invokers.GUIInvokerResult;
import org.isatools.isatab_v1.ISATABLoader;
import org.isatools.isatab_v1.mapping.ISATABReducedMapper;
import org.isatools.tablib.exceptions.TabValidationException;
import org.isatools.tablib.utils.BIIObjectStore;
import org.isatools.tablib.utils.logging.TabLoggingEventWrapper;
import org.junit.Test;

import java.io.File;
import java.util.*;

import static java.lang.System.out;
import static org.junit.Assert.assertTrue;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 10/02/2011
 *         Time: 14:51
 */
public class ISAConfigurationBatchTest {

    public static final String BASE_DIR = System.getProperty("basedir");
    public static final String ISATAB_TEST_DIRECTORY = BASE_DIR + "/target/test-classes/test-data/isatab/batch_test/tabfiles/";
    public static final String ISACONFIG_LOCATION = BASE_DIR + "/target/test-classes/test-data/isatab/batch_test/configs/isa_configurator/";

    @Test
    public void testLoadingAllISAtab() {
        ISAConfigurationSet.setConfigPath(ISACONFIG_LOCATION);

        File isatabDirectory = new File(ISATAB_TEST_DIRECTORY);

        File[] isatabFiles = isatabDirectory.listFiles();

        Map<Pair<File, GUIInvokerResult>, Set<String>> summaryReport = new HashMap<Pair<File, GUIInvokerResult>, Set<String>>();

        out.println("These files will be tested:");

        for (File isatab : isatabFiles) {
            out.println("\t" + isatab.getName());
        }

        for (File isatab : isatabFiles) {
            if (isatab.isDirectory()) {
                out.println("-- TESTING | " + isatab.getName() + " --");
                GUIISATABValidator validator = new GUIISATABValidator();
                GUIInvokerResult result = validator.validate(isatab.getAbsolutePath());
                summaryReport.put(new Pair(isatab, result), getValidatorReport(validator.getLog()));
            } else {
                out.println(isatab.getName() + " is not a directory");
            }
        }

        System.out.println("Summary");

        for (Pair<File, GUIInvokerResult> file : summaryReport.keySet()) {
            System.out.println(file.fst.getName() + " " + (file.snd == GUIInvokerResult.SUCCESS ? " Passed" : "Failed") + " validation!");
            Set<String> messages = summaryReport.get(file);
            if(messages.size() > 0 ) {
                System.out.println("Errors found: ");
                for(String message : messages) {
                    System.out.println("\t" + message);
                }
            } else {
                System.out.println("No errors found...");
            }
        }
    }

    protected Set<String> getValidatorReport(List<TabLoggingEventWrapper> logResult) {
        Set<String> messages = new HashSet<String>();

        for (TabLoggingEventWrapper tlew : logResult) {

            Level eventLevel = tlew.getLogEvent().getLevel();

            if (eventLevel.equals(Level.ERROR)) {
                messages.add(tlew.getFormattedMessage());
            }
        }

        return messages;
    }
}
