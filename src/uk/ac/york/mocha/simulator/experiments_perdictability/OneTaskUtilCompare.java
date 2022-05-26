package uk.ac.york.mocha.simulator.experiments_perdictability;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
import uk.ac.york.mocha.simulator.simulator.SimualtorNWC;
import uk.ac.york.mocha.simulator.simulator.Utils;

public class OneTaskUtilCompare {

	static DecimalFormat df = new DecimalFormat("#.###");

	static int cores = 8;

	public static void main(String args[]) {
		oneTaskWithFaults();
	}

	public static void oneTaskWithFaults() {
		int intanceNum = 10;
		int hyperPeriodNum = -1;
		int seed = 1000;

		SystemParameters.utilPerTask = Double.parseDouble(df.format((double) 20 / (double) 10));

		List<Pair<List<DirectedAcyclicGraph>, CacheHierarchy>> allSys = new ArrayList<>();

		for (int i = 0; i < 1000; i++) {
			SystemGenerator gen = new SystemGenerator(cores, 1, true, true, null, 1000 + i, true,
					SystemParameters.printGen);
			Pair<List<DirectedAcyclicGraph>, CacheHierarchy> sys = gen.generatedDAGInstancesInOneHP(intanceNum,
					hyperPeriodNum, null, false);
			allSys.add(sys);
		}

		for (int i = 8; i <= 40; i = i + 8) {
			SystemParameters.utilPerTask = Double.parseDouble(df.format((double) i / (double) 10));
			RunOneGroup(1, intanceNum, hyperPeriodNum, true, null, seed, seed, null, 1000, true, ExpName.predict,
					allSys);
		}

	}

	public static void RunOneGroup(int taskNum, int intanceNum, int hyperperiodNum, boolean takeAllUtil,
			List<List<Double>> util, int taskSeed, int tableSeed, List<List<Long>> periods, int NoS, boolean randomC,
			ExpName name, List<Pair<List<DirectedAcyclicGraph>, CacheHierarchy>> allsystems) {

		List<OneSystemResults> allSys = new ArrayList<>();

		int[] instanceNo = new int[taskNum];

		if (periods != null && hyperperiodNum > 0) {
			long totalHP = Utils.getHyperPeriod(periods.get(0)) * hyperperiodNum;

			for (int i = 0; i < periods.size(); i++) {
				int insNo = (int) (totalHP / periods.get(0).get(i));
				instanceNo[i] = insNo > intanceNum ? insNo : intanceNum;
			}
		} else if (intanceNum > 0) {
			for (int i = 0; i < instanceNo.length; i++)
				instanceNo[i] = intanceNum;
		} else {
			System.out.println("Cannot get same instances number for randomly generated periods.");
		}

		taskSeed = 1000;
		for (int i = 0; i < NoS; i++) {
			System.out.println(
					"\n\n****************************************************************************************************");
			System.out.println(
					"Util per task: " + SystemParameters.utilPerTask + " --- Current system number: " + (i + 1));

			List<DirectedAcyclicGraph> dags = allsystems.get(i).getFirst();

			for (DirectedAcyclicGraph d : dags) {
				d.getSchedParameters().setWCET((long) Math
						.ceil((double) d.getSchedParameters().getPeriod() * (double) SystemParameters.utilPerTask));

				SystemGenerator.generateWCETs(dags, new Random(1000), true, false);
			}

			OneSystemResults res = null;
			res = testOneCaseThreeMethod(allsystems.get(i), taskNum, instanceNo, cores, taskSeed, tableSeed, i);
			allSys.add(res);

			taskSeed++;
		}

		new AllSystemsResults(allSys, instanceNo, cores, taskNum, name);
	}

	/**
	 * This test case will generate two fixed DAG structure.
	 */
	public static OneSystemResults testOneCaseThreeMethod(Pair<List<DirectedAcyclicGraph>, CacheHierarchy> sys,
			int tasks, int[] NoInstances, int cores, int taskSeed, int tableSeed, int not) {

		boolean lcif = false;

		SimualtorNWC cacheWFSim = new SimualtorNWC(SimuType.CLOCK_LEVEL, Hardware.PROC_CACHE,
				Allocation.CACHE_AWARE_NEW, RecencyType.TIME_DEFAULT, sys.getFirst(), sys.getSecond(), cores, tableSeed,
				lcif);
		Pair<List<DirectedAcyclicGraph>, double[]> pair1 = cacheWFSim.simulate(SystemParameters.printSim);

		for (DirectedAcyclicGraph d : sys.getFirst()) {
			for (Node n : d.getFlatNodes()) {
				n.sensitivity = 0;
			}
		}

		SimualtorNWC cacheCASim = new SimualtorNWC(SimuType.CLOCK_LEVEL, Hardware.PROC_CACHE,
				Allocation.CACHE_AWARE_PREDICT_R, RecencyType.TIME_DEFAULT, sys.getFirst(), sys.getSecond(), cores,
				tableSeed, lcif);
		Pair<List<DirectedAcyclicGraph>, double[]> pair2 = cacheCASim.simulate(SystemParameters.printSim);

		for (DirectedAcyclicGraph d : sys.getFirst()) {
			for (Node n : d.getFlatNodes()) {
//				n.sensitivity = SystemParameters.d_norPathET * n.gmpETNorm + SystemParameters.d_norPathNum * n.gmpNumNorm
//						+ SystemParameters.d_norNodeET * n.nodeETNorm
//						+ SystemParameters.d_norInOutDegree * n.nodeInOutDegreeNorm;
			}
		}

		SimualtorNWC cacheCASim3 = new SimualtorNWC(SimuType.CLOCK_LEVEL, Hardware.PROC_CACHE,
				Allocation.CACHE_AWARE_PREDICT_R, RecencyType.TIME_DEFAULT, sys.getFirst(), sys.getSecond(), cores,
				tableSeed, lcif);
		Pair<List<DirectedAcyclicGraph>, double[]> pair3 = cacheCASim3.simulate(SystemParameters.printSim);

		double sumWeights = 0;
		for (DirectedAcyclicGraph d : sys.getFirst()) {
			for (Node n : d.getFlatNodes()) {
//				n.sensitivity = SystemParameters.norNodeET / (SystemParameters.norNodeET + SystemParameters.norPathET)
//						* n.nodeETNorm
//						+ SystemParameters.norPathET / (SystemParameters.norNodeET + SystemParameters.norPathET)
//								* n.gmpETNorm;
			}
		}
		SimualtorNWC cacheCASim4 = new SimualtorNWC(SimuType.CLOCK_LEVEL, Hardware.PROC_CACHE,
				Allocation.CACHE_AWARE_PREDICT_R, RecencyType.TIME_DEFAULT, sys.getFirst(), sys.getSecond(), cores,
				tableSeed, lcif);
		Pair<List<DirectedAcyclicGraph>, double[]> pair4 = cacheCASim4.simulate(SystemParameters.printSim);

		List<DirectedAcyclicGraph> m1 = pair1.getFirst();
		List<DirectedAcyclicGraph> m2 = pair2.getFirst();
		List<DirectedAcyclicGraph> m3 = pair3.getFirst();
		List<DirectedAcyclicGraph> m4 = pair4.getFirst();

		List<List<DirectedAcyclicGraph>> allMethods = new ArrayList<>();

		List<DirectedAcyclicGraph> method1 = new ArrayList<>();
		List<DirectedAcyclicGraph> method2 = new ArrayList<>();
		List<DirectedAcyclicGraph> method3 = new ArrayList<>();
		List<DirectedAcyclicGraph> method4 = new ArrayList<>();

		List<DirectedAcyclicGraph> dags = sys.getFirst();
		/*
		 * get a number of instances from each DAG based on long[] NoInstances.
		 */
		int count = 0;
		int currentID = -1;
		for (int i = 0; i < dags.size(); i++) {
			if (currentID != dags.get(i).id) {

				currentID = dags.get(i).id;
				count = 0;
			}

			if (count < NoInstances[dags.get(i).id]) {
				method1.add(m1.get(i));
				method2.add(m2.get(i));
				method3.add(m3.get(i));
				method4.add(m4.get(i));
				count++;
			}
		}

		allMethods.add(method1);
		allMethods.add(method2);
		allMethods.add(method3);
		allMethods.add(method4);

		List<double[]> cachePerformance = new ArrayList<>();
		cachePerformance.add(pair1.getSecond());
		cachePerformance.add(pair2.getSecond());
		cachePerformance.add(pair3.getSecond());
		cachePerformance.add(pair4.getSecond());

		OneSystemResults result = new OneSystemResults(allMethods, cachePerformance);

		return result;
	}
}
