package uk.ac.york.mocha.simulator.errorResults;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import uk.ac.york.mocha.simulator.dag.DirectedAcyclicGraph;
import uk.ac.york.mocha.simulator.dag.Node;
import uk.ac.york.mocha.simulator.generator.SystemGenerator;
import uk.ac.york.mocha.simulator.parameters.SystemParameters;
import uk.ac.york.mocha.simulator.parameters.SystemParameters.Allocation;
import uk.ac.york.mocha.simulator.parameters.SystemParameters.Hardware;
import uk.ac.york.mocha.simulator.parameters.SystemParameters.RecencyType;
import uk.ac.york.mocha.simulator.parameters.SystemParameters.SimuType;
import uk.ac.york.mocha.simulator.simulator.SimualtorNWC;

/* Number, Type, Effect */

/*
 * Show the climb effects, can we model that as pressure, i.e. if the pressure
 * is higher than the threshold then it will impact the makespan?
 */

public class TestFaultsImpact {

	static DecimalFormat df = new DecimalFormat("#.###");

	public static enum faultType {
		all_nodes, all_critical, all_non_critical, high_et, high_in_degree, high_out_degree, high_in_out_degree,
		critical_high_et_in_out_degree
	}

	public static void main(String args[]) {
		faults();
	}

	public static void faults() {

		int cores = 8;
		int seed = 1000;
		boolean print = true;

		SystemGenerator gen = new SystemGenerator(SystemParameters.coreNum, 1, true, true, null, seed, true,
				SystemParameters.printGen);
		List<DirectedAcyclicGraph> dags = gen.generatedDAGInstancesInOneHP(1, -1, null, false);

		if (print) {

			List<Node> longestPath = dags.get(0).longestPath;
			String out = "longest path: ";
			for (int i = 0; i < longestPath.size(); i++) {
				out += longestPath.get(i).getShortName();

				if (i != longestPath.size() - 1)
					out += "  ->  ";
			}
			System.out.println(out);
		}

		run(dags, cores, seed, faultType.high_in_out_degree, print);

	}

	public static void run(List<DirectedAcyclicGraph> dags, int cores, int seed, faultType type, boolean print) {
		System.out.println(
				"\n$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		setUpSpecificFaults(dags, type, 0.2, 0.5, true, true);
		oneRun(dags, cores, seed, print);
		System.out.println(
				"$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$\n\n");

		System.out.println(
				"\n$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		setUpSpecificFaults(dags, type, 0.2, 0.5, false, true);
		oneRun(dags, cores, seed, print);
		System.out.println(
				"$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$\n");
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
			case all_critical:
				faultNodes = new ArrayList<>(critical);
				break;
			case all_non_critical:
				faultNodes = new ArrayList<>(non_critical);
				break;

			case high_et:
				allNodes.sort((c1, c2) -> compareNodebyET(c1, c2));
				faultNodeNum = (int) Math.ceil(percent * (double) allNodes.size());

				for (int i = 0; i < faultNodeNum; i++) {
					faultNodes.add(allNodes.get(i));
				}
				break;

			case high_out_degree:
				allNodes.sort((c1, c2) -> compareNodebyOutDegree(c1, c2));

				faultNodeNum = (int) Math.ceil(percent * (double) allNodes.size());

				for (int i = 0; i < faultNodeNum; i++) {
					faultNodes.add(allNodes.get(i));
				}
				break;

			case high_in_degree:
				allNodes.sort((c1, c2) -> compareNodebyInDegree(c1, c2));

				faultNodeNum = (int) Math.ceil(percent * (double) allNodes.size());

				for (int i = 0; i < faultNodeNum; i++) {
					faultNodes.add(allNodes.get(i));
				}
				break;

			case high_in_out_degree:
				allNodes.sort((c1, c2) -> compareNodebyInAndOutDegree(c1, c2));

				faultNodeNum = (int) Math.ceil(percent * (double) allNodes.size());

				for (int i = 0; i < faultNodeNum; i++) {
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
					n.cvp.range = faultEfect; // ((double) rng.nextInt(effect + 1) / (double) 100) / 3.0;
					if (print)
						System.out.println(n.toString() + ": " + n.cvp.median + ", " + n.cvp.range);
				}

				faultNodesinDAGs.add(faultNodes);
			} else {
				allNodes.removeAll(faultNodes);

				for (Node n : allNodes) {
					n.hasFaults = true;
					n.cvp.median = 0;
					n.cvp.range = faultEfect; // ((double) rng.nextInt(effect + 1) / (double) 100) / 3.0;
					if (print)
						System.out.println(n.toString() + ": " + n.cvp.median + ", " + n.cvp.range);
				}

				faultNodesinDAGs.add(allNodes);
			}

		}

		return faultNodesinDAGs;
	}

	public static long oneRun(List<DirectedAcyclicGraph> dags, int cores, int seed, boolean print) {
		SimualtorNWC no_fault = new SimualtorNWC(SimuType.CLOCK_LEVEL, Hardware.PROC_CACHE,
				Allocation.CACHE_AWARE_ROBUST, RecencyType.TIME_DEFAULT, dags, cores, seed, true);
		no_fault.simulate(print);
		System.out.println(dags.get(0).finishTime);

		return dags.get(0).finishTime;
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
