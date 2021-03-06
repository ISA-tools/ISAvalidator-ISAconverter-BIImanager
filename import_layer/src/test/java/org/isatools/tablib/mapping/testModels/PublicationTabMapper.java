/*
 * __________
 * CREDITS
 * __________
 *
 * Team page: http://isatab.sf.net/
 * - Marco Brandizi (software engineer: ISAvalidator, ISAconverter, BII data management utility, BII model)
 * - Eamonn Maguire (software engineer: ISAcreator, ISAcreator configurator, ISAvalidator, ISAconverter,  BII data management utility, BII web)
 * - Nataliya Sklyar (software engineer: BII web application, BII model,  BII data management utility)
 * - Philippe Rocca-Serra (technical coordinator: user requirements and standards compliance for ISA software, ISA-tab format specification, BII model, ISAcreator wizard, ontology)
 * - Susanna-Assunta Sansone (coordinator: ISA infrastructure design, standards compliance, ISA-tab format specification, BII model, funds raising)
 *
 * Contributors:
 * - Manon Delahaye (ISA team trainee: BII web services)
 * - Richard Evans (ISA team trainee: rISAtab)
 *
 *
 * ______________________
 * Contacts and Feedback:
 * ______________________
 *
 * Project overview: http://isatab.sourceforge.net/
 *
 * To follow general discussion: isatab-devel@list.sourceforge.net
 * To contact the developers: isatools@googlegroups.com
 *
 * To report bugs: http://sourceforge.net/tracker/?group_id=215183&atid=1032649
 * To request enhancements: �http://sourceforge.net/tracker/?group_id=215183&atid=1032652
 *
 *
 * __________
 * License:
 * __________
 *
 * Reciprocal Public License 1.5 (RPL1.5)
 * [OSI Approved License]
 *
 * Reciprocal Public License (RPL)
 * Version 1.5, July 15, 2007
 * Copyright (C) 2001-2007
 * Technical Pursuit Inc.,
 * All Rights Reserved.
 *
 * http://www.opensource.org/licenses/rpl1.5.txt
 *
 * __________
 * Sponsors
 * __________
 * This work has been funded mainly by the EU Carcinogenomics (http://www.carcinogenomics.eu) [PL 037712] and in part by the
 * EU NuGO [NoE 503630](http://www.nugo.org/everyone) projects and in part by EMBL-EBI.
 */

package org.isatools.tablib.mapping.testModels;

import org.apache.commons.beanutils.PropertyUtils;
import org.isatools.tablib.mapping.ClassTabMapper;
import org.isatools.tablib.mapping.properties.StringPropertyMappingHelper;
import org.isatools.tablib.schema.SectionInstance;
import org.isatools.tablib.utils.BIIObjectStore;

/**
 * Note: the property alias stringId is needed because of a bug in
 * {@link PropertyUtils#setProperty(Object, String, Object)}
 */
public class PublicationTabMapper extends ClassTabMapper<Publication> {
	@SuppressWarnings("unchecked")
	public PublicationTabMapper(BIIObjectStore store, SectionInstance sectionInstance) {
		super(store, sectionInstance);

		mappingHelpersConfig.put("id",
				new MappingHelperConfig<StringPropertyMappingHelper>(
						StringPropertyMappingHelper.class, new String[][]{{"propertyName", "stringId"}}
				)
		);
		mappingHelpersConfig.put("Author",
				new MappingHelperConfig<StringPropertyMappingHelper>(
						StringPropertyMappingHelper.class, new String[][]{{"propertyName", "author"}}
				)
		);
		mappingHelpersConfig.put("Publication Title",
				new MappingHelperConfig<StringPropertyMappingHelper>(
						StringPropertyMappingHelper.class, new String[][]{{"propertyName", "title"}}
				)
		);
		mappingHelpersConfig.put("Journal",
				new MappingHelperConfig<StringPropertyMappingHelper>(
						StringPropertyMappingHelper.class, new String[][]{{"propertyName", "journal"}}
				)
		);
		mappingHelpersConfig.put("Keywords",
				new MappingHelperConfig<StringPropertyMappingHelper>(
						StringPropertyMappingHelper.class, new String[][]{{"propertyName", "keywords"}}
				)
		);
	}


	@Override
	public String getStoreKey(Publication mappedObject) {
		return mappedObject == null ? null : Long.toString(mappedObject.getId());
	}

}