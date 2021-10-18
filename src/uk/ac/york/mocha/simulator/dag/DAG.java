package uk.ac.york.mocha.simulator.dag;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import uk.ac.york.mocha.simulator.dag.Node.NodeType;
import uk.ac.york.mocha.simulator.parameters.SchedulingParameters;
import uk.ac.york.mocha.simulator.parameters.StructuralParameters;
import uk.ac.york.mocha.simulator.parameters.SystemParameters;
import uk.ac.york.mocha.simulator.parameters.SystemParameters.DagType;

public class DAG implements Serializable {

	private static final long serialVersionUID = -4076503208112904549L;

	public final int id;
	public int instanceNo = -1;
	public long totalInstNum = -1;
	public final String name;

	/* Shceduling parameters of Sporadic DAG task */
	private final SchedulingParameters sched_param;

	/* Structual parameters of Sporadic DAG task */
	private final StructuralParameters dag_param;

	/* DAG parameters */
	private Node source;
	private Node sink;
	private int nodeNum;
	private int layers;

	/* Self-defined list structure */
	private List<Node> flatNodes;
	private List<List<Node>> layeredNodes;
	private List<ImmutablePair<Node, Node>> edges;

	/* Third party graph structure */
	private Graph<Node, DefaultEdge> graph;

	private List<Node> longestPath;
	private List<Node> shortestPath;

	private Random rng;
	private boolean instantiated = false;

	/*
	 * Run-time parameters
	 */
	public long releaseTime = Long.MAX_VALUE;
	public long startTime = Long.MAX_VALUE;
	public long finishTime = Long.MAX_VALUE;
	public List<Node> allocNodes = new ArrayList<>();

	public List<ExecutionBlock> wdm = new ArrayList<>();

	/*
	 * Offline parameters
	 */
	public boolean hard = false;

	public DAG(SchedulingParameters sched_param, StructuralParameters dag_param, int id, int seed, boolean hard,
			DagType type) {

		this.id = id;
		this.name = "DAG " + id;

		this.rng = new Random(seed);

		this.sched_param = sched_param;
		this.dag_param = dag_param;

		this.layeredNodes = new ArrayList<>();
		this.edges = new ArrayList<>();

		this.layers = dag_param.getLayers();

		this.graph = new DefaultDirectedGraph<Node, DefaultEdge>(DefaultEdge.class);

		this.longestPath = new ArrayList<>();
		this.shortestPath = new ArrayList<>();

		switch (type) {
		case Random:
			constructDAG();
			break;
		case NFG:
			constructNFGDAG(layers);
			break;
		case Huawei:
			constructHuaweiDAG(layers);
			break;

		default:
			break;
		}

		this.hard = hard;
	}

	public void reset() {
		this.startTime = Long.MAX_VALUE;
		this.finishTime = Long.MAX_VALUE;
		this.allocNodes.clear();

		for (Node n : flatNodes) {
			n.start = -1;
			n.finish = false;
			n.finishAt = -1;
			n.partition = -1;
			n.affinity = -1;
			n.delayed = -1;
		}

	}

	/*****************************************************************
	 ******* Get Mutliple instances of one sporadic DAG task ********* NOTE: This method
	 * can only be invoked once! ***********
	 *****************************************************************/
	public List<DAG> getInstances(long instanceNum) {

		/*
		 * Check whether it is the FIRST call
		 */
		if (instantiated) {
			System.err.println("Error: DirectedAcyclicGraph.getInstances() can only be invoked once! ");
			System.exit(-1);
			return null;
		} else
			instantiated = true;

		/*
		 * Deep copy the DAG
		 */
		List<DAG> instances = new ArrayList<>();
		for (int i = 0; i < instanceNum; i++) {
			DAG ins = deepCopy();
			ins.instanceNo = i;
			ins.releaseTime = i * sched_param.getPeriod();

			ins.instantiated = true;

			for (Node n : ins.flatNodes)
				n.setDagInstNo(i);

			instances.add(ins);
		}

		return instances;
	}

	/*******************************************************************
	 ******************** Generate DAG structure *********************** This method does not depend on
	 * external library! ********
	 *******************************************************************/
	public void constructDAG() {
		if (layeredNodes.size() > 0) {
			System.err.println("The DAG task has already being generated!");
			return;
		}

		int nodeCounter = 1;

		/*
		 * Helper lists for constructing edges
		 */
		List<Node> parents = new ArrayList<>(); // potential parent nodes for a
												// layer
		List<Node> childless = new ArrayList<>(); // nodes without successors
		List<Node> orphans = new ArrayList<>(); // nodes without predecessors

		/*
		 * initialize source and sink node
		 */
		this.source = new Node(0, NodeType.SOURCE, 0, id, rng);
		// this.graph.addVertex(source);

		List<Node> firstLayer = new ArrayList<>();
		firstLayer.add(source);
		this.layeredNodes.add(firstLayer);

		parents = firstLayer;
		childless.addAll(parents);

		/*
		 * constrcut the DAG layer by layer
		 * 
		 */

		// System.out.println(layers);

		for (int l = 1; l < layers - 1; l++) {

			/*
			 * generate nodes for this layer
			 */
			int nodeNum = dag_param.parallelism_max == dag_param.parallelism_min ? dag_param.parallelism_min
					: rng.nextInt(dag_param.parallelism_max - dag_param.parallelism_min) + dag_param.parallelism_min;
			// int nodeNum = rng.nextInt(dag_param.getParallelism()) + 1;
//			System.out.println(nodeNum);
			List<Node> nodePerLayer = new ArrayList<>();

			for (int k = 0; k < nodeNum; k++) {
				Node n = new Node(l, NodeType.NORMAL, nodeCounter, id, rng);

				nodePerLayer.add(n);
				nodeCounter++;
			}

			this.layeredNodes.add(nodePerLayer);
			orphans.addAll(nodePerLayer);

			/*
			 * generate edges for these nodes
			 */
			for (Node n : nodePerLayer) {

				/*
				 * generate edges for these nodes
				 */
				for (Node parent : parents) {
					if (rng.nextDouble() <= this.dag_param.getConnect_prob()) {
						/*
						 * connect two nodes
						 */
						ImmutablePair<Node, Node> e = new ImmutablePair<Node, Node>(parent, n);

						edges.add(e);

						parent.addChildren(n);
						n.addParent(parent);

						/*
						 * update the lists of orphan and childless nodes
						 */
						if (orphans.contains(n))
							orphans.remove(n);
						if (childless.contains(parent))
							childless.remove(parent);
					}
				}
			}

			/* update parents for the next layer */
			parents = nodePerLayer;
			childless.addAll(parents);

			/* connect orphans to the source node */
			for (Node n : orphans) {
				ImmutablePair<Node, Node> e = new ImmutablePair<Node, Node>(source, n);

				edges.add(e);
				source.addChildren(n);
				n.addParent(source);
			}
			orphans.clear();

		}

		/* generate the sink node */
		this.sink = new Node(layers + 1, NodeType.SINK, nodeCounter, id, rng); // TODO
																				// add
																				// links,
																				// make
																				// it
																				// random.
		List<Node> lastLayer = new ArrayList<>();
		lastLayer.add(sink);
		this.layeredNodes.add(lastLayer);

		/*
		 * Link all childless nodes to the sink node
		 */
		for (Node n : childless) {
			ImmutablePair<Node, Node> e = new ImmutablePair<Node, Node>(n, sink);
			edges.add(e);
			n.addChildren(sink);
			sink.addParent(n);
		}
		childless.clear();

		this.flatNodes = layeredNodes.stream().flatMap(s -> s.stream()).collect(Collectors.toList());
		flatNodes.sort((n1, n2) -> Integer.compare(n1.getId(), n2.getId()));
		this.nodeNum = flatNodes.size();

		for (Node n : flatNodes) {
			this.graph.addVertex(n);
			n.constrcutRelations(flatNodes);
		}

		for (ImmutablePair<Node, Node> e : edges)
			this.graph.addEdge(e.left, e.right);
	}

	public void constructHuaweiDAG(int layers) {

		int nodeCounter = 1;

		/*
		 * Helper lists for constructing edges
		 */
		List<Node> parents = new ArrayList<>(); // potential parent nodes for a
												// layer
		List<Node> nodeCurrentLayer = new ArrayList<>();
		/*
		 * initialize source and sink node
		 */
		this.source = new Node(0, NodeType.SOURCE, 0, id, rng);

		List<Node> firstLayer = new ArrayList<>();
		firstLayer.add(source);
		this.layeredNodes.add(firstLayer);

		parents = firstLayer;

		/*
		 * constrcut the DAG layer by layer
		 */
		for (int l = 1; l < layers * 2; l++) {

			if (l % 2 == 1) {
				/*
				 * generate nodes for fan-in
				 */
				int nodeNum = rng.nextInt(dag_param.parallelism_max - dag_param.parallelism_min)
						+ dag_param.parallelism_min;
				List<Node> nodePerLayer = new ArrayList<>();

				for (int k = 0; k < nodeNum; k++) {
					Node n = new Node(l, NodeType.NORMAL, nodeCounter, id, rng);

					nodePerLayer.add(n);

				}

				this.layeredNodes.add(nodePerLayer);
				nodeCurrentLayer = nodePerLayer;
			} else {
				/*
				 * generate nodes for fan-out
				 */
				int nodeNum = 1;
				List<Node> nodePerLayer = new ArrayList<>();

				for (int k = 0; k < nodeNum; k++) {
					Node n = new Node(l, NodeType.NORMAL, nodeCounter, id, rng);

					nodePerLayer.add(n);

				}

				this.layeredNodes.add(nodePerLayer);
				nodeCurrentLayer = nodePerLayer;
			}

			for (Node n : nodeCurrentLayer) {
				for (Node parent : parents) {
					ImmutablePair<Node, Node> e = new ImmutablePair<Node, Node>(parent, n);

					edges.add(e);

					parent.addChildren(n);
					n.addParent(parent);
				}
			}

			/* update parents for the next layer */
			parents = nodeCurrentLayer;
			nodeCounter++;
		}

		/* generate the sink node */
		this.sink = new Node(layers * 2 + 1, NodeType.SINK, nodeCounter, id, rng); // TODO
																					// add
																					// links,
																					// make
																					// it
																					// random.
		List<Node> lastLayer = new ArrayList<>();
		lastLayer.add(sink);
		this.layeredNodes.add(lastLayer);

		/*
		 * Link all childless nodes to the sink node
		 */
		for (Node n : parents) {
			ImmutablePair<Node, Node> e = new ImmutablePair<Node, Node>(n, sink);
			edges.add(e);
			n.addChildren(sink);
			sink.addParent(n);
		}

		this.flatNodes = layeredNodes.stream().flatMap(s -> s.stream()).collect(Collectors.toList());
		flatNodes.sort((n1, n2) -> Integer.compare(n1.getId(), n2.getId()));
		this.nodeNum = flatNodes.size();

		for (Node n : flatNodes) {
			this.graph.addVertex(n);
			n.constrcutRelations(flatNodes);
		}

		for (ImmutablePair<Node, Node> e : edges)
			this.graph.addEdge(e.left, e.right);
	}

	public void constructNFGDAG(int layers) {

		int nodeCounter = 1;

		/*
		 * Helper lists for constructing edges
		 */
		List<Node> parents = new ArrayList<>(); // potential parent nodes for a
												// layer
		List<Node> nodeCurrentLayer = new ArrayList<>();
		/*
		 * initialize source and sink node
		 */
		this.source = new Node(0, NodeType.SOURCE, 0, id, rng);

		List<Node> firstLayer = new ArrayList<>();
		firstLayer.add(source);
		this.layeredNodes.add(firstLayer);

		parents = firstLayer;

		/*
		 * constrcut the DAG layer by layer
		 */
		for (int l = 1; l < layers * 2; l++) {

			if (l % 2 == 1) {
				/*
				 * generate nodes for fan-in
				 */
				int nodeNum = rng.nextInt(dag_param.parallelism_max - dag_param.parallelism_min)
						+ dag_param.parallelism_min;
				List<Node> nodePerLayer = new ArrayList<>();

				for (int k = 0; k < nodeNum; k++) {
					Node n = new Node(l, NodeType.NORMAL, nodeCounter, id, rng);

					nodePerLayer.add(n);
					nodeCounter++;
				}

				this.layeredNodes.add(nodePerLayer);
				nodeCurrentLayer = nodePerLayer;
			} else {
				/*
				 * generate nodes for fan-out
				 */
				int nodeNum = 1;
				List<Node> nodePerLayer = new ArrayList<>();

				for (int k = 0; k < nodeNum; k++) {
					Node n = new Node(l, NodeType.NORMAL, nodeCounter, id, rng);

					nodePerLayer.add(n);
					nodeCounter++;
				}

				this.layeredNodes.add(nodePerLayer);
				nodeCurrentLayer = nodePerLayer;
			}

			for (Node n : nodeCurrentLayer) {
				for (Node parent : parents) {
					ImmutablePair<Node, Node> e = new ImmutablePair<Node, Node>(parent, n);

					edges.add(e);

					parent.addChildren(n);
					n.addParent(parent);
				}
			}

			/* update parents for the next layer */
			parents = nodeCurrentLayer;
		}

		/* generate the sink node */
		this.sink = new Node(layers * 2 + 1, NodeType.SINK, nodeCounter, id, rng); // TODO
																					// add
																					// links,
																					// make
																					// it
																					// random.
		List<Node> lastLayer = new ArrayList<>();
		lastLayer.add(sink);
		this.layeredNodes.add(lastLayer);

		/*
		 * Link all childless nodes to the sink node
		 */
		for (Node n : parents) {
			ImmutablePair<Node, Node> e = new ImmutablePair<Node, Node>(n, sink);
			edges.add(e);
			n.addChildren(sink);
			sink.addParent(n);
		}

		this.flatNodes = layeredNodes.stream().flatMap(s -> s.stream()).collect(Collectors.toList());
		flatNodes.sort((n1, n2) -> Integer.compare(n1.getId(), n2.getId()));
		this.nodeNum = flatNodes.size();

		for (Node n : flatNodes) {
			this.graph.addVertex(n);
			n.constrcutRelations(flatNodes);
		}

		for (ImmutablePair<Node, Node> e : edges)
			this.graph.addEdge(e.left, e.right);
	}

	int[] path; // used to store temporary path
	int[] bestPath; // used to store temporary path

	int length = 0; // length of the path
	int bestLength = 0; // length of the longest path

	long distance = 0; // distance of the path
	long bestDistance; // distance of the longest path

	int[] visited; // used to mark a node as visited

	public void findPath(boolean longest) {
		visited = new int[flatNodes.size()];

		path = new int[layers];
		bestPath = new int[layers];

		/*
		 * compute a longest path from begin to end
		 */
		bestDistance = longest ? Long.MIN_VALUE : Long.MAX_VALUE;
		dsf(source, longest);
		if (bestDistance != Long.MIN_VALUE && bestDistance != Long.MAX_VALUE) {
			for (Integer id : bestPath) {
				if (id != -1) {
					if (longest)
						longestPath.add(flatNodes.get(id));
					else
						shortestPath.add(flatNodes.get(id));
				}

			}

			// System.out.println("Critical Path: " + (longest?
			// Arrays.toString(longestPath.toArray()):
			// Arrays.toString(shortestPath.toArray())));
			// System.out.println("distance: " + bestDistance + " edges: " +
			// bestLength);
		}

		else {
			System.err.println("Did not find critical path");
			System.exit(-1);
		}
	}

	private void dsf(Node current, boolean longest) {
		visited[current.getId()] = 1;
		path[length++] = current.getId();

		if (current.getId() == sink.getId()) {

			// System.out.println("Path: " +
			// Arrays.toString(Arrays.copyOfRange(path, 0, length)));

			if ((longest && distance > bestDistance) || (!longest && distance < bestDistance)) {

				// System.out.println("previous longest path: " +
				// Arrays.toString(bestPath) + " length: " + bestDistance);

				for (int i = 0; i < length; i++)
					bestPath[i] = path[i];
				for (int i = length; i < bestPath.length; i++)
					bestPath[i] = -1;

				bestLength = length;
				bestDistance = distance;

				// System.out.println("previous longest path: " +
				// Arrays.toString(bestPath) + " length: " + bestDistance);
				//
			}
		} else {
			List<Node> succ = current.getChildren();

			for (int i = 0; i < succ.size(); i++) {
				Node suc = succ.get(i);

				if (visited[suc.getId()] == 0) {
					distance += current.getET(SystemParameters.useWCET, false);
					dsf(suc, longest);
					distance -= current.getET(SystemParameters.useWCET, false);
				}
			}
		}

		visited[current.getId()] = 0;
		length--;
	}

	private int getAactiveNodoNumber(long time) {

		int nodesNum = 0;
		
		for (Node n : flatNodes) {

			long startTime = n.finishWithNoP - n.getWCET();
			long finishTime = n.finishWithNoP;

			if (startTime <= time && finishTime > time) {
				nodesNum++;
			}
		}

		return nodesNum;

	}

//	public void getWDM() {
//		List<ExecutionBlock> wdm = new ArrayList<>();
//
//		List<Node> nodes = this.flatNodes;
//
//		for (int i = 0; i < nodes.size(); i++) {
//			Node n = nodes.get(i);
//
//			List<Node> parents = n.getParent();
//
//			long start = 0;
//			for (Node parent : parents) {
//				if (start < parent.finishWDM)
//					start = parent.finishWDM;
//			}
//
//			n.finishWDM = start + n.getWCET();
//		}
//
//		int systemTime = 0;
//
//		int ebID = 0;
//		long heightTracker = 1;
//		long widthTracker = 0;
//
//		while (getAactiveNodoNumber(systemTime) > 0) {
//			int activeNodeNum = getAactiveNodoNumber(systemTime);
//
//			if (activeNodeNum == heightTracker) {
//				widthTracker++;
//				systemTime++;
//			} else {
//				ExecutionBlock eb = new ExecutionBlock(ebID, widthTracker, heightTracker, systemTime - widthTracker);
//
//				if (wdm.size() > 1 && eb.start != wdm.get(wdm.size() - 1).end) {
//					System.out.println("DirectedAcyclicGraph.getWDM()");
//					System.exit(-1);
//				}
//
//				wdm.add(eb);
//
//				ebID++;
//				widthTracker = 1;
//
//				heightTracker = getAactiveNodoNumber(systemTime);
//				systemTime++;
//			}
//		}
//
//		ExecutionBlock eb = new ExecutionBlock(ebID, widthTracker, heightTracker, systemTime - widthTracker);
//		wdm.add(eb);
//
//		this.wdm = new ArrayList<>(wdm);
//	}

	public List<ExecutionBlock> getPWDM(int coreNum) {
		
		getFinishTimeForAllNodes(coreNum);
		
		List<ExecutionBlock> pwdm = new ArrayList<>();

		
		long lastfinish = flatNodes.get(flatNodes.size()-1).finishWithNoP;
		long systemTime = 0;

		int ebID = 0;
		long heightTracker = 1;
		long widthTracker = 0;

		while (systemTime <= lastfinish || getAactiveNodoNumber(systemTime) > 0) {
			int activeNodeNum = getAactiveNodoNumber(systemTime);

			if (activeNodeNum == heightTracker) {
				widthTracker++;
				systemTime++;
			} else {
				ExecutionBlock eb = new ExecutionBlock(ebID, widthTracker, heightTracker, systemTime - widthTracker);

				if (pwdm.size() > 1 && eb.start != pwdm.get(pwdm.size() - 1).end) {
					System.out.println("DirectedAcyclicGraph.getWDM()");
					System.exit(-1);
				}

				pwdm.add(eb);

				ebID++;
				widthTracker = 1;

				heightTracker = getAactiveNodoNumber(systemTime);
				systemTime++;
			}
		}

		ExecutionBlock eb = new ExecutionBlock(ebID, widthTracker, heightTracker, systemTime - widthTracker);
		pwdm.add(eb);
		
		for(int i=0; i<pwdm.size();i++) {
			if(pwdm.get(i).height == 0) {
				pwdm.remove(pwdm.get(i));
				i--;
			}
		}

//		System.out.println("*** CoreNum: " + coreNum);
//		for(ExecutionBlock eeb : pwdm) {
//			System.out.println("EB" + id + ": " + eeb.height);
//		}
		
//		int id = 0;
//
//		for (ExecutionBlock eb : wdm) {
//			if (eb.height <= coreNum) {
//				pwdm.add(new ExecutionBlock(id, eb.width, eb.height, eb.start));
//				id++;
//			} else {
//				long blockNum = eb.height / coreNum;
//				long lastblockHeight = eb.height % coreNum;
//
//				int i = 0;
//				for (; i < blockNum; i++) {
//					pwdm.add(new ExecutionBlock(id, eb.width, coreNum, eb.start + eb.width * (i)));
//					id++;
//				}
//
//				if (lastblockHeight > 0) {
//					pwdm.add(new ExecutionBlock(id, eb.width, lastblockHeight, eb.start + eb.width * (i)));
//					id++;
//				}
//			}
//
//		}
//
//		for (int i = 0; i < pwdm.size() - 1; i++) {
//			ExecutionBlock eb1 = pwdm.get(i);
//			ExecutionBlock eb2 = pwdm.get(i + 1);
//
//			eb2.start = eb1.end;
//			eb2.end = eb2.start + eb2.width;
//		}
		
		
		
		return pwdm;
	}

	private void getFinishTimeForAllNodes(int NoP) {

		for (Node n : flatNodes)
			n.finishWithNoP = -1;

		for (Node n : flatNodes) {
			if(n.finishWithNoP == -1)
				getFinish(n, NoP);
		}

	}

	private long getFinish(Node n, int NoP) {
		for (Node p : n.getParent()) {
			if (p.finishWithNoP == -1) {
				getFinish(p, NoP);
			}
		}

		long start = n.getParent().size()==0? 0 : n.getParent().stream().mapToLong(c -> c.finishWithNoP).max().getAsLong();
		
		if(start <0) {
			System.err.println("start time less than 0!");
			System.exit(-1);
		}

		if (n.getHighCon().size() <= NoP) {
			long finish = start + n.getWCET();

			n.finishWithNoP = finish;
			return finish;
		} else {
			long intraInterference = (long) Math
					.ceil((double) n.getHighCon().stream().mapToLong(c -> c.getWCET()).sum() / (double) NoP);
			long finish = start + n.getWCET() + intraInterference;

			n.finishWithNoP = finish;
			return finish;
		}
	}

	public Node getSource() {
		return source;
	}

	public Node getSink() {
		return sink;
	}

	public SchedulingParameters getSchedParameters() {
		return sched_param;
	}

	public StructuralParameters getDAGParameters() {
		return dag_param;
	}

	public int getNodeNum() {
		return nodeNum;
	}

	public List<Node> getFlatNodes() {
		return flatNodes;
	}

	public String getName() {
		return name + "-" + instanceNo;
	}

	/*****************************************************************
	 ************** A good way to deep copy an object ****************
	 *****************************************************************/
	public DAG deepCopy() {

		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(this);

			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bais);

			DAG dag = (DAG) ois.readObject();

			oos.flush();
			baos.flush();

			baos.close();
			oos.close();
			bais.close();
			ois.close();

			return dag;
		} catch (EOFException eof) {
			eof.printStackTrace();
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String toString() {
		String out = "************************************************************************************************\n";
		out += getName() + ": \n";
		out += "Nodes by layer: \n";

		for (List<Node> nodesPerLayer : this.layeredNodes) {
			for (Node n : nodesPerLayer) {
				out += n.toString() + "    ";
			}
			out += "\n";
		}
		out += "\n";

		out += "Edges by Nodes: \n";

		for (List<Node> nodesPerLayer : this.layeredNodes) {
			for (Node n : nodesPerLayer) {
				List<ImmutablePair<Node, Node>> childrenEdge = new ArrayList<>();
				for (ImmutablePair<Node, Node> edge : edges) {
					if (edge.left.toString().equals(n.toString()))
						childrenEdge.add(edge);
				}
				out += n.toString() + "-> ";
				for (ImmutablePair<Node, Node> edge : childrenEdge) {
					out += edge.right.toString() + "  ";
				}
				out += "\n";
			}

		}

		out += "************************************************************************************************\n";

		return out;
	}

	public String printExeInfo() {
		String out = "************************************************************************************************\n";
		out += getName() + " starts at " + source.start + ", finishes at " + finishTime + " \n";
		out += "Nodes by layer: \n";

		for (List<Node> nodesPerLayer : this.layeredNodes) {
			for (Node n : nodesPerLayer) {
				out += n.getExeInfo() + "    \n";
			}
			out += "\n";
		}
		out += "\n";

		out += "************************************************************************************************\n";

		return out;
	}

	public static void main(String args[]) {
		int seed = 1000;
		Random rng = new Random(seed);

		for (int i = 0; i < 1; i++) {
			SchedulingParameters sched_param = new SchedulingParameters(10, 100, 100, 50000, 1, 0);
			StructuralParameters dag_param = new StructuralParameters(SystemParameters.maxLayer,
					SystemParameters.minLayer, SystemParameters.maxParal, SystemParameters.minParal,
					SystemParameters.connectProb, rng);
			DAG dag = new DAG(sched_param, dag_param, 0, seed, true, DagType.Huawei);

			System.out.println(dag.toString());

			System.out.println("\n\n------------------------------------------------------\n\n");

			List<DAG> instances = dag.getInstances(10);

			for (DAG d : instances)
				System.out.println(d.instanceNo + "   " + d.startTime);
		}

		System.out.println("finished");

	}
}