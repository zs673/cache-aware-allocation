package uk.ac.york.mocha.simulator.dag;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Recency {

	public static final double[] costFactorMIN = { 0.3, 0.4, 0.5 };
	public static final double[] costFactorMAX = { 0.5, 0.6, 0.7 };

	public static final int cacheLevel = 3;
	public static final int[] recencyDepth = { 6, 20, 50 };

	public static final int Level2procNum = 4;

	public final List<List<Integer>> cacheHierarchy;

	/**********************************************************************
	 * A cache-aware recency table that describes the real WCET of a node *
	 **********************************************************************/
	public List<List<Double>> recencyTable;

	private static final DecimalFormat df = new DecimalFormat("#.##");

	private Random rng;

	public Recency(int procNum, int seed) {
		rng = new Random(seed);

		this.cacheHierarchy = new ArrayList<>();

		int procID = 0;
		for (int i = 0; i < procNum / 4; i++) {
			List<Integer> procPerLevel2 = new ArrayList<>();
			for (int j = 0; j < Level2procNum; j++) {
				procPerLevel2.add(procID);
				procID++;
			}
			this.cacheHierarchy.add(procPerLevel2);
		}

		recencyTable = new ArrayList<>(); // new double[cacheLevel][recencyDepth1];

		for (int i = 0; i < cacheLevel; i++) {
			List<Double> oneLevel = new ArrayList<>();
			for (int j = 0; j < recencyDepth[i]; j++) {

				if (j == 0) {
					double factor = rng.nextDouble() * (costFactorMAX[i] - costFactorMIN[i]) + costFactorMIN[i];
					double formatFactor = Double.parseDouble(df.format(factor));
					oneLevel.add(formatFactor);
				} else {
					double factor = -1;
					switch (i) {
					case 0:
						factor = oneLevel.get(j - 1) + 0.1;
						break;
					case 1:
						factor = oneLevel.get(j - 1) + 0.05;
						break;
					case 2:
						factor = oneLevel.get(j - 1) + 0.01;
						break;

					default:
						break;
					}

					factor = factor > 1.0 ? 1.0 : factor;
					double formatFactor = Double.parseDouble(df.format(factor));
					oneLevel.add(formatFactor);
				}
			}
			recencyTable.add(oneLevel);
		}

		recencyTable.get(cacheLevel - 1).set(recencyDepth[2] - 1, 1.0);
	}

	public long computeET(List<List<Node>> history, Node n, int proc, boolean cacheAware) {
		/**
		 * Compute recency distance at each cache level
		 */

		long ET = n.getWCET();
		if (!cacheAware)
			return ET;

		/*
		 * level 1 recency distance
		 */
		int level1Distance = getNodeLastIndex(history.get(proc), n) == -1 ? Integer.MAX_VALUE
				: history.get(proc).size() - getNodeLastIndex(history.get(proc), n);

		if (level1Distance <= Recency.recencyDepth[0]) {
			return (long) Math.ceil((double) n.getWCET() * recencyTable.get(0).get(level1Distance - 1));
		}

		/*
		 * level 2 recency distance
		 */
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
		/* For shared cache, we rely on node finish time to compute recency order. */
		finishedNodes.sort((c1, c2) -> compareNodeForRecency(c1, c2, n));

		int level2Distance = getNodeLastIndex(finishedNodes, n) == -1 ? Integer.MAX_VALUE
				: finishedNodes.size() - getNodeLastIndex(finishedNodes, n);

		if (level2Distance <= Recency.recencyDepth[1]) {
			return (long) Math.ceil((double) n.getWCET() * recencyTable.get(1).get(level2Distance - 1));
		}

		/*
		 * level 3 recency distance
		 */
		List<Node> allhistory = history.stream().flatMap(c -> c.stream()).collect(Collectors.toList());

		allhistory.sort((c1, c2) -> compareNodeForRecency(c1, c2, n));
		int level3Distance = getNodeLastIndex(allhistory, n) == -1 ? Integer.MAX_VALUE
				: allhistory.size() - getNodeLastIndex(allhistory, n);

		if (level3Distance <= Recency.recencyDepth[2]) {
			return (long) Math.ceil((double) n.getWCET() * recencyTable.get(2).get(level3Distance - 1));
		}

		return ET;
	}

	private int getNodeLastIndex(List<Node> nodes, Node n) {

		for (int i = nodes.size() - 1; i >= 0; i--) {
			if (nodes.get(i).getDagID() == n.getDagID() && nodes.get(i).getId() == n.getId())
				return i;
		}

		return -1;
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

		Recency table = new Recency(8, 1000);

		for (int i = 0; i < table.recencyTable.size(); i++) {
			for (int j = 0; j < table.recencyTable.get(i).size(); j++) {
				System.out.print(table.recencyTable.get(i).get(j) + " | ");
			}
			System.out.println();
		}

//		for (List<Double> d : table.recencyTable)
//			System.out.println(Arrays.toString(d.toArray()));

	}
}
