package uk.ac.york.mocha.simulator.simulation;

import java.util.ArrayList;
import java.util.List;

import uk.ac.york.mocha.simulator.dag.DirectedAcyclicGraph;
import uk.ac.york.mocha.simulator.dag.Node;

public class SimpleLoadBalancing extends AllocationMethod {

	@Override
	protected List<Node> getEligibileNode(List<Node> readyNodes, int procNum) {

		/*
		 * sort ready nodes list by FPS+WF, take first procNum nodes to execute.
		 */
		readyNodes.sort((c1, c2) -> compareNode(c1, c2));

		List<Node> eligibile = new ArrayList<>();

		for (int i = 0; i < procNum; i++) {
			if (i >= readyNodes.size())
				break;
			eligibile.add(readyNodes.get(i));
		}

		return eligibile;
	}

	/*
	 * A prioritised load balancing policy
	 */
	protected int compareNode(Node c1, Node c2) {

		DirectedAcyclicGraph dag1 = Utils.getDagByIndex(c1.getDagID(), c1.getDagInstNo());
		DirectedAcyclicGraph dag2 = Utils.getDagByIndex(c2.getDagID(), c2.getDagInstNo());

		if (dag1.getSchedParameters().getPriority() > dag2.getSchedParameters().getPriority()) {
			return -1;
		} else if (dag1.getSchedParameters().getPriority() < dag2.getSchedParameters().getPriority()) {
			return 1;
		} else {
			return 0;
		}

	}

}
