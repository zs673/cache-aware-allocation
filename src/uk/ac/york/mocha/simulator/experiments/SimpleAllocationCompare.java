package uk.ac.york.mocha.simulator.experiments;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import uk.ac.york.mocha.simulator.dag.DirectedAcyclicGraph;
import uk.ac.york.mocha.simulator.generator.SystemGenerator;
import uk.ac.york.mocha.simulator.simulator.Simualtor;
import uk.ac.york.mocha.simulator.simulator.Simualtor.Allocation;
import uk.ac.york.mocha.simulator.simulator.Simualtor.Hardware;
import uk.ac.york.mocha.simulator.simulator.Simualtor.SimuType;

public class SimpleAllocationCompare {

	public static void main(String args[]) {
		testOneCase();
	}

	/**
	 * This test case will generate two fixed DAG strcuture.
	 */
	public static void testOneCase() {
		int cores = 8;
		boolean printSim = false;

		SystemGenerator gen = new SystemGenerator(100, 1000, cores, 2, true, 1000);
		List<DirectedAcyclicGraph> dags = gen.generatedDAGInstancesInOneHP(-1);

		Simualtor procOnlySim = new Simualtor(SimuType.CLOCK_LEVEL, Hardware.PROC_ONLY, Allocation.LOAD_BALANCE, dags,
				cores, 1000);
		List<Long> procOnlySim_res = procOnlySim.simulate(printSim);

		Simualtor cacheLBSim = new Simualtor(SimuType.CLOCK_LEVEL, Hardware.PROC_CACHE, Allocation.LOAD_BALANCE, dags,
				cores, 1000);
		List<Long> cacheLBSim_res = cacheLBSim.simulate(printSim);

		Simualtor cacheCASim = new Simualtor(SimuType.CLOCK_LEVEL, Hardware.PROC_CACHE, Allocation.CACHE_AWARE, dags,
				cores, 1000);
		List<Long> cacheCASim_res = cacheCASim.simulate(printSim);

		String out = "";
		System.out.println(
				"\n\n\n************************************ Simualtion Summary *************************************");
		out += Arrays.toString(dags.stream().map(c -> c.id + "_" + c.instanceNo).collect(Collectors.toList()).toArray())
				+ "\n";
		System.out.println(out);
		out += Arrays.toString(procOnlySim_res.toArray());
		System.out.println(Arrays.toString(procOnlySim_res.toArray()));
		out += Arrays.toString(cacheLBSim_res.toArray());
		System.out.println(Arrays.toString(cacheLBSim_res.toArray()));
		out += Arrays.toString(cacheCASim_res.toArray());
		System.out.println(Arrays.toString(cacheCASim_res.toArray()));
		System.out.println(
				"*********************************************************************************************");
	}
}
