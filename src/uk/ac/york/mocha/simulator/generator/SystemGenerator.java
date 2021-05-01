package uk.ac.york.mocha.simulator.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import uk.ac.york.mocha.simulator.dag.DirectedAcyclicGraph;
import uk.ac.york.mocha.simulator.dag.Node;
import uk.ac.york.mocha.simulator.parameters.SystemParameters;
import uk.ac.york.mocha.simulator.parameters.SchedulingParameters;
import uk.ac.york.mocha.simulator.parameters.StructuralParameters;
import uk.ac.york.mocha.simulator.simulator.Utils;

public class SystemGenerator {

	private boolean isHarmonic; // distribution of periods

	private int total_tasks;
	private double totalUtil;

	private int seed;
	private Random ran;

	private boolean print;
	private int cores;

	public SystemGenerator(int total_partitions, int totalTasks, boolean isHarmonic, int seed, boolean print) {

		this.totalUtil = 1.0 * (double) totalTasks;
		this.total_tasks = totalTasks;
		this.isHarmonic = isHarmonic;
		this.print = print;
		this.ran = new Random(seed);
		this.cores = total_partitions;
	}

	public List<DirectedAcyclicGraph> generatedDAGInstancesInOneHP(int forceInstanceNum, int hyperPeriodNum,
			List<Long> periods) {

		if (periods != null && periods.size() != total_tasks) {
			System.err
					.println("SystemGenerator.generatedDAGInstancesInOneHP(): # of given periods != # of total tasks");
			System.exit(-1);
		}

		if (print)
			System.out.println(
					"----------------------------------- Scheduling parameters -----------------------------------");

		List<DirectedAcyclicGraph> dagTasks = generateSporadicDAGs(periods);

		List<DirectedAcyclicGraph> dags = new ArrayList<>();

		if (hyperPeriodNum > 0) {
			
			long hyperPeriod = Utils.getHyperPeriod(
					dagTasks.stream().map(c -> c.getSchedParameters().getPeriod()).collect(Collectors.toList()));

			long totoalDuration = hyperPeriod * hyperPeriodNum;

			for (DirectedAcyclicGraph dag : dagTasks) {
				long instances = totoalDuration / dag.getSchedParameters().getPeriod();

				dag.totalInstNum = instances > forceInstanceNum? instances : forceInstanceNum;
				
				if(dag.totalInstNum <=0) {
					System.err.println("SystemGenerator.generatedDAGInstancesInOneHP(): DAG instances is less or equal to 0!");
					System.exit(-1);
				}

				dags.addAll(dag.getInstances(instances));
			}

		} else if (forceInstanceNum > 0) {
			for (DirectedAcyclicGraph dag : dagTasks) {
				dag.totalInstNum = forceInstanceNum;
				dags.addAll(dag.getInstances(forceInstanceNum));
			}
		} else {
			long hyperPeriod = Utils.getHyperPeriod(
					dagTasks.stream().map(c -> c.getSchedParameters().getPeriod()).collect(Collectors.toList()));

			for (DirectedAcyclicGraph dag : dagTasks) {
				int instances = (int) (hyperPeriod / dag.getSchedParameters().getPeriod());
				assert (instances > 0);

				dags.addAll(dag.getInstances(instances));
			}
		}

		if (dags == null || dags.size() == 0) {
			System.out.println("SystemGenerator.generatedDAGInstancesInOneHP()");
			System.exit(-1);
		}

		return dags;
	}

	private List<DirectedAcyclicGraph> generateSporadicDAGs(List<Long> periods) {

		List<DirectedAcyclicGraph> dags = new ArrayList<>();
		List<SchedulingParameters> schedParam = generateSchedParam(periods);

		/*
		 * Assign scheduling parameters to each DAG
		 */
		for (int i = 0; i < total_tasks; i++) {
			int minLayer = 5;
			int maxLayer = 8;
			int parallelism = 10;
			double connectProb = 0.5;

			StructuralParameters dag_param = new StructuralParameters(maxLayer, minLayer, parallelism, connectProb,
					seed);
			DirectedAcyclicGraph dagTask = new DirectedAcyclicGraph(schedParam.get(i), dag_param, i, seed);
			dags.add(dagTask);
		}

		generateWCETs(dags);

		for (DirectedAcyclicGraph d : dags) {
			d.findPath(true);
			d.findPath(false);
		}

//		for (DirectedAcyclicGraph d : dags)
//			System.out.println(d.toString());

		return dags;
	}

	private void generateWCETs(List<DirectedAcyclicGraph> dags) {

		if (print) {
			System.out.println("Assigned and generated WCET (in us):");
			System.out.println("-----------------------------------------------------------------------------");
		}

		for (DirectedAcyclicGraph d : dags) {

			long totalWCET = d.getSchedParameters().getWCET();
			List<Node> node = d.getFlatNodes();

			long[] c = new long[d.getNodeNum()];
			long sumC = 0;

			for (int i = 0; i < c.length; i++) {
				c[i] = ran.nextInt(100);
				sumC += c[i];
			}

			double ratio = (double) sumC / (double) totalWCET;

			long sum = 0;
			for (int i = 0; i < c.length; i++) {
				long cNode = (long) Math.ceil((double) c[i] / ratio);
				sum += cNode;
				if (cNode == 0)
					cNode = 1;
				node.get(i).setWCET(cNode);
			}

			d.setWCET(sum);
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
		UUnifastDiscard unifastDiscard = new UUnifastDiscard(totalUtil, total_tasks, 1000, cores, ran);
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
		for (int i = 0; i < total_tasks; i++)
			priorities.add(SystemParameters.MAX_PRIORITY - (i + 1) * 2);
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
