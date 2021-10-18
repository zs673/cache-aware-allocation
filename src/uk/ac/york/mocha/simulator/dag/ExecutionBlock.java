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

	public long start;
	public long end;

	public ExecutionBlock(int id, long width, long height, long start) {

		this.width = width;
		this.height = height;

		this.start = start;
		this.end = this.start + this.width;

		this.id = id;
	}



	@Override
	public String toString() {
		return "EB_" + id + "   Width: " + width + " Height: " + height + " Start: " + start + " End: " + end;
	}
}
