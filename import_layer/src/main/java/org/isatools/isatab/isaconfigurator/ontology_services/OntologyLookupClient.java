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

package org.isatools.isatab.isaconfigurator.ontology_services;

import org.apache.log4j.Logger;
import org.isatools.isatab.isaconfigurator.validators.OntologyValidationManager;

import java.util.Set;

/**
 * A small interface used by the ISAConfigurator validator (namely {@link OntologyValidationManager}) for querying
 * Ontology Lookup services, such as BioPortal or EBI's OLS.
 *
 * @author brandizi
 *         <b>date</b>: Oct 9, 2009
 */
public abstract class OntologyLookupClient {
    protected static final Logger log = Logger.getLogger(OntologyLookupClient.class);

    /**
     * @param sourceSymbol what is used as ontology source symbol, e.g.: MO, OBI, IAO.
     * @return true if the ontology with this symbol is found by the queried ontology service
     */
    public abstract boolean sourceExists(String sourceSymbol);

    /**
     * @param sourceSymbol what is used as ontology source symbol, e.g.: MO, OBI, IAO.
     * @param acc          the term accession, without the prefix, e.g. 12354, OBI_30304.
     * @return true if the term is found by the queried ontology service
     */
    public abstract boolean termExists(String sourceSymbol, String acc);

    /**
     * @param sourceSymbol what is used as ontology source symbol, e.g.: MO, OBI, IAO.
     * @param acc          the term accession, without the prefix, e.g. 12354, OBI_30304.
     * @return a list of accessions of those terms that are direct parents of the queried term. "being parent"
     *         depends on the specific ontology server that is queries, e.g.: it may or not use inference, it may or not
     *         consider other relations than sub-class-of (e.g.: part-of).
     */
    public abstract Set<String> getTermParentAccessions(String sourceSymbol, String acc);

    /**
     * Converts an ID returned by an Ontology Lookup Service into an accession, as intended in the ISATAB world,
     * for example it converts MFO:20304 into 20304. This should be used to deal with different result formats
     * provided by different services.
     */
    protected static String id2Accession(String id) {
        if (id == null) {
            return null;
        }
        int isep = id.indexOf(':');
        if (isep == -1) {
            return id;
        }
        return id.substring(isep + 1);
    }

}
