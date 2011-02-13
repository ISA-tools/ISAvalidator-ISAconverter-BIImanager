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

package org.isatools.tablib.export;

import org.isatools.tablib.schema.SectionBlockInstance;
import org.isatools.tablib.schema.SectionInstance;
import org.isatools.tablib.utils.BIIObjectStore;
import uk.ac.ebi.bioinvindex.model.Identifiable;
import uk.ac.ebi.utils.reflection.ReflectionUtils;

import java.util.*;

/**
 * An exporter for repeatable blocks of sections, like Study in ISATAB.
 * Similarly to the mapper, this kind of exporter uses single section exporters to produce the
 * content of a section block and also repeats over
 *
 * @author brandizi
 *         <b>date</b>: Feb 9, 2010
 */
public abstract class SectionBlockTabExporter<XT extends Identifiable>
		extends ComposedTabExporter<SectionBlockInstance, SectionExporter>
		implements FormatComponentExporter<SectionBlockInstance> {
	/**
	 * If it's not null, will be used to sort the exported objects and spawn results in a particular order.
	 */
	protected Comparator<? super XT> comparator;


	public SectionBlockTabExporter(BIIObjectStore store) {
		super(store);
	}


	/**
	 * Does the export. Call {@link #getSortedSources()} and for each source invokes {@link #export(Identifiable)},
	 * returns a section block instance containing all the sections collected from the latter.
	 */
	public SectionBlockInstance export() {
		Collection<XT> sources = getSortedSources();
		if (sources == null || sources.size() == 0) {
			log.trace("AbstractTabExporter: no instance of " + getSourceClass().getSimpleName() + " to export");
			return null;
		}

		log.debug(this.getClass().getSimpleName() + ".export(), begin");
		List<SectionInstance> results = new LinkedList<SectionInstance>();
		for (XT source : sources) {
			log.trace("exporting a whole block for the object " + source);
			results.addAll(export(source));
		}

		log.debug("  ... the end");
		return results.isEmpty() ? null : new SectionBlockInstance(results);
	}

	/**
	 * Export a single source and collects all the single sections that this produces, by using the configured
	 * component exporters.
	 */
	public List<SectionInstance> export(XT source) {
		List<SectionInstance> results = new ArrayList<SectionInstance>();
		for (SectionExporter exporter : exporters) {
			exporter.setCurrentSource(source);
			SectionInstance result = exporter.export();
			if (result == null) {
				log.trace("No result for the export of " + exporter.getClass().getSimpleName());
				continue;
			}
			results.add(result);
		}
		return results;
	}

	/**
	 * TODO: caching
	 */
	public Class<XT> getSourceClass() {
		return ReflectionUtils.getTypeArgument(SectionBlockTabExporter.class, this.getClass(), 0);
	}


	/**
	 * Gets all the sources which need to be mapped. By default returns the objects in the store of type
	 * {@link #getSourceClass()}.
	 */
	protected Collection<XT> getSources() {
		return getStore().valuesOfType(getSourceClass());
	}

	/**
	 * Gets all the objects of SourceType which are in the store, sort them according to the comparator passed to
	 * the constructor.
	 * <p/>
	 * TODO: caching
	 */
	protected Collection<XT> getSortedSources() {
		Collection<XT> sources = getSources();
		if (comparator == null) {
			return sources;
		}

		List<XT> sourcesList = new ArrayList<XT>(sources);
		Collections.sort(sourcesList, comparator);

		return sourcesList;
	}

}
