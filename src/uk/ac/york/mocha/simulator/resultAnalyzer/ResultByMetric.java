package uk.ac.york.mocha.simulator.resultAnalyzer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ResultByMetric {

	DecimalFormat df = new DecimalFormat("#.##");

	List<List<Double>> resultsPerMetric = new ArrayList<>();

	List<List<Double>> compare = new ArrayList<>();

	int metricIndex;

	public ResultByMetric(List<ResultByMethod> valuePerMethod, int metricIndex) {
		this.metricIndex = metricIndex;
		getResults(valuePerMethod);
	}

	public void getResults(List<ResultByMethod> resultByMethod) {

		for (int j = 0; j < resultByMethod.size(); j++) {
			List<List<Double>> m = resultByMethod.get(j).resultsPerMethod;
			resultsPerMetric.add(m.get(metricIndex));
		}

		for (int j = 0; j < resultsPerMetric.size() - 1; j++) {
			List<Double> m1 = resultsPerMetric.get(j);
			List<Double> m2 = resultsPerMetric.get(j + 1);

			List<Double> compareTwoMethod = new ArrayList<>();
			for (int k = 0; k < m1.size(); k++) {
				double reducePercent = ((double) (m1.get(k) - m2.get(k))) / (double) m1.get(k);
				compareTwoMethod.add(reducePercent);
			}
			compare.add(compareTwoMethod);
		}

	}

}
