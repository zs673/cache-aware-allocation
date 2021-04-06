package uk.ac.york.mocha.simulator.allocation;

import java.util.List;

import uk.ac.york.mocha.simulator.dag.DirectedAcyclicGraph;
import uk.ac.york.mocha.simulator.dag.Node;
import uk.ac.york.mocha.simulator.dag.Recency;

public abstract class AllocationMethod {

	
//	protected abstract int compareNode(List<DirectedAcyclicGraph> dags, Node c1, Node c2);

	public abstract void getEligibileNode(List<DirectedAcyclicGraph> dags, List<Node> readyNodes, List<Integer> availableProcs, List<List<Node>> history, Recency table);

}
