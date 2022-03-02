package uk.ac.york.mocha.simulator.experiments_AJLR_v2_0;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.Pair;

import uk.ac.york.mocha.simulator.dag.DirectedAcyclicGraph;
import uk.ac.york.mocha.simulator.dag.Node;
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

public class TestFaultsImpact {

	static DecimalFormat df = new DecimalFormat("#.###");

	public static enum faultType {
		all_nodes, all_critical, all_non_critical, high_et, high_pathNum, high_pathET, sensivitiy,
		high_in_degree, high_out_degree, high_in_out_degree
	}

	static int nos = 1000;
	static int[] allCores = { 4, 8 };
	static boolean print = true;
	static double[] allPercent = { 0.1, 0.2, 0.3, 0.4, 0.5 };
	static double[] allEffect = { 0.1, 0.2, 0.3, 0.4, 0.5 };
	static int[] allInstanceNum = { 1, 3, 5, 10 };

	public static void main(String args[]) {
		List<Thread> t = new ArrayList<>();

		t.add(new Thread(new Runnable() {

			@Override
			public void run() {
				faults_cores();

			}
		}));
		t.add(new Thread(new Runnable() {

			@Override
			public void run() {
				faults_percent();

			}
		}));
//		t.add(new Thread(new Runnable() {
//
//			@Override
//			public void run() {
//				faults_effect();
//
//			}
//		}));
//		t.add(new Thread(new Runnable() {
//
//			@Override
//			public void run() {
//				faults_instanceNum();
//			}
//		}));

		for (Thread r : t)
			r.run();

		for (Thread r : t)
			try {
				r.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	}

	public static void faults_cores() {
		for (int i = 0; i < allCores.length; i++) {
			faults(allCores[i], allPercent[allPercent.length - 1], allEffect[allEffect.length - 1], allInstanceNum[0]);
		}
	}

	public static void faults_percent() {
		for (int i = 0; i < allPercent.length; i++) {
			faults(allCores[0], allPercent[i], allEffect[allEffect.length - 1], allInstanceNum[0]);
		}
	}

	public static void faults_effect() {
		for (int i = 0; i < allEffect.length; i++) {
			faults(allCores[0], allPercent[allPercent.length - 1], allEffect[i], allInstanceNum[0]);
		}
	}

	public static void faults_instanceNum() {
		for (int i = 0; i < allInstanceNum.length; i++) {
			faults(allCores[0], allPercent[allPercent.length - 1], allEffect[allEffect.length - 1], allInstanceNum[i]);
		}
	}

	public static void faults(int cores, double percent, double effect, int instanceNum) {
		int seed = 1000;

		List<List<Long>> res = new ArrayList<>();
		for (int i = 0; i < nos; i++) {
			System.out.println("No. of system: " + i);

			SystemGenerator gen = new SystemGenerator(SystemParameters.coreNum, 1, true, true, null, seed, true, print);
			Pair<List<DirectedAcyclicGraph>, CacheHierarchy> sys = gen.generatedDAGInstancesInOneHP(instanceNum, -1, null, false);

//			System.out.println("No. node: " + dags.get(0).getFlatNodes().size());

			// for (int i = 0; i < dags.get(0).allpaths.size(); i++) {
			// for (Node n : dags.get(0).allpaths.get(i)) {
			// System.out.print(n.getShortName() + " ");
			// }
			// System.out.println();
			// }

			List<DirectedAcyclicGraph> dags = sys.getFirst();
			
			if (print) {
				// System.out.println(dags.get(0).toString());

				List<Node> longestPath = dags.get(0).longestPath;
				String out = "longest path: ";
				for (int k = 0; k < longestPath.size(); k++) {
					out += longestPath.get(k).getShortName();

					if (k != longestPath.size() - 1)
						out += "  ->  ";
				}
				System.out.println(out);
			}

			List<Long> results = new ArrayList<>();

			results.addAll(run(sys, cores, seed, faultType.all_nodes, percent, effect, print));
			// if(print)
			// System.out.println();
			results.addAll(run(sys, cores, seed, faultType.high_pathET, percent, effect, print));
			// if(print)
			// System.out.println();
			results.addAll(run(sys, cores, seed, faultType.high_et, percent, effect, print));
			// if(print)
			// System.out.println();
			results.addAll(run(sys, cores, seed, faultType.high_pathNum, percent, effect, print));
			// if(print)
			// System.out.println();
			results.addAll(run(sys, cores, seed, faultType.sensivitiy, percent, effect, print));
			// if(print)
			// System.out.println();

			res.add(results);
			seed++;
		}

		String out = "";
		for (List<Long> ll : res) {
			for (int k = 0; k < ll.size(); k++) {
				System.out.print(ll.get(k) + " ");
				out += ll.get(k);
				if (k != ll.size() - 1)
					out += " ";
			}
			System.out.println();
			out += "\n";
		}

		String folderAndFile = "result/" + "faults" + "/out" + "_" + cores + "_" + percent + "_" + effect + "_"
				+ instanceNum + ".txt";
		Utils.writeResult(folderAndFile, out);
	}

	public static List<Long> run(Pair<List<DirectedAcyclicGraph>,CacheHierarchy> sys, int cores, int seed, faultType type, double percent,
			double effect, boolean print) {

		List<Long> makespans = new ArrayList<>();

		if (print)
			System.out.println("\n$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ " + type.toString()
					+ "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		setUpSpecificFaults(sys.getFirst(), type, percent, effect, true, print);
		long makespan_opposite = oneRun(sys, cores, seed, print);
		makespans.add(makespan_opposite);
		if (print)
			System.out.println(
					"$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");

		if (print)
			System.out.println("\n$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ " + type.toString()
					+ "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		setUpSpecificFaults(sys.getFirst(), type, percent, effect, false, print);
		long makespan = oneRun(sys, cores, seed, print);
		makespans.add(makespan);
		if (print)
			System.out.println(
					"$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$\n");

		return makespans;
	}

	public static List<List<Node>> setUpSpecificFaults(List<DirectedAcyclicGraph> dags, faultType type, double percent,
			double faultEfect, boolean opposite, boolean print) {

		List<List<Node>> faultNodesinDAGs = new ArrayList<>();

		for (DirectedAcyclicGraph d : dags)
			for (Node n : d.getFlatNodes())
				n.hasFaults = false;

		for (DirectedAcyclicGraph d : dags) {

			// all nodes with no order
			List<Node> allNodes = new ArrayList<Node>(d.getFlatNodes());

			// critical nodes
			List<Node> critical = new ArrayList<Node>();

			// non-critical nodes
			List<Node> non_critical = new ArrayList<>();
			for (Node n : allNodes) {
				if (d.longestPath.contains(n))
					critical.add(n);
				else
					non_critical.add(n);
			}

			List<Node> faultNodes = new ArrayList<>();
			int faultNodeNum = 0;

			switch (type) {
			case all_nodes:
				faultNodes = new ArrayList<>(allNodes);
				break;
//			case all_critical:
//				faultNodes = new ArrayList<>(critical);
//				break;
//			case all_non_critical:
//				faultNodes = new ArrayList<>(non_critical);
//				break;

			case high_et:
				allNodes.sort((c1, c2) -> compareNodebyET(c1, c2));
				faultNodeNum = (int) Math.ceil(percent * (double) allNodes.size());

				for (int i = 0; i < faultNodeNum; i++) {
					faultNodes.add(allNodes.get(i));
				}
				break;

			case high_pathNum:
				allNodes.sort((c1, c2) -> compareNodebyPath(dags, c1, c2));
				faultNodeNum = (int) Math.ceil(percent * (double) allNodes.size());

				for (int i = 0; i < faultNodeNum; i++) {
					faultNodes.add(allNodes.get(i));
				}
				break;

			case high_pathET:
				allNodes.sort((c1, c2) -> compareNodebyPathET(dags, c1, c2));
				faultNodeNum = (int) Math.ceil(percent * (double) allNodes.size());

				for (int i = 0; i < faultNodeNum; i++) {
					faultNodes.add(allNodes.get(i));
				}
				break;

//			case high_out_degree:
//				allNodes.sort((c1, c2) -> compareNodebyOutDegree(c1, c2));
//
//				faultNodeNum = (int) Math.ceil(percent * (double) allNodes.size());
//
//				for (int i = 0; i < faultNodeNum; i++) {
//					faultNodes.add(allNodes.get(i));
//				}
//				break;
//
//			case high_in_degree:
//				allNodes.sort((c1, c2) -> compareNodebyInDegree(c1, c2));
//
//				faultNodeNum = (int) Math.ceil(percent * (double) allNodes.size());
//
//				for (int i = 0; i < faultNodeNum; i++) {
//					faultNodes.add(allNodes.get(i));
//				}
//				break;
//
//			case high_in_out_degree:
//				allNodes.sort((c1, c2) -> compareNodebyInAndOutDegree(c1, c2));
//
//				faultNodeNum = (int) Math.ceil(percent * (double) allNodes.size());
//
//				for (int i = 0; i < faultNodeNum; i++) {
//					faultNodes.add(allNodes.get(i));
//				}
//				break;

			case sensivitiy:
				allNodes.sort((c1, c2) -> compareNodebyPathET(dags, c1, c2));
				faultNodeNum = (int) Math.ceil(percent / 3.0 * (double) allNodes.size());

				for (int i = 0; i < faultNodeNum; i++) {
					if (!faultNodes.contains(allNodes.get(i)))
						faultNodes.add(allNodes.get(i));
				}
				

				allNodes.sort((c1, c2) -> compareNodebyPath(dags, c1, c2));
				faultNodeNum = (int) Math.ceil(percent / 3.0 * (double) allNodes.size());

				for (int i = 0; i < faultNodeNum; i++) {
					if (!faultNodes.contains(allNodes.get(i)))
						faultNodes.add(allNodes.get(i));
				}

				allNodes.sort((c1, c2) -> compareNodebyET(c1, c2));
				faultNodeNum = (int) Math.ceil(percent / 3.0 * (double) allNodes.size());

				for (int i = 0; i < faultNodeNum; i++) {
					if (!faultNodes.contains(allNodes.get(i)))
						faultNodes.add(allNodes.get(i));
				}
				break;

			default:
				break;
			}

			if (print)
				System.out.println("Fault nodes: ");

			if (!opposite) {
				for (Node n : faultNodes) {
					n.hasFaults = true;
					n.cvp.median = 0;
					n.cvp.range = faultEfect; // ((double) rng.nextInt(effect +
												// 1) / (double) 100) / 3.0;
					if (print)
						System.out.println(n.toString() + ": " + n.cvp.median + ", " + n.cvp.range);
				}

				faultNodesinDAGs.add(faultNodes);
			} else {
				allNodes.removeAll(faultNodes);

				for (Node n : allNodes) {
					n.hasFaults = true;
					n.cvp.median = 0;
					n.cvp.range = faultEfect; // ((double) rng.nextInt(effect +
												// 1) / (double) 100) / 3.0;
					if (print)
						System.out.println(n.toString() + ": " + n.cvp.median + ", " + n.cvp.range);
				}

				faultNodesinDAGs.add(allNodes);
			}
//			System.out.println("Fault nodes: " +faultNodes.size());
		}

		return faultNodesinDAGs;
	}

	public static long oneRun(Pair<List<DirectedAcyclicGraph>, CacheHierarchy> sys, int cores, int seed, boolean print) {
		SimualtorNWC no_fault = new SimualtorNWC(SimuType.CLOCK_LEVEL, Hardware.PROC_CACHE,
				Allocation.CACHE_AWARE_ROBUST_v2_1, RecencyType.TIME_DEFAULT, sys.getFirst(),sys.getSecond(), cores, seed, true);
		no_fault.simulate(print);

		List<DirectedAcyclicGraph> dags = sys.getFirst();
		if (print)
			System.out.println(dags.get(dags.size() - 1).finishTime - dags.get(dags.size() - 1).startTime);

		return dags.get(dags.size() - 1).finishTime - dags.get(dags.size() - 1).startTime;
	}

	public static int compareNodebyPathET(List<DirectedAcyclicGraph> dags, Node c1, Node c2) {

		return -Long.compare(c1.pathET, c2.pathET);

		// DirectedAcyclicGraph dag1 = Utils.getDagByIndex(dags, c1.getDagID(),
		// c1.getDagInstNo());
		//
		// int count1 = 0;
		// int count2 = 0;
		//
		// for (int i = 0; i < dag1.allpaths.size(); i++) {
		// if (dag1.allpaths.get(i).contains(c1)) {
		// count1++;
		// }
		// if (dag1.allpaths.get(i).contains(c2)) {
		// count2++;
		// }
		// }
		//
		// return -Integer.compare(count1, count2);
	}

	
	public static int compareNodebyPath(List<DirectedAcyclicGraph> dags, Node c1, Node c2) {

		return -Long.compare(c1.pathNum, c2.pathNum);

		// DirectedAcyclicGraph dag1 = Utils.getDagByIndex(dags, c1.getDagID(),
		// c1.getDagInstNo());
		//
		// int count1 = 0;
		// int count2 = 0;
		//
		// for (int i = 0; i < dag1.allpaths.size(); i++) {
		// if (dag1.allpaths.get(i).contains(c1)) {
		// count1++;
		// }
		// if (dag1.allpaths.get(i).contains(c2)) {
		// count2++;
		// }
		// }
		//
		// return -Integer.compare(count1, count2);
	}

	public static int compareNodebyET(Node c1, Node c2) {
		return -Long.compare(c1.getWCET(), c2.getWCET());
	}

	public static int compareNodebyOutDegree(Node c1, Node c2) {
		return -Integer.compare(c1.getChildren().size(), c2.getChildren().size());
	}

	public static int compareNodebyInDegree(Node c1, Node c2) {
		return -Integer.compare(c1.getParent().size(), c2.getParent().size());
	}

	public static int compareNodebyInAndOutDegree(Node c1, Node c2) {
		return -Integer.compare(c1.getParent().size() + c1.getChildren().size(),
				c2.getParent().size() + c2.getChildren().size());
	}

}
