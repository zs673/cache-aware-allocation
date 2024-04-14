package uk.ac.york.mocha.simulator.experiments_CARVB;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.math3.util.Pair;

import uk.ac.york.mocha.simulator.entity.DirectedAcyclicGraph;
import uk.ac.york.mocha.simulator.entity.Node;
import uk.ac.york.mocha.simulator.generator.CacheHierarchy;
import uk.ac.york.mocha.simulator.generator.SystemGenerator;
import uk.ac.york.mocha.simulator.parameters.SystemParameters;
import uk.ac.york.mocha.simulator.parameters.SystemParameters.Allocation;
import uk.ac.york.mocha.simulator.parameters.SystemParameters.ExpName;
import uk.ac.york.mocha.simulator.parameters.SystemParameters.Hardware;
import uk.ac.york.mocha.simulator.parameters.SystemParameters.RecencyType;
import uk.ac.york.mocha.simulator.parameters.SystemParameters.SimuType;
import uk.ac.york.mocha.simulator.resultAnalyzer.AllSystemsResults;
import uk.ac.york.mocha.simulator.resultAnalyzer.OneSystemResults;
import uk.ac.york.mocha.simulator.simulator.Simualtor;
import uk.ac.york.mocha.simulator.simulator.SimualtorNWC;
import uk.ac.york.mocha.simulator.simulator.SimualtorGYY;
import uk.ac.york.mocha.simulator.simulator.SimualtorHEFT;
import uk.ac.york.mocha.simulator.simulator.SimualtorYHX;
import uk.ac.york.mocha.simulator.simulator.Utils;

import java.io.*;

public class CARVB_General_YHX {

	static DecimalFormat df = new DecimalFormat("#.###");

	// static int cores = 8;// 4
	// static int nos = 1;// 500/100   //1000次重复实验
	// static int intanceNum = 10;// 100
	// static int taskNum = 1;// 100
	static int cores = 8;// 4
	static int nos = 1;// 500/100   //1000次重复实验
	static int intanceNum = 10;// 100
	static int taskNum = 1;// 100

	static int startUtil = 4;
	static int incrementUtil = 4;
	static int endUtil = 4000;

	static boolean print = false;

	static List<Double> speeds;

	public static void main(String args[]) {
		oneTaskWithFaults();
	}

	public static void oneTaskWithFaults() {
		int hyperPeriodNum = -1;
		int seed = 1000;

        int[] instanceNo = new int[taskNum];
        for (int j = 0; j < instanceNo.length; j++)
            instanceNo[j] = intanceNum;
        List<OneSystemResults> allRes = new ArrayList<>();
		for (int i = startUtil; i <= endUtil; i = i + incrementUtil) {
			SystemParameters.utilPerTask = Double.parseDouble(df.format((double) i / (double) 1000));
			OneSystemResults res = RunOneGroup(taskNum, intanceNum, hyperPeriodNum, true, null, seed, seed, null, nos, true, ExpName.predict);
            allRes.add(res);//每个res都是OneSystemResult类
		}
        new AllSystemsResults(allRes, instanceNo, cores, taskNum, ExpName.predict);
	}

	static boolean bigger = false;

	public static OneSystemResults RunOneGroup(int taskNum, int intanceNum, int hyperperiodNum, boolean takeAllUtil,
			List<List<Double>> util, int taskSeed, int tableSeed, List<List<Long>> periods, int NoS, boolean randomC,
			ExpName name) {
		
		// record the instanceNum of task
		int[] instanceNo = new int[taskNum];

		if (periods != null && hyperperiodNum > 0) {
			long totalHP = Utils.getHyperPeriod(periods.get(0)) * hyperperiodNum;

			for (int i = 0; i < periods.size(); i++) {//感觉写错了
				int insNo = (int) (totalHP / periods.get(0).get(i));
				instanceNo[i] = insNo > intanceNum ? insNo : intanceNum;
			}
		} else if (intanceNum > 0) {
			for (int i = 0; i < instanceNo.length; i++)
				instanceNo[i] = intanceNum;
		} else {
			System.out.println("Cannot get same instances number for randomly generated periods.");
		}

        int i = 0;
        System.out.println(
                "Util per task: " + SystemParameters.utilPerTask + " --- Current system number: " + (i + 1));

        SystemGenerator gen = new SystemGenerator(cores, taskNum, true, true, null, taskSeed + i, true, print);
        // Pair<List<DirectedAcyclicGraph>, CacheHierarchy> sys = gen.generatedDAGInstancesInOneHP(intanceNum, -1,
        // 		null, false);//sys: DAG instances + cache
        ArrayList<Long> period = new ArrayList<Long>();
        period.add((long)144000);
        Pair<List<DirectedAcyclicGraph>, CacheHierarchy> sys = gen.generatedDAGInstancesInOneHP(intanceNum, -1,
                period, false);//sys: DAG instances + cache
        speeds = gen.generateCoresSpeed(cores, true);

        OneSystemResults res = null;
        res = testOneCaseThreeMethod(sys, taskNum, instanceNo, cores, taskSeed, tableSeed, i);

        
        taskSeed++;
		
		return res;
	}

	/**
	 * This test case will generate two fixed DAG structure.
	 */
	public static OneSystemResults testOneCaseThreeMethod(Pair<List<DirectedAcyclicGraph>, CacheHierarchy> sys,
			int tasks, int[] NoInstances, int cores, int taskSeed, int tableSeed, int not) {

		boolean lcif = true;

		//get the features for each node for each DAG 1******
		// String python_file = "data_process/mlp.py";
		// ArrayList<ArrayList<Double>> features = new ArrayList<>();
		// for (DirectedAcyclicGraph d : sys.getFirst()) {
		// 	for (Node n : d.getFlatNodes()) {
		// 		ArrayList<Double> list = new ArrayList<Double>();
		// 		for (int k = 0; k < n.weights.length; k++) {
		// 			list.add(n.weights[k]);
		// 		}
		// 		features.add(list);
		// 	}

		// }

		// // write the feature matrixs into a mid-file
		// File file = new File("data_process/midway/array.txt");
		// BufferedWriter writer;
		// try {
		// 	writer = new BufferedWriter(new FileWriter(file));
		// 	for (int i = 0; i < features.size(); i++) {
		// 		for (int j = 0; j < 10; j++) {
		// 			writer.write(features.get(i).get(j) + " ");
		// 		}
		// 		writer.newLine();
		// 	}
		// 	writer.close();
		// } catch (IOException e) {
		// 	// TODO Auto-generated catch block
		// 	e.printStackTrace();
		// }

		// // process python to deal with the data
		// ProcessBuilder pb = new ProcessBuilder("python", python_file);
		// Process p;
		// try {
		// 	p = pb.start();
		// 	InputStream is = p.getInputStream();
		// 	Scanner scanner = new Scanner(is);
		// 	String output = scanner.nextLine();
		// 	ArrayList<Double> myOutputList = new ArrayList<>();
		// 	String[] outputArray = output.replaceAll("[\\[\\]]", "").split(", ");
		// 	for (String s : outputArray) {
		// 		myOutputList.add(Double.parseDouble(s));
		// 	}
		// 	scanner.close();

		// 	// System.out.println(myOutputList[0].size());
		// 	// add the sensitivity to each node
		// 	int feature_id = 0;
		// 	for (DirectedAcyclicGraph d : sys.getFirst()) {
		// 		for (Node n : d.getFlatNodes()) {
		// 			n.sensitivity = myOutputList.get(feature_id++);
		// 			// n.gyy_priority = (1 + n.sensitivity) * n.gyy_priority;// edit here
		// 			n.gyy_priority = (1 + n.sensitivity) * 2.3 * n.WCET + n.gyy_priority - n.WCET;// 1.5
		// 		}
		// 	}
		// } catch (IOException e) {
		// 	// TODO Auto-generated catch block
		// 	e.printStackTrace();
		// }

		// SimualtorYHX sim1 = new SimualtorYHX(SimuType.CLOCK_LEVEL,
		// Hardware.PROC_CACHE, Allocation.ONLINE_YHX_TEST, // PROC_CACHE
		// RecencyType.TIME_DEFAULT, sys.getFirst(), sys.getSecond(), cores, tableSeed,
		// lcif);
		// Pair<List<DirectedAcyclicGraph>, double[]> pair1 = sim1.simulate(print);//运行完的dags + 缓存命中

		// WFD  worst-fit allocation
		// Simualtor sim0 = new Simualtor(SimuType.CLOCK_LEVEL, Hardware.PROC_CACHE,
		// Allocation.WORST_FIT_OUR, // Hardware.PROC
		// RecencyType.TIME_DEFAULT, sys.getFirst(), sys.getSecond(), cores, tableSeed,
		// lcif, speeds);
		// Pair<List<DirectedAcyclicGraph>, double[]> pair0 = sim0.simulate(print);

		Simualtor sim0 = new Simualtor(SimuType.CLOCK_LEVEL, Hardware.PROC_CACHE,
		Allocation.WORST_FIT_OUR, // Hardware.PROC
		RecencyType.TIME_DEFAULT, sys.getFirst(), sys.getSecond(), cores, tableSeed,
		lcif, speeds);
		Pair<List<DirectedAcyclicGraph>, double[]> pair0 = sim0.simulate(print);

		// SimualtorNWC sim2 = new SimualtorNWC(SimuType.CLOCK_LEVEL,
		// Hardware.PROC_CACHE, Allocation.CACHE_AWARE_NEW, // PROC_CACHE
		// RecencyType.TIME_DEFAULT, sys.getFirst(), sys.getSecond(), cores, tableSeed,
		// lcif);
		// Pair<List<DirectedAcyclicGraph>, double[]> pair2 = sim2.simulate(print);//运行完的dags + 缓存命中
		
		SimualtorNWC sim1 = new SimualtorNWC(SimuType.CLOCK_LEVEL,
		Hardware.PROC_CACHE, Allocation.CACHE_AWARE_NEW, // PROC_CACHE
		RecencyType.TIME_DEFAULT, sys.getFirst(), sys.getSecond(), cores, tableSeed,
		lcif);
		Pair<List<DirectedAcyclicGraph>, double[]> pair1 = sim1.simulate(print);//运行完的dags + 缓存命中

		SimualtorYHX sim2 = new SimualtorYHX(SimuType.CLOCK_LEVEL, Hardware.PROC_CACHE,
		Allocation.ONLINE_YHX_Compare, // Hardware.PROC
		RecencyType.TIME_DEFAULT, sys.getFirst(), sys.getSecond(), cores, tableSeed,
		lcif);
		Pair<List<DirectedAcyclicGraph>, double[]> pair2 = sim2.simulate(print);


		// SimualtorNWC sim1 = new SimualtorNWC(SimuType.CLOCK_LEVEL,
		// Hardware.PROC_CACHE, Allocation.CACHE_AWARE_NEW, // PROC_CACHE
		// RecencyType.TIME_DEFAULT, sys.getFirst(), sys.getSecond(), cores, tableSeed,
		// lcif);
		// Pair<List<DirectedAcyclicGraph>, double[]> pair1 = sim1.simulate(print);//运行完的dags + 缓存命中



		// 1
		// for (DirectedAcyclicGraph d : sys.getFirst()) {
		// 	for (Node n : d.getFlatNodes()) {
		// 		n.sensitivity = 0;
		// 		for (int k = 0; k < n.weights.length; k++) {
		// 			n.sensitivity += n.weights[k];
		// 		}
		// 	}
		// }
		
		// SimualtorNWC cacheCASim = new SimualtorNWC(SimuType.CLOCK_LEVEL,
		// Hardware.PROC_CACHE,
		// Allocation.CACHE_AWARE_PREDICT_R, RecencyType.TIME_DEFAULT,
		// sys.getFirst(), sys.getSecond(), cores,
		// tableSeed, lcif);
		// Pair<List<DirectedAcyclicGraph>, double[]> pair2 = cacheCASim.simulate(print);

		//1
		// Simualtor cacheCASim0 = new Simualtor(SimuType.CLOCK_LEVEL,
		// 		Hardware.PROC, Allocation.WORST_FIT_OUR, // PROC_CACHE
		// 		RecencyType.TIME_DEFAULT, sys.getFirst(), sys.getSecond(), cores, tableSeed,
		// 		false, speeds);
		// Pair<List<DirectedAcyclicGraph>, double[]> pair0 = cacheCASim0.simulate(print);

		// HSF+new
		/*
		 * SimualtorGYY cacheCASim2 = new SimualtorGYY(SimuType.CLOCK_LEVEL,
		 * Hardware.PROC, Allocation.GYY_old, // Hardware.PROC_CACHE
		 * RecencyType.TIME_DEFAULT, sys.getFirst(), sys.getSecond(), cores, tableSeed,
		 * false, speeds);
		 * Pair<List<DirectedAcyclicGraph>, double[]> pair2 =
		 * cacheCASim2.simulate(print);
		 */

		// sen+old 1
		// SimualtorGYY sim1 = new SimualtorGYY(SimuType.CLOCK_LEVEL, Hardware.PROC,
		// 		Allocation.HSF, // Hardware.PROC
		// 		RecencyType.TIME_DEFAULT, sys.getFirst(), sys.getSecond(), cores, tableSeed,
		// 		false, speeds);
		// Pair<List<DirectedAcyclicGraph>, double[]> pair1 = sim1.simulate(print);

		// GYY-WCET
		/*
		 * SimualtorGYY cacheCASim4 = new SimualtorGYY(SimuType.CLOCK_LEVEL,
		 * Hardware.PROC, Allocation.GYY_WCET, // Hardware.PROC_CACHE
		 * RecencyType.TIME_DEFAULT, sys.getFirst(), sys.getSecond(), cores, tableSeed,
		 * false, speeds);
		 * Pair<List<DirectedAcyclicGraph>, double[]> pair4 =
		 * cacheCASim4.simulate(print);
		 */

		// SOTA sen+new 1
		// SimualtorGYY cacheCASim2 = new SimualtorGYY(SimuType.CLOCK_LEVEL,
		// 		Hardware.PROC, Allocation.GYY, // Hardware.PROC_CACHE
		// 		RecencyType.TIME_DEFAULT, sys.getFirst(), sys.getSecond(), cores, tableSeed,
		// 		false, speeds);
		// Pair<List<DirectedAcyclicGraph>, double[]> pair2 = cacheCASim2.simulate(print);

		// HEFT rank+old 1
		// SimualtorGYY cacheCASim3 = new SimualtorGYY(SimuType.CLOCK_LEVEL,
		// 		Hardware.PROC, Allocation.HEFT, // Hardware.PROC_CACHE
		// 		RecencyType.TIME_DEFAULT, sys.getFirst(), sys.getSecond(), cores, tableSeed,
		// 		false, speeds);
		// Pair<List<DirectedAcyclicGraph>, double[]> pair3 = cacheCASim3.simulate(print);

		// rank+new 1
		// SimualtorGYY cacheCASim4 = new SimualtorGYY(SimuType.CLOCK_LEVEL,
		// 		Hardware.PROC, Allocation.HEFT_NEW, // Hardware.PROC_CACHE
		// 		RecencyType.TIME_DEFAULT, sys.getFirst(), sys.getSecond(), cores, tableSeed,
		// 		false, speeds);
		// Pair<List<DirectedAcyclicGraph>, double[]> pair4 = cacheCASim4.simulate(print);

		// CPOP 1
		// SimualtorGYY cacheCASim5 = new SimualtorGYY(SimuType.CLOCK_LEVEL,
		// 		Hardware.PROC, Allocation.CPOP, // Hardware.PROC_CACHE
		// 		RecencyType.TIME_DEFAULT, sys.getFirst(), sys.getSecond(), cores, tableSeed,
		// 		false, speeds);
		// Pair<List<DirectedAcyclicGraph>, double[]> pair5 = cacheCASim5.simulate(print);

		// New + old 1
		// SimualtorGYY cacheCASim6 = new SimualtorGYY(SimuType.CLOCK_LEVEL,
		// 		Hardware.PROC, Allocation.sota_with_old, // Hardware.PROC_CACHE
		// 		RecencyType.TIME_DEFAULT, sys.getFirst(), sys.getSecond(), cores, tableSeed,
		// 		false, speeds);
		// Pair<List<DirectedAcyclicGraph>, double[]> pair6 = cacheCASim6.simulate(print);

		// New+New 1
		// SimualtorGYY cacheCASim7 = new SimualtorGYY(SimuType.CLOCK_LEVEL,
		// 		Hardware.PROC, Allocation.gyy_test, // Hardware.PROC_CACHE
		// 		RecencyType.TIME_DEFAULT, sys.getFirst(), sys.getSecond(), cores, tableSeed,
		// 		false, speeds);
		// Pair<List<DirectedAcyclicGraph>, double[]> pair7 = cacheCASim7.simulate(print);

		// get_new_metric(pair0);
		// get_new_metric(pair1);
		// get_new_metric(pair2);
		// get_new_metric(pair3);
		// get_new_metric(pair4);
		// get_new_metric(pair5);
		// get_new_metric(pair6);
		// get_new_metric(pair7);
		List<DirectedAcyclicGraph> m0 = pair0.getFirst();
		List<DirectedAcyclicGraph> m1 = pair1.getFirst();
		List<DirectedAcyclicGraph> m2 = pair2.getFirst();
		// List<DirectedAcyclicGraph> m3 = pair3.getFirst();
		// List<DirectedAcyclicGraph> m4 = pair4.getFirst();
		// List<DirectedAcyclicGraph> m5 = pair5.getFirst();
		// List<DirectedAcyclicGraph> m6 = pair6.getFirst();
		// List<DirectedAcyclicGraph> m7 = pair7.getFirst();

		/*
		 * find the example for paper
		 * if (m3.get(0).finishTime > m4.get(0).finishTime) {
		 * int yes = 0;
		 * }
		 */

		List<List<DirectedAcyclicGraph>> allMethods = new ArrayList<>();

		List<DirectedAcyclicGraph> method0 = new ArrayList<>();
		List<DirectedAcyclicGraph> method1 = new ArrayList<>();
		List<DirectedAcyclicGraph> method2 = new ArrayList<>();
		List<DirectedAcyclicGraph> method3 = new ArrayList<>();
		List<DirectedAcyclicGraph> method4 = new ArrayList<>();
		List<DirectedAcyclicGraph> method5 = new ArrayList<>();
		List<DirectedAcyclicGraph> method6 = new ArrayList<>();
		List<DirectedAcyclicGraph> method7 = new ArrayList<>();

		List<DirectedAcyclicGraph> dags = sys.getFirst();

		/*
		 * get a number of instances from each DAG based on long[] NoInstances.  
		 * 如果不设置forceInstanceNum，每个DAG周期不同，hyperperiod下instance数也不同
		 */
		int count = 0;
		int currentID = -1;
		for (int i = 0; i < dags.size(); i++) {
			if (currentID != dags.get(i).id) {

				currentID = dags.get(i).id;
				count = 0;
			}

			if (count < NoInstances[dags.get(i).id]) {
				method0.add(m0.get(i));
				method1.add(m1.get(i));
				method2.add(m2.get(i));
				// method3.add(m3.get(i));
				// method4.add(m4.get(i));
				// method5.add(m5.get(i));
				// method6.add(m6.get(i));
				// method7.add(m7.get(i));
				count++;
			}
		}

		allMethods.add(method0);
		allMethods.add(method1);
		allMethods.add(method2);
		// allMethods.add(method3);
		// allMethods.add(method4);
		// allMethods.add(method5);
		// allMethods.add(method6);
		// allMethods.add(method7);

		List<double[]> cachePerformance = new ArrayList<>();
		cachePerformance.add(pair0.getSecond());
		cachePerformance.add(pair1.getSecond());
		cachePerformance.add(pair2.getSecond());
		// cachePerformance.add(pair3.getSecond());
		// cachePerformance.add(pair4.getSecond());
		// cachePerformance.add(pair5.getSecond());
		// cachePerformance.add(pair6.getSecond());
		// cachePerformance.add(pair7.getSecond());

		OneSystemResults result = new OneSystemResults(allMethods, cachePerformance);

		return result;
	}

	/*
	 * compute SLR and speedup
	 */
	public static void get_new_metric(Pair<List<DirectedAcyclicGraph>, double[]> p) {
		List<DirectedAcyclicGraph> L = p.getFirst();
		double fast = speeds.get(0);
		for (int i = 1; i < speeds.size(); i++) {
			fast = Math.max(fast, speeds.get(i));
		}
		for (DirectedAcyclicGraph d : L) {
			double culm = 0.0, crit = 0.0;
			for (Node n : d.getFlatNodes()) {
				culm += n.WCET / fast;
				if (n.isCritical) {
					crit += n.WCET / fast;
				}
			}
			d.SLR = (d.finishTime - d.startTime) / crit;
			d.speedup = culm / (d.finishTime - d.startTime);
		}
	}
}
