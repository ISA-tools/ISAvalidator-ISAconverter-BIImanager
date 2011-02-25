package org.isatools.tablib.export.graph_algorithm.layering_tests.wrappers;

import java.util.List;

import org.isatools.tablib.export.graph_algorithm.Node;
import org.isatools.tablib.export.graph_algorithm.TabValueGroup;
import org.isatools.tablib.export.graph_algorithm.layering_tests.model.BioExtract;
import org.isatools.tablib.export.graph_algorithm.simple_biomodel_tests.node_wrappers.BioMaterialWrapper;
import org.isatools.tablib.export.graph_algorithm.simple_biomodel_tests.node_wrappers.ExpNodeWrapper;

public class BioExtractWrapper extends BioMaterialWrapper
{

	BioExtractWrapper ( BioExtract base, NodeFactory nodeFactory )
	{
		super ( base, nodeFactory );
	}

	private BioExtractWrapper ( ExpNodeWrapper original )
	{
		super ( original );
	}

	public List<TabValueGroup> getTabValues ()
	{
		return super.getTabValues ( "Extract Name", "Characteristic" );
	}

	public Node createIsolatedClone ()
	{
		return new BioExtractWrapper ( this );
	}

}
