package org.isatools.tablib.export.graph_algorithm.layering_tests.wrappers;

import java.util.List;

import org.isatools.tablib.export.graph_algorithm.Node;
import org.isatools.tablib.export.graph_algorithm.TabValueGroup;
import org.isatools.tablib.export.graph_algorithm.layering_tests.model.BioLabeledExtract;
import org.isatools.tablib.export.graph_algorithm.simple_biomodel_tests.node_wrappers.BioMaterialWrapper;
import org.isatools.tablib.export.graph_algorithm.simple_biomodel_tests.node_wrappers.ExpNodeWrapper;

public class BioLabeledExtractWrapper extends BioMaterialWrapper
{

	BioLabeledExtractWrapper ( BioLabeledExtract base, NodeFactory nodeFactory )
	{
		super ( base, nodeFactory );
	}

	private BioLabeledExtractWrapper ( ExpNodeWrapper original )
	{
		super ( original );
	}

	public List<TabValueGroup> getTabValues ()
	{
		return super.getTabValues ( "Labeled Extract Name", "Characteristic" );
	}

	public Node createIsolatedClone ()
	{
		return new BioLabeledExtractWrapper ( this );
	}

}
