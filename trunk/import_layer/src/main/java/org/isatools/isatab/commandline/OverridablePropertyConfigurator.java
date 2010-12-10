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

import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggerRepository;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

/**
 * TODO: No longer used, remove
 * <p/>
 * An extension of {@link PropertyConfigurator} that allows to programmatically override the properties that are
 * loaded from a file or a URL.
 * <p/>
 * TODO: To be moved in the model package, utils subpackage.
 * <p/>
 * <dl><dt>date:</dt><dd>Dec 8, 2008</dd></dl>
 *
 * @author brandizi
 */
public class OverridablePropertyConfigurator extends PropertyConfigurator {
    private static Properties properties = new Properties();

    /**
     * Use this before start using the logger, it will get the modified values
     */
    public static void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    /**
     * Use this before start using the logger, it will get the modified values
     */
    public static void setProperties(Properties properties) {
        properties.putAll(properties);
    }


    /**
     * Reads the config properties from the file and overrides them with the ones overridden in this configurator
     */
    @Override
    public void doConfigure(String configFileName, LoggerRepository hierarchy) {
        // Code copied from the original log4j configurator
        Properties props = new Properties();
        try {
            FileInputStream istream = new FileInputStream(configFileName);
            props.load(istream);
            istream.close();
        }
        catch (IOException e) {
            LogLog.error("Could not read configuration file [" + configFileName + "].", e);
            LogLog.error("Ignoring configuration file [" + configFileName + "].");
            return;
        }
        // If we reach here, then the config file is alright.

        // Adds up our properties
        props.putAll(properties);

        doConfigure(props, hierarchy);

    }

    /**
     * Reads the config properties from the file and overrides them with the ones overridden in this configurator
     */
    @Override
    public void doConfigure(URL configURL, LoggerRepository hierarchy) {
        // Code copied from the original log4j configurator
        Properties props = new Properties();
        LogLog.debug("Reading configuration from URL " + configURL);

        try {
            props.load(configURL.openStream());
        }
        catch (java.io.IOException e) {
            LogLog.error("Could not read configuration file from URL [" + configURL + "].", e);
            LogLog.error("Ignoring configuration file [" + configURL + "].");
            return;
        }

        // Adds up our properties
        props.putAll(properties);

        doConfigure(props, hierarchy);
    }

}
