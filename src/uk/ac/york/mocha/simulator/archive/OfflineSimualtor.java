package uk.ac.york.mocha.simulator.archive;
//package uk.ac.york.mocha.simulator.simulator;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Collectors;
//
//import uk.ac.york.mocha.simulator.allocation.AllocationMethod;
//import uk.ac.york.mocha.simulator.allocation.LoadBalancing;
//import uk.ac.york.mocha.simulator.allocation.CacheAwareAlloc;
//import uk.ac.york.mocha.simulator.dag.DirectedAcyclicGraph;
//import uk.ac.york.mocha.simulator.dag.Node;
//import uk.ac.york.mocha.simulator.dag.Node.NodeType;
//import uk.ac.york.mocha.simulator.dag.Recency;
//import uk.ac.york.mocha.simulator.dag.Recency.RecencyType;
//import uk.ac.york.mocha.simulator.generator.SystemGenerator;
//
///*
// * This is a Multiprocessor Non-preemptive Multi-DAG Simulator
// */
//
//public class OfflineSimualtor {
//
//	/**********************************************************************
//	 ********************** Simualtor Configuration ***********************
//	 **********************************************************************/
//	public enum SimuType {
//		CLOCK_LEVEL, NODE_LEVEL
//	};
//
//	public enum Hardware {
//		PROC_ONLY, PROC_CACHE
//	};
//
//	public enum Allocation {
//		LOAD_BALANCE, CACHE_AWARE
//	};
//
//	private SimuType type;
//	private Hardware hardware;
//	private Allocation alloc;
//
//	/**********************************************************************
//	 ***** The gloabl recency table and cache hierarchy of the system *****
//	 **********************************************************************/
//	private static Recency table;
//
//	/**********************************************************************
//	 ************************ DAGs to be executed *************************
//	 **********************************************************************/
//	public List<DirectedAcyclicGraph> dags;
//
//	/**********************************************************************
//	 *********************** Current system time **************************
//	 **********************************************************************/
//	private long systemTime = 0;
//
//	/**********************************************************************
//	 ************************* Runtime queues *****************************
//	 **********************************************************************/
//	/* a sleeping queue for DAGs waiting to be RELEASED */
//	List<DirectedAcyclicGraph> sleepingDAGs;
//
//	/* a ready queue for DAGs waiting to be EXECUTED */
//	List<DirectedAcyclicGraph> readyDAGs;
//
//	/* a ready queue for the nodes waiting to be EXECUTED */
//	List<Node> readyNodes;
//
//	/* a run queue for EXECUTING nodes */
//	Node[] currentExe;
//
//	/* the next available time of each processor */
//	long[] allProcs;
//
//	/* a list recording all executed nodes on each processor */
//	List<List<Node>> history;
//
//	/********************* Runtime queues *********************************/
//
//	public OfflineSimualtor(SimuType type, Hardware hardware, Allocation alloc, List<DirectedAcyclicGraph> dags,
//			int procNum, int recencySeed) {
//		if (procNum % 4 != 0 || procNum / 4 < 1) {
//			System.err.println("Number of cores must be 4, 8, 16, 20...");
//			System.exit(-1);
//		}
//
//		this.type = type;
//		this.hardware = hardware;
//		this.alloc = alloc;
//
//		this.dags = new ArrayList<>(dags);
//
//		this.sleepingDAGs = new ArrayList<>(dags);
//		this.readyDAGs = new ArrayList<>();
//		this.readyNodes = new ArrayList<>();
//		this.currentExe = new Node[procNum];
//		this.allProcs = new long[procNum];
//
//		table = new Recency(RecencyType.ORDER, procNum, recencySeed);
//
//		this.history = new ArrayList<>();
//		for (int i = 0; i < procNum; i++) {
//			List<Node> oneProc = new ArrayList<>();
//			this.history.add(oneProc);
//		}
//	}
//
//	public List<Long> simulate(boolean online, boolean printSim) {
//
//		/*
//		 * Reset Run-time parameters of DAGs and their nodes
//		 */
//		for (DirectedAcyclicGraph dag : dags)
//			dag.reset();
//
//		boolean cacheAware = false;
//		AllocationMethod allocM = null;
//
//		switch (alloc) {
//		case LOAD_BALANCE:
//			allocM = new LoadBalancing();
//			break;
//		case CACHE_AWARE:
//			allocM = new CacheAwareAlloc();
//			break;
//		default:
//			System.err.println("The simualtion method is NOT supported ! ");
//			return null;
//		}
//
//		switch (hardware) {
//		case PROC_ONLY:
//			cacheAware = false;
//			break;
//		case PROC_CACHE:
//			cacheAware = true;
//		default:
//			break;
//		}
//
//		if (online) {
//			while (sleepingDAGs.size() > 0 || readyDAGs.size() > 0) {
//
//				/*
//				 * Update the maintained list by time
//				 */
//				UpdateSystemStatus();
//
//				/*
//				 * Get all available processors at the current time
//				 */
//				List<Integer> availableProc = getAvailableCores();
//
//				/*
//				 * Execute ready nodes on available processors
//				 */
//				ExecuteReadyNodes(availableProc, allocM, cacheAware);
//
//				/*
//				 * advance to next time unit.
//				 */
//				oneTick();
//			}
//		} else {
//			while (sleepingDAGs.size() > 0 || readyDAGs.size() > 0) {
//
//				/*
//				 * Execute ready nodes on available processors
//				 */
//				ExecuteNodesOffline(allocM, cacheAware);
//
//			}
//		}
//
//		/*
//		 * We summarise and report the simualtion results here
//		 */
//		if (printSim) {
//			reprotSimulationResult();
//		}
//
//		/*
//		 * Summarise finish time of each DAG instance
//		 */
//		List<Long> finishTimes = dags.stream().map(c -> (c.finishTime - c.startTime)).collect(Collectors.toList());
//
//		return finishTimes;
//
//	}
//
//	private void ExecuteNodesOffline(AllocationMethod allocM, boolean cacheAware) {
//
//		for (int i = 0; i < sleepingDAGs.size(); i++) {
//			if (sleepingDAGs.get(i).startTime == 0) {
//				readyDAGs.add(sleepingDAGs.get(i));
//				sleepingDAGs.remove(i);
//				i--;
//			}
//		}
//
//		readyDAGs.sort((d1,
//				d2) -> -Integer.compare(d1.getSchedParameters().getPriority(), d2.getSchedParameters().getPriority()));
//
//		while (sleepingDAGs.size() > 0 || readyDAGs.size() > 0) {
//
//		}
//	}
//
//	/*****************************************************************
//	 *** Maintain and update run-time queues based on current time ***
//	 *****************************************************************/
//	public void UpdateSystemStatus() {
//		/*
//		 * Check 1) whether any executing node finishes at the current time; 2) any
//		 * ready node can execute now; 3) any DAG has finished execution.
//		 */
//		for (int i = 0; i < currentExe.length; i++) {
//
//			Node n = currentExe[i];
//			if (n != null && n.finishAt <= systemTime) {
//				currentExe[i] = null;
//				n.finish = true;
//
//				history.get(i).add(n);
//
//				/*
//				 * A node can execute if all its parents are finished
//				 */
//				for (Node child : n.getChildren()) {
//					boolean isReady = true;
//					for (Node parent : child.getParent()) {
//						if (!parent.finish)
//							isReady = false;
//					}
//
//					if (isReady)
//						readyNodes.add(child);
//				}
//
//				/*
//				 * A DAG is finished when its SINK node finishes execution
//				 */
//				if (n.getType().equals(NodeType.SINK)) {
//					Utils.getDagByIndex(dags, n.getDagID(), n.getDagInstNo()).finishTime = systemTime;
//				}
//			}
//		}
//
//		/*
//		 * Checking whether the sleeping DAGs can be released now.
//		 */
//		for (int i = 0; i < sleepingDAGs.size(); i++) {
//			DirectedAcyclicGraph dag = sleepingDAGs.get(i);
//			if (dag.startTime <= systemTime) {
//
//				sleepingDAGs.remove(dag);
//				readyDAGs.add(dag);
//				readyNodes.add(dag.getSource());
//				i--;
//			}
//		}
//
//		/*
//		 * Check whether the ready DAGs are finished now.
//		 */
//		for (int i = 0; i < readyDAGs.size(); i++) {
//			DirectedAcyclicGraph dag = readyDAGs.get(i);
//			if (dag.finishTime <= systemTime) {
//				readyDAGs.remove(dag);
//				i--;
//			}
//		}
//	}
//
//	/******************************************************************
//	 ********** Choose the next node in the queue to execute **********
//	 ******************************************************************/
//	private void ExecuteReadyNodes(List<Integer> availableProc, AllocationMethod allocM, boolean cacheAware) {
//
//		/*
//		 * get ready nodes to execute by the specified allocation method
//		 */
//		allocM.getEligibileNode(dags, readyNodes, availableProc, history, table);
//
//		for (int i = 0; i < readyNodes.size(); i++) {
//			if (readyNodes.get(i).partition == -1)
//				continue;
//
//			Node n = readyNodes.get(i);
//
//			n.start = systemTime;
//
//			currentExe[n.partition] = n;
//			allProcs[n.partition] = n.finishAt = systemTime + table.computeET(history, n, n.partition, cacheAware);
//
//			readyNodes.remove(n);
//			i--;
//		}
//
//	}
//
//	/****************************************************************
//	 *** Advance to next time unit. Sometimes we jump, time flies ***
//	 ****************************************************************/
//	private void oneTick() {
//		/*
//		 * We jump to the next available time if all cores are busy.
//		 */
//		boolean jump = true;
//		long min = Long.MAX_VALUE;
//		for (long i : allProcs) {
//			min = min < i ? min : i;
//			if (i <= systemTime)
//				jump = false;
//		}
//
//		if (jump)
//			systemTime = min;
//		else
//			systemTime++;
//	}
//
//	private List<Integer> getAvailableCores() {
//		List<Integer> available = new ArrayList<>();
//		for (int i = 0; i < allProcs.length; i++) {
//			if (allProcs[i] <= systemTime)
//				available.add(i);
//		}
//		return available;
//	}
//
//	/******************************************************************
//	 ****************** Summarise and report results ******************
//	 ******************************************************************/
//	private String reprotSimulationResult() {
//		String res = "Simulation type: " + type.toString() + "    " + "Allocation: " + alloc;
//
//		System.out.println("*****************************************************************");
//		System.out.println(res);
//		System.out.println("*****************************************************************");
//
//		res += "\n\n";
//
//		System.out.println(
//				"\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> Execution Trace <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
//
//		List<List<Long>> finishTimes = new ArrayList<>();
//
//		for (List<Node> nodes : history) {
//			List<Long> finishPerProc = nodes.stream().map(c -> c.finishAt).collect(Collectors.toList());
//			finishTimes.add(finishPerProc);
//		}
//
//		res += "Execuation Trace: \n\n";
//
//		for (int i = 0; i < history.size(); i++) {
//
//			if (i % 4 == 0) {
//				res += "Level 2 Cache Group: " + i + "\n";
//				System.out.println(">>> Level 2 Cache Group: " + i + ":");
//			}
//			res += "    Processor: " + i + "\n";
//			System.out.println(">>>   Processor: " + i);
//
//			for (Node n : history.get(i)) {
//				res += "        " + n.getExeInfo() + ", \n";
//				n.printExeInfo(">>>     ");
//			}
//
//			res += "\n";
//		}
//		System.out.println(
//				">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> Execution Trace End <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<\n\n");
//
//		System.out.println(
//				"--------------------------------------- DAG Execution Summary ---------------------------------------------");
//
//		res += "DAG Execution Summary: \n\n";
//
//		for (DirectedAcyclicGraph dag : dags) {
//			res += "DAG_" + dag.id + "_" + dag.instanceNo + "   finishes at  dag.finishTime. \n";
//			System.out.printf(
//					"---  DAG_" + dag.id + "_" + dag.instanceNo
//							+ "starts at t=%8d,   finishes at t=%8d,   duration t=%8d. \n",
//					dag.startTime, dag.finishTime, (dag.finishTime - dag.startTime));
//		}
//
//		System.out.println(
//				"--------------------------------------- DAG Execution Summary ---------------------------------------------");
//
//		return res;
//	}
//
//	public static void main(String args[]) {
//
//		int cores = 8;
//
//		SystemGenerator gen = new SystemGenerator(100, 1000, cores, 2, true, 1000);
//		List<DirectedAcyclicGraph> dags = gen.generatedDAGInstancesInOneHP(-1,null);
//
//		OfflineSimualtor sim = new OfflineSimualtor(SimuType.CLOCK_LEVEL, Hardware.PROC_CACHE, Allocation.LOAD_BALANCE,
//				dags, cores, 1000);
//		sim.simulate(true, false);
//		System.out.println("finished");
//	}
//
//}
