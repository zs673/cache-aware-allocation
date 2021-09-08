package uk.ac.york.mocha.simulator.dag;

import java.io.Serializable;

public class ExecutionBlock implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7639841212144404625L;
	public int id;
	public long width;
	public long height;

	public ExecutionBlock(int id, long width, long height) {
		this.width = width;
		this.height = height;
		this.id = id;
	}

	@Override
	public String toString() {
		return "EB " + id + ": Width " + width + " Height " + height;
	}
}
