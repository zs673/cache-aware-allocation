package uk.ac.york.mocha.simulator.allocation;

import java.util.List;

import uk.ac.york.mocha.simulator.dag.DirectedAcyclicGraph;
import uk.ac.york.mocha.simulator.dag.Node;
import uk.ac.york.mocha.simulator.dag.Recency;

public class OurAlloc extends AllocationMethod{

	@Override
	public void getEligibileNode(List<DirectedAcyclicGraph> dags, List<Node> readyNodes, List<Integer> availableProcs, long[] procs,
			List<List<Node>> history_level1, List<List<Node>> history_level2, List<Node> history_level3, List<List<Node>> allocNodes,
			Recency table, long currentTime, boolean affinity) {
		// TODO Auto-generated method stub
		
	}

}
