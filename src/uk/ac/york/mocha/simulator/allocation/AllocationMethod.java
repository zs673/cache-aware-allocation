package uk.ac.york.mocha.simulator.allocation;

import java.util.List;

import uk.ac.york.mocha.simulator.dag.Node;

public abstract class AllocationMethod {

	
	protected abstract int compareNode(Node c1, Node c2);

	public abstract List<Node> getEligibileNode(List<Node> readyNodes, int procNum);

}
