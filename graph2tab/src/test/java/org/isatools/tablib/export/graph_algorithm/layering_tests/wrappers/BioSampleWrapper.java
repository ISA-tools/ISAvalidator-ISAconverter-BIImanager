package org.isatools.tablib.export.graph_algorithm.layering_tests.wrappers;

import java.util.List;

import org.isatools.tablib.export.graph_algorithm.Node;
import org.isatools.tablib.export.graph_algorithm.TabValueGroup;
import org.isatools.tablib.export.graph_algorithm.layering_tests.model.BioSample;
import org.isatools.tablib.export.graph_algorithm.simple_biomodel_tests.node_wrappers.BioMaterialWrapper;
import org.isatools.tablib.export.graph_algorithm.simple_biomodel_tests.node_wrappers.ExpNodeWrapper;

public class BioSampleWrapper extends BioMaterialWrapper
{

	BioSampleWrapper ( BioSample base )
	{
		super ( base );
	}

	private BioSampleWrapper ( ExpNodeWrapper original )
	{
		super ( original );
	}

	public List<TabValueGroup> getTabValues ()
	{
		return super.getTabValues ( "Sample Name", "Characteristic" );
	}

	public Node createIsolatedClone ()
	{
		return new BioSampleWrapper ( this );
	}

}
