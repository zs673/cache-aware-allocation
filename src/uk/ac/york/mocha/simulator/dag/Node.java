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

	private final int id;
	private final int dagID;
	private int dagInstNo = Integer.MIN_VALUE;

	private final int layer;
	private final NodeType type;

	private long WCET;
	private long BCET;
	private long MCET;

//	private long WCETinRange;

	private List<Node> successors;
	private List<Node> predecessors;

	private List<Node> anc;
	private List<Node> des;
	private List<Node> con;

	public int priority = -1;

	public long finishWithNoP = -1;

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

	Random rng;

	/*
	 * Offline parameters
	 */
	public int offline_partition = -1;
	public boolean now = false;

	public long finishWDM = 0;

	public Node(int layer, NodeType type, int id, int dagID, Random rnd) {
		this(-1, layer, type, id, dagID, rnd);
	}

	public Node(long WCET, int layer, NodeType type, int id, int dagID, Random rng) {
		this.WCET = WCET;
//		System.out.println("WCET: " +  this.WCET);

		this.layer = layer;
		this.type = type;

		this.id = id;
		this.dagID = dagID;

		this.successors = new ArrayList<>();
		this.predecessors = new ArrayList<>();
		
		

		this.rng = rng;
	}
	
	public void constrcutRelations(List<Node> all) {
		getAnc();
		getDes();
		getCon(all);
	}

	private void getAnc() {
		this.anc = new ArrayList<>(this.predecessors);

		int sizeBefore = anc.size();
		int sizeAfter = 0;
		while (sizeBefore != sizeAfter) {
			sizeBefore = anc.size();

			for (int i=0; i<anc.size();i++) {
				for (Node p : anc.get(i).predecessors) {
					if (!anc.contains(p))
						anc.add(p);
				}
			}

			sizeAfter = anc.size();
		}

	}

	private void getDes() {
		this.des = new ArrayList<>(this.successors);

		int sizeBefore = des.size();
		int sizeAfter = 0;
		while (sizeBefore != sizeAfter) {
			sizeBefore = des.size();

			for (int i=0; i<des.size();i++) {
				for (Node p : des.get(i).successors) {
					if (!des.contains(p))
						des.add(p);
				}
			}

			sizeAfter = des.size();
		}

	}

	private void getCon(List<Node> all) {
		this.con = new ArrayList<>(all);
		con.removeAll(anc);
		con.removeAll(des);
	}

	public List<Node> getHighCon(){
		List<Node> highCon = new ArrayList<>();
		
		for(Node n : con) {
			if(n.priority > this.priority)
				highCon.add(n);
		}
		
		return highCon;
	}
	
	/*
	 * Return the WORST-CASE execution time of the node
	 */
	public long getET(boolean worst, boolean inRange) {
		if (worst)
			return MCET;
		else {
			if (!inRange)
				return MCET;
			else {
				long realET = -1;
				switch (SystemParameters.etType) {
				case uniform:
					realET = BCET + Math.round((double) rng.nextInt(101) / (double) 100 * (double) (WCET - BCET));
					realET = realET == 0 ? 1 : realET;
				case normal:
					long std = WCET - MCET;
					if (std == 0)
						std = 1;

					realET = MCET + Math.round((double) rng.nextGaussian() * (double) std);
					realET = realET < 1 ? 1 : realET;
				default:
					break;
				}

				System.out.println("realET: " + realET);
				if (realET < 0) {
					System.out.println("realET is less than 1");
					System.exit(-1);
				}

				return realET;
			}
		}
	}

	public long getWCET() {
		return WCET;
	}

	public long getBCET() {
		return BCET;
	}

	public long getMCET() {
		return MCET;
	}

	public void setWCET(long WCET) {
		this.WCET = Math.round((double) WCET * 1.5);

		this.BCET = Math.round((double) WCET * (double) 0.5);
		this.MCET = WCET;// Math.round((double) (WCET + BCET) / (double) 2);

		if (this.WCET == 0)
			this.WCET = 1;
		if (this.BCET == 0)
			this.BCET = 1;
		if (this.MCET == 0)
			this.MCET = 1;

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
	
	public List<Node> getCon(){
		return con;
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

	@Override
	public String toString() {
		return "Node " + dagID + "_" + dagInstNo + "_" + id + ", C:" + WCET + ", P:" + partition + ", A:" + affinity;
	}

	public String getFullName() {
		return "N " + dagID + "_" + dagInstNo + "_" + id;
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

}