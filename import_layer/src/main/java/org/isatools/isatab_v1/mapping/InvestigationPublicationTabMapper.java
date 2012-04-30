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

package org.isatools.isatab_v1.mapping;

import org.isatools.isatab.mapping.attributes.CommentMappingHelper;
import org.isatools.tablib.exceptions.TabInternalErrorException;
import org.isatools.tablib.mapping.ClassTabMapper;
import org.isatools.tablib.mapping.MappingUtils;
import org.isatools.tablib.mapping.properties.StringPropertyMappingHelper;
import org.isatools.tablib.schema.SectionInstance;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.model.Investigation;
import uk.ac.ebi.bioinvindex.model.Publication;
import uk.ac.ebi.bioinvindex.model.term.PublicationStatus;
import uk.ac.ebi.bioinvindex.model.xref.ReferenceSource;

import java.util.Collection;
import java.util.List;

/**
 * Maps the publications in the Investigation file.
 * <p/>
 * Sept 2008
 *
 * @author brandizi
 */
public class InvestigationPublicationTabMapper extends ClassTabMapper<Publication> {
    private MappingUtils mappingUtils;


    @SuppressWarnings("unchecked")
    public InvestigationPublicationTabMapper(BIIObjectStore store, SectionInstance sectionInstance) {
        super(store, sectionInstance);

        mappingHelpersConfig.put("Investigation PubMed ID", new MappingHelperConfig<StringPropertyMappingHelper>(
                StringPropertyMappingHelper.class, new String[][]{{"propertyName", "pmid"}}
        ));
        mappingHelpersConfig.put("Investigation Publication DOI", new MappingHelperConfig<StringPropertyMappingHelper>(
                StringPropertyMappingHelper.class, new String[][]{{"propertyName", "doi"}}
        ));
        mappingHelpersConfig.put("Investigation Publication Author List", new MappingHelperConfig<StringPropertyMappingHelper>(
                StringPropertyMappingHelper.class, new String[][]{{"propertyName", "authorList"}}
        ));
        mappingHelpersConfig.put("Investigation Publication Title", new MappingHelperConfig<StringPropertyMappingHelper>(
                StringPropertyMappingHelper.class, new String[][]{{"propertyName", "title"}}
        ));

        mappingHelpersConfig.put("Comment", new MappingHelperConfig<CommentMappingHelper>(CommentMappingHelper.class));

        mappingUtils = new MappingUtils(store);
    }


    public Publication map(int recordIndex) {

        // Basic properties
        Publication pub = super.map(recordIndex);


        // The status, retrieved from the corresponding fields
        //
        // Use the default ontology for status
        ReferenceSource source = new ReferenceSource("bii:publication-states");
        source.setAcc("bii:publication-states");
        source.setUrl("bii:publication:states");


        PublicationStatus status = (PublicationStatus) mappingUtils.createOntologyEntry(
                getSectionInstance(), recordIndex, "Investigation Publication Status", "Investigation Publication Status", source,
                PublicationStatus.class
        );

        if (status.getAcc() == null) {
            status.setAcc(status.getName());
        }
        if (status.getAcc() != null) {
            pub.setStatus(status);
        }

        Collection<Investigation> investigations = getStore().valuesOfType(Investigation.class);
        if (investigations == null || investigations.size() == 0) {
            throw new TabInternalErrorException("No investigation to attach the publication to: " + pub);
        }

        investigations.iterator().next().addPublication(pub);

        log.trace("New mapped publication: " + pub);
        return pub;
    }


    public Publication newMappedObject() {
        return new Publication(null, null);
    }


    /**
     * null, we don't save it
     */
    @Override
    public String getStoreKey(Publication mappedObject) {
        return null;
    }


    @Override
    public List<Integer> getMatchedFieldIndexes() {
        List<Integer> result = super.getMatchedFieldIndexes();
        addMatchedField("Investigation Publication Status", result);
        addMatchedField("Investigation Publication Status Term Accession Number", result);
        addMatchedField("Investigation Publication Status Term Source REF", result);
        return result;
    }
}