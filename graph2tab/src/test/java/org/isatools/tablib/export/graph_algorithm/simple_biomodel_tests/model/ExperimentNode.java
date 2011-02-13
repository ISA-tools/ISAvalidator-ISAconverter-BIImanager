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
 * This work is licenced under the Creative Commons Attribution-Share Alike 2.0 UK: England & Wales License. 
 * To view a copy of this licence, visit http://creativecommons.org/licenses/by-sa/2.0/uk/ or send a letter to
 * Creative Commons, 171 Second Street, Suite 300, San Francisco, California 94105, USA.
 *
 * __________
 * Sponsors
 * __________
 * This work has been funded mainly by the EU Carcinogenomics (http://www.carcinogenomics.eu) [PL 037712] and in part by the
 * EU NuGO [NoE 503630](http://www.nugo.org/everyone) projects and in part by EMBL-EBI.
 */

package org.isatools.tablib.export.graph_algorithm.simple_biomodel_tests.model;

import java.util.*;

/**
 * A simple but realistic object model of an experimental graph.
 * <p/>
 * <dl><dt>date</dt><dd>Jun 1, 2010</dd></dl>
 *
 * @author brandizi
 */
public abstract class ExperimentNode {

	private String name;
	private Set<ExperimentNode> inputs = new HashSet<ExperimentNode>(),
			outputs = new HashSet<ExperimentNode>();
	private List<Annotation> annotations = new ArrayList<Annotation>();

	/**
	 * Every node has a name.
	 */
	public ExperimentNode(String name) {
		this.name = name;
	}

	public boolean addInput(ExperimentNode input) {
		if (!inputs.add(input)) {
			return false;
		}
		input.outputs.add(this);
		return true;
	}

	public boolean addOutput(ExperimentNode output) {
		if (!outputs.add(output)) {
			return false;
		}
		output.inputs.add(this);
		return true;
	}

	public Set<ExperimentNode> getInputs() {
		return Collections.unmodifiableSet(inputs);
	}

	public Set<ExperimentNode> getOutputs() {
		return Collections.unmodifiableSet(outputs);
	}


//	public boolean removeInput ( ExperimentNode input ) {
//		if ( !inputs.remove ( input ) ) return false;
//		input.outputs.remove ( this );
//		return true;
//	}
//
//	public boolean removeOutput ( ExperimentNode output ) {
//		if ( !outputs.remove ( output ) ) return false;
//		output.inputs.remove ( this );
//		return true;
//	}

	/**
	 * In this simple model, annotations are used for several node property types, eg: biomaterial
	 * characteristics or protocol parameters.
	 */
	protected void addAnnotation(String type, String value, String termAcc, String termSrc) {
		annotations.add(new Annotation(type, value, termAcc, termSrc));
	}

	/**
	 * In this simple model, annotations are used for several node property types, eg: biomaterial
	 * characteristics or protocol parameters.
	 */
	public List<Annotation> getAnnotations() {
		return Collections.unmodifiableList(annotations);
	}

	public String getName() {
		return name;
	}

}
