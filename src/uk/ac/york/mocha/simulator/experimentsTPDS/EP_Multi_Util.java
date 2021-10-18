package uk.ac.york.mocha.simulator.experimentsTPDS;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.util.Pair;

import uk.ac.york.mocha.simulator.dag.DAG;
import uk.ac.york.mocha.simulator.generator.SystemGenerator;
import uk.ac.york.mocha.simulator.parameters.SystemParameters;
import uk.ac.york.mocha.simulator.parameters.SystemParameters.ExpName;
import uk.ac.york.mocha.simulator.schedule.InfoCap;
import uk.ac.york.mocha.simulator.schedule.SemiWorkConversing;
import uk.ac.york.mocha.simulator.schedule.TPDSHe;

public class EP_Multi_Util {

	static DecimalFormat df = new DecimalFormat("#.###");

	public static void main(String args[]) {
		int nopSever = 4;
		
		try {
			nopSever = Integer.parseInt(args[1]);
		} catch (Exception e) {
		}
		
		changeTaskParal(nopSever);
	}

	public static void changeTaskParal(int nopSever) {

		int not = 4;
		int intanceNum = 1;
		int hyperPeriodNum = 1;
		int seed = 1000;

		int startingVar = 1;
		int endVar = 16;

		SystemParameters.NoS = 500;

		List<List<Integer>> res = new ArrayList<>();
		List<List<List<long[]>>> delay = new ArrayList<>();

		List<Thread> runners = new ArrayList<>();

		for (int i = startingVar; i <= endVar; i++) {
			final int index = i;

			Thread t = new Thread(new Runnable() {

				@Override
				public void run() {

					double utilPerTask = (double) 1.0;

					Pair<List<Integer>, List<List<long[]>>> r = RunOneGroup(index*2, not, intanceNum, hyperPeriodNum,
							true, null, seed, seed, null, SystemParameters.NoS, utilPerTask, true, ExpName.taskNum,
							1, 10);

					res.set(index - startingVar, r.getFirst());
					System.out.println("Util: " + utilPerTask + "  >>>>>>>>>>>> " + r.getFirst());

					List<List<long[]>> r2 = r.getSecond();
					delay.set(index - startingVar, r2);

					System.out.println("Util: " + utilPerTask + "  >>>>>>>>>>>> our delay");
					for (int i = 0; i < r2.size(); i++) {
						System.out.println(Arrays.toString(r2.get(i).get(0)));
					}

					System.out.println("Util: " + utilPerTask + "  >>>>>>>>>>>> he delay");
					for (int i = 0; i < r2.size(); i++) {
						System.out.println(Arrays.toString(r2.get(i).get(1)));
					}
				}
			});

			runners.add(t);

			res.add(new ArrayList<>());
			delay.add(new ArrayList<>());
		}

		for (Thread thread : runners)
			thread.start();

		for (Thread thread : runners) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		System.out.println("\n\n\n---------- Results ----------");
		for (List<Integer> r1 : res) {
			System.out.println(r1);
		}

		for (int k = 0; k < delay.size(); k++) {
			double utilPerTask = k;
			System.out.println("************** Util Per Task: " + utilPerTask + "  ************************");

			List<List<long[]>> r2 = delay.get(k);

			System.out.println(">>>>>>>>>>>> our delay");
			for (int i = 0; i < r2.size(); i++) {
				System.out.println(Arrays.toString(r2.get(i).get(0)));
			}

			System.out.println(">>>>>>>>>>>> he delay");
			for (int i = 0; i < r2.size(); i++) {
				System.out.println(Arrays.toString(r2.get(i).get(1)));
			}
		}

	}

	public static Pair<List<Integer>, List<List<long[]>>> RunOneGroup(int cores, int taskNum, int intanceNum,
			int hyperperiodNum, boolean takeAllUtil, List<List<Double>> util, int taskSeed, int tableSeed,
			List<List<Long>> periods, int NoS, double utilPerTask, boolean randomC, ExpName name, int maxPara,
			int minPara) {

		taskSeed = 1000;

		int NoSched_our = 0;
		int NoSched_he = 0;

		List<List<long[]>> delay = new ArrayList<>();

		for (int i = 0; i < NoS; i++) {
			System.out.println(
					"\n\n****************************************************************************************************");
			System.out.println("Change Task Number: " + taskNum + " --- Current system number: " + (i + 1)
					+ " --- cores: " + cores);

			SystemGenerator gen = new SystemGenerator(cores, taskNum, true, takeAllUtil,
					util == null ? null : util.get(i), taskSeed, randomC, SystemParameters.printGen, utilPerTask,
					maxPara, minPara);

			Pair<List<DAG>, List<DAG>> p = gen.generatedDAGInstancesInOneHP(intanceNum, hyperperiodNum,
					periods == null ? null : periods.get(i), false, SystemParameters.dagType);

			List<DAG> dagsInOneHP = p.getFirst();
			List<DAG> dagTasks = p.getSecond();

			List<InfoCap> he = new TPDSHe().getResponseTime(dagTasks, cores);
			List<InfoCap> our = new SemiWorkConversing().getResponseTime(dagsInOneHP, cores);

			if (isSchedulable(dagTasks, he))
				NoSched_he++;

			if (isSchedulable(dagsInOneHP, our))
				NoSched_our++;

			if (our != null) {
				long[] delayHe = new long[dagTasks.size()];
				long[] delayOur = new long[dagTasks.size()];

				for (int k = 0; k < he.size(); k++) {
					delayHe[k] = he.get(k).best_inter;
				}

				for (int k = 0; k < our.size(); k++) {
					int index = dagsInOneHP.get(k).id;
					if (delayOur[index] < our.get(k).best_inter)
						delayOur[index] = our.get(k).best_inter;
				}

				List<long[]> delays = new ArrayList<>();
				delays.add(delayOur);
				delays.add(delayHe);

				delay.add(delays);
			}

//			System.out.println("us: " + our);
//			System.out.println("us deadline: "
//					+ dagsInOneHP.stream().map(c -> c.getSchedParameters().getDeadline()).collect(Collectors.toList()));
//			System.out.println("he: " + he);
//			System.out.println("he deadline: "
//					+ dagTasks.stream().map(c -> c.getSchedParameters().getDeadline()).collect(Collectors.toList()));

			taskSeed++;
		}

//		System.out.println("us: " + NoSched_our);
//		System.out.println("he: " + NoSched_he);

		List<Integer> res = new ArrayList<>();
		res.add(NoSched_our);
		res.add(NoSched_he);

		return new Pair<List<Integer>, List<List<long[]>>>(res, delay);
	}

	private static boolean isSchedulable(List<DAG> dags, List<InfoCap> response_time) {

//		if (response_time.size() != dags.size()) {
//			System.err.println("EP_Multi.isSchedulable()");
//			System.err.println("fatal error: array size not equal!");
//		}

		if (response_time == null)
			return false;

		for (int i = 0; i < dags.size(); i++) {
			if (dags.get(i).getSchedParameters().getDeadline() < response_time.get(i).best_response_time)
				return false;
		}

		return true;
	}

}
