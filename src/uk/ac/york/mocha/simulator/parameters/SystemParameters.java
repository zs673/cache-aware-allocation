package uk.ac.york.mocha.simulator.parameters;

public class SystemParameters {

	/**********************************************************************
	 ************************** Cache Hierarchy ***************************
	 **********************************************************************/
	public static final int cacheLevel = 3;
	public static final int Level2procNum = 4;
	
	/**********************************************************************
	 **************************** DAG Structure ***************************
	 **********************************************************************/
	/* parameters for NFJ DAGs */
	public static final int depth = 5;
	public static final double fork_prob = 0.3;
	public static final double join_prob = 0.8;
	public static final int fork_max = 2;
	public static final int fork_min = 4;

	/**********************************************************************
	 **************************** Recency table ***************************
	 **********************************************************************/
	/*
	 * Recency Table Type
	 */
	public static enum RecencyType {
		ORDER, TIME
	};

	/*
	 * Recency Table by order
	 */
	public static final int[] costFactorMIN = { 30, 50, 60 };
	public static final int[] costFactorMAX = { 50, 60, 70 };
	public static final int[] recencyDepth = { 6, 20, 50 };

	/*
	 * Recency Table by time
	 */
	public static final long v1 = 0;
	public static final long v2 = 20000;
	public static final long v3 = 500000;
	public static final long v4 = 5000000;

	public static final double delta1 = 0.3;
	public static final double delta2 = 0.5;
	public static final double delta3 = 0.8;
	public static final double delta4 = 1.0;

	/**********************************************************************
	 ************************** System Generator **************************
	 **********************************************************************/

	public final static int MAX_PRIORITY = 1000;

	/*
	 * Harmonic periods
	 */
	public final static int MAX_PERIOD = 1440;
	public final static int MIN_PERIOD = 1;

	/*
	 * Non-harmonic periods
	 */
	public final static int minT = 100;
	public final static int maxT = 1000;

}
