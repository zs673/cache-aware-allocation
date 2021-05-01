package uk.ac.york.mocha.simulator.archive;
//package uk.ac.york.mocha.simulator.experiments;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.io.UnsupportedEncodingException;
//import java.text.DecimalFormat;
//import java.util.ArrayList;
//import java.util.List;
//
//import org.apache.commons.math3.stat.descriptive.rank.Median;
//
//import uk.ac.york.mocha.simulator.dag.DirectedAcyclicGraph;
//import uk.ac.york.mocha.simulator.dag.Recency.RecencyType;
//import uk.ac.york.mocha.simulator.generator.SystemGenerator;
//import uk.ac.york.mocha.simulator.simulator.OnlineSimualtor;
//import uk.ac.york.mocha.simulator.simulator.OnlineSimualtor.Allocation;
//import uk.ac.york.mocha.simulator.simulator.OnlineSimualtor.Hardware;
//import uk.ac.york.mocha.simulator.simulator.OnlineSimualtor.SimuType;
//
//public class CacheAwareCompare {
//	
//	public static int tradeCount = 0;
//
//	static int minT = 100;
//	static int maxT = 1000;
//
//	static int NoS = 1000;
//	static int NoT = 2;
//	static int NoP = 8;
//
//	static DecimalFormat df = new DecimalFormat("#.###");
//
//	public static void main(String args[]) {
//
////		changePeriodsRunner();
//		changeTaskNumRunner();
//	}
//
//	public static void changeTaskNumRunner() {
//		int intanceNum = 50;
//
//		Thread t1 = new Thread(new Runnable() {
//
//			@Override
//			public void run() {
//				changingTasksNum(2, intanceNum);
//			}
//		});
//
//		Thread t2 = new Thread(new Runnable() {
//
//			@Override
//			public void run() {
//				changingTasksNum(3, intanceNum);
//			}
//		});
//
//		Thread t3 = new Thread(new Runnable() {
//
//			@Override
//			public void run() {
//				changingTasksNum(4, intanceNum);
//			}
//		});
//
//		Thread t4 = new Thread(new Runnable() {
//
//			@Override
//			public void run() {
//				changingTasksNum(5, intanceNum);
//			}
//		});
//
//		Thread t5 = new Thread(new Runnable() {
//
//			@Override
//			public void run() {
//				changingTasksNum(6, intanceNum);
//			}
//		});
//
//		t1.start();
//		t2.start();
//		t3.start();
//		t4.start();
//		t5.start();
//
//		try {
//			t1.join();
//			t2.join();
//			t3.join();
//			t4.join();
//			t5.join();
//
//		} catch (InterruptedException e) {
//		}
//	}
//
//	public static void changePeriodsRunner() {
//		Thread t1 = new Thread(new Runnable() {
//
//			@Override
//			public void run() {
////				changingTasks();
//				changingPeriods(1);
//			}
//		});
//
//		Thread t2 = new Thread(new Runnable() {
//
//			@Override
//			public void run() {
////				changingTasksNum();
//				changingPeriods(2);
//			}
//		});
//
//		Thread t3 = new Thread(new Runnable() {
//
//			@Override
//			public void run() {
////				changingTasksNum();
//				changingPeriods(3);
//			}
//		});
//
//		Thread t4 = new Thread(new Runnable() {
//
//			@Override
//			public void run() {
////				changingTasksNum();
//				changingPeriods(4);
//			}
//		});
//
//		Thread t5 = new Thread(new Runnable() {
//
//			@Override
//			public void run() {
////				changingTasksNum();
//				changingPeriods(5);
//			}
//		});
//
//		t1.start();
//		t2.start();
//		t3.start();
//		t4.start();
//		t5.start();
//
//		try {
//			t1.join();
//			t2.join();
//			t3.join();
//			t4.join();
//			t5.join();
//
//		} catch (InterruptedException e) {
//		}
//	}
//
//	public static void changingPeriods(int num) {
//
//		long startT = 100000;
//		List<Long> periods = new ArrayList<>();
//		periods.add(startT);
//		periods.add(startT * num);
//
//		List<List<String>> resd = new ArrayList<>();
//		List<List<String>> resf = new ArrayList<>();
//
//		int seed = 1000;
//
//		for (int i = 0; i < NoS; i++) {
//			if (i == 147)
//				System.out.println();
//
//			System.out.println(
//					"\n\n****************************************************************************************************");
//			System.out.println("Change Periods: " + num + " --- Current system number: " + (i + 1));
//
//			List<String> out = testOneCase(NoT, 50, NoP, seed, 1000, periods);
//
//			String[] outSd = out.get(0).split("\n");
//
//			List<String> rd = new ArrayList<>();
//			for (int j = 0; j < outSd.length; j++)
//				rd.add(outSd[j]);
//
//			resd.add(rd);
//
//			String[] outSf = out.get(1).split("\n");
//
//			List<String> rf = new ArrayList<>();
//			for (int j = 0; j < outSf.length; j++)
//				rf.add(outSf[j]);
//
//			resf.add(rf);
//
//			seed++;
//		}
//
//		resultAnalyzer(resd, "Change_Taskset_taskNo_" + NoT + " Duration", 0);
//		resultAnalyzer(resf, "Change_Taskset_taskNo_" + NoT + " Finish", 1);
//
//	}
//
//	public static void changingTasksNum(int taskNum, int intanceNum) {
//
//		int seed = 1000;
//		List<List<String>> resd = new ArrayList<>();
//		List<List<String>> resf = new ArrayList<>();
//
//		for (int i = 0; i < NoS; i++) {
//			if (i == 148)
//				System.out.println();
//
//			System.out.println(
//					"\n\n****************************************************************************************************");
//			System.out.println("Change Task Number: " + taskNum + " --- Current system number: " + (i + 1));
//			List<String> out = testOneCase(taskNum, intanceNum, NoP, seed, 1000, null);
//
//			String[] outSd = out.get(0).split("\n");
//
//			List<String> rd = new ArrayList<>();
//			for (int j = 0; j < outSd.length; j++)
//				rd.add(outSd[j]);
//
//			resd.add(rd);
//
//			String[] outSf = out.get(1).split("\n");
//
//			List<String> rf = new ArrayList<>();
//			for (int j = 0; j < outSf.length; j++)
//				rf.add(outSf[j]);
//
//			resf.add(rf);
//
//			seed++;
//		}
//
//		resultAnalyzer(resd, "Change_Taskset_taskNo_" + taskNum + " Duration", 0);
//		resultAnalyzer(resf, "Change_Taskset_taskNo_" + taskNum + " Finish", 1);
//
//	}
//
//	public static void changingTasks() {
//		List<List<String>> resd = new ArrayList<>();
//		List<List<String>> resf = new ArrayList<>();
//
//		int seed = 1000;
//
//		for (int i = 0; i < NoS; i++) {
//			System.out.println(
//					"\n\n****************************************************************************************************");
//			System.out.println("Change Taskset --- Current system number: " + (i + 1));
//			List<String> out = testOneCase(NoT, -1, NoP, seed, 1000, null);
//
//			String[] outSd = out.get(0).split("\n");
//
//			List<String> rd = new ArrayList<>();
//			for (int j = 0; j < outSd.length; j++)
//				rd.add(outSd[j]);
//
//			resd.add(rd);
//
//			String[] outSf = out.get(1).split("\n");
//
//			List<String> rf = new ArrayList<>();
//			for (int j = 0; j < outSf.length; j++)
//				rf.add(outSf[j]);
//
//			resf.add(rf);
//
//			seed++;
//		}
//
//		resultAnalyzer(resd, "Change_Taskset_taskNo_" + NoT + " Duration", 0);
//		resultAnalyzer(resf, "Change_Taskset_taskNo_" + NoT + " Finish", 1);
//
//	}
//
//	/**
//	 * This test case will generate two fixed DAG strcuture.
//	 */
//	public static List<String> testOneCase(int tasks, int NoInstances, int cores, int taskSeed, int tableSeed,
//			List<Long> periods) {
//
//		boolean printSim = true;
//		boolean online = true;
//
//		SystemGenerator gen = new SystemGenerator(minT, maxT, cores, tasks, true, taskSeed);
//		List<DirectedAcyclicGraph> dags = gen.generatedDAGInstancesInOneHP(NoInstances, periods);
//
//		OnlineSimualtor cacheLBSim = new OnlineSimualtor(SimuType.CLOCK_LEVEL, Hardware.PROC_CACHE,
//				Allocation.LOAD_BALANCE, RecencyType.TIME, dags, cores, tableSeed, false, false);
//		List<List<Long>> cacheLBSim_res = cacheLBSim.simulate(online, printSim);
//
//		OnlineSimualtor cacheCASim = new OnlineSimualtor(SimuType.CLOCK_LEVEL, Hardware.PROC_CACHE,
//				Allocation.CACHE_AWARE, RecencyType.TIME, dags, cores, tableSeed, false, true);
//		List<List<Long>> cacheCASim_res = cacheCASim.simulate(online, printSim);
//
//		System.out.println("----------------------------------- Execution Summary -----------------------------------");
//
//		StringBuilder builder_dur = new StringBuilder();
//		System.out.println("Duration");
//
//		dags.stream().forEach(c -> {
//			System.out.printf("%10s", (c.id + "_" + c.instanceNo));
//		});
//		System.out.println();
//
//		cacheLBSim_res.get(0).stream().forEach(c -> {
//			builder_dur.append(String.format("%10s", c));
//		});
//		builder_dur.append("\n");
//
//		cacheCASim_res.get(0).stream().forEach(c -> {
//			builder_dur.append(String.format("%10s", c));
//		});
//		builder_dur.append("\n");
//
//		for (int i = 0; i < cacheLBSim_res.get(0).size(); i++) {
//			double reducePercent = ((double) (cacheLBSim_res.get(0).get(i) - cacheCASim_res.get(0).get(i)))
//					/ (double) cacheLBSim_res.get(0).get(i);
//
//			builder_dur.append(String.format("%10s", df.format(reducePercent)));
//		}
//		builder_dur.append("\n");
//
//		System.out.print(builder_dur.toString());
//
//		StringBuilder builder_fin = new StringBuilder();
//		System.out.println("Finish");
//
//		cacheLBSim_res.get(1).stream().forEach(c -> {
//			builder_fin.append(String.format("%10s", c));
//		});
//		builder_fin.append("\n");
//
//		cacheCASim_res.get(1).stream().forEach(c -> {
//			builder_fin.append(String.format("%10s", c));
//		});
//		builder_fin.append("\n");
//
//		for (int i = 0; i < cacheLBSim_res.get(1).size(); i++) {
//			double reducePercent = ((double) (cacheLBSim_res.get(1).get(i) - cacheCASim_res.get(1).get(i)))
//					/ (double) cacheLBSim_res.get(1).get(i);
//
//			builder_fin.append(String.format("%10s", df.format(reducePercent)));
//		}
//		builder_fin.append("\n");
//
//		System.out.print(builder_fin.toString());
//
//		System.out.println(
//				"****************************************************************************************************");
//
//		List<String> res = new ArrayList<>();
//		res.add(builder_dur.toString());
//		res.add(builder_fin.toString());
//		return res;
//	}
//
//	private static void resultAnalyzer(List<List<String>> res, String expName, int type) {
//		StringBuilder out = new StringBuilder();
//
//		System.out
//				.println("\n\n\n********************************** " + expName + " **********************************");
//
//		System.out.println("Absolute speed up of DAG instance:");
//		out.append("Absolute speed up of DAG instance:\n");
//
//		res.stream().forEach(c1 -> {
////			c1.forEach(c -> {
////
////				System.out.println(c);
////				out.append(c + "\n");
////			});
//
//			System.out.println(c1.get(2));
//			out.append(c1.get(2) + "\n");
//		});
//
//		System.out.println("Data analysis of speed up for DAG instance:");
//		out.append("Data analysis of speed up for DAG instance£º \n");
//
//		System.out.printf("%10s    ", "AVG");
//		out.append(String.format("\n\n%10s    ", "AVG"));
//
//		System.out.printf("%10s    ", "MED");
//		out.append(String.format("%10s    ", "MED"));
//
//		System.out.printf("%10s    ", "MAX");
//		out.append(String.format("%10s    ", "MAX"));
//
//		System.out.printf("%10s \n", "MIN");
//		out.append(String.format("%10s \n", "MIN"));
//
//		List<List<Double>> summaryAll = new ArrayList<>();
//
//		res.stream().forEach(s -> {
//
//			String c = s.get(s.size() - 1);
//			String[] cs = c.split(" ");
//
//			List<Double> v = new ArrayList<>();
//			for (int i = 0; i < cs.length; i++) {
//				try {
//					double d = Double.parseDouble(cs[i]);
//					v.add(d);
//				} catch (NullPointerException e) {
//				} catch (NumberFormatException e) {
//				}
//			}
//
//			double avg = v.stream().mapToDouble(c1 -> c1).sum() / (double) v.size();
//			double max = v.stream().mapToDouble(c1 -> c1).max().getAsDouble();
//			double min = v.stream().mapToDouble(c1 -> c1).min().getAsDouble();
//
//			Median median = new Median();
//			double[] v_d = new double[v.size()];
//			for (int i = 0; i < v.size(); i++) {
//				v_d[i] = v.get(i);
//			}
//			double med = median.evaluate(v_d);
//
//			List<Double> summary = new ArrayList<>();
//			summary.add(avg);
//			summary.add(med);
//			summary.add(max);
//			summary.add(min);
//			summaryAll.add(summary);
//
//			System.out.printf("%10s    ", df.format(avg));
//			out.append(String.format("%10s    ", df.format(avg)));
//
//			System.out.printf("%10s    ", df.format(med));
//			out.append(String.format("%10s    ", df.format(med)));
//
//			System.out.printf("%10s    ", df.format(max));
//			out.append(String.format("%10s    ", df.format(max)));
//
//			System.out.printf("%10s \n", df.format(min));
//			out.append(String.format("%10s \n", df.format(min)));
//
//		});
//
//		out.append("\n----------------- Absolute DAG Speedup of All Systems -----------------");
//		System.out.println("\n----------------- Absolute DAG Speedup For All Systems -----------------");
//
//		out.append(String.format("     %10s    %10s    %10s    %10s \n", "avg", "med", "max", "min"));
//		System.out.printf("     %10s    %10s    %10s    %10s \n", "avg", "med", "max", "min");
//
//		List<List<Double>> summartAllHtoV = new ArrayList<>();
//
//		for (int j = 0; j < summaryAll.get(0).size(); j++) {
//			List<Double> summary = new ArrayList<>();
//			for (int i = 0; i < summaryAll.size(); i++) {
//				summary.add(summaryAll.get(i).get(j));
//			}
//			summartAllHtoV.add(summary);
//		}
//
//		summartAllHtoV.forEach(v -> {
//
//			Median median = new Median();
//			double[] v_d = new double[v.size()];
//			for (int i = 0; i < v.size(); i++) {
//				v_d[i] = v.get(i);
//			}
//
//			double avg = v.stream().mapToDouble(c1 -> c1).sum() / (double) v.size();
//			double med = median.evaluate(v_d);
//			double max = v.stream().mapToDouble(c1 -> c1).max().getAsDouble();
//			double min = v.stream().mapToDouble(c1 -> c1).min().getAsDouble();
//
//			int count = summartAllHtoV.indexOf(v);
//
//			switch (count) {
//			case 0:
//				System.out.print("AVGs ");
//				out.append("AVGs ");
//				break;
//			case 1:
//				System.out.print("MEDs ");
//				out.append("MEDs ");
//				break;
//			case 2:
//				System.out.print("MAXs ");
//				out.append("MAXs ");
//				break;
//			case 3:
//				System.out.print("MINs ");
//				out.append("MINs ");
//				break;
//			default:
//				break;
//			}
//
//			System.out.printf("%10s    ", df.format(avg));
//			out.append(String.format("%10s    ", df.format(avg)));
//
//			System.out.printf("%10s    ", df.format(med));
//			out.append(String.format("%10s    ", df.format(med)));
//
//			System.out.printf("%10s    ", df.format(max));
//			out.append(String.format("%10s    ", df.format(max)));
//
//			System.out.printf("%10s \n", df.format(min));
//			out.append(String.format("%10s \n", df.format(min)));
//
//		});
//
//		System.out.println("\n\n Speed up of each system:");
//		out.append("\n\n Speed up of each system:");
//
//		List<Double> avgSpeedUp = new ArrayList<>();
//		res.forEach(c -> {
//			String[] method1_temp = c.get(0).split(" ");
//			String[] method2_temp = c.get(1).split(" ");
//
//			List<Long> m1 = new ArrayList<>();
//			List<Long> m2 = new ArrayList<>();
//
//			for (int i = 0; i < method1_temp.length; i++) {
//				if (method1_temp[i].length() > 0) {
//					m1.add(Long.parseLong(method1_temp[i]));
//				}
//			}
//
//			for (int i = 0; i < method2_temp.length; i++) {
//				if (method2_temp[i].length() > 0) {
//					m2.add(Long.parseLong(method2_temp[i]));
//				}
//			}
//
//			if (m1.size() != m2.size()) {
//				System.out.println("!!!");
//				System.exit(-1);
//			}
//
//			double totalPrec = 0;
//			if (type == 0) {
//				long total1 = m1.stream().mapToLong(v -> v).sum();
//				long total2 = m2.stream().mapToLong(v -> v).sum();
//				totalPrec = ((double) (total1 - total2)) / (double) total1;
//			}
//
//			if (type == 1) {
//				long makespan1 = m1.stream().mapToLong(v -> v).max().getAsLong();
//				long makespan2 = m2.stream().mapToLong(v -> v).max().getAsLong();
//				totalPrec = ((double) (makespan1 - makespan2)) / (double) makespan1;
//			}
//
//			avgSpeedUp.add(totalPrec);
//
//			System.out.printf("%10s\n", df.format(totalPrec));
//			out.append(String.format("%10s\n", df.format(totalPrec)));
//
//		});
//
//		int betterCase = 0;
//		int equalCase = 0;
//		for (Double d : avgSpeedUp) {
//			if (d >= 0)
//				betterCase++;
//			if (d == 0)
//				equalCase++;
//		}
//
//		System.out.println("Number of better cases for method 2: " + betterCase);
//		out.append("Number of better cases for method 2: " + betterCase);
//
//		System.out.println("Number of equal cases: " + equalCase);
//		out.append("Number of equal cases: " + equalCase);
//
//		System.out.println(
//				"\n********************************** " + expName + " **********************************\n\n\n");
//
//		writeResult("resultCA/" + expName + ".txt", out.toString());
//	}
//
//	public static void writeResult(String filename, String result) {
//		PrintWriter writer = null;
//		try {
//			writer = new PrintWriter(new FileWriter(new File(filename), false));
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		writer.println(result);
//		writer.close();
//	}
//}
