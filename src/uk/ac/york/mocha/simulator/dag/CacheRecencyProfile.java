package uk.ac.york.mocha.simulator.dag;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.special.Erf;
import org.apache.commons.math3.util.Pair;

import uk.ac.york.mocha.simulator.dag.Node.NodeType;
import uk.ac.york.mocha.simulator.parameters.SystemParameters;
import uk.ac.york.mocha.simulator.parameters.SystemParameters.RecencyType;

public class CacheRecencyProfile {
	private RecencyType type;
	private final List<List<Integer>> cacheHierarchy;

	/**********************************************************************
	 * A cache-aware recency table that describes the real WCET of a node *
	 **********************************************************************/
	public List<List<Double>> recencyTable;

	private static final DecimalFormat df = new DecimalFormat("#.###");

	private Random rng;
	
	private Node node;

	public CacheRecencyProfile(Node n, int procNum, int seed) {

		this(n, RecencyType.TIME_DEFAULT, procNum, seed);
	}

	public CacheRecencyProfile(Node n, RecencyType type, int procNum, int seed) {

		if (procNum % SystemParameters.Level2CoreNum != 0 || procNum / SystemParameters.Level2CoreNum < 1) {
			System.err.println("Number of cores must be multiple of " + SystemParameters.Level2CoreNum);
			System.exit(-1);
		}

		this.node = n;
		this.type = type;
		rng = new Random(seed);

		this.cacheHierarchy = new ArrayList<>();

		int procID = 0;
		for (int i = 0; i < procNum / 4; i++) {
			List<Integer> procPerLevel2 = new ArrayList<>();
			for (int j = 0; j < SystemParameters.Level2CoreNum; j++) {
				procPerLevel2.add(procID);
				procID++;
			}
			this.cacheHierarchy.add(procPerLevel2);
		}

		if (this.type == RecencyType.ORDER)
			createRecencyTableByOrder(seed);
	}

	private void createRecencyTableByOrder(int seed) {
		recencyTable = new ArrayList<>(); // new
											// double[cacheLevel][recencyDepth1];

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

	public Pair<Long, Integer> computeET(long time, List<List<Node>> history_level1, List<List<Node>> history_level2,
			List<Node> history_level3, Node n, int proc, boolean cacheAware, boolean fault, long additionalTime) {
		/**
		 * Compute recency distance at each cache level
		 */

		long ET = n.getWCET();
		if (!cacheAware)
			return new Pair<Long, Integer>(ET, 4);

		// switch (this.type) {
		// case TIME_DEFAULT:

		/**************************************************************************
		 ************************* level 1 recency distance ************************
		 ***************************************************************************/
		long leve1Time = time != -1 ? time : getTimeofLastIndex(history_level1.get(proc), n, SystemParameters.v2) + additionalTime;

		switch (type) {
		case TIME_DEFAULT:
			if (SystemParameters.v1 <= leve1Time && leve1Time <= SystemParameters.v2) {
				double speedUp = (SystemParameters.delta2 - SystemParameters.delta1) / (double) SystemParameters.v2 * (double) leve1Time
						+ SystemParameters.delta1;

				if (fault && rng.nextInt(100) < SystemParameters.fault_rate && SystemParameters.fault_range > 0) {
					double faultRange = ((double) (rng.nextInt(SystemParameters.fault_range) - SystemParameters.fault_median))
							/ (double) 100;

					speedUp = speedUp + speedUp * faultRange;
				}
				return new Pair<Long, Integer>((long) Math.ceil((double) n.getWCET() * speedUp), 1);
			}
			break;

		case TIME_CURVE:
			if (SystemParameters.v1 <= leve1Time && leve1Time <= SystemParameters.v2) {
				double speedUp = (SystemParameters.delta2 - SystemParameters.delta1) * (Math.pow(leve1Time, 3) - SystemParameters.v1)
						/ Math.pow(SystemParameters.v2, 3) + SystemParameters.delta1;

				if (fault && rng.nextInt(100) < SystemParameters.fault_rate && SystemParameters.fault_range > 0) {
					double faultRange = ((double) (rng.nextInt(SystemParameters.fault_range) - SystemParameters.fault_median))
							/ (double) 100;

					speedUp = speedUp + speedUp * faultRange;
				}
				return new Pair<Long, Integer>((long) Math.ceil((double) n.getWCET() * speedUp), 1);
			}

		case TIME_STEP:
			if (SystemParameters.v1 <= leve1Time && leve1Time <= SystemParameters.v2) {
				double timeNormalised = (double) (4 * (leve1Time - SystemParameters.v1))
						/ (double) (SystemParameters.v2 - SystemParameters.v1) - 2;

				double speedUp = (SystemParameters.delta2 - SystemParameters.delta1) * (Erf.erf(timeNormalised) - -1) / 2
						+ SystemParameters.delta1;

				if (fault && rng.nextInt(100) < SystemParameters.fault_rate && SystemParameters.fault_range > 0) {
					double faultRange = ((double) (rng.nextInt(SystemParameters.fault_range) - SystemParameters.fault_median))
							/ (double) 100;

					speedUp = speedUp + speedUp * faultRange;
				}
				return new Pair<Long, Integer>((long) Math.ceil((double) n.getWCET() * speedUp), 1);
			}
		default:
			break;
		}

		/**************************************************************************
		 ************************* level 2 recency distance ************************
		 ***************************************************************************/
		int clusterID = proc / SystemParameters.Level2CoreNum;
		long level2Time = time != -1 ? time : getTimeofLastIndex(history_level2.get(clusterID), n, SystemParameters.v3) + additionalTime;

		switch (type) {
		case TIME_DEFAULT:
			if (SystemParameters.v1 <= level2Time && level2Time < SystemParameters.v2) {
				double speedUp = SystemParameters.delta2;

				if (fault && rng.nextInt(100) < SystemParameters.fault_rate && SystemParameters.fault_range > 0) {
					double faultRange = ((double) (rng.nextInt(SystemParameters.fault_range) - SystemParameters.fault_median))
							/ (double) 100;

					speedUp = speedUp + speedUp * faultRange;
				}

				return new Pair<Long, Integer>((long) Math.ceil((double) n.getWCET() * speedUp), 2);
			}

			if (SystemParameters.v2 <= level2Time && level2Time <= SystemParameters.v3) {
				double speedUp = (SystemParameters.delta3 - SystemParameters.delta2) / (double) SystemParameters.v3 * (double) level2Time
						+ SystemParameters.delta2;

				if (fault && rng.nextInt(100) < SystemParameters.fault_rate && SystemParameters.fault_range > 0) {
					double faultRange = ((double) (rng.nextInt(SystemParameters.fault_range) - SystemParameters.fault_median))
							/ (double) 100;

					speedUp = speedUp + speedUp * faultRange;
				}

				return new Pair<Long, Integer>((long) Math.ceil((double) n.getWCET() * speedUp), 2);
			}
			break;

		case TIME_CURVE:
			if (SystemParameters.v1 <= level2Time && level2Time < SystemParameters.v2) {
				double speedUp = SystemParameters.delta2;

				if (fault && rng.nextInt(100) < SystemParameters.fault_rate && SystemParameters.fault_range > 0) {
					double faultRange = ((double) (rng.nextInt(SystemParameters.fault_range) - SystemParameters.fault_median))
							/ (double) 100;

					speedUp = speedUp + speedUp * faultRange;
				}

				return new Pair<Long, Integer>((long) Math.ceil((double) n.getWCET() * speedUp), 2);
			}

			if (SystemParameters.v2 <= level2Time && level2Time <= SystemParameters.v3) {
				double speedUp = (SystemParameters.delta3 - SystemParameters.delta2) * (Math.pow(level2Time, 3) - SystemParameters.v2)
						/ Math.pow(SystemParameters.v3, 3) + SystemParameters.delta2;

				if (fault && rng.nextInt(100) < SystemParameters.fault_rate && SystemParameters.fault_range > 0) {
					double faultRange = ((double) (rng.nextInt(SystemParameters.fault_range) - SystemParameters.fault_median))
							/ (double) 100;

					speedUp = speedUp + speedUp * faultRange;
				}

				return new Pair<Long, Integer>((long) Math.ceil((double) n.getWCET() * speedUp), 2);
			}
			break;
		case TIME_STEP:
			if (SystemParameters.v1 <= level2Time && level2Time < SystemParameters.v2) {
				double speedUp = SystemParameters.delta2;

				if (fault && rng.nextInt(100) < SystemParameters.fault_rate && SystemParameters.fault_range > 0) {
					double faultRange = ((double) (rng.nextInt(SystemParameters.fault_range) - SystemParameters.fault_median))
							/ (double) 100;

					speedUp = speedUp + speedUp * faultRange;
				}

				return new Pair<Long, Integer>((long) Math.ceil((double) n.getWCET() * speedUp), 2);
			}

			if (SystemParameters.v2 <= level2Time && level2Time <= SystemParameters.v3) {
				double timeNormalised = (double) (4 * (leve1Time - SystemParameters.v2))
						/ (double) (SystemParameters.v3 - SystemParameters.v2) - 2;

				double speedUp = (SystemParameters.delta3 - SystemParameters.delta2) * (Erf.erf(timeNormalised) - -1) / 2
						+ SystemParameters.delta2;

				if (fault && rng.nextInt(100) < SystemParameters.fault_rate && SystemParameters.fault_range > 0) {
					double faultRange = ((double) (rng.nextInt(SystemParameters.fault_range) - SystemParameters.fault_median))
							/ (double) 100;

					speedUp = speedUp + speedUp * faultRange;
				}

				return new Pair<Long, Integer>((long) Math.ceil((double) n.getWCET() * speedUp), 2);
			}

		default:
			break;
		}

		/**************************************************************************
		 ************************* level 3 recency distance ************************
		 ***************************************************************************/
		long level3Time = time != -1 ? time : getTimeofLastIndex(history_level3, n, SystemParameters.v4) + additionalTime;

		switch (type) {
		case TIME_DEFAULT:

			if (SystemParameters.v1 <= level3Time && level3Time < SystemParameters.v3) {
				double speedUp = SystemParameters.delta3;

				if (fault && rng.nextInt(100) < SystemParameters.fault_rate && SystemParameters.fault_range > 0) {
					double faultRange = ((double) (rng.nextInt(SystemParameters.fault_range) - SystemParameters.fault_median))
							/ (double) 100;

					speedUp = speedUp + speedUp * faultRange;
				}

				return new Pair<Long, Integer>((long) Math.ceil((double) n.getWCET() * speedUp), 3);
			}

			if (SystemParameters.v3 <= level3Time && level3Time <= SystemParameters.v4) {
				double speedUp = (SystemParameters.delta4 - SystemParameters.delta3) / (double) SystemParameters.v4 * (double) level3Time
						+ SystemParameters.delta3;

				if (fault && rng.nextInt(100) < SystemParameters.fault_rate && SystemParameters.fault_range > 0) {
					double faultRange = ((double) (rng.nextInt(SystemParameters.fault_range) - SystemParameters.fault_median))
							/ (double) 100;

					speedUp = speedUp + speedUp * faultRange;
				}

				return new Pair<Long, Integer>((long) Math.ceil((double) n.getWCET() * speedUp), 3);
			}

			break;

		case TIME_CURVE:
			if (SystemParameters.v1 <= level3Time && level3Time < SystemParameters.v3) {
				double speedUp = SystemParameters.delta3;

				if (fault && rng.nextInt(100) < SystemParameters.fault_rate && SystemParameters.fault_range > 0) {
					double faultRange = ((double) (rng.nextInt(SystemParameters.fault_range) - SystemParameters.fault_median))
							/ (double) 100;

					speedUp = speedUp + speedUp * faultRange;
				}

				return new Pair<Long, Integer>((long) Math.ceil((double) n.getWCET() * speedUp), 3);
			}

			if (SystemParameters.v3 <= level3Time && level3Time <= SystemParameters.v4) {
				double speedUp = (SystemParameters.delta4 - SystemParameters.delta3) * (Math.pow(level3Time, 3) - SystemParameters.v3)
						/ Math.pow(SystemParameters.v4, 3) + SystemParameters.delta3;

				if (fault && rng.nextInt(100) < SystemParameters.fault_rate && SystemParameters.fault_range > 0) {
					double faultRange = ((double) (rng.nextInt(SystemParameters.fault_range) - SystemParameters.fault_median))
							/ (double) 100;

					speedUp = speedUp + speedUp * faultRange;
				}

				return new Pair<Long, Integer>((long) Math.ceil((double) n.getWCET() * speedUp), 3);
			}

		case TIME_STEP:
			if (SystemParameters.v1 <= level3Time && level3Time < SystemParameters.v3) {
				double speedUp = SystemParameters.delta3;

				if (fault && rng.nextInt(100) < SystemParameters.fault_rate && SystemParameters.fault_range > 0) {
					double faultRange = ((double) (rng.nextInt(SystemParameters.fault_range) - SystemParameters.fault_median))
							/ (double) 100;

					speedUp = speedUp + speedUp * faultRange;
				}

				return new Pair<Long, Integer>((long) Math.ceil((double) n.getWCET() * speedUp), 3);
			}

			if (SystemParameters.v3 <= level3Time && level3Time <= SystemParameters.v4) {
				double timeNormalised = (double) (4 * (leve1Time - SystemParameters.v3))
						/ (double) (SystemParameters.v4 - SystemParameters.v3) - 2;

				double speedUp = (SystemParameters.delta4 - SystemParameters.delta3) * (Erf.erf(timeNormalised) - -1) / 2
						+ SystemParameters.delta3;

				if (fault && rng.nextInt(100) < SystemParameters.fault_rate && SystemParameters.fault_range > 0) {
					double faultRange = ((double) (rng.nextInt(SystemParameters.fault_range) - SystemParameters.fault_median))
							/ (double) 100;

					speedUp = speedUp + speedUp * faultRange;
				}

				return new Pair<Long, Integer>((long) Math.ceil((double) n.getWCET() * speedUp), 3);
			}

		default:
			break;
		}

		return new Pair<Long, Integer>(ET, 4);
		/*
		 * level 1 recency distance
		 */

		// int level1Index = getNodeLastIndex(history.get(proc), n) == -1 ?
		// Integer.MAX_VALUE
		// : getNodeLastIndex(history.get(proc), n);
		// long leve1Time = level1Index == -1 ? Long.MAX_VALUE : 0;
		// for (int i = level1Index; i < history.get(proc).size(); i++) {
		// leve1Time += history.get(proc).get(i).finishAt -
		// history.get(proc).get(i).start;
		// }

		/*
		 * level 2 recency distance
		 */
		// List<Integer> Level2ProcsT = new ArrayList<>();
		// for (List<Integer> group : cacheHierarchy) {
		// if (group.contains(proc)) {
		// Level2ProcsT.addAll(group);
		// break;
		// }
		// }
		//
		// if (Level2ProcsT.size() == 0) {
		// System.err.println("Simualtor.computeET()" + ": " + "Processor not
		// found!");
		// System.exit(-1);
		// }
		//
		// List<Node> finishedNodesT = new ArrayList<>();
		// for (Integer index : Level2ProcsT) {
		// finishedNodesT.addAll(history_level1.get(index));
		// }
		// /* For shared cache, we rely on node finish time to compute recency
		// order. */
		// finishedNodesT.sort((c1, c2) -> compareNodeForRecency(c1, c2, n));

		// int level2Index = getNodeLastIndex(finishedNodesT, n) == -1 ?
		// Integer.MAX_VALUE
		// : getNodeLastIndex(finishedNodesT, n);
		// long leve2Time = level2Index == -1 ? Long.MAX_VALUE : 0;
		//
		// for (int i = level2Index; i < finishedNodesT.size(); i++) {
		// leve2Time += finishedNodesT.get(i).finishAt -
		// finishedNodesT.get(i).start;
		// }

		/*
		 * level 3 recency distance
		 */
		// List<Node> allhistoryT = history_level1.stream().flatMap(c ->
		// c.stream()).collect(Collectors.toList());

		// int level3Index = getNodeLastIndex(allhistoryT, n) == -1 ?
		// Integer.MAX_VALUE
		// : getNodeLastIndex(allhistoryT, n);
		// long leve3Time = level3Index == -1 ? Long.MAX_VALUE : 0;
		//
		// for (int i = level3Index; i < allhistoryT.size(); i++) {
		// leve3Time += allhistoryT.get(i).finishAt - allhistoryT.get(i).start;
		// }

		// case ORDER:
		// /*
		// * level 1 recency distance
		// */
		// int level1Distance = getNodeLastIndex(history_level1.get(proc), n) ==
		// -1 ? Integer.MAX_VALUE
		// : history_level1.get(proc).size() -
		// getNodeLastIndex(history_level1.get(proc), n);
		//
		// if (level1Distance <= SystemParameters.recencyDepth[0]) {
		// return new Pair<Long, Integer>(
		// (long) Math.ceil((double) n.getWCET() *
		// recencyTable.get(0).get(level1Distance - 1)), 1);
		// }
		//
		// /*
		// * level 2 recency distance
		// */
		// List<Integer> Level2Procs = new ArrayList<>();
		// for (List<Integer> group : cacheHierarchy) {
		// if (group.contains(proc)) {
		// Level2Procs.addAll(group);
		// break;
		// }
		// }
		//
		// if (Level2Procs.size() == 0) {
		// System.err.println("Simualtor.computeET()" + ": " + "Processor not
		// found!");
		// System.exit(-1);
		// }
		//
		// List<Node> finishedNodes = new ArrayList<>();
		// for (Integer index : Level2Procs) {
		// finishedNodes.addAll(history_level1.get(index));
		// }
		// /* For shared cache, we rely on node finish time to compute recency
		// order. */
		// finishedNodes.sort((c1, c2) -> compareNodeForRecency(c1, c2, n));
		//
		// int level2Distance = getNodeLastIndex(finishedNodes, n) == -1 ?
		// Integer.MAX_VALUE
		// : finishedNodes.size() - getNodeLastIndex(finishedNodes, n);
		//
		// if (level2Distance <= SystemParameters.recencyDepth[1]) {
		// return new Pair<Long, Integer>(
		// (long) Math.ceil((double) n.getWCET() *
		// recencyTable.get(1).get(level2Distance - 1)), 2);
		// }
		//
		// /*
		// * level 3 recency distance
		// */
		// List<Node> allhistory = history_level1.stream().flatMap(c ->
		// c.stream()).collect(Collectors.toList());
		//
		// allhistory.sort((c1, c2) -> compareNodeForRecency(c1, c2, n));
		// int level3Distance = getNodeLastIndex(allhistory, n) == -1 ?
		// Integer.MAX_VALUE
		// : allhistory.size() - getNodeLastIndex(allhistory, n);
		//
		// if (level3Distance <= SystemParameters.recencyDepth[2]) {
		// return new Pair<Long, Integer>(
		// (long) Math.ceil((double) n.getWCET() *
		// recencyTable.get(2).get(level3Distance - 1)), 3);
		// }
		//
		// return new Pair<Long, Integer>(ET, 4);
		//
		// default:
		// break;
		// }

		// System.err.println("Recency.computeET(): No ET is computed");
		// System.exit(-1);

		// return null;

	}

	public double createRecencyTableByOrder(long time) {

		if (SystemParameters.v1 < time && time <= SystemParameters.v2) {
			return (SystemParameters.delta2 - SystemParameters.delta1) / SystemParameters.v2 * time + SystemParameters.delta1;
		} else if (SystemParameters.v1 < time && time <= SystemParameters.v3)
			return (SystemParameters.delta3 - SystemParameters.delta2) / SystemParameters.v3 * time + SystemParameters.delta2;
		else if (SystemParameters.v1 < time && time <= SystemParameters.v4)
			return (SystemParameters.delta4 - SystemParameters.delta3) / SystemParameters.v4 * time + SystemParameters.delta3;
		else // start <= v1 || v2 <= start
			return SystemParameters.delta4;
	}

	private long getTimeofLastIndex(List<Node> nodes, Node n, long bound) {

		long time = 0;

		for (int i = nodes.size() - 1; i >= 0; i--) {
			if (nodes.get(i).getDagID() == n.getDagID() && nodes.get(i).getId() == n.getId()) {
				time = time + nodes.get(i).finishAt - nodes.get(i).start;
				return time;
			} else {
				time = time + nodes.get(i).finishAt - nodes.get(i).start;
				if (time > bound)
					return time;
			}
		}

		return -1;
	}

	// private int getNodeLastIndex(List<Node> nodes, Node n) {
	//
	// for (int i = nodes.size() - 1; i >= 0; i--) {
	// if (nodes.get(i).getDagID() == n.getDagID() && nodes.get(i).getId() ==
	// n.getId())
	// return i;
	// }
	//
	// return -1;
	// }
	//
	// private int compareNodeForRecency(Node n1, Node n2, Node current) {
	//
	// int compare = -Long.compare(n1.finishAt, n2.finishAt);
	//
	// if (compare == 0) {
	// if (n1.getFullName().equals(current.getFullName()))
	// compare = -1;
	// if (n2.getFullName().equals(current.getFullName()))
	// compare = 1;
	// }
	//
	// return compare;
	// }

	public RecencyType getType() {
		return type;
	}

	public static void main(String args[]) {

		RecencyProfile rp = new RecencyProfile(RecencyType.TIME_DEFAULT, 8, 1000);

		Node n = new Node(1000000, -1, NodeType.NORMAL, -1, -1);

		for (long i = SystemParameters.v1; i < SystemParameters.v4; i+=100) {

			long a = rp.computeET(i, null, null, null, n, 8, true, false, 0).getFirst();
			System.out.println(a);
		}

	}
}
