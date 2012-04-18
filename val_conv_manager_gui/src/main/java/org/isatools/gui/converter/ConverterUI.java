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

package org.isatools.gui.converter;

import org.apache.log4j.spi.LoggingEvent;
import org.isatools.gui.AppContainer;
import org.isatools.gui.ApplicationType;
import org.isatools.gui.CommonUI;
import org.isatools.gui.Globals;
import org.isatools.isatab.gui_invokers.AllowedConversions;
import org.isatools.isatab.gui_invokers.GUIISATABConverter;
import org.isatools.isatab.gui_invokers.GUIInvokerResult;
import org.isatools.tablib.utils.BIIObjectStore;
import org.isatools.tablib.utils.logging.TabLoggingEventWrapper;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

/**
 * ConverterInterfaceManager
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 * @date Nov 26, 2009
 */


public class ConverterUI extends CommonUI {

    private ConversionUtilUI conversionGUI;

    public ConverterUI(AppContainer appContainer, ApplicationType useAs, String[] options) {
        super(appContainer, useAs, options);
    }

    protected void instantiateConversionUtilPanel(final String fileLocation) {
        conversionGUI = new ConversionUtilUI(buttonImage, buttonImageOver, appContainer);


        conversionGUI.addPropertyChangeListener("toMenu", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                appContainer.setGlassPanelContents(selectISATABUI);
                appContainer.validate();
            }
        });

        conversionGUI.addPropertyChangeListener("doConversion", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {

                final File outputDir = new File(propertyChangeEvent.getNewValue().toString());
                // if the output directory does not exist, then create it. this is a precautionary measure just in case!
                if (!outputDir.exists()) {
                    outputDir.mkdir();
                }

                AllowedConversions ac = AllowedConversions.ALL;
                for (String conversionType : conversionGUI.getConversions().keySet()) {
                    if (conversionGUI.getConversions().get(conversionType).isSelected()) {
                        for (AllowedConversions conversion : AllowedConversions.values()) {
                            if (conversion.getType().equals(conversionType)) {
                                ac = conversion;
                            }
                        }
                        // do conversions and output the converted files to the directory defined in outputdir textfield
                    }
                }

                final AllowedConversions ac1 = ac;
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        final GUIISATABConverter converter = new GUIISATABConverter();
                        GUIInvokerResult result = converter.convert(isatabValidator.getStore(), fileLocation, outputDir.getPath(), ac1);
                        if (result == GUIInvokerResult.SUCCESS) {

                            appContainer.setGlassPanelContents(createResultPanel(
                                    new ImageIcon(getClass().getResource("/images/converter/conversion_success.png")),
                                    Globals.CONVERT_ANOTHER, Globals.CONVERT_ANOTHER_OVER, Globals.EXIT, Globals.EXIT_OVER,
                                    "<p>conversion success. files sent to " + outputDir.getAbsolutePath() + "</p><p>" + isatabValidator.report(true) + "</p>"));
                            appContainer.validate();
                        } else {
                            String topMostError = "";

                            for (TabLoggingEventWrapper tlew : converter.getLog()) {
                                LoggingEvent le = tlew.getLogEvent();
                                topMostError = le.getMessage().toString();
                            }
                            appContainer.setGlassPanelContents(createResultPanel(new ImageIcon(getClass().getResource("/images/converter/conversion_failed.png")), Globals.CONVERT_ANOTHER, Globals.CONVERT_ANOTHER_OVER, Globals.EXIT, Globals.EXIT_OVER, "<p>error when converting files: </p><p>" + topMostError + "</p>"));
                            appContainer.validate();
                        }
                    }
                });
            }
        });

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                conversionGUI.createGUI();
                appContainer.setGlassPanelContents(conversionGUI);
                appContainer.validate();
            }
        });
    }

    protected void createLoginScreen() {
        // not implemented here.
    }

    protected void loadToDatabase(BIIObjectStore store, String isatabSubmissionPath, String report) {
        // not implemented in here.
    }

    protected void showLoaderMenu() {
        // not implemented in here
    }

}


