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

package org.isatools.tablib.mapping;

import org.apache.log4j.Logger;
import org.isatools.tablib.schema.Record;
import org.isatools.tablib.schema.SectionInstance;
import org.isatools.tablib.utils.BIIObjectStore;
import org.isatools.tablib.utils.logging.TabNDC;
import uk.ac.ebi.bioinvindex.model.Identifiable;
import uk.ac.ebi.utils.reflection.ReflectionUtils;

import java.util.List;

/**
 * A mapper which is able to build the mapping record by record. Basically it's an
 * {@link #AbstractTabMapper} extended with methods for mapping on the basis of the
 * single record.
 * <p/>
 * Dec 19, 2007
 *
 * @author brandizi
 * @param <T> the object type mapped by this mapper
 */
public abstract class RecordOrientedTabMapper<T extends Identifiable>
        extends SectionTabMapper {
    private Class<T> mappedClass;

    protected static final Logger log = Logger.getLogger(RecordOrientedTabMapper.class);


    public RecordOrientedTabMapper(BIIObjectStore store, SectionInstance sectionInstance) {
        super(store, sectionInstance);
    }


    /**
     * Maps a single record to a single object. This should not save the mapped
     * object into the store, {@link #map()} should do that instead.
     */
    public abstract T map(int recordIndex);

    /**
     * Does the mapping job record-by-record, stores the mapped objects in the Object store (in case
     * it is needed) and returns the store. The default version implemented here goes
     * through all records, calls {@link #map(int)} and stores the result
     */
    public BIIObjectStore map() {
        SectionInstance sectionInstance = getSectionInstance();
        String sectionName = sectionInstance.getSection().getId();
        BIIObjectStore store = getStore();
        String logpref = String.format("Mapping records in the section '%s': ", sectionName);

        TabNDC ndc = TabNDC.getInstance();
        ndc.pushSection(sectionInstance);

        try {
            log.trace("\n\n" + logpref + "start");

            List<Record> records = sectionInstance.getRecords();
            int sz = records == null ? 0 : records.size();

            if (sz == 0) {
                log.debug("WARNING:" + logpref + "attempt to map the empty section " + sectionName);
                return null;
            }


            for (int i = 0; i < sz; i++) {
                T mappedObject = this.map(i);
                this.store((T) mappedObject);
            }
        }
        finally {
            log.trace(logpref + "returning with " + store.size() + " objects \n\n");
            ndc.popTabDescriptor();
        }
        return store;
    }


    /**
     * The concrete class this mapper uses to create new mapped objects.
     * It is computed from MappedTypeImpl.
     */
    public Class<T> getMappedClass() {
        if (mappedClass != null) {
            return mappedClass;
        }
        return mappedClass = ReflectionUtils.getTypeArgument(
                RecordOrientedTabMapper.class, this.getClass(), 0
        );
    }


    /**
     * By default calls getMappedClass().newInstance (). You may override it with your
     * custom initialization, for instance if empty constructor is not available.
     * <p/>
     * TODO: proper exception
     */
    public T newMappedObject() throws InstantiationException, IllegalAccessException {
        return getMappedClass().newInstance();
    }


    /**
     * Stores a newly mapped object into an objectStore. Does it in a way specific to
     * this class mapper. By default uses {@link #getMappedClass()}.
     */
    public void store(BIIObjectStore store, T mappedObject) {
        if (mappedObject == null) {
            log.trace("RecordOrientedTabMapper, null object to store(), skipping");
            return;
        }

        String id = getStoreKey(mappedObject);

        if (id == null && log.isTraceEnabled()) {
            log.trace(
                    "RecordOrientedTabMapper.store(): null value for object key of object " + mappedObject
                            + ", not saving it in the store"
            );
            return;
        }

        store.put(this.getMappedClass(), id, mappedObject);
    }


    /**
     * Uses this.getStore()
     */
    public void store(T mappedObject) {
        store(this.getStore(), mappedObject);
    }


    /**
     * Allows to return an identifier string, which is specific to the object
     * mapped by this mapper and  which has to be used as a key in the object store.
     * <p/>
     * WARNING: if you return null the object will be silently discarded (not stored by store() methods)
     */
    public abstract String getStoreKey(T mappedObject);

}
