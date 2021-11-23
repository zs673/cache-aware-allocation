package uk.ac.york.mocha.simulator.experiments;

import java.util.ArrayList;
import java.util.List;

import uk.ac.york.mocha.simulator.resultAnalyzer.OneSystemResults;

public class ResultCap {

	public List<OneSystemResults> allSys = new ArrayList<>();
	
	public synchronized void add(OneSystemResults res) {
		allSys.add(res);
	}
}
