package uk.ac.york.mocha.simulator.dag;

import java.io.Serializable;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.special.Erf;
import org.apache.commons.math3.util.Pair;

import uk.ac.york.mocha.simulator.dag.Node.NodeType;
import uk.ac.york.mocha.simulator.generator.CacheHierarchy;
import uk.ac.york.mocha.simulator.parameters.SystemParameters;
import uk.ac.york.mocha.simulator.parameters.SystemParameters.RecencyType;

public class RecencyProfileSyn extends RecencyProfile implements Serializable {
	
	private static final long serialVersionUID = -6902741116934537381L;

	public RecencyProfileSyn(CacheHierarchy cache, int procNum, int seed) {

		this(cache, RecencyType.TIME_DEFAULT, procNum, seed);
	}

	public RecencyProfileSyn(CacheHierarchy cache, RecencyType type, int procNum, int seed) {

		super(cache, type);
		
	}

	public Pair<Long, Integer> computeET(long time, List<List<Node>> history_level1, List<List<Node>> history_level2,
			List<Node> history_level3, Node n, int proc, boolean cacheAware, long additionalTime, boolean error) {

		Pair<Long, Integer> res = computeET(time, history_level1, history_level2, history_level3, n, proc, cacheAware,
				additionalTime);

		if (n != null && error) {
			double err = n.cvp.getVary(res.getSecond());
//			double err = n.cvp.getMedian();

			if (err < -1 || err > 1) {
				System.out.println("Error Value: " + err);
				System.exit(-1);
			}

//			System.out.println(n.getFullName() + "   :   err: " + err);

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

		RecencyProfileSyn rp = new RecencyProfileSyn(new CacheHierarchy(8, 3, 4),RecencyType.TIME_DEFAULT, 8, 1000);

		Node n = new Node(1000000, -1, NodeType.NORMAL, -1, -1, rp, false, new Random(1000));

		for (long i = SystemParameters.v1; i < SystemParameters.v4; i += 100) {

			long a = rp.computeET(i, null, null, null, n, 8, true, 0, true).getFirst();
			System.out.println(a);
		}

	}
}
