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

package org.isatools.tablib.export;


import org.isatools.tablib.schema.Record;
import org.isatools.tablib.schema.SectionInstance;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.model.Identifiable;
import uk.ac.ebi.utils.reflection.ReflectionUtils;

import java.util.*;

/**
 * Record-oriented exporting component. Performs the exporting job by exporting the instances of a given class,
 * on a per-instance, per-record basis.
 * <p/>
 * date: Mar 31, 2008
 *
 * @author brandizi
 * @param <XT> the type of object to be exported.
 */
public abstract class RecordOrientedExporter<XT extends Identifiable> extends SectionExporter {
    /**
     * If it's not null, will be used to sort the exported objects and spawn results in a particular order.
     */
    protected Comparator<? super XT> comparator;

    public RecordOrientedExporter(BIIObjectStore store) {
        super(store);
    }


    /**
     * Goes through all the instances of the mapped class and produces a new record for each one.
     *
     * @see org.isatools.tablib.export.AbstractTabExporter#export()
     */
    public SectionInstance export() {
        log.debug(this.getClass().getSimpleName() + ".export(), begin ");

        SectionInstance sectionInstance = createSectionInstance();

        Collection<XT> sources = getSortedSources();
        if (sources == null || sources.size() == 0) {
            log.trace("AbstractTabExporter: no instance of " + getSourceClass().getSimpleName() + " to export");
        } else {
            for (XT source : sources) {
                Record record = new Record(sectionInstance);
                log.trace("export of " + source);
                export(source, record);
                if (record.isNull()) {
                    log.trace("AbstractTabExporter: empty record corresponding to " + source);
                    continue;
                }
                sectionInstance.addRecord(record);
            }
        }

        log.debug("   export() end");

        return sectionInstance;
    }


    /**
     * Exports a single instance of the exported class.
     *
     * @param source the object to be mapped
     * @param record an empty record to be populated by the exported object.
     */
    public abstract Record export(XT source, Record record);

    /**
     * TODO: caching
     */
    public Class<XT> getSourceClass() {
        return ReflectionUtils.getTypeArgument(RecordOrientedExporter.class, this.getClass(), 0);
    }


    /**
     * Gets all the sources which need to be mapped. By default returns the objects in the store of type
     * {@link #getSourceClass()}.
     */
    protected Collection<XT> getSources() {
        return getStore().valuesOfType(getSourceClass());
    }

    /**
     * Gets all the objects of SourceType which are in the store, sort them according to the comparator passed to
     * the constructor.
     * <p/>
     * TODO: the implementation is not so efficient.
     */
    protected Collection<XT> getSortedSources() {
        Collection<XT> sources = getSources();
        if (comparator == null) {
            return sources;
        }
        if (sources == null || sources.size() < 2) {
            return sources;
        }

        List<XT> sourcesList = new ArrayList<XT>(sources);
        Collections.sort(sourcesList, comparator);

        return sourcesList;
    }

}
