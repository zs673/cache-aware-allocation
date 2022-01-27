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

public class RecencyProfile {

	private RecencyType type;
	public final List<List<Integer>> cacheHierarchy;

	/**********************************************************************
	 * A cache-aware recency table that describes the real WCET of a node *
	 **********************************************************************/
	public List<List<Double>> recencyTable;

	private static final DecimalFormat df = new DecimalFormat("#.###");

	private Random rng;

	public RecencyProfile(int procNum, int seed) {

		this(RecencyType.TIME_DEFAULT, procNum, seed);
	}

	public RecencyProfile(RecencyType type, int procNum, int seed) {

		if (procNum % SystemParameters.Level2CoreNum != 0 || procNum / SystemParameters.Level2CoreNum < 1) {
			System.err.println("Number of cores must be multiple of " + SystemParameters.Level2CoreNum);
			System.exit(-1);
		}

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
							: ((double) (rng
									.nextInt(SystemParameters.costFactorMAX[i] - SystemParameters.costFactorMIN[i])
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
			List<Node> history_level3, Node n, int proc, boolean cacheAware, long additionalTime, boolean error) {

		Pair<Long, Integer> res = computeET(time, history_level1, history_level2, history_level3, n, proc, cacheAware,
				additionalTime);

		if (n != null && error) {
			double err = n.cvp.getVary();
			
			if(err < -1 || err > 1) {
				System.out.println("Error Value: " + err);
				System.exit(-1);
			}

//			System.out.println(n.getFullName() + "   :   " + err);
			
			long ETwithErr = (long) Math.ceil((double) res.getFirst() * (1.0 + err));

			return new Pair<Long, Integer>(ETwithErr, res.getSecond());
		} else
			return res;

	}

	private Pair<Long, Integer> computeET(long time, List<List<Node>> history_level1, List<List<Node>> history_level2,
			List<Node> history_level3, Node n, int proc, boolean cacheAware, long additionalTime) {
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
		long leve1Time = time != -1 ? time
				: getTimeofLastIndex(history_level1.get(proc), n, SystemParameters.v2) + additionalTime;

		switch (type) {
		case TIME_DEFAULT:
			if (SystemParameters.v1 <= leve1Time && leve1Time <= SystemParameters.v2) {
				double speedUp = (SystemParameters.delta2 - SystemParameters.delta1) / (double) SystemParameters.v2
						* (double) leve1Time + SystemParameters.delta1;

				return new Pair<Long, Integer>((long) Math.ceil((double) n.getWCET() * speedUp), 1);
			}
			break;

		case TIME_CURVE:
			if (SystemParameters.v1 <= leve1Time && leve1Time <= SystemParameters.v2) {
				double speedUp = (SystemParameters.delta2 - SystemParameters.delta1)
						* (Math.pow(leve1Time, 3) - SystemParameters.v1) / Math.pow(SystemParameters.v2, 3)
						+ SystemParameters.delta1;

				return new Pair<Long, Integer>((long) Math.ceil((double) n.getWCET() * speedUp), 1);
			}

		case TIME_STEP:
			if (SystemParameters.v1 <= leve1Time && leve1Time <= SystemParameters.v2) {
				double timeNormalised = (double) (4 * (leve1Time - SystemParameters.v1))
						/ (double) (SystemParameters.v2 - SystemParameters.v1) - 2;

				double speedUp = (SystemParameters.delta2 - SystemParameters.delta1) * (Erf.erf(timeNormalised) - -1)
						/ 2 + SystemParameters.delta1;

				return new Pair<Long, Integer>((long) Math.ceil((double) n.getWCET() * speedUp), 1);
			}
		default:
			break;
		}

		/**************************************************************************
		 ************************* level 2 recency distance ************************
		 ***************************************************************************/
		int clusterID = proc / SystemParameters.Level2CoreNum;
		long level2Time = time != -1 ? time
				: getTimeofLastIndex(history_level2.get(clusterID), n, SystemParameters.v3) + additionalTime;

		switch (type) {
		case TIME_DEFAULT:
			if (SystemParameters.v1 <= level2Time && level2Time < SystemParameters.v2) {
				double speedUp = SystemParameters.delta2;

				return new Pair<Long, Integer>((long) Math.ceil((double) n.getWCET() * speedUp), 2);
			}

			if (SystemParameters.v2 <= level2Time && level2Time <= SystemParameters.v3) {
				double speedUp = (SystemParameters.delta3 - SystemParameters.delta2) / (double) SystemParameters.v3
						* (double) level2Time + SystemParameters.delta2;

				return new Pair<Long, Integer>((long) Math.ceil((double) n.getWCET() * speedUp), 2);
			}
			break;

		case TIME_CURVE:
			if (SystemParameters.v1 <= level2Time && level2Time < SystemParameters.v2) {
				double speedUp = SystemParameters.delta2;

				return new Pair<Long, Integer>((long) Math.ceil((double) n.getWCET() * speedUp), 2);
			}

			if (SystemParameters.v2 <= level2Time && level2Time <= SystemParameters.v3) {
				double speedUp = (SystemParameters.delta3 - SystemParameters.delta2)
						* (Math.pow(level2Time, 3) - SystemParameters.v2) / Math.pow(SystemParameters.v3, 3)
						+ SystemParameters.delta2;

				return new Pair<Long, Integer>((long) Math.ceil((double) n.getWCET() * speedUp), 2);
			}
			break;
		case TIME_STEP:
			if (SystemParameters.v1 <= level2Time && level2Time < SystemParameters.v2) {
				double speedUp = SystemParameters.delta2;

				return new Pair<Long, Integer>((long) Math.ceil((double) n.getWCET() * speedUp), 2);
			}

			if (SystemParameters.v2 <= level2Time && level2Time <= SystemParameters.v3) {
				double timeNormalised = (double) (4 * (leve1Time - SystemParameters.v2))
						/ (double) (SystemParameters.v3 - SystemParameters.v2) - 2;

				double speedUp = (SystemParameters.delta3 - SystemParameters.delta2) * (Erf.erf(timeNormalised) - -1)
						/ 2 + SystemParameters.delta2;

				return new Pair<Long, Integer>((long) Math.ceil((double) n.getWCET() * speedUp), 2);
			}

		default:
			break;
		}

		/**************************************************************************
		 ************************* level 3 recency distance ************************
		 ***************************************************************************/
		long level3Time = time != -1 ? time
				: getTimeofLastIndex(history_level3, n, SystemParameters.v4) + additionalTime;

		switch (type) {
		case TIME_DEFAULT:

			if (SystemParameters.v1 <= level3Time && level3Time < SystemParameters.v3) {
				double speedUp = SystemParameters.delta3;

				return new Pair<Long, Integer>((long) Math.ceil((double) n.getWCET() * speedUp), 3);
			}

			if (SystemParameters.v3 <= level3Time && level3Time <= SystemParameters.v4) {
				double speedUp = (SystemParameters.delta4 - SystemParameters.delta3) / (double) SystemParameters.v4
						* (double) level3Time + SystemParameters.delta3;

				return new Pair<Long, Integer>((long) Math.ceil((double) n.getWCET() * speedUp), 3);
			}

			break;

		case TIME_CURVE:
			if (SystemParameters.v1 <= level3Time && level3Time < SystemParameters.v3) {
				double speedUp = SystemParameters.delta3;

				return new Pair<Long, Integer>((long) Math.ceil((double) n.getWCET() * speedUp), 3);
			}

			if (SystemParameters.v3 <= level3Time && level3Time <= SystemParameters.v4) {
				double speedUp = (SystemParameters.delta4 - SystemParameters.delta3)
						* (Math.pow(level3Time, 3) - SystemParameters.v3) / Math.pow(SystemParameters.v4, 3)
						+ SystemParameters.delta3;

				return new Pair<Long, Integer>((long) Math.ceil((double) n.getWCET() * speedUp), 3);
			}

		case TIME_STEP:
			if (SystemParameters.v1 <= level3Time && level3Time < SystemParameters.v3) {
				double speedUp = SystemParameters.delta3;

				return new Pair<Long, Integer>((long) Math.ceil((double) n.getWCET() * speedUp), 3);
			}

			if (SystemParameters.v3 <= level3Time && level3Time <= SystemParameters.v4) {
				double timeNormalised = (double) (4 * (leve1Time - SystemParameters.v3))
						/ (double) (SystemParameters.v4 - SystemParameters.v3) - 2;

				double speedUp = (SystemParameters.delta4 - SystemParameters.delta3) * (Erf.erf(timeNormalised) - -1)
						/ 2 + SystemParameters.delta3;

				return new Pair<Long, Integer>((long) Math.ceil((double) n.getWCET() * speedUp), 3);
			}

		default:
			break;
		}

		return new Pair<Long, Integer>(ET, 4);

	}

	public double createRecencyTableByOrder(long time) {

		if (SystemParameters.v1 < time && time <= SystemParameters.v2) {
			return (SystemParameters.delta2 - SystemParameters.delta1) / SystemParameters.v2 * time
					+ SystemParameters.delta1;
		} else if (SystemParameters.v1 < time && time <= SystemParameters.v3)
			return (SystemParameters.delta3 - SystemParameters.delta2) / SystemParameters.v3 * time
					+ SystemParameters.delta2;
		else if (SystemParameters.v1 < time && time <= SystemParameters.v4)
			return (SystemParameters.delta4 - SystemParameters.delta3) / SystemParameters.v4 * time
					+ SystemParameters.delta3;
		else // start <= v1 || v2 <= start
			return SystemParameters.delta4;
	}

	private long getTimeofLastIndex(List<Node> nodes, Node n, long bound) {

		long time = 0;

		for (int i = nodes.size() - 1; i >= 0; i--) {
			if (nodes.get(i).getDagID() == n.getDagID() && nodes.get(i).getId() == n.getId()) {
				time = time + nodes.get(i).expectedET;
				return time;
			} else {
				time = time + nodes.get(i).expectedET;
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

		// Recency table = new Recency(RecencyType.ORDER, 8, 1000);
		//
		// for (int j = 0; j < table.recencyTable.get(0).size(); j++) {
		// for (int i = 0; i < table.recencyTable.size(); i++) {
		// System.out.print(table.recencyTable.get(i).get(j) + " | ");
		// }
		// System.out.println();
		// }

		// long time = 0;
		// boolean fault = false;
		//
		// Random rng = new Random(1000);
		//
		// StringBuilder distance = new StringBuilder();
		// StringBuilder recency = new StringBuilder();
		//
		// for (; time <= SystemParameters.v4; time += 1) {
		// double speedUp = -1;
		//
		// if (SystemParameters.v1 <= time && time <= SystemParameters.v2) {
		// speedUp = (SystemParameters.delta2 - SystemParameters.delta1) /
		// (double) SystemParameters.v2
		// * (double) time + SystemParameters.delta1;
		//
		// if (fault && rng.nextInt(100) < SystemParameters.fault_rate) {
		// double faultRange = ((double)
		// (rng.nextInt(SystemParameters.fault_range)
		// - SystemParameters.fault_median)) / (double) 100;
		//
		// speedUp = speedUp + speedUp * faultRange;
		// }
		//
		// }
		//
		// if (SystemParameters.v2 < time && time <= SystemParameters.v3) {
		// speedUp = (SystemParameters.delta3 - SystemParameters.delta2) /
		// (double) SystemParameters.v3
		// * (double) time + SystemParameters.delta2;
		//
		// if (fault && rng.nextInt(100) < SystemParameters.fault_rate) {
		// double faultRange = ((double)
		// (rng.nextInt(SystemParameters.fault_range)
		// - SystemParameters.fault_median)) / (double) 100;
		//
		// speedUp = speedUp + speedUp * faultRange;
		// }
		// }
		//
		// if (SystemParameters.v3 < time && time <= SystemParameters.v4) {
		// speedUp = (SystemParameters.delta4 - SystemParameters.delta3) /
		// (double) SystemParameters.v4
		// * (double) time + SystemParameters.delta3;
		//
		// if (fault && rng.nextInt(100) < SystemParameters.fault_rate) {
		// double faultRange = ((double)
		// (rng.nextInt(SystemParameters.fault_range)
		// - SystemParameters.fault_median)) / (double) 100;
		//
		// speedUp = speedUp + speedUp * faultRange;
		// }
		// }
		//
		// distance.append(time + "\n");
		// recency.append(speedUp + "\n");
		//// System.out.println(time);
		//// System.out.println(speedUp);
		// }
		//
		// Utils.writeResult("result/recency_distance.txt",
		// distance.toString());
		// Utils.writeResult("result/recency_value.txt", recency.toString());

		// for(double i=-2; i<=2;i=i+0.01) {
		// double d =Erf.erf(i);
		// System.out.println(d);
		// }

		RecencyProfile rp = new RecencyProfile(RecencyType.TIME_DEFAULT, 8, 1000);

		Node n = new Node(1000000, -1, NodeType.NORMAL, -1, -1);

		for (long i = SystemParameters.v1; i < SystemParameters.v4; i += 100) {

			long a = rp.computeET(i, null, null, null, n, 8, true, 0, true).getFirst();
			System.out.println(a);
		}

	}
}
