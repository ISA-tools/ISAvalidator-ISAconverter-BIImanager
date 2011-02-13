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
 * - Manon Delahaye (ISA team trainee:  BII web services)
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
 * To request enhancements:  http://sourceforge.net/tracker/?group_id=215183&atid=1032652
 *
 *
 * __________
 * License:
 * __________
 *
 * This work is licenced under the Creative Commons Attribution-Share Alike 2.0 UK: England & Wales License. To view a copy of this licence, visit http://creativecommons.org/licenses/by-sa/2.0/uk/ or send a letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California 94105, USA.
 *
 * __________
 * Sponsors
 * __________
 * This work has been funded mainly by the EU Carcinogenomics (http://www.carcinogenomics.eu) [PL 037712] and in part by the
 * EU NuGO [NoE 503630](http://www.nugo.org/everyone) projects and in part by EMBL-EBI.
 */

package org.isatools.tablib.export.properties;

import org.apache.commons.beanutils.PropertyUtils;
import org.isatools.tablib.exceptions.TabInternalErrorException;
import org.isatools.tablib.schema.SectionInstance;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.model.Identifiable;

import java.util.Collection;

/**
 * Exports a collection property into a single field, using semicolon as separator.
 *
 * @author brandizi
 *         <b>date</b>: Feb 11, 2010
 * @param <ST> as in {@link PropertyExportingHelper}.
 * @param <ET> the element type returned by the ST's property this helper deals with. Such
 * property will return {@link Collection Collection&lt;ET&gt;}.
 */
public abstract class CollectionExportingHelper<ST extends Identifiable, ET> extends PropertyExportingHelper<ST> {
	public CollectionExportingHelper(BIIObjectStore store, String propertyName) {
		super(store, propertyName);
	}

	@Override
	public abstract SectionInstance setFields(SectionInstance sectionInstance);

	@SuppressWarnings("unchecked")
	protected Collection<ET> getPropertyValues(ST source) {
		Collection<ET> value;
		try {
			value = (Collection<ET>) PropertyUtils.getProperty(source, getPropertyName());
		}
		catch (Exception e) {
			throw new TabInternalErrorException(
					String.format("Internal error while exporting property '%s' from class '%s': %s",
							getPropertyName(), source.getClass().getSimpleName(), e.getMessage()
					),
					e
			);
		}
		return value;
	}

}
