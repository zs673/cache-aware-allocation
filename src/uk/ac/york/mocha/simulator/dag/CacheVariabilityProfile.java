package uk.ac.york.mocha.simulator.dag;

import java.util.Random;

public class CacheVariabilityProfile {

	private int median;
	private int range;
	private int upper_bound;
	private int lower_bound;
	
	Random rng;

	public CacheVariabilityProfile(int median, int range, int seed) {
		this.median = median;
		this.range = range;
		this.lower_bound = (median - range) >= 0 ? median - range : 0;
		this.upper_bound = median + range <= 100 ? median + range : 100;
		
		this.rng = new Random(seed);
	}

	public double getMedian() {
		return (double) median / (double) 100;
	}

	public double getRange() {
		return (double) range / (double) 100;
	}
	
	public double getVary() {
		
		long std = upper_bound - median;
		if (std == 0)
			return 0;

		double vary = median + Math.round((double) rng.nextGaussian() * (double) std);
	
		vary = vary < 1 ? 1 : vary;
		
		
		return vary;
	}

	public static void main(String args[]) {
		
		CacheVariabilityProfile cvp = new CacheVariabilityProfile(100, 50, 1000);
		
		for(int i=0; i<1000;i++) {
			
		}
	}

}
