package uk.ac.york.mocha.simulator.dag;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import uk.ac.york.mocha.simulator.parameters.SystemParameters;

public class Node implements Serializable {

	private static final long serialVersionUID = -6902741116934537381L;

	/* NodeType */
	public enum NodeType {
		SOURCE, SINK, NORMAL
	}

	/*
	 * Node identifiers: id - node ID; dagID - the ID of its DAG; daginstNo - the
	 * instance Number of its DAG.
	 */
	private final int id;
	private final int dagID;
	private int dagInstNo = Integer.MIN_VALUE;

	/*
	 * The layer of the node in the DAG strcuture
	 */
	private final int layer;

	/*
	 * The type of the node, can be SOURCE, SINK, or Normal
	 */
	private final NodeType type;

	/*
	 * The worst-case execution time of the node
	 */
	private long WCET;
	// private long WCETinRange;

	/*
	 * A list of successors/predecessors of the node.
	 */
	private List<Node> successors;
	private List<Node> predecessors;

	/*
	 * The priority of the node
	 */
	public int priority = -1;

	/*
	 * Is the node belongs to the critcal path of the DAG.
	 */
	public boolean isCritical = false;

	/*
	 * Does the node has faults in execution time?
	 */
	public boolean hasFaults = false;

	/*
	 * Simulation parameters
	 */
	public long start = -1;
	public boolean finish = false;
	public long finishAt = -1;

	public int partition = -1;
	public int affinity = -1;

	public int delayed = -1;
	public long expectedET = -1;

	/*
	 * Offline parameters
	 */
	public int offline_partition = -1;
	public boolean now = false;
	
	public int pathNum = 0;

	/*
	 * The CRP of the node
	 */
	// public CacheRecencyProfile crp;

	/*
	 * The variability of the node
	 */
	public CacheVariabilityProfile cvp;
	Random rng;

	public List<List<Node>> allPaths = new ArrayList<>();
	public List<Long> allPathLength = new ArrayList<>();
	
	public long[] expectedETPerCore;
	 
	public Node(int layer, NodeType type, int id, int dagID, Random rng) {
		this(-1, layer, type, id, dagID, rng);
	}

	public Node(long WCET, int layer, NodeType type, int id, int dagID, Random rng) {
		this.WCET = WCET;
		this.layer = layer;
		this.type = type;

		this.id = id;
		this.dagID = dagID;

		this.successors = new ArrayList<>();
		this.predecessors = new ArrayList<>();

		// this.crp = new CacheRecencyProfile(this, SystemParameters.coreNum,
		// 1000);
		this.cvp = new CacheVariabilityProfile(this, SystemParameters.err_median,
				((double) rng.nextInt(SystemParameters.err_range + 1) / (double) 100) / 3.0, rng);
		this.rng = rng;
	}

	public Node deepCopy() {
		Node copy = new Node(this.WCET, this.layer, this.type, this.id, this.dagID, this.rng);
		copy.start = start;
		copy.finish = finish;
		copy.finishAt = finishAt;
		copy.partition = partition;
		copy.affinity = affinity;
		copy.delayed = delayed;
		return copy;
	}

	@Override
	public String toString() {
//		return "Node " + dagID + "_" + dagInstNo + "_" + id + ", C:" + WCET + " P:" + partition;
		return "Node " + dagID + "_" + id + ", C:" + WCET + ", in: " + predecessors.size() + ", out: "
				+ successors.size() + ", in+out: " + predecessors.size() + successors.size() + ", pathNum: " + pathNum;
//		return "Node " + dagID + "_" + dagInstNo + "_" + id + ", C:" + WCET + ", P:" + partition + ", A:" + affinity;
	}

	public String getFullName() {
		return "N " + dagID + "_" + dagInstNo + "_" + id;
	}

	public String getShortName() {
		return "N_" + id;
	}

	public String getExeInfo() {
		return "Node " + dagID + "-" + dagInstNo + "_" + id + ": " + WCET + ", starts: " + start + ", finish: "
				+ finishAt + ", duration: " + (finishAt - start) + ", partition: " + partition + ", affinity: "
				+ affinity;
	}

	public void printExeInfo(String prefix) {
		System.out.printf(prefix
				+ " Node %2d_%2d_%2d    ---    WCET: %5d, starts: %5d, finishes: %5d, duration: %5d, partition: %2d, affinity: %2d\n",
				dagID, dagInstNo, id, WCET, start, finishAt, (finishAt - start), partition, affinity);
	}

	/*
	 * Return the WORST-CASE execution time of the node
	 */
	public long getWCET() {
		return WCET;
	}

	public void setWCET(long wCET) {
		WCET = wCET;
	}

	public int getLayer() {
		return layer;
	}

	public NodeType getType() {
		return type;
	}

	public void addChildren(Node n) {
		successors.add(n);
	}

	public List<Node> getChildren() {
		return successors;
	}

	public List<Node> getParent() {
		return predecessors;
	}

	public void addParent(Node n) {
		predecessors.add(n);
	}

	public int getDagID() {
		return dagID;
	}

	public int getId() {
		return id;
	}

	public int getDagInstNo() {
		return dagInstNo;
	}

	public void setDagInstNo(int dagInstNo) {
		this.dagInstNo = dagInstNo;
	}

}