package uk.ac.york.mocha.simulator.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.Pair;

import uk.ac.york.mocha.simulator.dag.DAGtoPython;
import uk.ac.york.mocha.simulator.dag.DAG;
import uk.ac.york.mocha.simulator.dag.Node;
import uk.ac.york.mocha.simulator.parameters.SchedulingParameters;
import uk.ac.york.mocha.simulator.parameters.StructuralParameters;
import uk.ac.york.mocha.simulator.parameters.SystemParameters;
import uk.ac.york.mocha.simulator.parameters.SystemParameters.DagType;
import uk.ac.york.mocha.simulator.simulator.Utils;

public class SystemGenerator {

	private boolean isHarmonic; // distribution of periods

	private int total_tasks;
	private double totalUtil;

	private int seed;
	private Random ran;

	private boolean print;
	private int cores;
	private boolean takeAllUtil;
	private List<Double> assignedUtils;

	private boolean randomC;

	public int maxPara;
	public int minPara;

	public boolean david;

	public SystemGenerator(int total_partitions, int totalTasks, boolean isHarmonic, boolean takeAllUtil,
			List<Double> assignedUtils, int seed, boolean randomC, boolean print, double utilPerTask, int maxPara,
			int minPara, boolean david) {

		if (utilPerTask > 0) {
			this.totalUtil = utilPerTask * (double) totalTasks;
		} else {
			this.totalUtil = SystemParameters.utilPerTask * (double) totalTasks;
		}

		this.takeAllUtil = takeAllUtil;
		this.total_tasks = totalTasks;
		this.isHarmonic = isHarmonic;
		this.print = print;
		this.ran = new Random(seed);
		this.cores = total_partitions;

		this.assignedUtils = assignedUtils;

		this.randomC = randomC;
		this.maxPara = maxPara;
		this.minPara = minPara;

		this.david = david;
	}

	public SystemGenerator(int total_partitions, int totalTasks, boolean isHarmonic, boolean takeAllUtil,
			List<Double> assignedUtils, int seed, boolean randomC, boolean print, double utilPerTask, int maxPara,
			int minPara) {
		this(total_partitions, totalTasks, isHarmonic, takeAllUtil, assignedUtils, seed, randomC, print, utilPerTask,
				maxPara, minPara, false);
	}

	public SystemGenerator(int total_partitions, int totalTasks, boolean isHarmonic, boolean takeAllUtil,
			List<Double> assignedUtils, int seed, boolean randomC, boolean print) {

		this(total_partitions, totalTasks, isHarmonic, takeAllUtil, assignedUtils, seed, randomC, print,
				SystemParameters.utilPerTask, SystemParameters.maxParal, SystemParameters.minParal, false);
	}

	public Pair<List<DAG>, List<DAG>> generatedDAGInstancesInOneHP(int forceInstanceNum, int hyperPeriodNum,
			List<Long> periods, boolean hard, DagType type) {

		if (periods != null && periods.size() != total_tasks) {
			System.err
					.println("SystemGenerator.generatedDAGInstancesInOneHP(): # of given periods != # of total tasks");
			System.exit(-1);
		}

		if (print)
			System.out.println(
					"----------------------------------- Scheduling parameters -----------------------------------");

		List<DAG> dagTasks = null;
		boolean schedulable = false;

		Pair<Long, List<int[]>> res = null;

		if (hard) {
			while (!schedulable) {
				dagTasks = generateSporadicDAGs(periods, hard, type);

				/**
				 * Set offline scheduling and allocation for the hard task
				 */
				DAG dag = dagTasks.get(0);
				dag.hard = hard;

				res = DAGtoPython.pharseDAGForPython(dag, cores);

				long blocking = 0;

				if (dagTasks.size() > 1) {
					List<Node> blockingNodes = new ArrayList<>();

					for (int i = 1; i < dagTasks.size(); i++) {
						blockingNodes.addAll(dagTasks.get(i).getFlatNodes());
					}

					blockingNodes.sort((c1, c2) -> -Long.compare(c1.getET(SystemParameters.useWCET, false),
							c2.getET(SystemParameters.useWCET, false)));

					for (int i = 0; i < SystemParameters.fan_in; i++) {
						blocking += blockingNodes.get(i).getET(SystemParameters.useWCET, false);
					}
				}

				long response_time = res.getFirst() + blocking;
				if (response_time > dag.getSchedParameters().getDeadline())
					schedulable = false;
				else
					schedulable = true;
			}

			List<int[]> prio = res.getSecond();

			/*
			 * Set priority for hard real-time nodes
			 */
			for (Node n : dagTasks.get(0).getFlatNodes()) {
				int id = n.getId();

				for (int i = 0; i < prio.size(); i++) {
					if (prio.get(i)[0] - 1 == id) {
						n.priority = prio.get(i)[1];
						break;
					}
				}
			}

//			/*
//			 * Set allocation for hard real-time nodes
//			 */
//			List<Node> hardNodes = new ArrayList<>(dagTasks.get(0).getFlatNodes());
//			hardNodes.sort((c1, c2) -> -Integer.compare(c1.priority, c2.priority));
//
//			int currentCore = 0;
//			for (int i = 0; i < hardNodes.size(); i++) {
//				Node n = hardNodes.get(i);
//				n.offline_partition = currentCore;
//
//				if (currentCore == 7)
//					currentCore = 0;
//				else
//					currentCore++;
//			}
		} else {
			dagTasks = generateSporadicDAGs(periods, hard, type);
		}
		/************************************************************************************/

		List<DAG> dags = new ArrayList<>();

		if (hyperPeriodNum > 0) {

			long hyperPeriod = Utils.getHyperPeriod(
					dagTasks.stream().map(c -> c.getSchedParameters().getPeriod()).collect(Collectors.toList()));

			long totoalDuration = hyperPeriod * hyperPeriodNum;

			for (DAG dag : dagTasks) {
				long instances = totoalDuration / dag.getSchedParameters().getPeriod();

				dag.totalInstNum = instances > forceInstanceNum ? instances : forceInstanceNum;

				if (dag.totalInstNum <= 0) {
					System.err.println(
							"SystemGenerator.generatedDAGInstancesInOneHP(): DAG instances is less or equal to 0!");
					System.exit(-1);
				}

				dags.addAll(dag.getInstances(instances));
			}

		} else if (forceInstanceNum > 0) {

			long duration = dagTasks.get(total_tasks - 1).getSchedParameters().getPeriod() * forceInstanceNum;

			for (DAG dag : dagTasks) {
				dag.totalInstNum = (long) Math.ceil((double) duration / (double) dag.getSchedParameters().getPeriod());
				dags.addAll(dag.getInstances(dag.totalInstNum));
			}

		} else if (forceInstanceNum == -1 && hyperPeriodNum == -1) {
			for (DAG dag : dagTasks) {
				dags.addAll(dag.getInstances(1));
			}
		}

		else {
			long hyperPeriod = Utils.getHyperPeriod(
					dagTasks.stream().map(c -> c.getSchedParameters().getPeriod()).collect(Collectors.toList()));

			for (DAG dag : dagTasks) {
				int instances = (int) (hyperPeriod / dag.getSchedParameters().getPeriod());
				assert (instances > 0);

				dags.addAll(dag.getInstances(instances));
			}
		}

//		System.out.println("Number of instances");
//		for (DirectedAcyclicGraph d : dagTasks)
//			System.out.print(d.totalInstNum + "    ");
//		System.out.println();

		if (print) {
			if (dags == null || dags.size() == 0) {
				System.out.println("SystemGenerator.generatedDAGInstancesInOneHP()");
				System.exit(-1);
			}
		}

		dags.sort((c1, c2) -> compareDAG(c1, c2));

		Pair<List<DAG>, List<DAG>> p = new Pair<>(dags, dagTasks);

		return p;
	}

	private int compareDAG(DAG d1, DAG d2) {
		int ret = Long.compare(d1.releaseTime, d2.releaseTime);

		if (ret == 0) {
			int ret1 = -Integer.compare(d1.getSchedParameters().getPriority(), d2.getSchedParameters().getPriority());
			return ret1;
		} else
			return ret;
	}

	private List<DAG> generateSporadicDAGs(List<Long> periods, boolean hard, DagType type) {

		List<DAG> dags = new ArrayList<>();
		List<SchedulingParameters> schedParam = generateSchedParam(periods);

		/*
		 * Assign scheduling parameters to each DAG
		 */
		for (int i = 0; i < total_tasks; i++) {
			StructuralParameters dag_param = new StructuralParameters(SystemParameters.maxLayer,
					SystemParameters.minLayer, minPara, maxPara, SystemParameters.connectProb, ran);
			DAG dagTask = null;

			if (i == 0 && hard) {
				dagTask = new DAG(schedParam.get(i), dag_param, i, seed, true, type);
			} else {
				dagTask = new DAG(schedParam.get(i), dag_param, i, seed, false, type);
			}

			dags.add(dagTask);
		}

		generateWCETs(dags, david);

//		for (DirectedAcyclicGraph d : dags) {
//			d.findPath(true);
//			d.findPath(false);
//		}

//		for (DirectedAcyclicGraph d : dags)
//			System.out.println(d.toString());

//		for (DAG d : dags)
//			d.getWDM();

		return dags;
	}

	private void generateWCETs(List<DAG> dags, boolean david) {

		if (print) {
			System.out.println("Assigned and generated WCET (in us):");
			System.out.println("-----------------------------------------------------------------------------");
		}

		for (DAG d : dags) {

			long totalWCET = d.getSchedParameters().getWCET();
			List<Node> node = d.getFlatNodes();
			long[] c = new long[d.getNodeNum()];
			long sumC = 0;
			long sum = 0;

			if (david) {
				List<Double> ratio = DavidUtilisationGenerator.getDavidUtilVector(c.length, 1);

				for (int i = 0; i < c.length; i++) {
					long cNode = (long) Math.ceil((double) totalWCET * (double) ratio.get(i));

					if (cNode == 0)
						cNode = 1;

					sum += cNode;
					node.get(i).setWCET(cNode);
				}

			} else {
				for (int i = 0; i < c.length; i++) {
					c[i] = randomC ? ran.nextInt(100) : 100;
					sumC += c[i];
				}

				double ratio = (double) sumC / (double) totalWCET;

				for (int i = 0; i < c.length; i++) {
					long cNode = (long) Math.ceil((double) c[i] / ratio);

					if (cNode == 0)
						cNode = 1;

					sum += cNode;
					node.get(i).setWCET(cNode);
				}
			}

			d.getSchedParameters().setWCET(sum);

			if (print)
				System.out.printf("|    DAG_%2d   |   Assigned WCET: %10d   |   Actual WCET: %10d   |\n", d.id,
						totalWCET, sum);

		}

		if (print)
			System.out.println("-----------------------------------------------------------------------------");
	}

	/*
	 * generate scheduling parameters for DAGs
	 */
	private List<SchedulingParameters> generateSchedParam(List<Long> periodsT) {
		/*
		 * generates uniformly distributed periods
		 */
		List<Long> periods;
		if (periodsT == null) {
			periods = new ArrayList<>(total_tasks);
			while (true) {

				if (isHarmonic) {

					/* harmonic period, same periods are not allowed */
					List<Long> harmonicPeriods = new ArrayList<>();

					for (long i = SystemParameters.MIN_PERIOD; i <= SystemParameters.MAX_PERIOD; ++i) {
						if (SystemParameters.MAX_PERIOD % i == 0) {
							harmonicPeriods.add(i * 1000);
						}
					}

					if (harmonicPeriods.size() <= total_tasks) {
						System.err.println("not enough harmonic periods");
						System.exit(-1);
					}

					long period = harmonicPeriods.get(ran.nextInt(harmonicPeriods.size()));
					if (!periods.contains(period))
						periods.add(period);

				} else {
					/* log Uniform distrubtion */
					double a1 = Math.log(SystemParameters.minT);
					double a2 = Math.log(SystemParameters.maxT + 1);
					double scaled = ran.nextDouble() * (a2 - a1);
					double shifted = scaled + a1;
					double exp = Math.exp(shifted);

					int result = (int) exp;
					result = Math.max(SystemParameters.minT, result);
					result = Math.min(SystemParameters.maxT, result);

					long period = result * 1000;
					if (!periods.contains(period))
						periods.add(period);
				}

				if (periods.size() >= total_tasks)
					break;
			}

		} else {
			periods = new ArrayList<>(periodsT);
		}
		periods.sort((p1, p2) -> Double.compare(p1, p2));

		if (print) {
			System.out.print("task periods & deadline (in us): ");
			for (int i = 0; i < periods.size(); i++) {
				long p = periods.get(i);
				System.out.print(p + "   ");
			}
			System.out.println();
		}

		/*
		 * generate utils by UUifastDiscard
		 */
		List<Double> utils = null;

		if (david) {
			utils = new ArrayList<>(DavidUtilisationGenerator.getDavidUtilVector(total_tasks, totalUtil));
		} else {
			if (this.assignedUtils == null) {
				UUnifastDiscard unifastDiscard = new UUnifastDiscard(totalUtil, total_tasks, 1000, cores, takeAllUtil,
						ran);
				while (true) {
					utils = unifastDiscard.getUtils();

					double tt = 0;
					for (int i = 0; i < utils.size(); i++) {
						tt += utils.get(i);
					}

					if (utils != null)
						if (utils.size() == total_tasks && tt <= totalUtil)
							break;
				}
			} else {
				if (this.assignedUtils.size() != total_tasks) {
					System.err.println(
							"SystemGenerator.generateSchedParam(): pre-assigned utilisations does not match task number");
					System.exit(-1);
				}

				utils = new ArrayList<>(this.assignedUtils);
			}
		}

		if (print) {
			System.out.print("task utils: ");
			double tt = 0;
			for (int i = 0; i < utils.size(); i++) {
				tt += utils.get(i);
				System.out.print(utils.get(i) + "   ");
			}
			System.out.println();
			System.out.println("total uitls: " + tt);
		}

		/*
		 * generate priority by DMPO
		 */
		List<Integer> priorities = new ArrayList<>();
		for (int i = 0; i < total_tasks; i++) {
//			priorities.add(SystemParameters.MAX_PRIORITY - (i + 1) * 2);

			if (i == 0)
				priorities.add(SystemParameters.MAX_PRIORITY - (i + 1) * 2);
			else {
				priorities.add(SystemParameters.MAX_PRIORITY - (i + 1) * 2);
//				priorities.add(10); // TODO: Now we have equal priority for non real-time DAGs.
			}
		}

		priorities.sort((p1, p2) -> -Integer.compare(p1, p2));

		if (print) {
			System.out.print("task priority: ");
			for (int i = 0; i < priorities.size(); i++) {
				long p = priorities.get(i);
				System.out.print(p + "   ");
			}
			System.out.println();
		}

		/*
		 * generate scheduling parameters
		 */
		List<SchedulingParameters> schedParams = new ArrayList<>();
		for (int i = 0; i < total_tasks; i++) {
			long WCET = (long) Math.ceil((double) periods.get(i) * utils.get(i));

			SchedulingParameters param = new SchedulingParameters(priorities.get(i), periods.get(i), periods.get(i),
					WCET, utils.get(i), -1);

			schedParams.add(param);
		}

//		schedParams.sort((p1, p2) -> Long.compare(p1.getWCET(), p2.getWCET()));

		return schedParams;
	}

//	public static void main(String args[]) {
//		SystemGenerator gen = new SystemGenerator(100, 1000, 32, 16, true, 1000);
//		gen.generatedDAGInstancesInOneHP(-1, -1, null);
//	}

}
