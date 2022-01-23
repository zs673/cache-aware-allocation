package uk.ac.york.mocha.simulator.allocation;

import java.util.List;

import uk.ac.york.mocha.simulator.dag.DirectedAcyclicGraph;
import uk.ac.york.mocha.simulator.dag.Node;
import uk.ac.york.mocha.simulator.dag.RecencyProfile;

public abstract class AllocationMethods {

//	protected abstract int compareNode(List<DirectedAcyclicGraph> dags, Node c1, Node c2);

	public abstract void allocate(List<DirectedAcyclicGraph> dags, List<Node> readyNodes,
			List<Integer> availableProcs, long[] procs, List<List<Node>> history_level1,
			List<List<Node>> history_level2, List<Node> history_level3, List<List<Node>> allocNodes, RecencyProfile table,
			long currentTime, boolean lcif, boolean recency_fault, int onlyCritical);

}
