package uk.ac.york.mocha.simulator.dag;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import uk.ac.york.mocha.simulator.parameters.SystemParameters;
import uk.ac.york.mocha.simulator.parameters.SystemParameters.RecencyType;

public class Recency {

	private RecencyType type;
	public final List<List<Integer>> cacheHierarchy;

	/**********************************************************************
	 * A cache-aware recency table that describes the real WCET of a node *
	 **********************************************************************/
	public List<List<Double>> recencyTable;

	private static final DecimalFormat df = new DecimalFormat("#.##");

	private Random rng;

	public Recency(RecencyType type, int procNum, int seed) {

		if (procNum % SystemParameters.Level2procNum != 0 || procNum / SystemParameters.Level2procNum < 1) {
			System.err.println("Number of cores must be multiple of " + SystemParameters.Level2procNum);
			System.exit(-1);
		}

		this.type = type;
		rng = new Random(seed);

		this.cacheHierarchy = new ArrayList<>();

		int procID = 0;
		for (int i = 0; i < procNum / 4; i++) {
			List<Integer> procPerLevel2 = new ArrayList<>();
			for (int j = 0; j < SystemParameters.Level2procNum; j++) {
				procPerLevel2.add(procID);
				procID++;
			}
			this.cacheHierarchy.add(procPerLevel2);
		}

		if (this.type == RecencyType.ORDER)
			createRecencyTableByOrder(seed);

	}

	private void createRecencyTableByOrder(int seed) {
		recencyTable = new ArrayList<>(); // new double[cacheLevel][recencyDepth1];

		for (int i = 0; i < SystemParameters.cacheLevel; i++) {
			List<Double> oneLevel = new ArrayList<>();
			for (int j = 0; j < SystemParameters.recencyDepth[i]; j++) {

				if (i == 0 && j == 0) {
					double factor = seed == -1 ? 0.3
							: ((double) (rng.nextInt(SystemParameters.costFactorMAX[i] - SystemParameters.costFactorMIN[i])
									+ SystemParameters.costFactorMIN[i])) / (double) 100;
					double formatFactor = Double.parseDouble(df.format(factor));
					oneLevel.add(formatFactor);
				} else {
					double factor = -1;
					switch (i) {
					case 0:
						factor = oneLevel.get(j - 1) + 0.01;
						break;
					case 1:
						if (j == 0)
							factor = recencyTable.get(i - 1).get(recencyTable.get(i - 1).size() - 1) + 0.05;
						else
							factor = oneLevel.get(j - 1) + 0.01;
						break;
					case 2:
						if (j == 0)
							factor = recencyTable.get(i - 1).get(recencyTable.get(i - 1).size() - 1) + 0.1;
						else
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

		recencyTable.get(SystemParameters.cacheLevel - 1).set(SystemParameters.recencyDepth[2] - 1, 1.0);
	}

	public long computeET(List<List<Node>> history_level1, List<List<Node>> history_level2, List<Node> history_level3,
			Node n, int proc, boolean cacheAware) {
		/**
		 * Compute recency distance at each cache level
		 */

		long ET = n.getWCET();
		if (!cacheAware)
			return ET;

		switch (this.type) {
		case TIME:

			/*
			 * level 1 recency distance
			 */
			long leve1Time = getTimeofLastIndex(history_level1.get(proc), n, SystemParameters.v2);

			if (SystemParameters.v1 <= leve1Time && leve1Time <= SystemParameters.v2) {
				double speedUp = (SystemParameters.delta2 - SystemParameters.delta1) / (double) SystemParameters.v2 * (double) leve1Time
						+ SystemParameters.delta1;
				return (long) Math.ceil((double) n.getWCET() * speedUp);
			}

			/*
			 * level 2 recency distance
			 */
			int clusterID = proc / SystemParameters.Level2procNum;
			long level2Time = getTimeofLastIndex(history_level2.get(clusterID), n, SystemParameters.v3);

			if (SystemParameters.v2 < level2Time && level2Time <= SystemParameters.v3) {
				double speedUp = (SystemParameters.delta3 - SystemParameters.delta2) / (double) SystemParameters.v3 * (double) level2Time
						+ SystemParameters.delta2;
				return (long) Math.ceil((double) n.getWCET() * speedUp);
			}

			/*
			 * level 3 recency distance
			 */
			long level3Time = getTimeofLastIndex(history_level3, n, SystemParameters.v4);

			if (SystemParameters.v3 < level3Time && level3Time <= SystemParameters.v4) {
				double speedUp = (SystemParameters.delta4 - SystemParameters.delta3) / (double) SystemParameters.v4 * (double) level3Time
						+ SystemParameters.delta3;
				return (long) Math.ceil((double) n.getWCET() * speedUp);
			}

			return ET;
		/*
		 * level 1 recency distance
		 */

//			int level1Index = getNodeLastIndex(history.get(proc), n) == -1 ? Integer.MAX_VALUE
//					: getNodeLastIndex(history.get(proc), n);
//			long leve1Time = level1Index == -1 ? Long.MAX_VALUE : 0;		
//			for (int i = level1Index; i < history.get(proc).size(); i++) {
//			leve1Time += history.get(proc).get(i).finishAt - history.get(proc).get(i).start;
//		}

		/*
		 * level 2 recency distance
		 */
//			List<Integer> Level2ProcsT = new ArrayList<>();
//			for (List<Integer> group : cacheHierarchy) {
//				if (group.contains(proc)) {
//					Level2ProcsT.addAll(group);
//					break;
//				}
//			}
//
//			if (Level2ProcsT.size() == 0) {
//				System.err.println("Simualtor.computeET()" + ": " + "Processor not found!");
//				System.exit(-1);
//			}
//
//			List<Node> finishedNodesT = new ArrayList<>();
//			for (Integer index : Level2ProcsT) {
//				finishedNodesT.addAll(history_level1.get(index));
//			}
//			/* For shared cache, we rely on node finish time to compute recency order. */
//			finishedNodesT.sort((c1, c2) -> compareNodeForRecency(c1, c2, n));

//			int level2Index = getNodeLastIndex(finishedNodesT, n) == -1 ? Integer.MAX_VALUE
//					: getNodeLastIndex(finishedNodesT, n);
//			long leve2Time = level2Index == -1 ? Long.MAX_VALUE : 0;
//
//			for (int i = level2Index; i < finishedNodesT.size(); i++) {
//				leve2Time += finishedNodesT.get(i).finishAt - finishedNodesT.get(i).start;
//			}

		/*
		 * level 3 recency distance
		 */
//			List<Node> allhistoryT = history_level1.stream().flatMap(c -> c.stream()).collect(Collectors.toList());

//			int level3Index = getNodeLastIndex(allhistoryT, n) == -1 ? Integer.MAX_VALUE
//					: getNodeLastIndex(allhistoryT, n);
//			long leve3Time = level3Index == -1 ? Long.MAX_VALUE : 0;
//
//			for (int i = level3Index; i < allhistoryT.size(); i++) {
//				leve3Time += allhistoryT.get(i).finishAt - allhistoryT.get(i).start;
//			}

		case ORDER:
			/*
			 * level 1 recency distance
			 */
			int level1Distance = getNodeLastIndex(history_level1.get(proc), n) == -1 ? Integer.MAX_VALUE
					: history_level1.get(proc).size() - getNodeLastIndex(history_level1.get(proc), n);

			if (level1Distance <= SystemParameters.recencyDepth[0]) {
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
				finishedNodes.addAll(history_level1.get(index));
			}
			/* For shared cache, we rely on node finish time to compute recency order. */
			finishedNodes.sort((c1, c2) -> compareNodeForRecency(c1, c2, n));

			int level2Distance = getNodeLastIndex(finishedNodes, n) == -1 ? Integer.MAX_VALUE
					: finishedNodes.size() - getNodeLastIndex(finishedNodes, n);

			if (level2Distance <= SystemParameters.recencyDepth[1]) {
				return (long) Math.ceil((double) n.getWCET() * recencyTable.get(1).get(level2Distance - 1));
			}

			/*
			 * level 3 recency distance
			 */
			List<Node> allhistory = history_level1.stream().flatMap(c -> c.stream()).collect(Collectors.toList());

			allhistory.sort((c1, c2) -> compareNodeForRecency(c1, c2, n));
			int level3Distance = getNodeLastIndex(allhistory, n) == -1 ? Integer.MAX_VALUE
					: allhistory.size() - getNodeLastIndex(allhistory, n);

			if (level3Distance <= SystemParameters.recencyDepth[2]) {
				return (long) Math.ceil((double) n.getWCET() * recencyTable.get(2).get(level3Distance - 1));
			}

			return ET;

		default:
			break;
		}

		return -1;

	}

	public double createRecencyTableByOrder(long time) {

		if (SystemParameters.v1 < time && time <= SystemParameters.v2) {
			return (SystemParameters.delta2 - SystemParameters.delta1) / SystemParameters.v2 * time + SystemParameters.delta1;
		} else if (SystemParameters.v2 < time && time <= SystemParameters.v3)
			return (SystemParameters.delta3 - SystemParameters.delta2) / SystemParameters.v3 * time + SystemParameters.delta2;
		else if (SystemParameters.v3 < time && time <= SystemParameters.v4)
			return (SystemParameters.delta4 - SystemParameters.delta3) / SystemParameters.v4 * time + SystemParameters.delta3;
		else // start <= v1 || v2 <= start
			return SystemParameters.delta4;
	}

	private long getTimeofLastIndex(List<Node> nodes, Node n, long bound) {

		long time = 0;

		for (int i = nodes.size() - 1; i >= 0; i--) {
			if (nodes.get(i).getDagID() == n.getDagID() && nodes.get(i).getId() == n.getId())
				return time;
			else {
				time = time + nodes.get(i).finishAt - nodes.get(i).start;
				if (time > bound)
					return time;
			}
		}

		return -1;
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
			if (n1.getFullName().equals(current.getFullName()))
				compare = -1;
			if (n2.getFullName().equals(current.getFullName()))
				compare = 1;
		}

		return compare;
	}

	public RecencyType getType() {
		return type;
	}

	public static void main(String args[]) {

//		Recency table = new Recency(RecencyType.ORDER, 8, 1000);
//
//		for (int j = 0; j < table.recencyTable.get(0).size(); j++) {
//			for (int i = 0; i < table.recencyTable.size(); i++) {
//				System.out.print(table.recencyTable.get(i).get(j) + " | ");
//			}
//			System.out.println();
//		}

		long time = 0;

		for (; time <= SystemParameters.v4; time += 500) {
			double speedUp = -1;

			if (SystemParameters.v1 <= time && time <= SystemParameters.v2) {
				speedUp = (SystemParameters.delta2 - SystemParameters.delta1) / (double) SystemParameters.v2 * (double) time
						+ SystemParameters.delta1;
			}

			if (SystemParameters.v2 < time && time <= SystemParameters.v3) {
				speedUp = (SystemParameters.delta3 - SystemParameters.delta2) / (double) SystemParameters.v3 * (double) time
						+ SystemParameters.delta2;
			}

			if (SystemParameters.v3 < time && time <= SystemParameters.v4) {
				speedUp = (SystemParameters.delta4 - SystemParameters.delta3) / (double) SystemParameters.v4 * (double) time
						+ SystemParameters.delta3;
			}

			System.out.println(speedUp);
		}

	}
}
