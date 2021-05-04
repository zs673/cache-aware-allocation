package uk.ac.york.mocha.simulator.allocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.util.Pair;

import uk.ac.york.mocha.simulator.dag.DirectedAcyclicGraph;
import uk.ac.york.mocha.simulator.dag.Node;
import uk.ac.york.mocha.simulator.dag.Recency;
import uk.ac.york.mocha.simulator.simulator.Utils;

public class CacheAwareAlloc extends AllocationMethod {

	@Override
	public void getEligibileNode(List<DirectedAcyclicGraph> dags, List<Node> readyNodes, List<Integer> availableProcs,
			long[] availableTimeAllProcs, List<List<Node>> history_level1, List<List<Node>> history_level2,
			List<Node> history_level3, List<List<Node>> allocHistory, Recency table, long currentTime,
			boolean affinity) {

		/*
		 * Entry for debugging a single node
		 */
		for (Node n : readyNodes) {
			if (n.getDagID() == 0 && n.getDagInstNo() == 1 && n.getId() == 0) {
				break;
			}
		}

		if (readyNodes.size() == 0 || availableProcs.size() == 0)
			return;

		readyNodes.stream().forEach(c -> c.partition = -1);

		/*
		 * Sort ready nodes list by FPS+WF, take first procNum nodes to allocate.
		 */
		readyNodes.sort((c1, c2) -> compareNode(dags, c1, c2));

		List<Node> preEligible = new ArrayList<>();
		for (int i = 0; i < availableProcs.size(); i++) {
			if (readyNodes.size() == i)
				break;
			preEligible.add(readyNodes.get(i));
		}

		List<Integer> availableP = new ArrayList<>(availableProcs);

		List<List<Long>> speedUpTable = new ArrayList<>();

		for (Node n : preEligible) {
			List<Long> ETdrop = new ArrayList<>();

			for (int i = 0; i < history_level1.size(); i++) {
				int proc = i;
				if (availableP.contains(proc)) {
					/*
					 * Option 1: Speed up by ABSOLUTE vaue
					 */

					long WCET = n.getWCET();
					long realET = table.computeET(history_level1, history_level2, history_level3, n, proc, true, false)
							.getFirst();
					long speedup = WCET - realET;

					/*
					 * Option 2: Speed up by RELATIVE vaue
					 */
//				double speedup = ((double) (n.getWCET() - table.computeET(history_level1, history_level2, history_level3, n, proc, true))) / (double) n.getWCET();

					/*
					 * Option 3: Cache-aware ET
					 */
//					long speedup = table.computeET(history_level1, history_level2, history_level3, n, proc, true);

					ETdrop.add(speedup);
				}
			}

			speedUpTable.add(ETdrop);
		}

		List<Integer> allocProcs = new ArrayList<>();
		List<Integer> allocNodes = new ArrayList<>();

		List<List<Node>> historyCut = new ArrayList<>();
		for (int i = 0; i < history_level1.size(); i++) {
			if (availableP.contains(i))
				historyCut.add(history_level1.get(i));
		}

		List<List<Node>> allocHistoryCut = new ArrayList<>();
		for (int i = 0; i < allocHistory.size(); i++) {
			if (availableP.contains(i))
				allocHistoryCut.add(allocHistory.get(i));
		}

		for (int k = 0; k < availableP.size(); k++) {
			if (k >= preEligible.size())
				break;

			Pair<Integer, Integer> p = setPartition(speedUpTable, allocNodes, allocProcs, allocHistoryCut, allocHistory,
					preEligible, availableP, availableTimeAllProcs, table, currentTime, affinity);

//			if (p == null)
//				break;

			Node n = preEligible.get(p.getFirst().intValue());

//			if (p.getSecond() != -1) {
			n.partition = availableP.get(p.getSecond().intValue());
			n.expectedET = n.getWCET() - speedUpTable.get(p.getFirst()).get(p.getSecond());

			allocNodes.add(p.getFirst().intValue());
			allocProcs.add(p.getSecond().intValue());
//			} else {
//				allocNodes.add(p.getFirst().intValue());
//				k--;
//			}

		}

	}

	private Pair<Integer, Integer> setPartition(List<List<Long>> speedUpTable, List<Integer> allocNodes,
			List<Integer> allocProcs, List<List<Node>> allocHistory, List<List<Node>> fullAllocHistory,
			List<Node> preEligible, List<Integer> procs, long[] availableTimeAllProcs, Recency table, long time,
			boolean affinity) {

		int row = -1;
		int col = -1;
		long max = Long.MIN_VALUE;

		for (int i = 0; i < speedUpTable.size(); i++) {
			if (!allocNodes.contains(i)) {
				for (int j = 0; j < speedUpTable.get(i).size(); j++) {
					if (!allocProcs.contains(j)) {
						if (max < speedUpTable.get(i).get(j)) {
							max = speedUpTable.get(i).get(j);
							row = i;
							col = j;
						}
					}

				}
			}
		}

		if (affinity) {
			Node n = preEligible.get(row);
			List<Integer> freeProcIndex = new ArrayList<>();
			List<Integer> freeProc = new ArrayList<>();
			List<Integer> freeCluster = new ArrayList<>();

			/**
			 * Find all available cores that can have the same speed up
			 */
			for (int i = 0; i < procs.size(); i++) {
				if (!allocProcs.contains(i) && speedUpTable.get(row).get(i) == max) {
					freeProcIndex.add(i);

					int proc = procs.get(i);
					freeProc.add(proc);

					int c = proc / 4;
					if (!freeCluster.contains(c))
						freeCluster.add(c);
				}
			}

			if (freeProcIndex.size() > 1) {
				/*
				 * Search in history for same node & DAG allocation
				 */
				List<Long> NodeHis = new ArrayList<>();
				List<Long> DAGHis = new ArrayList<>();

				for (int i = 0; i < freeProcIndex.size(); i++) {
					NodeHis.add((long) 0);
					DAGHis.add((long) 0);
				}

				for (int i = 0; i < freeProcIndex.size(); i++) {
					int procIndex = freeProcIndex.get(i);

					List<Node> nodesInProc = allocHistory.get(procIndex);

					long Nodenum = 0;
					long DAGnum = 0;

					for (Node nh : nodesInProc) {
//						if (nh.getDagID() == n.getDagID() && nh.getId() == n.getId())
						Nodenum += nh.finishAt - nh.start;

						if (nh.getDagID() == n.getDagID())
							DAGnum++;
					}

					NodeHis.set(i, Nodenum);
					DAGHis.set(i, DAGnum);
				}

				long minExecutionTime = Collections.min(NodeHis);
				int minETIndex = NodeHis.indexOf(minExecutionTime);

				col = freeProcIndex.get(minETIndex);

//				int timeCount = 0;
//				for (Long l : NodeHis) {
//					if (l == minExecutionTime)
//						timeCount++;
//				}
//				if (timeCount == 0) {
//					col = freeProcIndex.get(minETIndex);
//				} else {
//
//					List<Integer> availableClusters = new ArrayList<>();
//					for (int i = 0; i < freeProc.size(); i++) {
//						int c = freeProc.get(i) / 4;
//						if (!availableClusters.contains(c))
//							availableClusters.add(c);
//					}
//
//					List<Integer> dagHisOnCluster = new ArrayList<>();
//					for (int i = 0; i < availableClusters.size(); i++) {
//						dagHisOnCluster.add(0);
//					}
//
//					for (int i = 0; i < fullAllocHistory.size(); i++) {
//						int proc = i;
//
//						if (availableClusters.contains(proc / 4)) {
//							List<Node> nodesOnCore = fullAllocHistory.get(i);
//
//							int count = 0;
//
//							for (Node node : nodesOnCore) {
//								if (node.getDagID() == n.getDagID())
//									count++;
//							}
//
//							int clusterIndex = availableClusters.indexOf(proc / 4);
//							dagHisOnCluster.set(clusterIndex, count + dagHisOnCluster.get(clusterIndex));
//
//						}
//					}
//
//					int maxNumCluster = Collections.max(dagHisOnCluster);
//					int maxCluster = availableClusters.get(dagHisOnCluster.indexOf(maxNumCluster));
//
//					if (maxNumCluster != 0) {
//						for (Integer p : freeProcIndex) {
//							int proc = procs.get(p);
//							if (proc / 4 == maxCluster) {
//								col = p;
//								break;
//							}
//						}
//					}
//
//				}

			}

		}

//		if (n.delayed != 0 && trade) {
//			long speedUp = max;
//			int allocationIndex = col;
//			int allocation = procs.get(allocationIndex);
//			long finish = time + (n.getWCET() - speedUp);
//
//			if (time < availableTimeAllProcs[allocation]) {
//				System.err.println("CacheAwareAlloc.setPartition() - Error in timing: 1");
//				System.exit(-1);
//			}
//
//			for (int i = 0; i < availableTimeAllProcs.length; i++) {
//				if (!freeProc.contains(i)) {
//					long speedUp_busy = n.getWCET() - table.computeET(fullHistory, n, i, true);
//					long finish_busy = time + (n.getWCET() - speedUp_busy);
//
//					if (finish_busy < finish) {
//						n.delayed = 0;
//						n.partition=i;
//						return new Pair<Integer, Integer>(row, -1);
//					}
//				}
//			}
//		}

		if (row == -1 || col == -1) {
			System.err.println("SimpleCacheAware.getIndexOfMaximum(): Cannot find the max value!");

			System.exit(-1);
		}

		return new Pair<Integer, Integer>(row, col);
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

/****************************************************************************************************************************
 ******************************************************* OLD VERSIONS *******************************************************
 ****************************************************************************************************************************/

/******************************************************************************
 * v0.2 ***********************************************************************
 * 
 * @Override public void getEligibileNode(List<DirectedAcyclicGraph> dags,
 *           List<Node> readyNodes, List<Integer> availableProcs,
 *           List<List<Node>> history, Recency table) {
 *
 *           if (readyNodes.size() == 0 || availableProcs.size() == 0) return;
 *
 *           readyNodes.stream().forEach(c -> c.partition = -1);
 *
 *           readyNodes.sort((c1, c2) -> compareNode(dags, c1, c2));
 *
 *           List<Node> preEligible = new ArrayList<>();
 *
 *           for (Node n : readyNodes) { if (n.getDagID() == 1 &&
 *           n.getDagInstNo() == 2 && n.getId() == 5) {
 *           System.out.println("check"); break; } }
 *
 *           for (int i = 0; i < availableProcs.size(); i++) { if (i >=
 *           readyNodes.size()) break; preEligible.add(readyNodes.get(i)); }
 *
 *           List<Integer> availableP = new ArrayList<>(availableProcs);
 *
 *           for (Node n : preEligible) { List<Long> ETdrop = new ArrayList<>();
 *
 *           for (int i = 0; i < availableP.size(); i++) { int proc =
 *           availableP.get(i);
 *
 *           ETdrop.add(n.getWCET() - table.computeET(history, n, proc, true));
 *           }
 *
 *           int procIndex = getIndexOfMaximum(ETdrop); n.partition =
 *           availableP.get(procIndex); availableP.remove(procIndex); } }
 *
 *           private int getIndexOfMaximum(List<Long> l) { int index = -1; long
 *           max = Long.MIN_VALUE;
 *
 *
 *           for (int i = 0; i < l.size(); i++) { if (max < l.get(i)) { max =
 *           l.get(i); index = i; } }
 *
 *           if (index == -1) System.out.println();
 *
 *           return index; }
 *
 ***********************************************************************************/

/******************************************************************************
 * v0.1 ***********************************************************************
 * 
 * @Override public void getEligibileNode(List<DirectedAcyclicGraph> dags,
 *           List<Node> readyNodes, List<Integer> availableProcs,
 *           List<List<Node>> history, Recency table) {
 * 
 *           if (readyNodes.size() == 0) return;
 * 
 *           readyNodes.stream().forEach(c -> c.partition = -1);
 * 
 * 
 *           sort ready nodes list by FPS+WF, take first procNum nodes to
 *           execute.
 * 
 *           readyNodes.sort((c1, c2) -> compareNode(dags, c1, c2));
 * 
 *           if (availableProcs.size() == 1 || readyNodes.size() == 1) {
 *           readyNodes.get(0).partition = availableProcs.get(0); }
 * 
 *           List<Node> preEligible = new ArrayList<>();
 * 
 *           for (int i = 0; i < availableProcs.size(); i++) { if (i >=
 *           readyNodes.size()) break; preEligible.add(readyNodes.get(i)); }
 * 
 *           List<Node> eligibile = new ArrayList<>();
 * 
 *           for (int i = 0; i < availableProcs.size(); i++) { if
 *           (preEligible.size() == 0) break;
 * 
 *           int proc = availableProcs.get(i);
 * 
 *           List<Long> ETdrop = preEligible.stream().map(n -> (n.getWCET() -
 *           table.computeET(history, n, proc, true)))
 *           .collect(Collectors.toList());
 * 
 *           int maxIndex = getIndexOfMaximum(ETdrop);
 * 
 *           preEligible.get(maxIndex).partition = availableProcs.get(i);
 * 
 *           eligibile.add(preEligible.get(maxIndex));
 *           preEligible.remove(maxIndex);
 * 
 * 
 *           }
 * 
 * 
 *           }
 ***********************************************************************************/
