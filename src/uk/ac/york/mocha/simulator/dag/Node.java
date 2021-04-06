package uk.ac.york.mocha.simulator.dag;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Node implements Serializable {

	private static final long serialVersionUID = -6902741116934537381L;

	/* NodeType */
	public enum NodeType {
		SOURCE, SINK, NORMAL // TODO: a bit of more work could be done here to provide a finer-grained node
								// type classification
	}

	private final int id;
	private final int dagID;
	private int dagInstNo = Integer.MIN_VALUE;

	private final int layer;
	private final NodeType type;

	private long WCET;

	private List<Node> successors;
	private List<Node> predecessors;

	/*
	 * Simualtion parameters
	 */
	public long start = -1;
	public boolean finish = false;
	public long finishAt = -1;
	public int partition = -1;

	public Node(int layer, NodeType type, int id, int dagID) {
		this(-1, layer, type, id, dagID);
	}

	public Node(long WCET, int layer, NodeType type, int id, int dagID) {
		this.WCET = WCET;
		this.layer = layer;
		this.type = type;

		this.id = id;
		this.dagID = dagID;

		this.successors = new ArrayList<>();
		this.predecessors = new ArrayList<>();
	}

	public Node deepCopy() {
		Node copy = new Node(this.WCET, this.layer, this.type, this.id, this.dagID);
		copy.start = start;
		copy.finish = finish;
		copy.finishAt = finishAt;
		copy.partition = partition;
		return copy;
	}

	@Override
	public String toString() {
		return "Node " + dagID + "-" + dagInstNo + "_" + id + ": " + WCET;
	}

	public String getExeInfo() {
		return "Node " + dagID + "-" + dagInstNo + "_" + id + ": " + WCET + ", starts: " + start + ", finish: "
				+ finishAt + ", duration: " + (finishAt - start) + ", partition: " + partition; 
	}

	public void printExeInfo(String prefix) {
		System.out.printf(prefix+" Node %2d_%2d_%2d    ---    WCET: %5d, starts: %5d, finishes: %5d, duration: %5d, partition: %2d \n", dagID, dagInstNo, id, WCET, start,
				finishAt, (finishAt - start), partition);
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