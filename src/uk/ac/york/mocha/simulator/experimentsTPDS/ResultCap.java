package uk.ac.york.mocha.simulator.experimentsTPDS;

import java.util.ArrayList;
import java.util.List;

class ResultCap {
	public int NoSched_our = 0;
	public int NoSched_he = 0;
	public int NoSched_seq = 0;

	public List<List<long[]>> inter_delay = new ArrayList<>();
	public List<List<long[]>> intra_delay = new ArrayList<>();
	public List<List<long[]>> response_time = new ArrayList<>();

	public synchronized void addResponseTime(List<long[]> response) {
		response_time.add(response);
	}

	public synchronized void addIntraDelay(List<long[]> delays) {
		intra_delay.add(delays);
	}

	public synchronized void addInterDelay(List<long[]> delays) {
		inter_delay.add(delays);
	}

	public synchronized void incrementOur() {
		NoSched_our++;
	}
	
	public synchronized void incrementSeq() {
		NoSched_seq++;
	}

	public synchronized void incrementHe() {
		NoSched_he++;
	}

}
