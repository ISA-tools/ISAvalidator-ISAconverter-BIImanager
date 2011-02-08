/*

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

package org.isatools.tablib.utils;

import org.apache.commons.lang.StringUtils;
import uk.ac.ebi.bioinvindex.model.term.*;

import java.util.Collection;

public class Utils {
    private Utils() {
    }

    /**
     * Tells whether two property values are to be considered the same or not. This will possibly be removed
     * when proper equivalence criteria are implemented in the model.
     * @param <PT>
     * @param <PT>
     */
    public static <PV extends PropertyValue<?>> boolean equal(PV pv1, PV pv2) {
        if (pv1 == null) {
            return pv2 == null;
        }
        if (pv2 == null) {
            return pv1 == null;
        }

        Property<?> pt1 = pv1.getType(), pt2 = pv2.getType();
        if (pt1 == null) {
            if (pt2 != null) {
                return false;
            }
        } else if (pt2 == null) {
            if (pt1 != null) {
                return false;
            }
        } else if (!StringUtils.equals(pt1.getValue(), pt2.getValue())) {
            return false;
        }

        if (!StringUtils.equals(pv1.getValue(), pv2.getValue())) {
            return false;
        }

        UnitValue uv1 = pv1.getUnit(), uv2 = pv2.getUnit();
        if (uv1 == null) {
            if (uv2 != null) {
                return false;
            }
        } else if (uv2 == null) {
            if (uv1 != null) {
                return false;
            }
        } else {
            if (!StringUtils.equals(uv1.getValue(), uv2.getValue())) {
                return false;
            }
            Unit u1 = uv1.getType(), u2 = uv2.getType();
            if (u1 == null) {
                if (u2 != null) {
                    return false;
                }
            } else if (u2 == null) {
                if (u1 != null) {
                    return false;
                }
            } else if (!StringUtils.equals(u1.getValue(), u2.getValue())) {
                return false;
            }
        }

        Collection<OntologyTerm> ots1 = pv1.getOntologyTerms(), ots2 = pv2.getOntologyTerms();
        if (ots1.size() != ots2.size()) {
            return false;
        }

        for (OntologyTerm ot1 : ots1) {
            if (!ots2.contains(ot1)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Tells if a property value is inside a collection, by using {@link #equal(PropertyValue, PropertyValue)}.
     * This will possibly be removed when proper equivalence criteria are implemented in the model.
     */
    public static <PV extends PropertyValue<?>> boolean contains(Collection<PV> pvals, PV pv) {
        for (PV pvi : pvals) {
            if (equal(pv, pvi)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Splits a string according to a separator RE. If fixedSize is >= 0 returns an array of the specified
     * size, otherwise just returns the result given by {@link String#split(String)}. If mvalue is null or empty string,
     * returns an array of nulls (zero-sized or fixedSize sized). mvalue is first {@link String#trim()}ed before anything
     * else.
     */
    public static String[] splitMultiValue(String mvalue, String sepRe, int fixedSize) {
        mvalue = StringUtils.trimToNull(mvalue);
        if (mvalue == null) {
            return fixedSize <= 0 ? new String[0] : new String[fixedSize];
        }

        String chunks[] = mvalue.split(sepRe);
        if (fixedSize < 0) {
            return chunks;
        }

        if (fixedSize == chunks.length) {
            return chunks;
        }

        String result[] = new String[fixedSize];
        for (int i = 0; i < Math.min(fixedSize, chunks.length); i++) {
            result[i] = chunks[i];
        }
        return result;
    }

    /**
     * A wrapper with fixedSize == -1
     */
    public static String[] splitMultiValue(String mvalue, String sepRe) {
        return splitMultiValue(mvalue, sepRe, -1);
    }

//	/**
//	 * Logs the message and sets the validation result as {@link GUIInvokerResult#WARNING}. Does that by 
//	 * TODO: taking the context from some static variable (to be defined) and setting the "validation.result" 
//	 * key in it.
//	 *   
//	 */
//	public static void logValidationWarning ( Logger log, String msg ) {
//		log.warn ( msg );
//		store.valueOfType ( TabMappingContext.class ).put ( "validation.result", GUIInvokerResult.WARNING );
//	}
//
//	/**
//	 * Like {@link #logValidationWarning(BIIObjectStore, Logger, String)}, but logs an error instead.
//	 */
//	public static void logValidationError ( Logger log, String msg ) {
//		log.error ( msg );
//		store.valueOfType ( TabMappingContext.class ).put ( "validation.result", GUIInvokerResult.WARNING );
//	}

}
