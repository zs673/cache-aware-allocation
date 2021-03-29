package uk.ac.york.mocha.simulator.allocation;

import java.util.ArrayList;
import java.util.List;

import uk.ac.york.mocha.simulator.dag.DirectedAcyclicGraph;
import uk.ac.york.mocha.simulator.dag.Node;
import uk.ac.york.mocha.simulator.dag.Recency;
import uk.ac.york.mocha.simulator.simulator.Utils;

public class LoadBalancing extends AllocationMethod {

	@Override
	public List<Node> getEligibileNode(List<DirectedAcyclicGraph> dags, List<Node> readyNodes, List<Integer> availableProcs, List<List<Node>> history, Recency table){

		/*
		 * sort ready nodes list by FPS+WF, take first procNum nodes to execute.
		 */
		readyNodes.sort((c1, c2) -> compareNode(dags, c1, c2));

		List<Node> eligibile = new ArrayList<>();

		for (int i = 0; i < availableProcs.size(); i++) {
			if (i >= readyNodes.size())
				break;
			eligibile.add(readyNodes.get(i));
		}

//		Collections.shuffle(eligibile);
		return eligibile;
	}

	/*
	 * A prioritised load balancing policy
	 */
	protected int compareNode(List<DirectedAcyclicGraph> dags, Node c1, Node c2) {

		DirectedAcyclicGraph dag1 = Utils.getDagByIndex(dags, c1.getDagID(), c1.getDagInstNo());
		DirectedAcyclicGraph dag2 = Utils.getDagByIndex(dags, c2.getDagID(), c2.getDagInstNo());

		if (dag1.getSchedParameters().getPriority() > dag2.getSchedParameters().getPriority()) {
			return -1;
		} else if (dag1.getSchedParameters().getPriority() < dag2.getSchedParameters().getPriority()) {
			return 1;
		} else {
			return -Long.compare(c1.getWCET(), c2.getWCET());
		}

	}

}
