package uk.ac.york.mocha.simulator.dag;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Random;

public class CacheVariabilityProfile implements Serializable {
	
	private static final long serialVersionUID = -6902741116934537381L;

	static DecimalFormat df = new DecimalFormat("#.###");

	private double median;
	private double range;

	Random rng;
	int seed;
	
	Node n;

	public CacheVariabilityProfile(Node n, double median, double range, int seed) {
		this.n = n;
		
		this.median = median;
		this.range = range;

		this.seed = seed;
		this.rng = new Random(seed);
	}

	public double getMedian() {
		return median;
	}

	public double getRange() {
		return range;
	}
	
	public int getSeed() {
		return seed;
	}

	public double getVary() {

		double ran = rng.nextGaussian();
		double vary = median + (ran * range);

		double out = Double.parseDouble(df.format(vary));
		
		if(out >= 1)
			out = 1;
		
		if(out <= -1)
			out = -1;
		
		return out;
	}

	public static void main(String args[]) {

		CacheVariabilityProfile cvp = new CacheVariabilityProfile(null, 0, 1.0/3.0, 1000);

		for (int i = 0; i < 10; i++) {
			System.out.println(cvp.getVary());
		}
	}

}
