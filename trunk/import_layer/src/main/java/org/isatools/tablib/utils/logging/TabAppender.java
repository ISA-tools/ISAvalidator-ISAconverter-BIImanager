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

package org.isatools.tablib.utils.logging;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;

/**
 * The TabAppender define a new append() method that receives a {@link TabLoggingEventWrapper}, which can be used to
 * perform logging operations in a way that is aware of the kind of objects provided by {@link TabNDC}, i.e.: the
 * localization of the structure element in the TAB format being processed and identification of the BII model objects
 * being processed.
 *
 * @author brandizi
 *         <b>date</b>: Apr 2, 2009
 */
public abstract class TabAppender extends AppenderSkeleton {

    /**
     * You should define what your tab-appender does here and not in {@link #append(LoggingEvent)}.
     */
    protected abstract void append(TabLoggingEventWrapper tabEvent);

    /**
     * This create the extendend {@link TabLoggingEventWrapper} and delegates all to {@link #append(TabLoggingEventWrapper)}.
     * You should not need to override this method, override {@link #append(TabLoggingEventWrapper)}
     */
    @Override
    protected final void append(LoggingEvent event) {
        if (layout == null) {
            append(new TabLoggingEventWrapper(event, null));
        } else {
            StringBuilder formattedMessage = new StringBuilder("");
            formattedMessage.append(layout.format(event));
            if (layout.ignoresThrowable()) {
                String[] s = event.getThrowableStrRep();
                if (s != null) {
                    int len = s.length;
                    for (int i = 0; i < len; i++) {
                        formattedMessage.append(s[i]);
                        formattedMessage.append(Layout.LINE_SEP);
                    }
                }
            }
            append(new TabLoggingEventWrapper(event, formattedMessage.toString()));
        }
    }

    /**
     * true, cause when there is no layout, we just report the original message.
     */
    @Override
    public boolean requiresLayout() {
        return true;
    }
}
