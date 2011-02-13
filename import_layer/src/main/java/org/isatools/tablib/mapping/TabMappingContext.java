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

import org.isatools.tablib.exceptions.TabInternalErrorException;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.model.Identifiable;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * TODO: It's a dirty solution to use {@link BIIObjectStore} as a context container, in addition to a container for
 * mapped objects. We actually have to clean up this and pass a context around, where getStore() is available,
 * not viceversa.
 *
 * @author brandizi
 *         <b>date</b>: Apr 29, 2009
 */
public class TabMappingContext extends Identifiable implements Map<String, Object> {
    private Map<String, Object> context = new HashMap<String, Object>();
    private final static String UNSUPPORTED_METHOD_ERROR =
            "TabMappingContext is only provisionally an Identifiable due to compatibility problems, but you should not"
                    + " use Identifiable's methods";


    public void clear() {
        context.clear();
    }

    public boolean containsKey(Object key) {
        return context.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return context.containsValue(value);
    }

    public Set<java.util.Map.Entry<String, Object>> entrySet() {
        return context.entrySet();
    }

    public boolean equals(Object o) {
        return context.equals(o);
    }

    public Object get(Object key) {
        return context.get(key);
    }

    public int hashCode() {
        return context.hashCode();
    }

    public boolean isEmpty() {
        return context.isEmpty();
    }

    public Set<String> keySet() {
        return context.keySet();
    }

    public Object put(String key, Object value) {
        return context.put(key, value);
    }

    public void putAll(Map<? extends String, ? extends Object> t) {
        context.putAll(t);
    }

    public Object remove(Object key) {
        return context.remove(key);
    }

    public int size() {
        return context.size();
    }

    public Collection<Object> values() {
        return context.values();
    }


    @Override
    public Long getId() {
        throw new TabInternalErrorException(UNSUPPORTED_METHOD_ERROR);
    }

    @Override
    public Timestamp getSubmissionTs() {
        throw new TabInternalErrorException(UNSUPPORTED_METHOD_ERROR);
    }

    @Override
    public void setId(Long id) {
        throw new TabInternalErrorException(UNSUPPORTED_METHOD_ERROR);
    }

    @Override
    public void setSubmissionTs(Timestamp submissionTs) {
        throw new TabInternalErrorException(UNSUPPORTED_METHOD_ERROR);
    }

}
