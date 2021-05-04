package uk.ac.york.mocha.simulator.resultAnalyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import uk.ac.york.mocha.simulator.dag.DirectedAcyclicGraph;

public class ResultFactory {

	public enum ResultType {
		MAKESPAN, UTIL, DURATION
	}
	
	List<List<DirectedAcyclicGraph>> allMethods;
	
	long[][] maxValues;
	
	public ResultFactory(List<List<DirectedAcyclicGraph>> allMethods) {
		this.allMethods = new ArrayList<>(allMethods);
		
		maxValues = getMaxValues(allMethods);
	}

	public long[][] getMaxValues(List<List<DirectedAcyclicGraph>> allMethods) {

		assert (allMethods.size() > 0);

		int resultTypeNum = ResultType.values().length;
		int taskNum = allMethods.get(0).stream().mapToInt(c -> c.id).max().getAsInt() + 1;

		long[][] maxValues = new long[resultTypeNum][taskNum];

		for (int i = 0; i < maxValues.length; i++) {
			for (int j = 0; j < maxValues[i].length; j++) {
				maxValues[i][j] = Long.MIN_VALUE;
			}
		}

//		for (int k = 0; k < ResultType.values().length; k++) {

		for (int i = 0; i < allMethods.get(0).size(); i++) {
			final int index = i;
			final int dagID = allMethods.get(0).get(i).id;

			for (int j = 0; j < resultTypeNum; j++) {
				switch (j) {
				case 0:
					List<Long> makespan = allMethods.stream().map(c -> c.get(index).finishTime - c.get(index).startTime)
							.collect(Collectors.toList());
					long makespanMax = makespan.stream().mapToLong(c -> c).max().getAsLong();

					if (maxValues[0][dagID] < makespanMax)
						maxValues[0][dagID] = makespanMax;

					break;
				case 1:
					List<Long> sumC = allMethods.stream()
							.map(c -> c.get(index).getFlatNodes().stream().mapToLong(c1 -> c1.finishAt - c1.start).sum())
							.collect(Collectors.toList());
					long sumCMax = sumC.stream().mapToLong(c -> c).max().getAsLong();

					if (maxValues[1][dagID] < sumCMax)
						maxValues[1][dagID] = sumCMax;
					break;
				case 2:
					List<Long> duration = allMethods.stream().map(c -> c.get(index).finishTime - c.get(index).releaseTime)
							.collect(Collectors.toList());
					long durationMax = duration.stream().mapToLong(c -> c).max().getAsLong();

					if (maxValues[2][dagID] < durationMax)
						maxValues[2][dagID] = durationMax;

					break;

				default:
					break;
				}
			}

		}

//		}

		return maxValues;
	}

}
