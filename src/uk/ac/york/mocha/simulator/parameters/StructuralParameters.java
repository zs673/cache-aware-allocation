package uk.ac.york.mocha.simulator.parameters;

import java.io.Serializable;
import java.util.Random;

public class StructuralParameters implements Serializable {

	private static final long serialVersionUID = -4517859999449660269L;

	/* DAGType */
	public enum NodeType { // TODO: to support NFJ DAGs
		NORMAL, NFJ
	}

	/* parameters for normal DAGs */
	private final int parallelism;
	private final double connect_prob;
	private final int layers;

	private final int seed;
	private Random rng;

	/* DAG structure Parameters */
	public StructuralParameters(int maxLayer, int minLayer, int parallelism, double connect_prob, int seed) {

		if (maxLayer - minLayer <= 2) {
			System.err.println("maxLayer-minLayer <= 2 !!");
			System.err.println("maxLayer: " + maxLayer + "    minLayer: " + minLayer);
			System.exit(-1);
		}

		this.parallelism = parallelism;
		this.connect_prob = connect_prob;

		this.seed = seed;
		rng = new Random(seed);
		this.layers = rng.nextInt(maxLayer - minLayer) + minLayer;
	}

	/* DAG structure Parameters with fixed Layer */
	public StructuralParameters(int layer, int parallelism, double connect_prob, int seed) {

		this.parallelism = parallelism;
		this.connect_prob = connect_prob;

		this.seed = seed;
		rng = new Random(seed);
		this.layers = layer;
	}

	public int getParallelism() {
		return parallelism;
	}

	public double getConnect_prob() {
		return connect_prob;
	}

	public int getDepth() {
		return SystemParameters.depth;
	}

	public double getFork_prob() {
		return SystemParameters.fork_prob;
	}

	public double getJoin_prob() {
		return SystemParameters.join_prob;
	}

	public int getFork_max() {
		return SystemParameters.fork_max;
	}

	public int getFork_min() {
		return SystemParameters.fork_min;
	}

	public int getLayers() {
		return layers;
	}

	public int getSeed() {
		return seed;
	}

}
