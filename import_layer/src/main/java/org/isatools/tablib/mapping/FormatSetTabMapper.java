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
import org.apache.log4j.Logger;
import org.isatools.tablib.exceptions.TabInternalErrorException;
import org.isatools.tablib.schema.FormatInstance;
import org.isatools.tablib.schema.FormatSetInstance;
import org.isatools.tablib.utils.BIIObjectStore;

import java.util.*;

/**
 * Maps the whole format set.
 * <p/>
 * Mar 10, 2008
 *
 * @author brandizi
 */
public abstract class FormatSetTabMapper extends AbstractComposedTabMapper<FormatTabMapper> {
    private final FormatSetInstance formatSetInstance;
    private List<FormatTabMapper> tabMappers;
    protected static final Logger log = Logger.getLogger(FormatSetTabMapper.class);

    /**
     * Defines a map of section ID =&gt; format mapper, which tells which format mapper has to be used to map
     * the instances of a given section. If a format in the format set is not in this map, it is ignored.
     */
    protected Map<String, Class<? extends FormatTabMapper>> formatMappersConfig =
            new HashMap<String, Class<? extends FormatTabMapper>>();


    public FormatSetTabMapper(BIIObjectStore store, FormatSetInstance formatSetInstance) {
        super(store);
        this.formatSetInstance = formatSetInstance;
    }

    @Override
    public List<FormatTabMapper> getTabMappers() {
        if (tabMappers == null) {
            initTabMappers();
        }

        return tabMappers;
    }


    /**
     * Goes through all the format instances in the format set instance and invokes getSectionMapper()
     * tabMappers is initially
     */
    private void initTabMappers() {
        tabMappers = new ArrayList<FormatTabMapper>();
        for (FormatInstance formatInstance : getFormatSetInstance().getFormatInstances()) {
            FormatTabMapper formatMapper = getFormatMapper(formatInstance);
            if (formatMapper != null) {
                tabMappers.add(formatMapper);
            }
        }

        if (tabMappers.size() == 0) {
            return;
        }

        // Sorts according to priority, must return a negative result when p2 &lt; p1
        // because ascending sorting is always performed by Collections.sort
        Collections.sort(
                tabMappers,
                new Comparator<FormatTabMapper>() {
                    public int compare(FormatTabMapper o1, FormatTabMapper o2) {
                        return o2.getPriority() - o1.getPriority();
                    }
                }
        );
    }


    private FormatTabMapper getFormatMapper(FormatInstance formatInstance) {
        String formatId = formatInstance.getFormat().getId();

        Class<? extends FormatTabMapper> mapperClass = formatMappersConfig.get(formatId);
        if (mapperClass == null) {
            log.trace("WARNING, No mapper defined for format " + formatId + " in " + this.getClass().getCanonicalName());
            return null;
        }

        FormatTabMapper mapper;

        try {
            mapper = (FormatTabMapper) ConstructorUtils.invokeConstructor(
                    mapperClass,
                    new Object[]{this.getStore(), formatInstance},
                    new Class<?>[]{BIIObjectStore.class, FormatInstance.class}
            );
        }
        catch (Exception ex) {
            throw new TabInternalErrorException(
                    "Error with the creation of a mapper for format " + formatId + "(class " + mapperClass.getSimpleName()
                            + "): " + ex.getMessage(),
                    ex
            );
        }

        return mapper;
    }


    public FormatSetInstance getFormatSetInstance() {
        return formatSetInstance;
    }


}
