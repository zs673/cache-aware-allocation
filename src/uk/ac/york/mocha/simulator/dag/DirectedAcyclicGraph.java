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

import uk.ac.york.mocha.simulator.dag.Node.NodeType;
import uk.ac.york.mocha.simulator.parameters.SchedulingParameters;
import uk.ac.york.mocha.simulator.parameters.StructuralParameters;

public class DirectedAcyclicGraph implements Serializable {

	private static final long serialVersionUID = -4076503208112904549L;

	public final int id;
	public int instanceNo = Integer.MIN_VALUE;
	public final String name;

	/* Shceduling parameters of Sporadic DAG task */
	private final SchedulingParameters sched_param;

	/* Structual parameters of Sporadic DAG task */
	private final StructuralParameters dag_param;

	/* DAG parameters */
	private Node source;
	private Node sink;

	private List<Node> flatNodes;
	private List<List<Node>> nodes;
	private List<ImmutablePair<Node, Node>> edges;

	private int nodeNum;
	private int layers;

	private List<Node> criticalPath; // TODO: compute ciritcal path

	private Random rng;

	/*
	 * Run-time parameters
	 */
	public long startTime = 0;
	public long finishTime = Long.MAX_VALUE;

	private boolean instantiated = false;

	public DirectedAcyclicGraph(SchedulingParameters sched_param, StructuralParameters dag_param, int id, int seed) {

		this.id = id;
		this.name = "DAG task " + id;

		this.rng = new Random(seed);

		this.sched_param = sched_param;
		this.dag_param = dag_param;

		this.nodes = new ArrayList<>();
		this.edges = new ArrayList<>();

		this.layers = dag_param.getLayers();

		constructDAG();

		this.flatNodes = nodes.stream().flatMap(s -> s.stream()).collect(Collectors.toList());
		this.nodeNum = flatNodes.size();

		this.criticalPath = new ArrayList<>();

	}

	/*****************************************************************
	 ******* Get Mutliple instances of one sporadic DAG task *********
	 ********** NOTE: This method can only be invoked once! **********
	 *****************************************************************/
	public List<DirectedAcyclicGraph> getInstances(int instanceNum) {

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
		List<DirectedAcyclicGraph> instances = new ArrayList<>();
		for (int i = 0; i < instanceNum; i++) {
			DirectedAcyclicGraph ins = deepCopy();
			ins.instanceNo = i;
			ins.startTime = i * sched_param.getPeriod();

			for (Node n : ins.flatNodes)
				n.setDagInstNo(i);

			instances.add(ins);
		}

		return instances;
	}

	/******************************************************************
	 ******************** Generate DAG structure **********************
	 ******** This method does notdepend on external library! *********
	 ******************************************************************/
	private void constructDAG() {
		if (nodes.size() > 0) {
			System.err.println("The DAG task has already being generated!");
			return;
		}

		int nodeCounter = 1;

		/*
		 * Helper lists for constructing edges
		 */
		List<Node> parents = new ArrayList<>(); // potential parent nodes for a layer
		List<Node> childless = new ArrayList<>(); // nodes without successors
		List<Node> orphans = new ArrayList<>(); // nodes without predecessors

		/*
		 * initialize source and sink node
		 */
		this.source = new Node(0, NodeType.SOURCE, 0, id);

		List<Node> firstLayer = new ArrayList<>();
		firstLayer.add(source);
		this.nodes.add(firstLayer);

		parents = firstLayer;
		childless.addAll(parents);

		/*
		 * constrcut the DAG layer by layer
		 */
		for (int l = 1; l < layers - 1; l++) {

			/*
			 * generate nodes for this layer
			 */
			int nodeNum = rng.nextInt(dag_param.getParallelism()) + 1;
			List<Node> nodePerLayer = new ArrayList<>();

			for (int k = 0; k < nodeNum; k++) {
				Node n = new Node(l, NodeType.NORMAL, nodeCounter, id);
				nodePerLayer.add(n);
				nodeCounter++;
			}

			this.nodes.add(nodePerLayer);
			orphans.addAll(nodePerLayer);

			/*
			 * generate edges for these nodes
			 */
			for (Node n : nodePerLayer) {
				/*
				 * generate edges for these nodes
				 */
				for (Node parent : parents) {
					if (rng.nextDouble() < this.dag_param.getConnect_prob()) {
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
		this.sink = new Node(layers + 1, NodeType.SINK, nodeCounter, id); // TODO add links, make it random.
		List<Node> lastLayer = new ArrayList<>();
		lastLayer.add(sink);
		this.nodes.add(lastLayer);

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

	public List<Node> getCriticalPath() {
		return criticalPath;
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
	private DirectedAcyclicGraph deepCopy() {

		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(this);
			oos.flush();
			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bais);
			return (DirectedAcyclicGraph) ois.readObject();
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
		out += this.name + ": \n";
		out += "Nodes by layer: \n";

		for (List<Node> nodesPerLayer : this.nodes) {
			for (Node n : nodesPerLayer) {
				out += n.toString() + "    ";
			}
			out += "\n";
		}
		out += "\n";

		out += "Edges by Nodes: \n";

		for (List<Node> nodesPerLayer : this.nodes) {
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
		out += this.name + " starts at " + source.start + ", finishes at " + finishTime + " \n";
		out += "Nodes by layer: \n";

		for (List<Node> nodesPerLayer : this.nodes) {
			for (Node n : nodesPerLayer) {
				out += n.printExeInfo() + "    ";
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
			int minLayer = rng.nextInt(10);
			int maxLayer = minLayer + rng.nextInt(15) + 5;
			int parallelism = rng.nextInt(10) + 5;
			double connectProb = (double) rng.nextInt(9) / 10 + 0.2;

			SchedulingParameters sched_param = new SchedulingParameters(10, 100, 100, 50000, 1, 0);
			StructuralParameters dag_param = new StructuralParameters(maxLayer, minLayer, parallelism, connectProb,
					seed);
			DirectedAcyclicGraph dag = new DirectedAcyclicGraph(sched_param, dag_param, 0, rng.nextInt());

			System.out.println(dag.toString());

			System.out.println("\n\n------------------------------------------------------\n\n");

			List<DirectedAcyclicGraph> instances = dag.getInstances(10);

			for (DirectedAcyclicGraph d : instances)
				System.out.println(d.instanceNo + "   " + d.startTime);
		}

		System.out.println("finished");

	}
}