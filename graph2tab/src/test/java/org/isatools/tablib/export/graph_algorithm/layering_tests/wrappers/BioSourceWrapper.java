package org.isatools.tablib.export.graph_algorithm.layering_tests.wrappers;

import java.util.List;

import org.isatools.tablib.export.graph_algorithm.Node;
import org.isatools.tablib.export.graph_algorithm.TabValueGroup;
import org.isatools.tablib.export.graph_algorithm.layering_tests.model.BioSource;
import org.isatools.tablib.export.graph_algorithm.simple_biomodel_tests.node_wrappers.BioMaterialWrapper;
import org.isatools.tablib.export.graph_algorithm.simple_biomodel_tests.node_wrappers.ExpNodeWrapper;

public class BioSourceWrapper extends BioMaterialWrapper
{

	BioSourceWrapper ( BioSource base, NodeFactory nodeFactory )
	{
		super ( base, nodeFactory );
	}

	private BioSourceWrapper ( ExpNodeWrapper original )
	{
		super ( original );
	}

	@Override
	public List<TabValueGroup> getTabValues ()
	{
		return super.getTabValues ( "Source Name", "Characteristic" );
	}

	@Override
	public Node createIsolatedClone ()
	{
		return new BioSourceWrapper ( this );
	}

}
