package uk.ac.york.mocha.simulator.simulator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.Pair;

import uk.ac.york.mocha.simulator.dag.DAGtoPython;
import uk.ac.york.mocha.simulator.dag.DirectedAcyclicGraph;
import uk.ac.york.mocha.simulator.dag.Node;
import uk.ac.york.mocha.simulator.parameters.SystemParameters;

public class Utils {

	public static List<List<Node>> getAllocHistoryByLevel2Cache(List<List<Node>> allocHis) {

		List<List<Node>> level2 = new ArrayList<>();
		for (int i = 0; i < allocHis.size() / SystemParameters.Level2CoreNum; i++) {
			level2.add(new ArrayList<>());
		}

		for (int i = 0; i < allocHis.size(); i++) {
			int cluster = i / SystemParameters.Level2CoreNum;

			level2.get(cluster).addAll(allocHis.get(i));
		}
		
		return level2;
	}

	public static void assignPriorityOur(List<DirectedAcyclicGraph> dags) {
		for (DirectedAcyclicGraph dag : dags) {
			Pair<Long, List<int[]>> res = null;

			dag.hard = true;

			res = DAGtoPython.pharseDAGForPython(dag, 8);
			List<int[]> prio = res.getSecond();

			for (Node n : dag.getFlatNodes()) {
				int id = n.getId();

				for (int i = 0; i < prio.size(); i++) {
					if (prio.get(i)[0] - 1 == id) {
						n.priority = prio.get(i)[1];
						break;
					}
				}
			}
		}
	}

	/*
	 * Order nodes by 1) its DAG priority and 2) its WCET.
	 */
	public static int compareNode(List<DirectedAcyclicGraph> dags, Node c1, Node c2) {

		DirectedAcyclicGraph dag1 = Utils.getDagByIndex(dags, c1.getDagID(), c1.getDagInstNo());
		DirectedAcyclicGraph dag2 = Utils.getDagByIndex(dags, c2.getDagID(), c2.getDagInstNo());

		if (dag1.getSchedParameters().getPriority() > dag2.getSchedParameters().getPriority()) {
			return -1;
		} else if (dag1.getSchedParameters().getPriority() < dag2.getSchedParameters().getPriority()) {
			return 1;
		} else {

			int c = -Long.compare(c1.getWCET(), c2.getWCET());

			if (c != 0)
				return c;
			else {
				return Integer.compare(c1.getDagInstNo(), c2.getDagInstNo());
			}

		}

	}
	

	public static int compareNodeByID(List<DirectedAcyclicGraph> dags, Node c1, Node c2) {
		
		return Integer.compare(c1.getId(), c2.getId());
	}

	public static int compareNodeWithPriority(List<DirectedAcyclicGraph> dags, Node c1, Node c2) {

		DirectedAcyclicGraph dag1 = Utils.getDagByIndex(dags, c1.getDagID(), c1.getDagInstNo());
		DirectedAcyclicGraph dag2 = Utils.getDagByIndex(dags, c2.getDagID(), c2.getDagInstNo());

		if (dag1.getSchedParameters().getPriority() > dag2.getSchedParameters().getPriority()) {
			return -1;
		} else if (dag1.getSchedParameters().getPriority() < dag2.getSchedParameters().getPriority()) {
			return 1;
		} else {
			int c = -1;

			if (dag1.id != dag2.id) {
				System.out.println(
						"Utils.compareNodeWithHard(): the IDs of DAG-1 and DAG-2 are not equal, but they have the same priority!");
				System.exit(-1);
			}

			c = Integer.compare(c1.getDagInstNo(), c1.getDagInstNo());

			if (c != 0)
				return c;
			else {
				return -Long.compare(c1.priority, c2.priority);
			}

		}

	}

	public static int compareNodeWithHard(List<DirectedAcyclicGraph> dags, Node c1, Node c2) {

		DirectedAcyclicGraph dag1 = Utils.getDagByIndex(dags, c1.getDagID(), c1.getDagInstNo());
		DirectedAcyclicGraph dag2 = Utils.getDagByIndex(dags, c2.getDagID(), c2.getDagInstNo());

		if (dag1.getSchedParameters().getPriority() > dag2.getSchedParameters().getPriority()) {
			return -1;
		} else if (dag1.getSchedParameters().getPriority() < dag2.getSchedParameters().getPriority()) {
			return 1;
		} else {
			int c = -1;

			if (dag1.hard) {
				if (dag1.id != dag2.id) {
					System.out.println(
							"Utils.compareNodeWithHard(): the IDs of DAG-1 and DAG-2 are not equal, but there should be only one DAG in the system!");
					System.exit(-1);
				}

				c = Integer.compare(c1.getDagInstNo(), c1.getDagInstNo());

				if (c != 0)
					return c;
				else {
					return -Long.compare(c1.priority, c2.priority);
				}

			} else {
				c = -Long.compare(c1.getWCET(), c2.getWCET());

				// if (c != 0)
				return c;
				// else {
				// return Integer.compare(c1.getDagInstNo(), c1.getDagInstNo());
				// }
			}

		}

	}

	/*
	 * Compute the hyperperiod of input DAGs. NOTE: The simulation covers a complete
	 * hyperperiod.
	 */
	public static long getHyperPeriod(List<Long> periods) {

		if (periods == null)
			return 0;

		List<Long> period_copy = new ArrayList<>(periods);
		long lcm = 1;
		int divisor = 2;

		while (true) {
			int counter = 0;
			boolean divisible = false;

			for (int i = 0; i < period_copy.size(); i++) {

				if (period_copy.get(i) == 1) {
					counter++;
				}

				if (period_copy.get(i) % divisor == 0) {
					divisible = true;
					period_copy.set(i, period_copy.get(i) / divisor);
				}
			}

			if (divisible) {
				lcm = lcm * divisor;
			} else {
				divisor++;
			}

			if (counter == period_copy.size()) {
				return lcm;
			}
		}
	}

	public static DirectedAcyclicGraph getDagByIndex(List<DirectedAcyclicGraph> dags, int id, int instanceID) {
		for (DirectedAcyclicGraph dag : dags)
			if (dag.id == id && dag.instanceNo == instanceID)
				return dag;

		return null;
	}

	public static List<DirectedAcyclicGraph> deepCopy(List<DirectedAcyclicGraph> dags) {

		List<DirectedAcyclicGraph> dp = new ArrayList<>();

		for (DirectedAcyclicGraph d : dags)
			dp.add(d.deepCopy());

		return dp;
	}

	public static void writeResult(String filename, String result) {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new FileWriter(new File(filename), false));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		writer.println(result);
		writer.close();
	}


}
