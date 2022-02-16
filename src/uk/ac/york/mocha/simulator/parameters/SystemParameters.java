package uk.ac.york.mocha.simulator.parameters;

public class SystemParameters {

	/**********************************************************************
	 ********************** Simualtor Configuration ***********************
	 **********************************************************************/
	public enum SimuType {
		CLOCK_LEVEL, NODE_LEVEL
	};

	public enum Hardware {
		PROC_ONLY, PROC_CACHE
	};

	public enum Allocation {
		SIMPLE, RANDOM, BEST_FIT, FIRST_FIT, WORST_FIT, WORST_FIT_OUR, CACHE_AWARE, CACHE_AWARE_NEW, CACHE_AWARE_ROBUST,
		OFFLINE_CACHE_AWARE, CACHE_AWARE_OUR,

	};

	/**********************************************************************
	 *********************** Experimental Settings ************************
	 **********************************************************************/
	public enum ExpName {
		oneDAG, taskNum, recency_fault, recency_fault_util, recency_pattern, offline, offline_multi, methods, periods,
		tasks, sysUtil, sysUtilOneDAG, util_compare
	}

	public static int NoS = 1000;
	public static final boolean printSim = false;
	public static final boolean printGen = true;

	/**********************************************************************
	 ************************** Number of cores ***************************
	 **********************************************************************/
	public static int coreNum = 8;

	/**********************************************************************
	 ************************** Cache Hierarchy ***************************
	 **********************************************************************/
	public static final int cacheLevel = 3;
	public static final int Level2CoreNum = 4;

	/**********************************************************************
	 **************************** DAG Structure ***************************
	 **********************************************************************/

	/* parameters for generic DAGs */
	public static final double connectProb = 0.2;
	public static final int minLayer = 5;
	public static final int maxLayer = 10;
	public static int minParal = 2;
	public static int maxParal = 10;

	/* parameters for NFJ DAGs */
	public static final int depth = 5;
	public static final double fork_prob = 0.3;
	public static final double join_prob = 0.8;
	public static final int fork_max = 2;
	public static final int fork_min = 4;
	public static final int fan_in = 3;

	/* Error Range */
	public static double err_median = 0.0;
	public static int err_range = 100;

	/* Recency fault rate */
	public static int fault_rate = 5;
	public static int fault_range = 10;
	public static int fault_median = 5;

	/**********************************************************************
	 **************************** Recency table ***************************
	 **********************************************************************/
	/*
	 * Recency Table Type
	 */
	public static enum RecencyType {
		ORDER, TIME_DEFAULT, TIME_CURVE, TIME_STEP
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
	public static final long v2 = 10000;
	public static final long v3 = 100000;
	public static final long v4 = 500000;

	public static final double delta1 = 0.3;
	public static final double delta2 = 0.5;
	public static final double delta3 = 0.8;
	public static final double delta4 = 1.0;

	/**********************************************************************
	 ************************** System Generator **************************
	 **********************************************************************/

	public final static int MAX_PRIORITY = 1000;

	public static double utilPerTask = 2.0;

	/*
	 * Harmonic periods
	 */
	public final static int MAX_PERIOD = 144;
	public final static int MIN_PERIOD = 10;

	/*
	 * Non-harmonic periods
	 */
	public final static int minT = 100;
	public final static int maxT = 1000;

}
