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
 * To request enhancements: Êhttp://sourceforge.net/tracker/?group_id=215183&atid=1032652
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

import org.apache.commons.lang.StringUtils;
import org.isatools.tablib.export.ClassTabExporter;
import org.isatools.tablib.export.properties.PropertyExportingHelper;
import org.isatools.tablib.export.properties.StringExportingHelper;
import org.isatools.tablib.schema.FormatSet;
import org.isatools.tablib.schema.SchemaBuilder;
import org.isatools.tablib.schema.Section;
import org.isatools.tablib.utils.BIIObjectStore;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;

public class NewsTabExporter extends ClassTabExporter<News> {
	public NewsTabExporter(BIIObjectStore store) {
		super(store);
		propertyExportingHelpers = new ArrayList<PropertyExportingHelper<News>>();
		propertyExportingHelpers.add(new StringExportingHelper<News>(store, "date", "Date"));
		propertyExportingHelpers.add(new StringExportingHelper<News>(store, "title", "News Title"));
		propertyExportingHelpers.add(new StringExportingHelper<News>(store, "abs", "News Abstract"));

		this.comparator = new Comparator<News>() {
			/** by date, most recent first */
			public int compare(News o1, News o2) {
				if (o1 == null) {
					return o2 == null ? 0 : -1;
				}
				if (o2 == null) {
					return o1 == null ? 0 : +1;
				}

				String date1 = StringUtils.trimToEmpty(o1.getDate());
				return -date1.compareTo(StringUtils.trimToEmpty(o2.getDate()));
			}
		};
	}

	protected Section getSection() {
		InputStream input = new BufferedInputStream(this.getClass().getResourceAsStream("/test-data/tablib/foo_format_def.xml"));
		FormatSet schema = SchemaBuilder.loadFormatSetFromXML(input);
		return schema.getDescendant("news", Section.class);
	}
}
