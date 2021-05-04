package uk.ac.york.mocha.simulator.allocation;

import java.util.List;

import uk.ac.york.mocha.simulator.dag.DirectedAcyclicGraph;
import uk.ac.york.mocha.simulator.dag.Node;
import uk.ac.york.mocha.simulator.dag.Recency;
import uk.ac.york.mocha.simulator.simulator.Utils;

public class LoadBalancing extends AllocationMethod {

	@Override
	public void getEligibileNode(List<DirectedAcyclicGraph> dags, List<Node> readyNodes, List<Integer> availableProcs,
			long[] procs, List<List<Node>> history_level1, List<List<Node>> history_level2, List<Node> history_level3,
			List<List<Node>> allocHistory, Recency table, long currentTime, boolean affinity) {

		if (readyNodes.size() == 0 || availableProcs.size() == 0)
			return;

		/*
		 * Entry for debugging a single node
		 */
		for (Node n : readyNodes) {
			if (n.getDagID() == 0 && n.getDagInstNo() == 6 && n.getId() == 7) {
				break;
			}
		}

		/*
		 * sort ready nodes list by FPS+WF, take first procNum nodes to execute.
		 */
		readyNodes.sort((c1, c2) -> compareNode(dags, c1, c2));

		readyNodes.stream().forEach(c -> c.partition = -1);

//		List<Node> eligibile = new ArrayList<>();

		for (int i = 0; i < availableProcs.size(); i++) {
			if (i >= readyNodes.size())
				break;
//			eligibile.add(readyNodes.get(i));
			readyNodes.get(i).partition = availableProcs.get(i);
		}

//		Collections.shuffle(eligibile);
//		return eligibile;

//		System.out.println();
	}

	/*
	 * A prioritised load balancing policy
	 */
	private int compareNode(List<DirectedAcyclicGraph> dags, Node c1, Node c2) {

		DirectedAcyclicGraph dag1 = Utils.getDagByIndex(dags, c1.getDagID(), c1.getDagInstNo());
		DirectedAcyclicGraph dag2 = Utils.getDagByIndex(dags, c2.getDagID(), c2.getDagInstNo());

		if (dag1.getSchedParameters().getPriority() > dag2.getSchedParameters().getPriority()) {
			return -1;
		} else if (dag1.getSchedParameters().getPriority() < dag2.getSchedParameters().getPriority()) {
			return 1;
		} else {

			int c = -Long.compare(c1.getWCET(), c2.getWCET());

			if (c != 0)
				return c;
			else {
				return Integer.compare(c1.getDagInstNo(), c1.getDagInstNo());
			}

		}

	}

}
