package uk.ac.york.mocha.simulator.allocation;

import java.util.List;

import uk.ac.york.mocha.simulator.dag.DirectedAcyclicGraph;
import uk.ac.york.mocha.simulator.dag.Node;
import uk.ac.york.mocha.simulator.dag.RecencyProfileSyn;
import uk.ac.york.mocha.simulator.simulator.Utils;

public class SimpleAllocation extends AllocationMethods {

	@Override
	public void allocate(List<DirectedAcyclicGraph> dags, List<Node> readyNodes, List<List<Node>> localRunqueue,
			List<Integer> availableProcs, long[] procs, List<List<Node>> history_level1,
			List<List<Node>> history_level2, List<Node> history_level3, List<List<Node>> allocHistory,
			RecencyProfileSyn table, long currentTime, boolean affinity) {

		if (readyNodes.size() == 0 || availableProcs.size() == 0)
			return;

		readyNodes.sort((c1, c2) -> Utils.compareNode(dags, c1, c2));

		int coreIndex = 0;
		for (int i = 0; i < readyNodes.size(); i++) {
			readyNodes.get(i).partition = coreIndex;
			

			allocHistory.get(coreIndex).add(readyNodes.get(i));
			
			localRunqueue.get(coreIndex).add(readyNodes.get(i));
			
			
//			coreIndex++;
			if (coreIndex >= availableProcs.size())
				coreIndex = 0;
			
			
		}

		readyNodes.clear();
	}

}
