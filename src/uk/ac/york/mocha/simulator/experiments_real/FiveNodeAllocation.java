package uk.ac.york.mocha.simulator.experiments_real;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.Pair;

import uk.ac.york.mocha.simulator.dag.DirectedAcyclicGraph;
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

	public static final String[] tasks = { "tacle/adpcm_dec", "tacle/adpcm_enc", "tacle/gsm_dec", "tacle/gsm_enc", "tacle/h264_dec",
			"tacle/mpeg2", "tacle/statemate", "tacle/ndes", "tacle/ammunition", "tacle/g723_enc", "tacle/anagram", "tacle/audiobeam",
			"tacle/huff_dec", "tacle/huff_enc" };

	public static final int nos = 1000;
	public static int seed = 1000;
	public static int cores = 5;
	public static int taskNum = 5;
	public static final int instanceNum = 20;
	public static final boolean licf = true;
	public static boolean print = false;
	public static Random rng;

	public static void main(String args[]) {

//		oneRun();
//		changeCoreNum();
		changeTaskNum();
	}

	public static void oneRun() {
		print = true;
		runOne(3, 5, 5, print, seed, rng);

		System.out.println("\n\n\n\n\n***************************************************");
		System.out.println("***************************************************");
		System.out.println("***************************************************\n\n\n\n\n");

		runOne(4, 5, 5, print, seed, rng);

		System.out.println("\n\n\n\n\n***************************************************");
		System.out.println("***************************************************");
		System.out.println("***************************************************\n\n\n\n\n");

		runOne(5, 5, 5, print, seed, rng);
	}

	public static void changeCoreNum() {

		List<long[]> res0 = new ArrayList<>();
		List<long[]> res1 = new ArrayList<>();
		List<long[]> res2 = new ArrayList<>();

		cores = 3;
		for (int i = 0; i < nos; i++) {
			rng = new Random(seed);
			System.out.println(
					"--------------------- " + " Core: " + 3 + ", TaskNum: " + 5 + ", No. System " + (i + 1) + " ---------------------");
			long[] r = runOne(3, 5, instanceNum, print, seed, rng);
			res0.add(r);
			seed++;
		}

		cores = 4;
		for (int i = 0; i < nos; i++) {
			rng = new Random(seed);
			System.out.println(
					"--------------------- " + " Core: " + 4 + ", TaskNum: " + 5 + ", No. System " + (i + 1) + " ---------------------");
			long[] r = runOne(4, 5, instanceNum, print, seed, rng);
			res1.add(r);
			seed++;
		}

		cores = 5;
		for (int i = 0; i < nos; i++) {
			rng = new Random(seed);
			System.out.println(
					"--------------------- " + " Core: " + 5 + ", TaskNum: " + 5 + ", No. System " + (i + 1) + " ---------------------");
			long[] r = runOne(5, 5, instanceNum, print, seed, rng);
			res2.add(r);
			seed++;
		}

		System.out.println("\n\n--------------------- ALL RESULTS ---------------------");

		String out = "";
		for (int i = 0; i < res0.size(); i++) {
			System.out.println(res0.get(i)[0] + "," + res0.get(i)[1] + "," + res1.get(i)[0] + "," + res1.get(i)[1] + "," + res2.get(i)[0]
					+ "," + res2.get(i)[1]);
			out += res0.get(i)[0] + "," + res0.get(i)[1] + "," + res1.get(i)[0] + "," + res1.get(i)[1] + "," + res2.get(i)[0] + ","
					+ res2.get(i)[1] + "\n";
		}

	}

	public static void changeTaskNum() {

		List<long[]> res0 = new ArrayList<>();
		List<long[]> res1 = new ArrayList<>();
		List<long[]> res2 = new ArrayList<>();

		taskNum = 3;
		for (int i = 0; i < nos; i++) {
			rng = new Random(seed);
			System.out.println(
					"--------------------- " + " Core: " + 3 + ", TaskNum: " + 3 + ", No. System " + (i + 1) + " ---------------------");
			long[] r = runOne(3, 3, instanceNum, print, seed, rng);
			res0.add(r);
			seed++;
		}

		taskNum = 4;
		for (int i = 0; i < nos; i++) {
			rng = new Random(seed);
			System.out.println(
					"--------------------- " + " Core: " + 3 + ", TaskNum: " + 4 + ", No. System " + (i + 1) + " ---------------------");
			long[] r = runOne(3, 4, instanceNum, print, seed, rng);
			res1.add(r);
			seed++;
		}

		taskNum = 5;
		for (int i = 0; i < nos; i++) {
			rng = new Random(seed);
			System.out.println(
					"--------------------- " + " Core: " + 3 + ", TaskNum: " + 5 + ", No. System " + (i + 1) + " ---------------------");
			long[] r = runOne(3, 5, instanceNum, print, seed, rng);
			res2.add(r);
			seed++;
		}

		System.out.println("\n\n--------------------- ALL RESULTS ---------------------");

		String out = "";
		for (int i = 0; i < res0.size(); i++) {
			System.out.println(res0.get(i)[0] + "," + res0.get(i)[1] + "," + res1.get(i)[0] + "," + res1.get(i)[1] + "," + res2.get(i)[0]
					+ "," + res2.get(i)[1]);
			out += res0.get(i)[0] + "," + res0.get(i)[1] + "," + res1.get(i)[0] + "," + res1.get(i)[1] + "," + res2.get(i)[0] + ","
					+ res2.get(i)[1] + "\n";
		}

	}

	public static long[] runOne(int cores, int taskNum, int instanceNum, boolean print, int seed, Random rng) {

		List<String> taskName = new ArrayList<>();
		for (int i = 0; i < taskNum; i++) {
			taskName.add(tasks[i]);
		}

		CacheHierarchy cache = generateCache(cores);
		List<RecencyProfileReal> crps = readCRP("crp/profile.tacle.crp.json", taskName, cache);

		List<DirectedAcyclicGraph> dags = dagGenerator(crps.subList(0, taskNum), cache, cores, instanceNum, rng);

		return simulate(taskNum, dags, cache, cores, print, rng);
	}

	public static long[] simulate(int taskNum, List<DirectedAcyclicGraph> dags, CacheHierarchy cache, int cores, boolean print,
			Random rng) {

		for (DirectedAcyclicGraph d : dags) {
			// for (Node n : d.getFlatNodes())
			// n.hasFaults = true;

			d.releaseTime = 0;
		}

		List<DirectedAcyclicGraph> tasks = new ArrayList<>();
		for (int i = 0; i < instanceNum; i++) {
			for (int j = 0; j < taskNum; j++) {
				tasks.add(dags.get(j * 20 + i));
			}
			// tasks.add(dags.get(10 + i));
			// tasks.add(dags.get(20 + i));
			// tasks.add(dags.get(40 + i));
			// tasks.add(dags.get(60 + i));
			// tasks.add(dags.get(80 + i));
		}

		rng = new Random(seed);
		Simualtor sim1 = new Simualtor(SimuType.CLOCK_LEVEL, Hardware.PROC_CACHE, Allocation.CACHE_AWARE, RecencyType.TIME_DEFAULT, tasks,
				cache, cores, seed, licf);
		Pair<List<DirectedAcyclicGraph>, double[]> pair1 = sim1.simulate(print);

		rng = new Random(seed);
		Simualtor sim2 = new Simualtor(SimuType.CLOCK_LEVEL, Hardware.PROC_CACHE, Allocation.WORST_FIT, RecencyType.TIME_DEFAULT, tasks,
				cache, cores, seed, licf);
		Pair<List<DirectedAcyclicGraph>, double[]> pair2 = sim2.simulate(print);

		// pair1.getFirst().stream().forEach(c -> System.out.print(c.finishTime
		// - c.startTime + " "));
		// System.out.println();
		// pair2.getFirst().stream().forEach(c -> System.out.print(c.finishTime
		// - c.startTime + " "));

		System.out.println(sim1.totalMakespan + " " + sim2.totalMakespan);

		long[] makespan = { sim1.totalMakespan, sim2.totalMakespan };
		return makespan;
	}

	public static List<RecencyProfileReal> readCRP(String filename, List<String> taskNames, CacheHierarchy cache) {
		List<RecencyProfileReal> crps = Utils.readJson(filename, taskNames, cache);
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

	public static List<DirectedAcyclicGraph> dagGenerator(List<RecencyProfileReal> crps, CacheHierarchy cache, int cores, int instanceNum,
			Random rng) {

		crps.stream().forEach(c -> c.WCET = c.WCET * (1 + rng.nextDouble() * 0.2 - 0.1));
		crps.stream().forEach(c -> c.medainET = c.medainET * (1 + rng.nextDouble() * 0.2 - 0.1));

		List<Long> wcets = crps.stream().map(c -> (long) Math.round(c.medainET)).collect(Collectors.toList());
		List<Long> periods = crps.stream().map(c -> (long) Math.round(c.WCET) * 2).collect(Collectors.toList());
		List<Integer> priorities = crps.stream().map(c -> 1000).collect(Collectors.toList());

		SystemGenerator gen = new SystemGenerator(cores, crps.size(), false, false, null, rng, false, false, cache.level2);
		List<DirectedAcyclicGraph> sys = gen.generatedForSteven(wcets, periods, priorities, crps, cache, instanceNum);

		return sys;
	}

}
