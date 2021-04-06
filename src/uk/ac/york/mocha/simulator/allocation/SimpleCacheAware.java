package uk.ac.york.mocha.simulator.allocation;

import java.util.ArrayList;
import java.util.List;

import uk.ac.york.mocha.simulator.dag.DirectedAcyclicGraph;
import uk.ac.york.mocha.simulator.dag.Node;
import uk.ac.york.mocha.simulator.dag.Recency;
import uk.ac.york.mocha.simulator.simulator.Utils;

public class SimpleCacheAware extends AllocationMethod {

//	@Override
//	public List<Node> getEligibileNode(List<DirectedAcyclicGraph> dags, List<Node> readyNodes, List<Integer> availableProcs, List<List<Node>> history, Recency table){
//
//		/*
//		 * sort ready nodes list by FPS+WF, take first procNum nodes to execute.
//		 */
//		readyNodes.sort((c1, c2) -> compareNode(dags, c1, c2));
//
//		List<Node> eligibile = new ArrayList<>();
//
//		for (int i = 0; i < availableProcs.size(); i++) {
//			if (i >= readyNodes.size())
//				break;
//			eligibile.add(readyNodes.get(i));
//		}
//
//		return eligibile;
//	}

	@Override
	public void getEligibileNode(List<DirectedAcyclicGraph> dags, List<Node> readyNodes,
			List<Integer> availableProcs, List<List<Node>> history, Recency table) {

		if (readyNodes.size() == 0 || availableProcs.size() == 0)
			return;
		

		/*
		 * sort ready nodes list by FPS+WF, take first procNum nodes to execute.
		 */
		readyNodes.sort((c1, c2) -> compareNode(dags, c1, c2));

//		List<Node> eligibile = new ArrayList<>();
//
//		for (int i = 0; i < availableProcs.size(); i++) {
//			if (i >= readyNodes.size())
//				break;
//			eligibile.add(readyNodes.get(i));
//		}

//		if (availableProcs.size() == 1) {
//			List<Node> singleNode = new ArrayList<>();
//			singleNode.add(readyNodes.get(0));
//			readyNodes.get(0).
//			return singleNode;
//		}

		List<Node> preEligible = new ArrayList<>();
		
		for(Node n : readyNodes) {
			if(n.getDagID()==1 && n.getDagInstNo()==2 && n.getId()==5) {
				System.out.println("check");
				break;
			}
		}

		for (int i = 0; i < availableProcs.size(); i++) {
			if (i >= readyNodes.size())
				break;
			preEligible.add(readyNodes.get(i));
		}

		List<Integer> availableP = new ArrayList<>(availableProcs);
		
		
		for(Node n : preEligible) {
			List<Long> ETdrop = new ArrayList<>();
			
			for (int i = 0; i < availableP.size(); i++) {
				int proc = availableP.get(i);
				
				ETdrop.add(n.getWCET() - table.computeET(history, n, proc, true));
			}
			
			int procIndex = getIndexOfMaximum(ETdrop);
			n.partition = availableP.get(procIndex);
			availableP.remove(procIndex);
		}
		
//		List<List<Long>> ETdropTable = new ArrayList<>();
//
//		for (int i = 0; i < availableProcs.size(); i++) {
//			if (preEligible.size() == 0)
//				break;
//
//			int proc = availableProcs.get(i);
//
//			List<Long> ETdrop = preEligible.stream().map(n -> (n.getWCET() - table.computeET(history, n, proc, true)))
//					.collect(Collectors.toList());
//
//			int maxIndex = getIndexOfMaximum(ETdrop);
//
//			eligibile.add(preEligible.get(maxIndex));
//			preEligible.remove(maxIndex);
//		}

//		return eligibile;
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
			return -Long.compare(c1.getWCET(), c2.getWCET());
		}

	}

	private int getIndexOfMaximum(List<Long> l) {
		int index = -1;
		long max = Long.MIN_VALUE;

		for (int i = 0; i < l.size(); i++) {
			if (max < l.get(i)) {
				max = l.get(i);
				index = i;
			}
		}

		if (index == -1)
			System.out.println();

		return index;
	}

}
