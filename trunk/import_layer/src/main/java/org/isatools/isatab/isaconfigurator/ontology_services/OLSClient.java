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

import org.isatools.tablib.exceptions.TabInternalErrorException;
import uk.ac.ebi.bioinvindex.utils.i18n;
import uk.ac.ebi.ook.web.services.Query;
import uk.ac.ebi.ook.web.services.QueryServiceLocator;

import javax.xml.rpc.ServiceException;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.lang.System.out;

/**
 * The client for the EBI's OLS Service. See {@link OntologyLookupClient} for details.
 * this basically uses the SOAP web service and its Java client, available via the EBI's Maven repository.
 *
 * @author brandizi
 *         <b>date</b>: Oct 9, 2009
 */
public class OLSClient extends OntologyLookupClient {
    private Set<String> _ontologySymbols;


    @Override
    public boolean sourceExists(String sourceSymbol) {
        if (sourceSymbol == null) {
            return false;
        }
        return getOntologySymbols().contains(sourceSymbol.toUpperCase());
    }

    @Override
    public boolean termExists(String sourceSymbol, String acc) {
        try {
            QueryServiceLocator locator = new QueryServiceLocator();
            Query service = locator.getOntologyQuery();

            // It seems we have to try different patterns
            //
            String[] accPatterns = new String[]{
                    sourceSymbol + ":" + acc,
                    acc
            };

            for (String accPattern : accPatterns) {
                Map<String, String> data = service.getTermMetadata(accPattern, sourceSymbol);
                if (data != null) {
                    return true;
                }
            }
            return false;
        }
        catch (RemoteException e) {
            log.debug("Internal error while invoking EBI's OLS:" + e.getMessage(), e);
            // throw new TabInternalErrorException ( "Internal error while invoking EBI's OLS:" + e.getMessage (), e );
        }
        catch (ServiceException e) {
            log.debug("Internal error while invoking EBI's OLS:" + e.getMessage(), e);
            // throw new TabInternalErrorException ( "Internal error while invoking EBI's OLS:" + e.getMessage (), e );
        }
        return false;
    }

    @Override
    public Set<String> getTermParentAccessions(String sourceSymbol, String acc) {
        try {
            QueryServiceLocator locator = new QueryServiceLocator();
            Query service = locator.getOntologyQuery();

            // It seems we have to try different patterns
            //
            String[] accPatterns = new String[]{
                    sourceSymbol + ":" + acc,
                    acc
            };

            for (String accPattern : accPatterns) {
                Map<String, String> terms = service.getTermParents(accPattern, sourceSymbol);
                if (terms == null) {
                    continue;
                }

                Set<String> result = new HashSet<String>();
                for (String parentAcc : terms.keySet()) {
                    result.add(super.id2Accession(parentAcc));
                }
                return result;
            }

            return Collections.emptySet();
        }
        catch (RemoteException e) {
            log.debug("Internal error while invoking EBI's OLS:" + e.getMessage(), e);
            // throw new TabInternalErrorException ( "Internal error while invoking EBI's OLS:" + e.getMessage (), e );
        }
        catch (ServiceException e) {
            log.debug("Internal error while invoking EBI's OLS:" + e.getMessage(), e);
            // throw new TabInternalErrorException ( "Internal error while invoking EBI's OLS:" + e.getMessage (), e );
        }
        return Collections.emptySet();
    }

    /**
     * @return a list of ontology symbols present in OLS.
     */
    private Set<String> getOntologySymbols() {
        if (_ontologySymbols != null) {
            return _ontologySymbols;
        }

        _ontologySymbols = new HashSet<String>();

        QueryServiceLocator locator = new QueryServiceLocator();

        try {
            Query service = locator.getOntologyQuery();
            Map<String, String> ontologies = service.getOntologyNames();

            if (ontologies == null) {
                throw new TabInternalErrorException(i18n.msg("ols_no_ontology"));
            }

            _ontologySymbols = ontologies.keySet();
            return _ontologySymbols;
        }
        catch (RemoteException e) {
            log.debug("Internal error while invoking EBI's OLS:" + e.getMessage(), e);
            // throw new TabInternalErrorException ( "Internal error while invoking EBI's OLS:" + e.getMessage (), e );
        }
        catch (ServiceException e) {
            log.debug("Internal error while invoking EBI's OLS:" + e.getMessage(), e);
            // throw new TabInternalErrorException ( "Internal error while invoking EBI's OLS:" + e.getMessage (), e );
        }
        return Collections.emptySet();
    }

    /**
     * TODO: move to a proper Junit test...
     */
    public static void main(String[] args) throws Exception {
        OntologyLookupClient cli = new OLSClient();

        out.println("\n exists: NDFRT:C288711 :" + cli.termExists("NDFRT", "C288711"));
        out.println(cli.getTermParentAccessions("DOID", "169"));
    }


}
