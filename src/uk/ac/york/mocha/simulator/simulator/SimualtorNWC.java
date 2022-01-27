package uk.ac.york.mocha.simulator.simulator;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.Pair;

import uk.ac.york.mocha.simulator.allocation.AllocationMethods;
import uk.ac.york.mocha.simulator.allocation.OnlineAndOffline;
import uk.ac.york.mocha.simulator.allocation.OnlineCacheAware;
import uk.ac.york.mocha.simulator.allocation.OnlineCacheAwareRobust;
import uk.ac.york.mocha.simulator.allocation.OnlineCacheAwareWithOrdering;
import uk.ac.york.mocha.simulator.allocation.OnlineFFD;
import uk.ac.york.mocha.simulator.allocation.OnlineRandom;
import uk.ac.york.mocha.simulator.allocation.OnlineWFD;
import uk.ac.york.mocha.simulator.allocation.OnlineWFWithOrdering;
import uk.ac.york.mocha.simulator.allocation.SimpleAllocation;
import uk.ac.york.mocha.simulator.allocation.onlineBFD;
import uk.ac.york.mocha.simulator.dag.DirectedAcyclicGraph;
import uk.ac.york.mocha.simulator.dag.Node;
import uk.ac.york.mocha.simulator.dag.Node.NodeType;
import uk.ac.york.mocha.simulator.dag.RecencyProfile;
import uk.ac.york.mocha.simulator.generator.SystemGenerator;
import uk.ac.york.mocha.simulator.parameters.SystemParameters;
import uk.ac.york.mocha.simulator.parameters.SystemParameters.Allocation;
import uk.ac.york.mocha.simulator.parameters.SystemParameters.Hardware;
import uk.ac.york.mocha.simulator.parameters.SystemParameters.RecencyType;
import uk.ac.york.mocha.simulator.parameters.SystemParameters.SimuType;

/*
 * This is a Multiprocessor Non-preemptive Multi-DAG Simulator
 */

public class SimualtorNWC {

	private SimuType type;
	private Hardware hardware;
	private Allocation alloc;

	List<Integer> cores;

	/**********************************************************************
	 ***** The global recency table and cache hierarchy of the system *****
	 **********************************************************************/
	private static RecencyProfile profile;

	/**********************************************************************
	 ************************ DAGs to be executed *************************
	 **********************************************************************/
	public List<DirectedAcyclicGraph> dags;

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

	/* ready queue per core */
	List<List<Node>> localRunQueue;

	/* the next available time of each processor */
	long[] coreTime;

	/* Control for enable/disable week cluster-level affinity */
	boolean lcif;

	/* cache performance */
	long[] cachePerformance;
	int totalAccess = 0;

	boolean recency_fault = false;

	/* a list recording all allocated nodes on each processor */
	List<List<Node>> allocHistory;

	/* A list containing allocation history, for printing and debugging. */
	List<String[]> allocHistoryInfo;

	/* Execution history by cache level */
	List<List<Node>> history_level1;
	List<List<Node>> history_level2;
	List<Node> history_level3;

	DecimalFormat df = new DecimalFormat("#.###");

	/********************* Runtime queues *********************************/

	public SimualtorNWC(SimuType type, Hardware hardware, Allocation alloc, RecencyType recency, List<DirectedAcyclicGraph> dags,
			int procNum, int recencySeed, boolean lcif, boolean recency_fault) {

		this.type = type;
		this.hardware = hardware;
		this.alloc = alloc;

		this.dags = new ArrayList<>(dags);

		this.sleepingDAGs = new ArrayList<>(dags);
		sleepingDAGs.sort((c1, c2) -> Long.compare(c1.releaseTime, c2.releaseTime));

		this.readyDAGs = new ArrayList<>();
		this.readyNodes = new ArrayList<>();
		this.currentExe = new Node[procNum];
		this.coreTime = new long[procNum];

		localRunQueue = new ArrayList<>();
		for (int i = 0; i < procNum; i++) {
			localRunQueue.add(new ArrayList<>());
		}

		cores = new ArrayList<>();
		for (int i = 0; i < procNum; i++) {
			cores.add(i);
		}

		this.lcif = lcif;

		profile = new RecencyProfile(recency, procNum, recencySeed);

		this.history_level1 = new ArrayList<>();
		this.history_level2 = new ArrayList<>();
		this.history_level3 = new ArrayList<>();

		this.allocHistory = new ArrayList<>();
		this.allocHistoryInfo = new ArrayList<>();

		for (int i = 0; i < procNum; i++) {
			List<Node> oneProc = new ArrayList<>();
			this.history_level1.add(oneProc);

			List<Node> oneProcAlloc = new ArrayList<>();
			this.allocHistory.add(oneProcAlloc);
		}

		for (int i = 0; i < (procNum / SystemParameters.Level2CoreNum); i++) {
			List<Node> oneCluster = new ArrayList<>();
			this.history_level2.add(oneCluster);
		}

		this.cachePerformance = new long[4];
		this.recency_fault = recency_fault;

	}

	public Pair<List<DirectedAcyclicGraph>, double[]> simulate(boolean printSim) {
		return simulate(printSim, 0);
	}

	public Pair<List<DirectedAcyclicGraph>, double[]> simulate(boolean printSim, int onlyCritical) {

		/*
		 * Reset Run-time parameters of DAGs and their nodes
		 */
		for (DirectedAcyclicGraph dag : dags) {
			dag.reset();
		}

		boolean cacheAware = false;
		AllocationMethods allocM = null;

		switch (alloc) {
		case SIMPLE:
			allocM = new SimpleAllocation();
			break;
		case RANDOM:
			allocM = new OnlineRandom();
			break;
		case BEST_FIT:
			allocM = new onlineBFD();
			break;
		case WORST_FIT:
			allocM = new OnlineWFD();
			break;
		case FIRST_FIT:
			allocM = new OnlineFFD();
			break;
		case CACHE_AWARE:
			allocM = new OnlineCacheAware();
			break;
		case CACHE_AWARE_ROBUST:
			allocM = new OnlineCacheAwareRobust();
			break;
		case OFFLINE_CACHE_AWARE:
			allocM = new OnlineAndOffline();
			break;
		case CACHE_AWARE_OUR:
			Utils.assignPriorityOur(dags);
			allocM = new OnlineCacheAwareWithOrdering();
			break;
		case WORST_FIT_OUR:
			Utils.assignPriorityOur(dags);
			allocM = new OnlineWFWithOrdering();
			break;
		default:
			System.err.println("The simualtion method is NOT supported ! ");
			System.exit(-1);
			return null;
		}

		switch (hardware) {
		case PROC_ONLY:
			cacheAware = false;
			break;
		case PROC_CACHE:
			cacheAware = true;
		default:
			break;
		}

		debug_output_begin(printSim);

		while (!finished()) {
			/*
			 * Update the maintained list by time
			 */
			UpdateSystemStatus();

			/*
			 * Execute ready nodes on available processors
			 */
			allocateAndExecute(allocM, cacheAware, onlyCritical, printSim);

			/*
			 * advance to next time unit.
			 */
			advance();
		}

		debug_output_end(printSim);

		List<DirectedAcyclicGraph> result = new ArrayList<>();

		for (DirectedAcyclicGraph d : dags) {
			result.add(d.deepCopy());
		}

		double[] cachePerf = new double[cachePerformance.length];
		for (int i = 0; i < cachePerformance.length; i++) {
			double d = (double) cachePerformance[i] / (double) totalAccess;
			cachePerf[i] = Double.parseDouble(df.format(d));
		}

		return new Pair<>(result, cachePerf);

	}

	/******************************************************************
	 ************* Check whether all workload is finished *************
	 ******************************************************************/
	private boolean finished() {

		if (sleepingDAGs.size() > 0)
			return false;
		if (readyDAGs.size() > 0)
			return false;

//		for (Node n : currentExe) {
//			if (n != null)
//				return false;
//		}
//
//		for (List<Node> nl : localRunQueue)
//			if (nl.size() > 0)
//				return false;

		return true;
	}

	/*****************************************************************
	 *** Maintain and update run-time queues based on current time ***
	 *****************************************************************/
	public void UpdateSystemStatus() {

		/*
		 * Check 1) whether any executing node finishes at the current time; 2)
		 * any ready node can execute now; 3) any DAG has finished execution.
		 */
		for (int i = 0; i < currentExe.length; i++) {

			/*
			 * Check whether the current running node is finished now.
			 */
			Node n = currentExe[i];
			if (n != null && n.finishAt <= systemTime) {
				currentExe[i] = null;
				n.finish = true;

				DirectedAcyclicGraph d = Utils.getDagByIndex(dags, n.getDagID(), n.getDagInstNo());
				d.allocNodes.add(n);

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
					Utils.getDagByIndex(dags, n.getDagID(), n.getDagInstNo()).finishTime = systemTime;
				}
			}

		}

		/*
		 * Checking whether the sleeping DAGs can be released now.
		 */
		for (int i = 0; i < sleepingDAGs.size(); i++) {
			DirectedAcyclicGraph dag = sleepingDAGs.get(i);
			if (dag.releaseTime <= systemTime) {

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
				i--;
			}
		}
	}

	/******************************************************************
	 ********** Choose the next node in the queue to execute **********
	 ******************************************************************/
	private void allocateAndExecute(AllocationMethods allocM, boolean cacheAware, int onlyCritical, boolean printSim) {

		/*
		 * get ready nodes to execute by the specified allocation method
		 */
		allocM.allocate(dags, readyNodes, localRunQueue, cores, coreTime, null, null, null, allocHistory, profile, systemTime, lcif);

		///////////////// Debug Output //////////////////////
		String[] oneSched = new String[coreTime.length];
		for (int i = 0; i < oneSched.length; i++) {
			if (coreTime[i] > systemTime) {
				oneSched[i] = "*";
			} else {
				oneSched[i] = "-";
			}
		}
		boolean add = false;
		///////////////// Debug Output //////////////////////

		/*
		 * Execute the ready node on each core, if the core is currently free.
		 */
		for (int i = 0; i < currentExe.length; i++) {
			/*
			 * Check whether nodes in local ready queue can start
			 */
			List<Node> localReadyNodes = localRunQueue.get(i);
			if (currentExe[i] == null && localReadyNodes.size() > 0) {
				Node n = localReadyNodes.get(0);
				localReadyNodes.remove(0);

				currentExe[i] = n;

				boolean falutsOccur = false;
				if (recency_fault && onlyCritical == 0) {
					falutsOccur = true;
				}

				if (recency_fault && onlyCritical == 1 && n.isCritical) {
					falutsOccur = true;
				}

				if (recency_fault && onlyCritical == 2 && !n.isCritical) {
					falutsOccur = true;
				}

				Pair<Long, Integer> ETWithCache = profile.computeET(-1, history_level1, history_level2, history_level3, n, n.partition,
						cacheAware, 0, falutsOccur);

				long realET = ETWithCache.getFirst();

				n.start = systemTime;
				coreTime[n.partition] = n.finishAt = systemTime + realET;
				n.expectedET = realET;

				int cacheEffects = ETWithCache.getSecond();
				cachePerformance[cacheEffects - 1] = cachePerformance[cacheEffects - 1] + 1;

				totalAccess++;

				/*
				 * A DAG is started when its SOURCE node starts execution
				 */
				if (n.getType().equals(NodeType.SOURCE)) {
					Utils.getDagByIndex(dags, n.getDagID(), n.getDagInstNo()).startTime = systemTime;
				}

				/* add the node to history for each cache level */
				history_level1.get(n.partition).add(n);
				int clusterID = n.partition / SystemParameters.Level2CoreNum;
				history_level2.get(clusterID).add(n);
				history_level3.add(n);

				///////////////// Debug Output //////////////////////
				// oneSched[n.partition] = n.getDagID() + "_" + n.getDagInstNo()
				// + "_" + n.getId() + ":" + n.finishAt;
				oneSched[n.partition] = n.getDagID() + "_" + n.getDagInstNo() + "_" + n.getId();
				add = true;
				///////////////// Debug Output //////////////////////
			}
		}

		if (add) {
			allocHistoryInfo.add(oneSched);

			debug_print_allocation(printSim, oneSched);

		}

	}

	/****************************************************************
	 *** Advance to next time unit. Sometimes we jump, time flies ***
	 ****************************************************************/
	private void advance() {
		long oldTime = systemTime;
		/*
		 * We jump to the next available time if all cores are busy.
		 */
		boolean jump = true;
		long firstFinish = Long.MAX_VALUE;
		for (long i : coreTime) {
			firstFinish = firstFinish < i ? firstFinish : i;
			if (i <= systemTime) {
				jump = false;
				break;
			}
		}

		if (jump)
			systemTime = firstFinish;
		else {

			long earliestFinish = Long.MAX_VALUE;

			/*
			 * earliest finish of executing node.
			 */
			for (long i : coreTime) {
				if (i > systemTime)
					earliestFinish = earliestFinish < i ? earliestFinish : i;
			}

			/*
			 * earliest release of sleeping node
			 */
			long earliestRelease = sleepingDAGs.size() > 0 ? sleepingDAGs.get(0).releaseTime : Long.MAX_VALUE;

			systemTime = earliestFinish < earliestRelease ? earliestFinish : earliestRelease;
		}

		if (systemTime == oldTime) {
			System.out.println("Simualtor.advance(): timing error. The system time is not advanced! ");
			System.exit(-1);
		}

		if (systemTime == Long.MAX_VALUE && (sleepingDAGs.size() > 0 || readyNodes.size() > 0)) {
			System.out.println("Simualtor.advance(): timing error. The system time is Long Max but there is still waiting nodes/DAGs ! ");
			System.exit(-1);
		}
	}

	/******************************************************************
	 ***************** Debug Info and Results Summary *****************
	 ******************************************************************/
	private void debug_output_begin(boolean printSim) {
		if (printSim) {
			String res = "Simulation type: " + type.toString() + "    " + "Allocation: " + alloc;

			System.out.println("*****************************************************************");
			System.out.println(res);
			System.out.println("*****************************************************************");

			res += "\n\n";

			System.out.println(
					"\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> Allocation Info <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");

			System.out.printf("%10s    ", "sysTime");
			for (int i = 0; i < cores.size(); i++) {
				System.out.printf("%15s    ", "Core:" + i);
			}
			System.out.println();

		}
	}

	private void debug_print_allocation(boolean printSim, String[] oneSched) {
		if (printSim) {
			System.out.printf("%10s    ", systemTime);
			for (String s : oneSched) {
				System.out.printf("%15s    ", s);
			}
			System.out.println();

			// System.out.println("System Time: " + systemTime);
			// System.out.println("Sleeping DAGs: " +
			// Arrays.toString(sleepingDAGs.toArray()));
			// System.out.println("Ready DAGs: " +
			// Arrays.toString(sleepingDAGs.toArray()));
			// System.out.println("Ready Nodes: " +
			// Arrays.toString(readyNodes.toArray()));
			// System.out.println("Running Nodes: " +
			// Arrays.toString(currentExe));
		}

	}

	private void debug_output_end(boolean printSim) {
		if (printSim) {
			System.out.println(
					"\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> Allocation Info <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");

			System.out.println("\n\n\n");
			System.out
					.println("--------------------------------------- DAG Execution Summary ---------------------------------------------");

			for (DirectedAcyclicGraph dag : dags) {
				System.out.printf(
						"---  DAG_" + dag.id + "_" + dag.instanceNo + "    starts at t=%8d,   finishes at t=%8d,   makespan t=%8d. \n",
						dag.releaseTime, dag.finishTime, (dag.finishTime - dag.releaseTime));
			}

			System.out
					.println("--------------------------------------- DAG Execution Summary ---------------------------------------------");
		}

	}

	public static void main(String args[]) {

		for (int i = 0; i < 1; i++) {
			SystemParameters.utilPerTask = 0.2;

			SystemGenerator gen = new SystemGenerator(SystemParameters.coreNum, 1, true, true, null, i, true, false);
			List<DirectedAcyclicGraph> dags = gen.generatedDAGInstancesInOneHP(1, -1, null, false);

			// SimualtorNWC cacheBFSim = new SimualtorNWC(SimuType.CLOCK_LEVEL,
			// Hardware.PROC_CACHE,
			// Allocation.CACHE_AWARE_ROBUST, RecencyType.TIME_DEFAULT, dags,
			// SystemParameters.coreNum, i, true,
			// true);
			// cacheBFSim.simulate(true, 0);

			// System.out.println("The " + i + "th systen FINISHED!");
			//
			SimualtorNWC cacheBFSim1 = new SimualtorNWC(SimuType.CLOCK_LEVEL, Hardware.PROC_CACHE, Allocation.SIMPLE,
					RecencyType.TIME_DEFAULT, dags, SystemParameters.coreNum, i, true, true);
			cacheBFSim1.simulate(true, 0);
		}

	}

}
