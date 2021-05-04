package uk.ac.york.mocha.simulator.resultAnalyzer;

import java.util.ArrayList;
import java.util.List;

import uk.ac.york.mocha.simulator.dag.DirectedAcyclicGraph;

public class ResultPerMethod {
	

	
	List<DirectedAcyclicGraph> dags;
	Long[] cachePerformance;
	
	List<List<List<Double>>> results = new ArrayList<>();
	
	public ResultPerMethod(List<DirectedAcyclicGraph> dags, Long[] cachePerformance) {
		this.dags = dags;
		this.cachePerformance = cachePerformance;
	}

	
	public void getResults() {
		
	}
	

	
	
}
