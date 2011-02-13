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
import org.apache.xerces.jaxp.DocumentBuilderFactoryImpl;
import org.apache.xpath.XPathAPI;
import org.isatools.tablib.exceptions.TabInternalErrorException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.SAXException;
import uk.ac.ebi.bioinvindex.utils.i18n;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.*;

import static java.lang.System.out;

/**
 * The client for the BioPortal Service. See {@link OntologyLookupClient} for details.
 *
 * @author brandizi
 *         <b>date</b>: Oct 9, 2009
 */
public class BioPortalClient extends OntologyLookupClient {
    private Map<String, String> _ontologyIdsCache = null;
    protected static final Logger log = Logger.getLogger(BioPortalClient.class);

    /**
     * Gives a map of Symbol=>BioPortalID. It is cached.
     */
    private Map<String, String> getOntologyIds() {
        if (_ontologyIdsCache != null) {
            return _ontologyIdsCache;
        }

        _ontologyIdsCache = new HashMap<String, String>();
        try {
            Document dom = callREST("http://rest.bioontology.org/bioportal/ontologies");
            if (dom == null) {
                return _ontologyIdsCache;
            }

            NodeIterator ontoItr = XPathAPI.selectNodeIterator(dom, "success/data/list/ontologyBean");
            for (Node node = ontoItr.nextNode(); node != null; node = ontoItr.nextNode()) {
                String abbr = getTextNode(node, "abbreviation");
                if (abbr == null) {
                    continue;
                }

                String id = getTextNode(node, "ontologyId");
                if (id == null) {
                    continue;
                }

                _ontologyIdsCache.put(abbr, id);
            }

        }
        catch (TransformerException e) {
            log.debug("Internal error while invoking BioPortal Service:" + e.getMessage(), e);
            // throw new TabInternalErrorException ( "Internal error while invoking BioPortal Service:" + e.getMessage (), e );
        }
        return _ontologyIdsCache;
    }


    @Override
    public boolean sourceExists(String sourceSymbol) {
        return getOntologyIds().get(sourceSymbol) != null;
    }


    @Override
    public boolean termExists(String source, String acc) {
        if (source == null || acc == null) {
            return false;
        }

        Map<String, String> oids = getOntologyIds();
        String oid = oids.get(source);
        if (oid == null) {
            return false;
        }

        try {
            // It seems we have to try different combinations
            //
            final String prefix = "http://rest.bioontology.org/bioportal/virtual/ontology/";
            final String[] urls = new String[]{
                    prefix + oid + "/" + source + ":" + acc,
                    prefix + oid + "/" + acc,
                    prefix + oid + "/" + source.toLowerCase() + ":" + acc
            };

            for (String url : urls) {
                Document dom = callREST(url);
                if (dom == null) {
                    continue;
                }

                NodeIterator clsItr = XPathAPI.selectNodeIterator(dom, "success/data/classBean");
                Node node = clsItr.nextNode();
                if (node == null) {
                    continue;
                }

                Map<String, String> result = new HashMap<String, String>();

                return getTextNode(node, "id") != null;
            }

        }
        catch (TransformerException e) {
            log.debug("Internal error while invoking BioPortal Service:" + e.getMessage(), e);
            // throw new TabInternalErrorException ( "Internal error while invoking BioPortal Service:" + e.getMessage (), e );
        }
        return false;

    }


    @Override
    public Set<String> getTermParentAccessions(String source, String acc) {
        if (source == null || acc == null) {
            return null;
        }

        Map<String, String> oids = getOntologyIds();
        String oid = oids.get(source);
        if (oid == null) {
            return Collections.emptySet();
        }

        try {
            // It seems we have to try different combinations
            //
            final String prefix = "http://rest.bioontology.org/bioportal/virtual/parents/";

            final String[] urls = new String[]
                    {
                            prefix + oid + "/" + source + ":" + acc,
                            prefix + oid + "/" + acc,
                            prefix + oid + "/" + source.toLowerCase() + ":" + acc
                    };

            for (String url : urls) {
                url += "?level=1";

                Document dom = callREST(url);
                if (dom == null) {
                    continue;
                }

                NodeIterator parentItr = XPathAPI.selectNodeIterator(dom, "success/data/list/classBean");

                Set<String> result = new HashSet<String>();
                for (Node node = parentItr.nextNode(); node != null; node = parentItr.nextNode()) {
                    String id = getTextNode(node, "id");
                    if (id == null) {
                        log.debug("I found a null parent of " + source + ":" + acc);
                        continue;
                    }
                    result.add(id2Accession(id));
                }
                return result;
            }


        }
        catch (TransformerException e) {

            throw new TabInternalErrorException(i18n.msg("bioportal_invocation_error", e.getMessage()), e);
        }
        return Collections.emptySet();
    }


    private String getTextNode(Node node, String tag) {
        try {
            NodeIterator abbrItr = XPathAPI.selectNodeIterator(node, tag);
            Node anode = abbrItr.nextNode();
            if (anode == null) {
                return null;
            }
            return anode.getTextContent();
        }
        catch (TransformerException e) {
            log.debug("Internal error while invoking BioPortal Service:" + e.getMessage(), e);
            // throw new TabInternalErrorException ( "Internal error while invoking BioPortal Service:" + e.getMessage (), e );
        }
        return null;
    }

    /**
     * All the requests in BioPortal have to be made via a REST URL and then we need to parse the resulting
     * XML.
     */
    private Document callREST(String url) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactoryImpl.newInstance();

            dbf.setFeature("http://xml.org/sax/features/validation", false);
            dbf.setFeature("http://xml.org/sax/features/namespaces", false);
            dbf.setFeature("http://apache.org/xml/features/validation/schema", false);
            dbf.setFeature("http://apache.org/xml/features/validation/schema-full-checking", false);

            DocumentBuilder db = dbf.newDocumentBuilder();
            Document dom = db.parse(url);

            return dom;
        }
        catch (ParserConfigurationException e) {
            log.debug("Internal error while invoking BioPortal Services:" + e.getMessage(), e);
        }
        catch (SAXException e) {
            log.debug("Internal error while invoking BioPortal Services:" + e.getMessage(), e);
        }
        catch (IOException e) {
            log.debug("Internal error while invoking BioPortal Services:" + e.getMessage(), e);
        }
        return null;
    }


    /**
     * TODO: move to a real Junit test...
     */
    public static void main(String[] args) throws Exception {
        OntologyLookupClient cli = new BioPortalClient();

        out.println("\n exists: NDFRT:C288711 :" + cli.termExists("NDFRT", "C288711"));
        out.println(cli.getTermParentAccessions("DOID", "169"));
    }

}
