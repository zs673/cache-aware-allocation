package uk.ac.york.mocha.simulator.experiments_perdictability;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.util.Pair;
import org.python.modules.math;

import uk.ac.york.mocha.simulator.entity.DirectedAcyclicGraph;
import uk.ac.york.mocha.simulator.entity.Node;
import uk.ac.york.mocha.simulator.generator.CacheHierarchy;
import uk.ac.york.mocha.simulator.generator.SystemGenerator;
import uk.ac.york.mocha.simulator.parameters.SystemParameters;
import uk.ac.york.mocha.simulator.parameters.SystemParameters.Allocation;
import uk.ac.york.mocha.simulator.parameters.SystemParameters.Hardware;
import uk.ac.york.mocha.simulator.parameters.SystemParameters.RecencyType;
import uk.ac.york.mocha.simulator.parameters.SystemParameters.SimuType;
import uk.ac.york.mocha.simulator.simulator.SimualtorNWC;
import uk.ac.york.mocha.simulator.simulator.Utils;

/* Number, Type, Effect */

/*
 * Show the climb effects, can we model that as pressure, i.e. if the pressure
 * is higher than the threshold then it will impact the makespan?
 */

public class VariabilityRandom {

	static DecimalFormat df = new DecimalFormat("#.###");

	public static enum faultType {
		all_nodes, all_critical, all_non_critical, high_et, high_pathNum, high_pathET, sensivitiy, high_in_degree, high_out_degree, high_in_out_degree, statSensitivity
	}

	static int nop = 4;
	static int nos = 10;
	static int[] allCores = { 4 };
	static boolean print = false;

	static double maxPercent = 0.5;
	static int maxEffect = 20000;

	static int[] allInstanceNum = { 1, 3, 5, 10 };

	static Random rng = new Random(1000);

	public static void main(String args[]) {

		start(nop);

	}

	public static void start(int nop) {

		faults(allCores[0], allInstanceNum[0], nop);
	}

	public static synchronized void addAll(List<List<Long>> res, List<List<Long>> add) {
		res.addAll(add);
	}

	public static void faults(int cores, int instanceNum, int nop) {
		final int initialSeed = 1000;

		List<List<Long>> allResult = new ArrayList<>();

		List<Thread> runners = new ArrayList<>();

		for (int i = 0; i < nop; i++) {

			final int id = i;
			final int workload = (int) Math.ceil((double) nos / (double) nop);

			runners.add(new Thread(new Runnable() {

				@Override
				public void run() {
					int startingSeed = initialSeed + id * workload;
					List<List<Long>> result = runOneThread(cores, instanceNum, startingSeed, workload, id);
					addAll(allResult, result);
				}
			}));
		}

		for (Thread t : runners)
			t.start();

		for (Thread t : runners)
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		String out = "";
		for (List<Long> ll : allResult) {
			for (int k = 0; k < ll.size(); k++) {
				// System.out.print(ll.get(k) + " ");
				out += ll.get(k);
				if (k != ll.size() - 1)
					out += " ";
			}
			// System.out.println();
			out += "\n";
		}

		String folderName = "result/" + "faults_new/";
		String fileName = "/ran" + "_" + cores + "_" + instanceNum + ".txt";
		Utils.writeResult(folderName, fileName, out);
	}

	public static List<List<Long>> runOneThread(int cores, int instanceNum, int startingSeed, int workload, int id) {

		int seed = startingSeed;

		List<List<Long>> res = new ArrayList<>();

		for (int i = 0; i < workload; i++) {
			System.out.println("No. of system: " + (i + id * workload) + " --- " + "cores: " + cores + ", No. instance: " + instanceNum);

			SystemGenerator gen = new SystemGenerator(SystemParameters.coreNum, 1, true, true, null, seed, true, print);
			Pair<List<DirectedAcyclicGraph>, CacheHierarchy> sys = gen.generatedDAGInstancesInOneHP(instanceNum, -1, null, false);

			List<Long> results = new ArrayList<>();

			results.addAll(run(sys, cores, seed, print));

			res.add(results);
			seed++;
		}

		return res;
	}

	public static List<Long> run(Pair<List<DirectedAcyclicGraph>, CacheHierarchy> sys, int cores, int seed, boolean print) {

		List<Long> makespans = new ArrayList<>();

		setUpSpecificFaults(sys.getFirst(), print);
		long makespan_opposite = oneRun(sys, cores, seed, print);
		makespans.add(makespan_opposite);

		return makespans;
	}

	public static List<List<Node>> setUpSpecificFaults(List<DirectedAcyclicGraph> dags, boolean print) {

		List<List<Node>> faultNodesinDAGs = new ArrayList<>();

		for (DirectedAcyclicGraph d : dags)
			for (Node n : d.getFlatNodes())
				n.hasFaults = false;

		for (DirectedAcyclicGraph d : dags) {

			List<Node> allNodes = new ArrayList<Node>(d.getFlatNodes());

			List<Node> faultNodes = new ArrayList<>();
			int faultNodeNum = 0;

			double percent = rng.nextDouble() * maxPercent;
			long effect = rng.nextInt(maxEffect);

			Collections.shuffle(allNodes);

			faultNodeNum = (int) math.ceil((double) percent * (double) allNodes.size());
			for(int i=0; i<faultNodeNum; i++) {
				faultNodes.add(allNodes.get(i));
			}

			for (Node n : faultNodes) {
				n.hasFaults = true;
				n.cvp.median = 0;
				n.cvp.range = effect;
				if (print)
					System.out.println(n.toString() + ": " + n.cvp.median + ", " + n.cvp.range);
			}

			faultNodesinDAGs.add(faultNodes);

		}

		return faultNodesinDAGs;
	}

	public static long oneRun(Pair<List<DirectedAcyclicGraph>, CacheHierarchy> sys, int cores, int seed, boolean print) {
		SimualtorNWC no_fault = new SimualtorNWC(SimuType.CLOCK_LEVEL, Hardware.PROC_CACHE, Allocation.CACHE_AWARE_ROBUST_v2_2,
				RecencyType.TIME_DEFAULT, sys.getFirst(), sys.getSecond(), cores, seed, true);
		no_fault.simulate(print);

		List<DirectedAcyclicGraph> dags = sys.getFirst();
		if (print)
			System.out.println(dags.get(dags.size() - 1).finishTime - dags.get(dags.size() - 1).startTime);

		return dags.get(dags.size() - 1).finishTime - dags.get(dags.size() - 1).startTime;
	}

}
