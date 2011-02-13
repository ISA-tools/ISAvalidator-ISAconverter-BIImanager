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

package org.isatools.tablib.utils.logging;

import org.apache.log4j.NDC;
import org.apache.ojb.broker.metadata.torque.TableDescriptor;
import org.isatools.isatab.mapping.AssayGroup;
import org.isatools.tablib.exceptions.TabInternalErrorException;
import org.isatools.tablib.schema.FormatInstance;
import org.isatools.tablib.schema.SectionInstance;
import uk.ac.ebi.bioinvindex.model.Identifiable;
import uk.ac.ebi.bioinvindex.model.Investigation;
import uk.ac.ebi.bioinvindex.model.Material;
import uk.ac.ebi.bioinvindex.model.Study;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * This is an {@link NDC} that is specialized in reporting the context we are during the process of an TAB format and
 * related operations, such as mapping or exporting.
 * <p/>
 * It actually maintains two different independent stacks, one for tracing the structure in the TAB that is being processed,
 * and another one to trace the objects in the BII model that are being processed.
 * <p/>
 * As the Log4j's NDC, there is one TabNDC per thread. Differently than the original Log4j NDC, you must {@link #getInstance()}
 * to use the TabNDC methods (I think the singleton pattern is cleaner).
 * <p/>
 * Moreover, the class keeps {@link NDC} synchronized, so that log4j appenders report automatically the information managed
 * here.
 * <p/>
 * TODO: performance is still to be tuned.
 * <p/>
 * <dl><dt>date:</dt><dd>Mar 31, 2009</dd></dl>
 *
 * @author brandizi
 */
public class TabNDC {
    private TabNDC() {
    }

    /**
     * Delegating synchronization to {@link Hashtable} is enough here.
     */
    private static Map<Thread, TabNDC> instances = new Hashtable<Thread, TabNDC>();

    private Stack<TabLogDescriptor> tabDescriptorStack = new Stack<TabLogDescriptor>();
    private final Stack<Identifiable> objectStack = new Stack<Identifiable>();


    /**
     * One instance per thread. There is internal management of synchronization, so all should be thread-safe.
     */
    public static TabNDC getInstance() {
        Thread thread = Thread.currentThread();
        TabNDC instance = instances.get(thread);
        if (instance == null) {
            instance = new TabNDC();
            instances.put(thread, instance);
        }
        return instance;
    }

    /**
     * Generic push of a TAB descriptor
     */
    private void pushTabDescriptor(TabLogDescriptor descr) {
        tabDescriptorStack.push(descr);
        NDC.push(descr.toString());
    }

    /**
     * push is type-specific, pop returns the generic type
     */
    public void pushFormat(String name, String label, String path) {
        pushTabDescriptor(new FormatLogDescriptor(name, label, path));
    }

    /**
     * push is type-specific, pop returns the generic type
     */
    public void pushFormat(FormatInstance formatInstance) {
        pushTabDescriptor(new FormatLogDescriptor(formatInstance));
    }


    /**
     * push is type-specific, pop returns the generic type
     */
    public void pushSection(String name, String sectionHeader, int startingLineIndex) {
        pushTabDescriptor(new SectionLogDescriptor(name, sectionHeader, startingLineIndex));
    }

    /**
     * push is type-specific, pop returns the generic type
     */
    public void pushSection(SectionInstance sectionInstance) {
        pushTabDescriptor(new SectionLogDescriptor(sectionInstance));
    }

    /**
     * push is type-specific, pop returns the generic type
     */
    public void pushRecord(String value, int row, int col) {
        pushTabDescriptor(new RecordLogDescriptor(value, row, col));
    }

    /**
     * The one on top of the stack
     */
    public TabLogDescriptor getTabDescriptor() {
        if (tabDescriptorStack.isEmpty()) {
            throw new TabInternalErrorException("TabNDC, call to getTabDescriptor(), with empty stack");
        }
        return tabDescriptorStack.peek();
    }

    ;

    /**
     * push is type-specific, pop returns the generic type
     */
    public TabLogDescriptor popTabDescriptor() {
        if (tabDescriptorStack.isEmpty()) {
            throw new TabInternalErrorException("TabNDC, call to popTabDescriptor(), with empty stack");
        }
        NDC.pop();
        return tabDescriptorStack.pop();
    }

    ;

    /**
     * i.e.: all the current NDC about the tab descriptors (returns a clone of it)
     */
    @SuppressWarnings("unchecked")
    public List<TabLogDescriptor> getTabDescriptors() {
        return (Stack<TabLogDescriptor>) tabDescriptorStack.clone();
    }

    /**
     * Pushes an object in the object stack (which is independent from the {@link TableDescriptor} stack
     */
    public void pushObject(Identifiable object) {
        objectStack.push(object);
        NDC.push(getObjectDescription(objectStack.peek()));
    }

    /**
     * Pops the object in the object stack
     */
    public Identifiable popObject() {
        if (objectStack.isEmpty()) {
            throw new TabInternalErrorException("TabNDC, popObject() without any object pushed in current logging context.");
        }
        NDC.pop();
        return objectStack.pop();
    }

    /**
     * The one on the top
     */
    public Identifiable getObject() {
        return objectStack.peek();
    }

    /**
     * i.e.: all the NDC about the objects (returns a clone)
     */
    @SuppressWarnings("unchecked")
    public List<Identifiable> getObjectStack() {
        return (List<Identifiable>) objectStack.clone();
    }


    /**
     * Returns a description of all objects in the list, by using {@link #getObjectDescription(Identifiable)}
     */
    public static String getObjectDescriptions(List<Identifiable> objects) {
        StringBuilder result = new StringBuilder("");
        for (Identifiable o : objects) {
            result.append(getObjectDescription(o));
        }
        return result.toString();
    }


    /**
     * A string description of a BII model object. It simply reports information in the form type:&lt;summary&gt;,
     * e.g.: study:BII-S-1.
     */
    public static String getObjectDescription(Identifiable o) {
        String type, value;
        if (o instanceof Investigation) {
            type = "investigation";
            value = ((Investigation) o).getAcc();
        } else if (o instanceof Study) {
            type = "study";
            value = ((Study) o).getAcc();
        } else if (o instanceof AssayGroup) {
            type = "assay file";
            AssayGroup ag = (AssayGroup) o;
            value = ag.getFilePath();
        } else if (o instanceof Material) {
            type = "material";
            value = ((Material) o).getAcc();
        } else {
            type = o.getClass().getSimpleName();
            value = o.toString();
        }

        return type + ":" + value;
    }

}

