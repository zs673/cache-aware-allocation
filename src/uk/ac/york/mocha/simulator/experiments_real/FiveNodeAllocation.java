package uk.ac.york.mocha.simulator.experiments_real;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.Pair;

import uk.ac.york.mocha.simulator.dag.DirectedAcyclicGraph;
import uk.ac.york.mocha.simulator.dag.Node;
import uk.ac.york.mocha.simulator.dag.RecencyProfileReal;
import uk.ac.york.mocha.simulator.generator.CacheHierarchy;
import uk.ac.york.mocha.simulator.generator.SystemGenerator;
import uk.ac.york.mocha.simulator.parameters.SystemParameters.Allocation;
import uk.ac.york.mocha.simulator.parameters.SystemParameters.Hardware;
import uk.ac.york.mocha.simulator.parameters.SystemParameters.RecencyType;
import uk.ac.york.mocha.simulator.parameters.SystemParameters.SimuType;
import uk.ac.york.mocha.simulator.simulator.Simualtor;
import uk.ac.york.mocha.simulator.simulator.Utils;

public class FiveNodeAllocation {

	public static int seed = 1000;
	public static final int cores = 3;
	public static final int taskNum = 5;
	public static final int instanceNum = 20;
	public static final boolean print = true;

	public static void main(String args[]) {

		List<long[]> res = new ArrayList<>();

		for (int i = 0; i < 1000; i++) {
			long[] r = runOne(cores, taskNum, instanceNum, print, seed);
			res.add(r);
			seed++;
		}

		System.out.println(res.toArray());

//		System.out.println("\n\n\n\n\n***************************************************");
//		System.out.println("***************************************************");
//		System.out.println("***************************************************\n\n\n\n\n");
//		
//		runOne(4, 5, 5);
//		
//		System.out.println("\n\n\n\n\n***************************************************");
//		System.out.println("***************************************************");
//		System.out.println("***************************************************\n\n\n\n\n");
//		
//		runOne(5, 5, 5);
	}

	public static long[] runOne(int cores, int taskNum, int instanceNum, boolean print, int seed) {
		CacheHierarchy cache = generateCache(cores);
		List<RecencyProfileReal> crps = readCRP("crp/profile.tacle.crp.json", cache);

		List<DirectedAcyclicGraph> dags = dagGenerator(crps.subList(0, taskNum), cache, cores, instanceNum, seed);

		return simulate(dags, cache, cores, print, seed);
	}

	public static long[] simulate(List<DirectedAcyclicGraph> dags, CacheHierarchy cache, int cores, boolean print,
			int seed) {

		for (DirectedAcyclicGraph d : dags) {
			for (Node n : d.getFlatNodes())
				n.hasFaults = true;

//			d.releaseTime = 0;
		}

		Simualtor sim1 = new Simualtor(SimuType.CLOCK_LEVEL, Hardware.PROC_CACHE, Allocation.CACHE_AWARE,
				RecencyType.TIME_DEFAULT, dags, cache, cores, seed, true);
		Pair<List<DirectedAcyclicGraph>, double[]> pair1 = sim1.simulate(print);

		Simualtor sim2 = new Simualtor(SimuType.CLOCK_LEVEL, Hardware.PROC_CACHE, Allocation.WORST_FIT,
				RecencyType.TIME_DEFAULT, dags, cache, cores, seed, true);
		Pair<List<DirectedAcyclicGraph>, double[]> pair2 = sim2.simulate(print);

		pair1.getFirst().stream().forEach(c -> System.out.print(c.finishTime - c.startTime + " "));
		System.out.println();
		pair2.getFirst().stream().forEach(c -> System.out.print(c.finishTime - c.startTime + " "));

//		System.out.println(sim1.totalMakespan + " " + sim2.totalMakespan);
		
		long[] makespan = { sim1.totalMakespan, sim2.totalMakespan };
		return makespan;
	}

	public static List<RecencyProfileReal> readCRP(String filename, CacheHierarchy cache) {
		List<RecencyProfileReal> crps = Utils.readJson(filename, cache);
		return crps;
	}

	public static CacheHierarchy generateCache(int cores) {

		List<int[]> level2 = new ArrayList<>();

		for (int i = 0; i < cores; i++) {
			int[] cluster = { i };
			level2.add(cluster);
		}

		CacheHierarchy cache = new CacheHierarchy(cores, 3, level2);

		return cache;
	}

	public static List<DirectedAcyclicGraph> dagGenerator(List<RecencyProfileReal> crps, CacheHierarchy cache,
			int cores, int instanceNum, int seed) {

		List<Long> wcets = crps.stream().map(c -> (long) Math.round(c.WCET)).collect(Collectors.toList());
		List<Long> periods = crps.stream().map(c -> (long) Math.round(c.WCET) * 2).collect(Collectors.toList());
		List<Integer> priorities = crps.stream().map(c -> 1000).collect(Collectors.toList());

		SystemGenerator gen = new SystemGenerator(cores, crps.size(), false, false, null, seed, false, false,
				cache.level2);
		List<DirectedAcyclicGraph> sys = gen.generatedForSteven(wcets, periods, priorities, crps, cache, instanceNum);

		return sys;
	}

}
