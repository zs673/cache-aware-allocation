package uk.ac.york.mocha.simulator.errorResults;

import java.text.DecimalFormat;
import java.util.List;

import uk.ac.york.mocha.simulator.dag.DirectedAcyclicGraph;
import uk.ac.york.mocha.simulator.generator.SystemGenerator;
import uk.ac.york.mocha.simulator.parameters.SystemParameters;
import uk.ac.york.mocha.simulator.parameters.SystemParameters.Allocation;
import uk.ac.york.mocha.simulator.parameters.SystemParameters.ExpName;
import uk.ac.york.mocha.simulator.parameters.SystemParameters.Hardware;
import uk.ac.york.mocha.simulator.parameters.SystemParameters.RecencyType;
import uk.ac.york.mocha.simulator.parameters.SystemParameters.SimuType;
import uk.ac.york.mocha.simulator.simulator.Simualtor;
import uk.ac.york.mocha.simulator.simulator.SimualtorNWC;

public class Test {

	static DecimalFormat df = new DecimalFormat("#.###");

	public static void main(String args[]) {
		oneTaskWithFaults();
	}

	public static void oneTaskWithFaults() {

		int intanceNum = 3;
		int hyperPeriodNum = -1;
		int seed = 1000;

		SystemParameters.fault_rate = 100;
		SystemParameters.fault_median = 50;
		SystemParameters.fault_range = SystemParameters.fault_median * 2;

		SystemParameters.utilPerTask = Double.parseDouble(df.format((double) 16 / (double) 10));

		RunOneGroupThreeMethod(1, intanceNum, hyperPeriodNum, true, null, seed, seed, null, SystemParameters.NoS, true,
				ExpName.recency_fault_util);

	}

	public static void RunOneGroupThreeMethod(int taskNum, int intanceNum, int hyperperiodNum, boolean takeAllUtil,
			List<List<Double>> util, int taskSeed, int tableSeed, List<List<Long>> periods, int NoS, boolean randomC,
			ExpName name) {

		taskSeed = 1000;

		SystemGenerator gen = new SystemGenerator(SystemParameters.coreNum, taskNum, true, takeAllUtil, null, taskSeed,
				randomC, SystemParameters.printGen);
		List<DirectedAcyclicGraph> dags = gen.generatedDAGInstancesInOneHP(intanceNum, hyperperiodNum, null, false);

		Simualtor cacheWFSim = new Simualtor(SimuType.CLOCK_LEVEL, Hardware.PROC_CACHE, Allocation.CACHE_AWARE,
				RecencyType.TIME_DEFAULT, dags, 8, tableSeed, false, true);
		cacheWFSim.simulate(true, 0);
		
		System.out.println("\n\n**********************************************************\n\n");

		SimualtorNWC cacheCASim = new SimualtorNWC(SimuType.CLOCK_LEVEL, Hardware.PROC_CACHE,
				Allocation.CACHE_AWARE_ROBUST, RecencyType.TIME_DEFAULT, dags, 8, tableSeed, false, true);
		cacheCASim.simulate(true, 0);
	}

}
