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

package org.isatools.isatab.isaconfigurator.validators;

import org.isatools.isatab.configurator.schema.FieldType;
import org.isatools.isatab.isaconfigurator.ISAConfigurationSet;
import org.isatools.isatab.isaconfigurator.ontology_services.OntologyLookupClient;
import org.isatools.isatab.isaconfigurator.ontology_services.OntologyLookupClientFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Some facilities for performing the Ontology Validation in the ISAConfigurator validation of the ISA Import Layer.
 *
 * @author brandizi
 *         <b>date</b>: Oct 9, 2009
 */
public class OntologyValidationManager {
    private Map<String, Set<String>> configuredSourceCache = new HashMap<String, Set<String>>();
    private Map<String, Boolean> termExistenceCache = new HashMap<String, Boolean>();
    private Map<String, Boolean> existingSourceCache = new HashMap<String, Boolean>();
    private Map<String, Boolean> branchMatchingCache = new HashMap<String, Boolean>();

    private OntologyLookupClient ontoClient = OntologyLookupClientFactory.newInstance();

    /**
     * Checks that the ontology source used for a field corresponds to the one specified for it in the
     * configuration.
     *
     * @param sourceAcc the source
     * @param cfgField  the ISA configuration for the field
     */
    public boolean validateConfiguredOntologySource(String sourceAcc, FieldType cfgField) {
        String header = cfgField.getHeader().toLowerCase();
        Set<String> sources = configuredSourceCache.get(header);
        if (sources == null) {
            sources = ISAConfigurationSet.getOntologySourceAbbreviations(cfgField);
            configuredSourceCache.put(header, sources);
        }
        return sources.contains(sourceAcc);
    }

    /**
     * Checks that a term exists, according to what is returned by the underlining Ontology Lookup Service.
     */
    public boolean validateTermExistence(String source, String acc) {
        if (!sourceExists(source)) {
            return false;
        }

        String termStr = source + ":" + acc;

        {
            Boolean result = termExistenceCache.get(termStr);
            if (result != null) {
                return result;
            }
        }

        {
            boolean result = ontoClient.termExists(source, acc);
            termExistenceCache.put(termStr, result);
            return result;
        }
    }

    /**
     * Checks that a term is a subclass of a branch that is configured for its field.
     *
     * @param source   the term ontology source
     * @param acc      the term accession
     * @param cfgField the ISA configuration for the term's field
     * @return true if the field has no configured root terms (so called branches), false if it has branches and the
     *         term does not belong in any of them.
     */
    public boolean validateBranchMatching(String source, String acc, FieldType cfgField) {
        String header = cfgField.getHeader().toLowerCase();
        String cacheKey = source + ":" + acc + "\\\\" + header;
        Boolean cachedResult = branchMatchingCache.get(cacheKey);
        if (cachedResult != null) {
            return cachedResult;
        }

        Set<String> branches = ISAConfigurationSet.getBranchIds(cfgField).get(source);
        if (branches == null || branches.isEmpty()) {
            branchMatchingCache.put(cacheKey, true);
            return true;
        }
        ;

        boolean result = validateBranchMatching(source, acc, branches, new HashSet<String>());
        branchMatchingCache.put(cacheKey, result);
        return result;
    }

    /**
     * This is used by {@link #validateBranchMatching(String, String, FieldType)} for walking the ontology graph from
     * the initial term to match back to its roots.
     */
    private boolean validateBranchMatching(String source, String acc, Set<String> branchIds, Set<String> visitedNodes) {
        // It's rare, but loops can occur in the ontology (I guess because of equivalence relations)
        if (visitedNodes.contains(acc)) {
            return false;
        }
        visitedNodes.add(acc);

        if (branchIds.contains(acc)) {
            return true;
        }

        for (String parentId : ontoClient.getTermParentAccessions(source, acc)) {
            if (validateBranchMatching(source, parentId, branchIds, visitedNodes)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tells if a source exists in the available Ontology Lookup Services. This method caches the results.
     */
    public boolean sourceExists(String source) {
        Boolean cachedValue = existingSourceCache.get(source);
        if (cachedValue != null) {
            return cachedValue;
        }

        boolean result = ontoClient.sourceExists(source);
        existingSourceCache.put(source, result);
        return result;
    }
}