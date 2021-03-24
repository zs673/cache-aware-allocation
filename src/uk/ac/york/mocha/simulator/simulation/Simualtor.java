package uk.ac.york.mocha.simulator.simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import uk.ac.york.mocha.simulator.allocation.AllocationMethod;
import uk.ac.york.mocha.simulator.allocation.SimpleLoadBalancing;
import uk.ac.york.mocha.simulator.dag.DirectedAcyclicGraph;
import uk.ac.york.mocha.simulator.dag.Node;
import uk.ac.york.mocha.simulator.dag.Recency;
import uk.ac.york.mocha.simulator.dag.Node.NodeType;
import uk.ac.york.mocha.simulator.generator.SystemGenerator;

/*
 * This is a Multiprocessor Non-preemptive Multi-DAG Simulator
 */

public class Simualtor {

	public static List<DirectedAcyclicGraph> dags;

	private enum SimuType {
		WCET, ET
	};

	private enum Allocation {
		LOAD_BALANCE, CACHE_AWARE
	};

	/**********************************************************************
	 ***** The gloabl recency table and cache hierarchy of the system *****
	 **********************************************************************/
	private static Recency table;

	public static final int Level2procNum = 4;
	List<List<Integer>> cacheHierarchy;

	/**********************************************************************
	 *********************** Current system time **************************
	 **********************************************************************/
	private long systemTime = 0;

	/**********************************************************************
	 ************************* Runtime queues *****************************
	 **********************************************************************/
	/* a sleeping queue for DAGs waiting to be RELEASED */
	List<DirectedAcyclicGraph> sleepingDAGs;

	/* a ready queue for DAGs waiting to be EXECUTED */
	List<DirectedAcyclicGraph> readyDAGs;

	/* a ready queue for the nodes waiting to be EXECUTED */
	List<Node> readyNodes;

	/* a run queue for EXECUTING nodes */
	Node[] currentExe;

	/* the next available time of each processor */
	long[] allProcs;

	/* a list recording all executed nodes on each processor */
	List<List<Node>> history;

	/********************* Runtime queues *********************************/

	public Simualtor(List<DirectedAcyclicGraph> dags, int procNum, int seed) {
		if (procNum % 4 != 0 || procNum / 4 < 1) {
			System.err.println("Number of cores must be 4, 8, 16, 20...");
			System.exit(-1);
		}

		Simualtor.dags = new ArrayList<>(dags);

		sleepingDAGs = new ArrayList<>(dags);
		readyDAGs = new ArrayList<>();
		readyNodes = new ArrayList<>();
		currentExe = new Node[procNum];
		allProcs = new long[procNum];

		cacheHierarchy = new ArrayList<>();

		int procID = 0;
		for (int i = 0; i < procNum / 4; i++) {
			List<Integer> procPerLevel2 = new ArrayList<>();
			for (int j = 0; j < Level2procNum; j++) {
				procPerLevel2.add(procID);
				procID++;
			}
			cacheHierarchy.add(procPerLevel2);
		}

		table = new Recency(1000);

		history = new ArrayList<>();
		for (int i = 0; i < procNum; i++) {
			List<Node> oneProc = new ArrayList<>();
			history.add(oneProc);
		}
	}

	public void simulate(Allocation alloc, SimuType type) {

		boolean cacheAware = false;
		AllocationMethod allocM = null;

		switch (alloc) {
		case LOAD_BALANCE:
			allocM = new SimpleLoadBalancing();
			break;
		default:
			System.err.println("The simualtion method is NOT supported ! ");
			return;
		}

		switch (type) {
		case WCET:
			cacheAware = false;
			break;
		case ET:
			cacheAware = true;
		default:
			break;
		}

		while (sleepingDAGs.size() > 0 || readyDAGs.size() > 0) {

			/*
			 * Update the maintained list by time
			 */
			UpdateSystemStatus();

			/*
			 * Get all available processors at the current time
			 */
			List<Integer> availableProc = getAvailableCores();

			/*
			 * Execute ready nodes on available processors
			 */
			ExecuteReadyNodes(availableProc, allocM, cacheAware);

			/*
			 * advance to next time unit.
			 */
			oneTick();
		}
	}

	/******************************************************************
	 ********** Choose the next node in the queue to execute **********
	 ******************************************************************/
	private void ExecuteReadyNodes(List<Integer> availableProc, AllocationMethod allocM, boolean cacheAware) {

		/*
		 * get ready nodes to execute by the specified allocation method
		 */
		List<Node> eligibile = allocM.getEligibileNode(readyNodes, availableProc.size());
		assert (eligibile.size() <= availableProc.size());

		for (int i = 0; i < availableProc.size(); i++) {
			if (i >= eligibile.size())
				break;

			Node n = eligibile.get(i);

			n.start = systemTime;
			currentExe[availableProc.get(i)] = n;
			allProcs[availableProc.get(i)] = n.finishAt = systemTime + computeET(n, availableProc.get(i), cacheAware);

			readyNodes.remove(n);
		}

	}

	/*****************************************************************
	 *** Maintain and update run-time queues based on current time ***
	 *****************************************************************/
	public void UpdateSystemStatus() {
		/*
		 * Check 1) whether any executing node finishes at the current time; 2) any
		 * ready node can execute now; 3) any DAG has finished execution.
		 */
		for (int i = 0; i < currentExe.length; i++) {

			Node n = currentExe[i];
			if (n != null && n.finishAt <= systemTime) {
				currentExe[i] = null;
				n.finish = true;

				history.get(i).add(n);

				/*
				 * A node can execute if all its parents are finished
				 */
				for (Node child : n.getChildren()) {
					boolean isReady = true;
					for (Node parent : child.getParent()) {
						if (!parent.finish)
							isReady = false;
					}

					if (isReady)
						readyNodes.add(child);
				}

				/*
				 * A DAG is finished when its SINK node finishes execution
				 */
				if (n.getType().equals(NodeType.SINK)) {
					Utils.getDagByIndex(n.getDagID(), n.getDagInstNo()).finishTime = systemTime;
				}
			}
		}

		/*
		 * Checking whether the sleeping DAGs can be released now.
		 */
		for (int i = 0; i < sleepingDAGs.size(); i++) {
			DirectedAcyclicGraph dag = sleepingDAGs.get(i);
			if (dag.startTime <= systemTime) {

				sleepingDAGs.remove(dag);
				readyDAGs.add(dag);
				readyNodes.add(dag.getSource());
				i--;
			}
		}

		/*
		 * Check whether the ready DAGs are finished now.
		 */
		for (int i = 0; i < readyDAGs.size(); i++) {
			DirectedAcyclicGraph dag = readyDAGs.get(i);
			if (dag.finishTime <= systemTime) {
				readyDAGs.remove(dag);
				System.out.println(dag.printExeInfo());
				i--;
			}
		}
	}

	/****************************************************************
	 *** Advance to next time unit. Sometimes we jump, time flies ***
	 ****************************************************************/
	private void oneTick() {
		/*
		 * We jump to the next available time if all cores are busy.
		 */
		boolean jump = true;
		long min = Long.MAX_VALUE;
		for (long i : allProcs) {
			min = min < i ? min : i;
			if (i <= systemTime)
				jump = false;
		}

		if (jump)
			systemTime = min;
		else
			systemTime++;
	}

	private List<Integer> getAvailableCores() {
		List<Integer> available = new ArrayList<>();
		for (int i = 0; i < allProcs.length; i++) {
			if (allProcs[i] <= systemTime)
				available.add(i);
		}
		return available;
	}

	private long computeET(Node n, int proc, boolean cacheAware) {

		long ET = n.getWCET();

		if (!cacheAware)
			return ET;

		/**
		 * Compute recency distance at each cache level
		 */
		/* level 1 recency distance */
		int level1Distance = history.get(proc).size() - history.get(proc).lastIndexOf(n);

		/* level 2 recency distance */
		List<Integer> Level2Procs = new ArrayList<>();

		for (List<Integer> group : cacheHierarchy) {
			if (group.contains(proc)) {
				Level2Procs.addAll(group);
				break;
			}
		}

		if (Level2Procs.size() == 0) {
			System.err.println("Simualtor.computeET()" + ": " + "Processor not found!");
			System.exit(-1);
		}

		List<Node> finishedNodes = new ArrayList<>();
		for (Integer index : Level2Procs) {
			finishedNodes.addAll(history.get(index));
		}
		finishedNodes.sort((c1, c2) -> compareNodeForRecency(c1, c2, n));

		int level2Distance = finishedNodes.size() - finishedNodes.lastIndexOf(n);

		/* level 3 recency distance */
		List<Node> allhistory = history.stream().flatMap(c -> c.stream()).collect(Collectors.toList());

		allhistory.sort((c1, c2) -> compareNodeForRecency(c1, c2, n));
		int level3Distance = allhistory.size() - allhistory.lastIndexOf(n);

		if (level1Distance <= Recency.recencyDepth[0]) {
			long ET1 = (long) Math.ceil((double) n.getWCET() * table.recencyTable.get(0).get(level1Distance - 1));
			ET = ET < ET1 ? ET : ET1;
		}
		if (level2Distance <= Recency.recencyDepth[1]) {
			long ET2 = (long) Math.ceil((double) n.getWCET() * table.recencyTable.get(1).get(level2Distance - 1));
			ET = ET < ET2 ? ET : ET2;
		}
		if (level3Distance <= Recency.recencyDepth[2]) {
			long ET3 = (long) Math.ceil((double) n.getWCET() * table.recencyTable.get(2).get(level3Distance - 1));
			ET = ET < ET3 ? ET : ET3;
		}

		return ET;
	}

	private int compareNodeForRecency(Node n1, Node n2, Node current) {

		int compare = -Long.compare(n1.finishAt, n2.finishAt);

		if (compare == 0) {
			if (n1.equals(current))
				compare = -1;
			if (n2.equals(current))
				compare = 1;
		}

		return compare;
	}

	public static void main(String args[]) {

		int cores = 4;

		SystemGenerator gen = new SystemGenerator(100, 1000, cores, 2, true, 1000);
		List<DirectedAcyclicGraph> dags = gen.generatedDAGInstancesInOneHP();

		Simualtor sim = new Simualtor(dags, cores, 1000);
		sim.simulate(Allocation.LOAD_BALANCE, SimuType.ET);
		System.out.println("finished");
	}

}
