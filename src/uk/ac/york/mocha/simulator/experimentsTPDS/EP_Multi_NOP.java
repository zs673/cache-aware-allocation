package uk.ac.york.mocha.simulator.experimentsTPDS;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.Pair;

import uk.ac.york.mocha.simulator.dag.DAG;
import uk.ac.york.mocha.simulator.generator.SystemGenerator;
import uk.ac.york.mocha.simulator.parameters.SystemParameters;
import uk.ac.york.mocha.simulator.parameters.SystemParameters.ExpName;
import uk.ac.york.mocha.simulator.schedule.InfoCap;
import uk.ac.york.mocha.simulator.schedule.SemiWorkConversing;
import uk.ac.york.mocha.simulator.schedule.TPDSHe;
import uk.ac.york.mocha.simulator.simulator.Utils;

public class EP_Multi_NOP {

	final static DecimalFormat df = new DecimalFormat("#.###");

	final static String expName = "nop_sched.txt";
	static String outFolder = "result_multi";

//	final static int cores = 8;
	final static int minParal = 1;
	final static int maxParal = 10;
	final static int not = 4;
	final static int intanceNum = 1;
	final static int hyperPeriodNum = 1;
	final static int seed = 1000;

	public static void main(String args[]) {
		int nopSever = 1;
		SystemParameters.NoS = 1000;

		try {
			nopSever = Integer.parseInt(args[0]);
			System.out.println("Input received, Number of Sever Core: " + nopSever);
			
			if(Integer.parseInt(args[1]) == 1) {
				SystemParameters.david = true;
				outFolder = "result_multi_daivd";
			}
			
		} catch (Exception e) {
			System.out.println("Default number of Sever Core: " + nopSever);
		}

		changeTaskNoP(nopSever);
	}

	public static void changeTaskNoP(int nopSever) {

		int startingNoP = 2;
		int endNoP = 32;
		

		List<ResultCap> caps = new ArrayList<>();

		for (int i = startingNoP; i <= endNoP; i=i+2) {
			final int index = i;

			double utilPerTask = (double) index / (double) 10 / (double) not * (double) 3;

			ResultCap r = RunOneGroup(index, not, intanceNum, hyperPeriodNum, true, null, seed, seed, null,
					SystemParameters.NoS, utilPerTask, true, ExpName.taskNum, minParal, maxParal, nopSever);

			caps.add(r);

			for (int k = 0; k < 4; k++)
				writeSchedToSystem(r, index, k);
		}
		
		System.out.println("--------------------------------");
		for (ResultCap cap : caps) {
			System.out.println(cap.NoSched_our + " " + cap.NoSched_he);
		}
		System.out.println("------------- DONE -------------");
		
	}

	private static void writeSchedToSystem(ResultCap cap, int cores, int mode) {
		String our_file = "";
		String he_file = "";
		String our_out = "";
		String he_out = "";
		String file = "";
		String out = "";

		switch (mode) {
		case 0: // intra-task interference
			our_file = "nop" + "_" + cores + "_" + "intra" + "_" + "our" + ".txt";
			he_file = "nop" + "_" + cores + "_" + "intra" + "_" + "he" + ".txt";

			List<List<long[]>> intra = cap.intra_delay;
			for (List<long[]> i : intra) {
				long[] our = i.get(0);
				for (long l : our) {
					our_out += l + " ";
				}
				our_out += "\n";

				long[] he = i.get(1);
				for (long l : he) {
					he_out += l + " ";
				}
				he_out += "\n";
			}

			Utils.writeResult(outFolder, our_file, our_out);
			Utils.writeResult(outFolder, he_file, he_out);

			break;
		case 1: // inter-task interference
			our_file = "nop" + "_" + cores + "_" + "inter" + "_" + "our" + ".txt";
			he_file = "nop" + "_" + cores + "_" + "inter" + "_" + "he" + ".txt";

			List<List<long[]>> inter = cap.inter_delay;
			for (List<long[]> i : inter) {
				long[] our = i.get(0);
				for (long l : our) {
					our_out += l + " ";
				}
				our_out += "\n";

				long[] he = i.get(1);
				for (long l : he) {
					he_out += l + " ";
				}
				he_out += "\n";
			}

			Utils.writeResult(outFolder, our_file, our_out);
			Utils.writeResult(outFolder, he_file, he_out);

			break;
		case 2: // response time
			our_file = "nop" + "_" + cores + "_" + "response" + "_" + "our" + ".txt";
			he_file = "nop" + "_" + cores + "_" + "response" + "_" + "he" + ".txt";

			List<List<long[]>> response = cap.response_time;
			for (List<long[]> i : response) {
				long[] our = i.get(0);
				for (long l : our) {
					our_out += l + " ";
				}
				our_out += "\n";

				long[] he = i.get(1);
				for (long l : he) {
					he_out += l + " ";
				}
				he_out += "\n";
			}

			Utils.writeResult(outFolder, our_file, our_out);
			Utils.writeResult(outFolder, he_file, he_out);

			break;
		case 3: // sched info
			file = "nop" + "_" + cores + "_" + "sched" + ".txt";

			out += cap.NoSched_our + " ";
			out += cap.NoSched_he + "\n";

			Utils.writeResult(outFolder, file, out);

			break;
		}
	}

	public static ResultCap RunOneGroup(int cores, int taskNum, int intanceNum, int hyperperiodNum, boolean takeAllUtil,
			List<List<Double>> util, int taskSeed, int tableSeed, List<List<Long>> periods, int NoS, double utilPerTask,
			boolean randomC, ExpName name, int maxPara, int minPara, int nopSever) {

		int workload = NoS / nopSever;
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
						System.out.println("Current system number: " + (k) + " --- number of cores: " + cores);

						SystemGenerator gen = new SystemGenerator(cores, taskNum, true, takeAllUtil,
								util == null ? null : util.get(k), seed, randomC, SystemParameters.printGen,
								utilPerTask, maxPara, minPara, SystemParameters.david);

						Pair<List<DAG>, List<DAG>> p = gen.generatedDAGInstancesInOneHP(intanceNum, hyperperiodNum,
								periods == null ? null : periods.get(k), false, SystemParameters.dagType);

						List<DAG> dagsInOneHP = p.getFirst();
						List<DAG> dagTasks = p.getSecond();

						List<InfoCap> he = new TPDSHe().getResponseTime(dagTasks, cores);
						List<InfoCap> our = new SemiWorkConversing().getResponseTime(dagsInOneHP, cores);

						if (isSchedulable(dagTasks, he))
							cap.incrementHe();

						if (isSchedulable(dagsInOneHP, our))
							cap.incrementOur();

						if (our != null) {
							long[] interDelayHe = new long[dagTasks.size()];
							long[] interDelayOur = new long[dagTasks.size()];

							long[] intraDelayHe = new long[dagTasks.size()];
							long[] intraDelayOur = new long[dagTasks.size()];

							long[] responseTimeHe = new long[dagTasks.size()];
							long[] responseTimeOur = new long[dagTasks.size()];

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

							List<long[]> interDelays = new ArrayList<>();
							interDelays.add(interDelayOur);
							interDelays.add(interDelayHe);
							cap.addInterDelay(interDelays);

							List<long[]> intraDelays = new ArrayList<>();
							intraDelays.add(intraDelayOur);
							intraDelays.add(intraDelayHe);
							cap.addIntraDelay(intraDelays);

							List<long[]> response_times = new ArrayList<>();
							response_times.add(responseTimeOur);
							response_times.add(responseTimeHe);
							cap.addResponseTime(response_times);
						}

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

