package uk.ac.york.mocha.simulator.schedule;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.Pair;

import uk.ac.york.mocha.simulator.dag.DAG;
import uk.ac.york.mocha.simulator.dag.DAGtoPython;
import uk.ac.york.mocha.simulator.dag.ExecutionBlock;
import uk.ac.york.mocha.simulator.dag.Node;
import uk.ac.york.mocha.simulator.generator.SystemGenerator;
import uk.ac.york.mocha.simulator.parameters.SystemParameters.DagType;

public class TPDSOurMultiDAG {

	List<ExecutionBlock> systemEB = new ArrayList<>();

	List<List<Long>> compuationCost = new ArrayList<>();

	public static void main(String args[]) {
		int coreNum = 8;

		for (int i = 0; i < 1000; i++) {
			SystemGenerator gen = new SystemGenerator(8, 5, true, true, null, i, true, true);
			List<DAG> dags = gen.generatedDAGInstancesInOneHP(-1, -1, null, false, DagType.Random).getFirst();

			dags.sort(
					(c1, c2) -> Long.compare(c1.getSchedParameters().getPeriod(), c2.getSchedParameters().getPeriod()));

			new TPDSOurMultiDAG().getResponseTime(dags, coreNum);
			System.out.println("------------------------------------------------------------------------------------");
		}

	}

	public List<InfoCap> getResponseTime(List<DAG> dags, int cores) {

		for (int i = 0; i < 5; i++) {
			compuationCost.add(new ArrayList<>());
		}

		List<InfoCap> response_time = new ArrayList<>();
		List<ExecutionBlock> system = new ArrayList<>();

		for (DAG d : dags) {
			Pair<long[], List<int[]>> res = DAGtoPython.pharseDAGForPython(d, cores);
			List<int[]> prio = res.getSecond();

			for (Node n : d.getFlatNodes()) {
				int id = n.getId();

				for (int i = 0; i < prio.size(); i++) {
					if (prio.get(i)[0] - 1 == id) {
						n.priority = prio.get(i)[1];
						break;
					}
				}
			}

			long t5_1 = System.nanoTime();
			InfoCap response = getBestResponseTime(system, d, cores);
			long t5_2 = System.nanoTime();
			compuationCost.get(4).add(t5_2 - t5_1);

//			if (response.best_response_time > d.getSchedParameters().getDeadline()) {
//				return null;
//			}

			response_time.add(response);
		}

		for (int i = 0; i < compuationCost.size(); i++) {
			String fileName = "time " + (i + 1) + ".txt";
			String time = "";
			for (int j = 0; j < compuationCost.get(i).size(); j++) {
				time += compuationCost.get(i).get(j) + "\n";
			}

			writeResult(fileName, time);
		}

		return response_time;
	}

	public static void writeResult(String filename, String result) {

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new FileWriter(new File(filename), true));
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

	private InfoCap getBestResponseTime(List<ExecutionBlock> system, DAG d, int totalcore) {

		long best_core = -1;
		long best_response_time = Long.MAX_VALUE;
		long best_delay = Long.MAX_VALUE;

		List<ExecutionBlock> best_sys = new ArrayList<>();

		for (int i = totalcore; i > 0; i--) {

			List<ExecutionBlock> sysMirror = new ArrayList<>();
			deepCopyEBList(sysMirror, system);

			long t3_1 = System.nanoTime();
			List<ExecutionBlock> ebs_DAG = d.getPWDM(i);
			long t3_2 = System.nanoTime();

			compuationCost.get(2).add(t3_2 - t3_1);

			long t4_1 = System.nanoTime();
			long delay = addExecutionBlocksForOneDAG(sysMirror, ebs_DAG, d.releaseTime, totalcore);
			long t4_2 = System.nanoTime();

			compuationCost.get(3).add(t4_2 - t4_1);

			long response = -1;
			if (i > 1) {
				long[] times = DAGtoPython.pharseDAGForPython(d, i).getFirst();
				response = times[0];
				compuationCost.get(0).add(times[1]);
				compuationCost.get(1).add(times[2]);
			} else {
				response = delay + d.getFlatNodes().stream().mapToLong(c -> c.getWCET()).sum();
			}

			if (response < best_response_time) {
				best_core = i;
				best_response_time = response;
				best_delay = delay;
				deepCopyEBList(best_sys, sysMirror);
			}
		}

//		System.out.println("best core: " + best_core);

		deepCopyEBList(system, best_sys);

		return new InfoCap(best_core, best_response_time, best_delay, best_response_time - best_delay);
	}

	private long addExecutionBlocksForOneDAG(List<ExecutionBlock> system, List<ExecutionBlock> ebs, long startDAG,
			int coreNum) {

		if (system.size() == 0) {

			deepCopyEBList(system, ebs);
			addEmptyBlocksAndCheck(system);

			return 0;

		} else {

			long totalDelay = 0;

			for (ExecutionBlock eb : ebs) {

				long start = startDAG + eb.start + totalDelay;

				long delay = addEBtoSystem(eb, start, system, coreNum);

				totalDelay += delay;
			}

			return totalDelay;
		}
	}

	private long addEBtoSystem(ExecutionBlock eb, long start, List<ExecutionBlock> system, int core) {

		long width = eb.width;

		int index = 0;

		long delay = 0;

		boolean found = false;

		/***************************************************************
		 * Find the first EB in the system for add
		 ***************************************************************/
		for (; index < system.size(); index++) {
			ExecutionBlock sysEB = system.get(index);
			if (sysEB.start <= start && sysEB.end > start) {
				found = true;
				break;
			}
		}

		if (found) {
			/****************************************************************************************
			 * If the target EB starts in the middle of the system EB, break the system EB
			 * into two
			 ****************************************************************************************/
			if (start != system.get(index).start) {
				long oldWidth = system.get(index).width;
				long oldEnd = system.get(index).end;

				/* shrink the current system execution block */
				system.get(index).width = start - system.get(index).start;
				system.get(index).end = system.get(index).start + system.get(index).width;

				/* add new system execution block for add */
				ExecutionBlock newSysEB = new ExecutionBlock(-1, oldWidth - system.get(index).width,
						system.get(index).height, system.get(index).end);

				if (system.get(index).end + oldWidth - system.get(index).width != oldEnd) {
					System.err.println(
							"SemiWorkConversing.addEBtoSystem(): the new execution block does not end as expected.");
					System.exit(-1);
				}

				system.add(index + 1, newSysEB);

				/* point to the first system EB for add. */
				index++;
			}

			/****************************
			 * Perform the add operator
			 ****************************/
			for (; index < system.size(); index++) {
				ExecutionBlock sysEB = system.get(index);
				long capacity = sysEB.width;

				if (sysEB.height + eb.height <= core) {

					/*
					 * If this happens, it means the target EB can be completely added to the
					 * existing EB.
					 */
					if (capacity > width) {
						long newCap = capacity - width;
						long newHeight = sysEB.height;

						sysEB.height += eb.height;
						sysEB.width = capacity - newCap;
						sysEB.end = sysEB.start + sysEB.width;

						ExecutionBlock newSysEB = new ExecutionBlock(-1, newCap, newHeight, sysEB.end);

						if (index == system.size() - 1)
							system.add(newSysEB);
						else
							system.add(index + 1, newSysEB);

						width = 0;
						break;

					} else {
						width = width - capacity;
						sysEB.height += eb.height;
					}

				}
				/*
				 * This indicates the system cannot add the target EB on the current system EB
				 */
				else {
					delay += capacity;
				}
			}

			/*
			 * If the target EB is not fully added after iterating all system EBs, we create
			 * new system EB directly.
			 */
			if (width > 0) {
				ExecutionBlock newSysEB = new ExecutionBlock(-1, width, eb.height, system.get(system.size() - 1).end);
				system.add(newSysEB);

				width = 0;
			}
		}
		/*********************************************************************************
		 * If the system EB is not found, the target EB starts when there is no one
		 * executing
		 *********************************************************************************/
		else {
			ExecutionBlock newEB = new ExecutionBlock(-1, eb.width, eb.height, start);
			system.add(newEB);
		}

		addEmptyBlocksAndCheck(system);

		return delay;
	}

	private List<ExecutionBlock> deepCopyEBList(List<ExecutionBlock> newList, List<ExecutionBlock> oldList) {

		newList.clear();

		for (ExecutionBlock eb : oldList) {
			ExecutionBlock newEB = new ExecutionBlock(eb.id, eb.width, eb.height, eb.start);
			newList.add(newEB);
		}

		return newList;
	}

	private void addEmptyBlocksAndCheck(List<ExecutionBlock> sys) {
		for (int i = 0; i < sys.size() - 1; i++) {
			ExecutionBlock eb1 = sys.get(i);
			ExecutionBlock eb2 = sys.get(i + 1);

			if (eb1.end != eb2.start) {
				if (eb1.end > eb2.start) {
					System.out.println("SemiWorkConversing.addEmptyBlocksAndCheck()");
					System.exit(-1);
				}
				ExecutionBlock eb = new ExecutionBlock(-1, eb2.start - eb1.end, 0, eb1.end);
				sys.add(i + 1, eb);
			}
		}

	}

}