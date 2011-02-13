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

import org.apache.commons.beanutils.ConstructorUtils;
import org.isatools.tablib.exceptions.TabInternalErrorException;
import org.isatools.tablib.exceptions.TabStructureError;
import org.isatools.tablib.schema.FormatInstance;
import org.isatools.tablib.schema.SectionInstance;
import org.isatools.tablib.utils.BIIObjectStore;

import java.util.*;

/**
 * Maps a block of repeatable and adjoining sections.
 * <p/>
 * TODO: being sub-class of SectionTabMapper is not much correct. To change this a need another class at the upper level,
 * to be passed to the format mappers.
 * <p/>
 * Mar 10, 2008
 *
 * @author brandizi
 */
public class SectionBlockTabMapper extends SectionTabMapper implements ComposedTabMapper<SectionTabMapper> {
    private List<SectionTabMapper> tabMappers;

    /**
     * Defines a map of section ID =&gt; section mapper, which tells which section mapper has to be used to map
     * the instances of a given section. If a section in the format is not in this map, it is ignored.
     */
    protected Map<String, Class<? extends SectionTabMapper>> sectionMappersConfig =
            new HashMap<String, Class<? extends SectionTabMapper>>();


    public SectionBlockTabMapper(BIIObjectStore store, SectionInstance firstSection) {
        super(store, firstSection);
    }


    public List<SectionTabMapper> getTabMappers() {
        if (tabMappers == null) {
            initTabMappers();
        }

        return tabMappers;
    }


    /**
     * Goes through all the composing mappers and invoke their map() method
     */
    @Override
    public BIIObjectStore map() {
        List<SectionTabMapper> tabMappers = getTabMappers();
        if (tabMappers == null || tabMappers.size() == 0) {
            log.trace("SectionBlockTabMapper without any class mapper");
            return getStore();
        }

        for (SectionTabMapper mapper : tabMappers) {
            mapper.map();
        }

        return getStore();
    }


    /**
     * Goes through all section instances in the format instance and invokes getSectionMapper()
     * tabMappers is initially
     */
    protected void initTabMappers() {
        tabMappers = new ArrayList<SectionTabMapper>();
        SectionInstance firstSectionInstance = getSectionInstance();
        String firstSectionId = firstSectionInstance.getSectionId();
        FormatInstance formatInstance = firstSectionInstance.getParent();

        List<SectionInstance> instances = formatInstance.getSectionInstances();
        int firstSectionIndex = instances.indexOf(firstSectionInstance);

        if (firstSectionIndex == -1)
        // That's a nasty inconsistency, since the first section instance should come by the same format instance
        {
            throw new TabStructureError("SectionBlockTabMapper, I cannot find back the first section: " + firstSectionId);
        }

        instances = instances.subList(firstSectionIndex, instances.size());

        for (SectionInstance sectionInstance : instances) {

            // We terminate when matching another of our starting section
            if (firstSectionId.equals(sectionInstance.getSectionId()) && sectionInstance != firstSectionInstance) {
                break;
            }

            SectionTabMapper sectionMapper = getSectionMapper(sectionInstance);

            // We also terminate upon the first section not about ourself
            if (sectionMapper == null) {
                break;
            }

            log.trace(
                    this.getClass().getSimpleName() + ": adding section mapper " + sectionMapper.getClass().getSimpleName()
                            + "for section " + sectionInstance.getSectionId()
            );
            tabMappers.add(sectionMapper);
        }

        if (tabMappers.size() == 0) {
            return;
        }

        // Sorts according to priority, must return a negative result when p2 &lt; p1
        // because ascending sorting is always performed by Collections.sort
        Collections.sort(
                tabMappers,
                new Comparator<SectionTabMapper>() {
                    public int compare(SectionTabMapper o1, SectionTabMapper o2) {
                        return o2.getPriority() - o1.getPriority();
                    }
                }
        );

    }


    private SectionTabMapper getSectionMapper(SectionInstance sectionInstance) {
        String sectionId = sectionInstance.getSection().getId();

        Class<? extends SectionTabMapper> mapperClass = sectionMappersConfig.get(sectionId);
        if (mapperClass == null) {
            log.trace("WARNING: No mapper defined for section " + sectionId + " in " + this.getClass().getSimpleName());
            return null;
        }

        SectionTabMapper mapper;

        try {
            mapper = (SectionTabMapper) ConstructorUtils.invokeConstructor(
                    mapperClass,
                    new Object[]{this.getStore(), sectionInstance},
                    new Class<?>[]{BIIObjectStore.class, SectionInstance.class}
            );
        }
        catch (Exception ex) {
            throw new TabInternalErrorException("Error with the creation of a mapper for section " + sectionId);
        }

        return mapper;
    }


    /**
     * Format mappers with higher priority are invoked before the ones with lower priority Default is 0
     */
    public int getPriority() {
        return 0;
    }


    /**
     * TODO: returns an empty array, this class extends the wrong one and this method doesn't make much sense here.
     */
    @Override
    public List<Integer> getMatchedFieldIndexes() {
        return Collections.emptyList();
    }

    /**
     * TODO: returns true, this class extends the wrong one and this method doesn't make much sense here.
     */
    @Override
    public boolean validateHeaders() {
        return true;
    }

}
