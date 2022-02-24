package uk.ac.york.mocha.simulator.dag;

public class RecencyProfileReal {

	String job_name;
	int job_id;

	double WCET;

	double[] breaks;
	double[] slopes;
	double[] intercepts;

	public RecencyProfileReal(String job_name, int job_id, double WCET, double[] breaks, double[] slopes, double[] intercepts) {
		this.job_name = job_name;
		this.job_id = job_id;

		this.WCET = WCET;

		this.breaks = breaks;
		this.slopes = slopes;
		this.intercepts = intercepts;

	}


	/**
	 * Calculate the speed-up by reading the CRP table Precentage of execution
	 * time speedup
	 */
	public double getET(double rd) {

		// Find linear piece where rd fits
		int c = 0;
		while (c < breaks.length && rd < breaks[c])
			++c;

		// Fit rd to the linear pc
		double crp = slopes[c - 1] * rd + intercepts[c - 1];

		// Normalize crp in interval [CPR_MIN, 1.0]
		if (crp > 1.0) {
			return 1.0;
		}
		if (crp <= 0.01) {
			return 0.01;
		}

		return crp;
	}

}
