package uk.ac.york.mocha.simulator.experimentsTPDS;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.Pair;

import uk.ac.york.mocha.simulator.dag.DAG;
import uk.ac.york.mocha.simulator.generator.SystemGenerator;
import uk.ac.york.mocha.simulator.parameters.SystemParameters;
import uk.ac.york.mocha.simulator.parameters.SystemParameters.ExpName;
import uk.ac.york.mocha.simulator.schedule.FederatedSchedule;
import uk.ac.york.mocha.simulator.schedule.InfoCap;
import uk.ac.york.mocha.simulator.schedule.RTSSOurSingleDAG;
import uk.ac.york.mocha.simulator.schedule.TPDSOurMultiDAG;
import uk.ac.york.mocha.simulator.schedule.TPDSHe;

public class EP_Multi_SysUtil {

	final static DecimalFormat df = new DecimalFormat("#.###");

	final static String expName = "util";
	static String outFolder = "result_multi";

	final static int cores = 8;
	final static int not = 5;
	final static int intanceNum = 1;
	final static int hyperPeriodNum = 1;
	final static int minParal = 1;
	final static int maxParal = 10;
	final static int seed = 1000;

	public static void main(String args[]) {
		int nopSever = 1;

		SystemParameters.NoS = 1000;

		try {
			nopSever = Integer.parseInt(args[0]);
			System.out.println("Input received, Number of Sever Core: " + nopSever);

			if (Integer.parseInt(args[1]) == 1) {
				SystemParameters.david = true;
				outFolder = "result_multi_daivd";
				System.out.println("Using david's utilisation!");
			}

		} catch (Exception e) {
			System.out.println("No input given, using the default Number of Sever Core: " + nopSever);
		}

		changeUtil(nopSever);
	}

	public static void changeUtil(int nopSever) {

		int startingUtil = 1;
		int endUtil = 12;

		List<ResultCap> caps = new ArrayList<>();

		for (int i = startingUtil; i <= endUtil; i++) {
			final int index = i;

			double utilPerTask = (double) cores / (double) 10 / (double) not * (double) index / (double) 2;

			ResultCap r = RunOneGroup(cores, not, intanceNum, hyperPeriodNum, true, null, seed, seed, null,
					SystemParameters.NoS, Double.parseDouble(df.format(utilPerTask)), true, ExpName.taskNum, minParal,
					maxParal, nopSever);

			caps.add(r);

			for (int k = 0; k <= endUtil - startingUtil; k++)
				ResultCollector.writeSchedToSystem(r, index, k, expName, outFolder);
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

		int workload = 50000 / nopSever;
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
						cap.checkFinish();

						if (cap.isFinish())
							return;

						boolean ourS = false;
						boolean heS = false;
						boolean fedS = false;

						System.out.println(
								"Current system number: " + (k) + " --- system util: " + (utilPerTask * taskNum));

						SystemGenerator gen = new SystemGenerator(cores, taskNum, true, takeAllUtil,
								util == null ? null : util.get(k), seed, randomC, SystemParameters.printGen,
								utilPerTask, maxPara, minPara, SystemParameters.david);

						Pair<List<DAG>, List<DAG>> p = gen.generatedDAGInstancesInOneHP(intanceNum, hyperperiodNum,
								periods == null ? null : periods.get(k), false, SystemParameters.dagType);

						List<DAG> dagsInOneHP = p.getFirst();
						List<DAG> dagTasks = p.getSecond();

						List<InfoCap> seq = new RTSSOurSingleDAG().getResponseTime(dagsInOneHP, cores);
						List<InfoCap> he = new TPDSHe().getResponseTime(dagTasks, cores);
						List<InfoCap> our = new TPDSOurMultiDAG().getResponseTime(dagsInOneHP, cores);
						List<InfoCap> fed = new FederatedSchedule().getResponseTime(dagsInOneHP, cores);

						if (isSchedulable(dagsInOneHP, seq))
							cap.incrementSeq();

						if (isSchedulable(dagTasks, he)) {
							cap.incrementHe();
							heS = true;
						}

						if (isSchedulable(dagsInOneHP, our)) {
							cap.incrementOur();
							ourS = true;
						}

						if (isSchedulable(dagsInOneHP, fed)) {
							cap.incrementFed();
							fedS = true;
						}

						if (ourS && heS && fedS) {
							cap.addCounter();

							long[] interDelayHe = new long[dagTasks.size()];
							long[] interDelayOur = new long[dagTasks.size()];
							long[] interDelayFed = new long[dagTasks.size()];

							long[] intraDelayHe = new long[dagTasks.size()];
							long[] intraDelayOur = new long[dagTasks.size()];
							long[] intraDelayFed = new long[dagTasks.size()];

							long[] responseTimeHe = new long[dagTasks.size()];
							long[] responseTimeOur = new long[dagTasks.size()];
							long[] responseTimeFed = new long[dagTasks.size()];

							for (int j = 0; j < he.size(); j++) {
								interDelayHe[j] = he.get(j).best_inter;
								intraDelayHe[j] = he.get(j).best_intra;
								responseTimeHe[j] = he.get(j).best_response_time;
							}

							for (int j = 0; j < our.size(); j++) {
								int index = dagsInOneHP.get(j).id;

								if (interDelayOur[index] < our.get(j).best_inter)
									interDelayOur[index] = our.get(j).best_inter;

								if (intraDelayOur[index] < our.get(j).best_intra)
									intraDelayOur[index] = our.get(j).best_intra;

								if (responseTimeOur[index] < our.get(j).best_response_time)
									responseTimeOur[index] = our.get(j).best_response_time;
							}

							for (int j = 0; j < fed.size(); j++) {
								int index = dagsInOneHP.get(j).id;

								if (interDelayFed[index] < fed.get(j).best_inter)
									interDelayFed[index] = fed.get(j).best_inter;

								if (intraDelayFed[index] < fed.get(j).best_intra)
									intraDelayFed[index] = fed.get(j).best_intra;

								if (responseTimeFed[index] < fed.get(j).best_response_time)
									responseTimeFed[index] = fed.get(j).best_response_time;
							}

							List<long[]> interDelays = new ArrayList<>();
							interDelays.add(interDelayOur);
							interDelays.add(interDelayHe);
							interDelays.add(interDelayFed);
							cap.addInterDelay(interDelays);

							List<long[]> intraDelays = new ArrayList<>();
							intraDelays.add(intraDelayOur);
							intraDelays.add(intraDelayHe);
							intraDelays.add(intraDelayFed);
							cap.addIntraDelay(intraDelays);

							List<long[]> response_times = new ArrayList<>();
							response_times.add(responseTimeOur);
							response_times.add(responseTimeHe);
							response_times.add(responseTimeFed);
							cap.addResponseTime(response_times);
						}

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

		for (int i = 0; i < dags.size(); i++) {
			if (dags.get(i).getSchedParameters().getDeadline() < response_time.get(i).best_response_time)
				return false;
		}

		return true;
	}

}
