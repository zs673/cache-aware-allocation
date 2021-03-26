package uk.ac.york.mocha.simulator.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import uk.ac.york.mocha.simulator.dag.DirectedAcyclicGraph;
import uk.ac.york.mocha.simulator.dag.Node;
import uk.ac.york.mocha.simulator.parameters.SchedulingParameters;
import uk.ac.york.mocha.simulator.parameters.StructuralParameters;

public class SystemGenerator {
	public final static int MAX_PRIORITY = 1000;
	public final static int MAX_PERIOD = 1440;
	public final static int MIN_PERIOD = 120;

	private boolean isHarmonic; // distribution of periods
	private int maxT;
	private int minT;

	private int total_tasks;
	private double totalUtil;

	private int seed;
	private Random ran;

	private boolean print;

	public SystemGenerator(int minT, int maxT, int total_partitions, int totalTasks, boolean isPeriodLogUni, int seed) {
		this.minT = minT;
		this.maxT = maxT;
		this.totalUtil = 0.5 * (double) totalTasks;
		this.total_tasks = totalTasks;
		this.isHarmonic = isPeriodLogUni;
		this.print = true;
		this.ran = new Random(seed);
	}

	public List<DirectedAcyclicGraph> generatedDAGInstancesInOneHP(int forceInstanceNum) {

		if (print)
			System.out.println(
					"*********************************** Scheduling parameters ***********************************");

		List<DirectedAcyclicGraph> dagTasks = generateSporadicDAGs();

		if (print)
			System.out.println(
					"*********************************************************************************************");

		List<DirectedAcyclicGraph> dags = new ArrayList<>();
		
		if(forceInstanceNum > 0) {
			for (DirectedAcyclicGraph dag : dagTasks) {
				int instances = forceInstanceNum;
				assert (instances > 0);

				dags.addAll(dag.getInstances(instances));
			}
		}
		else {
			long hyperPeriod = getHyperPeriod(
					dagTasks.stream().map(c -> c.getSchedParameters().getPeriod()).collect(Collectors.toList()));

			for (DirectedAcyclicGraph dag : dagTasks) {
				int instances = (int) (hyperPeriod / dag.getSchedParameters().getPeriod());
				assert (instances > 0);

				dags.addAll(dag.getInstances(instances));
			}
		}



		return dags;
	}

	/*
	 * Compute the hyperperiod of input DAGs. NOTE: The simulation covers a complete
	 * hyperperiod.
	 */
	private long getHyperPeriod(List<Long> periods) {
		List<Long> period_copy = new ArrayList<>(periods);
		long lcm = 1;
		int divisor = 2;

		while (true) {
			int counter = 0;
			boolean divisible = false;

			for (int i = 0; i < period_copy.size(); i++) {

				if (period_copy.get(i) == 1) {
					counter++;
				}

				if (period_copy.get(i) % divisor == 0) {
					divisible = true;
					period_copy.set(i, period_copy.get(i) / divisor);
				}
			}

			if (divisible) {
				lcm = lcm * divisor;
			} else {
				divisor++;
			}

			if (counter == period_copy.size()) {
				return lcm;
			}
		}
	}

	private List<DirectedAcyclicGraph> generateSporadicDAGs() {

		List<DirectedAcyclicGraph> dags = new ArrayList<>();
		List<SchedulingParameters> schedParam = generateSchedParam();

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
	private List<SchedulingParameters> generateSchedParam() {

		/*
		 * generates uniformly distributed periods
		 */
		List<Long> periods = new ArrayList<>(total_tasks);
		while (true) {

			if (isHarmonic) {

				/* harmonic period, same periods are allowed */
				List<Long> harmonicPeriods = new ArrayList<>();

				for (long i = 1; i <= MAX_PERIOD; ++i) {
					if (MAX_PERIOD % i == 0 && i >= 10) {
						harmonicPeriods.add(i * 1000);
					}
				}
//				
//				if (harmonicPeriods.size() <= total_tasks) {
//					System.err.println("not enough harmonic periods");
//					System.exit(-1);
//				}

				long period = harmonicPeriods.get(ran.nextInt(harmonicPeriods.size()));
				if (!periods.contains(period))
					periods.add(period);

			} else {
				/* log Uniform distrubtion */
				double a1 = Math.log(minT);
				double a2 = Math.log(maxT + 1);
				double scaled = ran.nextDouble() * (a2 - a1);
				double shifted = scaled + a1;
				double exp = Math.exp(shifted);

				int result = (int) exp;
				result = Math.max(minT, result);
				result = Math.min(maxT, result);

				long period = result * 1000;
				if (!periods.contains(period))
					periods.add(period);
			}

			if (periods.size() >= total_tasks)
				break;
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
		UUnifastDiscard unifastDiscard = new UUnifastDiscard(totalUtil, total_tasks, 1000, ran);
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
			priorities.add(MAX_PRIORITY - (i + 1) * 2);
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

		schedParams.sort((p1, p2) -> Long.compare(p1.getWCET(), p2.getWCET()));

		return schedParams;
	}

	public static void main(String args[]) {
		SystemGenerator gen = new SystemGenerator(100, 1000, 32, 16, true, 1000);
		gen.generatedDAGInstancesInOneHP(-1);
	}

}
