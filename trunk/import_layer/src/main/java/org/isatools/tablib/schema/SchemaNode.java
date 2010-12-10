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

import java.util.*;


/**
 * This is the generic node that is used to represent a node in a TAB schema.
 * WARNING: I assume that children are never null and I don't check this (TODO: yet).
 * <p/>
 * May 23, 2007
 *
 * @author brandizi
 */
public class SchemaNode {
    protected static final Logger log = Logger.getLogger(SchemaNode.class);

    public SchemaNode() {
    }

    public SchemaNode(String id) {
        setAttr("id", id);
    }

    private SchemaNode parent;

    /**
     * My parent if any
     */
    public SchemaNode getParent() {
        return this.parent;
    }

    /**
     * My parent if any
     */
    public void setParent(SchemaNode parent) {
        this.parent = parent;
    }

    // TODO: generics for on ChildType?
    private List<SchemaNode> children = new ArrayList<SchemaNode>();

    /**
     * My children
     */
    public List<SchemaNode> getChildren() {
        return children;
    }

    /**
     * My children, automatically call setParent().
     */
    public void setChildren(List<SchemaNode> children) {
        this.children = new ArrayList<SchemaNode>();
        for (SchemaNode child : children) {
            this.addChild(child);
        }
    }

    /**
     * My children, automatically call setParent()
     */
    public void addChild(SchemaNode child) {
        this.children.add(child);
        child.setParent(this);
    }

    /**
     * Lookup a children by id. I always expect that all che nodes at a given level has distinct IDs.
     */
    public SchemaNode getChild(String id, boolean isCaseSensitive) {
        if (id == null) {
            throw new TabInternalErrorException("SchemaNode.getChild (), id is null");
        }

        for (SchemaNode child : children) {
            if (isCaseSensitive && id.equals(child.getAttr("id")) || id.equalsIgnoreCase(child.getAttr("id"))) {
                return child;
            }
        }
        return null;
    }


    public SchemaNode getChild(String id) {
        return getChild(id, true);
    }


    /**
     * Filters children by type (class)
     */
    @SuppressWarnings("unchecked")
    public <NodeType extends SchemaNode> List<NodeType> getChildren(Class<? extends NodeType> cls) {
        if (cls == null) {
            throw new TabInternalErrorException("SchemaNode.getChildren (), class is null");
        }

        List<NodeType> rval = new ArrayList<NodeType>();
        for (SchemaNode child : children) {
            if (cls.isInstance(child)) {
                rval.add((NodeType) child);
            }
        }
        return rval;
    }


    /**
     * Lookup a descendant with a given ID. If more than one, return the first (in depth-first fashion)
     */
    public SchemaNode getDescendant(String id) {
        List<SchemaNode> rval = getDescendants(id);
        return rval.size() == 0 ? null : rval.get(0);
    }

    /**
     * Returns all descenants with a given ID (depth-first order)
     */
    public List<SchemaNode> getDescendants(String id) {
        List<SchemaNode> descs = getDescendants();
        List<SchemaNode> rval = new ArrayList<SchemaNode>();
        if (rval == null) {
            return null;
        }

        for (SchemaNode node : descs) {
            if (id.equals(node.getAttr("id"))) {
                rval.add(node);
            }
        }
        return rval;
    }

    /**
     * All my descendants in depth-first order
     */
    public List<SchemaNode> getDescendants() {
        List<SchemaNode> rval = new ArrayList<SchemaNode>();
        for (SchemaNode child : children) {
            rval.add(child);
            rval.addAll(child.getDescendants());
        }
        return rval;
    }

    /**
     * All descendants of a given type (depth-first)
     */
    public List<SchemaNode> getDescendants(Class<?> cls) {
        List<SchemaNode> descs = getDescendants();
        List<SchemaNode> rval = new ArrayList<SchemaNode>();
        if (rval == null) {
            return null;
        }

        for (SchemaNode node : descs) {
            if (cls.isInstance(node)) {
                rval.add(node);
            }
        }

        return rval;
    }


    /**
     * All descendants of a given type and with a given id (depth-first)
     */
    public <NodeType extends SchemaNode> List<NodeType> getDescendants(String id, Class<? extends NodeType> cls) {
        List<SchemaNode> descs = getDescendants();
        List<NodeType> rval = new ArrayList<NodeType>();
        if (rval == null) {
            return null;
        }

        for (SchemaNode node : descs) {
            if (cls.isInstance(node) && id.equals(node.getAttr("id"))) {
                rval.add((NodeType) node);
            }
        }

        return rval;
    }


    /**
     * Lookup a descendant with a given ID and type. If more than one, return the first (in depth-first fashion)
     */
    public <NodeType extends SchemaNode> NodeType getDescendant(String id, Class<NodeType> cls) {
        List<NodeType> rval = getDescendants(id, cls);
        return rval.size() == 0 ? null : rval.get(0);
    }


    private Map<String, String> attrs = new HashMap<String, String>();

    public void setAttr(String name, String value) {
        attrs.put(name, value);
    }

    public String getAttr(String name) {
        return (String) attrs.get(name);
    }

    public Map<String, String> getAttrs() {
        return this.attrs;
    }

    public void setAttrs(Map<String, String> attrs) {
        this.attrs = attrs;
    }


    /**
     * @return toString ( false )
     */
    public String toString() {
        return toString(false);
    }

    /**
     * @return the serialization of the whole tree (if recurse == true) or the single node
     */
    public String toString(boolean recurse) {
        StringBuilder rval = new StringBuilder("<" + getXmlElementName());
        Map<String, String> attrs = getAttrs();
        Set<String> keys = attrs.keySet();
        for (String key : keys) {
            rval.append(" " + key + " = \"" + attrs.get(key) + "\"");
        }

        if (recurse) {
            rval.append(">\n");
            for (SchemaNode child : getChildren()) {
                rval.append(child.toString(true) + "\n");
            }
            rval.append("</" + getXmlElementName() + ">");
        } else {
            rval.append("/>\n");
        }

        return rval.toString();
    }


    public String getXmlElementName() {
        return "node";
    }


    /**
     * An helper for id attribute
     *
     * @return {@link #getAttr(String) getAttr ( "id" )}
     */
    public String getId() {
        return getAttr("id");
    }

    /**
     * An helper for {@link #setAttr(String, String) setAttr ( "id", id )}
     */
    public void setId(String id) {
        this.setAttr("id", id);
    }

}
