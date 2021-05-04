package uk.ac.york.mocha.simulator.resultAnalyzer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import uk.ac.york.mocha.simulator.dag.DirectedAcyclicGraph;

public class ResultPerSystemByMethod {

	List<DirectedAcyclicGraph> dags;
	long[][] maxValues;

	DecimalFormat df = new DecimalFormat("#.##");

	List<List<Double>> resultsPerMethod = new ArrayList<>();
	long[] cachePerformance;

	public ResultPerSystemByMethod(List<DirectedAcyclicGraph> dags, long[] cachePerformance, long[][] maxValues) {
		this.dags = new ArrayList<>(dags);
		this.cachePerformance = cachePerformance;
		this.maxValues = maxValues;
		
		getResults();
	}

	public void getResults() {

		for (int i = 0; i < ResultPerSystem.ResultType.values().length; i++) {
			final int index = i;
			switch (index) {
			case 0:
				List<Double> makespan = dags.stream()
						.map(c -> Double.parseDouble(
								df.format(((double) (c.finishTime - c.startTime)) / (double) maxValues[index][c.id])))
						.collect(Collectors.toList());

				resultsPerMethod.add(makespan);
				break;

			case 1:
				/*
				 * Here we compute the real utilisation of the task
				 */
				List<Double> util = dags.stream()
						.map(c -> Double.parseDouble(df.format(
								(((double) c.getFlatNodes().stream().mapToLong(c1 -> c1.finishAt - c1.start).sum()
										/ (double) c.getSchedParameters().getPeriod())
										/ ((double) maxValues[index][c.id] / (double) c.getSchedParameters().getPeriod()))))

						).collect(Collectors.toList());

				resultsPerMethod.add(util);

				break;
			case 2:

				List<Double> finish = dags.stream()
						.map(c -> Double.parseDouble(
								df.format(((double) c.finishTime - c.startTime) / (double) maxValues[index][c.id])))
						.collect(Collectors.toList());
				
				resultsPerMethod.add(finish);
				break;
			default:
				break;
			}
		}

	}

}
