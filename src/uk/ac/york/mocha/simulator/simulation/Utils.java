package uk.ac.york.mocha.simulator.simulation;

import uk.ac.york.mocha.simulator.dag.DirectedAcyclicGraph;

public class Utils {

	public static DirectedAcyclicGraph getDagByIndex(int id, int instanceID) {
		for (DirectedAcyclicGraph dag : Simualtor.dags)
			if (dag.id == id && dag.instanceNo == instanceID)
				return dag;

		return null;
	}

}
