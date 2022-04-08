package uk.ac.york.mocha.simulator.schedule;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import uk.ac.york.mocha.simulator.dag.DAG;
import uk.ac.york.mocha.simulator.dag.DAGtoPython;
import uk.ac.york.mocha.simulator.generator.SystemGenerator;
import uk.ac.york.mocha.simulator.parameters.SystemParameters.DagType;

public class RTSSOurSingleDAG {

	public static void main(String args[]) {
		int coreNum = 16;
		for (int i = 0; i < 1; i++) {
			SystemGenerator gen = new SystemGenerator(coreNum, 2, true, true, null, i, true, true);
			List<DAG> dags = gen.generatedDAGInstancesInOneHP(-2, -2, null, false, DagType.Random).getFirst();

			List<InfoCap> response_time = new RTSSOurSingleDAG().getResponseTime(dags, coreNum);

			System.out.println(dags.stream().map(c -> c.releaseTime).collect(Collectors.toList()));
			System.out.println(response_time.stream().map(c -> c.best_response_time).collect(Collectors.toList()));
			System.out.println(response_time.stream().map(c -> c.best_inter).collect(Collectors.toList()));
			System.out.println(response_time.stream().map(c -> c.best_intra).collect(Collectors.toList()));

			System.out.println("------------------------------------------------------------------------------------");

			List<DAG> dags1 = new ArrayList<>();
			for (int k = 0; k < 2; k++) {
				dags1.add(dags.get(k));
			}
			List<InfoCap> response_time1 = new TPDSHe().getResponseTime(dags1, coreNum);
			System.out.println(response_time1.stream().map(c -> c.best_response_time).collect(Collectors.toList()));
		}

	}

	public List<InfoCap> getResponseTime(List<DAG> dags, int cores) {

		return null;
//		List<InfoCap> response_time = new ArrayList<>();
//
//		long systemTime = 0;
//		for (DAG d : dags) {
//
//			long release = d.releaseTime;
//			long response = -1;
//
//			long makespan = DAGtoPython.pharseDAGForPython(d, cores).getFirst();
//
//			if (release >= systemTime) {
//
//				response = release + makespan;
//				systemTime = response;
//			} else {
//				response = systemTime + makespan;
//				systemTime = response;
//			}
//
//			response = response - release;
//			InfoCap res = new InfoCap(cores, response, response - makespan, makespan);
//
//			response_time.add(res);
//		}
//
//		return response_time;
	}

}
