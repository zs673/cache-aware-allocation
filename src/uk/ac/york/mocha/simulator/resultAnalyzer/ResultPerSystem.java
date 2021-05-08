package uk.ac.york.mocha.simulator.resultAnalyzer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.Pair;

import uk.ac.york.mocha.simulator.dag.DirectedAcyclicGraph;

public class ResultPerSystem {

	public enum ResultType {
		MAKESPAN, UTIL, DURATION
	}

	DecimalFormat df = new DecimalFormat("#.##");

	List<List<DirectedAcyclicGraph>> allMethods;

	long[][] maxValues;

	List<ResultByMethod> resultsPerMethod;
	List<ResultByMetric> resultsPerMetric;

	List<String> resultsPerMetric_String;

	public ResultPerSystem(List<List<DirectedAcyclicGraph>> allMethods, List<long[]> cachePerformance) {
		this.allMethods = new ArrayList<>(allMethods);

		maxValues = getMaxValues(allMethods);

		this.resultsPerMethod = new ArrayList<>();

		for (int i = 0; i < allMethods.size(); i++) {
			resultsPerMethod.add(new ResultByMethod(allMethods.get(i), cachePerformance.get(i), maxValues));
		}

		resultsPerMetric = new ArrayList<>();

		for (int i = 0; i < ResultType.values().length; i++) {
			resultsPerMetric.add(new ResultByMetric(resultsPerMethod, i));
		}
		
		for (int i = 0; i < ResultType.values().length; i++) {
			Pair<StringBuilder, StringBuilder> p = stringBuilding(resultsPerMetric.get(i));
			
		}
	}

	public Pair<StringBuilder, StringBuilder> stringBuilding(ResultByMetric result) {

		StringBuilder builder = new StringBuilder();
		StringBuilder builder_Speedup = new StringBuilder();

		/*
		 * Add noralmised durations to duration buffer.
		 */
		for (List<Double> oneMethod : result.resultsPerMetric) {
			oneMethod.stream().forEach(c -> {
				builder.append(c + ",");
			});
			builder.append("\n");
		}

		/*
		 * Compute Speed up.
		 */
		for (int k = 0; k < result.resultsPerMetric.size() - 1; k++) {
			List<Double> m1 = result.resultsPerMetric.get(k);
			List<Double> m2 = result.resultsPerMetric.get(k + 1);

			for (int i = 0; i < m1.size(); i++) {
				double reducePercent = ((double) (m1.get(i) - m2.get(i)))
						/ (double) m1.get(i);

				builder_Speedup.append(df.format(reducePercent) + ",");
			}
		}
		builder_Speedup.append("\n");

		return new Pair<StringBuilder, StringBuilder>(builder, builder_Speedup);
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
					List<Long> sumC = allMethods.stream().map(
							c -> c.get(index).getFlatNodes().stream().mapToLong(c1 -> c1.finishAt - c1.start).sum())
							.collect(Collectors.toList());
					long sumCMax = sumC.stream().mapToLong(c -> c).max().getAsLong();

					if (maxValues[1][dagID] < sumCMax)
						maxValues[1][dagID] = sumCMax;
					break;
				case 2:
					List<Long> duration = allMethods.stream()
							.map(c -> c.get(index).finishTime - c.get(index).releaseTime).collect(Collectors.toList());
					long durationMax = duration.stream().mapToLong(c -> c).max().getAsLong();

					if (maxValues[2][dagID] < durationMax)
						maxValues[2][dagID] = durationMax;

					break;

				default:
					break;
				}
			}

		}

		return maxValues;
	}

}
