package org.isatools.isatab.manager;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.isatools.isatab.gui_invokers.GUIISATABLoader;
import org.isatools.isatab.gui_invokers.GUIISATABUnloader;
import org.isatools.isatab.gui_invokers.GUIISATABValidator;
import org.isatools.isatab.gui_invokers.GUIInvokerResult;
import org.isatools.isatab.isaconfigurator.ISAConfigurationSet;
import org.isatools.tablib.exceptions.TabIOException;
import org.isatools.tablib.exceptions.TabMissingResourceException;
import org.isatools.tablib.utils.logging.TabLoggingEventWrapper;
import uk.ac.ebi.bioinvindex.utils.i18n;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;


/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 13/04/2011
 *         Time: 11:32
 */
public class SimpleManager {

    private static Logger log = Logger.getLogger(SimpleManager.class.getName());

    public SimpleManager() {
    }

    public void loadISAtab(String isatabFile, String configurationDirectory) {

        if (loadConfiguration(configurationDirectory)) {

            GUIISATABValidator isatabValidator = new GUIISATABValidator();

            GUIInvokerResult validationResult = isatabValidator.validate(isatabFile);
            if (validationResult == GUIInvokerResult.SUCCESS) {
                GUIISATABLoader loader = new GUIISATABLoader();
                log.info("Validation successful, now proceeding to load ISAtab into the BII...");
                GUIInvokerResult loadingResult = loader.persist(isatabValidator.getStore(), isatabFile);

                if (loadingResult == GUIInvokerResult.SUCCESS) {
                    log.info("Loading completed and reindexing performed");

                }

            } else {
                log.error("Loading failed. See log for details.");
            }


        } else {
            log.error("Couldn't find the configuration directory: " + configurationDirectory);
        }

    }


    public void unLoadISAtab(Set<String> toUnload) {

        GUIISATABUnloader unloaderUtil = new GUIISATABUnloader();

        log.info(toUnload.size() > 1 ? "unloading studies..." : "unloading study");

        GUIInvokerResult result = unloaderUtil.unload(toUnload);

        if (result == GUIInvokerResult.SUCCESS) {
            // fire updates back to the listener(s)
            log.info("Unloading complete. Unloaded " + toUnload.size() + " studies");

        } else {
            log.info("Some problems were encountered when loading");
        }

    }

    private Set<String> convertArrayToSet(String... toUnload) {
        Set<String> asSet = new HashSet<String>();

        asSet.addAll(Arrays.asList(toUnload));

        return asSet;
    }

    private boolean loadConfiguration(String configuration) {
        if (new File(configuration).exists()) {
            ISAConfigurationSet.setConfigPath(configuration);
            return true;
        } else {
            return false;
        }
    }

    public static void main(String[] args) {
        SimpleManager manager = new SimpleManager();

        if (args[0].equals("load")) {
            // args[0] should be the isatab directory. args[1] should be the configuration directory
            manager.loadISAtab(args[1], args[2]);
            System.exit(0);
        }


        if(args[0].equals("unload")) {
            if(args.length > 1) {
                Set<String> toUnload = new HashSet<String>();
                toUnload.addAll(Arrays.asList(args).subList(1, args.length));
                manager.unLoadISAtab(toUnload);
                System.exit(0);
            } else {
                log.info("No studies to load.");
            }
        }
    }
}
