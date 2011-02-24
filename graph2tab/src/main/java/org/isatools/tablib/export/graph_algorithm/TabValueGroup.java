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

import java.util.List;

/**
 * <p>A group of header/value pairs that are exported for a particular element in the experimental graph. Eg:
 * Characteristic + its value and the fields about the term source and accession are an example of
 * tab value group.</p>
 * <p/>
 * <p><b>WARNING</b>: The <b>implementor has full responsibility in returning headers in a consistent way</b>.
 * We cannot control the exported order of header groups of the same type that have different tails for
 * different objects. Eg: if you have "organism" and you plan to add "Term Source REF" and "Term Accession",
 * it's far better that you <b>always</b> return these 3 headers, no matter that the respective values for them are
 * empty or not. Otherwise you could get unexpected results. As an example, consider:</p>
 * <p/>
 * <table border = "1" cellspacing = "0">
 * <tr><td>sample name</td> 	<td>organism</td>  <td>term src</td>  <td>term acc</td>     <td>...</td></tr>
 * <tr><td>s1</td>            <td>mus-mus</td>   <td></td>          <td>123</td>          <td></td></tr>
 * <tr><td>s2</td>            <td>human</td>     <td>FMA</td>       <td></td>             <td></td></tr>
 * </table>
 * <p/>
 * <p>if s1 returns only the non empty headers (sample name, term acc) and s2 does the same (sample name, term src),
 * then the exported table will be:</p>
 * <p/>
 * <table border = "1" cellspacing = "0">
 * <tr><td>sample name</td> 	<td>organism</td>  <td>term acc</td>  <td>term src</td>     <td>...</td></tr>
 * <tr><td>s1</td>            <td>mus-mus</td>   <td>123</td>       <td></td>             <td></td></tr>
 * <tr><td>s2</td>            <td>human</td>     <td></td>          <td>FMA</td>          <td></td></tr>
 * </table>
 * <p/>
 * <p>which is wrong if the column order is meaningful for you. This is due to the way table values are inserted in the
 * exported table and at the moment we don't know how to overcome this problem. Good news is it doesn't occur in a case
 * like this:</p>
 * <p/>
 * <table border = "1" cellspacing = "0">
 * <tr><td>temperature</td> 	 <td>unit</td>  <td>term acc</td>  <td>term src</td>     <td>...</td></tr>
 * <tr><td>120</td>            <td></td>      <td></td>          <td></td>             <td></td></tr>
 * <tr><td>240</td>            <td>C</td>     <td></td>         <td>UO</td>            <td></td></tr>
 * </table>
 * <p/>
 * <p>Here, you are free to report [unit, acc, src] only when you return the table group for the second temperature.
 * The important thing is that you provide all the three headers together. The exporter will add the unit columns to
 * the temperature correctly in the second case. In other words, <b>headers that goes together must be always all
 * exported, even if they have empty values</b>. See the examples for details.</p>
 * <p/>
 * <p/>
 * <dl><dt>date</dt><dd>May 10, 2010</dd></dl>
 *
 * @author brandizi
 */
public interface TabValueGroup {
    public List<String> getHeaders();

    public List<String> getValues();
}
