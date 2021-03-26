package uk.ac.york.mocha.simulator.simulator;

import java.util.List;

import uk.ac.york.mocha.simulator.dag.DirectedAcyclicGraph;

public class Utils {

	public static DirectedAcyclicGraph getDagByIndex(List<DirectedAcyclicGraph> dags, int id, int instanceID) {
		for (DirectedAcyclicGraph dag : dags)
			if (dag.id == id && dag.instanceNo == instanceID)
				return dag;

		return null;
	}

}
