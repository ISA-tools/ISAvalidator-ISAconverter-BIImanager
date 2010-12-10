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
package org.isatools.tablib.export.graph_algorithm;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>The skeleton node factory. You'll probably want to create nodes from a factory. In fact, the typical
 * implementation goes like this: your nodes are wrappers of experimental entities of your object model
 * (eg.: BioSource or ProtocolApplication). Such wrappers are defined by extending {@link DefaultAbstractNode}.
 * That said, when you need to create a new node, which wraps a particular object from your specific model,
 * you will want to check if such a wrapper has already been created. This is is done inside this factory, by
 * maintaining a map between your objects (the generic type B) an the wrapping {@link Node}s.</p>
 * <p/>
 * <p>The reason why you typically need wrappers is that if two wrapped elements are equivalent (eg.: the same biosample),
 * we still must be able to duplicate them and typically your equals() and hashCode() would prevent from doing that.</p>
 * <p/>
 * <p>For an example of what we're talking about here, see the implementation in
 * org.isatools.isatab.export.isatab.pipeline.wrapper_nodes.</p>
 * <p/>
 * <dl><dt>date</dt><dd>May 10, 2010</dd></dl>
 *
 * @author brandizi
 * @param <N> the type of specific node that the factory provides.
 * @param <B> the base element that the nodes created here wrap.
 */
public abstract class AbstractNodeFactory<N extends Node, B> {
    private Map<B, Node> bases2Nodes = new HashMap<B, Node>();

    /**
     * This is to be used during the initial creation of nodes to be passed to {@link TableBuilder}. The method
     * ensures that the same node is used for the same wrapped element.
     */
    public synchronized N getNode(B base) {
        Node n = bases2Nodes.get(base);
        if (n != null) {
            return (N) n;
        }

        n = createNewNode(base);
        bases2Nodes.put(base, n);
        return (N) n;
    }

    /**
     * Your specific creation method goes here
     */
    protected abstract Node createNewNode(B base);

}
