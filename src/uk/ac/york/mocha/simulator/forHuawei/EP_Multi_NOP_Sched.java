package uk.ac.york.mocha.simulator.forHuawei;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.Pair;

import uk.ac.york.mocha.simulator.dag.DAG;
import uk.ac.york.mocha.simulator.generator.SystemGenerator;
import uk.ac.york.mocha.simulator.parameters.SystemParameters;
import uk.ac.york.mocha.simulator.parameters.SystemParameters.ExpName;
import uk.ac.york.mocha.simulator.schedule.InfoCap;
import uk.ac.york.mocha.simulator.schedule.TPDSOurMultiDAG;

public class EP_Multi_NOP_Sched {

	final static DecimalFormat df = new DecimalFormat("#.###");

	final static String expName = "nopF";
	static String outFolder = "result_multi_sched";

//	final static int cores = 8;
	final static int minParal = 1;
	final static int maxParal = 10;
	final static int not = 5;
	final static int intanceNum = 1;
	final static int hyperPeriodNum = 1;
	final static int seed = 1000;
	final static int util = 7;

	public static void main(String args[]) {
		int nopSever = 1;
		SystemParameters.NoS = 1000;

		try {
			nopSever = Integer.parseInt(args[0]);
			System.out.println("Input received, Number of Sever Core: " + nopSever);

			if (Integer.parseInt(args[1]) == 1) {
				SystemParameters.david = true;
				outFolder = "result_multi_sched";
				System.out.println("Using david's utilisation!");
			}

		} catch (Exception e) {
			System.out.println("Default number of Sever Core: " + nopSever);
		}

		changeTaskNoP(nopSever);
	}

	public static void changeTaskNoP(int nopSever) {

		int startingNoP = 4;
		int endNoP = 32;

		List<ResultCap> caps = new ArrayList<>();

		for (int i = startingNoP; i <= endNoP; i = i + 4) {

			final int index = i;
			double utilPerTask = (double) i / (double) 10 / (double) not * (double) util;

			ResultCap r = RunOneGroup(index, not, intanceNum, hyperPeriodNum, true, null, seed, seed, null,
					SystemParameters.NoS, utilPerTask, true, ExpName.taskNum, minParal, maxParal, nopSever);

			caps.add(r);

			for (int k = 0; k < 4; k++)
				ResultCollector.writeSchedToSystem(r, index, expName, outFolder);
		}

		System.out.println("--------------------------------");
		for (ResultCap cap : caps) {
			System.out.println(cap.NoSched_our + " " + cap.NoSched_he + " " + cap.NoSched_seq + " " + cap.NoSched_fed
					+ " in " + cap.total_counter + " systems.");
		}
		System.out.println("------------- DONE -------------");

	}

	public static ResultCap RunOneGroup(int cores, int taskNum, int intanceNum, int hyperperiodNum, boolean takeAllUtil,
			List<List<Double>> util, int taskSeed, int tableSeed, List<List<Long>> periods, int NoS, double utilPerTask,
			boolean randomC, ExpName name, int maxPara, int minPara, int nopSever) {

		int workload = SystemParameters.NoS / nopSever;
		final int taskSeeds = 1000;

		ResultCap cap = new ResultCap();
		List<Thread> workers = new ArrayList<>();

		for (int i = 0; i < nopSever; i++) {
			int offset = i * workload;

			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					int seed = taskSeeds + offset;

					for (int k = offset; k < offset + workload; k++) {

						System.out.println("Current system number: " + (k) + " --- number of cores: " + cores
								+ " --- system util: " + df.format(utilPerTask * (double) taskNum / (double) cores));

						SystemGenerator gen = new SystemGenerator(cores, taskNum, true, takeAllUtil,
								util == null ? null : util.get(k), seed, randomC, SystemParameters.printGen,
								utilPerTask, maxPara, minPara, SystemParameters.david);

						Pair<List<DAG>, List<DAG>> p = gen.generatedDAGInstancesInOneHP(intanceNum, hyperperiodNum,
								periods == null ? null : periods.get(k), false, SystemParameters.dagType);

						List<DAG> dagsInOneHP = p.getFirst();
						List<InfoCap> our = new TPDSOurMultiDAG().getResponseTime(dagsInOneHP, cores);

						if (isSchedulable(dagsInOneHP, our)) {
							cap.incrementOur();
						}

						cap.addCounter();
						cap.addTotalCounter();

						seed++;
					}

				}
			});

			workers.add(t);
		}

		for (Thread t : workers)
			t.start();
		for (Thread t : workers) {
			try {
				t.join();
			} catch (InterruptedException e) {
			}
		}

		return cap;
	}

	private static boolean isSchedulable(List<DAG> dags, List<InfoCap> response_time) {

		if (response_time == null)
			return false;

		int unsched = 0;

		for (int i = 0; i < dags.size(); i++) {
			if (dags.get(i).getSchedParameters().getDeadline() < response_time.get(i).best_response_time)
				unsched++;
		}

		double missRate = (double) unsched / (double) dags.size();

		if (missRate <= 0.2)
			return true;
		else
			return false;

//		return true;
	}

}
