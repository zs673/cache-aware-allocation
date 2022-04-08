package uk.ac.york.mocha.simulator.schedule;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.Pair;

import uk.ac.york.mocha.simulator.dag.DAG;
import uk.ac.york.mocha.simulator.dag.DAGtoPython;
import uk.ac.york.mocha.simulator.dag.ExecutionBlock;
import uk.ac.york.mocha.simulator.dag.Node;
import uk.ac.york.mocha.simulator.generator.SystemGenerator;
import uk.ac.york.mocha.simulator.parameters.SystemParameters.DagType;

public class FederatedSchedule {

	public static void main(String args[]) {
		int coreNum = 8;
		for (int i = 0; i < 1; i++) {
			SystemGenerator gen = new SystemGenerator(coreNum, 4, true, true, null, i, true, true);
			List<DAG> dags = gen.generatedDAGInstancesInOneHP(-2, -2, null, false, DagType.Random).getFirst();

			List<Long> response_time = new TPDSOurMultiDAG().getResponseTime(dags, coreNum).stream()
					.map(c -> c.best_response_time).collect(Collectors.toList());

			System.out.println(response_time);

			System.out.println("------------------------------------------------------------------------------------");

			List<DAG> dags1 = new ArrayList<>();
			for (int k = 0; k < 4; k++) {
				dags1.add(dags.get(k));
			}

			List<Long> response_time1 = new FederatedSchedule().getResponseTime(dags1, coreNum).stream()
					.map(c -> c.best_response_time).collect(Collectors.toList());
			System.out.println(response_time1);
		}
	}

	List<ExecutionBlock> systemEB = new ArrayList<>();

	public List<InfoCap> getResponseTime(List<DAG> dags, int cores) {

		List<InfoCap> response_time = new ArrayList<>();
		List<ExecutionBlock> system = new ArrayList<>();

		for (DAG d : dags) {
			Pair<Long, List<int[]>> res = DAGtoPython.pharseDAGForPython(d, cores);
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

			InfoCap response = getBestResponseTime(system, d, cores);

			if (response.best_response_time > d.getSchedParameters().getDeadline()) {
				return null;
			}

			response_time.add(response);
		}

		return response_time;
	}

	private InfoCap getBestResponseTime(List<ExecutionBlock> system, DAG d, int totalcore) {
		long best_core = Integer.MAX_VALUE;
		long best_response_time = Long.MAX_VALUE;
		long best_delay = Long.MAX_VALUE;

		List<ExecutionBlock> best_sys = new ArrayList<>();

		for (int i = totalcore; i > 0; i--) {

			List<ExecutionBlock> sysMirror = new ArrayList<>();
			deepCopyEBList(sysMirror, system);

			long delay = addExecutionBlocksForOneDAG(sysMirror, d.getPWDM(i), d.releaseTime, totalcore);

			long response = -1;
			if (i > 1)
				response = delay + DAGtoPython.pharseDAGForPython(d, i).getFirst();
			else
				response = delay + d.getFlatNodes().stream().mapToLong(c -> c.getWCET()).sum();

			if (response <= d.getSchedParameters().getDeadline() && i < best_core) {
				best_core = i;
				best_response_time = response;
				best_delay = delay;
				deepCopyEBList(best_sys, sysMirror);
			}
		}

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
