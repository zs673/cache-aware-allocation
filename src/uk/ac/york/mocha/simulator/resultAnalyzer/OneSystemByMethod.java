package uk.ac.york.mocha.simulator.resultAnalyzer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import uk.ac.york.mocha.simulator.entity.DirectedAcyclicGraph;
import uk.ac.york.mocha.simulator.resultAnalyzer.AllSystemsResults.ResultType;

public class OneSystemByMethod {

	public List<DirectedAcyclicGraph> dags;
	private long[][] maxValues;

	private DecimalFormat df = new DecimalFormat("#.###");

	public List<List<Double>> resultsPerMethod = new ArrayList<>();
	double[] cachePerformance;

	public OneSystemByMethod(List<DirectedAcyclicGraph> dags, double[] cachePerformance, long[][] maxValues) {
		this.dags = new ArrayList<>(dags);
		this.cachePerformance = cachePerformance;
		this.maxValues = maxValues;

		getResults();
	}

	public void getResults() {

		for (int i = 0; i < ResultType.values().length; i++) {
			final int index = i;
			switch (index) {
				case 0:
					List<Double> makespan = dags.stream().map(c -> Double.parseDouble(
							df.format(((double) (c.finishTime - c.startTime)) / (double) maxValues[index][c.id])))
							// df.format(((double) (c.finishTime - c.startTime)))))
							.collect(Collectors.toList());

					resultsPerMethod.add(makespan);
					break;

				case 1:
					/*
					 * Here we compute the real utilisation of the task
					 */
					List<Double> util = dags.stream().map(c -> Double.parseDouble(
							df.format((((double) c.getFlatNodes().stream().mapToLong(c1 -> c1.finishAt - c1.start).sum()
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
				case 3:
					List<Double> makespan_abs = dags.stream().map(c -> Double.parseDouble(
							// df.format(((double) (c.finishTime - c.startTime)) / (double)
							// maxValues[index][c.id])))
							df.format(((double) (c.finishTime - c.startTime))))).collect(Collectors.toList());

					resultsPerMethod.add(makespan_abs);
					break;
				case 4:
					List<Double> SLR_D = dags.stream().map(c -> Double.parseDouble(
							df.format(((double) (c.SLR))))).collect(Collectors.toList());

					resultsPerMethod.add(SLR_D);
					break;
				case 5:
					List<Double> Speed_D = dags.stream().map(c -> Double.parseDouble(
							df.format(((double) (c.speedup))))).collect(Collectors.toList());

					resultsPerMethod.add(Speed_D);
					break;

				case 6:
					List<Double> node_cnt = dags.stream().map(c -> Double.parseDouble(
							df.format((((double) c.getFlatNodes().stream().mapToLong(c1 -> c1.delayCnt > 0 ? 1 : 0).sum()
									)
									)))

					).collect(Collectors.toList());

					resultsPerMethod.add(node_cnt);
				
				case 7:
					List<Double> defer_cnt = dags.stream().map(c -> Double.parseDouble(
							df.format((((double) c.getFlatNodes().stream().mapToLong(c1 -> c1.delayCnt).sum()
									)
									)))).collect(Collectors.toList());

					resultsPerMethod.add(defer_cnt);
				
				case 8:
				List<Double> max_defer_cnt = dags.stream().map(c -> Double.parseDouble(
					df.format(((double) c.getFlatNodes().stream().mapToLong(c1 -> c1.delayCnt).max().orElse(0)
							)))).collect(Collectors.toList());

					resultsPerMethod.add(max_defer_cnt);
				
				case 9:
				List<Double> node_size = dags.stream().map(c -> Double.parseDouble(
						df.format(((double) c.getFlatNodes().size()
								)))).collect(Collectors.toList());
						resultsPerMethod.add(node_size);
				default:
					break;
			}
		}

	}

}
