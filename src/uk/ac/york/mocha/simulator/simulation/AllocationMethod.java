package uk.ac.york.mocha.simulator.simulation;

import java.util.List;

import uk.ac.york.mocha.simulator.dag.Node;

public abstract class AllocationMethod {

	
	protected abstract int compareNode(Node c1, Node c2);

	protected abstract List<Node> getEligibileNode(List<Node> readyNodes, int procNum);

}
