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

import uk.ac.ebi.bioinvindex.model.Identifiable;

/**
 * Extends Identifiable just because of type compatibility with mappers
 */
public class News extends Identifiable {
	private String date, title, abs;

	public News() {
		super();
	}

	public News(String date, String title, String abs) {
		super();
		this.date = date;
		this.title = title;
		this.abs = abs;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAbs() {
		return abs;
	}

	public void setAbs(String abs) {
		this.abs = abs;
	}


	public String toString() {
		return String.format("date: %s, title: %s, abstract: %s", getDate(), getTitle(), getAbs());
	}


	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		News news = (News) obj;

		String date1 = news.getDate();
		String title1 = news.getTitle();
		String abs1 = news.getAbs();

		return
				(date == null && date1 == null || date.equals(date1))
						|| (title == null && title1 == null || title.equals(title1))
						|| (abs == null && abs1 == null || abs.equals(abs1));
	}


	@Override
	public int hashCode() {
		int hash = super.hashCode();
		if (date != null) {
			hash *= 31 + date.hashCode();
		}
		if (title != null) {
			hash *= 31 + title.hashCode();
		}
		if (abs != null) {
			hash *= 31 + abs.hashCode();
		}
		return hash;
	}

}
