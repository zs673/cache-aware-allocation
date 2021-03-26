package uk.ac.york.mocha.simulator.experiments;

import java.util.List;

import uk.ac.york.mocha.simulator.dag.DirectedAcyclicGraph;
import uk.ac.york.mocha.simulator.generator.SystemGenerator;
import uk.ac.york.mocha.simulator.simulator.Simualtor;
import uk.ac.york.mocha.simulator.simulator.Simualtor.Allocation;
import uk.ac.york.mocha.simulator.simulator.Simualtor.Hardware;
import uk.ac.york.mocha.simulator.simulator.Simualtor.SimuType;

public class SimpleAllocationCompare {

	public static void main(String args[]) {

		int cores = 8;

		SystemGenerator gen = new SystemGenerator(100, 1000, cores, 2, true, 1000);
		List<DirectedAcyclicGraph> dags = gen.generatedDAGInstancesInOneHP(-1);
		
		Simualtor sim0 = new Simualtor(SimuType.CLOCK_LEVEL, Hardware.PROC_ONLY, Allocation.LOAD_BALANCE, dags, cores,
				1000);
		String sim0_res = sim0.simulate();
		System.out.println("\n\n");
//		System.out.println(sim0_res);

		System.out.println("\n\n\n\n\n\n");

		Simualtor sim = new Simualtor(SimuType.CLOCK_LEVEL, Hardware.PROC_CACHE, Allocation.LOAD_BALANCE, dags, cores,
				1000);
		String sim_res = sim.simulate();
		System.out.println("\n\n");
//		System.out.println(sim_res);

		System.out.println("\n\n\n\n\n\n");

		Simualtor sim1 = new Simualtor(SimuType.CLOCK_LEVEL, Hardware.PROC_CACHE, Allocation.CACHE_AWARE, dags, cores,
				1000);
		String sim1_res = sim1.simulate();
		System.out.println("\n\n");
//		System.out.println(sim1_res);
	}
}
