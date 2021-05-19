package uk.ac.york.mocha.simulator.allocation;

import java.util.List;

import uk.ac.york.mocha.simulator.dag.DirectedAcyclicGraph;
import uk.ac.york.mocha.simulator.dag.Node;

public class OfflineAllocation {

	public static void allocate(DirectedAcyclicGraph dag, int processors) {
		List<Node> nodes = dag.getFlatNodes();

		dag.getFlatNodes().stream().map(c -> c.partition = -1);

		for (int i = 0; i < nodes.size(); i++) {
			nodes.get(i).priority = -1;
		}

	}



}
