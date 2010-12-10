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

package org.isatools.tablib.schema;

import org.apache.log4j.Logger;
import org.isatools.tablib.exceptions.TabInternalErrorException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


/**
 * May 30, 2007
 *
 * @author brandizi
 */
class SchemaBuilderSAXHandler extends DefaultHandler {
    /**
     * Maps XML element names to corresponding SchemaNode(s)
     */
    protected static final Map<String, Class<SchemaNode>> xml2Node = new HashMap<String, Class<SchemaNode>>();

    /**
     * The FormatSet I return back after XML parsing
     */
    protected FormatSet formatSet;

    protected static final Logger log = Logger.getLogger(SchemaBuilderSAXHandler.class);


    /**
     * The FormatSet I return back after XML parsing
     */
    public FormatSet getFormatSet() {
        return formatSet;
    }

    /**
     * What I am parsing
     */
    protected SchemaNode currentNode = null;

    // ------- Class Initialization ----------

    {
        String classNames[] = new String[]{"FormatSet", "Format", "Section", "Field"};
        for (String className : classNames) {
            try {
                Class<SchemaNode> cls = (Class<SchemaNode>) Class.forName("org.isatools.tablib.schema." + className);
                Method enameM = cls.getDeclaredMethod("getXmlElementName");
                SchemaNode node = cls.newInstance();
                String elName = (String) enameM.invoke(node);
                xml2Node.put(elName, (Class<SchemaNode>) cls);
            }
            catch (Exception ex) {
                throw new TabInternalErrorException("SchemaBuilder, error in initializing schema handlers: " + ex.getMessage());
            }
        }
    }


    public synchronized void startElement(String uri, String localName, String qName, Attributes atts)
            throws SAXException {
        try {
            // Create the node
            Class<SchemaNode> nodeCls = (Class<SchemaNode>) xml2Node.get(qName);

            if (nodeCls == null) {
                throw new SAXException("Syntax error in parsing the schema file: element '" + qName + "' is invalid");
            }

            SchemaNode node = nodeCls.newInstance();

            // Add the attributes
            int natts = atts.getLength();
            for (int i = 0; i < natts; i++) {
                String attName = atts.getQName(i);
                String attVal = atts.getValue(i);
                node.setAttr(attName, attVal);
            }


            // Do I need to attach this to the parent?
            if (node instanceof FormatSet) {
                if (formatSet == null) {
                    currentNode = formatSet = (FormatSet) node;
                } else {
                    throw new TabInternalErrorException("Syntax error in parsing the schema file: FormatSet cannot appear here");
                }
            } else {
                // We are not at root level, add the child and make it current node
                currentNode.addChild(node);
                currentNode = node;
            }

            // TODO: Probably this should be cleaned up., moved elsewhere etc.
            if (node instanceof Field) {
                ((Field) node).setupConstraints();
            }

            log.trace("Schema Builder, node added:\n  " + node);
        }
        catch (Exception ex) {
            throw new SAXException("SchemaBuilder, error in parsing the XML schema definition: " + ex.getMessage(), ex);
        }
    }

    /**
     * Goes back to the parent node
     */
    public synchronized void endElement(String namespaceURI, String localName, String qName)
            throws SAXException {
        currentNode = currentNode.getParent();
    }

}
