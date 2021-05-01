package uk.ac.york.mocha.simulator.experiments;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.descriptive.rank.Median;

import uk.ac.york.mocha.simulator.dag.DirectedAcyclicGraph;
import uk.ac.york.mocha.simulator.generator.SystemGenerator;
import uk.ac.york.mocha.simulator.parameters.SystemParameters;
import uk.ac.york.mocha.simulator.parameters.SystemParameters.RecencyType;
import uk.ac.york.mocha.simulator.simulator.Simualtor;
import uk.ac.york.mocha.simulator.simulator.Simualtor.Allocation;
import uk.ac.york.mocha.simulator.simulator.Simualtor.Hardware;
import uk.ac.york.mocha.simulator.simulator.Simualtor.SimuType;
import uk.ac.york.mocha.simulator.simulator.Utils;

import org.apache.commons.math3.util.Pair;

public class SimpleAllocationCompare {

	public enum expName {
		taskNum, periods, tasks
	}

	static int NoS = 1000;
	static boolean printSim = true;
	static boolean printGen = true;

	static DecimalFormat df = new DecimalFormat("#.###");

	public static void main(String args[]) {

//		changePeriodsRunner(5);
		changeTaskNumRunner(8);
	}

	public static void changeTaskNumRunner(int numMax) {

		int intanceNum = 10;
		int hyperPeriodNum = 3;
		int seed = 1000;
		int NoP = 8;

		List<Long> harmonicPeriods = new ArrayList<>();

		for (long i = 120; i <= SystemParameters.MAX_PERIOD; ++i) {
			if (SystemParameters.MAX_PERIOD % i == 0) {
				harmonicPeriods.add(i * 1000);
			}
		}

		List<Thread> threads = new ArrayList<>();

		for (int i = 1; i <= numMax; i++) {
			final int num = i;

			List<Long> periods = new ArrayList<>();
			for (int j = 0; j < num; j++) {
				periods.add(harmonicPeriods.get(j));
			}
			periods.sort((c1, c2) -> Long.compare(c1, c2));

			threads.add(new Thread(new Runnable() {
				@Override
				public void run() {
					RunOneGroup(num, intanceNum, hyperPeriodNum, NoP, seed, seed, periods, expName.taskNum);
				}
			}));
		}

		for (Thread t : threads)
			t.start();

		try {
			for (Thread t : threads)
				t.join();
		} catch (InterruptedException e) {
		}

	}

	public static void changePeriodRunner(int testNum) {

		int seed = 1000;
		int NoP = 8;
		int NoT = 2;
		int instanceNo = 50;

		List<Thread> threads = new ArrayList<>();

		for (int i = 1; i <= testNum; i++) {
			final int num = i;
			threads.add(new Thread(new Runnable() {
				@Override
				public void run() {
					long startT = 100000;
					List<Long> periods = new ArrayList<>();
					periods.add(startT);
					periods.add(startT * num);

					RunOneGroup(NoT, instanceNo, 1, NoP, seed, seed, periods, expName.periods);
				}
			}));
		}

		for (Thread t : threads)
			t.start();

		try {
			for (Thread t : threads)
				t.join();
		} catch (InterruptedException e) {
		}

	}

	public static void RunOneGroup(int taskNum, int intanceNum, int hyperperiodNum, int procNum, int taskSeed,
			int tableSeed, List<Long> periods, expName name) {

		List<List<String>> duration = new ArrayList<>();
		List<List<String>> finishTime = new ArrayList<>();

		List<List<String>> util = new ArrayList<>();
		List<List<String>> utilCompare = new ArrayList<>();

		List<List<String>> durationCompare = new ArrayList<>();
		List<List<String>> finishTimeCompare = new ArrayList<>();

		long[] instanceNo = new long[taskNum];

		if(periods != null) {
			long totalHP = Utils.getHyperPeriod(periods) * hyperperiodNum;

			for (int i = 0; i < periods.size(); i++) {
				long insNo = (totalHP / periods.get(i));
				instanceNo[i] = insNo;
			}
		}
		else {
			for(int i=0; i<instanceNo.length; i++)
				instanceNo[i] = intanceNum;
		}

//		List<List<DirectedAcyclicGraph>> allSystems = new ArrayList<>();
//		for (int i = 0; i < NoS; i++) {
//			System.out.println(
//					"\n\n****************************************************************************************************");
//			System.out.println("Generated system: " + i );
//			SystemGenerator gen = new SystemGenerator(procNum, taskNum, true, taskSeed);
//			List<DirectedAcyclicGraph> dags = gen.generatedDAGInstancesInOneHP(intanceNum, hyperperiodNum, periods);
//
//			allSystems.add(dags);
//			taskSeed++;
//		}

		for (int i = 0; i < NoS; i++) {

			if (i == 29) {
				System.out.println("here");
			}

			System.out.println(
					"\n\n****************************************************************************************************");
			System.out.println("Change Task Number: " + taskNum + " --- Current system number: " + (i + 1));

			SystemGenerator gen = new SystemGenerator(procNum, taskNum, true, taskSeed, printGen);
			List<DirectedAcyclicGraph> dags = gen.generatedDAGInstancesInOneHP(intanceNum, hyperperiodNum, periods);

			List<String> out = testOneCase(dags, taskNum, intanceNum, procNum, taskSeed, tableSeed, periods);

			String[] durationOneCase = out.get(0).split("\n");
			List<String> d = new ArrayList<>();
			for (int j = 0; j < durationOneCase.length; j++)
				d.add(durationOneCase[j]);
			duration.add(d);

			String[] durationCompareOneCase = out.get(1).split("\n");
			List<String> ds = new ArrayList<>();
			for (int j = 0; j < durationCompareOneCase.length; j++)
				ds.add(durationCompareOneCase[j]);
			durationCompare.add(ds);

			String[] UtilOneCase = out.get(2).split("\n");
			List<String> u = new ArrayList<>();
			for (int j = 0; j < UtilOneCase.length; j++)
				u.add(UtilOneCase[j]);
			util.add(u);

			String[] UtilCompareOneCase = out.get(3).split("\n");
			List<String> us = new ArrayList<>();
			for (int j = 0; j < UtilCompareOneCase.length; j++)
				us.add(UtilCompareOneCase[j]);
			utilCompare.add(us);

			String[] finishOneCase = out.get(4).split("\n");
			List<String> f = new ArrayList<>();
			for (int j = 0; j < finishOneCase.length; j++)
				f.add(finishOneCase[j]);
			finishTime.add(f);

			String[] finishCompareOneCase = out.get(5).split("\n");
			List<String> fs = new ArrayList<>();
			for (int j = 0; j < finishCompareOneCase.length; j++)
				fs.add(finishCompareOneCase[j]);
			finishTimeCompare.add(fs);

			taskSeed++;
		}

		String folder = "result/" + name + "/";
		File theDir = new File(folder);
		if (!theDir.exists()) {
			theDir.mkdirs();
		}

		resultAnalyzer(duration, folder, "duration_" + taskNum + ".txt");
		resultAnalyzer(durationCompare, folder, "duration_compare_" + taskNum + ".txt");

		resultAnalyzer(util, folder, "util_" + taskNum + ".txt");
		resultAnalyzer(utilCompare, folder, "util_compare_" + taskNum + ".txt");

		resultAnalyzer(finishTime, folder, "finish_" + taskNum + ".txt");
		resultAnalyzer(finishTimeCompare, folder, "finish_compare_" + taskNum + ".txt");

		String instanceNumString = "";
		for (int i = 0; i < instanceNo.length; i++) {
			if (i != instanceNo.length - 1)
				instanceNumString += instanceNo[i] + ",";
			else
				instanceNumString += instanceNo[i] + "\n";
		}
		Utils.writeResult(folder + "instanceNum_" + taskNum + ".txt", instanceNumString);
	}

	/**
	 * This test case will generate two fixed DAG strcuture.
	 */
	public static List<String> testOneCase(List<DirectedAcyclicGraph> dags, int tasks, int NoInstances, int cores,
			int taskSeed, int tableSeed, List<Long> periods) {

		Simualtor cacheLBSim = new Simualtor(SimuType.CLOCK_LEVEL, Hardware.PROC_CACHE, Allocation.LOAD_BALANCE,
				RecencyType.TIME, dags, cores, tableSeed, false);
		List<DirectedAcyclicGraph> method1 = cacheLBSim.simulate(printSim);

		Simualtor cacheCASim = new Simualtor(SimuType.CLOCK_LEVEL, Hardware.PROC_CACHE, Allocation.CACHE_AWARE,
				RecencyType.TIME, dags, cores, tableSeed, true);
		List<DirectedAcyclicGraph> method2 = cacheCASim.simulate(printSim);

		List<List<DirectedAcyclicGraph>> allMethods = new ArrayList<>();
		allMethods.add(method1);
		allMethods.add(method2);

		/**
		 * Get the max duration & finish of each DAG task for normalizaiton.
		 */
		int maxID = dags.stream().mapToInt(c -> c.id).max().getAsInt();
		long[] maxDuration = new long[maxID + 1];
		long[] maxMakespan = new long[maxID + 1];
		long[] maxFinish = new long[maxID + 1];

		for (int i = 0; i < maxDuration.length; i++) {
			maxDuration[i] = Long.MIN_VALUE;
			maxFinish[i] = Long.MIN_VALUE;
			maxMakespan[i] = Long.MIN_VALUE;
		}

		for (int i = 0; i < method1.size(); i++) {
			int id = method1.get(i).id;

			long dagFinish = method1.get(i).finishTime - method1.get(i).releaseTime > method2.get(i).finishTime
					- method2.get(i).releaseTime ? method1.get(i).finishTime - method1.get(i).releaseTime
							: method2.get(i).finishTime - method2.get(i).releaseTime;

			long makespan_m1 = method1.get(i).getFlatNodes().stream().mapToLong(c -> c.finishAt - c.start).sum();
			long makespan_m2 = method1.get(i).getFlatNodes().stream().mapToLong(c -> c.finishAt - c.start).sum();
			long dagMakespan = makespan_m1 > makespan_m2 ? makespan_m1 : makespan_m2;

			long dagDuration = method1.get(i).finishTime - method1.get(i).startTime > method2.get(i).finishTime
					- method2.get(i).startTime ? method1.get(i).finishTime - method1.get(i).startTime
							: method2.get(i).finishTime - method2.get(i).startTime;

			if (maxDuration[id] < dagDuration)
				maxDuration[id] = dagDuration;

			if (maxMakespan[id] < dagMakespan)
				maxMakespan[id] = dagMakespan;

			if (maxFinish[id] < dagFinish)
				maxFinish[id] = dagFinish;
		}

		/*
		 * Summarize duration and finish for all tested results all together.
		 */
		List<List<List<Double>>> results = new ArrayList<>();
		for (List<DirectedAcyclicGraph> oneMethod : allMethods) {

			/*
			 * we normalize duration of instances for each DAG so that the variations caused
			 * by different WCETs can be removed.
			 */
			List<Double> duration = oneMethod.stream()
					.map(c -> Double.parseDouble(
							df.format(((double) (c.finishTime - c.releaseTime)) / (double) maxDuration[c.id])))
					.collect(Collectors.toList());

			List<Double> sumC = oneMethod.stream()
					.map(c -> 
					
					Double.parseDouble(df.format((((double) c.getFlatNodes().stream().mapToLong(c1 -> c1.finishAt - c1.start).sum()
									/ (double) c.getSchedParameters().getPeriod()) / ((double) maxMakespan[c.id] / (double) c.getSchedParameters().getPeriod()))))
					
					
							)
					.collect(Collectors.toList());

			/*
			 * we don't normalize finish time as each instance as a very different finish
			 * time.
			 */
			List<Double> finish = oneMethod.stream()
					.map(c -> Double.parseDouble(df.format(((double) c.finishTime) /* / (double) maxFinish[c.id] */ )))
					.collect(Collectors.toList());

			List<List<Double>> res = new ArrayList<>();
			res.add(duration);
			res.add(sumC);
			res.add(finish);

			results.add(res);
		}

		Pair<StringBuilder, StringBuilder> duration_builder = createBuffer(results, 0);
		Pair<StringBuilder, StringBuilder> util_builder = createBuffer(results, 1);
		Pair<StringBuilder, StringBuilder> finish_builder = createBuffer(results, 2);

		/*
		 * Print organized results to console
		 */
		System.out.println("----------------------------------- Execution Summary -----------------------------------");
		dags.stream().forEach(c -> {
			System.out.print(c.id + "_" + c.instanceNo + " ");
		});
		System.out.println();

		System.out.println("Duration");
		System.out.print(duration_builder.getFirst().toString());
		System.out.println("Duration Comparsion");
		System.out.print(duration_builder.getSecond().toString());

		System.out.println("Util");
		System.out.print(util_builder.getFirst().toString());
		System.out.println("Util Comparsion");
		System.out.print(util_builder.getSecond().toString());

		System.out.println("Finish");
		System.out.print(finish_builder.getFirst().toString());
		System.out.println("Finish Comparsion");
		System.out.print(finish_builder.getSecond().toString());
		System.out.println(
				"****************************************************************************************************");

		List<String> res = new ArrayList<>();
		res.add(duration_builder.getFirst().toString());
		res.add(duration_builder.getSecond().toString());

		res.add(util_builder.getFirst().toString());
		res.add(util_builder.getSecond().toString());

		res.add(finish_builder.getFirst().toString());
		res.add(finish_builder.getSecond().toString());
		return res;
	}

	private static Pair<StringBuilder, StringBuilder> createBuffer(List<List<List<Double>>> results, int index) {

		StringBuilder builder = new StringBuilder();
		StringBuilder builder_Speedup = new StringBuilder();

		/*
		 * Add noralmised durations to duration buffer.
		 */
		for (List<List<Double>> oneMethod : results) {
			oneMethod.get(index).stream().forEach(c -> {
				builder.append(c + ",");
			});
			builder.append("\n");
		}

		/*
		 * Compute Speed up of duration for the tested method.
		 */
		for (int k = 0; k < results.size() - 1; k++) {
			List<List<Double>> m1 = results.get(k);
			List<List<Double>> m2 = results.get(k + 1);

			for (int i = 0; i < m1.get(index).size(); i++) {
				double reducePercent = ((double) (m1.get(index).get(i) - m2.get(index).get(i)))
						/ (double) m1.get(index).get(i);

				builder_Speedup.append(df.format(reducePercent) + ",");
			}
		}
		builder_Speedup.append("\n");

		return new Pair<StringBuilder, StringBuilder>(builder, builder_Speedup);
	}

	private static void resultAnalyzer(List<List<String>> res, String dir, String folder) {
		StringBuilder raw_data = new StringBuilder();
		StringBuilder out = new StringBuilder();

		System.out.println("\n\n********************************************************************");
		System.out.println("Data:");

		/*
		 * Organise results by method for all test cases.
		 */
		for (int i = 0; i < res.get(0).size(); i++) {
			final int index = i;

			res.stream().forEach(c1 -> {
				System.out.println(c1.get(index));
				raw_data.append(c1.get(index) + "\n");
			});

			System.out.println("\n");
			raw_data.append("\n");
		}

		/*
		 * Analysis the data of each method in terms of average, median, maximum and
		 * minmum values
		 */
		System.out.println("\n\nData analysis for each instance:");
		out.append("\n\nData analysis for each instance£º \n");

		System.out.printf("%10s    %10s    %10s    %10s    \n", "AVG", "MED", "MAX", "MIN");
		out.append("AVG,MED,MAX,MIN\n");

		List<List<List<Double>>> analysedDataEachMethod = new ArrayList<>();

		for (int k = 0; k < res.get(0).size(); k++) {
			List<List<Double>> summaryAll = new ArrayList<>();
			final int index = k;

			res.stream().forEach(s -> {

				String c = s.get(index);
				String[] cs = c.split(",");

				List<Double> v = new ArrayList<>();
				for (int i = 0; i < cs.length; i++) {
					try {
						double d = Double.parseDouble(cs[i]);
						v.add(d);
					} catch (NullPointerException e) {
					} catch (NumberFormatException e) {
					}
				}

				double avg = v.stream().mapToDouble(c1 -> c1).sum() / (double) v.size();
				double max = v.stream().mapToDouble(c1 -> c1).max().getAsDouble();
				double min = v.stream().mapToDouble(c1 -> c1).min().getAsDouble();

				Median median = new Median();
				double[] v_d = new double[v.size()];
				for (int i = 0; i < v.size(); i++) {
					v_d[i] = v.get(i);
				}
				double med = median.evaluate(v_d);

				List<Double> summary = new ArrayList<>();
				summary.add(avg);
				summary.add(med);
				summary.add(max);
				summary.add(min);
				summaryAll.add(summary);

				System.out.printf("%10s    ", df.format(avg));
				out.append(df.format(avg) + ",");

				System.out.printf("%10s    ", df.format(med));
				out.append(df.format(med) + ",");

				System.out.printf("%10s    ", df.format(max));
				out.append(df.format(max) + ",");

				System.out.printf("%10s \n", df.format(min));
				out.append(df.format(min) + ",\n");

			});

			System.out.println("\n");
			out.append("\n\n");
			analysedDataEachMethod.add(summaryAll);
		}

		System.out.println("\n\nFurther Data analysis of all test cases:");
		out.append("\n\nFurther Data analysis of all test cases£º \n");
		System.out.printf("     %10s    %10s    %10s    %10s \n", "avg", "med", "max", "min");
		out.append("avg med max min \n");

		for (int k = 0; k < analysedDataEachMethod.size(); k++) {
			List<List<Double>> summaryAll = analysedDataEachMethod.get(k);
			List<List<Double>> summartAllHtoV = new ArrayList<>();

			for (int j = 0; j < summaryAll.get(0).size(); j++) {
				List<Double> summary = new ArrayList<>();
				for (int i = 0; i < summaryAll.size(); i++) {
					summary.add(summaryAll.get(i).get(j));
				}
				summartAllHtoV.add(summary);
			}

			summartAllHtoV.forEach(v -> {

				Median median = new Median();
				double[] v_d = new double[v.size()];
				for (int i = 0; i < v.size(); i++) {
					v_d[i] = v.get(i);
				}

				double avg = v.stream().mapToDouble(c1 -> c1).sum() / (double) v.size();
				double med = median.evaluate(v_d);
				double max = v.stream().mapToDouble(c1 -> c1).max().getAsDouble();
				double min = v.stream().mapToDouble(c1 -> c1).min().getAsDouble();

				int count = summartAllHtoV.indexOf(v);

				switch (count) {
				case 0:
					System.out.print("AVGs ");
					out.append("AVGs,");
					break;
				case 1:
					System.out.print("MEDs ");
					out.append("MEDs,");
					break;
				case 2:
					System.out.print("MAXs ");
					out.append("MAXs,");
					break;
				case 3:
					System.out.print("MINs ");
					out.append("MINs,");
					break;
				default:
					break;
				}

				System.out.printf("%10s    ", df.format(avg));
				out.append(df.format(avg) + ",");

				System.out.printf("%10s    ", df.format(med));
				out.append(df.format(med) + ",");

				System.out.printf("%10s    ", df.format(max));
				out.append(df.format(max) + ",");

				System.out.printf("%10s \n", df.format(min));
				out.append(df.format(min) + ",\n");

			});
		}

		Utils.writeResult(dir + folder, raw_data.toString());
		Utils.writeResult(dir + "A_" + folder, out.toString());
	}

}

/********************************************
 * Discarded
 ****************************************************/
/*
 * System.out.println("\n\n Speed up of each system:");
 * out.append("\n\n Speed up of each system:\n");
 * 
 * List<Double> avgSpeedUp = new ArrayList<>(); res.forEach(c -> { String[]
 * method1_temp = c.get(0).split(" "); String[] method2_temp =
 * c.get(1).split(" ");
 * 
 * List<Double> m1 = new ArrayList<>(); List<Double> m2 = new ArrayList<>();
 * 
 * for (int i = 0; i < method1_temp.length; i++) { if (method1_temp[i].length()
 * > 0) { m1.add(Double.parseDouble(method1_temp[i])); } }
 * 
 * for (int i = 0; i < method2_temp.length; i++) { if (method2_temp[i].length()
 * > 0) { m2.add(Double.parseDouble(method2_temp[i])); } }
 * 
 * if (m1.size() != m2.size()) { System.out.println("!!!"); System.exit(-1); }
 * 
 * double totalPrec = 0; if (type == 0) { double total1 =
 * m1.stream().mapToDouble(v -> v).sum(); double total2 =
 * m2.stream().mapToDouble(v -> v).sum(); totalPrec = ((double) (total1 -
 * total2)); // / (double) total1 }
 * 
 * if (type == 1) { double makespan1 = m1.stream().mapToDouble(v ->
 * v).max().getAsDouble(); double makespan2 = m2.stream().mapToDouble(v ->
 * v).max().getAsDouble(); totalPrec = ((double) (makespan1 - makespan2)) /
 * (double) makespan1; }
 * 
 * avgSpeedUp.add(totalPrec);
 * 
 * System.out.printf("%10s\n", df.format(totalPrec));
 * out.append(String.format("%10s\n", df.format(totalPrec)));
 * 
 * });
 * 
 * int betterCase = 0; int equalCase = 0; for (Double d : avgSpeedUp) { if (d >=
 * 0) betterCase++; if (d == 0) equalCase++; }
 * 
 * System.out.println("Number of better cases for method 2: " + betterCase);
 * out.append("Number of better cases for method 2: " + betterCase);
 * 
 * System.out.println("Number of equal cases: " + equalCase);
 * out.append("Number of equal cases: " + equalCase);
 */

/*
 * StringBuilder builder_allc = new StringBuilder(); builder_allc.append(
 * "--------------------------------------------------------------------------------------------------------------\n"
 * ); System.out.println(
 * "--------------------------------------------------------------------------------------------------------------"
 * ); for (int i = 0; i < cores; i++) { if (i == 0) {
 * System.out.printf("cores%5s    ", "" + i);
 * builder_allc.append(String.format("cores%5s    ", "" + i)); } else {
 * System.out.printf("%10d    ", i);
 * builder_allc.append(String.format("%10d    ", i)); } } System.out.println(
 * "\n--------------------------------------------------------------------------------------------------------------"
 * ); builder_allc.append(
 * "\n--------------------------------------------------------------------------------------------------------------\n"
 * );
 * 
 * int dagID = method1.get(0).id; for (int i = 0; i < method1.size(); i++) { if
 * (method1.get(i).id == dagID) {
 * 
 * // System.out.println("**************** Instance" + method1.get(i).instanceNo
 * + " ****************"); // builder_allc.append("**************** Instance" +
 * method1.get(i).instanceNo + " ****************");
 * 
 * // String m1 = getDAGFullInfo(method1.get(i), cores,
 * method1.get(i).instanceNo, // Allocation.LOAD_BALANCE.toString()); //
 * builder_allc.append(m1); // //
 * 
 * String m2 = getDAGFullInfo(method2.get(i), cores, method1.get(i).instanceNo,
 * Allocation.CACHE_AWARE.toString()); builder_allc.append(m2);
 * 
 * System.out.println();
 * 
 * } }
 */

/*
 * public static String getDAGFullInfo(DirectedAcyclicGraph dag, int totalcores,
 * int instanceNo, String method) {
 * 
 * StringBuilder out = new StringBuilder();
 * 
 * List<Node> nodes = new ArrayList<>(dag.allocNodes); List<List<Node>>
 * nodesByPar = new ArrayList<>(); for (int i = 0; i < totalcores; i++) {
 * List<Node> nodesOnePar = new ArrayList<>(); nodesByPar.add(nodesOnePar); }
 * 
 * for (Node n : nodes) { int partition = n.partition;
 * nodesByPar.get(partition).add(n); }
 * 
 * for (int j = 0; j < nodesByPar.stream().mapToInt(c ->
 * c.size()).max().getAsInt(); j++) { for (int i = 0; i < nodesByPar.size();
 * i++) { try { System.out.printf("%10s    ", nodesByPar.get(i).get(j).getId());
 * out.append(String.format("%10s    ", nodesByPar.get(i).get(j).getId())); }
 * catch (Exception e) { System.out.printf("%10s    ", "-");
 * out.append(String.format("%10s    ", "-")); }
 * 
 * } System.out.println(); }
 * 
 * return out.toString();
 * 
 * }
 */