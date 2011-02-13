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

import org.apache.commons.lang.StringUtils;
import org.isatools.tablib.exceptions.TabInternalErrorException;
import org.isatools.tablib.exceptions.TabInvalidValueException;
import org.isatools.tablib.schema.constraints.FieldCardinalityConstraint;
import org.isatools.tablib.schema.constraints.FieldConstraint;
import org.isatools.tablib.schema.constraints.FollowsConstraint;
import org.isatools.tablib.schema.constraints.PrecedesConstraint;
import uk.ac.ebi.utils.regex.RegEx;

import java.util.*;
import java.util.regex.Pattern;

/**
 * The top class used to represent a TAB description format.
 * <p/>
 * TODO: the isCaseSensitive flag appears in few methods only, the ones we have needed so far. We should provide this
 * option wherever appropriate.
 * <p/>
 * May 25, 2007
 *
 * @author brandizi
 */
public class Field extends SchemaNode implements Cloneable {

    int index = -1;
    private FieldConstraint[] constraints = new FieldConstraint[0];


    /**
     * How we match an header, either in X or in X[Y](Z), X,Y,Z must match this pattern
     */
    public static final String ID_PATTERN = "[\\w_]+(?: *[\\w/_\\-\\: ]+)? *";

    public Field() {
    }


    /**
     * The ID is the first part of the header, for instance for Factor[Time](h), ID is
     * Factor. Unfortunately it is not unique after having parsed a real TAB, you must use
     * the position index for that.
     */
    public Field(String id) {
        super(id);
    }


    public Section getSection() {
        return (Section) getParent();
    }

    public void setSection(Section parent) {
        this.setParent(parent);
    }


    /**
     * Parses a possibly complex header and sets up corresponding bits in the field, for instance
     * "Factor[Grow Condition](media)" is splitted into Factor, GC, media
     * <p/>
     * Returns the array returned by the RE matcher, with the values:
     * <pre>
     *   0 - the whole parameter string ( "Factor[Grow Condition](media)" )
     *   1 - Factor
     *   2 - Grow Condition
     *   3 - media
     * </pre>
     */
    public static String[] parseHeaderRawResult(String header, boolean isCaseSensitive) {
        header = StringUtils.trimToEmpty(header);
        if (header.length() == 0) {
            throw new TabInvalidValueException("Field.parseHeaderRawResult(): header is empty");
        }

        // REs are our friends here
        //
        String
                // Used to match the whole header
                pattern = "^ *(" + ID_PATTERN + ") *(?:\\[ *(" + ID_PATTERN + ") *\\] *(?:\\( *(" + ID_PATTERN + ") *\\))?)? *$";

        log.trace("Field.parseHeader( '" + header + "' ), using the pattern: '" + pattern + "'");
        String bits[] = new RegEx(pattern, isCaseSensitive ? 0 : Pattern.CASE_INSENSITIVE).groups(header);
        log.trace(String.format("Field.parseHeaderRawResult('%s'), bits are: %s", header, Arrays.toString(bits)));
        if (bits == null || bits.length < 2) {
            throw new TabInvalidValueException("Field.parseHeader(): bad syntax for the header: " + header);
        }

        return bits;
    }

    /**
     * Case-sensitive version of {@link #parseHeaderRawResult(String)}
     */
    public static String[] parseHeaderRawResult(String header) {
        return parseHeaderRawResult(header, true);
    }


    /**
     * Parses a possibly complex header and sets up corresponding bits in the field, for instance
     * "Factor[Grow Condition](media)" is splitted into id = Factor, type = GC, type1 = media
     * <p/>
     * Pass the col parameter to store the column in the TAB where this header was matched.
     * <p/>
     * It gives back a clone of this field, changed according to header and pos. It DOES NOT change
     * the current field.
     * <p/>
     * TODO: Do we need type/subType properties?
     */
    public Field parseHeader(String header, int col, boolean isCaseSensitive) {
        String[] bits = parseHeaderRawResult(header, isCaseSensitive);

        if (bits == null || bits.length < 2) {
            throw new TabInvalidValueException("Field.parseHeader(): bad syntax for the header: " + header);
        }


        // Check the field ID
        //
        String id = this.getAttr("id");
        String newid = bits[1].trim();

        if (!(newid.equals(id) || (!isCaseSensitive && newid.equalsIgnoreCase(id)))) {
            throw new TabInvalidValueException(String.format(
                    "Field.parseHeader(): Cannot fill the field '%s' with the mismatching header: '%s'",
                    id, newid
            ));
        }

        // Build up a new field from the template copy
        Field clone = this.clone();

        // Rebuild the header, we need it with the case that has been defined
        String builtHeader = id;

        // Setup the type
        if (bits.length > 2 && bits[2] != null) {
            String type = bits[2].trim();
            clone.setAttr("type", type);
            builtHeader += " [" + type + "]";
        }

        // Setup the sub-type
        if (bits.length > 3 && bits[3] != null) {
            String type1 = bits[3].trim();
            clone.setAttr("type1", type1);
            builtHeader += " (" + type1 + ")";
        }

        clone.setAttr("header", builtHeader);

        // Track back the position
        clone.setIndex(col);

        return clone;
    }

    /**
     * Case-sensitive version of {@link #parseHeader(String, int)}
     */
    public Field parseHeader(String header, int col) {
        return parseHeader(header, col, true);
    }

    /**
     * Initializes the field's constraints with what it was found in the XML, i.e.: this method *must* be called
     * after having set attributes. Currently this is used in {@link SchemaBuilderSAXHandler}.
     */
    protected void setupConstraints() {
        List<FieldConstraint> result = new LinkedList<FieldConstraint>();

        FieldCardinalityConstraint cardCons = FieldCardinalityConstraint.parseConstraint(this);
        if (cardCons != null) {
            result.add(cardCons);
        }

        PrecedesConstraint precedesConss[] = PrecedesConstraint.parseConstraints(this);
        if (precedesConss != null) {
            result.addAll(Arrays.asList(precedesConss));
        }

        FollowsConstraint followsConss[] = FollowsConstraint.parseConstraints(this);
        if (followsConss != null) {
            result.addAll(Arrays.asList(followsConss));
        }

        this.setConstraints(result.toArray(new FieldConstraint[0]));

    }


    /**
     * The index in the in-memory representation of the TAB this (real) field is linked to
     * -1 if the index has not yet been assigned.
     */
    public int getIndex() {
        return index;
    }


    /**
     * The index in the in-memory representation of the TAB this (real) field is linked to
     * Is -1 if the index has not yet been assigned.
     */
    public void setIndex(int idx) {
        this.index = idx;
    }


    public String getXmlElementName() {
        return "field";
    }


    /**
     * Creates a copy of a field, so that new fields may be generated while parsing
     * Attaches the new field to the same section of the original one.
     */
    public Field clone() {
        Field clone;
        try {
            clone = (Field) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new TabInternalErrorException("Field.clone(): Uhhm, so strange, looks like object.clone() doesn't work");
        }

        clone.index = this.index;
        Map<String, String> attrs = new HashMap<String, String>();
        Map<String, String> oldAttrs = this.getAttrs();
        if (oldAttrs != null) {
            attrs.putAll(oldAttrs);
            clone.setAttrs(attrs);
        }

        if (this.constraints != null) {
            clone.constraints = new FieldConstraint[this.constraints.length];
            for (int i = 0; i < this.constraints.length; i++) {
                clone.constraints[i] = this.constraints[i];
            }
        }
        return clone;
    }


    @Override
    public void addChild(SchemaNode child) {
        throw new TabInternalErrorException("Cannot add children to a field");
    }


    @Override
    public void setChildren(List<SchemaNode> children) {
        throw new TabInternalErrorException("Cannot add children to a field");
    }

    /**
     * Get the constraints for this field, as they have been defined in the TAB definition XML file.
     */
    public FieldConstraint[] getConstraints() {
        return constraints;
    }


    public void setConstraints(FieldConstraint[] constraints) {
        this.constraints = constraints;
    }


    public StringBuilder dump() {
        String type = getAttr("type");
        String type1 = getAttr("type1");
        StringBuilder result = new StringBuilder(getId());
        if (type != null && type.length() != 0) {
            result.append("[" + type + "]");
        }
        if (type1 != null && type1.length() != 0) {
            result.append("(" + type1 + ")");
        }
        return result;
    }


}
